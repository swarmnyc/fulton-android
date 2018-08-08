package com.swarmnyc.fulton.android.util

import android.support.test.InstrumentationRegistry
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.cache.VoidCacheManagrImpl
import org.junit.Before
import org.junit.BeforeClass

abstract class BaseFultonTest {
    companion object {
        val voidCacheManager = VoidCacheManagrImpl()
    }

    /**
     * initializing Fulton for every test, so all test has clear fulton to use
     */
    @Before
    fun initEach() {
        Fulton.init(InstrumentationRegistry.getTargetContext()) {
            cacheManager = voidCacheManager
        }
    }
}