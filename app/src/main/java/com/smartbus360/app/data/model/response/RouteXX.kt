package com.smartbus360.app.data.model.response

data class RouteXX(
    val routeEnd: String,
    val routeId: Int,
    val routeName: String,
    val routeFinalStopReached: Int,
    val routeCurrentJourneyPhase : String?,
    val routeStart: String,
    val stoppageArrivalTime: Any,
    val stoppageDepartureTime: Any,
    val stoppageId: Int,
    val stoppageLandmark: Any,
    val stoppageLatitude: String,
    val stoppageLongitude: String,
    val stoppageName: String,
    val stoppageOrder: Int,
    val stoppageReachDateTime: Any,
    val stoppageReached: Int,
    val stoppageStopDuration: Int,
    val stopType: String
)