package com.swarmnyc.fulton.android

import com.github.kittinunf.fuel.core.Method
import java.lang.reflect.Type

data class RequestOptions(val method: Method, val url: String, val dataType: Type, val body: Any?, val cacheDuration: Int) {
    val startedAt = System.currentTimeMillis()
}