package com.swarmnyc.fulton.android.real

import com.google.gson.JsonObject
import com.swarmnyc.fulton.android.http.ApiClient
import com.swarmnyc.fulton.android.http.ApiPromise

class EchoApiClient : ApiClient() {
    override val urlRoot: String = "https://postman-echo.com"

    fun gzipPost(value: Any): ApiPromise<JsonObject> {
        return request {
            paths("post")
            body = value
            useGzip = true
        }
    }
}