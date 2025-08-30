package com.smartbus360.app.ui.screens

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.smartbus360.app.R
import com.smartbus360.app.data.database.AlertStatusDao
import com.smartbus360.app.data.database.AlertStatusEntity
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.data.service.StopSoundReceiver
import com.smartbus360.app.getRouteAndCalculateETA
import com.smartbus360.app.ui.component.UnauthorizedScreen
import com.smartbus360.app.ui.theme.InterMedium
import com.smartbus360.app.ui.theme.SmartBusTertiaryBlue
import com.smartbus360.app.utility.formatStopName
import com.smartbus360.app.utility.formatTo12HourTime
import com.smartbus360.app.viewModels.StudentScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreenDropDownComponent(
    state: State<GetUserDetailResponseX>,
    navController: NavHostController,
    etaList: SnapshotStateList<Double>,
    actualArrivalTimes: SnapshotStateList<String>,
    busLocationUpdates: MutableState<LatLngPlace>,
    studentScreenViewModel: StudentScreenViewModel = getViewModel()
){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context as? Activity // Ensure the context is an Activity
    val preferencesRepository = PreferencesRepository(context)
    // State to track dropdown expansion and selected date
//    var expanded by remember { mutableStateOf(false) }
//    var selectedDate by remember { mutableStateOf("Select Date") }
//    val dates = listOf("2024-09-25", "2024-09-26", "2024-09-27")
    val state = studentScreenViewModel.state.collectAsState()
    val stateBusReplaced = studentScreenViewModel.busReplacedStatus.collectAsState()
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

    val role = preferencesRepository.getUserRole()

    // Variables to display ETA, distance, and actual arrival times for each stoppage
    val etaList = remember { mutableStateListOf<Double>() }   // List of ETAs in minutes
    val distanceList = remember { mutableStateListOf<Double>() } // List of distances in km
    val actualArrivalTimes =
        remember { mutableStateListOf<String>() } // Arrival times in "hh:mm a" format

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.85F)
            .padding(bottom = 2.dp)
//            .height(400.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


if(state.value.success == true){

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

    Column (modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally){
        StatusIconLazyColumn(state,etaList,actualArrivalTimes,context )

    }
}
else if(state.value.success == false){
    UnauthorizedScreen()




}
else{
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center // Centers the content within the Box
    ) {
        CircularProgressIndicator()
    }
}
    }
}


var mediaPlayer: MediaPlayer? = null // Make MediaPlayer global for access in StopSoundReceiver


