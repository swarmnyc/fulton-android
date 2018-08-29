package com.swarmnyc.fulton.android.util

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.support.test.InstrumentationRegistry

object NetworkUtils {
    fun startWifi() {
        startNetwork(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun stopWifi() {
        stopNetwork(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun startData() {
        startNetwork(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun stopData() {
        stopNetwork(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private val typeMaps = mapOf(NetworkCapabilities.TRANSPORT_WIFI to "wifi", NetworkCapabilities.TRANSPORT_CELLULAR to "data")

    private fun startNetwork(type: Int) {
        InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("svc ${typeMaps[type]} enable")

        val context = InstrumentationRegistry.getContext()
        val connectivityManager = context.getSystemService<ConnectivityManager>(ConnectivityManager::class.java)

        for (i in 0..100) {
            connectivityManager.allNetworks.forEach {
                val capabilities = connectivityManager.getNetworkCapabilities(it)

                if (capabilities.hasTransport(type)) {
                    val info = connectivityManager.getNetworkInfo(it)
                    if (info.isConnected) {
                        return
                    }
                }
            }

            Thread.sleep(100)
        }

        throw Exception("Start ${typeMaps[type]} failed")
    }

    private fun stopNetwork(type: Int) {
        InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("svc ${typeMaps[type]} disable")

        val context = InstrumentationRegistry.getContext()
        val connectivityManager = context.getSystemService<ConnectivityManager>(ConnectivityManager::class.java)

        for (i in 0..100) {
            var hasNetwork = false

            connectivityManager.allNetworks.forEach {
                val capabilities = connectivityManager.getNetworkCapabilities(it)

                hasNetwork = hasNetwork or capabilities.hasTransport(type)
            }

            if (!hasNetwork) {
                return
            }

            Thread.sleep(100)
        }

        throw Exception("Stop ${typeMaps[type]} failed")
    }
}