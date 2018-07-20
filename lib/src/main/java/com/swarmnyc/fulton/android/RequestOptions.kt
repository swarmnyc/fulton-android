package com.swarmnyc.fulton.android

import com.github.kittinunf.fuel.core.Method
import com.swarmnyc.fulton.android.util.urlEncode
import java.lang.reflect.Type
import java.net.URI

class RequestOptions {
    constructor()

    constructor(method: Method, url: String, dataType: Type = String::class.java, cacheDuration: Int = Fulton.context.defaultCacheDuration) {
        this.method = method
        this.url = url
        this.dataType = dataType
        this.cacheDuration = cacheDuration
    }

    var method: Method = Method.GET

    var urlRoot: String? = null
    var dataType: Type? = null
    var subDataType: List<Type>? = null

    var cacheDuration: Int = Fulton.context.defaultCacheDuration

    var url: String? = null

    var paths: List<String>? = null

    var body: Any? = null
    var query: Map<String, Any>? = null
    var queryParams: QueryParams? = null
    var startedAt = System.currentTimeMillis()
    var timeOut = Fulton.context.requestTimeOut

    /**
     * the function of building url
     */
    fun buildUrl() {
        val u = buildString {
            append(urlRoot)

            if (paths?.isNotEmpty() == true) {
                paths!!.forEach { append("/$it") }
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
        if (dataType != null && subDataType != null) {
            this.dataType = JsonGenericType(dataType!!, *subDataType!!.toTypedArray())
        }
    }
}