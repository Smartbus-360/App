package com.smartbus360.app.viewModels

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.ViewModel
import com.smartbus360.app.utility.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//class NetworkViewModel(private val networkMonitor: NetworkMonitor) : ViewModel() {
//    val isConnected: StateFlow<Boolean> = networkMonitor.isConnected
//
//    override fun onCleared() {
//        super.onCleared()
//        networkMonitor.stopMonitoring()
//    }
//}


class NetworkViewModel(context: Context) : ViewModel() {

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> get() = _isConnected

    private val networkReceiver = NetworkBroadcastReceiver { isConnected ->
        _isConnected.value = isConnected
    }

    init {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkReceiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister the receiver when the ViewModel is cleared

    }

    // BroadcastReceiver to detect network changes
//    private class NetworkBroadcastReceiver(private val onNetworkChange: (Boolean) -> Unit) : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent?) {
//            val isConnected = isNetworkAvailable(context)
//            onNetworkChange(isConnected)
//        }
//
//        private fun isNetworkAvailable(context: Context): Boolean {
//            val connectivityManager =
//                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val network = connectivityManager.activeNetwork ?: return false
//            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//        }
//    }

    private class NetworkBroadcastReceiver(private val onNetworkChange: (Boolean) -> Unit) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val isConnected = isNetworkAvailable(context)
            onNetworkChange(isConnected)
        }

        private fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // API 23+ (Marshmallow and above)
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                // API < 23 (Lollipop and below)
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo != null && networkInfo.isConnected
            }
        }
    }


}