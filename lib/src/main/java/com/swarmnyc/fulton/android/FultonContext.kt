package com.swarmnyc.fulton.android

import com.swarmnyc.fulton.android.cache.CacheManager
import com.swarmnyc.fulton.android.error.ApiErrorHandler
import com.swarmnyc.fulton.android.http.RequestExecutor
import com.swarmnyc.fulton.android.identity.IdentityManager

interface FultonContext {
    var defaultCacheDurationMs: Int
    var defaultUseGzip: Boolean
    var requestTimeOutMs: Int
    var identityManager : IdentityManager
    var cacheManager: CacheManager

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
    var mockRequestExecutor: RequestExecutor?
}


