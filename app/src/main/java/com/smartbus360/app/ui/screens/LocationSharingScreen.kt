package com.smartbus360.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.utility.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.Locale
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.cert.CertificateException
import javax.net.ssl.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartbus360.app.viewModels.SpeedViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Row
import com.smartbus360.app.data.model.response.DriverLoginResponse
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.HttpException
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DriverScreen(preferencesRepository: PreferencesRepository,    driverState: DriverLoginResponse,onForceLogout: () -> Unit) {
    data class BlockedInfo(val message: String, val until: String? = null)
    var blockedByQr by rememberSaveable { mutableStateOf<BlockedInfo?>(null) }  // survive recomposition
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Get the context in a composable-safe way
    val activity = context as? Activity // Cast context to activity
    val driverId = preferencesRepository.getDriverId()
    val token = preferencesRepository.getAuthToken()
    // Local (function-scoped) data class to avoid collisions with the one in Bus screen

// Ping the server periodically; if 423, show the overlay & stop the rest of the screen
    LaunchedEffect(Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.smartbus360.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(MapAccessApi::class.java) // defined in BusLocationScreen.kt (same package)

        while (true) {
            try {
                val tokenStr = preferencesRepository.getAuthToken().orEmpty()
                val resp = api.ping("Bearer $tokenStr")
                when (resp.code()) {
                    423 -> {
                        scope.launch(Dispatchers.Main) {
                            blockedByQr = BlockedInfo("Driver is active on another device via QR.", null)
                        }
                    }
                    401 -> {
                        if (preferencesRepository.isQrSession()) {
                            // the QR device itself lost auth → logout
                            preferencesRepository.clearSession()
                            onForceLogout(); return@LaunchedEffect
                        } else {
                            // original credentials device after QR login → show blocked screen
                            scope.launch(Dispatchers.Main) {
                                blockedByQr = BlockedInfo("You are blocked until the QR session ends.", null)
                            }
                        }
                    }
                    else -> {
                        scope.launch(Dispatchers.Main) { blockedByQr = null }
                    }
                }

            } catch (e: HttpException) {
                if (e.code() == 423) blockedByQr = BlockedInfo("Driver is active on another device via QR.", null)
                if (e.code() == 401 && preferencesRepository.isQrSession()) {
                    preferencesRepository.clearSession()
                    onForceLogout(); return@LaunchedEffect
                }
            } catch (_: Exception) { /* ignore transient */ }
            delay(10_000)
        }
    }

//    if (blockedByQr != null) {
//        QrBlockedScreen(message = blockedByQr!!.message, until = blockedByQr!!.until)
//        return
//    }

    if (driverId == null || token.isNullOrBlank()) {
        // ❌ Session not ready, show fallback
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Session expired or not found.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { activity?.finishAffinity() }) {
                Text("Exit")
            }
        }
        return
    }
    val socket = remember { createSocket("drivers",preferencesRepository.getAuthToken()?:"NULL") }
    // --- QR override push listeners (instant flip) ---
    LaunchedEffect(socket) {
        val id = preferencesRepository.getDriverId() ?: return@LaunchedEffect

        // Join driver's room so backend can push to this device
        socket.emit("driverConnected", id)

        // When QR session becomes active elsewhere → block immediately
        socket.on("qrOverrideActive") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as org.json.JSONObject
                val until = data.optString("until", null)
                val eventDriverId = data.optInt("driverId", -1)
                if (eventDriverId == id) {
                    scope.launch(Dispatchers.Main) {
                        blockedByQr = BlockedInfo(
                            "Driver is active on another device via QR.",
                            until
                        )
                    }
                }
            }
        }

        socket.on("qrOverrideEnded") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as org.json.JSONObject
                val eventDriverId = data.optInt("driverId", -1)
                if (eventDriverId == id) {
                    scope.launch(Dispatchers.Main) {
                        blockedByQr = null
                    }
                }
            }
        }

    }

