package com.swarmnyc.fulton.android.http

import com.swarmnyc.fulton.android.FultonContext

typealias RequestCallback = (req: Request, res: Response) -> Unit

abstract class RequestExecutor (val context: FultonContext) {
    abstract fun execute(req: Request, callback: RequestCallback)
}