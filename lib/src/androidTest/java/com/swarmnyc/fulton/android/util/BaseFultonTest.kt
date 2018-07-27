package com.swarmnyc.fulton.android.util

import android.support.test.InstrumentationRegistry
import com.swarmnyc.fulton.android.Fulton
import org.junit.Before
import org.junit.BeforeClass

abstract class BaseFultonTest {

    /**
     * initializing Fulton for every test, so all test has clear fulton to use
     */
    @Before
    fun initEach() {
        Fulton.init(InstrumentationRegistry.getTargetContext())
    }
}