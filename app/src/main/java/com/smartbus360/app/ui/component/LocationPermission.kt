package com.smartbus360.app.ui.component

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LocationPermissionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Initial permissions check
    val hasForegroundLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    val hasBackgroundLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val hasNotificationPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Polite message state
    var showPoliteMessage by remember {
        mutableStateOf(!hasForegroundLocationPermission || !hasNotificationPermission)
    }

    // Dialog state variables
    var showExitTimer by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(5) }
    var showBackgroundPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Update the permissions check in the LaunchedEffect
    LaunchedEffect(hasForegroundLocationPermission, hasBackgroundLocationPermission, hasNotificationPermission) {
        // Trigger navigation when permissions are granted
        if (hasForegroundLocationPermission  && hasNotificationPermission) {
            navController.navigate("main") {
                popUpTo("home") { inclusive = true }
            }
        }
    }


    LaunchedEffect(showExitTimer) {
        if (showExitTimer) {
            while (countdown > 0) {
                delay(1000)
                countdown -= 1
            }
            activity?.finish() // Exit the app
        }
    }

    // Permissions to request
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val isForegroundLocationGranted = permissionsResult[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissionsResult[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        val isBackgroundLocationGranted = permissionsResult[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true

        val isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsResult[Manifest.permission.POST_NOTIFICATIONS] == true
        } else {
            true
        }

        when {
            (isForegroundLocationGranted  )|| isNotificationGranted -> {
                // All permissions granted
                navController.navigate("main") {
                    popUpTo("home") { inclusive = true }
                }
            }
//            isForegroundLocationGranted -> {
//                if (!isBackgroundLocationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    showBackgroundPermissionDialog = true
//                } else {
//                    showPermissionDeniedDialog = true
//                }
//            }
            else -> {
                showPermissionDeniedDialog = true
            }
        }
    }

    // Polite message dialog
    if (showPoliteMessage) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Location permission required") },
            text = {
                Text("SmartBus360 app collects location data to enable the following features, even when the app is in background: \n" +
                        "1. Fetching the driver's real-time location\n" +
                        "2. Displaying your current location's address and coordinates\n" +
                        "3. Mapping your location on the map\n" +
                        "4. Calculating your speed.\n\n" +
                        "To continue using these features, please grant location access.")

            },
            confirmButton = {
                TextButton(onClick = {
                    showPoliteMessage = false
                    permissionLauncher.launch(permissions.toTypedArray())
                }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = {
//                    showExitTimer = true
                    showPoliteMessage = false
                    navController.navigate("main") {
                        popUpTo("home") { inclusive = true }
                    }

                }) {
                    Text("Deny")
                }
            }
        )
    }



    // Permission denied dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Permissions Denied") },
            text = { Text("Permissions are required for this app to work. Please grant them in Settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDeniedDialog = false
                    showExitTimer = true
                }) {
                    Text("Exit")
                }
            }
        )
    }

    // Exit countdown dialog
    if (showExitTimer) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Exiting App") },
            text = { Text("Permissions are required for this app to work. Exiting in $countdown seconds.") },
            confirmButton = { }
        )
    }
}



@RequiresApi(Build.VERSION_CODES.R)
fun getBackgroundPermissionOptionLabel(context: Context): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val packageManager = context.packageManager
        return packageManager.backgroundPermissionOptionLabel.toString()
    }
    return null
}

//@RequiresApi(Build.VERSION_CODES.Q)
//fun requestPermissions(context: Context) {
//    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//        Toast.makeText(context, "Permissions already granted", Toast.LENGTH_SHORT).show()
//    } else {
//        ActivityCompat.requestPermissions(
//            context as ComponentActivity,
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
//            1001 // Example request code
//        )
//    }
//}
fun requestPermissions(context: Context) {
    // Request background location permission if not granted
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            context as ComponentActivity,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            1001 // Example request code
        )
    }
}

// Background permission dialog
//if (hasForegroundLocationPermission && !hasBackgroundLocationPermission) {
//    AlertDialog(
//        onDismissRequest = { showBackgroundPermissionDialog = false },
//        title = { Text("Background Location Required") },
//        text = {
//            Text("To provide real-time location tracking, the app requires 'Allow All the Time' background location access. Please grant it in the next prompt.")
//        },
//        confirmButton = {
//            TextButton(onClick = {
//                showBackgroundPermissionDialog = false
//                permissionLauncher.launch(
//                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                )
//            }) {
//                Text("Grant")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = {
//                showExitTimer = true
//                showBackgroundPermissionDialog = false
//            }) {
//                Text("Cancel")
//            }
//        }
//    )
//}

@Composable
fun ObservePermissionLifecycle(
    onPermissionChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Recheck permission status when the app resumes
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                onPermissionChange(hasPermission) // Notify parent about the updated state
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}


