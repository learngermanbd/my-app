package com.streamapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object ConnectivityUtil {

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getErrorTitle(context: Context): String {
        return if (isOnline(context)) {
            "Server unreachable"
        } else {
            "No internet connection"
        }
    }

    fun getErrorDescription(context: Context): String {
        return if (isOnline(context)) {
            "Could not reach the server. Check that the server is running and your IP is correct."
        } else {
            "Your device appears to be offline. Please check Wi-Fi or mobile data."
        }
    }
}
