package com.swarmnyc.fulton.android

class Response(val url: String = "", val status: Int = -1, val headers: Map<String, List<String>> = emptyMap(), val data: ByteArray = ByteArray(0), var error: Exception? = null) {
    val contentLength: Int
        get() = data.size

    init {
        if (error == null && (status < 200 || status >= 400)) {
            if (data.isNotEmpty()) {
                error = Exception(String(data))
            }
        }
    }
}