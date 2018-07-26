package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.error.ApiError
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.thenApply

typealias ApiPromise<T> = Promise<T, ApiError>
typealias ApiDeferred<T> = Deferred<T, ApiError>

infix fun <V, R> ApiPromise<V>.then(bind: (V) -> ApiPromise<R>): ApiPromise<R> {
    val deferred = deferred<R, ApiError>(context)
    success {
        try {
            bind(it).success(deferred::resolve).fail(deferred::reject)
        } catch (e: Exception) {
            deferred.reject(ApiError(e))
        }
    }.fail(deferred::reject)

    deferred.promise.thenApply { }

    return deferred.promise
}

inline infix fun <V, R> ApiPromise<V>.thenApply(crossinline bind: V.() -> ApiPromise<R>): ApiPromise<R> {
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