@Composable
fun StatusIconLazyColumn(
    state: State<GetUserDetailResponseX>,
    etaList: SnapshotStateList<Double>,
    actualArrivalTimes: SnapshotStateList<String>,
    context: Context
//    alertStatusDao: AlertStatusDao
) {
    val alertStatusDao: AlertStatusDao = get() // Koin example

    val coroutineScope = rememberCoroutineScope()
    val preferencesRepository = PreferencesRepository(context)
    // Initialize MediaPlayer with a custom alert sound
    val mediaPlayer = MediaPlayer.create(context, R.raw.alert_noti)

    if (state.value.routeStoppages?.isNotEmpty() == true) {

        val stopList = when (state.value.user?.routeCurrentJourneyPhase) {
            "afternoon" -> state.value.routeStoppages?.filter {
                it.stopType == "afternoon" &&
                        it.rounds?.afternoon?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
            } ?: emptyList()

            "evening" -> state.value.routeStoppages?.filter {
                it.stopType == "evening" &&
                        it.rounds?.evening?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
            } ?: emptyList()

            else -> state.value.routeStoppages?.filter {
                it.stopType == "morning" &&
                        it.rounds?.morning?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
            } ?: emptyList()
        }

        val totalItems = stopList.size  // Use the actual size of stopList



        // Initialize the alertStatus list with appropriate size
        val alertStatus = remember {
            mutableStateListOf<Boolean>().apply {
                addAll(List(totalItems) { true }) // Default false status for each stop
            }
        }

        // Fetch and update alert status from the database
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                alertStatusDao.getAlertStatuses().collect { allStatuses ->
                    val statusMap = allStatuses.associate { it.stopId to it.isEnabled }
                    // Ensure we don't exceed the list size
                    statusMap.forEach { (stopId, isEnabled) ->
                        if (stopId < alertStatus.size) {
                            alertStatus[stopId] = isEnabled
                        }
                    }
                }
            }
        }



        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth() // Make the Row fill the available width
                .wrapContentWidth(Alignment.CenterHorizontally) // Center the content horizontally
        ) {
            val message = when (state.value.user?.routeCurrentJourneyPhase) {
                "afternoon" -> {
                    "Afternoon" + " Round ${state.value.user?.routeCurrentRound}"
                }
                "evening" -> {
                    "Evening" + " Round ${state.value.user?.routeCurrentRound}"
                }
                else -> {
                    "Morning" + " Round ${state.value.user?.routeCurrentRound}"
                }
            }




            Text(
                text = message.replaceFirstChar { char -> char.uppercaseChar() },
                color = if (state.value.user?.routeCurrentJourneyPhase == "morning"
                    && state.value.user?.routeCurrentRound == 1
                )
                    Color(0xFF008000)

                else if (
                    state.value.user?.routeCurrentJourneyPhase == "morning"
                    && state.value.user?.routeCurrentRound == 2
                ){
                    Color(0xFF722F37)
                }
                else if (state.value.user?.routeCurrentJourneyPhase == "afternoon"
                    && state.value.user?.routeCurrentRound == 1
                )
                   Color(0xFF002366)

                else if (
                    state.value.user?.routeCurrentJourneyPhase == "afternoon"
                    && state.value.user?.routeCurrentRound == 2
                ){
                    Color(0xFF000000)
                }
                else if (
                    state.value.user?.routeCurrentJourneyPhase == "evening"
                    && state.value.user?.routeCurrentRound == 1
                ){
                    Color(0xFFFF7043)
                }
                else if (
                    state.value.user?.routeCurrentJourneyPhase == "evening"
                    && state.value.user?.routeCurrentRound == 2
                ){
                Color(0xFFFFD700)
                }

                else
                   Color(0xFF808080),
                textDecoration = TextDecoration.Underline,
                fontSize = 14.sp,
                fontFamily = InterMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically) // Center text vertically in Row
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()

                .padding(10.dp)
        ) {

            items(totalItems) {  index ->
                val stopList = when (state.value.user?.routeCurrentJourneyPhase) {
                    "afternoon" -> state.value.routeStoppages?.filter {
                        it.stopType == "afternoon"  &&
                                it.rounds?.afternoon?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
                    } ?: emptyList()

                    "evening" -> state.value.routeStoppages?.filter {
                        it.stopType == "evening"  &&
                                it.rounds?.evening?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
                    } ?: emptyList()

                    else -> state.value.routeStoppages?.filter {
                        it.stopType == "morning"  &&
                                it.rounds?.morning?.any { round -> round.round == state.value.user?.routeCurrentRound } == true
                    } ?: emptyList()
                }

                val stop = stopList.getOrNull(index)

                val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

                val arrivalTime12Hr = try {
                    val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Change to HH:mm
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format
                    val currentRound = state.value.user?.routeCurrentRound ?: 1 // Default to 1 if null
                    val morningRounds = stop?.rounds?.morning

                    // Debugging Statements
                    println("Afternoon Rounds: $morningRounds") // Check if rounds exist
                    println("Current Round: $currentRound")

                    val round = morningRounds?.find { it.round == currentRound }

                    println("Selected Round Data: $round") // Check if correct round is found

                    val arrivalTime = round?.arrivalTime
                    println("Selected Arrival Time: $arrivalTime") // Check if arrival time exists

                    arrivalTime?.let {
                        val date = inputFormat.parse(it)
                        date?.let { outputFormat.format(it) } // Convert to 12-hour format
                    } ?: "No Time Found" // Fallback if parsing fails
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Error" // Return error if something fails
                }
                 //
                //afternoon


                val arrivalAfternoonTime12Hr = try {
                    val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Change to HH:mm
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format
                    val currentRound = state.value.user?.routeCurrentRound ?: 1 // Default to 1 if null
                    val afternoonRounds = stop?.rounds?.afternoon

                    // Debugging Statements
                    println("Afternoon Rounds: $afternoonRounds") // Check if rounds exist
                    println("Current Round: $currentRound")

                    val round = afternoonRounds?.find { it.round == currentRound }

                    println("Selected Round Data: $round") // Check if correct round is found

                    val arrivalTime = round?.arrivalTime
                    println("Selected Arrival Time: $arrivalTime") // Check if arrival time exists

                    arrivalTime?.let {
                        val date = inputFormat.parse(it)
                        date?.let { outputFormat.format(it) } // Convert to 12-hour format
                    } ?: "No Time Found" // Fallback if parsing fails
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Error" // Return error if something fails
                }

                println("Final Converted Time: $arrivalAfternoonTime12Hr")


                //evening
                val arrivalEveningTime12Hr = try {
                    val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Change to HH:mm
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format
                    val currentRound = state.value.user?.routeCurrentRound ?: 1 // Default to 1 if null
                    val morningRounds = stop?.rounds?.evening

                    // Debugging Statements
                    println("Afternoon Rounds: $morningRounds") // Check if rounds exist
                    println("Current Round: $currentRound")

                    val round = morningRounds?.find { it.round == currentRound }

                    println("Selected Round Data: $round") // Check if correct round is found

                    val arrivalTime = round?.arrivalTime
                    println("Selected Arrival Time: $arrivalTime") // Check if arrival time exists

                    arrivalTime?.let {
                        val date = inputFormat.parse(it)
                        date?.let { outputFormat.format(it) } // Convert to 12-hour format
                    } ?: "No Time Found" // Fallback if parsing fails
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Error" // Return error if something fails
                }

                val defaultArrivalTime = when (state.value.user?.routeCurrentJourneyPhase) {
                    "afternoon" -> {
                        arrivalAfternoonTime12Hr
                    }
                    "evening" -> arrivalEveningTime12Hr
                    else -> {
                        arrivalTime12Hr
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Status Column
                    Column(
                        modifier = Modifier
                            .border(1.dp, Color.Black, RoundedCornerShape(15.dp))
                            .clip(RoundedCornerShape(15.dp))
                            .width(120.dp)
                            .height(55.dp)
                            .background(Color.White, RoundedCornerShape(15.dp))
                            .padding(horizontal = 4.dp)
                            .fillMaxWidth(),
                        Arrangement.Center
                    ) {
                        if (stop?.reached == 1) {
                            Text(
                                stringResource(R.string.arrived_at_bus_stop, stop.stopOrder ?: "Unknown"),
                                color = Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            stop.reachDateTime?.let {
                                Text(
                                    stringResource(R.string.reached_time, formatTo12HourTime(it)),
                                    color = Color.Blue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } ?: Text(
                                "Reached Time: Not Available",
                                color = Color.Blue,
                                fontSize = 10.sp
                            )
                            Text(
                                stringResource(R.string.default_time, defaultArrivalTime ?: "--:--"),
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (stop?.reached == 2) {
                            Text(
                                stringResource(R.string.missed_bus_stop, stop.stopOrder ?: "Unknown"),
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.default_time, defaultArrivalTime ?: "--:--"),
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            val eta = etaList.getOrNull(index)
                            val arrivalTime = actualArrivalTimes.getOrNull(index)

                            if (eta != null) {
                                Text(
                                    stringResource(R.string.arriving_in_mins, eta.roundToInt()),
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(
                                        R.string.estimated_arrival_time,
                                        arrivalTime ?: "--:-- --"
                                    ),
                                    color = Color.Blue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.default_time, defaultArrivalTime ?: "--:--"),
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    stringResource(R.string.arriving_in_mins_),
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.estimated_arrival_time_),
                                    color = Color.Blue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.default_time, defaultArrivalTime ?: "--:--"),
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }


                    }

                    // Status Icon
                    val statusIcon = if (stop?.reached == 1) {
                        painterResource(
                            when (index) {
                                0 -> R.drawable.bus_status_bar_started
                                totalItems - 1 -> R.drawable.bus_status_next
                                else -> R.drawable.bus_status_mid
                            }
                        )
                    } else {
                        painterResource(
                            when (index) {
                                0 -> R.drawable.bus_stop_start
                                totalItems - 1 -> R.drawable.busstopend
                                else -> R.drawable.busstop_middle
                            }
                        )
                    }
// Status Icon
                    val statusIcon2 = if (stop?.reached == 1 && (stop.stopHitCount ?: 0) >= 2) {
                        painterResource(
                            when (index) {
                                0 -> R.drawable.bus_status_bar_started
                                totalItems - 1 -> R.drawable.bus_status_next
                                else -> R.drawable.bus_status_mid
                            }
                        )
                    } else {
                        painterResource(
                            when (index) {
                                0 -> R.drawable.bus_stop_start
                                totalItems - 1 -> R.drawable.busstopend
                                else -> R.drawable.busstop_middle
                            }
                        )
                    }
                    Image(
                        painter = statusIcon,
                        contentDescription = "status icon",
                        modifier = Modifier.size(70.dp)
                    )

                    Image(
                        painter = statusIcon2,
                        contentDescription = "status icon",
                        modifier = Modifier.size(70.dp)

                    )

                    // Stop Details Column
                    Column(
                        modifier = Modifier.weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Stop ${index + 1}", fontSize = 12.sp)
                        Text(formatStopName(stop?.stopName), color = Color.Black, fontSize = 12.sp)
                    }

                    // Bell Icon to toggle alert status
                    Icon(
                        painter = painterResource(
                            id = if (alertStatus.getOrNull(index) == true)
                                R.drawable.bell
                            else R.drawable.disabled_bell_icon
                        ),
                        contentDescription = "Alert Icon",
                        tint = Color.Black,
                        modifier = Modifier
                            .clickable {
                                if (index < alertStatus.size) {
                                    alertStatus[index] = !alertStatus[index]
                                    coroutineScope.launch {
                                        alertStatusDao.insertAlertStatus(
                                            AlertStatusEntity(
                                                stopId = index,
                                                isEnabled = alertStatus[index]
                                            )
                                        )
                                    }
                                } else {
                                    Log.w("StudentScreen", "Attempted to access alertStatus out of bounds at index $index")
                                }

                            }
                    )
                }
            }

        }
        if (totalItems == 0) {
            // LazyColumn logic
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val message = if (state.value.user?.routeFinalStopReached == 1) {
                    stringResource(R.string.no_drop_off_locations_available)
                } else {
                    stringResource(R.string.no_pickup_locations_available)
                }
                Text(
                    text = message,
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

    }


}




fun showNotification(context: Context, title: String, message: String) {
    val channelId = "bus_alert_channel"
    val uniqueId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

    // Create notification channel if needed
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Bus Alert Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for bus alert notifications"
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Play sound
    try {
        AlertSoundPlayer.mediaPlayer?.release()
        AlertSoundPlayer.mediaPlayer = MediaPlayer.create(context, R.raw.alert_noti)
        AlertSoundPlayer.mediaPlayer?.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Intent with notification ID
    val stopIntent = Intent(context, StopSoundReceiver1::class.java).apply {
        putExtra("notification_id", uniqueId)
    }

    val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        uniqueId, // <— Use unique request code for each
        stopIntent,
        pendingIntentFlags
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.smartbus_nobg_2)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.alert_noti}"))
        .addAction(
            R.drawable.stop_sign,
            "dismiss",
            pendingIntent
        )
        .build()

    try {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(uniqueId, notification)

    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback for older versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(uniqueId, notification)
        }
    }
}


class StopSoundReceiver1 : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Stop the sound
        ContextCompat.getMainExecutor(context).execute {
            AlertSoundPlayer.mediaPlayer?.apply {
                stop()
                release()
            }
            AlertSoundPlayer.mediaPlayer = null
        }
        // Cancel the specific notification
        val notificationId = intent?.getIntExtra("notification_id", -1) ?: -1
        if (notificationId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }
    }
}



