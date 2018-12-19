package com.swarmnyc.fulton.android.http

import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.google.gson.JsonObject
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.error.HttpError
import com.swarmnyc.fulton.android.model.*
import com.swarmnyc.fulton.android.real.TopDogPostApiClient
import com.swarmnyc.fulton.android.util.BaseFultonTest
import com.swarmnyc.fulton.android.util.toJson
import com.swarmnyc.promisekt.Promise
import com.swarmnyc.promisekt.util.await
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch


private const val UrlRoot = "http://api.fulton.com"

@RunWith(AndroidJUnit4::class)
class ApiClientTest : BaseFultonTest() {
    companion object {
        val TAG = ApiClientTest::class.java.simpleName
    }

    @Test
    fun requestSuccessUnitTest() {
        val apiClient = object : ApiClient() {
            override val urlRoot: String = UrlRoot

            fun get(): Promise<Unit?> {
                return request {
                    mockResponse = Response(200)
                }
            }
        }

        val result = apiClient.get().await()

        assertEquals(Unit, result)
    }

    @Test
    fun requestSuccessStringTest() {
        val apiClient = object : ApiClient() {
            override val urlRoot: String = UrlRoot

            fun get(): Promise<String?> {
                return request {
                    mockResponse = Response(200, data = "TEST".toByteArray())
                }
            }
        }

        val result = apiClient.get().await()

        assertEquals("TEST", result)
    }

    @Test
    fun requestErrorTest() {
        val apiClient = object : ApiClient() {
            override val urlRoot: String = UrlRoot

            fun get(): Promise<String?> {
                return request {
                    mockResponse = Response(400, data = "TEST".toByteArray())
                }
            }
        }

        var result: String? = null
        apiClient.get()
                .catch {
                    result = it.cause?.message
                }.await(false)

        assertEquals("TEST", result)
    }

    @Test
    fun requestJoinTest() {
        val apiClient = object : ApiClient() {
            override val urlRoot: String = UrlRoot

            fun method1(): Promise<String> {
                return request {
                    mockResponse = Response(200, data = "1234".toByteArray())
                }
            }

            fun method2(@Suppress("UNUSED_PARAMETER") value: String): Promise<Int> {
                return request {
                    mockResponse = Response(200, data = "4567".toByteArray())
                }
            }
        }

        val result: Int = apiClient.method1().thenChain { apiClient.method2(it) }.await()!!

        assertEquals(4567, result)
    }

    @Test
    fun requestJoin2Test() {
        val apiClient = object : ApiClient() {
            override val urlRoot: String = UrlRoot

            fun method1(): Promise<String> {
                return request {
                    mockResponse = Response(200, data = "1234".toByteArray())
                }
            }
        }

        val result: Int = apiClient.method1().then { 4321 }.await()!!

        assertEquals(4321, result)
    }

    @Test()
    fun dataTypeTest() {
        val json = listOf(ModelA("A", 1, listOf(ModelB("AB", 1))), ModelA("B", 2, listOf())).toJson()
        val apiClient = object : ApiClient() {
            override val urlRoot: String = UrlRoot

            fun method1(): Promise<List<ModelA>> {
                return request {
                    method = Method.GET
                    paths = listOf("list")
                    resultTypeGenerics = listOf(ModelA::class.java)

                    mockResponse = Response(200, data = json.toByteArray())
                }
            }
        }

        val result = apiClient.method1().await()!!

        assertEquals(2, result.size)
        assertEquals("A", result[0].name)
        assertEquals(1, result[0].list.size)
        assertEquals("AB", result[0].list[0].name)
        assertEquals("B", result[1].name)
    }

    @Test
    fun errorHandleTest() {
        val apiClient = object : ApiClient() {
            override val urlRoot: String = UrlRoot

            fun get(): Promise<String> {
                return request {
                    mockResponse = Response(400, "TEST")
                }
            }
        }

        val latch = CountDownLatch(1)
        var result = false

        Fulton.context.errorHandler = {
            Log.d(TAG, "error called")
            result = true

            latch.countDown()
        }

        apiClient.get()

        latch.await()
        assertEquals(true, result)
    }

    @Test
    fun unitTest() {
        val apiClient = object : ApiClient() {
            override val urlRoot: String = UrlRoot

            fun get(): Promise<Unit> {
                return request {
                    mockResponse = Response(200, "Test")
                }
            }
        }

        val result = apiClient.get().await()

        assertEquals(Unit, result)
    }

    @Test
    fun mockRequestExecutorTest() {
        Fulton.context.requestExecutorMock = object : RequestExecutor {
            override fun execute(req: Request, callback: RequestCallback) {
                val author = TopDogAuthor("1", "test-author", "")

                callback(req, Response(200, ApiManyResult(listOf(TopDogPost("1", "test", listOf(), "", "", "", author, listOf())))))
            }
        }

        val apiClient = TopDogPostApiClient()

        val result = apiClient.listPosts().await()!!

        Fulton.context.requestExecutorMock = null

        assertEquals("test-author", result.data[0].author.name)
    }

    @Test
    fun anonymousApiClientTest() {
        val result: TopDogAuthor = Fulton.request<TopDogAuthor> {
            urlRoot = "http://api.fulton.com"
            resultType = TopDogAuthor::class.java
            paths("authors", "1")

            //or
            //url = "http://api.fulton.com/authors/1"

            mockResponse = Response(200, TopDogAuthor("1", "test-author", ""))
        }.await()!!

        assertEquals("test-author", result.name)
    }

    @Test
    fun jsonConvertErrorApiClientTest() {
        try {
            Fulton.request<ArrayList<Any>> {
                urlRoot = "http://api.fulton.com"
                resultType = ArrayList::class.java
                resultTypeGenerics(JsonObject::class.java)

                mockResponse = Response(200, TopDogAuthor("1", "test-author", ""))
            }.await(true)

            fail()
        } catch (e: HttpError) {
            assertEquals(Response.ErrorCodeJsonConvertError, e.status)
        }
    }
}