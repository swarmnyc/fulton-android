package com.swarmnyc.fulton.android.util

import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred

fun <V> promise(block: ((deferred: Deferred<V, Throwable>) -> Unit)): Promise<V, Throwable> {
    val def = deferred<V, Throwable>()

    def.promise.context.workerContext.offer {
        block.invoke(def)
    }
def.promise.fail {  }
    return def.promise
}