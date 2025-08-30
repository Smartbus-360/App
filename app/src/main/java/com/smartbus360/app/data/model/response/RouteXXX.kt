package com.smartbus360.app.data.model.response

data class RouteXXX(
    val afternoonStoppageArrivalTime: String,
    val afternoonStoppageDepartureTime: String,
    val eveningStoppageArrivalTime: Any,
    val eveningStoppageDepartureTime: Any,
    val rounds: Rounds? = null,
    val routeCurrentJourneyPhase: String,
    val routeCurrentRound: Int,
    val routeEnd: String,
    val routeFinalStopReached: Int,
    val routeId: Int,
    val routeMissedStops: Any,
    val routeName: String,
    val routeStart: String,
    val stopType: String,
    val stoppageArrivalTime: String,
    val stoppageDepartureTime: String,
    val stoppageId: Int,
    val stoppageLandmark: Any,
    val stoppageLatitude: String,
    val stoppageLongitude: String,
    val stoppageName: String,
    val stoppageOrder: Int,
    val stoppageReachDateTime: Any,
    val stoppageReached: Int,
    val stoppageStopDuration: Int
)