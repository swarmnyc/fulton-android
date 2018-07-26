package com.swarmnyc.fulton.android.http

/**
 * the class of api result that includes many items
 */
data class ApiOneResult<TEntity>(
        val data: TEntity
)

/**
 * the class of api result that includes many items
 */
data class ApiManyResult<TEntity>(
        val data: List<TEntity>,
        val pagination: ApiResultPagination? = null
)

/**
 * the class of pagination
 */
class ApiResultPagination(
        val index: Int = 0,
        var size: Int = 0,
        val total: Int = 0
) {
    val canNext: Boolean
        get() {
            return ((index + 1) * size) < total
        }
}