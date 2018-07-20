package com.swarmnyc.fulton.android

import android.util.Log
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import com.swarmnyc.fulton.android.util.fromJson
import com.swarmnyc.fulton.android.util.toJson
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.thenApply

typealias ApiPromise<T> = Promise<T, ApiError>

typealias ApiDeferred<T> = Deferred<T, ApiError>

infix fun <V, R> ApiPromise<V>.then(bind: (V) -> ApiPromise<R>): ApiPromise<R> {
    val deferred = deferred<R, ApiError>(context)
    success {
        try {
            bind(it).success(deferred::resolve).fail(deferred::reject)
        } catch (e: Exception) {
            deferred.reject(ApiError(e))
        }
    }.fail(deferred::reject)

    deferred.promise.thenApply { }

    return deferred.promise
}

inline infix fun <V, R> ApiPromise<V>.thenApply(crossinline bind: V.() -> ApiPromise<R>): ApiPromise<R> {
    val deferred = deferred<R, ApiError>(context)
    success {
        try {
            bind(it).success(deferred::resolve).fail(deferred::reject)
        } catch (e: Exception) {
            deferred.reject(ApiError(e))
        }
    }.fail(deferred::reject)

    return deferred.promise
}

/**
 * Base Api Client, supports
 * - error handle
 * - async
 * - cache
 * */
abstract class ApiClient {
    companion object {
        const val TAG = "fulton.api"
        const val NO_CACHE = 0
    }

    protected abstract val urlRoot: String

    /**
     * init request, like set headers
     * */
    open fun initRequest(req: Request) {
    }

    protected inline fun <reified T : Any> request(builder: RequestOptions.() -> Unit): ApiPromise<T> {
        val options = RequestOptions()
        options.urlRoot = urlRoot
        options.dataType = T::class.java

        builder(options)

        options.buildUrl()
        options.buildDataType()

        return request(options)
    }

    protected fun <T : Any> request(options: RequestOptions): ApiPromise<T> {
        val promise = deferred<T, ApiError>()

        promise.promise.context.workerContext.offer {
            if (options.method == Method.GET && options.cacheDuration > NO_CACHE) {
                val cacheResult = Fulton.context.cacheManagement.get<T>(options.url!!, options.dataType!!)
                if (cacheResult != null) {
                    // cache hits
                    promise.resolve(cacheResult)

                    return@offer
                }
            }

            startRequest(promise, options)
        }

        return promise.promise
    }

    protected open fun <T> startRequest(promise: ApiDeferred<T>, options: RequestOptions) {
        val req = FuelManager.instance.request(options.method, options.url!!)
        if (options.body != null) {
            req.body(options.body!!.toJson())
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "--> ${req.method} (${req.url})\n$req")
        }

        req.timeout(options.timeOut).response { _, res, result ->
            handleResponse(promise, options, req, res, result)
        }
    }

    protected open fun <T> handleResponse(promise: ApiDeferred<T>, options: RequestOptions, req: Request, res: Response, result: Result<ByteArray, FuelError>) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            val time = (System.currentTimeMillis() - options.startedAt)
            val msg = buildString {
                appendln("<-- ${res.statusCode} (${res.url})")
                appendln("Time Spent : $time")
                appendln("Response : ${res.responseMessage}")
                appendln("Length : ${res.contentLength}")
                appendln("Headers : (${res.headers.size})")
                for ((key, value) in res.headers) {
                    appendln("$key : $value")
                }
                appendln("Body : ${if (res.data.isNotEmpty()) String(res.data) else "(empty)"}")
            }

            Log.d(TAG, msg)
        }

        if (res.statusCode < 400) {
            try {
                handleSuccess(promise, options, res, result.get())
            } catch (e: Throwable) {
                handelError(promise, req, res, result.component2()!!)
            }
        } else {
            handelError(promise, req, res, result.component2()!!)
        }
    }

    protected open fun <T> handleSuccess(promise: ApiDeferred<T>, options: RequestOptions, res: Response, bytes: ByteArray) {
        var shouldCache = options.method == Method.GET && options.cacheDuration > NO_CACHE

        val result: T = when (options.dataType) {
            Unit::class.java, Nothing::class.java -> {
                shouldCache = false
                @Suppress("UNCHECKED_CAST")
                Unit as T
            }
            String::class.java -> {
                @Suppress("UNCHECKED_CAST")
                String(bytes) as T
            }
            else -> {
                bytes.fromJson(options.dataType!!)
            }
        }

        if (shouldCache) {
            cacheData(options.url!!, options.cacheDuration, bytes)
        }

        promise.resolve(result)
    }

    protected open fun <T> handelError(promise: ApiDeferred<T>, req: Request, res: Response, error: FuelError) {
        val apiError = ApiError(req, res, error.exception)

        try {
            promise.promise.fail {
                Fulton.context.errorHandler.onError(apiError)
            }

            promise.reject(apiError)
        } catch (e: Exception) {
            throw Exception("Error Handle failed", e)
        }
    }

    protected open fun cacheData(url: String, cache: Int, byteArray: ByteArray) {
        Fulton.context.cacheManagement.add(this.javaClass.simpleName, url, cache, byteArray)
    }

    open fun cleanCache() {
        Fulton.context.cacheManagement.clean(this.javaClass.simpleName)
    }
}

