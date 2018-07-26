package com.swarmnyc.fulton.android

import com.swarmnyc.fulton.android.cache.CacheManager
import com.swarmnyc.fulton.android.error.ApiErrorHandler
import com.swarmnyc.fulton.android.identity.IdentityManager

interface FultonContext {
    var defaultCacheDurationMs: Int
    var defaultUseGzip: Boolean
    var requestTimeOutMs: Int
    var identityManager : IdentityManager
    var cacheManager: CacheManager
    var errorHandler: ApiErrorHandler
}


