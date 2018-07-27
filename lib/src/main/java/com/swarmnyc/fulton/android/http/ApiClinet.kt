package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.*
import com.swarmnyc.fulton.android.error.ApiError
import com.swarmnyc.fulton.android.error.HttpApiError
import com.swarmnyc.fulton.android.util.fromJson
import nl.komponents.kovenant.deferred

/**
 * Base Api Client, supports
 * - error handle
 * - async
 * - cache
 * */
abstract class ApiClient {
    companion object {
        const val TAG = "fulton.api"
        const val GZip = "gzip"
        const val NO_CACHE = 0
    }

    protected abstract val urlRoot: String

    protected var context: FultonContext = Fulton.context

    /**
     * init request, like set headers
     * */
    open fun initRequest(req: Request) {
    }

    protected inline fun <reified T : Any> request(builder: Request.() -> Unit): ApiPromise<T> {
        val req = Request()
        req.urlRoot = urlRoot
        req.dataType = T::class.java

        builder(req)

        req.buildUrl()
        req.buildDataType()

        return request(req)
    }

    protected fun <T : Any> request(req: Request): ApiPromise<T> {
        val deferred = deferred<T, ApiError>()

        deferred.promise.context.workerContext.offer {
            if (req.method == Method.GET && req.cacheDurationMs > NO_CACHE) {
                val cacheResult = Fulton.context.cacheManager.get<T>(req.url!!, req.dataType!!)
                if (cacheResult != null) {
                    // cache hits
                    deferred.resolve(cacheResult)

                    return@offer
                }
            }

            startRequest(deferred, req)
        }

        return deferred.promise
    }

    protected open fun <T> startRequest(deferred: ApiDeferred<T>, req: Request) {
        initRequest(req)

        if (req.mockResponse != null) {
            req.mockResponse!!.url = req.url!!
            endRequest(deferred, req, req.mockResponse!!)
            return
        }

        val executor = context.mockRequestExecutor ?: context.requestExecutor

        executor.execute(req) { request, response ->
            endRequest(deferred, request, response)
        }
    }

    protected open fun <T> endRequest(deferred: ApiDeferred<T>, req: Request, res: Response) {
        if (res.error == null) {
            try {
                handleSuccess(deferred, req, res)
            } catch (e: Throwable) {
                handelError(deferred, req, res)
            }
        } else {
            handelError(deferred, req, res)
        }
    }

    protected open fun <T> handleSuccess(deferred: ApiDeferred<T>, options: Request, res: Response) {
        var shouldCache = options.method == Method.GET && options.cacheDurationMs > NO_CACHE

        val result: T = when (options.dataType) {
            Unit::class.java, Nothing::class.java -> {
                shouldCache = false
                @Suppress("UNCHECKED_CAST")
                Unit as T
            }
            else -> {
                res.data.fromJson(options.dataType!!)
            }
        }

        if (shouldCache) {
            cacheData(options.url!!, options.cacheDurationMs, res.data)
        }

        deferred.resolve(result)
    }

    protected open fun <T> handelError(deferred: ApiDeferred<T>, req: Request, res: Response) {
        val apiError = createError(req, res)

        try {
            onError(apiError)

            deferred.promise.fail {
                if (!it.isHandled) {
                    context.errorHandler.onError(apiError)
                }
            }

            deferred.reject(apiError)
        } catch (e: Exception) {
            throw Exception("Error Handle failed", e)
        }
    }

    protected open fun createError(req: Request, res: Response): ApiError {
        return HttpApiError(req, res)
    }

    protected open fun onError(error: ApiError) {}

    protected open fun cacheData(url: String, cache: Int, byteArray: ByteArray) {
        context.cacheManager.add(this.javaClass.simpleName, url, cache, byteArray)
    }

    open fun cleanCache() {
        context.cacheManager.clean(this.javaClass.simpleName)
    }
}

