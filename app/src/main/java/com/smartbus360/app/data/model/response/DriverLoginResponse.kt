package com.smartbus360.app.data.model.response

data class DriverLoginResponse(
    val driverId: Int = 0,
    val driverName: String = "",
    val email: String = "",
    val success: Boolean = false,
    val token: String = ""
)