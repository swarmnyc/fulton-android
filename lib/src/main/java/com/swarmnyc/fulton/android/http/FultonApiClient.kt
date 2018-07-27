package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.error.ApiError
import com.swarmnyc.fulton.android.error.FultonApiError
import com.swarmnyc.fulton.android.error.HttpApiError

/**
 * Api Client for Fulton
 * */
abstract class FultonApiClient : ApiClient() {
    protected var identityManager = context.identityManager

    override fun initRequest(req: Request) {
        if (identityManager.isValid()) {
            identityManager.token!!.apply {
                if (tokenType.toLowerCase() == "bearer") {
                    identityManager.token!!.apply {
                        req.headers("Authorization" to "$tokenType $accessToken")
                    }
                }
            }
        }
    }

    protected inline fun <reified T> list(queryParams: QueryParams? = null, noinline builder: (Request.() -> Unit)? = null): ApiPromise<ApiManyResult<T>> {
        return request {
            this.subResultType(T::class.java)

            this.queryParams = queryParams

            if (builder != null) builder(this)
        }
    }

    override fun createError(req: Request, res: Response): ApiError {
        return if (res.isJson) {
            FultonApiError(req, res)
        } else {
            HttpApiError(req, res)
        }
    }
}

