package com.smartbus360.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.gson.annotations.SerializedName
import com.smartbus360.app.R
import com.smartbus360.app.data.repository.PreferencesRepository
//import com.smartbus360.app.getRouteAndDrawContinuousPolyline
import com.smartbus360.app.ui.component.NetworkSnackbar
import com.smartbus360.app.viewModels.BusLocationScreenViewModel
import com.smartbus360.app.viewModels.NetworkViewModel
import com.smartbus360.app.viewModels.SnappingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.smartbus360.app.data.olaMaps.getSmartBusTileSource
import org.osmdroid.util.BoundingBox
import retrofit2.http.Header
import retrofit2.HttpException
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable


data class LatLngPlace(val latitude: Double, val longitude: Double, val placeName: String)
data class LatLng(val latitude: Double, val longitude: Double)
data class BlockedInfo(val message: String, val until: String? = null)


//@Composable
//fun BusLocationScreen(busLocationUpdates: State<LatLngPlace>) {
//    val mapView = remember { mutableStateOf<MapView?>(null) }
//    val previousBusLocation = remember { mutableStateOf<GeoPoint?>(null) }
//    val snappedBusLocation = remember { mutableStateOf<GeoPoint?>(null) } // Hold snapped location
//    val mapOrientation = remember { mutableStateOf(0f) } // Hold the map orientation
//    val lastOrientation = remember { mutableStateOf(0f) } // Store the last known orientation
//    var compassOverlay: CompassOverlay? = remember { null }
//
//    // Launch the coroutine to get snapped location
//    LaunchedEffect(busLocationUpdates.value) {
//        // Call snapping function asynchronously inside a coroutine
//        val snappedLocation = getSnappedLocationToRoad(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//        snappedBusLocation.value = snappedLocation ?: GeoPoint(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//    }
//
//    AndroidView(
//        factory = { context ->
//            // Create the MapView
//            val mapViewInstance = MapView(context)
//            mapViewInstance.setTileSource(TileSourceFactory.MAPNIK)
//            mapViewInstance.setMultiTouchControls(true)
//            mapView.value = mapViewInstance
//
//            // Set initial map position
//            mapViewInstance.controller.setZoom(17.5)
//            val initialPosition = GeoPoint(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//            mapViewInstance.controller.setCenter(initialPosition)
//
//            // Set up CompassOverlay
//            compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapViewInstance)
//            compassOverlay?.enableCompass() // Enable compass
//            mapViewInstance.overlays.add(compassOverlay) // Add compass overlay to the map
//
//            // Start orientation provider
//            (compassOverlay?.orientationProvider as? InternalCompassOrientationProvider)?.startOrientationProvider(compassOverlay)
//
//            mapViewInstance
//        },
//        update = { mapViewInstance ->
//            mapView.value?.let { mapView ->
//
//                // Get snapped bus position or fall back to current position
//                val currentBusPosition = snappedBusLocation.value ?: GeoPoint(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//                val previousPosition = previousBusLocation.value ?: currentBusPosition
//
//                // Remove existing bus marker
//                mapView.overlays.removeIf { it is Marker }
//
//                // Add new bus marker at the snapped position
//                val busMarker = Marker(mapView)
//                busMarker.position = currentBusPosition
//
//                // Set the bus icon and anchor
//                busMarker.icon = mapView.context.getDrawable(R.drawable.bus_location_icon)
//                busMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//                busMarker.title = busLocationUpdates.value.placeName
//                mapView.overlays.add(busMarker) // Add the marker to the map
//
//                // Move the map to the new bus location
//                mapView.controller.animateTo(currentBusPosition)
//
//                // Apply the map orientation (compass rotation)
//                compassOverlay?.let { compass ->
//                    val newOrientation = compass.orientationProvider.lastKnownOrientation
//
//                    // Check if the orientation has changed significantly to avoid small jittering updates
//                    if (Math.abs(newOrientation - lastOrientation.value) > 1f) {
//                        // Smoothly interpolate between the old and new orientation for smoother rotation
//                        val interpolatedOrientation = lastOrientation.value + 0.1f * (newOrientation - lastOrientation.value)
//                        mapView.mapOrientation = interpolatedOrientation
//                        lastOrientation.value = interpolatedOrientation
//                    }
//                }
//
//                // Save the current position as previous for the next update
//                previousBusLocation.value = currentBusPosition
//            }
//        },
//        modifier = Modifier.fillMaxSize()
//    )
//
//    // Clean up compass overlay on disposal
//    DisposableEffect(Unit) {
//        onDispose {
//            compassOverlay?.disableCompass()
//        }
//    }
//}



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UseCompatLoadingForDrawables")
@Composable
fun BusLocationScreen(busLocationUpdates: MutableState<LatLngPlace>,
                      busLocationScreenViewModel: BusLocationScreenViewModel = getViewModel(),
                      viewModel: SnappingViewModel = getViewModel(),
                      networkViewModel: NetworkViewModel = getViewModel(),
                      onForceLogout: () -> Unit                    // ← ADD THIS

) {
    val context = LocalContext.current
    val mapView = remember { mutableStateOf<MapView?>(null) }
    // Add with your other remember states, e.g., after snackbarHostState
    val mapAllowed = remember { mutableStateOf<Boolean?>(null) } // null = loading
    val preferencesRepository = PreferencesRepository(context)
    val previousBusLocation = remember { mutableStateOf<GeoPoint?>(null) }
    var snappedBusLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var blockedByQr by rememberSaveable { mutableStateOf<BlockedInfo?>(null) }
    // ── QR Override push listeners ──
    // create the socket once per composition
    val socket = remember {
        // Use the same createSocket(...) you used in LocationSharingScreen
        // Replace the import/package to match your project
        createSocket("drivers", preferencesRepository.getAuthToken().orEmpty())
    }

    LaunchedEffect(socket) {
        val driverId = preferencesRepository.getDriverId() ?: return@LaunchedEffect

        // join driver room
        socket.emit("driverConnected", driverId)

        socket.on("qrOverrideActive") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as org.json.JSONObject
                val until = data.optString("until", null)
                val eventDriverId = data.optInt("driverId", -1)
                if (eventDriverId == driverId) {
                    blockedByQr = BlockedInfo("Driver is active on another device via QR.", until)
                    mapAllowed.value = false
                }
            }
        }

        socket.on("qrOverrideEnded") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as org.json.JSONObject
                val eventDriverId = data.optInt("driverId", -1)
                if (eventDriverId == driverId) {
                    blockedByQr = null
                    mapAllowed.value = true
                }
            }
        }
    }

    DisposableEffect(socket) {
        onDispose {
            socket.off("qrOverrideActive")
            socket.off("qrOverrideEnded")
        }
    }

    val mapOrientation = remember { mutableFloatStateOf(0f) }
    val lastOrientation = remember { mutableFloatStateOf(0f) }
    val shouldCenterMap =
        remember { mutableStateOf(true) }  // Flag for controlling map centering behavior