object AlertSoundPlayer {
    var mediaPlayer: MediaPlayer? = null
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDropdownMenuWithCustomColor(navController: NavHostController)
{
    // Get the current date, yesterday's date, and the day before yesterday using Calendar
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    val currentDate = dateFormatter.format(calendar.time)
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val yesterday = dateFormatter.format(calendar.time)
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val dayBeforeYesterday = dateFormatter.format(calendar.time)

    // List of formatted dates
    val formattedDates = listOf(currentDate, yesterday, dayBeforeYesterday)

    // State to control the expansion of the dropdown
    var expanded by remember { mutableStateOf(false) }
    // State to track the selected date, defaulting to the current date
    var selectedDate by remember { mutableStateOf(formattedDates[0]) }

    Column(
        modifier = Modifier
            .height(30.dp)
            .width(100.dp)
            .clickable {
//                navController.navigate("notification")
            }

        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Exposed Dropdown Menu Box for better UI interaction
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            // The TextField that acts as the dropdown anchor
            TextField(
                value = selectedDate,
                onValueChange = { },
                readOnly = true,
              label = { Text(stringResource(R.string.date), color = Color.White,fontSize = 10.sp) }, // Keep the label text color black
                leadingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.DateRange else Icons.Filled.DateRange,
                        contentDescription = "Dropdown Icon",
                        tint = if (expanded) Color.Black else Color.White
                    )
                },
                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = GarudaYellow,
                    unfocusedContainerColor = SmartBusTertiaryBlue,
                    focusedContainerColor = SmartBusTertiaryBlue,
                    focusedTextColor = Color.White,
                    focusedIndicatorColor = SmartBusTertiaryBlue,
                    unfocusedIndicatorColor = SmartBusTertiaryBlue,
                    disabledTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
                modifier = Modifier
                    .menuAnchor() // Modifier to link the dropdown to the anchor
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        navController.navigate("reachDateTime")


                    } // Open dropdown on click
            )

            // Dropdown Menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                formattedDates.forEach { date ->
                    DropdownMenuItem(
                        text = { Text(date, color = Color.Black, fontSize = 10.sp) }, // Text color remains black
                        modifier = Modifier
                            .background(
                                if (selectedDate == date) SmartBusTertiaryBlue else Color.Transparent
                            )
                            .fillMaxWidth(),
                        onClick = {
                            selectedDate = date
                            expanded = false // Collapse dropdown after selection
                        }
                    )
                }
            }
        }
    }
}






