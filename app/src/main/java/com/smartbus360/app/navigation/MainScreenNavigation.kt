package com.smartbus360.app.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.smartbus360.app.R
import com.smartbus360.app.data.model.response.GetDriverDetailResponseNewXX
import com.smartbus360.app.data.olaMaps.RouteViewModel
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.ui.screens.AboutScreen
import com.smartbus360.app.ui.screens.BusLocationScreen
import com.smartbus360.app.ui.screens.CreditsScreen
import com.smartbus360.app.ui.screens.LatLngPlace
import com.smartbus360.app.ui.screens.LocationDisplay
import com.smartbus360.app.ui.screens.LocationHandler
import com.smartbus360.app.ui.screens.MainScreen
import com.smartbus360.app.ui.screens.RoleSelectionScreen
import com.smartbus360.app.ui.screens.SettingsLanguageSelectionScreen
import com.smartbus360.app.ui.screens.SettingsScreen
import com.smartbus360.app.ui.screens.createSocket
import com.smartbus360.app.ui.screens.emitLocationUpdate
import com.smartbus360.app.ui.screens.getPlaceNameFromLatLng
import com.smartbus360.app.ui.screens.isLocationEnabled
import com.smartbus360.app.ui.screens.promptEnableLocation
import com.smartbus360.app.ui.screens.startLocationUpdates
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.ui.webView.WebViewScreen
import com.smartbus360.app.utility.LocationBroadcastReceiver
import com.smartbus360.app.viewModels.BusLocationScreenViewModel
import com.smartbus360.app.viewModels.LanguageViewModel
import com.smartbus360.app.viewModels.MainScreenViewModel
import com.smartbus360.app.viewModels.RoleSelectionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.smartbus360.app.data.model.request.BusReachedStoppageRequest
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.background


@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MutableCollectionMutableState")
@Composable
fun MainScreenNavigation(
    navController: NavHostController ,
    busLocationScreenViewModel: BusLocationScreenViewModel = getViewModel(),
   routeViewModel: RouteViewModel = getViewModel()
) {

    val coroutineScope = rememberCoroutineScope()
    val navControllerBottomBar = rememberNavController()
//    val navControllerBottomBar = navController
    var placeName by remember { mutableStateOf("Fetching location...") }

    val context = LocalContext.current
    val busLocation = remember { mutableStateOf(LatLngPlace(20.5937, 78.962, "New Delhi")) }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    val state = busLocationScreenViewModel.state.collectAsState()
    var stoppages = remember { mutableStateOf(state.value.routes) }

    var repository = PreferencesRepository(context)
    var started by remember { mutableStateOf(repository.startedSwitchState()) }
    val socket: Socket?
    var nextStoppage by remember { mutableStateOf("") }
    var currentSpeed by remember { mutableFloatStateOf(0f) }
    val speedReadings = remember { mutableStateListOf<Float>() }
    var message by remember { mutableStateOf("Waiting for location updates...") }
    var speed by remember { mutableFloatStateOf(0f) }
    val stateBusReplaced = busLocationScreenViewModel.busReplacedStatus.collectAsState()
    val counter = remember { mutableIntStateOf(0) }

    // Track if we've drawn the first frame (so we can defer heavy work)
    var firstFramePassed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withFrameNanos { /* first frame rendered */ }
        firstFramePassed = true
    }

    // Optional: keep a short minimum overlay time to avoid blink
    var showOverlay by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(400) // 300–500ms feels nice
        showOverlay = false
    }
    val contentReady = firstFramePassed && !showOverlay

    // Mutable set to store IDs of triggered stoppages with explicit type
    val triggeredStoppages = remember { mutableStateOf(mutableSetOf<String>()) }
    val exitedStoppages = remember { mutableStateOf(mutableSetOf<String>()) }


    val fusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    // Fine Location permission state (for foreground location access)
    val fineLocationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Background Location permission state (only needed if API level >= 29)
    val backgroundLocationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//    val  context = LocalContext.current

    val activity = context as? Activity // Ensure the context is an Activity

    LaunchedEffect(firstFramePassed) {
        if (!firstFramePassed) return@LaunchedEffect
        val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    activity?.let {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            it,
                            100
                        )
                    }
                } catch (_: Exception) { /* ignore */
                }
            }
        }
    }

    socket = remember {
        createSocket("drivers", repository.getAuthToken() ?: "NULL")
    }


