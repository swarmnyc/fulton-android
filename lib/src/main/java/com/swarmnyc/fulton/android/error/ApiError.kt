package com.swarmnyc.fulton.android.error

import com.swarmnyc.fulton.android.http.Request
import com.swarmnyc.fulton.android.http.Response
import com.swarmnyc.fulton.android.util.fromJson
import java.util.*

open class ApiError(cause: Exception) : Exception(cause.message, cause) {
    var isHandled: Boolean = false
}

open class HttpApiError(val request: Request, val response: Response) : ApiError(response.error!!) {
    var status = response.status
}

/**
 * the class of api error
 */
class FultonApiError(request: Request, response: Response) : HttpApiError(request, response) {
    var code: String
        private set

    var items: Map<String, FultonApiErrorList>
        private set

    init {
        try {
            val result: FultonApiErrorResult = response.data.fromJson()
            if (result.error == null) {
                code = "unknown"
                items = mapOf()
            } else {
                code = result.error.code ?: "unknown"
                items = result.error.detail ?: mapOf()
            }
        } catch (e: Exception) {
            code = "unknown"
            items = mapOf()
        }
    }
}

/**
 * the class of api error detail
 */
data class FultonApiErrorResult(val error: FultonApiErrorContent?)


/**
 * the class of api error detail
 */
data class FultonApiErrorContent(val code: String?, val detail: Map<String, FultonApiErrorList>?)

/**
 * the class of api error detail
 */
class FultonApiErrorList : ArrayList<FultonApiErrorItem>()

/**
 * the class of api error detail item
 */
data class FultonApiErrorItem(
        val code: String,
        val message: String
)