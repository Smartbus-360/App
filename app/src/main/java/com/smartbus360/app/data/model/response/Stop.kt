package com.smartbus360.app.data.model.response

data class Stop(
    val defaultAfternoonArrivalTime: Any,
    val defaultAfternoonDepartureTime: Any,
    val defaultArrivalTime: String,
    val defaultDepartureTime: String,
    val defaultEveningArrivalTime: Any,
    val defaultEveningDepartureTime: Any,
    val estimatedTravelTime: String,
    val latitude: Double,
    val logId: Int,
    val longitude: Double,
    val reachDateTime: String,
    val reached: Int,
    val routeName: String,
    val stopId: Int,
    val stopName: String,
    val stopOrder: Int,
    val totalDistance: Int,
    val tripType: String
)