//    val appUpdateManager: AppUpdateManager = remember { AppUpdateManagerFactory.create(context) }
//
//    // State to track if an update is available
//    var updateAvailable by remember { mutableStateOf(false) }
//
//
//    val appUpdateInfoTask = appUpdateManager.appUpdateInfo
//    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
//        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
//            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
//        ) {
//            updateAvailable = true
//
//            try {
//                if (activity != null) {
//                    appUpdateManager.startUpdateFlowForResult(
//                        appUpdateInfo,
//                        AppUpdateType.IMMEDIATE,
//                        activity,
//                        100 // Request code for tracking
//                    )
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast
//                    .makeText(
//                        context,
//                        "Failed to start update",
//                        Toast.LENGTH_SHORT
//                    )
//                    .show()
//            }
//
//        }
//    }
//

// Reached bus stoppage notification coding.
    if (state.value.success == true) {

        var isLocationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }

        // Trigger permission request on first launch
//        LaunchedEffect(Unit) {
//            fineLocationPermissionState.launchPermissionRequest()
//        }

        // If fine location is granted, trigger background location request if needed
        LaunchedEffect(fineLocationPermissionState.status) {

            if (fineLocationPermissionState.status.isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                backgroundLocationPermissionState.launchPermissionRequest()
            }

        }

        // Trigger fetchGeoCode when latitude or longitude changes

//        LaunchedEffect(latitude, longitude) {
//            if (!isInternetAvailable(context)) {
////                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
//            } else {
////                Toast.makeText(context, "Unable to connect to server. Please try again later.", Toast.LENGTH_SHORT).show()
//                try {
//                    routeViewModel.fetchGeoCode("$latitude,$longitude")
//                    delay(1000)
//                    routeViewModel.fetchRoutes(
//                        "$latitude,$longitude",
//                        "12.993103152916301,77.54332622119354",
//                        ""
//                    )
//
//                } catch (e: Exception) {
//                    Log.e("LocationUpdate", "Error: ${e.message}")
//                }
//
//            }
////            routeViewModel.fetchGeoCode("$latitude,$longitude")
//        }

        // Observe the result from the ViewModel
        val geoCodeResult by routeViewModel.geoCodeResponse.collectAsState(initial = "")

        // Request location permission launcher

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    startLocationUpdates(context, fusedLocationProviderClient) { location ->
                        latitude = location.latitude
                        longitude = location.longitude

                        coroutineScope.launch(Dispatchers.IO) {
//                        placeName = getPlaceNameFromLatLng(context, latitude, longitude)
//                        placeName = routeViewModel.fetchGeoCode("${latitude},${longitude}")

//                         emitLocationUpdate(socket, 1, latitude, longitude)
                        }

                    }
                } else {
                    placeName = "Permission denied"

                }
            }
        )
        // 2) Do permission check AFTER first frame, using the launcher you defined above
        LaunchedEffect(firstFramePassed) {
            if (!firstFramePassed) return@LaunchedEffect
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startLocationUpdates(context, fusedLocationProviderClient) { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            placeName = getPlaceNameFromLatLng(context, latitude, longitude)
                            val kmh = location.speed * 3.6f
                            speed = if (kmh >= 1) 0.2f * kmh + 0.8f * speed else 0f
                        } catch (e: Exception) {
                            Log.e("LocationUpdate", "Error: ${e.message}")
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }


        // Check if permission is already granted; if not, request it
//        LaunchedEffect(Unit) {
//            if (ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                startLocationUpdates(context, fusedLocationProviderClient) { location ->
//                    latitude = location.latitude
//                    longitude = location.longitude
//                    coroutineScope.launch(Dispatchers.IO) {
//                        try {
//                            placeName = getPlaceNameFromLatLng(context, latitude, longitude)
//
//                            val currentSpeed = location.speed * 3.6f // Speed in km/h
//                            if (currentSpeed >= 1) {
//                                val alpha = 0.2f
//                                speed = alpha * currentSpeed + (1 - alpha) * speed
//                            } else {
//                                speed = 0f
//                            }
//
////                            if (currentSpeed >= 1) {
////                                speedReadings.add(currentSpeed)
////
////                                if (speedReadings.size > 5) {
////                                    speedReadings.removeAt(0)
////                                }
////
////                                speed = speedReadings.average().toFloat()
////                            } else {
////                                speed = 0f
////                            }
//                        } catch (e: Exception) {
//                            Log.e("LocationUpdate", "Error: ${e.message}")
//                        }
//                    }
//                }
//            } else {
//                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
//                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            }
//        }


        // Register the BroadcastReceiver for monitoring location changes
        DisposableEffect(context) {
            val receiver = LocationBroadcastReceiver { enabled ->
                isLocationEnabled = enabled
            }
            val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            context.registerReceiver(receiver, intentFilter)

            onDispose {
                context.unregisterReceiver(receiver)
            }
        }

        // Show a prompt if location is disabled
        if (!isLocationEnabled) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Location Required") },
                text = { Text("This app requires location services to be enabled.") },
                confirmButton = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                /* exit app logic */
                                promptEnableLocation(context)
                            }
                        ) {
                            Text("Enable Location")
                        }
                    }
                }
            )

        }

