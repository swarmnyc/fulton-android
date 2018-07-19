package com.swarmnyc.fulton.android

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response

class ApiError(val request: Request, val response: Response, cause: Exception) : Exception(cause) {
    var isHandled: Boolean = false
}