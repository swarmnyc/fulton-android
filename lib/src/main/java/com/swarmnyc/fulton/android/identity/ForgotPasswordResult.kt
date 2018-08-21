package com.swarmnyc.fulton.android.identity

/**
 * the return model for Forgot Password from API,
 * this token is for identify the request with the code send to the user
 *
 * App keeps the token, and the user enters the code. Then app sends the token and code to the backend to check it is valid or not
 */
data class ForgotPasswordResult(val token: String, val expire_in: Int)