////         bus stoppage reached
//        LaunchedEffect(latitude, longitude, state.value.routes) {
//
//            if (repository.journeyFinishedState() == "morning") {
//                if (state.value.routes.isNotEmpty()) {
//                    state.value.routes.filter {
//                        it.stopType == "morning" &&
//                                it.rounds?.morning?.any { round -> round.round == it.routeCurrentRound } == true
//                    }.forEach { stoppage ->
//
//                        val stoppageId = stoppage.stoppageId.toString()
//                        val isInsideRadius =
//                            isWithinRadius(
//                                currentLatitude = latitude,
//                                currentLongitude = longitude,
//                                targetLatitude = stoppage.stoppageLatitude.toDouble(),
//                                targetLongitude = stoppage.stoppageLongitude.toDouble()
//                            )
//
//                        if (isInsideRadius &&
//                            (stoppageId !in triggeredStoppages.value || stoppageId in exitedStoppages.value)
//                        ) {
//
//                            // Remove from exitedStoppages
//                            exitedStoppages.value =
//                                exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }
//
//                            // Create a ZonedDateTime instance
//                            val reachDateTime = ZonedDateTime.now()
//                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
//                            val formattedTime = reachDateTime.format(formatter)
//
//                            coroutineScope.launch {
//                                busLocationScreenViewModel.busReachedStoppage(
//                                    BusReachedStoppageRequest(
//                                        formattedTime,
//                                        "1",
//                                        stoppageId,
//                                        stoppage.routeId.toString(),
//                                        stoppage.stopType.toString(),
//                                        stoppage.routeCurrentRound
//                                    )
//                                )
//                            }
//
//                            // Trigger notification
//                            showLocationReachedNotification(
//                                context,
//                                title = "Stoppage Reached",
//                                message = "You have reached a scheduled stop at ${stoppage.stoppageName} at $formattedTime"
//                            )
//
//                            // Add to triggered list
//                            triggeredStoppages.value =
//                                triggeredStoppages.value.toMutableSet().apply { add(stoppageId) }
//
//                            coroutineScope.launch {
//                                delay(3000) // 3-second delay
//                                counter.value += 1
//                            }
//                        }
//                        // If the stoppage was triggered but now is outside the radius, mark it as exited
//                        else if (!isInsideRadius && stoppageId in triggeredStoppages.value) {
//                            exitedStoppages.value =
//                                exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
//                            triggeredStoppages.value =
//                                triggeredStoppages.value.toMutableSet().apply { remove(stoppageId) }
//                        }
//                    }
//                }
//            } else if (repository.journeyFinishedState() == "afternoon") {
//                if (state.value.routes.isNotEmpty()) {
//                    state.value.routes.filter {
//                        it.stopType == "afternoon" &&
//                                it.rounds?.afternoon?.any { round -> round.round == it.routeCurrentRound } == true
//                    }.forEach { stoppage ->
//
//                        val stoppageId = stoppage.stoppageId.toString()
//                        val isInsideRadius = isWithinRadius(
//                            currentLatitude = latitude,
//                            currentLongitude = longitude,
//                            targetLatitude = stoppage.stoppageLatitude.toDouble(),
//                            targetLongitude = stoppage.stoppageLongitude.toDouble()
//                        )
//
//                        if (isInsideRadius &&
//                            (stoppageId !in triggeredStoppages.value || stoppageId in exitedStoppages.value)
//                        ) {
//
//                            // Remove from exitedStoppages
//                            exitedStoppages.value =
//                                exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }
//
//                            // Create a ZonedDateTime instance
//                            val reachDateTime = ZonedDateTime.now()
//                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
//                            val formattedTime = reachDateTime.format(formatter)
//
//                            coroutineScope.launch {
//                                busLocationScreenViewModel.busReachedStoppage(
//                                    BusReachedStoppageRequest(
//                                        formattedTime,
//                                        "1",
//                                        stoppageId,
//                                        stoppage.routeId.toString(),
//                                        stoppage.stopType.toString(),
//                                        stoppage.routeCurrentRound
//                                    )
//                                )
//                            }
//
//                            // Trigger notification
//                            showLocationReachedNotification(
//                                context,
//                                title = "Stoppage Reached",
//                                message = "You have reached a scheduled stop at ${stoppage.stoppageName} at $formattedTime"
//                            )
//
//                            // Add to triggered list
//                            triggeredStoppages.value =
//                                triggeredStoppages.value.toMutableSet().apply { add(stoppageId) }
//
//                            coroutineScope.launch {
//                                delay(3000) // 3-second delay
//                                counter.value += 1
//                            }
//                        }
//                        // If the stoppage was triggered but now is outside the radius, mark it as exited
//                        else if (!isInsideRadius && stoppageId in triggeredStoppages.value) {
//                            exitedStoppages.value =
//                                exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
//                            triggeredStoppages.value =
//                                triggeredStoppages.value.toMutableSet().apply { remove(stoppageId) }
//                        }
//                    }
//                }
//            } else {
//                if (state.value.routes.isNotEmpty()) {
//                    state.value.routes.filter {
//                        it.stopType == "evening" &&
//                                it.rounds?.evening?.any { round -> round.round == it.routeCurrentRound } == true
//                    }.forEach { stoppage ->
//
//                        val stoppageId = stoppage.stoppageId.toString()
//                        val isInsideRadius = isWithinRadius(
//                            currentLatitude = latitude,
//                            currentLongitude = longitude,
//                            targetLatitude = stoppage.stoppageLatitude.toDouble(),
//                            targetLongitude = stoppage.stoppageLongitude.toDouble()
//                        )
//
//                        if (isInsideRadius &&
//                            (stoppageId !in triggeredStoppages.value || stoppageId in exitedStoppages.value)
//                        ) {
//
//                            // Remove from exitedStoppages
//                            exitedStoppages.value =
//                                exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }
//
//                            // Create a ZonedDateTime instance
//                            val reachDateTime = ZonedDateTime.now()
//                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
//                            val formattedTime = reachDateTime.format(formatter)
//
//                            coroutineScope.launch {
//                                busLocationScreenViewModel.busReachedStoppage(
//                                    BusReachedStoppageRequest(
//                                        formattedTime,
//                                        "1",
//                                        stoppageId,
//                                        stoppage.routeId.toString(),
//                                        stoppage.stopType.toString(),
//                                        stoppage.routeCurrentRound
//                                    )
//                                )
//                            }
//
//                            // Trigger notification
//                            showLocationReachedNotification(
//                                context,
//                                title = "Stoppage Reached",
//                                message = "You have reached a scheduled stop at ${stoppage.stoppageName} at $formattedTime"
//                            )
//
//                            // Add to triggered list
//                            triggeredStoppages.value =
//                                triggeredStoppages.value.toMutableSet().apply { add(stoppageId) }
//
//                            coroutineScope.launch {
//                                delay(3000) // 3-second delay
//                                counter.value += 1
//                            }
//                        }
//                        // If the stoppage was triggered but now is outside the radius, mark it as exited
//                        else if (!isInsideRadius && stoppageId in triggeredStoppages.value) {
//                            exitedStoppages.value =
//                                exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
//                            triggeredStoppages.value =
//                                triggeredStoppages.value.toMutableSet().apply { remove(stoppageId) }
//                        }
//                    }
//                }
//            }
//        }


