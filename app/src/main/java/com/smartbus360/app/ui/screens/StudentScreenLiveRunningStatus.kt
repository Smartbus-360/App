package com.smartbus360.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.smartbus360.app.R
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.getRouteAndCalculateETA
import com.smartbus360.app.ui.theme.Roboto
import com.smartbus360.app.ui.theme.SmartBusTertiaryBlue
import com.smartbus360.app.viewModels.StudentScreenViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StudentScreenLiveRunningStatus(
    state: State<GetUserDetailResponseX>,
    navController: NavHostController,
    navControllerBottomBar: NavHostController,
    etaList: SnapshotStateList<Double>,
    actualArrivalTimes: SnapshotStateList<String>,
    counter: MutableIntState,
    busLocationUpdates: MutableState<LatLngPlace>,



    )
{
    val context = LocalContext.current

    val studentScreenViewModel: StudentScreenViewModel = koinViewModel()

//    val state = studentScreenViewModel.state.collectAsState()
     val vmState = studentScreenViewModel.state.collectAsState()
    var threeLineBarExpanded by remember { mutableStateOf(false) }
    var stopName by remember { mutableStateOf("") }
    var nextStoppage by remember { mutableStateOf("") }

    // Track the elapsed time in seconds
    var elapsedTime by remember { mutableIntStateOf(0) }
    val previousCounter = remember { mutableIntStateOf(counter.intValue) }
    // Reset and start the timer each time the composable recomposes
    LaunchedEffect(Unit) {
        elapsedTime = 0
        while (true) {
            delay(1000L) // Delay 1 second
            elapsedTime++
        }
    }

    LaunchedEffect(counter.intValue) {
        if (counter.intValue != previousCounter.intValue) { // Check if the counter value has changed
            previousCounter.intValue = counter.intValue // Update the previous value
            delay(3000)
//            navControllerBottomBar.navigate("home") {
//                popUpTo("home") { inclusive = true }
//            }
        }
    }

    // Variables to display ETA, distance, and actual arrival times for each stoppage
    val snappedBusLocation = remember { mutableStateOf<GeoPoint?>(null) }

    val stoppagesForPolyLines =
        when (state.value.user?.routeCurrentJourneyPhase) {
            "afternoon" -> {
                state.value.routeStoppages?.filter { it.stopType == "afternoon"
                        &&
                        it.rounds?.afternoon?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
                }?.map { stoppage ->
                    if (stoppage.reached == 1) {
                        GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                    } else {
                        GeoPoint(stoppage.latitude.toDouble(),
                            stoppage.longitude.toDouble())

                    }
                }
            }
            "evening" -> {
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
            else -> {
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
                        )
                        // Use actual values otherwise
                    }
                }
            }
        }


    val etaList = remember { mutableStateListOf<Double>() }   // List of ETAs in minutes
    val distanceList = remember { mutableStateListOf<Double>() } // List of distances in km
    val actualArrivalTimes =
        remember { mutableStateListOf<String>() } // Arrival times in "hh:mm a" format


    if(state.value.success == true){

        // Launch the coroutine to get snapped location
//        LaunchedEffect(busLocationUpdates.value) {
//            val snappedLocation = getSnappedLocationToRoad(
//                busLocationUpdates.value.latitude,
//                busLocationUpdates.value.longitude
//            )
//            while(true) {
//                snappedBusLocation.value = snappedLocation ?: GeoPoint(
//                    busLocationUpdates.value.latitude,
//                    busLocationUpdates.value.longitude
//                )
//                delay(5000)
//            }
//        }
        LaunchedEffect(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude) {
            val snapped = getSnappedLocationToRoad(
                busLocationUpdates.value.latitude,
                busLocationUpdates.value.longitude
            )
            snappedBusLocation.value = snapped ?: GeoPoint(
                busLocationUpdates.value.latitude,
                busLocationUpdates.value.longitude
            )
        }

// LaunchEffect to update polyline and calculate ETA/distance for multiple stops, including cumulative time
        // LaunchedEffect to update polyline and calculate ETA/distance for multiple stops, including cumulative time and arrival times
        LaunchedEffect(busLocationUpdates.value) {
            if (snappedBusLocation.value != null) {
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

//        Column (modifier = Modifier.fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally){
//            StatusIconLazyColumn(state,etaList,actualArrivalTimes,context )
//
//        }
//    }
//
//
//    if (state.value.success == true){



        val shift = state.value.user?.routeCurrentJourneyPhase ?: "morning"
        val currentRound = state.value.user?.routeCurrentRound

// Filter the relevant stoppages based on shift and round
        val relevantStoppages = state.value.routeStoppages?.filter { stoppage ->
            when (shift) {
                "afternoon" -> stoppage.stopType == "afternoon" &&
                        stoppage.rounds?.afternoon?.any { it.round == currentRound } == true
                "evening" -> stoppage.stopType == "evening" &&
                        stoppage.rounds?.evening?.any { it.round == currentRound } == true
                else -> stoppage.stopType == "morning" &&
                        stoppage.rounds?.morning?.any { it.round == currentRound } == true
            }
        } ?: emptyList()

// Show ETA for first stoppage if journey hasn't started yet
        nextStoppage = if (relevantStoppages.isNotEmpty() && etaList.isNotEmpty()) {
            stringResource(
                R.string.is_reaching_stop_in_min,
                1,
                relevantStoppages[0].stopName,
                etaList[0].roundToInt()
            )
        } else {
            stringResource(R.string.journey_yet_to_start)
        }

// Loop over relevant stoppages to find the last reached one
        relevantStoppages.forEachIndexed { index, stoppage ->
            if (stoppage.reached == 1) {
                stopName = stoppage.stopName

                // Find the next stoppage after the current one
                for (i in index + 1 until relevantStoppages.size) {
                    val nextStop = relevantStoppages[i]

                    nextStoppage = if (i < etaList.size) {
                        stringResource(
                            R.string.is_reaching_stop_in_min,
                            i + 1,
                            nextStop.stopName,
                            etaList[i].roundToInt()
                        )
                    } else if (state.value.success == true) {
                        stringResource(R.string.journey_finished)
                    } else {
                        stringResource(
                            R.string.is_reaching_stop_in_min_,
                            i + 1,
                            nextStop.stopName
                        )
                    }
                    break
                }

                return@forEachIndexed
            }
        }



    }



//if (elapsedTime == 30){
//    navControllerBottomBar.navigate("home") {
//        popUpTo("home") { inclusive = true }
//    }
//}
    // Format elapsed time for display
    val displayTime = if (elapsedTime < 60) {
        stringResource(R.string.sec_ago, elapsedTime)
    } else {
        stringResource(R.string.min_sec_ago, elapsedTime / 60, elapsedTime % 60)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = SmartBusTertiaryBlue)
            .padding(vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.Center,

//            Arrangement.SpaceBetween,

            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(50.dp)
        ) {
//            Text(
//                stringResource(R.string.your_bus, nextStoppage),
//                color = Color(0xFFFFFFFF),
//                fontSize = 14.sp
//                ,
//                modifier = Modifier
////                    .weight(1f) // Take up available space
//                    .align(Alignment.CenterVertically)
//                    .padding(end = 8.dp) // Add space between Text and black column
//            )
            val preferencesRepository = PreferencesRepository(context)
            val role = preferencesRepository.getUserRole()

            if (role == "student" && busLocationUpdates.value.latitude == 0.0
                && busLocationUpdates.value.longitude == 0.0
            ) {
                Text(
                    stringResource(R.string.bus_driver_is_offline),
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,

                    modifier = Modifier
//                    .weight(1f) // Take up available space
                        .align(Alignment.CenterVertically)
                        .padding(end = 8.dp) // Add space between Text and black column
                )
            }
            else{
                Text(
                    stringResource(R.string.bus_driver_is_online),
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                    ,
                    modifier = Modifier
//                    .weight(1f) // Take up available space
                        .align(Alignment.CenterVertically)
                        .padding(end = 8.dp) // Add space between Text and black column
                )
            }


        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun PreviewStudentScreenLiveRunningStatus() {
//    val etaList = remember { mutableStateListOf(0.0, 0.0, 0.0) }  // Use default Double values
//    val actualArrivalTimes = remember { mutableStateListOf("", "", "") }  // Use default empty strings
//    val counter = remember { mutableIntStateOf(0) }
//
//    val  context = LocalContext.current
//    val preferencesRepository = PreferencesRepository(context)
//    val studentScreenViewModel = StudentScreenViewModel(preferencesRepository)
//    val state = studentScreenViewModel.state.collectAsState()
//    StudentScreenLiveRunningStatus(state = state, navControllerBottomBar = rememberNavController(),
//        etaList = etaList,
//        actualArrivalTimes = actualArrivalTimes,
//        counter
//        )
//}
