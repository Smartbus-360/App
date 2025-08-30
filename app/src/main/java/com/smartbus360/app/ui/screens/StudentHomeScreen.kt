package com.smartbus360.app.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.smartbus360.app.R
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.getRouteAndCalculateETA
import com.smartbus360.app.ui.component.CustomDialog
import com.smartbus360.app.ui.component.NetworkSnackbar
import com.smartbus360.app.ui.component.NotificationPopup
import com.smartbus360.app.viewModels.NetworkViewModel
import com.smartbus360.app.viewModels.NotificationViewModel
import com.smartbus360.app.viewModels.StudentScreenViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun StudentHomeScreenContent( navController: NavHostController,
    navControllerBottomBar: NavHostController,
    busLocationUpdates: MutableState<LatLngPlace>,
    state: State<GetUserDetailResponseX>,
    stoppageX: State<List<GeoPoint>>,
    counter: MutableIntState,
    networkViewModel: NetworkViewModel = getViewModel(),
    notificationViewModel: NotificationViewModel = getViewModel(),
    studentScreenViewModel: StudentScreenViewModel = getViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity // Cast context to activity

    val preferencesRepository = PreferencesRepository(context)
    val stateNotification = notificationViewModel.state.collectAsState()
    val state1 = studentScreenViewModel.state.collectAsState()
    val stateStatus = studentScreenViewModel.state.collectAsState()
    val filteredNotifications = stateNotification.value.notifications.filter { notification ->
        notification.instituteType == state1.value.user?.institute_type
    }

//    val state = studentScreenViewModel.state.collectAsState()
//    val stoppageX = studentScreenViewModel.stoppages.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val isConnected by networkViewModel.isConnected.collectAsState()


    val mapView = remember { mutableStateOf<MapView?>(null) }
    val previousBusLocation = remember { mutableStateOf<GeoPoint?>(null) }
    val snappedBusLocation = remember { mutableStateOf<GeoPoint?>(null) }
    val mapOrientation = remember { mutableFloatStateOf(0f) }
    val lastOrientation = remember { mutableFloatStateOf(0f) }
    //  val context = LocalContext.current
    var compassOverlay: CompassOverlay? = remember { null }

    val stoppagesForPolyLines =
        if (state.value.user?.routeCurrentJourneyPhase == "morning") {
            state.value.routeStoppages?.filter { it.stopType == "morning"
                    &&
                    it.rounds?.morning?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
            }?.map { stoppage ->
                if (stoppage.reached == 1) {
                    GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                } else {
                    GeoPoint(
                        stoppage.latitude.toDouble(),
                        stoppage.longitude.toDouble()
                    ) // Use actual values otherwise
                }
            }
        }
    else if (state.value.user?.routeCurrentJourneyPhase == "afternoon") {
        state.value.routeStoppages?.filter { it.stopType == "afternoon"
                &&
                it.rounds?.afternoon?.any { round -> round.round == state.value.user?.routeCurrentRound } ==true
        }?.map { stoppage ->
            if (stoppage.reached == 1) {
                GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
            } else {
                GeoPoint(
                    stoppage.latitude.toDouble(),
                    stoppage.longitude.toDouble()
                ) // Use actual values otherwise
            }
        }
    }
    else{
            state.value.routeStoppages?.filter { it.stopType == "evening"
                    &&
                    it.rounds?.evening?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
            }?.map { stoppage ->
                if (stoppage.reached == 1) {
                    GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                } else {
                    GeoPoint(
                        stoppage.latitude.toDouble(),
                        stoppage.longitude.toDouble()
                    ) // Use actual values otherwise
                }
            }
    }

    val role = preferencesRepository.getUserRole()

    // Variables to display ETA, distance, and actual arrival times for each stoppage
    val etaList = remember { mutableStateListOf<Double>() }   // List of ETAs in minutes
    val distanceList = remember { mutableStateListOf<Double>() } // List of distances in km
    val actualArrivalTimes =
        remember { mutableStateListOf<String>() } // Arrival times in "hh:mm a" format

    BackHandler {
        showExitDialog = true
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

    // Launch the coroutine to get snapped location
    LaunchedEffect(busLocationUpdates.value) {
        val snappedLocation = getSnappedLocationToRoad(
            busLocationUpdates.value.latitude,
            busLocationUpdates.value.longitude
        )
        while(true) {
            snappedBusLocation.value = snappedLocation ?: GeoPoint(
                busLocationUpdates.value.latitude,
                busLocationUpdates.value.longitude
            )
            delay(5000)
        }
    }
    val lastRouteFetchTime = remember { mutableStateOf(0L) }

// LaunchEffect to update polyline and calculate ETA/distance for multiple stops, including cumulative time
    // LaunchedEffect to update polyline and calculate ETA/distance for multiple stops, including cumulative time and arrival times
    LaunchedEffect(busLocationUpdates.value) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastFetch = currentTime - lastRouteFetchTime.value
        if (snappedBusLocation.value != null &&
            timeSinceLastFetch > 60000 ) {

            lastRouteFetchTime.value = currentTime
            // Fetch routes and draw continuous polylines for all stoppages
            val etaDistanceArrivalList = stoppagesForPolyLines?.let {
                getRouteAndCalculateETA(
                    snappedBusLocation.value!!.latitude,
                    snappedBusLocation.value!!.longitude,
                    it
        //                if (state.value.user.routeFinalStopReached == 1) stoppageX.value.reversed() else stoppageX.value,
                )
            }
//            state.value.routeStoppages.filter { it.stopType == "dropoff" }
            // Clear previous lists for updated data
            etaList.clear()
            distanceList.clear()
            actualArrivalTimes.clear()

            // Variable to hold cumulative ETA in minutes
            var cumulativeEta = 0.0

            // Get current time as the base for calculating arrival times
            val currentTime = Calendar.getInstance()

            etaDistanceArrivalList?.forEach { (etaInMinutes, distanceInKm, _) ->
                // Add the current stop's ETA to the cumulative ETA
                cumulativeEta += etaInMinutes

                // Add the cumulative ETA for the current stop
                etaList.add(cumulativeEta)

                // Add distance for the current stop
                distanceList.add(distanceInKm)

                // Calculate the arrival time by adding the cumulative ETA to the current time
                val arrivalTime = Calendar.getInstance().apply {
                    timeInMillis = currentTime.timeInMillis
                    add(Calendar.MINUTE, cumulativeEta.toInt())
                }

                // Format the arrival time as "hh:mm a"
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val formattedArrivalTime = timeFormat.format(arrivalTime.time)

                // Add formatted arrival time to the list
                actualArrivalTimes.add(formattedArrivalTime)
            }

        } else {
            Log.e("BusLocationScreen", "snappedBusLocation or mapView is null")
        }
    }


    // Clean up compass overlay on disposal
    DisposableEffect(Unit) {
        onDispose {
            compassOverlay?.disableCompass()
        }
    }

//    val stateNotification = notificationViewModel.state.collectAsState()

    // Filter unseen notifications
    val unseenNotifications = stateNotification.value.notifications.filter {
        it.id != null && it.id.toString() !in preferencesRepository.getSeenNotifications()
    }

    var showPopup by remember { mutableStateOf(unseenNotifications.isNotEmpty()) }



    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) } // Attach SnackbarHost
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()  // Make the Column fill the available space
//            .padding(2.dp),  // Optional padding around the Column
        ,
        verticalArrangement = Arrangement.spacedBy(.5.dp),  // Space between each item
        horizontalAlignment = Alignment.CenterHorizontally  // Center the composables horizontally
    ) {
        //        Text(text = "Welcome to the Home Screen!")
        //        Text(text = "Welcome to the Home Screen!")
        //       StudentMainScreen()

        val context = LocalContext.current
        PreferencesRepository(context)

//        StudentScreen(preferencesRepository)



            StudentMainScreenComponent(state, navControllerBottomBar)
            StudentScreenComponentMid(state, navControllerBottomBar)
            StudentScreenDropDownComponent(
                state,
                navControllerBottomBar,
                etaList,
                actualArrivalTimes,
                busLocationUpdates
            )
            StudentScreenLiveRunningStatus(
                state,
                navController,
                navControllerBottomBar,
                etaList,
                actualArrivalTimes,
                counter,
                busLocationUpdates
            )
            NetworkSnackbar(
                isConnected = isConnected,
                snackbarHostState = snackbarHostState
            )







    }
}
}
