package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.fulton.android.error.ApiError
import com.swarmnyc.fulton.android.error.FultonApiError
import com.swarmnyc.fulton.android.error.HttpApiError
import java.util.ArrayList

/**
 * Api Client for Fulton
 * */
abstract class FultonApiClient : ApiClient() {
    protected var identityManager = Fulton.context.identityManager

    override fun initRequest(req: Request) {
        if (identityManager.isValid()) {
            req.headers("Authorization" to "bearer " + identityManager.token!!.access_token)
        }
    }

    protected inline fun <reified T> list(queryParams: QueryParams? = null, cacheDurationMs: Int? = null): ApiPromise<ApiManyResult<T>> {
        return request {
            this.subResultType(T::class.java)

            this.queryParams = queryParams
            if (cacheDurationMs != null) {
                this.cacheDurationMs = cacheDurationMs
            }
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

