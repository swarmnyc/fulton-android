package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.error.ApiError
import nl.komponents.kovenant.*

typealias ApiPromise<T> = Promise<T, ApiError>
typealias ApiDeferred<T> = Deferred<T, ApiError>

infix fun <V, R> ApiPromise<V>.thenApi(bind: (V) -> ApiPromise<R>): ApiPromise<R> {
    val deferred = deferred<R, ApiError>(context)
    success {
        try {
            bind(it).success(deferred::resolve).fail(deferred::reject)
        } catch (e: Exception) {
            deferred.reject(ApiError(e))
        }
    }.fail(deferred::reject)

    return deferred.promise
}

inline infix fun <V, R> ApiPromise<V>.thenApiApply(crossinline bind: V.() -> ApiPromise<R>): ApiPromise<R> {
    val deferred = deferred<R, ApiError>(context)
    success {
        try {
            bind(it).success(deferred::resolve).fail(deferred::reject)
        } catch (e: Exception) {
            deferred.reject(ApiError(e))
        }
    }.fail(deferred::reject)

    return deferred.promise
}

fun <V> ofSuccess(value: V): ApiPromise<V> {
    return Promise.ofSuccess(value)
}