//    var alwaysCenterMap by
//        remember { mutableStateOf(preferencesRepository.isMapAlwaysCenter()) }  // Flag for controlling map centering behavior
    var alwaysCenterMap by remember {
        mutableStateOf(true) // default ON
    }

// Also save the default ON state in preferences
    LaunchedEffect(Unit) {
        preferencesRepository.setMapAlwaysCenter(true)
    }

    val coroutineScope = rememberCoroutineScope()
    var snappedLocation by remember { mutableStateOf<GeoPoint?>(null) }



    var compassOverlay: CompassOverlay? = remember { null }

    val role = preferencesRepository.getUserRole()

    // Variables to display ETA, distance, and actual arrival times for each stoppage
    val etaList = remember { mutableStateListOf<Double>() }
    val distanceList = remember { mutableStateListOf<Double>() }
    val actualArrivalTimes = remember { mutableStateListOf<String>() }

    val state = busLocationScreenViewModel.state.collectAsState()
   val stateStudent = busLocationScreenViewModel.stateStudent.collectAsState()
    val stoppagesX = busLocationScreenViewModel.stoppages.collectAsState()
//    val stoppagesForPolyLines = busLocationScreenViewModel.stoppagesForPolyLines.collectAsState()
    val stoppagesForPolyLines =
        if(preferencesRepository.getUserRole() != "driver") {
            if (stateStudent.value.user?.routeCurrentJourneyPhase == "afternoon") {
                stateStudent.value.routeStoppages?.filter { it.stopType == "afternoon"
                        &&
                        it.rounds?.afternoon?.any { round -> round.round == stateStudent.value.user?.routeCurrentRound } == true
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
            else if (stateStudent.value.user?.routeCurrentJourneyPhase == "evening"){
                stateStudent.value.routeStoppages?.filter { it.stopType == "evening"
                        &&
                        it.rounds?.evening?.any { round -> round.round == stateStudent.value.user?.routeCurrentRound } == true
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
            else if (stateStudent.value.user?.routeCurrentJourneyPhase == "morning"){
                stateStudent.value.routeStoppages?.filter { it.stopType == "morning"
                        &&
                        it.rounds?.morning?.any { round -> round.round == stateStudent.value.user?.routeCurrentRound } == true
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
            else {
                stateStudent.value.routeStoppages?.filter { it.stopType == "morning"
                        &&
                        it.rounds?.evening?.any { round -> round.round == stateStudent.value.user?.routeCurrentRound } == true
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
        }
    else{
            if (preferencesRepository.journeyFinishedState() == "afternoon") {

                state.value.routes .filter { it.stopType == "afternoon"
                        &&
                        it.rounds?.afternoon?.any { round -> round.round == it.routeCurrentRound } == true
                }.map { stoppage ->
                    if (stoppage.stoppageReached == 1) {
                        GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                    } else {
                        GeoPoint(
                            stoppage.stoppageLatitude .toDouble(),
                            stoppage.stoppageLongitude .toDouble()
                        ) // Use actual values otherwise
                    }
                }




            }
            else if(preferencesRepository.journeyFinishedState() == "evening")
            {
                state.value.routes .filter { it.stopType == "evening"
                        &&
                        it.rounds?.evening?.any { round -> round.round == it.routeCurrentRound } == true
                }.map { stoppage ->
                    if (stoppage.stoppageReached == 1) {
                        GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                    } else {
                        GeoPoint(
                            stoppage.stoppageLatitude .toDouble(),
                            stoppage.stoppageLongitude .toDouble()
                        ) // Use actual values otherwise
                    }
                }
            }

            else {

                state.value.routes .filter { it.stopType == "morning"
                        &&
                        it.rounds?.morning?.any { round -> round.round == it.routeCurrentRound } == true
                }.map { stoppage ->
                    if (stoppage.stoppageReached == 1) {
                        GeoPoint(0.0, 0.0) // Set to 0.0, 0.0 if reached
                    } else {
                        GeoPoint(
                            stoppage.stoppageLatitude.toDouble(),
                            stoppage.stoppageLongitude.toDouble()
                        ) // Use actual values otherwise
                    }
                }

            }
    }

    var busLocation =
        GeoPoint(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//    LaunchedEffect(busLocationUpdates.value) {
//        val snappedLocation = getSnappedLocationToRoad(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//        snappedBusLocation.value = snappedLocation ?: GeoPoint(busLocationUpdates.value.latitude, busLocationUpdates.value.longitude)
//    }

    val snackbarHostState = remember { SnackbarHostState() }
    val isConnected by networkViewModel.isConnected.collectAsState()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            snappedBusLocation =
                viewModel.getSnappedLocationToRoad(
                    lat = busLocationUpdates.value.latitude,
                    lon = busLocationUpdates.value.longitude
                )
        }
    }
    LaunchedEffect(Unit) {
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.smartbus360.com/")  // same base as the rest of the app
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(MapAccessApi::class.java)
            val token = preferencesRepository.getAuthToken().orEmpty()

            val resp = api.ping("Bearer $token")
            when (resp.code()) {
                401 -> {
                    if (preferencesRepository.isQrSession()) {
                        preferencesRepository.clearSession()
                        onForceLogout(); return@LaunchedEffect
                    } else {
                        // original credentials device after QR login
                        blockedByQr = BlockedInfo("You are blocked until the QR session ends.", null)
                        mapAllowed.value = false
                        return@LaunchedEffect
                    }
                }
                423 -> {
                    blockedByQr = BlockedInfo("Driver is active on another device via QR.", null)
                    mapAllowed.value = false
                    return@LaunchedEffect
                }
            }


            if (!resp.isSuccessful) {
                if (resp.code() == 403) {
                    mapAllowed.value = false
                    snackbarHostState.showSnackbar("Map access disabled by superadmin")
                } else {
                    snackbarHostState.showSnackbar("Unable to load map (code ${resp.code()})")
                }
            } else {
                mapAllowed.value = true
            }
        } catch (e: HttpException) {
            if (e.code() == 401 && preferencesRepository.isQrSession()) {
                preferencesRepository.clearSession()
                onForceLogout()
                return@LaunchedEffect
            }
            if (e.code() == 423) {
                blockedByQr = BlockedInfo("Driver is active on another device via QR.", null)
                mapAllowed.value = false
            } else if (e.code() == 403) {
                mapAllowed.value = false
                snackbarHostState.showSnackbar("Map access disabled by superadmin")
            } else {
                snackbarHostState.showSnackbar("Network error: ${e.code()}")
            }
        } catch (_: Exception) {
            snackbarHostState.showSnackbar("Network error")
        }
    }
// ── QR Block Short-Circuit (if another device holds the driver via QR) ─────────
    blockedByQr?.let { blocked ->
        Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3CD)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = blocked.message,
                        color = Color(0xFF856404),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    blocked.until?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(text = "Until: $it", color = Color(0xFF856404))
                    }
                }
            }
        }
        return
    }

// ── Map Access Short-Circuit START ─────────────────────────────────────────────
// ── Map Access Short-Circuit (tri-state) ───────────
    when (mapAllowed.value) {
        null -> {
            // Loading spinner / blank screen so the map never flashes
            Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3CD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Checking map access…", color = Color.Gray, fontSize = 14.sp)
                }
            }
            return
        }
        false -> {
            Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3CD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Map access is disabled for your institute.\nContact your admin.",
                        color = Color(0xFF856404), fontSize = 16.sp
                    )
                }
            }
            return
        }
        true -> { /* fall through and render the map */ }
    }

