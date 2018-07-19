package com.swarmnyc.fulton.android.util

import nl.komponents.kovenant.Promise
import java.util.concurrent.CountDownLatch

fun <T> Promise<T?, Throwable>.await(): T?{
    val latch = CountDownLatch(1)

    var t:T? = null

    this.success {
        t = it
    }.always {
        latch.countDown()
    }

    latch.await()

    return t
}