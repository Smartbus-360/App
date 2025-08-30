package com.smartbus360.app.data.model.response

data class StopX(
    val defaultAfternoonArrivalTime: Any? = null,
    val defaultAfternoonDepartureTime: Any? = null,
    val defaultArrivalTime: String? = null,
    val defaultDepartureTime: Any? = null,
    val defaultEveningArrivalTime: Any? = null,
    val defaultEveningDepartureTime: Any? = null,
    val estimatedTravelTime: String? = null,
    val latitude: Double? = null,
    val logId: Int? = null,
    val longitude: Double? = null,
    val reachDateTime: String? = null,
    val reached: Int? = null,
    val round: Int? = null,
    val routeName: String? = null,
    val stopId: Int? = null,
    val stopName: String? = null,
    val stopOrder: Int? = null,
    val totalDistance: Double? = null,
    val tripType: String? = null
)
