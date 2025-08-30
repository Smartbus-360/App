package com.smartbus360.app.data.model.response

data class GetUserDetailResponse(
    val routeStoppages: List<RouteStoppage> = emptyList(),
    val success: Boolean = false,
    val user: User = User(),
    val message: Any? = null,
   val missedStops : Any? = null,
val currentJourneyPhase: String? = null,
val finalStopReached : Int = 0
)