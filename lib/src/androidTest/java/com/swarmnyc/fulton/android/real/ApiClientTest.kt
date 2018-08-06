package com.swarmnyc.fulton.android.real

import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.error.HttpError
import com.swarmnyc.fulton.android.http.ApiClientTest
import com.swarmnyc.fulton.android.model.TopDogAuthor
import com.swarmnyc.fulton.android.util.BaseFultonTest
import com.swarmnyc.fulton.android.util.await
import org.junit.Assert.*
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
        TopDogPostApiClient().listPosts().await()!!.apply {
            assertNotEquals(0, this.data.size)
            assertNotEquals(0, this.data[0].tags.size)
            assertNotNull(this.data[0].author)
        }
    }

    @Test
    fun execFailTest() {
        val latch = CountDownLatch(1)
        var error: HttpError? = null

        TopDogPostApiClient().error404().catch {
            error = it as HttpError

            latch.countDown()
        }

        latch.await()

        assertEquals(404, error!!.response.status)
        assertEquals("Not Found", error!!.message)
    }

    @Test
    fun gzipTest() {
        val body = TopDogAuthor("1", "Test", "abc")

        EchoApiClient().gzipPost(body).await()!!.apply {
            assertEquals("1", getAsJsonObject("data").getAsJsonPrimitive("id").asString)
        }
    }
}