//        LaunchedEffect(key1 = latitude, key2 = longitude) {
//            busLocationScreenViewModel.checkAndNotifyStoppage(
//                latitude = latitude,
//                longitude = longitude,
//                state = state.value,
//                context = context
//            )
//        }
    }

    //         bus stoppage reached
    LaunchedEffect(latitude, longitude, state.value.success) {

        if (repository.journeyFinishedState() == "morning") {
            if (state.value.routes.isNotEmpty()) {
                state.value.routes.filter {
                    it.stopType == "morning" &&
                            it.rounds?.morning?.any { round -> round.round == it.routeCurrentRound } == true
                }.forEach { stoppage ->

                    val stoppageId = stoppage.stoppageId.toString()
                    val isInsideRadius =
                        isWithinRadius(
                            currentLatitude = latitude,
                            currentLongitude = longitude,
                            targetLatitude = stoppage.stoppageLatitude.toDouble(),
                            targetLongitude = stoppage.stoppageLongitude.toDouble()
                        )

                    if (isInsideRadius &&
                        (stoppageId !in triggeredStoppages.value || stoppageId in exitedStoppages.value)
                    ) {

                        // Remove from exitedStoppages
                        exitedStoppages.value =
                            exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }

                        // Create a ZonedDateTime instance
                        val reachDateTime = ZonedDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                        val formattedTime = reachDateTime.format(formatter)

                        coroutineScope.launch {
                            busLocationScreenViewModel.busReachedStoppage(
                                BusReachedStoppageRequest(
                                    formattedTime,
                                    "1",
                                    stoppageId,
                                    stoppage.routeId.toString(),
                                    stoppage.stopType.toString(),
                                    stoppage.routeCurrentRound
                                )
                            )
                        }

                        // Trigger notification
                        showLocationReachedNotification(
                            context,
                            title = "Stoppage Reached",
                            message = "You have reached a scheduled stop at ${stoppage.stoppageName} at $formattedTime"
                        )

                        // Add to triggered list
                        triggeredStoppages.value =
                            triggeredStoppages.value.toMutableSet().apply { add(stoppageId) }

                        coroutineScope.launch {
                            delay(3000) // 3-second delay
                            counter.value += 1
                        }
                    }
                    // If the stoppage was triggered but now is outside the radius, mark it as exited
                    else if (!isInsideRadius && stoppageId in triggeredStoppages.value) {
                        exitedStoppages.value =
                            exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
                        triggeredStoppages.value =
                            triggeredStoppages.value.toMutableSet().apply { remove(stoppageId) }
                    }
                }
            }
        } else if (repository.journeyFinishedState() == "afternoon") {
            if (state.value.routes.isNotEmpty()) {
                state.value.routes.filter {
                    it.stopType == "afternoon" &&
                            it.rounds?.afternoon?.any { round -> round.round == it.routeCurrentRound } == true
                }.forEach { stoppage ->

                    val stoppageId = stoppage.stoppageId.toString()
                    val isInsideRadius = isWithinRadius(
                        currentLatitude = latitude,
                        currentLongitude = longitude,
                        targetLatitude = stoppage.stoppageLatitude.toDouble(),
                        targetLongitude = stoppage.stoppageLongitude.toDouble()
                    )

                    if (isInsideRadius &&
                        (stoppageId !in triggeredStoppages.value || stoppageId in exitedStoppages.value)
                    ) {

                        // Remove from exitedStoppages
                        exitedStoppages.value =
                            exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }

                        // Create a ZonedDateTime instance
                        val reachDateTime = ZonedDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                        val formattedTime = reachDateTime.format(formatter)

                        coroutineScope.launch {
                            busLocationScreenViewModel.busReachedStoppage(
                                BusReachedStoppageRequest(
                                    formattedTime,
                                    "1",
                                    stoppageId,
                                    stoppage.routeId.toString(),
                                    stoppage.stopType.toString(),
                                    stoppage.routeCurrentRound
                                )
                            )
                        }

                        // Trigger notification
                        showLocationReachedNotification(
                            context,
                            title = "Stoppage Reached",
                            message = "You have reached a scheduled stop at ${stoppage.stoppageName} at $formattedTime"
                        )

                        // Add to triggered list
                        triggeredStoppages.value =
                            triggeredStoppages.value.toMutableSet().apply { add(stoppageId) }

                        coroutineScope.launch {
                            delay(3000) // 3-second delay
                            counter.value += 1
                        }
                    }
                    // If the stoppage was triggered but now is outside the radius, mark it as exited
                    else if (!isInsideRadius && stoppageId in triggeredStoppages.value) {
                        exitedStoppages.value =
                            exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
                        triggeredStoppages.value =
                            triggeredStoppages.value.toMutableSet().apply { remove(stoppageId) }
                    }
                }
            }
        } else if (repository.journeyFinishedState() == "evening") {
            if (state.value.routes.isNotEmpty()) {
                state.value.routes.filter {
                    it.stopType == "evening" &&
                            it.rounds?.evening?.any { round -> round.round == it.routeCurrentRound } == true
                }.forEach { stoppage ->

                    val stoppageId = stoppage.stoppageId.toString()
                    val isInsideRadius = isWithinRadius(
                        currentLatitude = latitude,
                        currentLongitude = longitude,
                        targetLatitude = stoppage.stoppageLatitude.toDouble(),
                        targetLongitude = stoppage.stoppageLongitude.toDouble()
                    )

                    if (isInsideRadius &&
                        (stoppageId !in triggeredStoppages.value || stoppageId in exitedStoppages.value)
                    ) {

                        // Remove from exitedStoppages
                        exitedStoppages.value =
                            exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }

                        // Create a ZonedDateTime instance
                        val reachDateTime = ZonedDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                        val formattedTime = reachDateTime.format(formatter)

                        coroutineScope.launch {
                            busLocationScreenViewModel.busReachedStoppage(
                                BusReachedStoppageRequest(
                                    formattedTime,
                                    "1",
                                    stoppageId,
                                    stoppage.routeId.toString(),
                                    stoppage.stopType.toString(),
                                    stoppage.routeCurrentRound
                                )
                            )
                        }

                        // Trigger notification
                        showLocationReachedNotification(
                            context,
                            title = "Stoppage Reached",
                            message = "You have reached a scheduled stop at ${stoppage.stoppageName} at $formattedTime"
                        )

                        // Add to triggered list
                        triggeredStoppages.value =
                            triggeredStoppages.value.toMutableSet().apply { add(stoppageId) }

                        coroutineScope.launch {
                            delay(3000) // 3-second delay
                            counter.value += 1
                        }
                    }
                    // If the stoppage was triggered but now is outside the radius, mark it as exited
                    else if (!isInsideRadius && stoppageId in triggeredStoppages.value) {
                        exitedStoppages.value =
                            exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
                        triggeredStoppages.value =
                            triggeredStoppages.value.toMutableSet().apply { remove(stoppageId) }
                    }
                }
            }
        }
    }