// Clean up listeners to avoid duplicate handlers on recomposition
    DisposableEffect(socket) {
        onDispose {
            socket.off("qrOverrideActive")
            socket.off("qrOverrideEnded")
        }
    }

    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var message by remember { mutableStateOf("Waiting for location updates...") }
    var placeName by remember { mutableStateOf("Fetching location...") }
    var showExitDialog by remember { mutableStateOf(false) }

    val speedViewModel: SpeedViewModel = viewModel()
    val currentSpeed by speedViewModel.currentSpeed.collectAsState()


    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        // Show a confirmation dialog before exiting
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                Button(onClick = {
                    // Logic to exit the app
                   // activity?.finishAffinity() // Terminates the app
                    //  finishAndRemoveTask()
                    activity?.moveTaskToBack(true)
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                Button(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Exit App?") },
            text = { Text("Are you sure you want to exit the app?") }
        )
    }


//    var latitude by remember { mutableDoubleStateOf(0.0) }
//    var longitude by remember { mutableDoubleStateOf(0.0) }

    val coroutineScope = rememberCoroutineScope()
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Request location permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                startLocationUpdates(context, fusedLocationProviderClient) { location ->
                    val speed = location.speed
                    latitude = location.latitude
                    longitude = location.longitude
                    coroutineScope.launch(Dispatchers.IO) {
                        val currentPlace = getPlaceNameFromLatLng(context, latitude, longitude)
                        val driverId = preferencesRepository.getDriverId() ?: 0
                        emitLocationUpdate(socket, driverId, latitude, longitude, currentPlace,speed)

                    }
                }
            } else {
                placeName = "Permission denied"
            }
        }
    )
    // If blocked by an active QR session on another device, show a full-screen notice and return
//    if (blockedByQr != null) {
//        QrBlockedScreen(
//            message = blockedByQr!!.message,
//            until = blockedByQr!!.until
//        )
//        return
//    }

    // Check if permission is already granted; if not, request it
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates(context, fusedLocationProviderClient) { location ->
                latitude = location.latitude
                longitude = location.longitude
                coroutineScope.launch(Dispatchers.IO) {
                    val currentPlace = getPlaceNameFromLatLng(context, latitude, longitude)
                    placeName = currentPlace
                    val driverId = preferencesRepository.getDriverId() ?: 0
                    val calculatedSpeed = if (location.hasSpeed()) (location.speed * 3.6f) else 0f // ✅ Convert m/s → km/h
                    speedViewModel.updateSpeed(calculatedSpeed)  // <-- add this line

                    emitLocationUpdate(socket, driverId, latitude, longitude, currentPlace, calculatedSpeed)

                }
            }
        } else {
            // Request permission if not already granted
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    LaunchedEffect(Unit) {
        val driverId = preferencesRepository.getDriverId() ?: 0
        socket.emit("driverConnected", driverId)   // ✅ Join driver's room
    }


//    // Get permissions and start GPS updates
//    LocationHandler(onLocationUpdate = { newLatitude, newLongitude, location ->
//        val speed = if (location.hasSpeed()) (location.speed * 3.6f) else 0f
//        speedViewModel.updateSpeed(speed)
//        latitude = newLatitude
//        longitude = newLongitude
//        message = "Location updated: Latitude = $latitude, Longitude = $longitude"
//        coroutineScope.launch(Dispatchers.IO) {
//            val currentPlace = getPlaceNameFromLatLng(context, latitude, longitude)
//            placeName = currentPlace
//            val driverId = preferencesRepository.getUserId() ?: 0
//            emitLocationUpdate(socket, driverId, latitude, longitude, currentPlace,speed)
//        }
//    })


    LocationDisplay(latitude = latitude, longitude = longitude, message = message, placeName = placeName,speed = currentSpeed)

    EmitLocationButton(
        onEmitClick = {
        Log.d("SocketIO", "Emit button clicked")
            val currentPlace = getPlaceNameFromLatLng(context, latitude, longitude)

            // Emitting location is handled automatically by GPS updates
            val driverId = preferencesRepository.getDriverId() ?: 0
            Log.d("DriverScreen", "driverId used for emit = $driverId, role = ${preferencesRepository.getUserRole()}")
            emitLocationUpdate(socket, driverId, latitude, longitude, currentPlace,currentSpeed)
    }
    )
    // --- START / STOP SHARING CONTROLS ---
    Spacer(modifier = Modifier.height(16.dp))

// We’ll store whether service is running in PreferencesRepository
// Keep Start/Stop state in SharedPreferences so it persists across app restarts
    val prefs = remember { context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE) }
    var isSharing by remember { mutableStateOf(prefs.getBoolean("LOCATION_SHARING_STARTED", false)) }

// Ask for both FINE + BACKGROUND when starting on Android 10+
    val multiPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineOk = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val bgOk = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
            results[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
        else true

        if (fineOk && bgOk) {
            startLocationService(context)
            prefs.edit().putBoolean("LOCATION_SHARING_STARTED", true).apply()
            isSharing = true
        }
    }

    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // request FINE first
                    multiPermLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                } else {
                    // On Android 10+ also need BACKGROUND for continuous updates
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        multiPermLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        )
                    } else {
                        startLocationService(context)
                        prefs.edit().putBoolean("LOCATION_SHARING_STARTED", true).apply()
                        isSharing = true
                    }
                }
            },
            enabled = !isSharing
        ) { Text("Start Sharing") }

        Button(
            onClick = {
                stopLocationService(context)
                prefs.edit().putBoolean("LOCATION_SHARING_STARTED", false).apply()
                isSharing = false
            },
            enabled = isSharing
        ) { Text("Stop Sharing") }
    }

}


