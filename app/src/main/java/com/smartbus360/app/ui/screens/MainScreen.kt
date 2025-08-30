package com.smartbus360.app.ui.screens

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.smartbus360.app.R
import com.smartbus360.app.data.model.request.BusReachedStoppageRequest
import com.smartbus360.app.data.model.response.GetDriverDetailResponseNewXX
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.ui.theme.Inter
import com.smartbus360.app.ui.theme.InterMedium
import com.smartbus360.app.ui.theme.Montserrat
import com.smartbus360.app.ui.theme.SmartBusPrimaryBlue
import com.smartbus360.app.ui.theme.SmartBusSecondaryBlue
import com.smartbus360.app.ui.theme.SmartBusTertiaryBlue
import com.smartbus360.app.viewModels.MainScreenViewModel
import com.google.android.gms.location.LocationServices
import com.smartbus360.app.data.model.response.DriverXXXX
import com.smartbus360.app.data.model.response.UserXX
import com.smartbus360.app.ui.component.BatteryOptimizationScreen
import com.smartbus360.app.ui.component.NetworkSnackbar
import com.smartbus360.app.ui.component.ObservePermissionLifecycle
import com.smartbus360.app.viewModels.NetworkViewModel
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)

@Composable
fun MainScreen(
    navController: NavHostController,
    repository: PreferencesRepository,
    busLocation: MutableState<LatLngPlace>,
    state1: State<GetDriverDetailResponseNewXX>,
    speed: Float,
    counter: MutableIntState,
    mainScreenViewModel: MainScreenViewModel = getViewModel(),
    networkViewModel: NetworkViewModel = getViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Get the context in a composable-safe way
    val state = mainScreenViewModel.state.collectAsState()
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val currentTime = remember { LocalDateTime.now(ZoneOffset.UTC) }

    val startTime = state.value.replacedBy?.replacementStartTime?.let { LocalDateTime.parse(it, formatter) }
    val duration = state.value.replacedBy?.replacementDurationHours?.toLongOrNull()
    val endTime = startTime?.plusHours(duration ?: 0)

    val timeLeft = if (endTime != null && currentTime.isBefore(endTime)) {
        val minutes = ChronoUnit.MINUTES.between(currentTime, endTime)
        val hrs = minutes / 60
        val mins = minutes % 60
        "${hrs}h ${mins}m remaining"
    } else {
        "Replacement time is over"
    }
    val stateException = mainScreenViewModel.stateExceptionStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isConnected by networkViewModel.isConnected.collectAsState()

    val previousCounter = remember { mutableIntStateOf(counter.intValue) }
    // Reactive state for permission
    var hasBackgroundLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

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

    // Background Location permission state (only needed if API level >= 29)
    val backgroundLocationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var countBackgroundLocationPermissionAsked by remember { mutableIntStateOf(0) }

    var placeName by remember { mutableStateOf(busLocation.value.placeName) }
    var speed by remember { mutableFloatStateOf(0.0F) }
    var showExitDialog by remember { mutableStateOf(false) }
    var stopWarning by remember { mutableStateOf(false) }
    var journeyFinished by remember { mutableStateOf(false) }
    val activity = context as? Activity // Cast context to activity
    var latitude by remember { mutableDoubleStateOf(busLocation.value.latitude) }
    var longitude by remember { mutableDoubleStateOf(busLocation.value.longitude) }
    var message by remember { mutableStateOf("Waiting for location updates...") }
    // List to store the last few speed readings
    val speedReadings = remember { mutableStateListOf<Float>() }
    val authToken = repository.getAuthToken() ?: "null"
    var started by remember { mutableStateOf(repository.startedSwitchState()) }
    var backModal by remember { mutableStateOf(false) }
    val someValue = rememberSaveable { mutableStateOf("Some default value") }
    // var socket by remember { mutableStateOf<Socket?>(null) }
    var socket: Socket? = null
//    val state = mainScreenViewModel.state.collectAsState()
    var stopName by remember { mutableStateOf("") }
    var nextStoppage by remember { mutableStateOf("") }
    var currentSpeed by remember { mutableFloatStateOf(0.0F) }

    var name by remember {
        mutableStateOf(state.value.driver.driverName)
    }

    var fullname by remember {
        mutableStateOf(state.value.driver.driverName)
    }
    var stateFinalStoppage = mainScreenViewModel.state.collectAsState().value
    var finalStopReached by remember { mutableStateOf(repository.journeyFinishedState()) }
    if (stateFinalStoppage != null && stateFinalStoppage.success == true && stateFinalStoppage.routes != null) {
        val finalStopReached = stateFinalStoppage.routes.firstOrNull()?.routeCurrentJourneyPhase
//            stateFinalStoppage.routes.firstOrNull()?.routeFinalStopReached == 1
        repository.setJourneyFinished(finalStopReached)
    }



    if (state.value.success == true) {

//        com.smartbus360.app.ui.screens.BatteryOptimizationScreen()


        // LaunchedEffect to navigate when the counter value changes
        LaunchedEffect(counter.value) {
            if (counter.value != previousCounter.value) { // Check if the counter value has changed
                previousCounter.value = counter.value // Update the previous value
                delay(1000)
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }


        // Observer for lifecycle changes
        ObservePermissionLifecycle { updatedPermission ->
            hasBackgroundLocationPermission = updatedPermission
        }
        NotificationPermissionRequest()


        if (repository.journeyFinishedState() == "afternoon") {
            if (state.value.routes.isNotEmpty()) {
                val routes = state.value.routes.filter {
                    it.stopType == "afternoon" &&
                            it.rounds?.afternoon?.any { round -> round.round == it.routeCurrentRound } == true
                }

                // Reverse for journeyFinished state
                val missedStoppages = mutableListOf<Int>() // Track indices of missed stoppages
                var nextStopIndex: Int? = null

                nextStoppage = when {
                    routes.all { it.stoppageReached == 1 } -> {
                        // All stoppages reached
                        "All stoppages reached"
                    }

                    else -> {
                        for ((index, route) in routes.withIndex()) {
                            if (route.stoppageReached == 0) {
                                if (nextStopIndex == null) {
                                    nextStopIndex = index // Tentatively set as next stoppage
                                }
                                missedStoppages.add(index) // Mark as missed stoppage
                            } else if (route.stoppageReached == 1 && missedStoppages.isNotEmpty()) {
                                // Found a reached stoppage after missed ones
                                missedStoppages.forEach { missedIndex ->
                                    coroutineScope.launch {
//                                        mainScreenViewModel.busReachedStoppage(
//                                            BusReachedStoppageRequest(
//                                                "null",  // Use formatted time for the request
//                                                "2",
//                                                routes[missedIndex].stoppageId.toString(),
//                                                routes[missedIndex].routeId.toString(),
//                                                routes[missedIndex].stopType.toString(),
//                                                routes[missedIndex].routeCurrentRound
//
//                                            )
//                                        )
                                    }
                                }
                                missedStoppages.clear() // Clear missed stoppages after processing
                                nextStopIndex = null // Reset to find the next valid stoppage
                            }
                        }

                        // Determine the next stoppage
                        nextStopIndex?.let {
                            "Stop ${it + 1}\n${routes[it].stoppageName}" // Adjusted for reversed list  routes.size - it-1
                        } ?: "All stoppages reached"
                    }
                }
            }
        } else if (repository.journeyFinishedState() == "evening") {
            if (state.value.routes.isNotEmpty()) {
                val routes = state.value.routes.filter {
                    it.stopType == "evening" &&
                            it.rounds?.evening?.any { round -> round.round == it.routeCurrentRound } == true
                }

                // Reverse for journeyFinished state
                val missedStoppages = mutableListOf<Int>() // Track indices of missed stoppages
                var nextStopIndex: Int? = null

                nextStoppage = when {
                    routes.all { it.stoppageReached == 1 } -> {
                        // All stoppages reached
                        "All stoppages reached"
                    }

                    else -> {
                        for ((index, route) in routes.withIndex()) {
                            if (route.stoppageReached == 0) {
                                if (nextStopIndex == null) {
                                    nextStopIndex = index // Tentatively set as next stoppage
                                }
                                missedStoppages.add(index) // Mark as missed stoppage
                            } else if (route.stoppageReached == 1 && missedStoppages.isNotEmpty()) {
                                // Found a reached stoppage after missed ones
                                missedStoppages.forEach { missedIndex ->
                                    coroutineScope.launch {
//                                        mainScreenViewModel.busReachedStoppage(
//                                            BusReachedStoppageRequest(
//                                                "null",  // Use formatted time for the request
//                                                "2",
//                                                routes[missedIndex].stoppageId.toString(),
//                                                routes[missedIndex].routeId.toString(),
//                                                routes[missedIndex].stopType.toString(),
//                                                routes[missedIndex].routeCurrentRound
//                                            )
//                                        )
                                    }
                                }
                                missedStoppages.clear() // Clear missed stoppages after processing
                                nextStopIndex = null // Reset to find the next valid stoppage
                            }
                        }

                        // Determine the next stoppage
                        nextStopIndex?.let {
                            "Stop ${it + 1}\n${routes[it].stoppageName}" // Adjusted for reversed list  routes.size - it-1
                        } ?: "All stoppages reached"
                    }
                }
            }

        } else {
            if (state.value.routes.isNotEmpty()) {
                val routes = state.value.routes.filter {
                    it.stopType == "morning" &&
                            it.rounds?.morning?.any { round -> round.round == it.routeCurrentRound } == true
                }
                val missedStoppages = mutableListOf<Int>() // Track indices of missed stoppages
                var nextStopIndex: Int? = null

                nextStoppage = when {
                    routes.all { it.stoppageReached == 1 } -> {
                        // All stoppages reached
                        "All stoppages reached"
                    }

                    else -> {
                        for ((index, route) in routes.withIndex()) {
                            if (route.stoppageReached == 0) {
                                if (nextStopIndex == null) {
                                    nextStopIndex = index // Tentatively set as next stoppage
                                }
                                missedStoppages.add(index) // Mark as missed stoppage
                            } else if (route.stoppageReached == 1 && missedStoppages.isNotEmpty()) {
                                // Found a reached stoppage after missed ones
                                missedStoppages.forEach { missedIndex ->

                                    val reachDateTime =
                                        ZonedDateTime.now() // Or any specific date and time
                                    val formatter =
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS") // Adjust if needed

                                    // Format to string

                                    val formattedTime = reachDateTime.format(formatter)


                                    coroutineScope.launch {
//                                        mainScreenViewModel.busReachedStoppage(
//                                            BusReachedStoppageRequest(
//                                                formattedTime,  // Use formatted time for the request
//                                                "2",
//                                                routes[missedIndex].stoppageId.toString(),
//                                                routes[missedIndex].routeId.toString(),
//                                                routes[missedIndex].stopType,
//                                                routes[missedIndex].routeCurrentRound
//                                            )
//                                        )
                                    }
                                }
                                missedStoppages.clear() // Clear missed stoppages after processing
                                nextStopIndex = null // Reset to find the next valid stoppage
                            }
                        }

                        // Determine the next stoppage
                        nextStopIndex?.let {
                            "Stop ${it + 1}\n${routes[it].stoppageName}"
                        } ?: "All stoppages reached"
                    }
                }
            }
        }


        // Initialize SoundPool
        val soundPool = remember {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            SoundPool.Builder()
                .setMaxStreams(1) // Adjust as needed
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    load(context, R.raw.car_start, 1) // Load the sound effect
                    load(context, R.raw.car_stop, 2) // Load the sound effect
                }
        }
//if (started){
////     socket = remember { createSocket("drivers",repository.getAuthToken()?:"NULL") }
//    coroutineScope.launch {
//        navController.navigate("driver") {
//            popUpTo("home") { inclusive = false }
//        }
//    }
//
//}

        // Vibrator service
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Trigger vibration
        fun triggerVibration() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect =
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(500) // For older versions of Android
            }
        }

        // Play sound effect on start button click
        fun playSound(soundId: Int) {
            if (repository.soundSwitchState()) {
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        }

        fun playStartSound() = playSound(1)

        fun playStopSound() = playSound(2)

        // Dispose SoundPool when not needed
        DisposableEffect(Unit) {
            onDispose {
                soundPool.release()
            }
        }
        BackHandler {
            showExitDialog = true
        }


        // Background permission dialog
        if (!hasBackgroundLocationPermission && backModal) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismissal without user action */ },
                title = {
                    Text("Background Location Required")
                },
                text = {
                    Column {
                        Text("To provide real-time location tracking for students, we need 'Allow All the Time' background location access.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Please grant this permission in the next prompt so we can track the bus location even when the app is in the background.",
                            style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.primary)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        countBackgroundLocationPermissionAsked++

                        if (countBackgroundLocationPermissionAsked > 2) {
                            backModal = false // Close the modal
                            showPermissionDeniedDialog = true // Show the denial dialog
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                // Request background location access
                                backgroundLocationPermissionState.launchPermissionRequest()
                            }
                        }
                    }) {
                        Text("Grant Access", color = MaterialTheme.colors.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        backModal = false // Close the modal
                        showPermissionDeniedDialog = true // Show the denial dialog
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (!hasBackgroundLocationPermission && backModal) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismissal without user action */ },
                title = {
                    Text("Background Location Access Required")
                },
                text = {
                    Column {
                        Text("To ensure the bus's location is always visible to students, even when the app is in the background, we require 'Allow All the Time' background location access.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This permission allows us to provide real-time tracking of the bus, ensuring that the students can track its location accurately throughout their commute, whether the app is in the foreground or background.",
                            style = MaterialTheme.typography.body2.copy(color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            buildAnnotatedString {
                                append("Please grant the ")
                                withStyle(style = SpanStyle(color = Color.Blue)) {
                                    append("'Allow All the Time' ")
                                }
                                append("location permission so we can continue providing real-time updates.")
                            },
                            style = MaterialTheme.typography.body1.copy(color = Color.Black) // Default text style
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        countBackgroundLocationPermissionAsked++

                        if (countBackgroundLocationPermissionAsked > 2) {
                            backModal = false // Close the modal
                            showPermissionDeniedDialog = true // Show the denial dialog
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                // Request background location access
                                backgroundLocationPermissionState.launchPermissionRequest()
                            }
                        }
                    }) {
                        Text("Grant Access", color = MaterialTheme.colors.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        backModal = false // Close the modal
                        showPermissionDeniedDialog = true // Show the denial dialog
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

// Show settings dialog if the user denies background access
        if (showPermissionDeniedDialog && !hasBackgroundLocationPermission) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismissal without user action */ },
                title = {
                    Text("Permission Denied")
                },
                text = {
                    Column {
                        Text("To continue providing real-time bus tracking, the app requires 'Allow All the Time' background location access.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This feature ensures that students can track the bus in real-time, even when the app is not in use. Without this permission, the tracking will be limited.",
                            style = MaterialTheme.typography.body2.copy(color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            buildAnnotatedString {
                                append("Please enable ")
                                withStyle(style = SpanStyle(color = Color.Blue)) {
                                    append("'Allow All the Time' ")
                                }
                                append("in the app's settings to continue using this important feature.")
                            },
                            style = MaterialTheme.typography.body1.copy(color = Color.Black) // Default text style
                        )

                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Open app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Open Settings", color = MaterialTheme.colors.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        // Handle dismissal (e.g., exit the app or navigate back)
                        showPermissionDeniedDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }


        if (showExitDialog) {
            // Show a confirmation dialog before exiting
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                confirmButton = {
                    Button(onClick = { /* exit app logic */

                        activity?.finishAndRemoveTask()
                    }) {
                        Text(stringResource(R.string.exit))
                    }
                },
                dismissButton = {
                    Button(onClick = { showExitDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = { Text(stringResource(R.string.exit_app)) },
                text = { Text(stringResource(R.string.are_you_sure_you_want_to_exit_the_app)) }
            )
        }



        if (stopWarning) {
            // Show a confirmation dialog before exiting
            AlertDialog(
                onDismissRequest = { stopWarning = false },
                confirmButton = {
                    Button(onClick = { /* exit app logic */
                        coroutineScope.launch(Dispatchers.IO) {
                            started = false

                            playStopSound()
                            // Trigger vibration when the bus starts
                            triggerVibration()
                            stopLocationService(context)
                            socket?.let {
                                it.disconnect() // Disconnect the socket if it's initialized
                                it.close()      // Close the socket if it's initialized
                            }
                            repository.setStartedSwitch(false)
                            stopWarning = false

                        }

                    }) {
                        Text(
                            stringResource(R.string.stop_bus_title),
                        )
                    }
                },
                dismissButton = {
                    Button(onClick = { stopWarning = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.stop_bus)
                    )
                },
                text = { Text(stringResource(R.string.are_sure_you_want_to_stop_the_bus)) }
            )
        }
        // speed

        if (journeyFinished) {
            // Show a confirmation dialog before exiting
            AlertDialog(
                onDismissRequest = { journeyFinished = false },
                confirmButton = {
                    Button(onClick = { /* exit app logic */
                        coroutineScope.launch(Dispatchers.IO) {


                            repository.setJourneyFinished(
                                when (finalStopReached) {
                                    "morning" -> "afternoon"
                                    "afternoon" -> {
                                        "evening"
                                    }

                                    else -> {
                                        "morning"
                                    }
                                }
                            )


//                            repository.setJourneyFinished(true)
                            state.value.routes.firstOrNull()?.routeId?.let {
                                mainScreenViewModel.markFinalStop(
                                    it
                                )
                            }
                            withContext(Dispatchers.Main) {
//                                navController.navigate("home") {
//                                    popUpTo("home") { inclusive = false }
//                                }
                                // Restart the app
                                val intent =
                                    context.packageManager.getLaunchIntentForPackage(context.packageName)
                                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
//                                Runtime.getRuntime().exit(0) // Force exit the app


                            }
                            journeyFinished = false

                        }

                    }) {
                        Text(
                            stringResource(R.string.yes),
                        )
                    }
                },
                dismissButton = {
                    Button(onClick = { journeyFinished = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.mark_journey_finished_)
                    )
                },
                text = { Text(stringResource(R.string.are_sure_your_bus_journey_is_finished)) }
            )
        }

//
        val fusedLocationProviderClient =
            remember { LocationServices.getFusedLocationProviderClient(context) }
//
//     // Request location permission launcher
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    startLocationUpdates(context, fusedLocationProviderClient) { location ->
                        latitude = location.latitude
                        longitude = location.longitude

                        coroutineScope.launch(Dispatchers.IO) {
                            placeName = getPlaceNameFromLatLng(context, latitude, longitude)
//                         emitLocationUpdate(socket, 1, latitude, longitude)
                        }

                    }
                } else {
                    placeName = "Permission denied"
                }
            }
        )
//
//     // Check if permission is already granted; if not, request it
        // speed calculation
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startLocationUpdates(context, fusedLocationProviderClient) { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    coroutineScope.launch(Dispatchers.IO) {
                        placeName = getPlaceNameFromLatLng(context, latitude, longitude)
                        //   emitLocationUpdate(socket, 1, latitude, longitude)
                        currentSpeed = location.speed * 3.6f // Speed in km/h

                        // Filter out very small speed changes (e.g., below 1 km/h)
                        if (currentSpeed >= 1) {
                            speedReadings.add(currentSpeed)

                            // Keep only the last 5 readings
                            if (speedReadings.size > 5) {
                                speedReadings.removeAt(0)
                            }

                            // Calculate the average speed
                            speed = speedReadings.average().toFloat()
                        } else {
                            // If speed is below 1 km/h, consider it as stationary
                            speed = 0f
                        }
                    }
                }
            } else {
                // Request permission if not already granted
//                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
//
//
//
////       Get permissions and start GPS updates
        LocationHandler(onLocationUpdate = { newLatitude, newLongitude,Location ->
            latitude = newLatitude
            longitude = newLongitude
            message = "Location updated: Latitude = $latitude, Longitude = $longitude"
            placeName = getPlaceNameFromLatLng(context, latitude, longitude)
//         if (socket != null) {
//             emitLocationUpdate(socket, 1, latitude, longitude)
//         }
            // Replace 1 with the actual driver ID

        }
        )
//
//
        LocationDisplay(
            latitude = busLocation.value.latitude,
            longitude = busLocation.value.longitude,
            message = message,
            placeName = placeName,
            speed = currentSpeed
        )

//        BatteryOptimizationScreen() // Automatically checks and requests permission

// Remember the scale animation state
        val scale = remember { androidx.compose.animation.core.Animatable(1f) }
        val coroutineScope = rememberCoroutineScope()
        val interactionSource = remember { MutableInteractionSource() } // For interaction handling


        Scaffold(

            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.smartbus_nobg_2),
                                    contentDescription = "SmartBus Icon",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            navController.navigate("settings") {
                                                popUpTo("home") { inclusive = false }
                                            }
                                        }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SmartBus360",
                                    fontSize = 20.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isConnected) R.drawable.online_round else R.drawable.online_round
                                        ),
                                        contentDescription = if (isConnected) "Online icon" else "Offline icon",
                                        tint = if (isConnected) Color(0xFF00C853) else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )

                                    Spacer(modifier = Modifier.width(5.dp))

                                    Text(
                                        text = if (isConnected) stringResource(R.string.online) else stringResource(
                                            R.string.offline
                                        ),
                                        fontSize = 16.sp,
                                        fontFamily = Inter,
                                        color = if (isConnected) Color(0xFF00C853) else Color.Gray,
                                        fontWeight = FontWeight.ExtraLight,
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = if (isConnected && (hasForegroundLocationPermission || hasBackgroundLocationPermission)) stringResource(
                                        R.string.your_location_is_being_shared
                                    )
                                    else if (isConnected && (!hasForegroundLocationPermission || !hasBackgroundLocationPermission)) stringResource(
                                        R.string.your_location_permission_not_granted
                                    )
                                    else stringResource(R.string.location_sharing_is_paused),
                                    fontSize = 12.sp,
                                    fontFamily = Inter,
                                    color = if (isConnected) Color(0xFF00C853) else Color.Gray,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.padding(end = 2.dp)
                                )
                            }
                        }
                    },
                    backgroundColor = Color.White,
                    elevation = 4.dp
                )
            },
            floatingActionButton = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
