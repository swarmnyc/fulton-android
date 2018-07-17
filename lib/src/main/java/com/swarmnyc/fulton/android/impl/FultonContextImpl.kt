package com.swarmnyc.fulton.android.impl

import com.swarmnyc.fulton.android.CacheManagement
import com.swarmnyc.fulton.android.FultonContext
import java.lang.reflect.Type

class FultonContextImpl : FultonContext {
    override var defaultCacheDuration: Int = 300
    override val requestTimeOut: Int = 5
    override var cacheManagement: CacheManagement = TestCacheManagementImpl()
}

class TestCacheManagementImpl : CacheManagement{
    override fun add(api: String, url: String, duration: Int, data: ByteArray) {

    }

    override fun <T> get(url: String, type: Type): T? {
        return null
    }

    override fun clean(api: String) {
    }
}