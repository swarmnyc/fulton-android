package com.swarmnyc.fulton.android.http

import android.net.Uri
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.util.GenericType
import java.lang.reflect.Type

class Request {
    var connectionTimeOutMs = Fulton.context.defaultConnectTimeoutMs
    var readTimeOutMs = Fulton.context.defaultReadTimeOutMs
    var method: Method = Method.GET

    var urlRoot: String? = null
    var resultType: Type? = null

    var cacheDurationMs: Int = Fulton.context.defaultCacheDurationMs

    var url: String? = null

    var useGzip: Boolean = Fulton.context.defaultUseGzip
    var body: Any? = null

    var startedAt = System.currentTimeMillis()

    var shouldSendErrorToErrorHandler = true
    /**
     * get or set the path of the url
     * */
    var paths: List<String>? = null

    /**
     * add the path of the url
     * */
    fun paths(vararg values: String) {
        if (this.paths == null) {
            this.paths = values.asList()
        } else {
            this.paths = this.paths!! + values
        }
    }

    /**
     * get or set the query of the url, both of query and queryString will be added to url
     * */
    var query: Map<String, Any>? = null

    /**
     * add the query of the url, both of query and queryString will be added to url
     * */
    fun query(vararg values: Pair<String, Any>) {
        if (this.query == null) {
            this.query = values.toMap()
        } else {
            this.query = this.query!! + values
        }
    }

    /**
     * get or set the query string of the url, both of query and queryString will be added to url
     * */
    var queryString: String? = null

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
    fun headers(vararg values: Pair<String, String>) {
        headers.putAll(values)
    }

    var queryParams: QueryParams? = null

    fun queryParams(block: QueryParams.() -> Unit) {
        queryParams = QueryParams().apply(block)
    }

    /**
     * get or set the result type of the request
     * */
    var resultTypeGenerics: List<Type>? = null

    /**
     * add the result type of the request
     * */
    fun resultTypeGenerics(vararg values: Type) {
        if (this.resultTypeGenerics == null) {
            this.resultTypeGenerics = values.asList()
        } else {
            this.resultTypeGenerics = this.resultTypeGenerics!! + values
        }
    }

    /**
     * if the value is set, will use this response directly
     */
    var mockResponse: Response? = null

    /**
     * the function of building url
     */
    fun buildUrl() {
        val builder = Uri.parse(urlRoot).buildUpon()

        paths?.forEach {
            val p = if (it.startsWith("/")) {
                it.substring(1)
            } else {
                it
            }

            builder.appendEncodedPath(p)
        }

        query?.forEach {
            builder.appendQueryParameter(it.key, it.value.toString())
        }

        if (queryParams != null) {
            builder.encodedQuery(getQuery(builder) + queryParams!!.toQueryString())
        }

        if (queryString != null) {
            if (queryString!!.startsWith("&")){
                queryString = queryString!!.substring(1)
            }

            builder.encodedQuery(getQuery(builder) + queryString)
        }

        this.url = builder.build().toString()
    }

    fun buildDataType() {
        if (resultType != null && resultTypeGenerics != null) {
            this.resultType = GenericType(resultType!!, *resultTypeGenerics!!.toTypedArray())
        }
    }

    internal fun verify(): Exception? {
        if (url == null) return NullPointerException("Request.url cannot be null")
        if (resultType == null) return NullPointerException("Request.resultType cannot be null")

        return null
    }

    private fun getQuery(builder: Uri.Builder): String {
        val q = builder.build().query
        return if (q.isNullOrEmpty()) {
            ""
        } else {
            "$q&"
        }
    }
}


