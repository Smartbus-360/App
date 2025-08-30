package com.smartbus360.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun NetworkSnackbar(
    isConnected: Boolean,
    snackbarHostState: androidx.compose.material3.SnackbarHostState
) {
    LaunchedEffect(isConnected) {
        if (!isConnected) {
        snackbarHostState.showSnackbar(
                message = "No internet connection",
                actionLabel = "Dismiss"
            )
        } else {
//            scaffoldState.snackbarHostState.showSnackbar(
//                message = "Internet connection restored",
//                actionLabel = "OK"
//            )
        }
    }
}