// ── Map Access Short-Circuit END ───────────────────────────────────────────────


   if(
       if(preferencesRepository.getUserRole() != "driver")
       stateStudent.value.success == true
       else state.value.success == true
       )   {

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (busLocationUpdates.value.latitude == 0.0 && busLocationUpdates.value.longitude == 0.0) {
                    // Driver is offline → show blue background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFEAF4FF)), // light blue
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.bus_driver_is_offline),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {


                    // MapView Setup
                    AndroidView(
                        factory = { context ->
                            val mapViewInstance = MapView(context)
                            mapViewInstance.setMultiTouchControls(true)
                            mapViewInstance.setTilesScaledToDpi(false)   // avoid double-scaling for 512px tiles
                            mapViewInstance.setTileSource(getSmartBusTileSource())
                            // Remove default OSM attribution overlay
                            mapViewInstance.overlays.removeAll { it.javaClass.simpleName == "CopyrightOverlay" }
// <- use your tiles
                            mapViewInstance.setScrollableAreaLimitDouble(
                                BoundingBox(
                                    21.439444, // north (maxLat)
                                    81.874722, // east  (maxLon)
                                    20.733889, // south (minLat)
                                    80.969444  // west  (minLon)
                                )
                            )

                            mapViewInstance.minZoomLevel = 3.0
//                        mapViewInstance.maxZoomLevel = 15.0
                            mapViewInstance.controller.setZoom(14.0) // ~2 levels closer than 11.98

                            mapViewInstance.controller.setZoom(11.98)
                            mapViewInstance.controller.setCenter(
                                org.osmdroid.util.GeoPoint(
                                    21.24050,
                                    81.53110
                                )
                            )
                            mapView.value = mapViewInstance
                            mapViewInstance.setUseDataConnection(true) // be explicit: load tiles from network


                            // Set initial map position to the bus location
//                        mapViewInstance.controller.setZoom(17.5)
                            val lat = busLocationUpdates.value.latitude
                            val lon = busLocationUpdates.value.longitude
                            if (lat != 0.0 && lon != 0.0) {
                                mapViewInstance.controller.setCenter(GeoPoint(lat, lon))
                                if (mapViewInstance.zoomLevelDouble < 14.0) {
                                    mapViewInstance.controller.setZoom(14.0)
                                }

                            }

                            val initialPosition =
                                GeoPoint(
                                    busLocationUpdates.value.latitude,
                                    busLocationUpdates.value.longitude
                                )
                            mapViewInstance.controller.setCenter(initialPosition)

                            // Set up CompassOverlay
                            compassOverlay = CompassOverlay(
                                context,
                                InternalCompassOrientationProvider(context),
                                mapViewInstance
                            )
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
                                val currentBusPosition = GeoPoint(
                                    busLocationUpdates.value.latitude,
                                    busLocationUpdates.value.longitude
                                )
                                val previousPosition =
                                    previousBusLocation.value ?: currentBusPosition

                                // Remove existing bus marker
//                    mapView.overlays.removeIf { it is Marker && it.title == "Bus Location" }
                                synchronized(mapView.overlays) {
                                    val toRemoveMarkers =
                                        mapView.overlays.filter { it is Marker && it.title == "Bus Location" }
                                    mapView.overlays.removeAll(toRemoveMarkers)
                                }


                                // Add new bus marker at snapped position
                                val busMarker = Marker(mapView)
                                busMarker.position = currentBusPosition
                                busMarker.icon =
                                    mapView.context.getDrawable(R.drawable.bus_location)
                                busMarker.title = "Bus Location"
                                mapView.overlays.add(busMarker)

//                    mapViewInstance.overlays.removeIf { it is Marker && it.title?.startsWith("Stop") == true }

                                // Remove existing stop markers
                                synchronized(mapView.overlays) {
                                    val toRemoveStopMarkers =
                                        mapView.overlays.filter {
                                            it is Marker && it.title?.startsWith(
                                                "Stop"
                                            ) == true
                                        }
                                    mapView.overlays.removeAll(toRemoveStopMarkers)
                                }

                                stoppagesForPolyLines?.forEachIndexed { index, stopGeoPoint ->
                                    val stopMarker = Marker(mapViewInstance)
                                    stopMarker.position = stopGeoPoint
//                                stopMarker.icon =
//                                    mapViewInstance.context.getDrawable(R.drawable.busstoppoint)

                                    stopMarker.icon = ContextCompat.getDrawable(
                                        mapViewInstance.context,
                                        R.drawable.stoppage_icon
                                    )

                                    stopMarker.title = "Stop ${index + 1}"
                                    stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    mapViewInstance.overlays.add(stopMarker)
                                }


                                // Only move the map to the bus location initially or when the user requests it
                                if (shouldCenterMap.value && busLocationUpdates.value.latitude != 0.0
                                    && busLocationUpdates.value.longitude != 0.0
                                ) {
                                    mapView.controller.animateTo(currentBusPosition)
                                    shouldCenterMap.value =
                                        true // Stop automatic centering after initial load
                                }
//                            if (alwaysCenterMap) {
//                                mapView.controller.animateTo(currentBusPosition)
////                        shouldCenterMap.value = false // Stop automatic centering after initial load
//                            }

                                if ((shouldCenterMap.value || alwaysCenterMap) &&
                                    busLocationUpdates.value.latitude != 0.0 &&
                                    busLocationUpdates.value.longitude != 0.0
                                ) {
                                    mapView.controller.animateTo(currentBusPosition)
                                    if (!alwaysCenterMap) {
                                        shouldCenterMap.value = false
                                    }
                                }

                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }


                // Button to re-center map to bus location
                // Stacked Floating Action Buttons for map controls
                Box(
                    modifier = Modifier.fillMaxSize() // Ensures the entire screen is used
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd) // Aligns the column to the right side
                            .padding(16.dp) // Adds padding from the edges
                    ) {
                        // First Floating Action Button with Label
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(0.2f) // Ensure consistent alignment and width
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    shouldCenterMap.value = true // Trigger map re-centering
                                },
                                containerColor = Color.White.copy(alpha = 0.8f),
                                shape = CircleShape,
                                modifier = Modifier.size(48.dp) // Standard button size
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.map_bus_location),
                                    contentDescription = "Re-center Map",
                                    modifier = Modifier.requiredSize(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp)) // Space between button and label
                            Text(
                                text = stringResource(R.string.re_center_bus),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Space between the two buttons

                        // Second Floating Action Button with Label
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(0.2f) // Ensure consistent alignment and width
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    alwaysCenterMap = !alwaysCenterMap
                                    preferencesRepository.setMapAlwaysCenter(alwaysCenterMap)

                                },
                                containerColor = if (alwaysCenterMap) Color.White.copy(alpha = 0.8f) else Color.Gray.copy(
                                    alpha = 0.9f
                                ), // Change color based on enabled state,
                                shape = CircleShape,
                                modifier = Modifier.size(48.dp) // Standard button size
                            ) {
                                Icon(
                                    painter = if (alwaysCenterMap) painterResource(id = R.drawable.center_map) else painterResource(
                                        id = R.drawable.location_disabled_icon
                                    ),
                                    contentDescription = "Always Center Map",
                                    modifier = Modifier.requiredSize(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp)) // Space between button and label
                            Text(
                                text = stringResource(R.string.always_center_map),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }


                // Display ETA, Distance, and Arrival Time for each stop
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(8.dp)
                ) {

//            if (preferencesRepository.journeyFinishedState()) {
//                val reversedState = state.value.routes.reversed()
//                val reversedRoutes = stoppagesForPolyLines.value.reversed()
//                reversedRoutes.forEachIndexed { index, stop ->
//                    if (index < etaList.size && index < distanceList.size && index < actualArrivalTimes.size && index < state.value.routes.size) {
//
//                        val reachDateTime = reversedState[index].stoppageReachDateTime
//                        val formattedTime =
//                            reachDateTime?.let { formatTo12HourTime(it.toString()) } ?: "N/A"
//
//                        when (reversedState[index].stoppageReached) {
//                            1 -> {
//                                Text(text = "Stop ${index + 1} Reached", color = Color.Black)
//                                Text(text = "Arrived: ${formattedTime}", color = Color.Red)
//                            }
//
//                            2 -> {
//                                // Missed stoppage
//                                Text(text = "Stop ${index + 1} Missed", color = Color.Gray)
////                                Text(text = "Scheduled Arrival: ${formattedTime}", color = Color.Blue)
//                            }
//
//                            else -> {
//                                Text(
//                                    text = "Stop ${index + 1}: ETA: ${etaList[index].roundToInt()} min",
//                                    color = Color.Black
//                                )
//                                Text(
//                                    text = "Distance: ${distanceList[index].roundToInt()} km",
//                                    color = Color.Blue
//                                )
//                                Text(
//                                    text = "Arrival: ${actualArrivalTimes[index]}",
//                                    color = Color.Red
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(8.dp))
//                    }
//                }
//            } else {
//                stoppagesForPolyLines.value.forEachIndexed { index, stop ->
//                    if (index < etaList.size && index < distanceList.size && index < actualArrivalTimes.size && index < state.value.routes.size) {
//
//                        val reachDateTime = state.value.routes[index].stoppageReachDateTime
//                        val formattedTime =
//                            reachDateTime?.let { formatTo12HourTime(it.toString()) } ?: "N/A"
//
//                        when (state.value.routes[index].stoppageReached) {
//                            1 -> {
//                                Text(text = "Stop ${index + 1} Reached", color = Color.Black)
//                                Text(text = "Arrived: $formattedTime", color = Color.Red)
//                            }
//
//                            2 -> {
//                                // Missed stoppage
//                                Text(text = "Stop ${index + 1} Missed", color = Color.Gray)
////                                Text(text = "Scheduled Arrival: ${formattedTime}", color = Color.Blue)
//                            }
//
//                            else -> {
//                                Text(
//                                    text = "Stop ${index + 1}: ETA: ${etaList[index].roundToInt()} min",
//                                    color = Color.Black
//                                )
//                                Text(
//                                    text = "Distance: ${distanceList[index].roundToInt()} km",
//                                    color = Color.Blue
//                                )
//                                Text(
//                                    text = "Arrival: ${actualArrivalTimes[index]}",
//                                    color = Color.Red
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(8.dp))
//                    }
//                }
//            }


                    if (role == "student" && busLocationUpdates.value.latitude == 0.0
                        && busLocationUpdates.value.longitude == 0.0
                    ) {
                        Text(
                            text = stringResource(R.string.bus_driver_is_offline),
                            color = Color.Black
                        )
                    } else {

                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp, bottom = 120.dp)
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    if (busLocationUpdates.value.latitude != 0.0) {
                        Text(
                            text = stringResource(
                                R.string.latitude,
                                busLocationUpdates.value.latitude
                            ), color = Color.Black
                        )
                        Text(
                            text = stringResource(
                                R.string.longitude,
                                busLocationUpdates.value.longitude
                            ), color = Color.Black
                        )
                        Text(
                            text = stringResource(
                                R.string.current_location,
                                busLocationUpdates.value.placeName
                            ),
                            color = Color.Black
                        )
                    }
                }
                NetworkSnackbar(
                    isConnected = isConnected,
                    snackbarHostState = snackbarHostState
                )

            }
        }

       val lastRouteFetchTime = remember { mutableStateOf(0L) }
