package com.swarmnyc.fulton.android.cache

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.util.fromJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SqliteCacheManagerTest {
    companion object {
        /**
         * initializing Fulton for every test, so all test has clear fulton to use
         */
        @BeforeClass
        @JvmStatic
        fun init() {
            InstrumentationRegistry.getTargetContext().deleteDatabase(SqliteCacheManager.DbName)
        }
    }

    @Test
    fun addTest() {
        val manager = SqliteCacheManager(InstrumentationRegistry.getTargetContext())
        manager.add("test", "url", 10000, "Test1".toByteArray())
        manager.add("test", "url", 10000, "Test2".toByteArray())

        val result = manager.get("url")!!.fromJson<String>()

        assertEquals("Test2", result)
    }


    @Test
    fun timeoutTest() {
        val manager = SqliteCacheManager(InstrumentationRegistry.getTargetContext())
        manager.add("test", "url3", 1500, "Test".toByteArray())

        assertEquals("Test", manager.get("url3")!!.fromJson<String>())

        Thread.sleep(2000)

        assertEquals(null, manager.get("url3"))

    }

    @Test
    fun nullTest() {
        val manager = SqliteCacheManager(InstrumentationRegistry.getTargetContext())
        val result = manager.get("url2")

        assertNull(result)
    }

    @Test
    fun cleanTest() {
        val manager = SqliteCacheManager(InstrumentationRegistry.getTargetContext())
        manager.add("test", "url1", 10000, "Test1".toByteArray())
        manager.add("test", "url2", 10000, "Test2".toByteArray())

        manager.clean()

        val result =  manager.get("url1")

        assertNull(result)
    }
}