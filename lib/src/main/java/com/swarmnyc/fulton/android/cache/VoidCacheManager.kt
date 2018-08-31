package com.swarmnyc.fulton.android.cache

class VoidCacheManager : CacheManager {
    override fun add(cls: String, url: String, durationMs: Int, data: ByteArray) {}

    override fun get(url: String): ByteArray? {
        return null
    }

    override fun clean(cls: String?) {}
}