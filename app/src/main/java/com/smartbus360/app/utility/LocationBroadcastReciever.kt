package com.smartbus360.app.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.smartbus360.app.ui.screens.isLocationEnabled

class LocationBroadcastReceiver(private val onLocationStatusChanged: (Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val isLocationEnabled = isLocationEnabled(context)
            onLocationStatusChanged(isLocationEnabled)
        }
    }
}
