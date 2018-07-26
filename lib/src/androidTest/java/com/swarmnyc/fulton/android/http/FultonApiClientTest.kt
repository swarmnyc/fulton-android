package com.swarmnyc.fulton.android.http

import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.error.ApiError
import com.swarmnyc.fulton.android.identity.AccessToken
import com.swarmnyc.fulton.android.util.BaseFultonTest
import com.swarmnyc.fulton.android.util.await
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


private const val UrlRoot = "http://api.fulton.com"

@RunWith(AndroidJUnit4::class)
class FultonApiClientTest : BaseFultonTest() {
    companion object {
        val TAG = FultonApiClientTest::class.java.simpleName!!
    }

    @Test
    fun setTokenTest() {
        Fulton.context.identityManager.token = AccessToken("eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjVhZmI0MjkzMWZlNWMxMDAwZjFkN2ExMSIsInRzIjoxNTMyNjM0MzQ3NTgxLCJ1c2VybmFtZSI6IndhZGUifQ.BcpODw1sHii2_I425YRGQfq43vnZ8POChOXiVFd99Mg", "bearer", 3600)

        assertNotNull(Fulton.context.identityManager.user)
    }
}