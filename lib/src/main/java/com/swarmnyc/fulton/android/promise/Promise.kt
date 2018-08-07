package com.swarmnyc.fulton.android.promise

import android.util.Log
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

typealias Resolve<V> = (V) -> Unit
typealias Reject = (Throwable) -> Unit
typealias PromiseExecutor<V> = (resolve: Resolve<V>, reject: Reject) -> Unit
typealias PromiseWithSelfExecutor<V> = (resolve: Resolve<V>, reject: Reject, promise: Promise<V>) -> Unit

typealias ThenHandler<V, R> = (V) -> R
typealias ThenChainHandler<V, R> = (V) -> Promise<R>
typealias FailHandler = (Throwable) -> Unit

open class PromiseHandler<V>(val thenHandler: ThenHandler<V, *>?, val failHandler: FailHandler?)


/**
 * A simple Promise implementation based on https://www.promisejs.org/implementing/
 */
class Promise<V> {
    companion object {
        private const val TAG = "fulton.promise"
        var defaultOptions = PromiseOptions()

        var uncaughtError: Reject = {
            Log.e(TAG, "Promise uncaught error", it)
            throw it
        }

        fun <T> resolve(v: T): Promise<T> {
            return Promise { resolve, _ ->
                resolve(v)
            }
        }

        fun reject(e: Throwable): Promise<Any> {
            return Promise { _, reject, promise ->
                promise.shouldThrowUncaughtError = false
                reject(e)
            }
        }

        fun all(vararg promises: Promise<*>): Promise<Array<Any>> {
            return all(defaultOptions, *promises)
        }

        fun all(options: PromiseOptions, vararg promises: Promise<*>): Promise<Array<Any>> {
            return Promise(options) { resolve, reject ->
                val count = AtomicInteger(0)
                val result = Array<Any>(promises.size) {}

                promises.forEachIndexed { index, promise ->
                    promise.then {
                        result[index] = it!!

                        if (count.incrementAndGet() == promises.size) {
                            resolve(result)
                        }
                    }.catch {
                        reject(it)
                    }
                }
            }
        }

        fun race(vararg promises: Promise<*>): Promise<Any> {
            return race(defaultOptions, *promises)
        }

        fun race(options: PromiseOptions, vararg promises: Promise<*>): Promise<Any> {
            return Promise(options) { resolve, reject ->
                val send = AtomicBoolean(false)
                promises.forEach { promise ->
                    promise.then {
                        if (!send.getAndSet(true)) {
                            resolve(it!!)
                        }
                    }.catch {
                        if (!send.getAndSet(true)) {
                            reject(it)
                        }
                    }
                }
            }
        }
    }

    private constructor() {
        this.options = defaultOptions
        log { "New" }
    }

    constructor(executor: PromiseExecutor<V>) : this() {
        this.executor = executor

        execute()
    }

    constructor(executor: PromiseWithSelfExecutor<V>) : this() {
        this.executorWithSelf = executor

        execute()
    }

    private constructor(options: PromiseOptions) {
        this.options = options

        log { "New" }
    }

    constructor(options: PromiseOptions, executor: PromiseExecutor<V>) : this(options) {
        this.executor = executor

        execute()
    }

    constructor(options: PromiseOptions, executor: PromiseWithSelfExecutor<V>) : this(options) {
        this.executorWithSelf = executor

        execute()
    }

    private lateinit var options: PromiseOptions

    private var error: Throwable? = null
    private var executor: PromiseExecutor<V>? = null
    private var executorWithSelf: PromiseWithSelfExecutor<V>? = null
    private var future: Future<*>? = null // only promise root has future
    private var handlers = mutableListOf<PromiseHandler<V>>()
    private var parent: Promise<*>? = null
    private var shouldThrowErrorOnCancel = false
    private var value: V? = null
    private var executedAt: Long = 0
    private val id: String by lazy {
        "Promise@${options.idCounter.incrementAndGet()}"
    }

    internal var shouldThrowUncaughtError = true
    internal var state = AtomicInteger(PromiseState.Pending.ordinal)

    private fun execute() {
        log { "Execute Called" }

        executedAt = System.currentTimeMillis()
        executor?.let {
            future = options.executor.submit {
                // delay for the main thread add .then and .catch handler
                log { "Executing" }

                Thread.sleep(1)
                try {
                    it(::resolve, ::reject)
                    log { "Executed" }
                } catch (e: InterruptedException) {
                    log { "Execution Interrupted" }
                } catch (e: Throwable) {
                    log { "Execution Failed, Error=$e" }
                    reject(e)
                }
            }
        }

        executorWithSelf?.let {
            future = options.executor.submit {
                // delay for the main thread add .then and .catch handler
                log { "Executing" }

                Thread.sleep(1)
                try {
                    it(::resolve, ::reject, this)
                    log { "Executed" }
                } catch (e: InterruptedException) {
                    log { "Execution Interrupted" }
                } catch (e: Throwable) {
                    log { "Execution Failed" }
                    reject(e)
                }
            }
        }
    }

    fun resolve(v: V) {
        log { "Resolve Called, state: ${PromiseState.valueOf(state.get())}, handlers=${handlers.size}, value = $v" }

        if (state.get() != PromiseState.Pending.ordinal) return

        value = v
        state.set(PromiseState.Fulfilled.ordinal)

        handlers.forEach(::handle)

        handlers.clear()
    }

