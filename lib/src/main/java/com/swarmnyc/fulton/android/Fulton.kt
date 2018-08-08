package com.swarmnyc.fulton.android

import android.content.Context
import com.swarmnyc.fulton.android.http.AnonymousApiClient
import com.swarmnyc.fulton.android.http.Request
import com.swarmnyc.fulton.android.identity.IdentityManager
import com.swarmnyc.fulton.android.promise.Promise
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

object Fulton {
    private val initialized = AtomicBoolean(false)
    private val contextRef: AtomicReference<FultonContext> = AtomicReference()

    val context: FultonContext
        get() {
            if (!initialized.get()) {
                throw Exception("Fulton have to be initialized before use it. call Fulton.init() to solve this problem.")
            }

            return contextRef.get()
        }

    val identityManager: IdentityManager
        get() = context.identityManager

    fun init(context: Context, options: FultonInitOptions = FultonInitOptions()) {
        initialized.set(true)
        contextRef.set(FultonContextImpl(context, options))
    }

    fun init(context: Context, builder: FultonInitOptions.() -> Unit) {
        initialized.set(true)
        val options = FultonInitOptions()
        builder(options)
        contextRef.set(FultonContextImpl(context, options))
    }

    fun init(context: FultonContext) {
        initialized.set(true)
        contextRef.set(context)
    }

    private val apiClient: AnonymousApiClient by lazy {
        AnonymousApiClient()
    }

    fun <T> request(builder: Request.() -> Unit): Promise<T> {
        return apiClient.newRequest(builder)
    }
}

