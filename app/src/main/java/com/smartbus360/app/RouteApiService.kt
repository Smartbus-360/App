package com.smartbus360.app

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.smartbus360.app.ui.screens.Geometry
import com.google.gson.annotations.SerializedName
import com.smartbus360.app.data.network.RetrofitBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// Retrofit interface for the route API to calculate ETA and distance
interface RouteApiService {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): Response<RouteResponse>
}

// Data classes for the route response
data class RouteResponse(
    @SerializedName("routes") val routes: List<Route>
)

data class Route(
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("duration") val duration: Double,  // In seconds
    @SerializedName("distance") val distance: Double  // In meters
)

suspend fun getRouteAndDrawPolyline(
    busLat: Double,
    busLon: Double,
    stopLat: Double,
    stopLon: Double,
    mapView: MapView
): Pair<Double, Double>? {
    try {
        val coordinates = "$busLon,$busLat;$stopLon,$stopLat"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(RouteApiService::class.java)

        // Make the network request to the OSRM service asynchronously
        val response = api.getRoute(coordinates)
        if (response.isSuccessful) {
            val route = response.body()?.routes?.firstOrNull()

            route?.let {
                val durationInSeconds = it.duration
                val distanceInMeters = it.distance

                // Get the route geometry and draw a polyline on the map
                val coordinatesList = it.geometry.coordinates.map { coord ->
                    GeoPoint(coord[1], coord[0]) // OSRM returns [lon, lat]
                }

                // Remove any existing polyline overlays
                mapView.overlays.removeIf { overlay -> overlay is Polyline }

                // Create and add the new polyline
                val polyline = Polyline().apply {
                    setPoints(coordinatesList)
                    color = Color.Gray.toArgb()
                    width = 6f
                }
                mapView.overlays.add(polyline)

                mapView.invalidate() // Refresh the map view

                return Pair(durationInSeconds, distanceInMeters) // Return ETA and distance
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null // Return null if route fetching fails
}



suspend fun getRouteAndDrawContinuousPolyline(
    busLat: Double,
    busLon: Double,
    stoppages: List<GeoPoint>,
    mapView: MapView
): List<Triple<Double, Double, String>>? {
    val etaDistanceArrivalList = mutableListOf<Triple<Double, Double, String>>()
    var currentBusLat = busLat
    var currentBusLon = busLon



    try {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient())
            .build()
        val api = retrofit.create(RouteApiService::class.java)

        val allCoordinates = mutableListOf<GeoPoint>()
        allCoordinates.add(GeoPoint(currentBusLat, currentBusLon))

        for (stop in stoppages) {
            if (stop.latitude == 0.0 && stop.longitude == 0.0) {
                // Add default ETA, distance, and time values for invalid coordinates
                etaDistanceArrivalList.add(Triple(0.0, 0.0, "N/A"))
                continue
            }

            val coordinates = "$currentBusLon,$currentBusLat;${stop.longitude},${stop.latitude}"
            val response = api.getRoute(coordinates)

            if (response.isSuccessful) {
                val route = response.body()?.routes?.firstOrNull()

                route?.let {
                    val durationInSeconds = it.duration
                    val distanceInMeters = it.distance

                    val coordinatesList = it.geometry.coordinates.map { coord ->
                        GeoPoint(coord[1], coord[0])
                    }

                    mapView.overlays.removeIf { overlay -> overlay is Polyline }

                    allCoordinates.addAll(coordinatesList)

                    val polyline = Polyline().apply {
                        setPoints(allCoordinates)
                        color = Color.Blue.toArgb()
                        width = 6f
                    }
                    mapView.overlays.add(polyline)
                    mapView.invalidate()

                    val etaInMinutes = durationInSeconds / 60
                    val distanceInKm = distanceInMeters / 1000

                    val currentTime = Calendar.getInstance()
                    val arrivalTime = currentTime.clone() as Calendar
                    arrivalTime.add(Calendar.SECOND, durationInSeconds.toInt())
                    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val formattedArrivalTime = formatter.format(arrivalTime.time)

                    etaDistanceArrivalList.add(Triple(etaInMinutes, distanceInKm, formattedArrivalTime))

                    currentBusLat = stop.latitude
                    currentBusLon = stop.longitude
                }
            } else {
                // If the API call fails, add a default ETA, distance, and time for the stop
                etaDistanceArrivalList.add(Triple(0.0, 0.0, "N/A"))
            }
        }

        return etaDistanceArrivalList
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

//suspend fun getRouteAndDrawContinuousPolyline(
//    busLat: Double,
//    busLon: Double,
//    stoppages: List<GeoPoint>,
//    mapView: MapView
//): List<Triple<Double, Double, String>>? {
//    val etaDistanceArrivalList = mutableListOf<Triple<Double, Double, String>>()
//
//    try {
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://router.project-osrm.org/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(getOkHttpClient())
//            .build()
//
//        val api = retrofit.create(RouteApiService::class.java)
//
//        val validStops = stoppages.filter { it.latitude != 0.0 && it.longitude != 0.0 }
//        if (validStops.isEmpty()) return null
//
//        // Combine all coordinates: bus + stoppages
//        val points = buildList {
//            add("$busLon,$busLat")
//            addAll(validStops.map { "${it.longitude},${it.latitude}" })
//        }
//
//        val coordinates = points.joinToString(";")
//        val response = api.getRoute(coordinates)
//
//        if (response.isSuccessful) {
//            val route = response.body()?.routes?.firstOrNull()
//            route?.let {
//                // Convert OSRM geometry to GeoPoints
//                val coordinatesList = it.geometry.coordinates.map { coord ->
//                    GeoPoint(coord[1], coord[0])
//                }
//
//                // Clear previous polylines
//                mapView.overlays.removeIf { overlay -> overlay is Polyline }
//
//                val polyline = Polyline().apply {
//                    setPoints(coordinatesList)
//                    color = Color.Blue.toArgb()
//                    width = 6f
//                }
//
//                mapView.overlays.add(polyline)
//                mapView.invalidate()
//
//                // ETA & arrival time estimation (approximate per stop)
//                val totalDuration = it.duration
//                val totalDistance = it.distance
//                val numStops = validStops.size
//
//                for (i in 0 until numStops) {
//                    val etaMinutes = (totalDuration / numStops) / 60
//                    val distKm = (totalDistance / numStops) / 1000
//
//                    val arrivalTime = Calendar.getInstance().apply {
//                        add(Calendar.SECOND, ((totalDuration / numStops) * (i + 1)).toInt())
//                    }
//
//                    val formattedArrival = SimpleDateFormat("hh:mm a", Locale.getDefault())
//                        .format(arrivalTime.time)
//
//                    etaDistanceArrivalList.add(Triple(etaMinutes, distKm, formattedArrival))
//                }
//            }
//        } else {
//            Log.e("RouteAPI", "OSRM response failed: ${response.code()}")
//        }
//
//        return etaDistanceArrivalList
//    } catch (e: Exception) {
//        e.printStackTrace()
//        return null
//    }
//}

//suspend fun getRouteAndDrawPolylineForMultipleStops(
//    busLat: Double,
//    busLon: Double,
//    stoppages: List<GeoPoint>,  // List of stoppages
//    mapView: MapView
//): Pair<Double, Double>? {
//    try {
//        // Build the coordinates string from the bus location to all stoppages
//        val coordinates = buildString {
//            append("$busLon,$busLat")  // Start from the bus location
//            stoppages.forEach { stop ->
//                append(";${stop.longitude},${stop.latitude}")
//            }
//        }
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://router.project-osrm.org/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        val api = retrofit.create(RouteApiService::class.java)
//
//        // Make the network request to the OSRM service asynchronously
//        val response = api.getRoute(coordinates)
//        if (response.isSuccessful) {
//            val route = response.body()?.routes?.firstOrNull()
//
//            route?.let {
//                val durationInSeconds = it.duration  // Total ETA in seconds
//                val distanceInMeters = it.distance   // Total distance in meters
//
//                // Get the route geometry and draw a continuous polyline on the map
//                val coordinatesList = it.geometry.coordinates.map { coord ->
//                    GeoPoint(coord[1], coord[0]) // OSRM returns [lon, lat]
//                }
//
//                // Remove any existing polyline overlays
//                mapView.overlays.removeIf { overlay -> overlay is Polyline }
//
//                // Create and add the new polyline
//                val polyline = Polyline().apply {
//                    setPoints(coordinatesList)
//                    color = Color.Gray.toArgb()
//                    width = 6f
//                }
//                mapView.overlays.add(polyline)
//
//                mapView.invalidate() // Refresh the map view
//
//                return Pair(durationInSeconds, distanceInMeters) // Return ETA and distance
//            }
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//    return null // Return null if route fetching fails
//}


//LaunchedEffect(busLocationUpdates.value) {
//    if (snappedBusLocation.value != null && mapView.value != null) {
//        // Call the function for multiple stoppages
//        val etaAndDistance = getRouteAndDrawPolylineForMultipleStops(
//            snappedBusLocation.value!!.latitude,
//            snappedBusLocation.value!!.longitude,
//            stoppages,
//            mapView.value!!
//        )
//
//        etaAndDistance?.let { (etaInSeconds, distanceInMeters) ->
//            etaInMinutes = etaInSeconds / 60
//            distanceInKm = distanceInMeters / 1000
//
//            // Update the actual arrival time based on current time and ETA
//            if (etaInMinutes > 0) {
//                val calendar = Calendar.getInstance()
//                calendar.add(Calendar.MINUTE, etaInMinutes.roundToInt())
//                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
//                actualArrivalTime.value = formatter.format(calendar.time)
//            }
//        }
//    } else {
//        Log.e("BusLocationScreen", "snappedBusLocation or mapView is null")
//    }
//}

//
//suspend fun getRouteAndCalculateETA(
//    busLat: Double,
//    busLon: Double,
//    stoppages: List<GeoPoint>
//): List<Triple<Double, Double, String>>? {
//    // Triple for ETA (in minutes), distance (in km), and arrival time (formatted string)
//    val etaDistanceArrivalList = mutableListOf<Triple<Double, Double, String>>()
//    var currentBusLat = busLat
//    var currentBusLon = busLon
//
//    try {
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://router.project-osrm.org/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        val api = retrofit.create(RouteApiService::class.java)
//
//        // Iterate over each stop to calculate the route from the current bus location to the stop
//        for (stop in stoppages) {
//            val coordinates = "$currentBusLon,$currentBusLat;${stop.longitude},${stop.latitude}"
//            val response = api.getRoute(coordinates)
//
//            if (response.isSuccessful) {
//                val route = response.body()?.routes?.firstOrNull()
//
//                route?.let {
//                    val durationInSeconds = it.duration
//                    val distanceInMeters = it.distance
//
//                    // Calculate ETA and distance
//                    val etaInMinutes = durationInSeconds / 60  // Convert to minutes
//                    val distanceInKm = distanceInMeters / 1000  // Convert to kilometers
//
//                    // Calculate actual arrival time
//                    val currentTime = Calendar.getInstance()
//                    val arrivalTime = currentTime.clone() as Calendar
//                    arrivalTime.add(Calendar.SECOND, durationInSeconds.toInt())
//                    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
//                    val formattedArrivalTime = formatter.format(arrivalTime.time)
//
//                    // Add ETA, distance, and arrival time to the list
//                    etaDistanceArrivalList.add(Triple(etaInMinutes, distanceInKm, formattedArrivalTime))
//
//                    // Update the bus's current location to the stop location
//                    currentBusLat = stop.latitude
//                    currentBusLon = stop.longitude
//                }
//            }
//        }
//
//        return etaDistanceArrivalList  // Return list of ETA, distance, and arrival time for each stop
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//    return null  // Return null if fetching route fails
//}



suspend fun getRouteAndCalculateETA(
    busLat: Double,
    busLon: Double,
    stoppages: List<GeoPoint>
): List<Triple<Double, Double, String>>? {
    // Triple for ETA (in minutes), distance (in km), and arrival time (formatted string)
    val etaDistanceArrivalList = mutableListOf<Triple<Double, Double, String>>()
    var currentBusLat = busLat
    var currentBusLon = busLon

    try {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient())
            .build()
        val api = retrofit.create(RouteApiService::class.java)

        // Iterate over each stop to calculate the route from the current bus location to the stop
        for (stop in stoppages) {
            // Check if stop latitude and longitude are valid
            if (stop.latitude == 0.0 || stop.longitude == 0.0) {
                // Add a default value (e.g., -1.0) for skipped points
                etaDistanceArrivalList.add(Triple(-1.0, -1.0, "N/A"))
                continue
            }

            val coordinates = "$currentBusLon,$currentBusLat;${stop.longitude},${stop.latitude}"
            val response = api.getRoute(coordinates)

            if (response.isSuccessful) {
                val route = response.body()?.routes?.firstOrNull()

                route?.let {
                    val durationInSeconds = it.duration
                    val distanceInMeters = it.distance

                    // Calculate ETA and distance
                    val etaInMinutes = durationInSeconds / 60  // Convert to minutes
                    val distanceInKm = distanceInMeters / 1000  // Convert to kilometers

                    // Calculate actual arrival time
                    val currentTime = Calendar.getInstance()
                    val arrivalTime = currentTime.clone() as Calendar
                    arrivalTime.add(Calendar.SECOND, durationInSeconds.toInt())
                    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val formattedArrivalTime = formatter.format(arrivalTime.time)

                    // Add ETA, distance, and arrival time to the list
                    etaDistanceArrivalList.add(Triple(etaInMinutes, distanceInKm, formattedArrivalTime))

                    // Update the bus's current location to the stop location
                    currentBusLat = stop.latitude
                    currentBusLon = stop.longitude
                }
            }
        }

        return etaDistanceArrivalList  // Return list of ETA, distance, and arrival time for each stop
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null  // Return null if fetching route fails
}


// Custom interceptor to handle error responses
fun getCustomInterceptor(): Interceptor {
    return Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // Handle specific error codes
        when (response.code) {
            400 -> Log.e("Bad Request", "Error: ${response.message}")
            401 -> Log.e("Unauthorized", "Error: ${response.message}")
            404 -> Log.e("Not Found", "Error: ${response.message}")
            else -> {
                if (!response.isSuccessful) {
                    Log.e("Unexpected Error", "Error code: ${response.code}, message: ${response.message}")
                }
            }
        }

        response
    }
}

// Function to create OkHttpClient with SSL configuration
fun getOkHttpClient(): OkHttpClient {
    try {
        // TrustManager for accepting all certificates
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        // Create an SSLContext that uses our trust manager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val sslSocketFactory = sslContext.socketFactory

        // Logging interceptor for debugging purposes
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Build OkHttpClient
        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager) // Custom SSL Factory
            .hostnameVerifier { _, _ -> true } // Trust all hostnames
            .addInterceptor(logging) // Logging interceptor
            .addInterceptor(getCustomInterceptor()) // Custom error handling interceptor
            .build()

    } catch (e: Exception) {
        throw RuntimeException("Failed to create a secure client: ${e.message}", e)
    }
}