// LaunchEffect to update polyline and calculate ETA/distance for multiple stops, including cumulative time
        // LaunchedEffect to update polyline and calculate ETA/distance for multiple stops, including cumulative time and arrival times
//        LaunchedEffect(busLocationUpdates.value) {
//            val currentTime = System.currentTimeMillis()
//            val timeSinceLastFetch = currentTime - lastRouteFetchTime.value
//            if (busLocation != null && mapView.value != null) {
//                // Fetch routes and draw continuous polylines for all stoppages
//                val etaDistanceArrivalList = stoppagesForPolyLines?.let {
//                    getRouteAndDrawContinuousPolyline(
//                        busLocation.latitude,
//                        busLocation.longitude,
//            //                if(preferencesRepository.journeyFinishedState()) stoppagesForPolyLines.value.reversed()
//            //                else stoppagesForPolyLines.value
//                        it,
//                        mapView.value!!
//                    )
//                }
//
//                // Clear previous lists for updated data
//                etaList.clear()
//                distanceList.clear()
//                actualArrivalTimes.clear()
//
//                // Variable to hold cumulative ETA in minutes
//                var cumulativeEta = 0.0
//                var cumulativeDistance = 0.0
//
//                // Get current time as the base for calculating arrival times
//                val currentTime = Calendar.getInstance()
//
//                etaDistanceArrivalList?.forEach { (etaInMinutes, distanceInKm, _) ->
//                    // Add the current stop's ETA to the cumulative ETA
//                    cumulativeEta += etaInMinutes
//                    cumulativeDistance += distanceInKm
//
//                    // Add the cumulative ETA for the current stop
//                    etaList.add(cumulativeEta)
//
//                    // Add distance for the current stop
//                    // distanceList.add(distanceInKm)
//                    distanceList.add(cumulativeDistance)
//
//                    // Calculate the arrival time by adding the cumulative ETA to the current time
//                    val arrivalTime = Calendar.getInstance().apply {
//                        timeInMillis = currentTime.timeInMillis
//                        add(Calendar.MINUTE, cumulativeEta.toInt())
//                    }
//
//                    // Format the arrival time as "hh:mm a"
//                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
//                    val formattedArrivalTime = timeFormat.format(arrivalTime.time)
//
//                    // Add formatted arrival time to the list
//                    actualArrivalTimes.add(formattedArrivalTime)
//                }
//            } else {
//                Log.e("BusLocationScreen", "snappedBusLocation or mapView is null")
//            }
//        }



