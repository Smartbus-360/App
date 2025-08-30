package com.smartbus360.app.data.olaMaps

data class OlaRouteResponseBody(
    val geocoded_waypoints: List<GeocodedWaypointX>,
    val routes: List<RouteXX>,
    val source_from: String,
    val status: String
)