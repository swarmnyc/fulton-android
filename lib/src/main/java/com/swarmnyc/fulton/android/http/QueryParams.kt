package com.swarmnyc.fulton.android.http

import android.net.Uri
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.swarmnyc.fulton.android.util.JsonObjectBuilder
import com.swarmnyc.fulton.android.util.urlEncode

/**
 * the class of pagination for query params
 */
data class QueryParamPagination(
        var index: Int? = null,
        var size: Int? = null
)

/**
 * the class of Query Params
 * it can use JSON DSL
 * ``` kotlon
 * queryParams {
 *      filter = json {
 *          "\$or" to listOf(json {
 *              "title" to "manager"
 *          }, json {
 *          "title" to json{
 *              "\$regex" to "sales"
 *              "\$options" to "i"
 *          }
 *      })
 *      projection = mapOf("pro1" to true, "pro2" to false)
 *      includes = listOf("include1", "include2.include2")
 *      sort = mapOf("sort1" to true, "sort2" to false)
 *      // two styles
 *      pagination {
 *          index = 1
 *          size = 100
 *      }
 *      pagination = QueryParamPagination(1, 100)
 *  }
 * ```
 */
class QueryParams {
    /**
     * the parameter of filter, it can use JSON DSL or JSON Object
     */
    var filter: JsonObject? = null

    /**
     * the parameter of sort, the format is
     * ```
     * mapOf("sort1" to true, "sort2" to false)
     * ```
     */
    var sort: Map<String, Boolean>? = null

    /**
     * the parameter of the projection, the format is
     * ```
     * mapOf("pro1" to true, "pro2" to false)
     * ```
     */
    var projection: Map<String, Boolean>? = null

    /**
     * the parameter of the includes, the format is
     * ```
     * listOf("include1", "include2.include2")
     * ```
     */
    var includes: List<String>? = null

    /**
     * the parameter of the pagination, the format is
     * ```
     * pagination {
     *      index = 1
     *      size = 100
     * }
     * ```
     */
    var pagination: QueryParamPagination? = null

    /**
     * the parameter of query, the format is
     * ```
     * mapOf("field2" to any, "field1" to any)
     * ```
     */
    var query: Map<String, String>? = null

    /**
     * convert json object to query string recursively
     */
    private fun jsonToString(json: JsonElement, builder: Uri.Builder, root: String) {
        when {
            json.isJsonObject -> json.asJsonObject.entrySet().forEach { (key, value) ->
                // make array like root[key][]=value
                if (value.isJsonArray) {
                    value.asJsonArray.forEach { item ->
                        jsonToString(item, builder, "$root[$key][]")
                    }
                } else {
                    jsonToString(value, builder, "$root[$key]")
                }
            }

            json.isJsonArray -> json.asJsonArray.forEach { item ->
                jsonToString(item, builder, "$root[]")
            }

            json.isJsonPrimitive -> {
                builder.appendQueryParameter(root, json.asString)
            }
        }
    }

    /**
     * convert query params to query string
     */
    fun toQueryString(): String {
        val builder = Uri.Builder()

        filter?.apply {
            jsonToString(this, builder, "filter")
        }

        sort?.apply {
            if (this.isNotEmpty()) {
                val s = this.map { it.key + if (it.value) "" else "-" }.joinToString(",")
                builder.appendQueryParameter("sort", s)
            }
        }

        projection?.apply {
            if (this.isNotEmpty()) {
                val s = this.map { it.key + if (it.value) "" else "-" }.joinToString(",")
                builder.appendQueryParameter("projection", s)
            }
        }

        includes?.apply {
            if (this.isNotEmpty()) {
                builder.appendQueryParameter("include", this.joinToString(","))
            }
        }

        pagination?.apply {
            if (this.index != null) {
                builder.appendQueryParameter("pagination[index]", this.index.toString())
            }

            if (this.size != null) {
                builder.appendQueryParameter("pagination[size]", this.size.toString())
            }
        }

        query?.apply {
            if (this.isNotEmpty()) {
                for ((key, value) in this) {
                    builder.appendQueryParameter(key, value)
                }
            }
        }

        return builder.build().query
    }

    override fun toString(): String {
        return toQueryString()
    }
}

/**
 * the DSL for query params
 */
fun queryParams(block: QueryParams.() -> Unit): QueryParams = QueryParams().apply(block)

/**
 * the DSL for query params
 */
fun QueryParams.filter(block: JsonObjectBuilder.() -> Unit) {
    filter = JsonObjectBuilder().json(block)
}

/**
 * the DSL for pagination
 */
fun QueryParams.pagination(block: QueryParamPagination.() -> Unit) {
    pagination = QueryParamPagination().apply(block)
}