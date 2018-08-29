package com.swarmnyc.fulton.android.http

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import com.swarmnyc.fulton.android.util.Logger
import java.util.concurrent.atomic.AtomicInteger

@TargetApi(Build.VERSION_CODES.N)
internal class NetworkStateMonitorApplication(private val application: Application, callback: (Boolean) -> Unit) : NetworkStateMonitor(application, callback) {
    private var activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null

    override fun start() {
        if (activityLifecycleCallbacks == null) {
            // add ActivityLifecycleCallbacks to check if the app is in background or foreground.
            activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
                val counter = AtomicInteger()
                override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity?) {
                    if (counter.getAndIncrement() == 0) {
                        // start monitor is in foreground
                        Logger.Network.d("Monitor Start")

                        super@NetworkStateMonitorApplication.start()
                    }
                }

                override fun onActivityResumed(activity: Activity?) {}

                override fun onActivityPaused(activity: Activity?) {}

                override fun onActivityStopped(activity: Activity?) {
                    if (counter.decrementAndGet() == 0) {
                        // stop monitor is in foreground
                        Logger.Network.d("Monitor Stop")

                        super@NetworkStateMonitorApplication.stop()
                    }
                }

                override fun onActivityDestroyed(activity: Activity?) {}
                override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
            }

            try {
                application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
            } catch (e: Throwable) {
                Logger.Network.e("registerActivityLifecycleCallbacks failed", e)
            }
        }
    }

    override fun stop() {
        super.stop()
        try {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
            activityLifecycleCallbacks = null
        } catch (e: Throwable) {
            Logger.Network.e("unregisterActivityLifecycleCallbacks failed", e)
        }
    }
}