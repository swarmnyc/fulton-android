package com.swarmnyc.fulton.android.identity

/**
 * IdentityManager manage user and user access token
 */
interface IdentityManager {
    /**
     * get and set the user information
     */
    var user: User?

    /**
     * get and set the user access token
     */
    var token: AccessToken?

    /**
     * remove the data from Shared Preferences
     */
    fun clear()

    /**
     * check the access token is valid or not
     */
    fun isValid(): Boolean
}

