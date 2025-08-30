package com.smartbus360.app.data.model.response

data class DriverLocationsListResponse(
    val `data`: List<DriverLocation>,
    val status: String
)