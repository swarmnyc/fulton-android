package com.swarmnyc.fulton.android

import android.net.Uri
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.swarmnyc.fulton.android.util.await
import io.mockk.mockk
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    fun treadTest(){
        // there will create 3 thread, 1. main thread, 2. promise body thread 3. result(success, fail, always) thread

        val mainThread = Thread.currentThread().id
        val promise = deferred<Unit, Throwable>()
        Log.d(TAG, "Main Thread Id : ${Thread.currentThread().id}")
        promise.promise.context.workerContext.offer {
            Log.d(TAG, "Promise Thread Id : ${Thread.currentThread().id}")
            promise.resolve(Unit)
        }

        val latch = CountDownLatch(0)
        promise.promise.success {
            Log.d(TAG, "Success Thread Id : ${Thread.currentThread().id}")
            assertNotEquals(mainThread, Thread.currentThread().id)
        }.always {
            Log.d(TAG, "Always Thread Id : ${Thread.currentThread().id}")
            assertNotEquals(mainThread, Thread.currentThread().id)

            latch.countDown()
        }

        latch.await()
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
    fun requestSuccessUnitTest() {
        val apiClient = object : ApiClient() {
            override val apiUrl: String = "http://api.fulton.com"

            fun get(): Promise<Unit?, ApiError> {
                return request(Method.GET, buildUrl(), cache = 1)
            }

            override fun <T> startRequest(promise: Deferred<T, ApiError>, options: RequestOptions) {
                val req = mockk<Request>()
                val res = Response(URL(options.url), 200)
                val result = Result.Success<ByteArray, FuelError>(ByteArray(0))

                handleResponse(promise, options, req, res, result)
            }
        }

        val result = apiClient.get().await()

        assertEquals(Unit, result)
    }

    @Test
    fun requestSuccessStringTest() {
        val apiClient = object : ApiClient() {
            override val apiUrl: String = "http://api.fulton.com"

            fun get(): Promise<String?, ApiError> {
                return request(Method.GET, buildUrl(), cache = 1)
            }

            override fun <T> startRequest(promise: Deferred<T, ApiError>, options: RequestOptions) {
                val req = mockk<Request>()
                val res = Response(URL(options.url), 200)
                val result = Result.Success<ByteArray, FuelError>("TEST".toByteArray())

                handleResponse(promise, options, req, res, result)
            }
        }

        val result = apiClient.get().await()

        assertEquals("TEST", result)
    }

    @Test
    fun requestErrorTest() {
        val apiClient = object : ApiClient() {
            override val apiUrl: String = "http://api.fulton.com"

            fun get(): Promise<String?, ApiError> {
                return request(Method.GET, buildUrl(), cache = 1)
            }

            override fun <T> startRequest(promise: Deferred<T, ApiError>, options: RequestOptions) {
                val req = mockk<Request>()
                val res = Response(URL(options.url), 400)
                val result = Result.error(FuelError(Exception("TEST")))

                handleResponse(promise, options, req, res, result)
            }
        }

        var result: String? = null
        apiClient.get()
                .fail {
                    result = it.cause?.message
                }.await()

        assertEquals("TEST", result)

    }

    @Test
    fun requestJoinTest() {
        // TODO
    }

    @Test
    fun errorHandleTest() {
        val apiClient = object : ApiClient() {
            override val apiUrl: String = "http://api.fulton.com"

            fun get(): Promise<String?, ApiError> {
                return request(Method.GET, buildUrl(), cache = 1)
            }

            override fun <T> startRequest(promise: Deferred<T, ApiError>, options: RequestOptions) {
                val req = mockk<Request>()
                val res = Response(URL(options.url), 400)
                val result = Result.error(FuelError(Exception("TEST")))

                handleResponse(promise, options, req, res, result)
            }
        }

        val latch = CountDownLatch(1)
        var result = false

        Fulton.context.errorHandler = object : ApiErrorHandler {
            override fun onError(apiError: ApiError) {
                Log.d(TAG, "error called")
                assertEquals(true, apiError.isHandled)
                result = true

                latch.countDown()
            }
        }

        apiClient.get()
                .fail {
                    it.isHandled = true
                }

        latch.await()
        assertEquals(true, result)
    }
}
