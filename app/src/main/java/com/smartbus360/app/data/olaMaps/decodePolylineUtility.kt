package com.smartbus360.app.data.olaMaps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.model.response.OlaGeocodeResponse
import com.smartbus360.app.data.olaMaps.RetrofitInstance.olaMapRouteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.util.GeoPoint
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.UUID
import java.util.concurrent.TimeUnit

// Utility to decode the polyline (encoded route geometry)
fun decodePolyline(encoded: String): List<GeoPoint> {
    val poly = ArrayList<GeoPoint>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = GeoPoint(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        poly.add(p)
    }

    return poly
}

suspend fun fetchAccessToken(): String? {
    val response = oAuthService.getAccessToken(
        clientId = "a06d7387-d085-4c4d-aa78-bfd0239d09f6" , // "bd660bff-e191-480d-b9bf-2cba3e6aecbb", // Your client ID
        clientSecret =   "U6lCXbSxQz4yH4DAAbjZKPESgwlJIwdj"                          //"lvOuXHd9WMjErtXThh5DBgh7ONgxV1ei"   // Your client secret
    )
    return if (response.isSuccessful) {
        response.body()?.accessToken
    } else {
        Log.e("OAuth", "Error fetching access token: ${response.errorBody()?.string()}")
        null
    }
}


interface OlaMapService {
    @GET("places/v1/autocomplete")
    suspend fun getAutocomplete(
        @Query("input") searchText: String,
        @Header("Authorization") authHeader: String
    ): Response<AutocompleteResponse>
}

//data class AutocompleteResponse(
//    // Define the structure of the response here
//)

val olaRetrofit = Retrofit.Builder()
    .baseUrl("https://api.olamaps.io/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val olaMapService = olaRetrofit.create(OlaMapService::class.java)

suspend fun getAutocompleteResults(searchText: String): AutocompleteResponse? {
    val accessToken = fetchAccessToken() ?: return null
    val response = olaMapService.getAutocomplete(
        searchText = searchText,
        authHeader = "Bearer $accessToken"
    )
    return if (response.isSuccessful) {
        response.body()
    } else {
        Log.e("OlaMapAPI", "Error fetching autocomplete: ${response.errorBody()?.string()}")
        null
    }
}


class AutocompleteViewModel : ViewModel() {
    private val _autocompleteResults = MutableStateFlow<List<Prediction>?>(null)
    val autocompleteResults = _autocompleteResults.asStateFlow()

    fun onSearchTextChanged(newText: String) {
        viewModelScope.launch {
            val results = getAutocompleteResults(newText)
            _autocompleteResults.value = results?.predictions
        }
    }
}

//suspend fun fetchRoute(
//    origin: String,
//    destination: String,
//    waypoints: String?
//): com.example.garudamap2.data.olaMaps.RouteResponse? {
//    val accessToken = fetchAccessToken() ?: return null  // Fetch the token if not available
//    val response = olaMapRouteService.getRoute(
//        origin = origin,
//        destination = destination,
//        waypoints = waypoints,
//        authHeader = "Bearer $accessToken"
//    )
//
//    return if (response.isSuccessful) {
//        response.body()  // Return the route response on success
//    } else {
//        Log.e("OlaMapAPI", "Error fetching route: ${response.errorBody()?.string()}")
//        null
//    }
//}

suspend fun fetchRoute(
    origin: String,
    destination: String,
    waypoints: String,
    accessToken: String
): OlaRouteResponseBody? {
    return try {
        val response = olaMapRouteService.getRoute(
            origin = origin,
            destination = destination,
            waypoints = waypoints,
            authHeader = "Bearer $accessToken",
            requestId = UUID.randomUUID().toString()
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.e("OlaMapRouteService", "Error: ${response.code()}")
            null
        }
    } catch (e: Exception) {
        Log.e("OlaMapRouteService", "Exception: ${e.message}")
        null
    }
}
suspend fun fetchGeoCode(
    latLng: String,
    accessToken: String
): OlaGeocodeResponse? {
    return try {
        val response = olaMapRouteService.getReverseGeocode(
            latlng = latLng,
            authHeader = "Bearer $accessToken",
            requestId = UUID.randomUUID().toString()
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.e("OlaMapRouteService", "Error: ${response.code()}")
            null
        }
    } catch (e: Exception) {
        Log.e("OlaMapRouteService", "Exception: ${e.message}")
        null
    }
}




class RouteViewModel : ViewModel() {
    private val _routeResponse = MutableStateFlow<OlaRouteResponseBody?>(null)
    val routeResponse = _routeResponse.asStateFlow()

    private val _geoCodeResponse = MutableStateFlow<OlaGeocodeResponse?>(null)
    val geoCodeResponse = _geoCodeResponse.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    fun fetchRoutes(origin: String, destination: String, waypoints: String?) {
        viewModelScope.launch {
            try {
                val accessToken = fetchAccessToken() ?: throw Exception("Access token is null")
                val route = waypoints?.let { fetchRoute(origin, destination, it, accessToken) }
                _routeResponse.value = route
            } catch (e: Exception) {
                _errorState.value = "Failed to fetch routes: ${e.message}"
                Log.e("RouteViewModel", "Error fetching routes", e)
            }
        }
    }

    fun fetchGeoCode(latLng: String) {
        viewModelScope.launch {
            try {
                val accessToken = fetchAccessToken() ?: throw Exception("Access token is null")
                val geoCode = fetchGeoCode(latLng, accessToken)
                _geoCodeResponse.value = geoCode
            } catch (e: Exception) {
                _errorState.value = "Failed to fetch geocode: ${e.message}"
                Log.e("RouteViewModel", "Error fetching geocode", e)
            }
        }
    }
}



//object RetrofitInstance {
//
//    private const val BASE_URL = "https://api.olamaps.io/"
//
//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val client = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
//        .build()
//
//    val retrofit: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    val olaMapRouteService: OlaMapRouteService by lazy {
//        retrofit.create(OlaMapRouteService::class.java)
//    }
//}

object RetrofitInstance {

    private const val BASE_URL = "https://api.olamaps.io/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .dns(customDns)
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
        .addInterceptor(loggingInterceptor)   // Logging for debugging
        .addInterceptor(getRetryInterceptor(3)) // Retry up to 3 times
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val olaMapRouteService: OlaMapRouteService by lazy {
        retrofit.create(OlaMapRouteService::class.java)
    }
}

private fun getRetryInterceptor(maxRetries: Int): Interceptor {
    return Interceptor { chain ->
        var attempt = 0
        var response: okhttp3.Response
        while (true) {
            try {
                response = chain.proceed(chain.request())
                break
            } catch (e: Exception) {
                attempt++
                if (attempt > maxRetries) throw e
                Log.w("RetryInterceptor", "Retrying request... attempt $attempt")
            }
        }
        response
    }
}

private val customDns = object : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            Dns.SYSTEM.lookup(hostname)
        } catch (e: UnknownHostException) {
            Log.e("DNS Error", "Unable to resolve hostname: $hostname")
            emptyList() // Return an empty list on failure
        }
    }
}
