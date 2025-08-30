package com.smartbus360.app.data.olaMaps

import com.smartbus360.app.ui.screens.LatLng

data class RouteXX(
    val bounds:  Bounds?,
    val copyrights: String,
    val legs: List<LegX>,
    val overview_polyline: String,
    val summary: String,
    val travel_advisory: String,
    val warnings: List<Any?>,
    val waypoint_order: List<Int>
)

data class Bounds(
    val northeast: LatLng?, // Assuming it has northeast and southwest coordinates
    val southwest: LatLng?
)