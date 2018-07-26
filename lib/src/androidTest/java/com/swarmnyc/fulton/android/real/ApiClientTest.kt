package com.swarmnyc.fulton.android.real

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.gson.JsonObject
import com.swarmnyc.fulton.android.http.ApiClientTest
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.error.HttpApiError
import com.swarmnyc.fulton.android.http.ApiClient
import com.swarmnyc.fulton.android.http.ApiPromise
import com.swarmnyc.fulton.android.util.BaseFultonTest
import com.swarmnyc.fulton.android.util.await
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@Ignore
@RunWith(AndroidJUnit4::class)
class ApiClientTest : BaseFultonTest() {
    companion object {
        val TAG = ApiClientTest::class.java.simpleName!!
    }

    @Test
    fun execSuccessTest() {
        PostApiClient().listPosts().await()!!.apply {
            assertNotEquals(0, this.data.size)
            assertNotEquals(0, this.data[0].tags.size)
            assertNotNull(this.data[0].author)
        }
    }

    @Test
    fun execFailTest() {
        val latch = CountDownLatch(1)
        var error: HttpApiError? = null

        PostApiClient().error404().fail {
            latch.countDown()

            error = it as HttpApiError
        }

        latch.await()

        assertEquals(404, error!!.response.status)
        assertEquals("Not Found", error!!.message)
    }

    @Test
    fun gzipTest() {
        val body = Author("1", "Test", "abc")

        EchoApiClient().gzipPost(body).await()!!.apply {
            assertEquals("1", getAsJsonObject("data").getAsJsonPrimitive("id").asString)
        }
    }
}

class PostApiClient : ApiClient() {
    override val urlRoot: String = "https://topdog.varick.io/api/"

    fun listPosts(): ApiPromise<FultonApiManyResult<Post>> {
        return request {
            paths("posts")
            query("includes" to listOf("tags", "anchor"))
            subResultType(Post::class.java)
        }
    }

    fun error404(): ApiPromise<FultonApiManyResult<Post>> {
        return request {
            paths("404")
            subResultType(Post::class.java)
        }
    }
}

class EchoApiClient : ApiClient() {
    override val urlRoot: String = "https://postman-echo.com"

    fun gzipPost(value: Any): ApiPromise<JsonObject> {
        return request {
            paths("post")
            body = value
            useGzip = true
        }
    }
}


data class Post(
        val hotdogId: String,
        val name: String,
        val location: List<Double>,
        val address: String,
        val review: String,
        val picture: String,
        val author: Author,
        val tags: List<Tag>
)

data class Author(
        val id: String,
        val name: String,
        val imageUrl: String
)

data class Tag(
        val id: String,
        val name: String,
        val type: String
)

data class FultonApiManyResult<T>(val data: List<T>)