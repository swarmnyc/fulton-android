package com.swarmnyc.fulton.android.promise

import android.os.Handler
import android.os.Looper
import com.swarmnyc.fulton.android.util.readWriteLazy
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

open class PromiseOptions {
    //background executor
    var executor: ExecutorService by readWriteLazy {
        Executors.newCachedThreadPool { command ->
            Thread(command).also { thread ->
                thread.priority = Thread.NORM_PRIORITY
                thread.isDaemon = true
            }
        }
    }

    var uiExecutor: Executor by readWriteLazy {
        val handler = Handler(Looper.getMainLooper())

        Executor { command -> handler.post(command) }
    }
}
