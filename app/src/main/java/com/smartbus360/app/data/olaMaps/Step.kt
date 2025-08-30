package com.smartbus360.app.data.olaMaps

data class Step(
    val bearing_after: Int,
    val bearing_before: Int,
    val distance: Int,
    val duration: Int,
    val end_location: EndLocation,
    val instructions: String,
    val maneuver: String,
    val readable_distance: String,
    val readable_duration: String,
    val start_location: StartLocation
)