//                        .padding(bottom = 10.dp)
                        .size(60.dp)
                        .scale(scale.value) // Apply the scaling animation
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = if (!started) {
                                    listOf(
                                        Color(0xFF00E676),
                                        Color(0xFF008000)
                                    ) // Green gradient for start
                                } else {
                                    listOf(
                                        Color(0xFFFF6F61),
                                        Color(0xFFD32F2F)
                                    ) // Red gradient for stop
                                }
                            )
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null, // Disable default ripple for custom animation
                            onClick = {
                                coroutineScope.launch {
                                    // Bounce effect
                                    scale.animateTo(
                                        targetValue = 0.9f,
                                        animationSpec = tween(durationMillis = 100)
                                    )
                                    scale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 100)
                                    )
                                }

                                // Handle the button action
                                if (!started) {
                                    if (hasBackgroundLocationPermission || Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                                        startLocationService(context)

                                        Toast
                                            .makeText(
                                                context,
                                                "Bus Started. Your location is being tracked even in background.",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                        started = true
                                        repository.setStartedSwitch(true)

//                                        if (socket != null) {
//                                            emitLocationUpdate(
//                                                socket,
//                                                1,
//                                                busLocation.value.latitude,
//                                                busLocation.value.longitude
//                                            )
//                                        }

                                        playStartSound()
                                        triggerVibration()
                                    } else {
                                        backModal = true
                                    }
                                } else {
                                    stopWarning = true
                                }
                            }
                        )
                ) {
                    Text(
                        text = if (!started) stringResource(R.string.start) else stringResource(R.string.stop),
                        fontFamily = InterMedium,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            // Display the snackbar for network changes
            NetworkSnackbar(
                isConnected = isConnected,
                snackbarHostState = snackbarHostState
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply the scaffold's padding here
                    .verticalScroll(rememberScrollState()) // Enable scrolling
            ) {
                // UI components (like top bar, cards, etc.)
                Spacer(modifier = Modifier.height(10.dp)) // Add a spacer for appropriate space


                Column(
                    modifier = Modifier.fillMaxWidth(),
//                 verticalArrangement = Arrangement.Center
//                 horizontalAlignment = Alignment.CenterHorizontally
                ) {
                     Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            stringResource(
                                R.string.hi,
                                state.value.driver.driverName?.split(" ")?.get(0) ?: "no name"
                            ),
                            fontSize = 25.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold
                        )
//            if (role == "driver") {
                        Text(
                            stringResource(R.string.drive_for_safety),
                            fontSize = 20.sp,
                            fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

//            } else {
//                Text("You are a Student")
//            }

                    Spacer(Modifier.size(15.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CustomCard(state.value.driver)

                    }
//                    Spacer(Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                            .fillMaxWidth(),
//                     horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter =
                                painterResource(id = R.drawable.material_symbols_light_speed), // Custom mail icon
                                contentDescription = "stops Icon",
                                tint = SmartBusSecondaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))

                            Text(
                                stringResource(R.string.bus_speed),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(5.dp))
//                            Text("$currentSpeed km/h")
                            Text(
                                "${"%.2f".format(currentSpeed)} km/h", fontFamily = InterMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }


                    }

                    Column(
                        modifier = Modifier

                            .padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 20.dp)
                            .fillMaxWidth(),
//                     horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.98f), verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter =
                                painterResource(id = R.drawable.location_icon), // Custom mail icon
                                contentDescription = "Current Location Icon",
                                tint = SmartBusSecondaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))

                            Text(
                                stringResource(R.string.current_place),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                placeName, fontFamily = InterMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
//                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter =
                                painterResource(id = R.drawable.location_icon), // Custom mail icon
                                contentDescription = "stops Icon",
                                tint = SmartBusSecondaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))

                            Text(
                                stringResource(R.string.next_stop),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                nextStoppage, fontFamily = InterMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter =
                                painterResource(id = R.drawable.journey_icon2), // Custom mail icon
                                contentDescription = "stops Icon",
                                tint = SmartBusSecondaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))

                            Text(
                                stringResource(R.string.journey),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Inter,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            stateFinalStoppage.routes.firstOrNull()?.routeCurrentJourneyPhase?.let {
                                Text(
                                    //                                if (repository.journeyFinishedState())
                                    //                                    "Way to Home"
                                    //                                else
                                    //                                    "Way to ${state.value.driver.institute_type?.replaceFirstChar { it.uppercase() }}"


                                    it.replaceFirstChar { char -> char.uppercaseChar() } + " Round ${state.value.driver.routeCurrentRound}",

                                    color =
                                    if (repository.journeyFinishedState() == "morning"
                                        && state.value.driver.routeCurrentRound == 1
                                    )
                                        Color(0xFF008000)
                                    else if (repository.journeyFinishedState() == "morning"
                                        && state.value.driver.routeCurrentRound == 2
                                    )
                                        Color(0xFFF722F37)
                                    else if (repository.journeyFinishedState() == "afternoon"
                                        && state.value.driver.routeCurrentRound == 1
                                    )
                                        Color(0xFF002366)
                                    else if (repository.journeyFinishedState() == "afternoon"
                                        && state.value.driver.routeCurrentRound == 2
                                    )
                                        Color(0xFFF000000)
                                    else if (repository.journeyFinishedState() == "evening"
                                        && state.value.driver.routeCurrentRound == 1
                                    )
                                        Color(0xFFFF7043)
                                    else if (repository.journeyFinishedState() == "evening"
                                        && state.value.driver.routeCurrentRound == 2
                                    )
                                        Color(0xFFFFFD700)
                                    else
                                        Color(0xFF808080),

                                    fontFamily = InterMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                    }

                    Button(

                        onClick = {
                            coroutineScope.launch {

                                journeyFinished = true

                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .height(50.dp)
                            .fillMaxWidth()
                            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
//                    .padding(horizontal = 16.dp, vertical = 16.dp)
                        ,
                        colors =
                        if (repository.journeyFinishedState() == "morning"
                            && state.value.driver.routeCurrentRound == 1
                        )
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF008000)
                            )
                        else if (
                            repository.journeyFinishedState() == "morning"
                            && state.value.driver.routeCurrentRound == 2
                        ) {
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF722F37)
                            )
                        } else if (repository.journeyFinishedState() == "afternoon"
                            && state.value.driver.routeCurrentRound == 1
                        )
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF002366)
                            )
                        else if (
                            repository.journeyFinishedState() == "afternoon"
                            && state.value.driver.routeCurrentRound == 2
                        ) {
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000)
                            )
                        } else if (
                            repository.journeyFinishedState() == "evening"
                            && state.value.driver.routeCurrentRound == 1
                        ) {
                            ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
                        } else if (
                            repository.journeyFinishedState() == "evening"
                            && state.value.driver.routeCurrentRound == 2
                        ) {
                            ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        } else
                            ButtonDefaults.buttonColors(containerColor = Color(0xFF808080))
                    ) {
                        Text(stringResource(R.string.mark_journey_finished))

                    }


                    Spacer(modifier = Modifier.height(100.dp)) // Add a spacer for appropriate space


                }

                // Other parts of the UI
            }
        }

    }
    else if (state.value.success == false && state.value.message == "You are temporarily replaced by another driver.") {

//        state.value.success == false && state.value.message == "You are temporarily replaced by another driver." &&
    //    stateException.value?.message == ""

    // Remember the scale animation state
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
//    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() } // For interaction handling


    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.smartbus_nobg_2),
                                contentDescription = "SmartBus Icon",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        navController.navigate("settings") {
                                            popUpTo("home") { inclusive = false }
                                        }
                                    }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SmartBus360",
                                fontSize = 20.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isConnected) R.drawable.online_round else R.drawable.online_round
                                    ),
                                    contentDescription = if (isConnected) "Online icon" else "Offline icon",
                                    tint = if (isConnected) Color(0xFF00C853) else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = if (isConnected) stringResource(R.string.online) else stringResource(
                                        R.string.offline
                                    ),
                                    fontSize = 16.sp,
                                    fontFamily = Inter,
                                    color = if (isConnected) Color(0xFF00C853) else Color.Gray,
                                    fontWeight = FontWeight.ExtraLight,
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (isConnected && (hasForegroundLocationPermission || hasBackgroundLocationPermission)) stringResource(
                                    R.string.your_location_is_being_shared
                                )
                                else if (isConnected && (!hasForegroundLocationPermission || !hasBackgroundLocationPermission)) stringResource(
                                    R.string.your_location_permission_not_granted
                                )
                                else stringResource(R.string.location_sharing_is_paused),
                                fontSize = 12.sp,
                                fontFamily = Inter,
                                color = if (isConnected) Color(0xFF00C853) else Color.Gray,
                                fontWeight = FontWeight.Light,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        }
                    }
                },
                backgroundColor = Color.White,
                elevation = 4.dp
            )
        },

        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    )

    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder for the illustration (Replace with actual image)
            Image(
                painter = painterResource(id = R.drawable.replace_bus), // Replace with actual drawable
                contentDescription = "Driver Replaced",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "You have been temporarily replaced",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            if (state.value.replacedBy != null) {
                Text(
                    text = "${state.value.replacedBy?.name} is now assigned to your route. $timeLeft. Please contact your admin for more details.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Acknowledge Button
            Button(
                onClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SmartBusPrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "OK", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }


    }
}
//    else {
//
////UnauthorizedScreen()
// }
}

