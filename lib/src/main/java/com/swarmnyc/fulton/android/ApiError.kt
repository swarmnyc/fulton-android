package com.swarmnyc.fulton.android

class ApiError(cause: Exception, val request: Request? = null, val response: Response? = null) : Exception(cause.message, cause) {
    var isHandled: Boolean = false
    var status = response?.status ?: 0
}