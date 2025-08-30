package com.smartbus360.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smartbus360.app.R
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.getRouteAndDrawContinuousPolyline
import com.smartbus360.app.utility.formatTo12HourTime
import com.smartbus360.app.viewModels.BusLocationScreenViewModel
import com.smartbus360.app.viewModels.SnappingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import com.smartbus360.app.data.olaMaps.getSmartBusTileSource

@Composable
fun StudentBusLocationMapScreen(busLocationUpdates: State<LatLngPlace>, busLocationScreenViewModel: BusLocationScreenViewModel = getViewModel(), viewModel: SnappingViewModel = getViewModel())
{

    val mapView = remember { mutableStateOf<MapView?>(null) }
    val previousBusLocation = remember { mutableStateOf<GeoPoint?>(null) }
    var snappedBusLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val mapOrientation = remember { mutableFloatStateOf(0f) }
    val lastOrientation = remember { mutableFloatStateOf(0f) }
    val shouldCenterMap = remember { mutableStateOf(true) }  // Flag for controlling map centering behavior
    val coroutineScope = rememberCoroutineScope()
    var snappedLocation by remember { mutableStateOf<GeoPoint?>(null) }



    val context = LocalContext.current
    var compassOverlay: CompassOverlay? = remember { null }
    val preferencesRepository = PreferencesRepository(context)
    val role = preferencesRepository.getUserRole()

    // Variables to display ETA, distance, and actual arrival times for each stoppage
    val etaList = remember { mutableStateListOf<Double>() }
    val distanceList = remember { mutableStateListOf<Double>() }
    val actualArrivalTimes = remember { mutableStateListOf<String>() }

    val state = busLocationScreenViewModel.state.collectAsState()
    val stoppagesX = busLocationScreenViewModel.stoppages.collectAsState()
    val stoppagesForPolyLines = busLocationScreenViewModel.stoppagesForPolyLines.collectAsState()

    var busLocation =  GeoPoint(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//    LaunchedEffect(busLocationUpdates.value) {
//        val snappedLocation = getSnappedLocationToRoad(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//        snappedBusLocation.value = snappedLocation ?: GeoPoint(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            snappedBusLocation =
                viewModel.getSnappedLocationToRoad(lat = busLocationUpdates.value.latitude, lon = busLocationUpdates.value.longitude)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // MapView Setup
        AndroidView(
            factory = { context ->
                val mapViewInstance = MapView(context)
                mapViewInstance.setMultiTouchControls(true)
                mapViewInstance.setTileSource(getSmartBusTileSource())   // <- use your tiles
                mapViewInstance.controller.setZoom(12.0)
                mapViewInstance.controller.setCenter(org.osmdroid.util.GeoPoint(21.24050, 81.53110))
                mapView.value = mapViewInstance
                mapViewInstance.setUseDataConnection(true) // be explicit: load tiles from network


                // Set initial map position to the bus location
                mapViewInstance.controller.setZoom(17.5)
                val initialPosition = GeoPoint(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
                mapViewInstance.controller.setCenter(initialPosition)

                // Set up CompassOverlay
                compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapViewInstance)
                compassOverlay?.enableCompass()
                mapViewInstance.overlays.add(compassOverlay)

                // Add markers for stoppages
//                stoppagesX.value.forEachIndexed { index, stopGeoPoint ->
//                    val stopMarker = Marker(mapViewInstance)
//                    stopMarker.position = stopGeoPoint
//                    stopMarker.icon = mapViewInstance.context.getDrawable(R.drawable.busstoppoint)
//                    stopMarker.title = "Stop ${index + 1} \n "
//                    stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//                    mapViewInstance.overlays.add(stopMarker)
//                }

                mapViewInstance
            },
            update = { mapViewInstance ->
                mapView.value?.let { mapView ->
                    val currentBusPosition =  GeoPoint(busLocationUpdates.value.latitude,
                        busLocationUpdates.value.longitude)
                    val previousPosition = previousBusLocation.value ?: currentBusPosition

                    // Remove existing bus marker
                    mapView.overlays.removeIf { it is Marker && it.title == "Bus Location" }

                    // Add new bus marker at snapped position
                    val busMarker = Marker(mapView)
                    busMarker.position = currentBusPosition
                    busMarker.icon = mapView.context.getDrawable(R.drawable.bus_location)
                    busMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    busMarker.title = "Bus Location"
                    mapView.overlays.add(busMarker)

                    mapViewInstance.overlays.removeIf { it is Marker && it.title?.startsWith("Stop") == true }

                    stoppagesX.value.forEachIndexed { index, stopGeoPoint ->
                        val stopMarker = Marker(mapViewInstance)
                        stopMarker.position = stopGeoPoint
                        stopMarker.icon = mapViewInstance.context.getDrawable(R.drawable.busstoppoint)
                        stopMarker.title = "Stop ${index + 1}"
                        stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapViewInstance.overlays.add(stopMarker)
                    }



                    // Only move the map to the bus location initially or when the user requests it
                    if (shouldCenterMap.value) {
                        mapView.controller.animateTo(currentBusPosition)
                        shouldCenterMap.value = false // Stop automatic centering after initial load
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Button to re-center map to bus location
        FloatingActionButton(
            onClick = {
                shouldCenterMap.value = true // Set flag to true to re-center map on bus
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterEnd),
            containerColor = Color.White,
            shape = CircleShape

        ) {
            Icon(   painter = painterResource(id = R.drawable.map_bus_location)
                , contentDescription = "Re-center Map",
                modifier = Modifier.requiredSize(24.dp)
            )
        }

        // Display ETA, Distance, and Arrival Time for each stop
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(8.dp)
        ) {

            stoppagesForPolyLines.value.forEachIndexed { index, stop ->
                if (index < etaList.size && index < distanceList.size && index
                    < actualArrivalTimes.size && index < state.value.routes.size) {

                    val reachDateTime = state.value.routes[index].stoppageReachDateTime
                    val formattedTime = reachDateTime?.let { formatTo12HourTime(it.toString()) }
                        ?: "N/A" // Default to "N/A" if null

                    if (state.value.routes[index].stoppageReached == 1) {
                        Text(text = "Stop ${index + 1} Reached", color = Color.Black)
                        Text(text = "Arrived: ${formattedTime ?: "Unknown"}", color = Color.Red)
                        // Show "Unknown" if null
                    } else {
                        Text(text = "Stop ${index + 1}: ETA: ${etaList[index]
                            .roundToInt()} min", color = Color.Black)
                        Text(text = "Distance: ${distanceList[index]
                            .roundToInt()} km", color = Color.Blue)
                        Text(text = "Arrival: ${actualArrivalTimes[index]}", color = Color.Red)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }




            if(role == "student" && busLocationUpdates.value.latitude == 0.0
                && busLocationUpdates.value.longitude == 0.0)
            {
                Text(text = "Bus Driver is offline", color = Color.Black)
            }else{

            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, bottom = 120.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(8.dp)
        ) {
            if (busLocationUpdates.value.latitude!= 0.0){
                Text(text = "Latitude: ${busLocationUpdates.value.latitude}", color = Color.Black)
                Text(text = "Longitude: ${busLocationUpdates.value.longitude}", color = Color.Black)
                Text(text = "\nCurrent Location: ${busLocationUpdates.value.placeName}", color = Color.Black)
            }
        }
    }



// LaunchEffect to update polyline and calculate ETA/distance for multiple stops, including cumulative time
    // LaunchedEffect to update polyline and calculate ETA/distance for multiple stops, including cumulative time and arrival times
    LaunchedEffect(busLocationUpdates.value) {
        if (busLocation != null && mapView.value != null) {
            // Fetch routes and draw continuous polylines for all stoppages
            val etaDistanceArrivalList = getRouteAndDrawContinuousPolyline(
                busLocation!!.latitude,
                busLocation!!.longitude,
                stoppagesForPolyLines.value,
                mapView.value!!
            )

            // Clear previous lists for updated data
            etaList.clear()
            distanceList.clear()
            actualArrivalTimes.clear()

            // Variable to hold cumulative ETA in minutes
            var cumulativeEta = 0.0
            var cumulativeDistance = 0.0

            // Get current time as the base for calculating arrival times
            val currentTime = Calendar.getInstance()

            etaDistanceArrivalList?.forEach {
                    (etaInMinutes, distanceInKm, _) ->
                // Add the current stop's ETA to the cumulative ETA
                cumulativeEta += etaInMinutes
                cumulativeDistance += distanceInKm

                // Add the cumulative ETA for the current stop
                etaList.add(cumulativeEta)

                // Add distance for the current stop
                // distanceList.add(distanceInKm)
                distanceList.add(cumulativeDistance)

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
}
