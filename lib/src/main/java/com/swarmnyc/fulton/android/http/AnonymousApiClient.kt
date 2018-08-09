package com.swarmnyc.fulton.android.http

import com.swarmnyc.promisekt.Promise

internal class AnonymousApiClient : ApiClient() {
    override val urlRoot: String = ""

    fun <T> newRequest(builder: Request.() -> Unit): Promise<T> {
        val req = Request()

        builder(req)

        if (req.url == null) req.buildUrl()

        req.buildDataType()

        return request(req)
    }
}