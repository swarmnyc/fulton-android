package com.swarmnyc.fulton.android.util

import android.util.Log

internal open class Logger(val tag: String) {
    fun d(msg: String) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg)
        }
    }

    fun d(builder: () -> String) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, builder())
        }
    }

    fun e(msg: String, e: Throwable) {
        Log.d(tag, msg, e)
    }

    object Api : Logger("fulton.api")

    object Cache : Logger("fulton.cache")

    object Network : Logger("fulton.network")
}

