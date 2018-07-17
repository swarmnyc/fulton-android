package com.swarmnyc.fulton.android

import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import io.mockk.every
import io.mockk.mockk
import nl.komponents.kovenant.Promise
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import java.util.concurrent.CountDownLatch


@RunWith(AndroidJUnit4::class)
class ApiClientInstrumentedTest {
    companion object {
        val TAG = ApiClientInstrumentedTest::class.java.simpleName
    }

    @Test
    fun buildUrlTest() {
        val apiClient = object : ApiClient() {
            override val apiUrl: String = "http://api.fulton.com"

            public override fun buildUrl(path: String, queryString: Map<String, Any?>?): String {
                return super.buildUrl(path, queryString)
            }

            public override fun buildUrl(paths: List<String>?, queryString: Map<String, Any?>?): String {
                return super.buildUrl(paths, queryString)
            }
        }

        assertEquals("http://api.fulton.com/news", apiClient.buildUrl("news"))
        assertEquals("http://api.fulton.com/news", apiClient.buildUrl("/news"))

        assertEquals("http://api.fulton.com/news/id", apiClient.buildUrl(listOf("news", "id")))

        assertEquals("http://api.fulton.com/news?k1=a&k2=2&k3=%26", apiClient.buildUrl("news", mapOf("k1" to "a", "k2" to 2, "k3" to "&")))
    }

    @Test
    fun requestErrorTest() {
        val apiClient = object : ApiClient() {
            override val apiUrl: String = "http://api.fulton.com"

            fun get(): Promise<String?, Throwable> {
                return request(Method.GET, buildUrl(), cache = 1)
            }

            override fun createRequest(options: RequestOptions): Request {
                val requestMock = mockk<Request>(relaxed = true)

                every {
                    requestMock.response(any() as (Request, Response, Result<ByteArray, FuelError>) -> Unit)
                } answers {
                    requestMock
                }

                return requestMock
            }
        }

        val latch = CountDownLatch(1)

        apiClient.get() success {
            Log.d(TAG, "API RESULT : $it")
            latch.countDown()
        }

        latch.await()
    }

    @Test
    fun play() {
    }
}
