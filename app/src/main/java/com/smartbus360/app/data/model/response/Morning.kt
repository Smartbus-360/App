package com.smartbus360.app.data.model.response

data class Morning(
    val arrivalTime: String,
    val departureTime: String,
    val round: Int
)

data class Evening(
    val arrivalTime: String,
    val departureTime: String,
    val round: Int
)