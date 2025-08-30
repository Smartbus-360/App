package com.smartbus360.app.data.model.response

import com.google.gson.annotations.SerializedName

data class DriverLocation(
    @SerializedName("driver_name") val driverName: String,
    @SerializedName("id")   val id: Int,
    @SerializedName("latitude")  val latitude: String,
    @SerializedName("licence_number") val licenceNumber: String,
    @SerializedName("licence_number")  val longitude: String,
    @SerializedName("phone_number")  val phoneNumber: String
)