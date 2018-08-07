package com.swarmnyc.fulton.android.promise

import android.os.Handler
import android.os.Looper
import com.swarmnyc.fulton.android.util.readWriteLazy
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

open class PromiseOptions {
    internal var debugMode = false
    internal val idCounter: AtomicInteger by lazy {
        AtomicInteger()
    }

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
