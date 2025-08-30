package com.smartbus360.app.data.model.response

data class OlaGeocodeResponse(
    val error_message: String,
    val info_messages: List<Any?>,
    val plus_code: PlusCode,
    val results: List<Result>,
    val status: String
)