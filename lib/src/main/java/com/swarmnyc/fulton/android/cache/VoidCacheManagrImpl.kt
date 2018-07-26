package com.swarmnyc.fulton.android.cache

import java.lang.reflect.Type

class VoidCacheManagrImpl : CacheManager {
    override fun add(api: String, url: String, durationMs: Int, data: ByteArray) {

    }

    override fun <T> get(url: String, type: Type): T? {
        return null
    }

    override fun clean(api: String) {
    }
}