//       LaunchedEffect(busLocationUpdates.value) {
//           val currentTime = System.currentTimeMillis()
//           val timeSinceLastFetch = currentTime - lastRouteFetchTime.value
//
//           if (
//               busLocation != null &&
//               mapView.value != null &&
//               timeSinceLastFetch > 60000 // 15 seconds
//           ) {
//               lastRouteFetchTime.value = currentTime
//
//               val etaDistanceArrivalList = stoppagesForPolyLines?.let {
//                   getRouteAndDrawContinuousPolyline(
//                       busLocation.latitude,
//                       busLocation.longitude,
//                       it,
//                       mapView.value!!
//                   )
//               }
//
//               etaList.clear()
//               distanceList.clear()
//               actualArrivalTimes.clear()
//
//               var cumulativeEta = 0.0
//               var cumulativeDistance = 0.0
//               val currentTimeCalendar = Calendar.getInstance()
//
//               etaDistanceArrivalList?.forEach { (etaInMinutes, distanceInKm, _) ->
//                   cumulativeEta += etaInMinutes
//                   cumulativeDistance += distanceInKm
//                   etaList.add(cumulativeEta)
//                   distanceList.add(cumulativeDistance)
//
//                   val arrivalTime = Calendar.getInstance().apply {
//                       timeInMillis = currentTimeCalendar.timeInMillis
//                       add(Calendar.MINUTE, cumulativeEta.toInt())
//                   }
//
//                   val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
//                   actualArrivalTimes.add(timeFormat.format(arrivalTime.time))
//               }
//           }
//       }
//

       // Clean up compass overlay on disposal
        DisposableEffect(Unit) {
            onDispose {
                compassOverlay?.disableCompass()
            }
        }
    }

}





