package com.swarmnyc.fulton.android.identity

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.http.Request
import com.swarmnyc.fulton.android.http.RequestCallback
import com.swarmnyc.fulton.android.http.RequestExecutor
import com.swarmnyc.fulton.android.http.Response
import com.swarmnyc.promisekt.util.await
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class IdentityApiClientTest {
    private val token = AccessToken("eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjVhZmI0MjkzMWZlNWMxMDAwZjFkN2ExMSIsInRzIjoxNTMyNjM0MzQ3NTgxLCJ1c2VybmFtZSI6IndhZGUifQ.BcpODw1sHii2_I425YRGQfq43vnZ8POChOXiVFd99Mg", "bearer", 3600)

    init {
        Fulton.init(InstrumentationRegistry.getTargetContext()) {
            requestExecutorMock = object : RequestExecutor {
                override fun execute(req: Request, callback: RequestCallback) {
                    val url = req.url!!
                    val res: Response = when {
                        url.contains("register") || url.contains("login") -> {
                            Response(200, token)
                        }
                        url.contains("logout") -> {
                            Response(200, "")
                        }
                        else -> Response(404)
                    }

                    callback(req, res)
                }
            }
        }
    }

    private val apiClient = object : IdentityApiClient<User>() {
        override val urlRoot: String = "http://api.fulton.com"
    }


    @Test
    fun registerTest() {
        apiClient.register("un", "email", "pd").await()

        assertNotNull(Fulton.context.identityManager.user)
    }

    @Test
    fun loginTest() {
        apiClient.login("un", "email").await()

        assertNotNull(Fulton.context.identityManager.user)
    }

    @Test
    fun logoutTest() {
        Fulton.context.identityManager.token = token

        apiClient.logout().await()

        assertNull(Fulton.context.identityManager.user)
        assertNull(Fulton.context.identityManager.token)
    }
}