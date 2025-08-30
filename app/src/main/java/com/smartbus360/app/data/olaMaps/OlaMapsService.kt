package com.smartbus360.app.data.olaMaps

import com.smartbus360.app.data.model.response.OlaGeocodeResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface OlaMapsService {
    @GET("routing/v1/directions")
    fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String,
        @Query("mode") mode: String = "driving",
        @Query("alternatives") alternatives: Boolean = false,
        @Query("steps") steps: Boolean = false,
        @Query("overview") overview: String = "full",
        @Query("language") language: String = "en",
        @Query("traffic_metadata") trafficMetadata: Boolean = false,
        @Query("api_key") apiKey: String
    ): Call<OlaRouteResponse>

}

interface OlaMapRouteService {
    @POST("routing/v1/directions")
    suspend fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String,
        @Query("mode") mode: String = "driving",
        @Query("alternatives") alternatives: Boolean = false,
        @Query("steps") steps: Boolean = false,
        @Query("overview") overview: String = "full",
        @Query("language") language: String = "en",
        @Query("traffic_metadata") trafficMetadata: Boolean = false,
        @Header("Authorization") authHeader: String,
        @Header("X-Request-Id") requestId: String
    ): Response<OlaRouteResponseBody>

    @GET("places/v1/reverse-geocode")
    suspend fun getReverseGeocode(
        @Query("latlng") latlng: String,
        @Header("Authorization") authHeader: String,
        @Header("X-Request-Id") requestId: String
    ): Response<OlaGeocodeResponse>
}


interface OlaMapsApiService {

}