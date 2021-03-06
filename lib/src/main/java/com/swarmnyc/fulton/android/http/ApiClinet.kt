package com.swarmnyc.fulton.android.http

import com.google.gson.JsonSyntaxException
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.FultonContext
import com.swarmnyc.fulton.android.error.HttpError
import com.swarmnyc.promisekt.Promise
import com.swarmnyc.fulton.android.util.GenericType
import com.swarmnyc.fulton.android.util.fromJson
import java.lang.reflect.Type


/**
 * Base Api Client, supports
 * - error handle
 * - async
 * - cache
 * */
abstract class ApiClient(val context: FultonContext = Fulton.context) {
    protected abstract val urlRoot: String

    /**
     * init request, like set headers
     * */
    open fun initRequest(req: Request) {
    }

    protected inline fun <reified T> request(init: Request.() -> Unit): Promise<T> {
        val req = Request()
        req.urlRoot = urlRoot
        req.resultType = T::class.java

        req.init()

        return request(req)
    }

    protected fun <T> request(req: Request): Promise<T> {
        initRequest(req)

        if (req.urlRoot == null) req.urlRoot = urlRoot
        if (req.url == null) req.buildUrl()

        req.buildDataType()

        // check request
        val error = req.verify()

        return if (error == null) {
            Promise { promise ->
                if (req.method == Method.GET && req.cacheDurationMs > 0) {
                    val cacheData = Fulton.context.cacheManager.get(req.url!!)
                    if (cacheData != null) {
                        // cache hits
                        val cacheResult = deserialize<T>(req.resultType!!, cacheData)
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

        val executor = context.requestExecutorMock ?: context.requestExecutor

        executor.execute(req) { request, response ->
            endRequest(promise, request, response)
        }
    }

    protected open fun <T> endRequest(promise: Promise<T>, req: Request, res: Response) {
        if (res.error == null) {
            try {
                handleSuccess(promise, req, res)
            } catch (e: JsonSyntaxException) {
                res.status = Response.ErrorCodeJsonConvertError
                res.error = e
                handelError(promise, req, res)
            } catch (e: Throwable) {
                res.error = e
                handelError(promise, req, res)
            }
        } else {
            handelError(promise, req, res)
        }
    }

    protected open fun <T> handleSuccess(promise: Promise<T>, req: Request, res: Response) {
        val result = deserialize<T>(req.resultType!!, res.data)

        if (result == null) {
            promise.reject(Exception("Api result is empty"))
        } else {
            val shouldCache = req.method == Method.GET && req.cacheDurationMs > 0

            if (shouldCache && result !is Unit) {
                cacheData(req.url!!, req.cacheDurationMs, res.data)
            }

            promise.resolve(result)
        }
    }

    protected open fun <T> handelError(promise: Promise<T>, req: Request, res: Response) {
        val apiError = createError(req, res)

        if (!onError(apiError)) {
            promise.catch {
                if (req.shouldSendErrorToErrorHandler) {
                    context.errorHandler(apiError)
                }
            }

            promise.reject(apiError)
        }
    }

    protected open fun <T> deserialize(type: Type, data: ByteArray): T {
        val dataType = if (type is GenericType) {
            type.rawType
        } else {
            type
        }

        return when (dataType) {
            Unit::class.java, Nothing::class.java -> {
                @Suppress("UNCHECKED_CAST")
                Unit as T
            }
            else -> {
                data.fromJson(type)
            }
        }
    }

    protected open fun createError(req: Request, res: Response): Throwable {
        return HttpError(req, res)
    }

    /**
     * The error handler on ApiClient
     * @return if true, it means the error is handled, so .catch or Fulton.context.errorHandler.onError won't invoked.
     */
    protected open fun onError(error: Throwable): Boolean {
        return false
    }

    protected open fun cacheData(url: String, cache: Int, byteArray: ByteArray) {
        context.cacheManager.add(this.javaClass.simpleName, url, cache, byteArray)
    }

    open fun cleanCache() {
        context.cacheManager.clean(this.javaClass.simpleName)
    }
}

