package com.swarmnyc.fulton.android.http

typealias RequestCallback = (req: Request, res: Response) -> Unit

interface RequestExecutor {
    fun execute(req: Request, callback: RequestCallback)
}