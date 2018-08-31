package com.swarmnyc.fulton.android

import com.swarmnyc.fulton.android.cache.CacheManager
import com.swarmnyc.fulton.android.error.ApiErrorHandler
import com.swarmnyc.fulton.android.http.RequestExecutor
import com.swarmnyc.fulton.android.identity.IdentityManager
import java.lang.reflect.Type

interface FultonContext {
    var userType: Type

    /**
     * the default cache time
     */
    var defaultCacheDurationMs: Int

    /**
     * the default gzip setting, if true the http body will be gzip compressed
     */
    var defaultUseGzip: Boolean

    /**
     * the default request read time out
     * */
    var defaultReadTimeOutMs: Int?

    /**
     * the default request connect time out
     * */
    var defaultConnectTimeoutMs: Int?

    /**
     * the global error handler
     */
    var errorHandler: ApiErrorHandler

    /**
     * the executor to execute http or https request
     */
    var requestExecutor: RequestExecutor

    /**
     * if the value is not null, the api client will use this executor to start request
     */
    var requestExecutorMock: RequestExecutor?

    /**
     * the manager of identity, the default value is IdentityManagerImpl
     */
    var identityManager: IdentityManager

    /**
     * the manager of cache, the default value is SqliteCacheManager
     */
    var cacheManager: CacheManager
}