//@Composable
//fun BusLocationScreen(viewModel: MapViewModel) {
//    // Fetch the route when the screen is displayed
//    LaunchedEffect(Unit) {
//        viewModel.fetchRoute(
//            origin = "23.376717042301177,85.33988182241497",
//            destination = "23.350252879497532,85.29797076323321",
//            waypoints = "23.373354243593887,85.31509546218138",
//            apiKey = "CDjyXDYiY6u9SlDQpf7brSijvyuQNgL4ZotfvRdy"
//        )
//    }
//
//    // Observe the route data and display it
//    Box(modifier = Modifier.fillMaxSize()) {
//        AndroidView(
//            factory = { context ->
//                val mapView = MapView(context)
//                mapView.setTileSource(TileSourceFactory.MAPNIK)
//                mapView.setMultiTouchControls(true)
//
//                val polyline = Polyline()
//                viewModel.route?.geometry?.let { encodedGeometry ->
//                    polyline.setPoints(decodePolyline(encodedGeometry))
//                    mapView.overlays.add(polyline)
//                }
//
//                mapView
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//    }
//}


//
//@Composable
//fun BusLocationScreen(viewModel: RouteViewModel) {
//    val routeResponse by viewModel.routeResponse.collectAsState()
//
//    Column {
//        // Trigger the route fetching
//        Button(onClick = {
//            viewModel.fetchRoutes(
//                origin = "23.376717042301177,85.33988182241497",
//                destination = "23.350252879497532,85.29797076323321",
//                waypoints = "23.373354243593887,85.31509546218138"
//            )
//        }) {
//            Text("Fetch Route")
//        }
//
//        // Display the route data if available
//        routeResponse?.let { route ->
//            Text("Route fetched: ${route.toString()}")
//            // Optionally render route details or show it on the map
//        }
//    }
//}
//


