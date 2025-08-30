package com.smartbus360.app.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateLocationRequest(
    @SerializedName("driver_id") val driverId: Int,
    @SerializedName("latitude")  val latitude: String,
    @SerializedName("longitude")  val longitude: String
)