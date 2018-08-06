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
        var defaultOptions = PromiseOptions()

        var uncaughtError: Reject = {
            Log.e("fulton.api", "Promise uncaught error", it)
            throw it
        }

        fun <T> resolve(v: T): Promise<T> {
            return Promise { resolve, _ ->
                resolve(v)
            }
        }

        fun reject(e: Throwable): Promise<Any> {
            return Promise { _, reject, promise ->
                promise.shouldThrowError = false
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

    private var options: PromiseOptions
    private var executor: PromiseExecutor<V>? = null
    private var executorWithSelf: PromiseWithSelfExecutor<V>? = null
    private var shouldThrowError = true
    private var parent: Promise<*>? = null

    private constructor() {
        this.options = defaultOptions
    }

    constructor(executor: PromiseExecutor<V>) {
        this.options = defaultOptions
        this.executor = executor

        execute()
    }

    constructor(executor: PromiseWithSelfExecutor<V>) {
        this.options = defaultOptions
        this.executorWithSelf = executor

        execute()
    }

    private constructor(options: PromiseOptions) {
        this.options = options
    }

    constructor(options: PromiseOptions, executor: PromiseExecutor<V>) {
        this.options = options
        this.executor = executor

        execute()
    }

    constructor(options: PromiseOptions, executor: PromiseWithSelfExecutor<V>) {
        this.options = options
        this.executorWithSelf = executor

        execute()
    }

    internal var state = AtomicInteger(PromiseState.Pending.ordinal)

    private var value: V? = null
    private var error: Throwable? = null
    private var handlers = mutableListOf<PromiseHandler<V>>()
    private var future: Future<*>? = null

    private fun execute() {
        executor?.let {
            future = options.executor.submit {
                try {
                    it(::resolve, ::reject)
                } catch (e: Throwable) {
                    reject(e)
                }
            }
        }

        executorWithSelf?.let {
            future = options.executor.submit {
                try {
                    it(::resolve, ::reject, this)
                } catch (e: Throwable) {
                    reject(e)
                }
            }
        }
    }

    fun resolve(v: V) {
        if (state.get() != PromiseState.Pending.ordinal) return

        value = v
        state.set(PromiseState.Fulfilled.ordinal)

        handlers.forEach(::handle)

        handlers.clear()
    }

    fun reject(e: Throwable) {
        if (state.get() != PromiseState.Pending.ordinal) return

        error = e
        state.set(PromiseState.Rejected.ordinal)

        if (handlers.size == 0) {
            // no child
            if (shouldThrowError) {
                Promise.uncaughtError(e)
            }
        } else {
            handlers.forEach(::handle)
            handlers.clear()
        }
    }


    private fun handle(handler: PromiseHandler<V>) {
        when (state.get()) {
            PromiseState.Pending.ordinal -> handlers.add(handler)
            PromiseState.Fulfilled.ordinal -> {
                try {
                    handler.thenHandler?.let {
                        it(value!!)
                    }
                } catch (e: Throwable) {
                    reject(e)
                }
            }
            PromiseState.Rejected.ordinal -> {
                handler.failHandler?.let {
                    it(error!!)
                }
            }
        }
    }

    fun cancel() {
        if (state.get() != PromiseState.Pending.ordinal) return

        state.set(PromiseState.Canceled.ordinal)

        future?.let {
            if (!it.isDone || !it.isCancelled) {
                it.cancel(true)
            }
        }

        parent?.cancel()
    }

    fun <R> then(thenHandler: ThenHandler<V, R>): Promise<R> {
        // the same thread
        return thenInternal(thenHandler, null)
    }

    fun <R> thenUi(thenHandler: ThenHandler<V, R>): Promise<R> {
        return thenInternal(thenHandler, options.uiExecutor)
    }

    private fun <R> thenInternal(thenHandler: ThenHandler<V, R>, threadExecutor: Executor?): Promise<R> {
        return Promise<R>(options).also { promise ->
            promise.parent = this
            this.handle(PromiseHandler({
                val action = Runnable {
                    try {
                        val r = thenHandler(it)
                        promise.resolve(r)
                    } catch (e: Throwable) {
                        promise.reject(e)
                    }
                }

                if (threadExecutor == null) {
                    action.run()
                } else {
                    threadExecutor.execute(action)
                }
            }, promise::reject))
        }
    }

    fun <R> thenChain(thenHandler: ThenChainHandler<V, R>): Promise<R> {
        return thenChainInternal(thenHandler, null)
    }

    fun <R> thenChainUi(thenHandler: ThenChainHandler<V, R>): Promise<R> {
        return thenChainInternal(thenHandler, options.uiExecutor)
    }

    private fun <R> thenChainInternal(thenHandler: ThenChainHandler<V, R>, threadExecutor: Executor?): Promise<R> {
        return Promise<R>(options).also { promise ->
            promise.parent = this
            this.handle(PromiseHandler({
                val action = Runnable {
                    try {
                        val p = thenHandler(it)
                        p.then(promise::resolve).catch(promise::reject)
                    } catch (e: Throwable) {
                        promise.reject(e)
                    }
                }

                if (threadExecutor == null) {
                    action.run()
                } else {
                    threadExecutor.execute(action)
                }

            }, promise::reject))
        }
    }

    fun catch(failHandler: FailHandler): Promise<V> {
        return catchInternal(failHandler, null)
    }

    fun catchUi(failHandler: FailHandler): Promise<V> {
        return catchInternal(failHandler, options.uiExecutor)
    }

    private fun catchInternal(failHandler: FailHandler, threadExecutor: Executor?): Promise<V> {
        return Promise<V>(this.options).also { promise ->
            promise.parent = this
            this.handle(PromiseHandler(null) {
                val action = Runnable {
                    try {
                        failHandler(it)
                        promise.shouldThrowError = false
                        promise.reject(it)
                    } catch (e: Throwable) {
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
}