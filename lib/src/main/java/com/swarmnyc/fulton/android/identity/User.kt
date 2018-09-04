package com.swarmnyc.fulton.android.identity

/**
 * the class of user information
 */
open class User(
        val id: String,
        var displayName: String,
        var email: String?,
        var registeredAt: String?
)