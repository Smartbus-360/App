package com.smartbus360.app.data.model.response

//data class RouteStoppage(
//    val arrivalTime: String,
//    val departureTime: String,
//    val estimatedStopDuration: Int,
//    val landmark: String,
//    val latitude: String,
//    val longitude: String,
//    val reachDateTime: String,
//    val reached: Int,
//    val stopName: String,
//    val stopOrder: Int,
//    val stopType : String
//)

data class RouteStoppage(
    val stopName: String,
    val stopOrder: Int,
    val latitude: String,
    val longitude: String,
    val estimatedStopDuration: Int,
    val arrivalTime: String?, // Nullable because it's null in the JSON data
    val departureTime: String?, // Nullable because it's null in the JSON data
    val afternoonarrivalTime: String?, // Nullable
    val afternoondepartureTime: String?, // Nullable
    val eveningarrivalTime: String?, // Nullable
    val eveningdepartureTime: String?, // Nullable
    val landmark: String?, // Nullable because it can be null
    val reached: Int,
    val reachDateTime: String?, // Nullable
    val stopType: String
)
