package com.swarmnyc.fulton.android

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

object Fulton {
    private val initialized = AtomicBoolean(false)
    private val contextRef: AtomicReference<FultonContext> = AtomicReference()

    val context: FultonContext
        get() {
            if (!initialized.get()){
                throw Exception("Fulton have to be initialized before use it. call Fulton.init() to solve this problem.")
            }

            return contextRef.get()
        }

    fun init(context: Context) {
        initialized.set(true)
        contextRef.set(FultonContextImpl(context))
    }

    fun init(context: FultonContext) {
        initialized.set(true)
        contextRef.set(context)
    }
}