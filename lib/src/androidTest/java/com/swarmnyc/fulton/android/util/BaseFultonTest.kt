package com.swarmnyc.fulton.android.util

import android.support.test.InstrumentationRegistry
import com.swarmnyc.fulton.android.Fulton
import org.junit.BeforeClass

abstract class BaseFultonTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun init() {
            Fulton.init(InstrumentationRegistry.getTargetContext())
        }
    }
}