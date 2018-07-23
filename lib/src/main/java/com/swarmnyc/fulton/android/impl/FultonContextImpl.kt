package com.swarmnyc.fulton.android.impl

import com.swarmnyc.fulton.android.ApiError
import com.swarmnyc.fulton.android.ApiErrorHandler
import com.swarmnyc.fulton.android.CacheManagement
import com.swarmnyc.fulton.android.FultonContext
import java.lang.reflect.Type

class FultonContextImpl : FultonContext {
    override var defaultCacheDurationMs: Int = 300_000 // 5 minutes
    override var requestTimeOutMs: Int = 0
    override var cacheManagement: CacheManagement = TestCacheManagementImpl()
    override var errorHandler: ApiErrorHandler = VoidApiErrorHandler()
}

class VoidApiErrorHandler : ApiErrorHandler {
    override fun onError(apiError: ApiError) {
    }
}

class TestCacheManagementImpl : CacheManagement {
    override fun add(api: String, url: String, durationMs: Int, data: ByteArray) {

    }

    override fun <T> get(url: String, type: Type): T? {
        return null
    }

    override fun clean(api: String) {
    }
}