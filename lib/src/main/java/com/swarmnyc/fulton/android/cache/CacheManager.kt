package com.swarmnyc.fulton.android.cache

/**
 * cache manager cache a request for a specific time
 */
interface CacheManager {
    fun add(cls: String, url: String, durationMs: Int, data: ByteArray)

    fun get(url: String): ByteArray?

    fun clean(cls: String? = null)
}