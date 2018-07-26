package com.swarmnyc.fulton.android

import android.content.Context
import com.swarmnyc.fulton.android.error.ApiErrorHandler
import com.swarmnyc.fulton.android.cache.CacheManager
import com.swarmnyc.fulton.android.cache.VoidCacheManagrImpl
import com.swarmnyc.fulton.android.error.VoidApiErrorHandlerImpl
import com.swarmnyc.fulton.android.identity.IdentityManager
import com.swarmnyc.fulton.android.identity.IdentityManagerImpl


class FultonContextImpl(context: Context) : FultonContext {
    override var defaultCacheDurationMs: Int = 300_000 // 5 minutes
    override var defaultUseGzip: Boolean = false
    override var requestTimeOutMs: Int = 0
    override var errorHandler: ApiErrorHandler = VoidApiErrorHandlerImpl()

    override var cacheManager: CacheManager = VoidCacheManagrImpl()
    override var identityManager: IdentityManager = IdentityManagerImpl(context)
}



