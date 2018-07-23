package com.swarmnyc.fulton.android

import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.util.await
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@Ignore
@RunWith(AndroidJUnit4::class)
class ApiClientRealTest {
    companion object {
        val TAG = ApiClientTest::class.java.simpleName!!
    }

    private val apiClient = PostApiClient()

    @Test
    fun execSuccessTest() {
        apiClient.listPosts().await()!!.apply {
            assertNotEquals(0, this.data.size)
            assertNotEquals(0, this.data[0].tags.size)
            assertNotNull(this.data[0].author)
        }
    }

    @Test
    fun execFailTest() {
        val latch = CountDownLatch(1)
        var error: ApiError? = null

        apiClient.error404().fail {
            latch.countDown()

            error = it
        }

        latch.await()

        assertEquals(404, error!!.response!!.status)
        assertEquals("Not Found", error!!.message)
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