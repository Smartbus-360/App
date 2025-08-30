package com.smartbus360.app.data.model.response

data class Route(
    val routeEnd: String,
    val routeId: Int,
    val routeName: String,
    val routeStart: String,
    val stopLatitude: String,
    val stopLongitude: String,
    val stopName: String,
    val stopOrder: Int
)