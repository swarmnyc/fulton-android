package com.swarmnyc.fulton.android.util

import com.swarmnyc.fulton.android.FultonContext
import com.swarmnyc.fulton.android.http.Request
import com.swarmnyc.fulton.android.http.RequestCallback
import com.swarmnyc.fulton.android.http.RequestExecutor
import com.swarmnyc.fulton.android.http.Response

abstract class RequestExecutorMock(context: FultonContext) : RequestExecutor(context) {
    override fun execute(req: Request, callback: RequestCallback) {
        val res = mockResponse(req)

        callback(req, res)
    }

    abstract fun mockResponse(req: Request): Response
}