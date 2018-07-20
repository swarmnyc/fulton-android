package com.swarmnyc.fulton.android.util

import com.swarmnyc.fulton.android.ApiError
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.failUi
import java.util.concurrent.CountDownLatch

fun <T> Promise<T?, Throwable>.await(): T? {
    val latch = CountDownLatch(1)

    var t: T? = null
    var e: Throwable? = null

    this.success {
        t = it
    }.fail {
        e = it
    }.always {
        latch.countDown()
    }

    latch.await()
    if (e != null) {
        if ((e as? ApiError)?.isHandled != true) {
            throw e!!
        }
    }

    return t
}