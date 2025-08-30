package com.smartbus360.app.data.olaMaps

data class GeocodedWaypoint(
    val geocoder_status: String,
    val place_id: String,
    val types: List<Any>
)