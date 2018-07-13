package com.swarmnyc.fulton.android

import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicReference

object Fulton {
    private val contextRef: AtomicReference<FultonContext> = AtomicReference()

    var context: FultonContext
        get() = contextRef.get()
        set(value) {
            contextRef.set(value)
        }
}


interface FultonContext {
    var defaultCacheDuration: Int
    var cacheManagement: CacheManagement
}

interface CacheManagement {
    fun add(api: String, url: String, duration: Int, data: ByteArray)

    fun <T> get(url: String, type: Type): T?

    fun clean(api: String)
}

class FultonContextImpl : FultonContext {
    override var defaultCacheDuration: Int = 300

    override var cacheManagement: CacheManagement
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
}