package com.smartbus360.app.utility

//class LocationService : Service() {
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var wakeLock: PowerManager.WakeLock
//
//    override fun onCreate() {
//        super.onCreate()
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        acquireWakeLock()
//        startForegroundService()
//        requestLocationUpdates()
//    }
//
//    private fun acquireWakeLock() {
//        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
//        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationService::WakelockTag")
//        if (!wakeLock.isHeld) {
//            wakeLock.acquire()
//        }
//    }
//
//    private fun startForegroundService() {
//        val channelId = "LocationServiceChannel"
//        val channelName = "Location Service"
//        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//        val stopServiceIntent = Intent(this, LocationService::class.java).apply {
//            action = ACTION_STOP_SERVICE
//        }
//        val stopServicePendingIntent = PendingIntent.getService(this, 0, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//        val notification: Notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("Tracking Location")
//            .setContentText("Location service is running")
//            .setSmallIcon(R.drawable.logogaruda)
//            .setContentIntent(pendingIntent)
//            .addAction(R.drawable.stop_sign, "Stop", stopServicePendingIntent) // Add stop action
//            .build()
//
//        startForeground(1, notification)
//    }
//
//    private fun requestLocationUpdates() {
//        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
//            .setMinUpdateIntervalMillis(2000)
//            .build()
//
//        val hasFineLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        val hasBackgroundLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
//
//        if (hasFineLocationPermission && hasBackgroundLocationPermission) {
//            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
//                override fun onLocationResult(locationResult: LocationResult) {
//                    for (location in locationResult.locations) {
//                        Log.d("LocationService", "Location update: Lat = ${location.latitude}, Lon = ${location.longitude}")
//                        // Emit location to the server here
//                        emitLocationUpdate(location.latitude, location.longitude)
//                    }
//                }
//
//                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
//                    if (!locationAvailability.isLocationAvailable) {
//                        Log.e("LocationService", "Location is not available")
//                    }
//                }
//            }, null)
//        } else {
//            Log.e("LocationService", "Location permissions not granted")
//        }
//    }
//
//    private fun emitLocationUpdate(latitude: Double, longitude: Double) {
//        // Emit location via WebSocket here, similar to how it's done in DriverScreen
//        val intent = Intent("LOCATION_UPDATE")
//        intent.putExtra("latitude", latitude)
//        intent.putExtra("longitude", longitude)
//        sendBroadcast(intent) // Broadcast the location update
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (wakeLock.isHeld) {
//            wakeLock.release()  // Release the WakeLock when service is destroyed
//        }
//        fusedLocationClient.removeLocationUpdates(locationCallback) // Stop location updates
//    }
//
//    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            // Handle location results
//        }
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null // We don't provide binding
//    }
//
//    companion object {
//        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
//    }
//}

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.smartbus360.app.MainActivity
import com.smartbus360.app.R
import com.smartbus360.app.data.repository.PreferencesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.smartbus360.app.data.model.request.BusReachedStoppageRequest
import com.smartbus360.app.data.model.response.GetDriverDetailResponseNewXX
import com.smartbus360.app.data.model.response.RouteXXX
import com.smartbus360.app.navigation.isWithinRadius
import com.smartbus360.app.navigation.showLocationReachedNotification
import com.smartbus360.app.viewModels.BusLocationScreenViewModel
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.net.URISyntaxException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

//27-02-2025 original Location Share code



class LocationService : Service() {
    companion object {
        const val ACTION_START_SERVICE = "com.smartbus360.app.START_LOCATION"
        const val ACTION_STOP_SERVICE  = "com.smartbus360.app.STOP_LOCATION"
    }

    private val repository: PreferencesRepository by inject()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var wakeLock: PowerManager.WakeLock
    private val busLocationScreenViewModel: BusLocationScreenViewModel by inject()
    private lateinit var state: StateFlow<GetDriverDetailResponseNewXX> // Adjust the type as needed
    private var triggeredStoppages = mutableSetOf<String>()
    val exitedStoppages = mutableStateOf(mutableSetOf<String>())
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    //    private lateinit var socket: Socket
private var socket: Socket? = null

    private var driverId: Int = 0 // To be set based on user role








    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        acquireWakeLock()
        startForegroundService()
        // 🔐 Get token & driverId from your repository
        val token = repository.getAuthToken().orEmpty()
        driverId = repository.getUserId() ?: 0

        // 🌐 Connect socket (drivers namespace)
        socket = createSocket("drivers", token).apply {
            emit("driverConnected", driverId)
        }

//        createSocket("locationNamespace", "your_token") // Pass the appropriate namespace and token
        requestLocationUpdates()
//registerPhoneStateListener() // Register phone state listener

        // Access the ViewModel state
//        serviceScope.launch {
//            busLocationScreenViewModel.state.collect { stateValue ->
//                // Use the collected state value here
//                Log.d("LocationService", "Collected state: $stateValue")
//            }
//        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationService::WakelockTag")
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
    }

    private fun startForegroundService() {
        val channelId = "LocationServiceChannel"
        val channelName = "Location Service"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                .apply {
                description = "Used for location tracking in the background."
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopServiceIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopServicePendingIntent = PendingIntent.getService(this, 0, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking Location")
            .setContentText("Location service is running")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSmallIcon(R.drawable.smartbus360__5__removebg_preview)

            .setContentIntent(pendingIntent)
//            .addAction(R.drawable.stop_sign, "Stop", stopServicePendingIntent) // Add stop action
            .setOngoing(true)
            .build()

//        startForeground(1, notification)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }
    }






 private fun createSocket(namespace: String, token: String): Socket {
        return try {
            val options = IO.Options().apply {
                // Make sure the value is a List<String> as required by the API
                extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
            }
            val socket = IO.socket("https://api.smartbus360.com/$namespace", options)
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


    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

//        val hasFineLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        val hasBackgroundLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED



        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasBackgroundLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED





        if (hasFineLocationPermission && hasBackgroundLocationPermission) {
            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        Log.d("LocationService", "Location update: Lat = ${location.latitude}, Lon = ${location.longitude}, Speed = ${location.speed} m/s")

                        // Emit location update
//                        emitLocationUpdate(driverId, location.latitude, location.longitude)

                        val speedKmh = location.speed
                        emitLocationUpdate(driverId, location.latitude, location.longitude, speedKmh)

                        // Call checkAndHandleStopReached with dynamic speed
                        checkAndHandleStopReached(location.latitude, location.longitude)
//
                        // Call checkJourneyStateAndHandle
//                        checkJourneyStateAndHandle()
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (!locationAvailability.isLocationAvailable) {
                        Log.e("LocationService", "Location is not available")
                    }
                }
            }, null)
        } else {
            Log.e("LocationService", "Location permissions not granted")
        }

    }



    private fun emitLocationUpdate(driverId: Int, latitude: Double, longitude: Double,speedKmh : Float) {
        val locationData = JSONObject().apply {
            put("driverId", driverId)
            put("latitude", latitude)
            put("longitude", longitude)
            put("speed", speedKmh.toDouble())

        }
        Log.d("SocketIO", "Emitting location update: $locationData")
        socket?.emit("locationUpdate", locationData)

        // Broadcast the location update
        Intent("LOCATION_UPDATE").also { intent ->
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            sendBroadcast(intent)
        }
    }

    //
// stop reached Logic

    private fun checkAndHandleStopReached(latitude: Double, longitude: Double) {
//        val routes = state.value.routes
//        val repository = PreferencesRepository(context)


        if (repository.journeyFinishedState() == "afternoon") {
            if (busLocationScreenViewModel.state.value.routes.isNotEmpty()) {
                busLocationScreenViewModel.state.value.routes.filter {
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

                    if (isInsideRadius && (stoppageId !in triggeredStoppages || stoppageId in exitedStoppages.value)) {

                        // Remove from exitedStoppages since it's re-entered
                        exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }

                        // Prepare API request
                        val reachDateTime = ZonedDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                        val formattedTime = reachDateTime.format(formatter)

                        val request = BusReachedStoppageRequest(
                            formattedTime,
                            "1",
                            stoppageId,
                            stoppage.routeId.toString(),
                            stoppage.stopType.toString(),
                            stoppage.routeCurrentRound
                        )

                        // Trigger API
                        serviceScope.launch {
                            busLocationScreenViewModel.busReachedStoppage(request)
                        }

                        // Send notification
                        showLocationReachedNotification(
                            context = this,
                            title = "Stoppage Reached",
                            message = "You have reached ${stoppage.stoppageName} at $formattedTime"
                        )

                        // Add to triggered list
                        triggeredStoppages = triggeredStoppages.toMutableSet().apply { add(stoppageId) }
                    }
                    // If bus moves out of the radius, mark it as exited
                    else if (!isInsideRadius && stoppageId in triggeredStoppages) {
                        exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
                        triggeredStoppages = triggeredStoppages.toMutableSet().apply { remove(stoppageId) }
                    }
                }
            }
        }


        else if(repository.journeyFinishedState() == "evening" ){
            if (busLocationScreenViewModel.state.value.routes.isNotEmpty()) {
                busLocationScreenViewModel.state.value.routes.filter { it.stopType == "evening"
                        &&
                        it.rounds?.evening?.any { round -> round.round == it.routeCurrentRound } == true
                }.forEach { stoppage ->
                    val stoppageId = stoppage.stoppageId.toString()
                    val isInsideRadius = isWithinRadius(
                        currentLatitude = latitude,
                        currentLongitude = longitude,
                        targetLatitude = stoppage.stoppageLatitude.toDouble(),
                        targetLongitude = stoppage.stoppageLongitude.toDouble()
                    )

                    if (isInsideRadius && (stoppageId !in triggeredStoppages || stoppageId in exitedStoppages.value)) {

                        // Remove from exitedStoppages since it's re-entered
                        exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }

                        // Prepare API request
                        val reachDateTime = ZonedDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                        val formattedTime = reachDateTime.format(formatter)

                        val request = BusReachedStoppageRequest(
                            formattedTime,
                            "1",
                            stoppageId,
                            stoppage.routeId.toString(),
                            stoppage.stopType.toString(),
                            stoppage.routeCurrentRound
                        )

                        // Trigger API
                        serviceScope.launch {
                            busLocationScreenViewModel.busReachedStoppage(request)
                        }

                        // Send notification
                        showLocationReachedNotification(
                            context = this,
                            title = "Stoppage Reached",
                            message = "You have reached ${stoppage.stoppageName} at $formattedTime"
                        )

                        // Add to triggered list
                        triggeredStoppages = triggeredStoppages.toMutableSet().apply { add(stoppageId) }
                    }
                    // If bus moves out of the radius, mark it as exited
                    else if (!isInsideRadius && stoppageId in triggeredStoppages) {
                        exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
                        triggeredStoppages = triggeredStoppages.toMutableSet().apply { remove(stoppageId) }
                    }
                }
            }
        }
        else if(repository.journeyFinishedState() == "morning" ){
            if (busLocationScreenViewModel.state.value.routes.isNotEmpty()) {
                busLocationScreenViewModel.state.value.routes.filter { it.stopType == "morning"
                        &&
                        it.rounds?.morning?.any { round -> round.round == it.routeCurrentRound } == true
                }.forEach { stoppage ->
                    val stoppageId = stoppage.stoppageId.toString()
                    val isInsideRadius = isWithinRadius(
                        currentLatitude = latitude,
                        currentLongitude = longitude,
                        targetLatitude = stoppage.stoppageLatitude.toDouble(),
                        targetLongitude = stoppage.stoppageLongitude.toDouble()
                    )

                    if (isInsideRadius && (stoppageId !in triggeredStoppages || stoppageId in exitedStoppages.value)) {

                        // Remove from exitedStoppages since it's re-entered
                        exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { remove(stoppageId) }

                        // Prepare API request
                        val reachDateTime = ZonedDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                        val formattedTime = reachDateTime.format(formatter)

                        val request = BusReachedStoppageRequest(
                            formattedTime,
                            "1",
                            stoppageId,
                            stoppage.routeId.toString(),
                            stoppage.stopType.toString(),
                            stoppage.routeCurrentRound
                        )

                        // Trigger API
                        serviceScope.launch {
                            busLocationScreenViewModel.busReachedStoppage(request)
                        }

                        // Send notification
                        showLocationReachedNotification(
                            context = this,
                            title = "Stoppage Reached",
                            message = "You have reached ${stoppage.stoppageName} at $formattedTime"
                        )

                        // Add to triggered list
                        triggeredStoppages = triggeredStoppages.toMutableSet().apply { add(stoppageId) }
                    }
                    // If bus moves out of the radius, mark it as exited
                    else if (!isInsideRadius && stoppageId in triggeredStoppages) {
                        exitedStoppages.value = exitedStoppages.value.toMutableSet().apply { add(stoppageId) }
                        triggeredStoppages = triggeredStoppages.toMutableSet().apply { remove(stoppageId) }
                    }
                }
            }
        }

    }






    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {

                // ensure we’re running foreground + requesting updates
                requestLocationUpdates()
                return START_NOT_STICKY
            }
            ACTION_STOP_SERVICE -> {
                stopUpdatesAndStopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // started by system restart or without explicit action — keep running
                return START_NOT_STICKY
            }
        }
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        stopUpdatesAndStopSelf()
        super.onTaskRemoved(rootIntent)
    }


