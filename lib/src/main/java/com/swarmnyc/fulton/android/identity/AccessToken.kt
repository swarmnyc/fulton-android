package com.swarmnyc.fulton.android.identity

import com.google.gson.annotations.SerializedName

/**
 * the model of access accessToken
 */
data class AccessToken(
        /**
         * the access accessToken which reparents the user, usually it is AWT format, the payload it basic user information
         */
        @SerializedName("access_token") val accessToken: String,

        /**
         * the accessToken type, usually it is bearer
         */
        @SerializedName("token_type") val tokenType: String,

        /**
         * the time in seconds that the access accessToken expires
         */
        @SerializedName("expires_in") val expiresIn: Long
) {
    /**
     * the calculated time (not return from server), the value is current mill-seconds + expires_in * 1000
     */
    var expiresAt: Long = 0
}