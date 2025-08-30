package com.smartbus360.app.ui.component

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.smartbus360.app.utility.BatteryOptimizationHelper

@Composable
fun BatteryOptimizationScreen() {
    val context = LocalContext.current
    val activity = context as? Activity // Ensure context is an Activity

    LaunchedEffect(Unit) {
        activity?.let {
            BatteryOptimizationHelper.checkAndRequestBatteryOptimization(it)
        }
    }
}
