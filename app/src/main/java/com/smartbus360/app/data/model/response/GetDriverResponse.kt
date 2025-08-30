package com.smartbus360.app.data.model.response

data class GetDriverResponse(
    val driver: DriverX = DriverX(),
    val routes: List<Route> = emptyList(),
    val success: Boolean = false
)
