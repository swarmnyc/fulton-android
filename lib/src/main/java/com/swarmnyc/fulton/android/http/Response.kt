package com.swarmnyc.fulton.android.http

class Response(val url: String = "", val status: Int = -1, val headers: Map<String, List<String>> = emptyMap(), val data: ByteArray = ByteArray(0), var error: Exception? = null) {
    val contentLength: Int
        get() = data.size

    val isJson: Boolean

    init {
        isJson = headers["Content-Type"]?.any {
            it.toLowerCase().contains("json")
        } ?: false

        if (error == null && (status < 200 || status >= 400)) {
            error = if (isJson) {
                Exception("Api Error")
            } else {
                Exception(String(data))
            }
        }
    }
}