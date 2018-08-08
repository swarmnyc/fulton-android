package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.FultonContext
import com.swarmnyc.fulton.android.error.HttpError
import com.swarmnyc.fulton.android.promise.Promise
import com.swarmnyc.fulton.android.util.JsonGenericType
import com.swarmnyc.fulton.android.util.fromJson


/**
 * Base Api Client, supports
 * - error handle
 * - async
 * - cache
 * */
abstract class ApiClient(val context: FultonContext = Fulton.context) {
    companion object {
        const val TAG = "fulton.api"
        const val GZip = "gzip"
        const val NO_CACHE = 0
    }

    protected abstract val urlRoot: String

    /**
     * init request, like set headers
     * */
    open fun initRequest(req: Request) {
    }

    protected inline fun <reified T> request(builder: Request.() -> Unit): Promise<T> {
        val req = Request()
        req.urlRoot = urlRoot
        req.resultType = T::class.java

        builder(req)

        return request(req)
    }

    protected fun <T> request(req: Request): Promise<T> {
        initRequest(req)

        if (req.url == null) req.buildUrl()

        req.buildDataType()

        // check request
        val error = req.verify()

        return if (error == null) {
            Promise { promise ->
                promise.shouldThrowUncaughtError = false

                if (req.method == Method.GET && req.cacheDurationMs > NO_CACHE) {
                    val cacheResult = Fulton.context.cacheManager.get<T>(req.url!!, req.resultType!!)
                    if (cacheResult != null) {
                        // cache hits
                        promise.resolve(cacheResult)

                        return@Promise
                    }
                }

                startRequest(promise, req)
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            Promise.reject(error) as Promise<T>
        }
    }

    protected open fun <T> startRequest(promise: Promise<T>, req: Request) {
        if (req.mockResponse != null) {
            req.mockResponse!!.url = req.url!!
            endRequest(promise, req, req.mockResponse!!)
            return
        }

        val executor = context.mockRequestExecutor ?: context.requestExecutor

        executor.execute(req) { request, response ->
            endRequest(promise, request, response)
        }
    }

    protected open fun <T> endRequest(promise: Promise<T>, req: Request, res: Response) {
        if (res.error == null) {
            try {
                handleSuccess(promise, req, res)
            } catch (e: Throwable) {
                res.error = e
                handelError(promise, req, res)
            }
        } else {
            handelError(promise, req, res)
        }
    }

    protected open fun <T> handleSuccess(promise: Promise<T>, req: Request, res: Response) {
        var shouldCache = req.method == Method.GET && req.cacheDurationMs > NO_CACHE

        val dataType = if (req.resultType is JsonGenericType) {
            (req.resultType as JsonGenericType).rawType
        } else {
            req.resultType
        }

        val result: T = when (dataType) {
            Unit::class.java, Nothing::class.java -> {
                shouldCache = false
                @Suppress("UNCHECKED_CAST")
                Unit as T
            }
            ApiOneResult::class.java -> {
                // result is { data : T }, but convert to return T, so when using can skip .data
                res.data.fromJson<ApiOneResult<T>>(req.resultType!!).data
            }
            else -> {
                res.data.fromJson(req.resultType!!)
            }
        }

        if (result == null) {
            promise.reject(Exception("Api result is empty"))
        } else {
            if (shouldCache) {
                cacheData(req.url!!, req.cacheDurationMs, res.data)
            }

            promise.resolve(result)
        }
    }

    protected open fun <T> handelError(promise: Promise<T>, req: Request, res: Response) {
        val apiError = createError(req, res)

        onError(apiError)

        promise.catch {
            if (req.sendErrorToErrorHandler) {
                context.errorHandler.onError(apiError)
            }
        }

        promise.reject(apiError)
    }

    protected open fun createError(req: Request, res: Response): Throwable {
        return HttpError(req, res)
    }

    protected open fun onError(error: Throwable) {}

    protected open fun cacheData(url: String, cache: Int, byteArray: ByteArray) {
        context.cacheManager.add(this.javaClass.simpleName, url, cache, byteArray)
    }

    open fun cleanCache() {
        context.cacheManager.clean(this.javaClass.simpleName)
    }
}

