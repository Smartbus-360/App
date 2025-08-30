package com.smartbus360.app.viewModels

import androidx.lifecycle.ViewModel
import com.smartbus360.app.getOkHttpClient
import com.smartbus360.app.ui.screens.SnappingApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SnappingViewModel : ViewModel() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://router.project-osrm.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(getOkHttpClient())
        .build()

    private val api = retrofit.create(SnappingApiService::class.java)

    // Function to request snapped location
    suspend fun getSnappedLocationToRoad(lat: Double, lon: Double): GeoPoint? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.snapToRoad("$lon,$lat")
                if (response.isSuccessful) {
                    val coordinates = response.body()?.matchings?.firstOrNull()?.geometry?.coordinates?.firstOrNull()
                    val snappedLat = coordinates?.get(1) ?: lat
                    val snappedLon = coordinates?.get(0) ?: lon
                    return@withContext GeoPoint(snappedLat, snappedLon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext null // Return null if snapping fails
        }
    }
}
