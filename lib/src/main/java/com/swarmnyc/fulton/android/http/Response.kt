package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.util.toJsonBytes

class Response(var url: String, val status: Int, val headers: Map<String, List<String>>, val data: ByteArray, var error: Throwable?) {
    constructor(error: Exception) : this("", -1, mapOf(), ByteArray(0), error)
    constructor(status: Int) : this("", status, mapOf(), ByteArray(0), null)
    constructor(status: Int, data: ByteArray) : this("", status, mapOf(), data, null)
    constructor(status: Int, data: Any) : this("", status, mapOf(), data.toJsonBytes(), null)

    val contentLength: Int
        get() = data.size

    val isJson: Boolean = headers["Content-Type"]?.any {
        it.toLowerCase().contains("json")
    } ?: false

    init {
        if (error == null && (status < 200 || status >= 400)) {
            error = if (isJson) {
                Exception("Api Error")
            } else {
                Exception(String(data))
            }
        }
    }
}