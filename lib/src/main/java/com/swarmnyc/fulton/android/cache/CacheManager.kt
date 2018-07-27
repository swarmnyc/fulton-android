package com.swarmnyc.fulton.android.cache

import java.lang.reflect.Type

/**
 * cache manager cache a request for a specific time
 */
interface CacheManager {
    fun add(cls: String, url: String, durationMs: Int, data: ByteArray)

    fun <T> get(url: String, type: Type): T?

    fun clean(cls: String)
}