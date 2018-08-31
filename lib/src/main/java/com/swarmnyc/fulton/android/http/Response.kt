package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.util.toJsonBytes

class Response {
    companion object {
        /**
         * This error is like network is unavailable
         */
        const val ErrorCodeNetworkError = -1
        /**
         * This error is like the server has no response or url is unreachable
         */
        const val ErrorCodeRequestError = -2
        /**
         * This error is like converting json response data to the given Type to the object fails
         */
        const val ErrorCodeJsonConvertError = -3
    }

    constructor(error: Exception) : this("", -1, mapOf(), ByteArray(0), error)
    constructor(status: Int) : this("", status, mapOf(), ByteArray(0), null)
    constructor(status: Int, error: Exception) : this("", status, mapOf(), ByteArray(0), error)
    constructor(status: Int, data: ByteArray) : this("", status, mapOf(), data, null)
    constructor(status: Int, data: Any) : this("", status, mapOf(), data.toJsonBytes(), null)
    constructor(url: String, status: Int, headers: Map<String, List<String>>, data: ByteArray, error: Throwable?){
        this.url = url
        this.status = status
        this.headers = headers
        this.data = data

        this.isJson = headers["Content-Type"]?.any {
            it.toLowerCase().contains("json")
        } ?: false

        if (error == null && (status < 0 || status >= 400)) {
            this.error = if (isJson) {
                Exception("Api Error")
            } else {
                Exception(String(data))
            }
        }else{
            this.error = error
        }
    }

    var url: String
        internal set

    var status: Int
        internal set

    val headers: Map<String, List<String>>

    val data: ByteArray

    var error: Throwable?
        internal set

    val contentLength: Int
        get() = data.size

    val isJson: Boolean

    val dataString: String
        get() = String(data)
}