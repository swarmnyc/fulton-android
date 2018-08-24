package com.swarmnyc.fulton.android.sample

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.swarmnyc.fulton.android.Fulton
import com.swarmnyc.promisekt.Promise

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // initialize Fulton and change default options
        Fulton.init(this) {
            defaultUseGzip = true
        }

        // default uncaughtError throws errors, change the handler to avoid crashing
        Promise.uncaughtError = {
            Log.e("SampleApplication", "Uncaught Error", it)

            Toast.makeText(this, "Unexpected Error", Toast.LENGTH_LONG).show()
        }
    }
}