package com.smartbus360.app.data.api


import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse
import retrofit2.http.GET
import retrofit2.http.Header

data class BusResponse(
    val busId: Int,
    val busNumber: String,
    val availabilityStatus: String?,
    val driverId : Int?,
    val latitude: Double?,
    val longitude: Double?,
    val driverName: String?,
    val instituteName: String?,
    val speed: Double?

)

interface AdminApiService {
    @GET("admin/buses")
    suspend fun getBuses(
        @Header("Authorization") token: String
    ): List<BusResponse>
}
