package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.error.ApiError
import nl.komponents.kovenant.*

internal typealias ApiPromise<T> = Promise<T, ApiError>
internal typealias ApiDeferred<T> = Deferred<T, ApiError>

infix fun <V, R> Promise<V, Exception>.thenFlat(bind: (V) -> Promise<R, Exception>): Promise<R, Exception> {
    val deferred = deferred<R, Exception>(context)
    success {
        try {
            bind(it).success(deferred::resolve).fail(deferred::reject)
        } catch (e: Exception) {
            deferred.reject(ApiError(e))
        }
    }.fail(deferred::reject)

    return deferred.promise
}

inline infix fun <V, R> Promise<V, Exception>.thenFlatApply(crossinline bind: V.() -> Promise<R, Exception>): Promise<R, Exception> {
    val deferred = deferred<R, Exception>(context)
    success {
        try {
            bind(it).success(deferred::resolve).fail(deferred::reject)
        } catch (e: Exception) {
            deferred.reject(ApiError(e))
        }
    }.fail(deferred::reject)

    return deferred.promise
}

