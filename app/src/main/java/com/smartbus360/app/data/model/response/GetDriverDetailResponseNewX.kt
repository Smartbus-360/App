package com.smartbus360.app.data.model.response

data class GetDriverDetailResponseNewX(
    val driver: DriverXXX = DriverXXX(), // Assuming DriverXXX has default values set
    val routes: List<RouteXX> = emptyList(),
    val success: Boolean = false,
    val message: Any? = null
)