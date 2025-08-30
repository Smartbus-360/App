package com.smartbus360.app.data.olaMaps

data class RouteX(
    val bounds: String,
    val copyrights: String,
    val legs: List<Leg>,
    val overview_polyline: String,
    val summary: String,
    val travel_advisory: String,
    val warnings: List<Any?>,
    val waypoint_order: List<Int>
)