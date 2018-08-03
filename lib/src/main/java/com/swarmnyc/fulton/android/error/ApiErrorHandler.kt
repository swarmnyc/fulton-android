package com.swarmnyc.fulton.android.error

interface ApiErrorHandler {
    fun onError(error: Exception)
}