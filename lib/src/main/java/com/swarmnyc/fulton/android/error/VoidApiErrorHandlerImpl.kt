package com.swarmnyc.fulton.android.error

import com.swarmnyc.fulton.android.error.ApiError
import com.swarmnyc.fulton.android.error.ApiErrorHandler

class VoidApiErrorHandlerImpl : ApiErrorHandler {
    override fun onError(apiError: ApiError) {
    }
}