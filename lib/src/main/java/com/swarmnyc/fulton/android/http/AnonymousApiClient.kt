package com.swarmnyc.fulton.android.http

import com.swarmnyc.promisekt.Promise

internal class AnonymousApiClient : ApiClient() {
    override val urlRoot: String = ""

    fun <T> newRequest(init: Request.() -> Unit): Promise<T> {
        val req = Request().apply(init)

        return request(req)
    }
}