package com.smartbus360.app.data.olaMaps

data class AutocompleteResponse(
    val error_message: String,
    val info_messages: List<Any>,
    val predictions: List<Prediction>,
    val status: String
)