@Composable
fun CircularLoadingScreen() {
    // Centering the CircularProgressIndicator
    Box(
        modifier = Modifier
            .fillMaxSize() // Make the loading screen take up the whole screen
            .background(Color(0xFFF5F5F5)), // Set a light background color
        contentAlignment = Alignment.Center // Center the loading indicator
    ) {
        // Circular loading indicator with custom color and stroke width
        CircularProgressIndicator(
            color = Color.Blue, // Customize the color
            strokeWidth = 6.dp // Set the thickness of the progress indicator
        )
    }
}





@Composable
@Preview(showBackground = true)
fun CustomLanguageSelectionWithFillPreview() {
    val  context = LocalContext.current
    // Initialize mutable state lists
    val etaList = remember { mutableStateListOf<Double>() }   // List of ETAs in minutes
    val distanceList = remember { mutableStateListOf<Double>() } // List of distances in km
    val actualArrivalTimes = remember { mutableStateListOf<String>() } // Arrival times in "hh:mm a" format

    // You can add more logic here to update or modify these lists based on your data.

    // Example usage (for illustration purposes):
    LaunchedEffect(Unit) {
        // Simulating adding data to the lists
        etaList.addAll(listOf(5.0, 10.0, 15.0)) // Adding some ETAs
        distanceList.addAll(listOf(2.5, 4.7, 7.8)) // Adding some distances
        actualArrivalTimes.addAll(listOf("08:30 AM", "08:45 AM", "09:00 AM")) // Adding arrival times
    }
    val preferencesRepository = PreferencesRepository(context)
    val studentScreenViewModel = StudentScreenViewModel(preferencesRepository)
    val state = studentScreenViewModel.state.collectAsState()
//    StudentScreenDropDownComponent(state, navController = rememberNavController(),etaList,
//        actualArrivalTimes)
}


