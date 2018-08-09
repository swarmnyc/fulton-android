package com.swarmnyc.fulton.android.promise

import android.util.Log
import android.view.ViewParent
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

typealias ResolveAction<V> = (V) -> Unit
typealias RejectAction = (Throwable) -> Unit
typealias PromiseLambdaExecutor<V> = (resolve: ResolveAction<V>, reject: RejectAction) -> Unit
typealias PromiseExecutor<V> = (promise: Promise<V>) -> Unit

typealias ThenAction<V, R> = (V) -> R
typealias ThenChainAction<V, R> = (V) -> Promise<R>
typealias FailHandler = (Throwable) -> Unit

open class PromiseHandler<V>(val thenAction: ThenAction<V, *>?, val failHandler: FailHandler?)


/**
 * A simple Promise implementation based on https://www.promisejs.org/implementing/
 */
class Promise<V> {
    companion object {
        private const val TAG = "fulton.promise"
        var defaultOptions = PromiseOptions()

        var uncaughtError: RejectAction = {
            Log.e(TAG, "Promise uncaught error", it)
            throw it
        }

        fun <T> resolve(v: T): Promise<T> {
            return Promise<T>().also { promise ->
                promise.resolve(v)
            }
        }

        fun reject(e: Throwable): Promise<Any> {
            return Promise { promise ->
                promise.shouldThrowUncaughtError = false
                promise.reject(e)
            }
        }

        fun all(promises: Collection<Promise<*>>): Promise<Array<Any>> {
            return all(defaultOptions, *promises.toTypedArray())
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

    private constructor(options: PromiseOptions, parent: Promise<*>?) {
        this.options = options
        this.parent = parent

        if (parent == null) {
            log { "New" }
        } else {
            log { "New by ${parent.id}" }
        }
    }

    constructor(options: PromiseOptions, executor: PromiseLambdaExecutor<V>) : this(options, null) {
        this.executor = {
            executor(it::resolve, it::reject)
        }

        execute()
    }

    constructor(options: PromiseOptions, executor: PromiseExecutor<V>) : this(options, null) {
        this.executor = executor

        execute()
    }

    private constructor() : this(defaultOptions, null)
    constructor(executor: PromiseLambdaExecutor<V>) : this(defaultOptions, executor)
    constructor(executor: PromiseExecutor<V>) : this(defaultOptions, executor)

    private lateinit var options: PromiseOptions

    private var error: Throwable? = null
    private var executor: PromiseExecutor<V>? = null
    private var future: Future<*>? = null // only promise root has future
    private var handlers = mutableListOf<PromiseHandler<V>>()
    private val parent: Promise<*>?
    private var shouldThrowErrorOnCancel = false
    private var value: V? = null
    private var executedAt: Long = 0
    private val id: String by lazy {
        "Promise@${options.idCounter.incrementAndGet()}"
    }

    private var thenChainPromise: Promise<*>? = null

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
                    it(this)
                    log { "Executed" }
                } catch (e: InterruptedException) {
                    log { "Execution Interrupted" }
                } catch (e: Throwable) {
                    log { "Execution Failed, Error=$e" }
                    reject(e)
                }
            }
        }
    }

    val isDone: Boolean
        get() = state.get() != PromiseState.Pending.ordinal

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
                    handler.thenAction?.let {
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

    /**
     * Kill the thread if it is still running
     *
     * @param throwError is true, after the thread is killed, calling reject
     */
    fun cancel(throwError: Boolean = false) {
        log {
            buildString {
                append("Cancel Called, state=${PromiseState.valueOf(state.get())}, future=")

                if (future == null) {
                    append("null")
                } else {
                    future?.also {
                        append("isDone=${it.isDone},isCancelled=${it.isCancelled}")
                    }
                }
            }
        }

        if (state.get() != PromiseState.Pending.ordinal) return

        shouldThrowErrorOnCancel = throwError
        state.set(PromiseState.Canceled.ordinal)

        // also cancel child promise
        thenChainPromise?.also {
            it.cancel(throwError)
        }

        future?.also {
            if (!it.isDone || !it.isCancelled) {
                log { "Canceling" }

                it.cancel(true)

                if (shouldThrowErrorOnCancel) {
                    // sometime thread doesn't cause Interrupted Exception, so we call reject manually
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
    fun timeout(ms: Long, throwError: Boolean = false): Promise<V> {
        log { "Timeout Called, state=${PromiseState.valueOf(state.get())}" }
        if (state.get() != PromiseState.Pending.ordinal) return this

        this.options.executor.submit {
            Thread.sleep(ms)

            if (state.get() == PromiseState.Pending.ordinal) {
                log { "Timeout, canceling" }

                cancel(throwError)
            } else {
                log { "Timeout, Invalid, state=${PromiseState.valueOf(state.get())}" }
            }
        }

        return this
    }

    fun <R> then(thenAction: ThenAction<V, R>): Promise<R> {
        // the same thread
        return thenInternal(thenAction, null)
    }

    fun <R> thenUi(thenAction: ThenAction<V, R>): Promise<R> {
        return thenInternal(thenAction, options.uiExecutor)
    }

    private fun <R> thenInternal(thenAction: ThenAction<V, R>, threadExecutor: Executor?): Promise<R> {
        log { "Then Called" }
        return Promise<R>(options, this).also { promise ->
            this.handle(PromiseHandler({
                val action = Runnable {
                    try {
                        log { "ThenAction Called" }
                        promise.resolve(thenAction(it))
                    } catch (e: Throwable) {
                        log { "ThenAction Failed" }
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

    fun <R> thenChain(thenAction: ThenChainAction<V, R>): Promise<R> {
        return thenChainInternal(thenAction, null)
    }

    fun <R> thenChainUi(thenAction: ThenChainAction<V, R>): Promise<R> {
        return thenChainInternal(thenAction, options.uiExecutor)
    }

    private fun <R> thenChainInternal(thenAction: ThenChainAction<V, R>, threadExecutor: Executor?): Promise<R> {
        log { "ThenChain Called" }

        return Promise<R>(options, this).also { promise ->
            this.handle(PromiseHandler({ result ->
                val action = Runnable {
                    try {
                        promise.log { "ThenChainAction Called" }

                        val cp = thenAction(result)
                        promise.thenChainPromise = cp

                        cp.then {
                            promise.resolve(it)
                        }.catch {
                            promise.reject(it)
                        }
                    } catch (e: Throwable) {
                        promise.log { "ThenChainAction Failed" }

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

        return Promise<V>(this.options, this).also { promise ->
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
            val msg = "Thread: ${Thread.currentThread().id}, $id, ${block()}"

            Log.d(TAG, msg)
        }
    }
}