@Composable
fun EmitLocationButton(onEmitClick: () -> Unit) {
    Button(onClick = onEmitClick, modifier=Modifier
        .padding( top = 50.dp, start = 70.dp, end = 50.dp  )) {
        Text("Emit Location Update")
    }
}

 //Emit the driver's current location
fun emitLocationUpdate(socket: Socket, driverId: Int, latitude: Double, longitude: Double,placeName: String,speed: Float) {

    val locationData = JSONObject().apply {
        put("driverId", driverId)
        put("latitude", latitude)
        put("longitude", longitude)
        put("placeName", placeName) // ✅ Send place name
        put("speed", speed.toDouble())  // ✅ Added speed


//        put("token", token) // Include the hardcoded token")
    }
    Log.d("SocketIO", "Emitting location update: $locationData")
    socket.emit("locationUpdate", locationData)
}
//
//fun emitLocationUpdate(socket: Socket, driverId: Int, latitude: Double, longitude: Double) {
//    // Hardcoded access token
//    val hardcodedToken = "any_hardcoded_token_here"
//
//    // Create a JSON object for the location update, including the token
//    val locationData = JSONObject().apply {
//        put("driverId", driverId)
//        put("latitude", latitude)
//        put("longitude", longitude)
//        put("token", hardcodedToken) // Include the hardcoded token
//    }
//
//    Log.d("SocketIO", "Emitting location update: $locationData")
//    socket.emit("locationUpdate", locationData)
//}

// Subscribe to a specific driver's location updates
fun subscribeToDriver(socket: Socket, driverId: Int) {
    Log.d("SocketIO", "driverId: $driverId")
    val subscribeData = JSONObject().apply {
        put("driverId", driverId)
    }
    Log.d("SocketIO", "Subscribing to driver: $driverId")
    socket.emit("subscribeToDriver", subscribeData)
}

// Listen for location updates from the server
fun listenForLocationUpdates(socket: Socket, onLocationReceived: (Double, Double, String, String) -> Unit) {
    socket.on("locationUpdate") { args ->
        if (args.isNotEmpty()) {
            val data = args[0] as JSONObject
            val latitude = data.getDouble("latitude")
            val longitude = data.getDouble("longitude")
            val driverInfo = data.getJSONObject("driverInfo")
            val driverName = driverInfo.getString("name")
            val driverPhone = driverInfo.getString("phone")

            Log.d("SocketIO", "Received location update: Latitude = $latitude, Longitude = $longitude")
            CoroutineScope(Dispatchers.Main).launch {
                onLocationReceived(latitude, longitude, driverName, driverPhone)
            }
        } else {
            Log.d("SocketIO", "No data received for locationUpdate")
        }
    }
}

// Handle GPS updates and request location permissions
@Composable
fun LocationHandler(onLocationUpdate: (Double, Double,Location) -> Unit) {
    val context = LocalContext.current
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PermissionChecker.PERMISSION_GRANTED
        )
    }

    // Permission launcher to request location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
        }
    )

    // Request location permission if not granted
    if (!hasLocationPermission) {
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Start location updates if permission is granted
    if (hasLocationPermission) {
        DisposableEffect(locationManager) {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationUpdate(location.latitude, location.longitude,location)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000L,
                1f,
                locationListener
            )

            // Clean up location listener when no longer in use
            onDispose {
                locationManager.removeUpdates(locationListener)
            }
        }
    }
}

// Create socket connections for driver and student with respective namespaces
//fun createSocket(namespace: String): Socket {
//    return try {
//        val socket = IO.socket("https://0957-2401-4900-1c37-bc2e-3c53-26f8-6303-d4e8.ngrok-free.app/$namespace")
//        socket.on(Socket.EVENT_CONNECT) {
//            Log.d("SocketIO", "$namespace connected: ${socket.id()}")
//        }
//        socket.on(Socket.EVENT_CONNECT_ERROR) {
//            Log.e("SocketIO", "Connection error: ${it[0]}")
//        }
//        socket.on(Socket.EVENT_DISCONNECT) {
//            Log.d("SocketIO", "$namespace disconnected")
//        }
//        socket.connect()
//        socket
//    } catch (e: URISyntaxException) {
//        e.printStackTrace()
//        throw RuntimeException("Socket connection error", e)
//    }
//}

