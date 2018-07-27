package com.swarmnyc.fulton.android

import com.swarmnyc.fulton.android.cache.CacheManager
import com.swarmnyc.fulton.android.error.ApiErrorHandler
import com.swarmnyc.fulton.android.http.ApiPromise
import com.swarmnyc.fulton.android.http.Request
import com.swarmnyc.fulton.android.http.RequestExecutor
import com.swarmnyc.fulton.android.identity.IdentityManager

interface FultonContext {
    /**
     * the default cache time, default value is 5 mins
     */
    var defaultCacheDurationMs: Int

    /**
     * the default gzip setting, if true the http body will be gzip compressed
     */
    var defaultUseGzip: Boolean

    /**
     * the default request, if true the http body will be gzip compressed
     */
    var readTimeOutMs: Int?

    var connectTimeoutMs: Int?

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

    /**
     * the lazy way to create request
     */
    fun <T> request(builder: Request.() -> Unit): ApiPromise<T>
}


