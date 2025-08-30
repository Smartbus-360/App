package com.smartbus360.app.data.olaMaps

data class RouteResponse(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<RouteX>,
    val source_from: String,
    val status: String
)