//       Get permissions and start GPS updates
    LocationHandler(onLocationUpdate = { newLatitude, newLongitude, location ->
        latitude = newLatitude
        longitude = newLongitude
        message = "Location updated: Latitude = $latitude, Longitude = $longitude"
        placeName = getPlaceNameFromLatLng(context, latitude, longitude)
//
//        routeViewModel.fetchGeoCode("$latitude,$longitude").toString()


//        if (socket != null && state.value.success) {
//            emitLocationUpdate(socket, state.value.driver.driverId, latitude, longitude)
//        }
        // Replace 1 with the actual driver ID
    }
    )

    LaunchedEffect(
        socket, state.value.success, stateBusReplaced.value.status
    ) {
        while (true) {
            val driverId = when {
                stateBusReplaced.value.message == "Bus has been replaced." && stateBusReplaced.value.status == true ->
                    stateBusReplaced.value.replacedBus.replacement_driver_id

                else ->
                    state.value.driver?.driverId
            }

            if (socket != null && state.value.success == true && driverId != null) {
                try {
                    emitLocationUpdate(socket, driverId, latitude, longitude, placeName, speed)
                } catch (e: Exception) {
                    e.printStackTrace() // Handle errors gracefully
                }
            }
            delay(6000) // Poll every 5 seconds
        }
    }


    LocationDisplay(
        latitude = latitude,
        longitude = longitude,
        message = message,
        placeName = placeName,
        speed = currentSpeed
    )





    busLocation.value = LatLngPlace(latitude, longitude, placeName)
    Box(Modifier.fillMaxSize()) {

        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar(navControllerBottomBar)) {
                    BottomNavigationBar(navControllerBottomBar)
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Applies padding from Scaffold
            ) {

                BottomNavGraph(
                    navControllerBottomBar,
                    PreferencesRepository(context),
                    busLocation,
                    state,
                    speed,
                    counter,
                    onForceLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }   // clear entire stack
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        if (!contentReady) {
            StartupOverlay()
        }

    }

}





