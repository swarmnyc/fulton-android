package com.swarmnyc.fulton.android.real

import com.google.gson.JsonObject
import com.swarmnyc.fulton.android.http.ApiClient
import com.swarmnyc.fulton.android.promise.Promise

class EchoApiClient : ApiClient() {
    override val urlRoot: String = "https://postman-echo.com"

    fun gzipPost(value: Any): Promise<JsonObject> {
        return request {
            paths("post")
            body = value
            useGzip = true
        }
    }
}