package com.smartbus360.app.utility

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object BatteryOptimizationHelper {

    fun checkAndRequestBatteryOptimization(activity: Activity) {
        val packageName = activity.packageName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = activity.getSystemService(PowerManager::class.java)

            // Check if battery optimization is already ignored
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog(activity)
            }
        } else {
            // For Android 5 (Lollipop), open general battery settings
            showBatteryOptimizationDialog(activity)
        }
    }

    private fun showBatteryOptimizationDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Disable Battery Optimization")
            .setMessage("To ensure SmartBus360 works properly in the background, please disable battery optimization.")
            .setPositiveButton("Allow") { _, _ ->
                requestBatteryOptimization(activity)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun requestBatteryOptimization(activity: Activity) {
        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    // Open Battery Optimization Settings for Android 6+
                    val packageName = activity.packageName
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    activity.startActivity(intent)
                }
                Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP -> {
                    // Open Battery Saver Settings for Android 5 (Lollipop)
                    openBatterySettings(activity)
                }
                else -> {
                    // Fallback for unknown cases
                    openBatterySettings(activity)
                }
            }
        } catch (e: ActivityNotFoundException) {
            openBatterySettings(activity) // Fallback if the intent fails
        }
    }

    private fun openBatterySettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Settings.ACTION_SETTINGS) // Fallback to general settings
            activity.startActivity(intent)
        }
    }
}
