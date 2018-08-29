package com.swarmnyc.fulton.android.http

import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import com.swarmnyc.fulton.android.util.Logger

@TargetApi(Build.VERSION_CODES.N)
internal open class NetworkStateMonitor(private val context: Context, val callback: (Boolean) -> Unit) {
    private var networkCallback: ConnectivityManager.NetworkCallback? = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            Logger.Network.d("onLost")
            callback(false)
        }

        override fun onAvailable(network: Network?) {
            Logger.Network.d("onAvailable")
            callback(true)
        }
    }

    open fun start() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerDefaultNetworkCallback(networkCallback)

            if (connectivityManager.activeNetwork == null) {
                // if there is not active network, NetworkCallback would not be triggered
                callback(false)
            }
        } catch (e: Throwable) {
            Logger.Network.e("registerDefaultNetworkCallback failed", e)
        }
    }

    open fun stop() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Throwable) {
            Logger.Network.e("unregisterNetworkCallback failed", e)
        }
    }
}