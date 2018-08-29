package com.swarmnyc.fulton.android.http

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.error.HttpError
import com.swarmnyc.fulton.android.model.*
import com.swarmnyc.fulton.android.real.TopDogPostApiClient
import com.swarmnyc.fulton.android.util.BaseFultonTest
import com.swarmnyc.fulton.android.util.NetworkUtils
import com.swarmnyc.fulton.android.util.RequestExecutorMock
import com.swarmnyc.fulton.android.util.toJson
import com.swarmnyc.promisekt.Promise
import com.swarmnyc.promisekt.util.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch


private const val UrlRoot = "http://api.fulton.com"

@RunWith(AndroidJUnit4::class)
class NetworkStateMonitorTest : BaseFultonTest() {
    companion object {
        val TAG = NetworkStateMonitorTest::class.java.simpleName!!
    }

    @After
    fun reset(){
        NetworkUtils.startWifi()
        NetworkUtils.startData()
    }

    @Test
    fun monitorTest() {
        Fulton.init(InstrumentationRegistry.getContext()) {
            networkStateMonitorEnabled = true
        }

        NetworkUtils.startWifi()
        NetworkUtils.startData()

        assertEquals(true, Fulton.context.isNetworkAvailable)

        NetworkUtils.stopWifi()
        NetworkUtils.stopData()

        assertEquals(false, Fulton.context.isNetworkAvailable)

        NetworkUtils.startWifi()
        NetworkUtils.startData()

        assertEquals(true, Fulton.context.isNetworkAvailable)

    }

    @Test
    fun noNetworkTest() {
        Fulton.init(InstrumentationRegistry.getContext()) {
            networkStateMonitorEnabled = true
        }

        NetworkUtils.stopWifi()
        NetworkUtils.stopData()

        try {
            Fulton.request<TopDogAuthor> {
                urlRoot = "http://api.fulton.com"
                resultType = TopDogAuthor::class.java
            }.await(true)

            fail()
        } catch (e: HttpError) {
            assertEquals(e.message, Response.ErrorCodeNetworkError, e.status)
        }
    }
}