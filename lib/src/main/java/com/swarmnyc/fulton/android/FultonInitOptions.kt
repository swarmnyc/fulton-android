package com.swarmnyc.fulton.android

import com.swarmnyc.fulton.android.cache.CacheManager
import com.swarmnyc.fulton.android.error.ApiErrorHandler
import com.swarmnyc.fulton.android.http.RequestExecutor
import com.swarmnyc.fulton.android.identity.IdentityManager
import com.swarmnyc.fulton.android.identity.User
import java.lang.reflect.Type

val voidApiErrorHandler = {_:Throwable -> }

class FultonInitOptions {
    /**
     * the user type for serialize and deserialize, the default type is com.swarmnyc.fulton.android.identity.User
     */
    var userType: Type = User::class.java

    /**
     * the default cache time, default value is 5 mins
     */
    var defaultCacheDurationMs: Int = 300_000 // 5 minutes

    /**
     * the default gzip setting, if true the http body will be gzip compressed
     */
    var defaultUseGzip: Boolean = false

    /**
     * the default request read time out
     * */
    var defaultReadTimeOutMs: Int? = null

    /**
     * the default request connect time out
     * */
    var defaultConnectTimeoutMs: Int? = null

    /**
     * the global error handler
     */
    var errorHandler: ApiErrorHandler = voidApiErrorHandler

    /**
     * the executor to execute http or https request
     */
    var requestExecutor: RequestExecutor? = null

    /**
     * if the value is not null, the api client will use this executor to start request
     */
    var mockRequestExecutor: RequestExecutor? = null

    /**
     * the manager of identity, the default value is IdentityManagerImpl
     */
    var identityManager : IdentityManager? = null

    /**
     * the manager of cache, the default value is SqliteCacheManager
     */
    var cacheManager: CacheManager? = null

    /**
     * if true, the fulton will start monitor network statu
     */
    var networkStateMonitorEnabled: Boolean = false
}