    fun reject(e: Throwable) {
        log {
            "Reject Called, state: ${PromiseState.valueOf(state.get())}, handlers=${handlers.size}, " +
                    "error = $e, shouldThrowErrorOnCancel=$shouldThrowErrorOnCancel, " +
                    "shouldThrowUncaughtError=$shouldThrowUncaughtError"
        }

        when (state.get()) {
            PromiseState.Canceled.ordinal -> {
                if (!shouldThrowErrorOnCancel) {
                    return
                }

                state.set(PromiseState.RejectedOnCancel.ordinal)
            }
            PromiseState.Pending.ordinal -> {
                state.set(PromiseState.Rejected.ordinal)
            }
            else -> return
        }

        error = e

        if (handlers.size == 0) {
            // no child
            if (shouldThrowUncaughtError) {
                log { "Reject, not child, throw uncaught error" }
                Promise.uncaughtError(e)
            }
        } else {
            handlers.forEach(::handle)
            handlers.clear()
        }
    }


    private fun handle(handler: PromiseHandler<V>) {
        when (state.get()) {
            PromiseState.Pending.ordinal -> {
                log { "Handle Pending, Add a handler" }
                handlers.add(handler)
            }
            PromiseState.Fulfilled.ordinal -> {
                log { "Handle Fulfilled" }
                try {
                    handler.thenHandler?.let {
                        it(value!!)
                    }
                } catch (e: Throwable) {
                    reject(e)
                }
            }
            PromiseState.Rejected.ordinal, PromiseState.RejectedOnCancel.ordinal -> {
                log { "Handle Rejected" }
                handler.failHandler?.let {
                    it(error!!)
                }
            }
        }
    }

    fun cancel(throwError: Boolean = false) {
        log { "Cancel Called, state=${PromiseState.valueOf(state.get())}" }
        if (state.get() != PromiseState.Pending.ordinal) return

        shouldThrowErrorOnCancel = throwError
        state.set(PromiseState.Canceled.ordinal)

        future?.let {
            if (!it.isDone || !it.isCancelled) {
                log { "Canceling" }

                it.cancel(true)

                if (shouldThrowErrorOnCancel) {
                    reject(InterruptedException())
                }

                log { "Canceled" }
            }
        }

        parent?.cancel(throwError)
    }


    /**
     * if the running time over the given time, cancel the promise
     */
    fun timeout(ms: Long) {

    }

    fun <R> then(thenHandler: ThenHandler<V, R>): Promise<R> {
        // the same thread
        return thenInternal(thenHandler, null)
    }

    fun <R> thenUi(thenHandler: ThenHandler<V, R>): Promise<R> {
        return thenInternal(thenHandler, options.uiExecutor)
    }

    private fun <R> thenInternal(thenHandler: ThenHandler<V, R>, threadExecutor: Executor?): Promise<R> {
        log { "Then Called" }
        return Promise<R>(options).also { promise ->
            promise.parent = this

            this.handle(PromiseHandler({
                val action = Runnable {
                    try {
                        log { "ThenHandler Called" }
                        val r = thenHandler(it)
                        promise.resolve(r)
                    } catch (e: Throwable) {
                        log { "ThenHandler Failed" }
                        promise.reject(e)
                    }
                }

                if (threadExecutor == null) {
                    action.run()
                } else {
                    threadExecutor.execute(action)
                }
            }, {
                log { "FailHandler Called" }
                promise.reject(it)
            }))
        }
    }

    fun <R> thenChain(thenHandler: ThenChainHandler<V, R>): Promise<R> {
        return thenChainInternal(thenHandler, null)
    }

    fun <R> thenChainUi(thenHandler: ThenChainHandler<V, R>): Promise<R> {
        return thenChainInternal(thenHandler, options.uiExecutor)
    }

    private fun <R> thenChainInternal(thenHandler: ThenChainHandler<V, R>, threadExecutor: Executor?): Promise<R> {
        log { "ThenChain Called" }

        return Promise<R>(options).also { promise ->
            promise.parent = this
            this.handle(PromiseHandler({
                val action = Runnable {
                    try {
                        promise.log { "ThenChainHandler Called" }

                        val p = thenHandler(it)
                        p.then(promise::resolve).catch(promise::reject)
                    } catch (e: Throwable) {
                        promise.log { "ThenChainHandler Failed" }

                        promise.reject(e)
                    }
                }

                if (threadExecutor == null) {
                    action.run()
                } else {
                    threadExecutor.execute(action)
                }
            }, {
                promise.log { "FailHandler Called" }
                promise.reject(it)
            }))
        }
    }

    fun catch(failHandler: FailHandler): Promise<V> {
        return catchInternal(failHandler, null)
    }

    fun catchUi(failHandler: FailHandler): Promise<V> {
        return catchInternal(failHandler, options.uiExecutor)
    }

    private fun catchInternal(failHandler: FailHandler, threadExecutor: Executor?): Promise<V> {
        log { "Catch Called" }

        return Promise<V>(this.options).also { promise ->
            promise.parent = this
            this.handle(PromiseHandler(null) {
                val action = Runnable {
                    try {
                        promise.log { "FailHandler Called" }
                        failHandler(it)
                        promise.shouldThrowUncaughtError = false
                        promise.reject(it)
                    } catch (e: Throwable) {
                        promise.log { "FailHandler failed" }
                        promise.reject(e)
                    }
                }

                if (threadExecutor == null) {
                    action.run()
                } else {
                    threadExecutor.execute(action)
                }
            })
        }
    }

    private inline fun log(block: () -> String) {
        if (options.debugMode) {
            val msg = "$id-${Thread.currentThread().id} : ${block()}"

            Log.d(TAG, msg)
        }
    }
}