//    override fun onDestroy() {
//        super.onDestroy()
//        serviceJob.cancel() // Cancel all coroutines when the service is destroyed
//        val repository = PreferencesRepository(this)
//        if (wakeLock.isHeld) {
//            wakeLock.release()  // Release the WakeLock when service is destroyed
//        }
//        fusedLocationClient.removeLocationUpdates(locationCallback) // Stop location updates
//       // socket.disconnect() // Disconnect the socket when service is destroyed
//
//        socket?.let {
//            it.disconnect() // Disconnect the socket if it's initialized
//            it.close()      // Close the socket if it's initialized
//        }
////          unregisterPhoneStateListener()
//
//    }

    override fun onDestroy() {
        stopUpdatesAndStopSelf()
        super.onDestroy()
    }

    private fun stopUpdatesAndStopSelf() {
        // 1) stop location updates
        try { fusedLocationClient.removeLocationUpdates(locationCallback) } catch (_: Exception) {}

        // 2) disconnect socket
        try { socket?.disconnect(); socket?.close(); socket = null } catch (_: Exception) {}

        // 3) release wakelock
        try { if (::wakeLock.isInitialized && wakeLock.isHeld) wakeLock.release() } catch (_: Exception) {}

        // 4) reset “started” UI flag so button shows Stopped on next launch
        getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            .edit()
            .putBoolean("LOCATION_SHARING_STARTED", false)
            .apply()

        // 5) remove foreground + stop service
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION") stopForeground(true)
            }
        } catch (_: Exception) {}

        stopSelf()
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                Log.d(
                    "LocationService",
                    "Location update: Lat = ${location.latitude}, Lon = ${location.longitude}, Speed = ${location.speed} m/s"
                )

                // (Optional) Convert m/s -> km/h if you need it
                // val speedKmh = location.speed * 3.6f

                // Your existing logic:
                // emitLocationUpdate(driverId, location.latitude, location.longitude)
                checkAndHandleStopReached(location.latitude, location.longitude)
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            if (!locationAvailability.isLocationAvailable) {
                Log.e("LocationService", "Location is not available")
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't provide binding
    }




}



