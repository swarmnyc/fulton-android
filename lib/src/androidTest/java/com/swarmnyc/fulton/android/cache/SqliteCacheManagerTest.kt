package com.swarmnyc.fulton.android.cache

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
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

        val result = manager.get<String>("url", String::class.java)

        assertEquals("Test2", result)
    }


    @Test
    fun timeoutTest() {
        val manager = SqliteCacheManager(InstrumentationRegistry.getTargetContext())
        manager.add("test", "url3", 1500, "Test".toByteArray())

        assertEquals("Test", manager.get<String>("url3", String::class.java))

        Thread.sleep(2000)

        assertEquals(null, manager.get<String>("url3", String::class.java))

    }

    @Test
    fun nullTest() {
        val manager = SqliteCacheManager(InstrumentationRegistry.getTargetContext())
        val result = manager.get<String>("url2", String::class.java)

        assertNull(result)
    }

    @Test
    fun cleanTest() {
        val manager = SqliteCacheManager(InstrumentationRegistry.getTargetContext())
        manager.add("test", "url1", 10000, "Test1".toByteArray())
        manager.add("test", "url2", 10000, "Test2".toByteArray())

        manager.clean()

        val result =  manager.get<String>("url1", String::class.java)

        assertNull(result)
    }
}