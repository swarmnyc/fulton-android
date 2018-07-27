package com.swarmnyc.fulton.android.http

abstract class MockRequestExecutor : RequestExecutor() {
    override fun execute(req: Request, callback: RequestCallback) {
        val res = mockResponse(req)

        callback(req, res)
    }

    abstract fun mockResponse(req: Request): Response
}