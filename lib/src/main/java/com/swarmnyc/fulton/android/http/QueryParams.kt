package com.swarmnyc.fulton.android.http

import android.net.Uri
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.swarmnyc.fulton.android.util.JsonObjectBuilder

/**
 * the class of pagination for query params
 */
data class QueryParamPagination(
        var index: Int? = null,
        var size: Int? = null
)

class QueryParamSort {
    internal val map = mutableMapOf<String, Boolean>()

    fun asc(field: String) {
        map[field] = true
    }

    fun desc(field: String) {
        map[field] = false
    }
}

class QueryParamProjection {
    internal val map = mutableMapOf<String, Boolean>()

    fun show(field: String) {
        map[field] = true
    }

    fun hide(field: String) {
        map[field] = false
    }
}

class QueryParamIncludes {
    internal val list = mutableListOf<String>()

    fun add(vararg field: String) {
        list.addAll(field)
    }
}

/**
 * the class of Query Params
 * it can use JSON DSL
 * ``` kotlin
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
 *
 *      sort {
 *           desc("sort1")
 *           asc("sort2")
 *      }
 *
 *      projection {
 *           hide("pro2")
 *           show("pro1")
 *      }
 *
 *      includes {
 *            add("include1", "include2.include2")
 *       }
 *
 *      pagination {
 *          index = 1
 *          size = 100
 *      }
 *  }
 * ```
 */
class QueryParams {

    /**
     * the parameter of filter
     */
    var filter: JsonObject? = null

    /**
     * the parameter of filter
     */
    fun filter(init: JsonObjectBuilder.() -> Unit) {
        filter = JsonObjectBuilder().json(init)
    }

    /**
     * the parameter of sort
     * ```
     * sort = mapOf("sort1" to true, "sort2" to false)
     * ```
     * true for ascend, false for descend
     */
    var sort: Map<String, Boolean>? = null

    /**
     * the parameter of sort
     * ```
     * sort {
     *      desc("sort1")
     *      asc("sort2")
     * }
     * ```
     */
    fun sort(init: QueryParamSort.() -> Unit) {
        sort = QueryParamSort().apply(init).map
    }

    /**
     * the parameter of the projection
     * ```
     * projection = mapOf("proj1" to true, "proj2" to false)
     * ```
     * true for show, false for hide
     */
    var projection: Map<String, Boolean>? = null

    /**
     * the parameter of the projection
     * ```
     * projection {
     *  show("pro1")
     *  hide("pro2")
     * }
     * ```
     */
    fun projection(init: QueryParamProjection.() -> Unit) {
        projection = QueryParamProjection().apply(init).map
    }

    /**
     * the parameter of the includes
     * ```
     * includes = listOf("include1", "include2.include2") 
     * ```
     */
    var includes: List<String>? = null

    /**
     * the parameter of the includes
     * ```
     * includes {
     *    add("include1", "include2.include2")
     * }
     * ```
     */
    fun includes(init: QueryParamIncludes.() -> Unit) {
        includes = QueryParamIncludes().apply(init).list
    }

    /**
     * the parameter of the pagination
     */
    var pagination: QueryParamPagination? = null

    /**
     * the parameter of the pagination
     * ```
     * pagination {
     *      index = 1
     *      size = 100
     * }
     * ```
     */
    fun pagination(init: QueryParamPagination.() -> Unit) {
        pagination = QueryParamPagination().apply(init)
    }

    /**
     * the parameter of query
     * ```
     * query = mapOf("field2" to any, "field1" to any)
     * ```
     */
    var query: Map<String, String>? = null

    /**
     * the parameter of query
     * ```
     * query("field2" to any, "field1" to any)
     * ```
     */
    fun query(vararg pair: Pair<String, String>) {
        query = pair.toMap()
    }

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

        return builder.build().query ?: ""
    }

    override fun toString(): String {
        return toQueryString()
    }
}

/**
 * the DSL for query params
 */
fun queryParams(init: QueryParams.() -> Unit): QueryParams = QueryParams().apply(init)