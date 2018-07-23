package com.swarmnyc.fulton.android

import com.swarmnyc.fulton.android.impl.FultonContextImpl
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicReference

object Fulton {
    private val contextRef: AtomicReference<FultonContext> = AtomicReference(FultonContextImpl())

    var context: FultonContext
        get() = contextRef.get()
        set(value) {
            contextRef.set(value)
        }
}

interface FultonContext {
    var defaultCacheDurationMs: Int
    var requestTimeOutMs: Int
    var cacheManagement: CacheManagement
    var errorHandler:  ApiErrorHandler
}

interface ApiErrorHandler {
    fun onError(apiError: ApiError)
}


interface CacheManagement {
    fun add(api: String, url: String, durationMs: Int, data: ByteArray)

    fun <T> get(url: String, type: Type): T?

    fun clean(api: String)
}

