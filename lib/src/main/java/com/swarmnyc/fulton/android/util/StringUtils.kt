package com.swarmnyc.fulton.android.util

import android.util.Base64
import java.net.URLEncoder


/**
 * encode regular string to Base64 string
 * */
fun String.toBase64(): String {
    return Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
}

/**
 * decode Base64 to string
 * */
fun String.fromBase64(): String {
    return String(Base64.decode(this.toByteArray(), Base64.DEFAULT))
}

/**
 * encode string to Base64 Url
 * */
fun String.toBase64Url(): String {
    return Base64.encodeToString(this.toByteArray(), Base64.URL_SAFE)
}

/**
 * decode Base64 Url to string
 * */
fun String.fromBase64Url(): String {
    return String(Base64.decode(this.toByteArray(), Base64.URL_SAFE))
}

/**
 * decode Base64 string to regular string
 * */
fun String.decodeBase64(): ByteArray {
    return Base64.decode(this.toByteArray(), Base64.DEFAULT)
}

/**
 * decode Base64 string to regular string
 * */
fun String.decodeBase64Url(): ByteArray {
    return Base64.decode(this.toByteArray(), Base64.URL_SAFE)
}

/**
 * decode Base64 string to regular string
 * */
fun String.urlEncode(): String {
    return URLEncoder.encode(this, "UTF-8")
}