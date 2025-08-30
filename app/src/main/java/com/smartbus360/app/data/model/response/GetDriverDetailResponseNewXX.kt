package com.smartbus360.app.data.model.response

data class GetDriverDetailResponseNewXX(
    val currentJourneyPhase: List<String> = emptyList(),
    val driver: DriverXXXX = DriverXXXX(),  // Assuming DriverXXXX has a default constructor
    val finalStopReached: List<Int> = emptyList(),
    val missedStops: List<Any?> = emptyList(),
    val routes: List<RouteXXX> = emptyList(),
    val success: Boolean? = null,
    val message : String? = null,
    val replacedBy: ReplacedDriver? = null
)
