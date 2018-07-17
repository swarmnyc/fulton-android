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
    var defaultCacheDuration: Int
    var cacheManagement: CacheManagement
    val requestTimeOut: Int
}

interface CacheManagement {
    fun add(api: String, url: String, duration: Int, data: ByteArray)

    fun <T> get(url: String, type: Type): T?

    fun clean(api: String)
}