fun createSocket(namespace: String, token: String): Socket {
    return try {
        val options = IO.Options().apply {
            callFactory = createUnsafeOkHttpClient()
            webSocketFactory = createUnsafeOkHttpClient()
            // Make sure the value is a List<String> as required by the API
            extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
        }
        val socket = IO.socket(
//            "https://staging.smartbus360.com/$namespace"
            "https://api.smartbus360.com/$namespace"
            , options)

//        val socket = IO.socket("https://25a4-2401-4900-8836-eb8-bd6b-818d-4cf6-a3dd.ngrok-free.app/$namespace"
//            , options)
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketIO", "$namespace connected: ${socket.id()}")
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) {
            Log.e("SocketIO", "Connection error: ${it[0]}")
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("SocketIO", "$namespace disconnected")
        }
        socket.connect()
        socket
    } catch (e: URISyntaxException) {
        e.printStackTrace()
        throw RuntimeException("Socket connection error", e)
    }
}




fun createUnsafeOkHttpClient(): OkHttpClient {
    try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

fun enableTLS12(client: OkHttpClient.Builder): OkHttpClient.Builder {
    try {
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, null, null)
        val sslSocketFactory = sslContext.socketFactory
        client.sslSocketFactory(sslSocketFactory, systemDefaultTrustManager())
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return client
}

private fun systemDefaultTrustManager(): X509TrustManager {
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(null as KeyStore?)
    return trustManagerFactory.trustManagers[0] as X509TrustManager
}





@Composable
fun StudentScreen(repository: PreferencesRepository) {
    val socket = remember { createSocket("users",
        repository.getAuthToken()?:"null") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var driverName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Waiting for driver's location...") }

    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current // Get the context in a composable-safe way
    val activity = context as? Activity // Cast context to activity

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        // Show a confirmation dialog before exiting
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                Button(onClick = {
                    //    activity?.finishAffinity() // Terminates the app
                    activity?.moveTaskToBack(true)
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                Button(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Exit App?") },
            text = { Text("Are you sure you want to exit the app?") }
        )
    }


    LaunchedEffect(Unit) {
        val driverId = 1
        subscribeToDriver(socket, driverId) // Subscribe to a specific driver, replace 1
        // with driverId
        listenForLocationUpdates(socket) { newLatitude, newLongitude, name, phone ->
            latitude = newLatitude
            longitude = newLongitude
            driverName = name
            driverPhone = phone
            message = "Driver Location: Latitude = $latitude, Longitude = $longitude"
        }
    }

    LocationDisplayWithDriver(latitude = latitude, longitude = longitude,
        message = message,
        driverName = driverName,
        driverPhone = driverPhone)
}

@Composable
fun LocationDisplay(latitude: Double, longitude: Double, message: String, placeName: String,speed: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Current Latitude: $latitude")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Current Longitude: $longitude")
        Text(text = "Current Place: $placeName")
        Text(text = "Speed: ${"%.2f".format(speed)} km/h")

    }
}

@Composable
fun LocationDisplayWithDriver(latitude: Double, longitude: Double, driverName: String,
                              driverPhone: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Driver: $driverName")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Phone: $driverPhone")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Current Latitude: $latitude")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Current Longitude: $longitude")

    }

}
//@Composable
//fun DriverBlockedScreen(message: String, until: String?, onDone: () -> Unit) {
//    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Text(message, fontWeight = FontWeight.Bold, fontSize = 18.sp)
//            if (!until.isNullOrBlank()) {
//                Spacer(Modifier.height(8.dp))
//                Text("Until: $until")
//            }
//            Spacer(Modifier.height(16.dp))
//            Button(onClick = onDone) { Text("OK") }
//        }
//    }
//}
//

// Helper function to get place name from latitude and longitude
fun getPlaceNameFromLatLng(context: Context, latitude: Double, longitude: Double): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            listOfNotNull(
                address.thoroughfare,
                address.subLocality ,
                address.locality, // City or town
                address.adminArea, // State or region
                address.countryName // Country
            ).joinToString(", ") // Join the components with a comma
        } else {
            "No address found"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Unable to get address"
    }
}


// Start receiving continuous location updates
fun startLocationUpdates(
    context: Context,
    fusedLocationProviderClient: FusedLocationProviderClient,
    onLocationUpdate: (Location) -> Unit
) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 10000L
    ).setMinUpdateDistanceMeters(1f).build()

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    onLocationUpdate(location)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}

fun startLocationService(context: Context) {
    val intent = Intent(context, LocationService::class.java).apply {
        action = LocationService.ACTION_START_SERVICE   // use constant
    }
    ContextCompat.startForegroundService(context, intent)
}

fun stopLocationService(context: Context) {
    val intent = Intent(context, LocationService::class.java).apply {
        action = LocationService.ACTION_STOP_SERVICE
    }
    context.stopService(intent)
    context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        .edit().putBoolean("LOCATION_SHARING_STARTED", false).apply()

}
