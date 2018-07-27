package com.swarmnyc.fulton.android

import android.content.Context
import com.swarmnyc.fulton.android.error.ApiErrorHandler
import com.swarmnyc.fulton.android.cache.CacheManager
import com.swarmnyc.fulton.android.cache.SqliteCacheManager
import com.swarmnyc.fulton.android.cache.VoidCacheManagrImpl
import com.swarmnyc.fulton.android.error.VoidApiErrorHandlerImpl
import com.swarmnyc.fulton.android.http.*
import com.swarmnyc.fulton.android.identity.IdentityManager
import com.swarmnyc.fulton.android.identity.IdentityManagerImpl

class FultonContextImpl(context: Context) : FultonContext {
    override var defaultCacheDurationMs: Int = 300_000 // 5 minutes
    override var defaultUseGzip: Boolean = false
    override var readTimeOutMs: Int? = null
    override var connectTimeoutMs: Int? = null
    override var errorHandler: ApiErrorHandler = VoidApiErrorHandlerImpl()

    override var cacheManager: CacheManager = SqliteCacheManager(context)
    override var identityManager: IdentityManager = IdentityManagerImpl(context)
    override var requestExecutor: RequestExecutor = RequestExecutorImpl()
    override var mockRequestExecutor: RequestExecutor? = null

    private val apiClient: AnonymousApiClient by lazy {
        AnonymousApiClient()
    }

    override fun <T> request(builder: Request.() -> Unit): ApiPromise<T> {
        return apiClient.newRequest(builder)
    }

    class AnonymousApiClient : ApiClient() {
        override val urlRoot: String = ""

        fun <T> newRequest(builder: Request.() -> Unit): ApiPromise<T> {
            val req = Request()

            builder(req)

            if (req.url == null) req.buildUrl()

            req.buildDataType()

            return request(req)
        }
    }
}



