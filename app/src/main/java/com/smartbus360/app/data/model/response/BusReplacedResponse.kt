package com.smartbus360.app.data.model.response

data class BusReplacedResponse(
    val message: String = "",
    val replacedBus: ReplacedBus = ReplacedBus(),
    val status: Boolean? = null
)
