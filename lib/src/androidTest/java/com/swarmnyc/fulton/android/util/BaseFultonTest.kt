package com.swarmnyc.fulton.android.util

import android.support.test.InstrumentationRegistry
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.cache.VoidCacheManager
import org.junit.Before

abstract class BaseFultonTest {
    companion object {
        val voidCacheManager = VoidCacheManager()
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