@Composable
fun CustomCard(driver: DriverXXXX) {
    val context = LocalContext.current
    val view = LocalView.current
    var cardBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    var isImageLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(driver.driverProfilePicture) {
        isImageLoaded = false // Reset to false when the image URL changes
    }

    val imageLoader = ImageLoader.Builder(context)
        .allowHardware(false)
        .build()



    // Modifier to track the size and position of the card
    val cardModifier = Modifier
        .fillMaxWidth(0.9f)
        // .fillMaxHeight(0.38f)
        .onGloballyPositioned { coordinates ->
            // Capture the bounds (size and position) of the card
            cardBounds = coordinates.boundsInRoot()
        }

    Card(modifier = cardModifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(SmartBusTertiaryBlue),
        elevation = CardDefaults.elevatedCardElevation(20.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SmartBusPrimaryBlue,
                            SmartBusSecondaryBlue,
                            SmartBusTertiaryBlue
                        )
                    )
                )
                .padding(start = 10.dp, end = 10.dp, top = 10.dp),
//            horizontalAlignment = Alignment.Start
        ) {
//            Text("Driver Id: ${driver.driverId}", fontFamily = Roboto, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                SmartBusPrimaryBlue,
                                SmartBusSecondaryBlue,
                                SmartBusTertiaryBlue
                            )
                        )
                    ),
                horizontalArrangement = Arrangement.Center)
            {
                Text(
                    stringResource(R.string.driver_information),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold)

                Column(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(CircleShape)
                        .fillMaxWidth()
                        .shadow(0.dp, CircleShape),
                    ){
                    Icon(
                        painter = painterResource(id = R.drawable.sharing_icon),
                        contentDescription = "Share Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.End)
                            .shadow(0.dp, CircleShape)
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        delay(10)
                                        cardBounds?.let {
                                            val bitmap = captureComposableArea(view, it)
                                            val imageFile = saveBitmapToFile(context, bitmap)
                                            imageFile?.let { file ->
                                                shareImage(context, file, driver)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(
                                            "CaptureError",
                                            "Error capturing or sharing image: ${e.message}",
                                            e
                                        )
                                    }
                                }
                            }
                        ,
                        )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
//                horizontalArrangement = Alignment.CenterHorizontally
            ) {
                Column( modifier = Modifier
                    .fillMaxWidth(0.4f),
                    horizontalAlignment = Alignment.CenterHorizontally) {
//                    Image(
//                        painter = painterResource(R.drawable.jurica_koletic1),
//                        contentDescription = "Change icon",
//                        contentScale = ContentScale.FillBounds,
//                        modifier = Modifier
//                            .size(120.dp)
//                            .clip(RoundedCornerShape(20.dp))
//                    )
                    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                        Image(
                            painter = rememberImagePainter(
                                data = driver.driverProfilePicture,
                                builder = {
                                    placeholder(R.drawable.profile_icon)
                                    error(R.drawable.profile_icon)
//                                    crossfade(true)
//                                    listener(
//                                        onSuccess = { _, _ -> isImageLoaded = true },
//                                        onError = { _, _ -> isImageLoaded = true }
//                                    )
                                }
                            ),
                            contentDescription = "Driver Image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }

                    Text( driver.driverName?:"n/a", modifier = Modifier.padding(top = 4.dp),
                         fontFamily = InterMedium,
                        fontWeight = FontWeight.Medium)
                }


                Column(  modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)) {
                    Row {
                        Text(
                            stringResource(R.string.name)  , fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                        Text(
                            driver.driverName?: "n/a",  fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium )
                    }
                    Row {
                        Text(
                            stringResource(R.string.licence_no), fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                        Text(driver.driverLicense?: "n/a", fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                    }
                    Row {
                        Text(
                            stringResource(R.string.mobile), fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                        Text(
                            driver.driverPhone?: "n/a", fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                    }
                    Row {
                        Text(
                            stringResource(R.string.bus_no_driver), fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                        Text(
                            driver.busNumber?: "no bus assigned" , fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                    }
                    Row {
                        Text(
                            stringResource(R.string.email), fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                        Text(driver.driverEmail?:"n/a", fontFamily = InterMedium,
                            fontWeight = FontWeight.Medium)
                    }

                }

            }


        }
    }
}

fun captureComposableArea(view: View, bounds: androidx.compose.ui.geometry.Rect): Bitmap {
    // Create a bitmap based on the exact width and height of the card
    val bitmap = Bitmap.createBitmap(
        bounds.width.toInt(),
        bounds.height.toInt(),
        Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(bitmap)
    // Translate the canvas to the bounds of the card, so we only capture the card area
    canvas.translate(-bounds.left, -bounds.top)
    view.draw(canvas)

    return bitmap
}




fun captureComposableAsBitmap(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? {
    val filename = "Driver_detail_sharing${System.currentTimeMillis()}.png"
    val file = File(context.getExternalFilesDir(null), filename)

    try {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun shareImageViaWhatsApp(context: Context, imageFile: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider", // You need to define a FileProvider in the manifest
        imageFile
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        setPackage("com.whatsapp")  // Use this to open WhatsApp specifically
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }
}

fun shareImage(context: Context, imageFile: File, driver: DriverXXXX) {
    // Get URI for the image file using FileProvider
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider", // Ensure you have a FileProvider set up in the manifest
        imageFile
    )
    val text = "${driver.driverName} is Driving \n Contact no: ${driver.driverPhone}" +
            "\n Driver's Licence no : ${driver.driverLicense}" +
            "\n Bus no. : ${driver.busNumber}" +
            "\n Bus Model : ${driver.busModel}" +
            "\n Thank you for using SmartBus360 app."


    // Create the sharing intent
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"  // Specify the MIME type for the image
        putExtra(Intent.EXTRA_STREAM, uri)  // Attach the image file URI
        putExtra(Intent.EXTRA_TEXT, text)  // Attach the text to be shared
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Grant permission for the target app to read the URI
    }

    // Check if there is an app that can handle the intent and launch the chooser
    try {
        val chooser = Intent.createChooser(intent, "Share Image")
        context.startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
        // Show a toast message if no apps are available to handle the sharing
        Toast.makeText(context, "No app available to share the image", Toast.LENGTH_SHORT).show()
    }
}

fun shareImageFromDrawable(context: Context, drawableId: Int, user: UserXX?) {
    // Get the bitmap from the drawable resource
    val drawable = ContextCompat.getDrawable(context, drawableId)
    val bitmap = (drawable as BitmapDrawable).bitmap

    // Save the bitmap to a file
    val imageFile = saveBitmapToFile(context, bitmap)

    // Check if image file is successfully created
    imageFile?.let { file ->
        // Get URI for the image file using FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // Ensure you have a FileProvider set up in the manifest
            file
        )

        // Create the text to be shared along with the image
        val text = "${user?.driverName} is Driving \n Driver Contact no: ${user?.driverPhone}" +
                "\n Bus no.: ${user?.busNumber}" + "\n Your bus route is from ${user?.routeStart} to ${user?.routeEnd}" +
                "\n Thank you for using SmartBus360 app."


        // Create the sharing intent
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"  // Specify the MIME type for the image
            putExtra(Intent.EXTRA_STREAM, uri)  // Attach the image file URI
            putExtra(Intent.EXTRA_TEXT, text)  // Attach the text to be shared
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Grant permission for the target app to read the URI
        }

        // Launch the chooser to share the image
        try {
            val chooser = Intent.createChooser(intent, "Share Image")
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            // Show a toast message if no apps are available to handle the sharing
            Toast.makeText(context, "No app available to share the image", Toast.LENGTH_SHORT).show()
        }
    } ?: run {
        Toast.makeText(context, "Error saving image file", Toast.LENGTH_SHORT).show()
    }
}





fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val serviceChannel = NotificationChannel(
            "location_channel",
            "Location Sharing",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun promptEnableLocation(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
}





//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true)
//@Composable
//fun PreviewMainScreen() {
//    val  context = LocalContext.current
//    val preferencesRepository = PreferencesRepository(context)
////val    navController: NavHostController = rememberNavController(), // Initialize with rememberNavController()
////val    repository: PreferencesRepository = get(), // Retrieve instance via Dependency Injection (e.g., Koin or Hilt)
//  val  busLocation: MutableState<LatLngPlace> = remember { mutableStateOf(LatLngPlace(0.0, 0.0,"delhi")) } // Initial default location
//    val state: State<GetDriverDetailResponseNewXX> = remember { mutableStateOf(GetDriverDetailResponseNewXX()) } // Initial empty state for driver details
//  val  speed: Float = 0f // Initial speed value
//    val counter = remember { mutableIntStateOf(0) }
//
//   MainScreen(
//       navController = rememberNavController(),
//       preferencesRepository,
//       busLocation,
//       state,
//       speed,
//       counter
//   )
//}

@Composable
fun BatteryOptimizationDialog(
    onDismiss: () -> Unit,
    onAgree: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Keep SmartBus360 Running Smoothly",
                fontSize = 20.sp, // Increased size
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp, // Improved spacing
                color = Color(0xFF084C88) // SmartBusPrimaryBlue
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp) // Increased icon size
                        .background(Color(0xFFE3F2FD), shape = CircleShape) // Circular background
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_battery_charging_full_24),
                        contentDescription = "Battery Optimization Alert",
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(Color(0xFF2580D0)) // SmartBus360 Blue
                    )
                }
                Text(
                    text = "To ensure smooth real-time tracking, please allow SmartBus360 to run in the background by disabling battery optimization.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    lineHeight = 20.sp, // Improved readability
                    color = Color(0xFF555555) // Lighter gray for contrast
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "This helps maintain accurate location updates without interruptions, ensuring a safer and more reliable experience.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color(0xFF57A8F3) // SmartBusTertiaryBlue for softer highlight
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAgree,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF084C88)), // SmartBusPrimaryBlue
                shape = RoundedCornerShape(12.dp), // Softer edges
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(text = "I Agree", color = Color.White, fontSize = 16.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color.Gray), // Gray border instead of red
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(text = "Not Now", color = Color.Gray, fontSize = 16.sp)
            }
        },
        shape = RoundedCornerShape(18.dp), // More rounded corners for a modern feel
        containerColor = Color(0xFFF9F9F9), // Softer light gray background
        properties = DialogProperties(dismissOnClickOutside = false)
    )
}




@Composable
fun BatteryOptimizationScreen() {
    val context = LocalContext.current
//    var showDialog by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(isBatteryOptimizationEnabled(context)) }

    if (showDialog) {
        BatteryOptimizationDialog(
            onDismiss = { showDialog = false },
            onAgree = {
                showDialog = false
                openBatterySettings(context) // Function to navigate to battery settings
            }
        )
    }
}
fun isBatteryOptimizationEnabled(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return !powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

fun openBatterySettings(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    context.startActivity(intent)
}