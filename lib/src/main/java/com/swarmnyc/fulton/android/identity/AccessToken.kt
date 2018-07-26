package com.swarmnyc.fulton.android.identity

/**
 * the model of access accessToken
 */
data class AccessToken(
        /**
         * the access accessToken which reparents the user, usually it is AWT format, the payload it basic user information
         */
        val access_token: String,

        /**
         * the accessToken type, usually it is bearer
         */
        val token_type: String,

        /**
         * the time in seconds that the access accessToken expires
         */
        val expires_in: Long
) {
    /**
     * the calculated time (not return from server), the value is current mill-seconds + expires_in * 1000
     */
    var expires_at: Long = 0
}