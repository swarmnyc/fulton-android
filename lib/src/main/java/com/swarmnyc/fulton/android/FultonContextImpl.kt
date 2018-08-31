package com.swarmnyc.fulton.android

import android.content.Context
import com.swarmnyc.fulton.android.cache.CacheManager
import com.swarmnyc.fulton.android.cache.SqliteCacheManager
import com.swarmnyc.fulton.android.error.ApiErrorHandler
import com.swarmnyc.fulton.android.http.RequestExecutor
import com.swarmnyc.fulton.android.http.RequestExecutorImpl
import com.swarmnyc.fulton.android.identity.IdentityManager
import com.swarmnyc.fulton.android.identity.IdentityManagerImpl
import java.lang.reflect.Type


class FultonContextImpl(val context: Context, options: FultonInitOptions) : FultonContext {
    override var userType: Type = options.userType
    override var defaultCacheDurationMs: Int = options.defaultCacheDurationMs
    override var defaultUseGzip: Boolean = options.defaultUseGzip
    override var defaultReadTimeOutMs: Int? = options.defaultReadTimeOutMs
    override var defaultConnectTimeoutMs: Int? = options.defaultConnectTimeoutMs
    override var errorHandler: ApiErrorHandler = options.errorHandler

    override var cacheManager: CacheManager = options.cacheManager ?: SqliteCacheManager(context)
    override var identityManager: IdentityManager = options.identityManager
            ?: IdentityManagerImpl(context, options)

    override var requestExecutor: RequestExecutor = options.requestExecutor
            ?: RequestExecutorImpl(this)

    override var requestExecutorMock: RequestExecutor? = options.mockRequestExecutor
}



