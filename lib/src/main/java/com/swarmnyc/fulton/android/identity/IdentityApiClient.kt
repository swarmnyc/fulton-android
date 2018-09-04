package com.swarmnyc.fulton.android.identity

import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.FultonContext
import com.swarmnyc.fulton.android.http.ApiOneResult
import com.swarmnyc.fulton.android.http.FultonApiClient
import com.swarmnyc.fulton.android.http.Method
import com.swarmnyc.fulton.android.http.Request
import com.swarmnyc.promisekt.Promise

/**
 * the implementation of Account Api Client
 */
abstract class IdentityApiClient<T : User>(context: FultonContext = Fulton.context) : FultonApiClient(context) {
    open fun login(username: String, password: String): Promise<AccessToken> {
        val p = request<AccessToken> {
            method = Method.POST
            paths("login")
            body = mapOf("username" to username, "password" to password)
        }

        p.then(::setToken)

        return p
    }

    open fun register(username: String, email: String, password: String): Promise<AccessToken> {
        val p = request<AccessToken> {
            method = Method.POST
            paths("register")
            body = mapOf("username" to username, "email" to email, "password" to password)
        }

        p.then(::setToken)

        return p
    }

    open fun googleLogin(code: String): Promise<AccessToken> {
        val p = request<AccessToken> {
            cacheDurationMs = 0
            paths("google/callback")
            query("code" to code, "noRedirectUrl" to "true")
        }

        p.then(::setToken)

        return p
    }

    open fun facebookLogin(code: String): Promise<AccessToken> {
        val p = request<AccessToken> {
            cacheDurationMs = 0
            paths("facebook/callback")
            query("access_token" to code)
        }

        p.then(::setToken)

        return p
    }

    open fun oauthLogin(provider: String, vararg params: Pair<String, String>): Promise<AccessToken> {
        val p = request<AccessToken> {
            cacheDurationMs = 0
            paths("$provider/callback")
            query(*params)
        }

        p.then(::setToken)

        return p
    }

    open fun profile(): Promise<T> {
        val req = Request().apply {
            cacheDurationMs = 0
            resultType = context.userType
            paths("profile")
        }

        return request(req)
    }

    open fun updateProfile(user: T): Promise<Unit> {
        return request {
            method = Method.POST
            paths("profile")
            body = mapOf("data" to user)
        }
    }

    open fun forgotPassword(email: String): Promise<ForgotPasswordResult> {
        return request {
            method = Method.POST
            resultType = ApiOneResult::class.java
            resultTypeGenerics(ForgotPasswordResult::class.java)
            paths("forgot-password")
            body = mapOf("email" to email)
        }
    }

    open fun verifyResetPasswordCode(token: String, code: String): Promise<Unit> {
        return request {
            method = Method.POST
            paths("verify-reset-password")
            body = mapOf("token" to token, "code" to code)
        }
    }

    open fun resetPassword(token: String, code: String, password: String): Promise<Unit> {
        return request {
            method = Method.POST
            paths("reset-password")
            body = mapOf("token" to token, "code" to code, "password" to password)
        }
    }

    open fun changePassword(oldPassword: String, newPassword: String): Promise<Unit> {
        return request {
            method = Method.POST
            paths("change-password")
            body = mapOf("oldPasswordoldPassword" to oldPassword, "newPassword" to newPassword)
        }
    }

    open fun logout(): Promise<Unit> {
        val p = request<Unit> {
            cacheDurationMs = 0
            paths("logout")
        }

        p.then {
            identityManager.clear()
        }

        return p
    }

    private fun setToken(token: AccessToken) {
        context.identityManager.token = token
    }
}