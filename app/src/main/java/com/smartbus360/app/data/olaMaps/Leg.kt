package com.smartbus360.app.data.olaMaps

data class Leg(
    val distance: Int,
    val duration: Int,
    val end_address: String,
    val end_location: EndLocation,
    val readable_distance: String,
    val readable_duration: String,
    val start_address: String,
    val start_location: StartLocation,
    val steps: List<Step>
)