@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(stringResource(R.string.home), Icons.Default.Home, "home"),
        BottomNavItem(stringResource(R.string.bus_location), Icons.Default.LocationOn, "bus_location"),
        BottomNavItem( stringResource(R.string.about), Icons.Default.Info, "about")
    )

    val currentRoute = currentRoute(navController)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.1f),
        // Add padding for the rounded effect
        color = Color.White,
//        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),  // Set rounded corners
        tonalElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Black,
            contentColor = Color.White
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = null
                        )
                    },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SmartBusSecondaryBlue,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = SmartBusSecondaryBlue,
                        indicatorColor = Color.Transparent
                    ),
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNavGraph(
    navController: NavHostController,
    repository: PreferencesRepository,
    busLocation: MutableState<LatLngPlace>,
    state: State<GetDriverDetailResponseNewXX>,
    speed: Float,
    counter: MutableIntState,
    onForceLogout: () -> Unit   // ← add this

) {
    AnimatedNavHost(
        navController = navController,
        startDestination = "home", // change based on your initial route
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut()
        }
    ) {
        composable("home") { HomeScreenContent(navController,busLocation,state,speed,counter) }
        composable("bus_location") {
            val busLocationScreenViewModel: BusLocationScreenViewModel = koinViewModel()
            BusLocationScreen(busLocation, busLocationScreenViewModel,        onForceLogout = {if (repository.isQrSession()) { onForceLogout()}}  // ← forward it
            )
//            BusLocationScreen(viewModel = RouteViewModel())

        }
        composable("about") { AboutScreen(navController)
        }
        composable("settings") {
            val languageViewModel: LanguageViewModel = koinViewModel()
            val mainScreenViewModel: MainScreenViewModel = koinViewModel()
            SettingsScreen(navController, state)
        }
        composable("settings_Language") {
            val languageViewModel: LanguageViewModel = koinViewModel()
            val mainScreenViewModel: MainScreenViewModel = koinViewModel()
            SettingsLanguageSelectionScreen(navController, languageViewModel)
        }

        composable("role") {
            val roleSelectionViewModel: RoleSelectionViewModel = koinViewModel()
            val languageViewModel: LanguageViewModel = koinViewModel()
            RoleSelectionScreen(navController, roleSelectionViewModel,languageViewModel)
        }

            composable("privacy_policy") {

            WebViewScreen("https://smartbus360.com/home/privacy-policy",navController)
        }

        composable("terms&condition") {

            WebViewScreen("https://smartbus360.com/home/terms-and-conditions",navController)
        }

        composable("creditsScreen") {

            CreditsScreen(navController)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    busLocation: MutableState<LatLngPlace>,
    state: State<GetDriverDetailResponseNewXX>,
    speed: Float,
    counter: MutableIntState
) {

//        Text(text = "Welcome to the Home Screen!")
        val  context = LocalContext.current
        val preferencesRepository = PreferencesRepository(context)
        val mainScreenViewModel: MainScreenViewModel = koinViewModel()
        MainScreen(navController, preferencesRepository, busLocation, state, speed,counter )
//        MapScreen(busLocation)
  //    DriverScreen()

}




fun isWithinRadius(currentLatitude: Double, currentLongitude: Double, targetLatitude: Double,
                   targetLongitude: Double, radiusMeters: Float = 100f): Boolean {
    val currentLocation = Location("").apply {
        latitude = currentLatitude
        longitude = currentLongitude
    }
    val targetLocation = Location("").apply {
        latitude = targetLatitude
        longitude = targetLongitude
    }
    return currentLocation.distanceTo(targetLocation) <= radiusMeters
}

fun showLocationReachedNotification(context: Context, title: String, message: String) {
    val notificationManager = NotificationManagerCompat.from(context)
    val notificationId = 2
    val channelId = "location_updates"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Location Updates",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for bus location updates"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.smartbus_nobg_2) // Use your notification icon
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    notificationManager.notify(notificationId, builder.build())
}

@Composable
fun LocationPromptDialog(onEnableClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Enable") },
        text = { Text("Location is required for this feature. Please enable it in settings.") },
        confirmButton = {
            Button(onClick = onEnableClick) {
                Text("Enable")
            }
        },
        dismissButton = {
            Button(onClick = {}) {
                Text("Cancel")
            }
        }
    )
}

// Function to check if location services are enabled
fun isLocationServiceEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

// Define this function to handle actions after location is enabled
fun onLocationEnabled() {
    // Logic to handle when location is enabled, e.g., fetching location data
    println("Location is enabled, proceeding with location-based functionality.")
}




data class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun shouldShowBottomBar(navController: NavHostController): Boolean {
    val currentRoute = currentRoute(navController)
    return currentRoute != "settings" && currentRoute != "settings_Language" && currentRoute != "role"
}

@Composable
private fun StartupOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
