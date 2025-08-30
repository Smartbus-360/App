package com.smartbus360.app.data.model.response

data class RouteStoppageXX(
    val afternoonarrivalTime: String,
    val afternoondepartureTime: String,
    val arrivalTime: String,
    val departureTime: String,
    val estimatedStopDuration: Int,
    val eveningarrivalTime: Any,
    val eveningdepartureTime: Any,
    val landmark: Any,
    val latitude: String,
    val longitude: String,
    val reachDateTime: String,
    val reached: Int,
    val rounds: RoundsX? = null,
    val stopHitCount: Int,
    val stopName: String,
    val stopOrder: Int,
    val stopType: String
)