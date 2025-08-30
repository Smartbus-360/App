package com.smartbus360.app.data.model.response

//data class GetUserDetailResponseX(
//    val currentJourneyPhase: String,
//    val finalStopReached: Int,
//    val missedStops: Any,
//    val routeStoppages: List<RouteStoppageXX>,
//    val success: Boolean,
//    val user: UserXX
//)

data class GetUserDetailResponseX(
    val currentJourneyPhase: String? = null,
    val finalStopReached: Int? = null,
    val missedStops: Any? = null,
    val routeStoppages: List<RouteStoppageXX>? = null,
    val success: Boolean? = null,
    val user: UserXX? = null
)
