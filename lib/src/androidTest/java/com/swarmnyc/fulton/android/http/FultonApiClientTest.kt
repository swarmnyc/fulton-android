package com.swarmnyc.fulton.android.http

import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.identity.AccessToken
import com.swarmnyc.fulton.android.model.TopDogAuthor
import com.swarmnyc.fulton.android.model.TopDogPost
import com.swarmnyc.fulton.android.util.BaseFultonTest
import com.swarmnyc.fulton.android.util.await
import org.junit.Assert.*
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

            fun list(): ApiPromise<TopDogPost> {
                return request {
                    mockResponse = Response(200)
                }
            }

            override fun <T> handleResponse(promise: ApiDeferred<T>, req: Request, res: Response) {
                super.handleResponse(promise, req, res)
                request = req
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

            fun listAuthor(): ApiPromise<ApiManyResult<TopDogAuthor>> {
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
}