// Function to request snapped location from road-matching API
suspend fun getSnappedLocationToRoad(lat: Double, lon: Double): GeoPoint? {
    try {
        // Example request to OSRM or another snapping service
        val url = "https://router.project-osrm.org/match/v1/driving/$lon,$lat?overview=false&geometries=geojson"

        // Create Retrofit instance for network call
        val retrofit = Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(SnappingApiService::class.java)

        // Make the network request to the OSRM service asynchronously
        val response = api.snapToRoad("$lon,$lat")
        if (response.isSuccessful) {
            // Parse snapped coordinates from the response
            val coordinates = response.body()?.matchings?.firstOrNull()?.geometry?.coordinates?.firstOrNull()
            val snappedLat = coordinates?.get(1) ?: lat
            val snappedLon = coordinates?.get(0) ?: lon

            return GeoPoint(snappedLat, snappedLon)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null // Return null if snapping fails
}

// Retrofit interface for the road-snapping API
interface SnappingApiService {
    @GET("match/v1/driving/{coordinates}")
    suspend fun snapToRoad(@Path("coordinates") coordinates: String): Response<SnappingResponse>
}

// Data classes to represent the snapping API response
data class SnappingResponse(
    @SerializedName("matchings") val matchings: List<Matching>
)

data class Matching(
    @SerializedName("geometry") val geometry: Geometry
)

data class Geometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>
)
// Map access check – call any endpoint protected by canViewMap
// Map access check – dedicated endpoint, returns 204 when allowed
interface MapAccessApi {
    @GET("api/map/access-check")
    suspend fun ping(@Header("Authorization") bearer: String): retrofit2.Response<Unit>
}




@Composable
fun NotificationPermissionRequest() {
    val context = LocalContext.current
    // Check if the permission is already granted
    val isGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted.value = granted
    }

    // If not granted, request the permission
    if (!isGranted.value) {
        SideEffect {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
