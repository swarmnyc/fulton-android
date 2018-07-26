package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.util.JsonGenericType
import com.swarmnyc.fulton.android.util.urlEncode
import java.lang.reflect.Type
import java.net.URI

class Request {
    var timeOutMs = Fulton.context.requestTimeOutMs
    var method: Method = Method.GET

    var urlRoot: String? = null
    var dataType: Type? = null

    var cacheDurationMs: Int = Fulton.context.defaultCacheDurationMs

    var url: String? = null

    var useGzip: Boolean = Fulton.context.defaultUseGzip
    var body: Any? = null
    var queryParams: QueryParams? = null

    var startedAt = System.currentTimeMillis()

    /**
     * get or set the path of the url
     * */
    var paths: List<String>? = null

    /**
     * set the path of the url
     * */
    fun paths(vararg values: String) {
        this.paths = values.asList()
    }

    /**
     * get or set the query of the url
     * */
    var query: Map<String, Any>? = null

    /**
     * set the query of the url
     * */
    fun query(vararg values: Pair<String, Any>) {
        this.query = values.toMap()
    }

    /**
     * get or set the headers of the request
     * the default values are
     * Content-Type: application/json
     * Accept: application/json
     * */
    var headers: MutableMap<String, String> = mutableMapOf("Content-Type" to "application/json", "Accept" to "application/json")

    /**
     * add the headers of the request
     * */
    fun headers(vararg values: Pair<String, String>){
        headers.putAll(values)
    }

    /**
     * get or set the result type of the request
     * */
    var subResultType: List<Type>? = null

    /**
     * set the result type of the request
     * */
    fun subResultType(vararg values: Type){
        subResultType = values.asList()
    }

    /**
     * the function of building url
     */
    fun buildUrl() {
        val u = buildString {
            append(urlRoot)

            if (paths?.isNotEmpty() == true) {
                paths!!.forEach { append("/$it") }
            }

            if (queryParams != null) {
                append(queryParams!!.toQueryString())
            }

            if (query?.isNotEmpty() == true) {
                append("?")

                append(query!!.entries.joinToString("&") {
                    "${it.key}=${it.value.toString().urlEncode()}"
                })
            }
        }

        // the normalize doesn't remove double // on below API 21
        this.url = URI.create(u).normalize().toString()
    }

    fun buildDataType() {
        if (dataType != null && subResultType != null) {
            this.dataType = JsonGenericType(dataType!!, *subResultType!!.toTypedArray())
        }
    }
}


