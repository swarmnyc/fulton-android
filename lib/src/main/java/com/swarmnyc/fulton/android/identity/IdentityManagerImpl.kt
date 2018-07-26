package com.swarmnyc.fulton.android.identity

import android.content.Context
import com.swarmnyc.fulton.android.util.decodeBase64Url
import com.swarmnyc.fulton.android.util.fromJson
import com.swarmnyc.fulton.android.util.toJson


/**
 * the class of Identity Manager, Use SharedPreferences to store accessToken and user
 * */
class IdentityManagerImpl(private val context: Context) : IdentityManager {
    companion object {
        const val SP_File = "fulton_identify"
        const val Field_AccessToken = "access_token"
        const val Field_User = "user"
    }

    private var _user: User? = null
    private var _token: AccessToken? = null

    init {
        // load data form shared preferences
        val sp = context.getSharedPreferences(SP_File, Context.MODE_PRIVATE)

        sp?.getString(Field_AccessToken, null)?.apply {
            _token = this.fromJson()
            parseJwt()
        }

        sp?.getString(Field_User, null)?.apply {
            _user = this.fromJson()
        }
    }

    override var user: User?
        get () = _user
        set(value) {
            _user = value

            if (_user == null) {
                clear()
            } else {
                context.getSharedPreferences(SP_File, Context.MODE_PRIVATE).edit().apply {
                    putString(Field_User, _user!!.toJson())
                    apply()
                }
            }
        }

    override var token: AccessToken?
        get() {
            return _token
        }
        set(value) {
            _token = value

            if (_token == null) {
                clear()
            } else {
                _token!!.expires_at = System.currentTimeMillis() + (_token!!.expires_in * 1000)
                parseJwt()

                context.getSharedPreferences(SP_File, Context.MODE_PRIVATE).edit().apply {
                    putString(Field_AccessToken, _token!!.toJson())

                    apply()
                }
            }
        }

    override fun clear() {
        _token = null
        _user = null

        context.getSharedPreferences(SP_File, Context.MODE_PRIVATE).edit().apply {
            clear()
            apply()
        }
    }

    override fun isValid(): Boolean {
        return if (_token == null) {
            false
        } else {
            _token!!.expires_at > System.currentTimeMillis()
        }
    }

    private fun parseJwt() {
        val arr = _token!!.access_token.split(".")
        if (arr.size == 3) {
            _user = arr[1].decodeBase64Url().fromJson()
        }

    }
}