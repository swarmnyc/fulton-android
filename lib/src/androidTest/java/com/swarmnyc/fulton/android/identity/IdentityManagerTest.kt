package com.swarmnyc.fulton.android.identity

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.swarmnyc.fulton.android.FultonInitOptions
import com.swarmnyc.fulton.android.util.BaseFultonTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class IdentityManagerTest : BaseFultonTest() {
    private val identityManager = IdentityManagerImpl(InstrumentationRegistry.getContext(), FultonInitOptions())

    companion object {
        val TAG = IdentityManagerTest::class.java.simpleName!!
    }

    @Test
    fun setTokenTest() {
        identityManager.token = AccessToken("eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjVhZmI0MjkzMWZlNWMxMDAwZjFkN2ExMSIsInRzIjoxNTMyNjM0MzQ3NTgxLCJ1c2VybmFtZSI6IndhZGUifQ.BcpODw1sHii2_I425YRGQfq43vnZ8POChOXiVFd99Mg", "bearer", 3600)

        assertNotNull(identityManager.user)

        val sp = InstrumentationRegistry.getContext().getSharedPreferences(IdentityManagerImpl.SP_File, Context.MODE_PRIVATE)

        assertNotNull(sp.getString(IdentityManagerImpl.Field_AccessToken, null))
    }

    @Test
    fun cleanTokenTest() {
        identityManager.token = null

        assertNull(identityManager.user)

        val sp = InstrumentationRegistry.getContext().getSharedPreferences(IdentityManagerImpl.SP_File, Context.MODE_PRIVATE)

        assertNull(sp.getString(IdentityManagerImpl.Field_AccessToken, null))
    }
}