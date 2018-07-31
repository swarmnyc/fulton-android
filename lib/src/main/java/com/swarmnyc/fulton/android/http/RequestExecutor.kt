package com.swarmnyc.fulton.android.http

typealias RequestCallback = (req: Request, res: Response) -> Unit

abstract class RequestExecutor {
    abstract fun execute(req: Request, callback: RequestCallback)
}