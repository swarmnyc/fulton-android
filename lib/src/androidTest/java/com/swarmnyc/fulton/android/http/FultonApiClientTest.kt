package com.swarmnyc.fulton.android.http

import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.identity.AccessToken
import com.swarmnyc.fulton.android.model.TopDogAuthor
import com.swarmnyc.fulton.android.util.BaseFultonTest
import com.swarmnyc.promisekt.Promise
import com.swarmnyc.promisekt.util.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FultonApiClientTest : BaseFultonTest() {
    companion object {
        val TAG = FultonApiClientTest::class.java.simpleName!!
    }

    @Test
    fun setBearerTokenTest() {
        val accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjVhZmI0MjkzMWZlNWMxMDAwZjFkN2ExMSIsInRzIjoxNTMyNjM0MzQ3NTgxLCJ1c2VybmFtZSI6IndhZGUifQ.BcpODw1sHii2_I425YRGQfq43vnZ8POChOXiVFd99Mg"
        Fulton.context.identityManager.token = AccessToken(accessToken, "bearer", 3600)

        var request: Request? = null
        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com"

            fun list(): Promise<Unit> {
                return request {
                    mockResponse = Response(200)
                }
            }

            override fun <T> endRequest(promise: Promise<T>, req: Request, res: Response) {
                request = req
                super.endRequest(promise, req, res)
            }
        }


        apiClient.list().await()

        assertEquals("bearer $accessToken", request!!.headers["Authorization"])

        assertNotNull(Fulton.context.identityManager.user)
    }

    @Test
    fun listTest() {
        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com"

            fun listAuthor(): Promise<ApiManyResult<TopDogAuthor>> {
                return list {
                    val data = ApiManyResult(listOf(TopDogAuthor("1", "Test1", "abc"), TopDogAuthor("2", "Test2", "efg")))
                    mockResponse = Response(200, data)
                }
            }
        }

        val result = apiClient.listAuthor().await()!!

        assertEquals(2, result.data.size)
        assertEquals("Test1", result.data[0].name)
    }

    @Test
    fun listWithPathTest() {
        var request: Request? = null

        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com"

            fun listAuthor(): Promise<ApiManyResult<TopDogAuthor>> {
                return list {
                    paths("list", "abc")

                    mockResponse = Response(200 , data = ApiManyResult(listOf<TopDogAuthor>()))
                }
            }

            override fun <T> endRequest(promise: Promise<T>, req: Request, res: Response) {
                request = req
                super.endRequest(promise, req, res)
            }
        }

        apiClient.listAuthor().await()

        assertEquals("http://api.fulton.com/list/abc", request?.url)
    }

    @Test
    fun detailTest() {
        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com"

            fun getAuthor(id: String): Promise<TopDogAuthor> {
                return detail(id) {
                    val data = ApiOneResult(TopDogAuthor(id, "Test1", "abc"))
                    mockResponse = Response(200, data)
                }
            }
        }

        val result = apiClient.getAuthor("abc").await()!!

        assertEquals("abc", result.id)
    }

    @Test
    fun detailWithPathTest() {
        var request: Request? = null

        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com/authors"

            fun getAuthor(id: String): Promise<TopDogAuthor> {
                return detail(id) {
                    paths("self")

                    val data = ApiOneResult(TopDogAuthor(id, "Test1", "abc"))
                    mockResponse = Response(200, data)
                }
            }

            override fun <T> endRequest(promise: Promise<T>, req: Request, res: Response) {
                request = req
                super.endRequest(promise, req, res)
            }
        }

        apiClient.getAuthor("abc").await()

        assertEquals("http://api.fulton.com/authors/self/abc", request?.url)
    }

    @Test
    fun createTest() {
        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com/authors"

            fun createAuthor(obj: TopDogAuthor): Promise<TopDogAuthor> {
                return create(obj) {
                    mockResponse = Response(201, ApiOneResult(obj))
                }
            }
        }

        val result = apiClient.createAuthor(TopDogAuthor("1", "Test1", "abc")).await()!!

        assertEquals("1", result.id)
    }

    @Test
    fun updateTest() {
        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com/authors"

            fun update(obj: TopDogAuthor): Promise<Unit> {
                return super.update(obj.id, obj) {
                    mockResponse = Response(202)
                }
            }
        }

        apiClient.update(TopDogAuthor("1", "Test1", "abc")).await()
    }

    @Test
    fun updatePartialTest() {
        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com/authors"

            fun update(id: String, obj: Map<String, Any>): Promise<Unit> {
                return super.update(id, obj) {
                    mockResponse = Response(202)
                }
            }
        }

        apiClient.update("1", mapOf()).await()
    }

    @Test
    fun deleteTest() {
        val apiClient = object : FultonApiClient() {
            override val urlRoot: String = "http://api.fulton.com/authors"

            fun delete(id: String): Promise<Unit> {
                return delete(id) {
                    mockResponse = Response(202)
                }
            }
        }

        apiClient.delete("1").await()
    }
}