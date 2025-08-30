package com.smartbus360.app.data.model.response

data class GetDriverDetailResponseNew(
    val driver: DriverXX = DriverXX(),
    val routes: List<RouteX> = emptyList(),
    val success: Boolean = false
)