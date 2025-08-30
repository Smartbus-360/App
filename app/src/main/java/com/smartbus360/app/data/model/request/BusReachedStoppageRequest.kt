package com.smartbus360.app.data.model.request

data class BusReachedStoppageRequest(
    val reachDateTime: String,
    val reached: String,
    val stoppageId: String,
    val routeId : String,
    val tripType : String,
    val round: Int
)