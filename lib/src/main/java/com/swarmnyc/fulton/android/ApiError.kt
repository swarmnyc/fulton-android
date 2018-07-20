package com.swarmnyc.fulton.android

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response

class ApiError : Exception {
    constructor(cause: Exception) : super(cause) {
        this.request = null
        this.response = null
    }

    constructor(request: Request, response: Response, cause: Exception) : super(cause) {
        this.request = request
        this.response = response
    }

    val request: Request?
    val response: Response?
    var isHandled: Boolean = false
}