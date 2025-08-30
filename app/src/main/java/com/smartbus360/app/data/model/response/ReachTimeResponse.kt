package com.smartbus360.app.data.model.response

data class ReachTimeResponse(
    val data: List<DataX> = emptyList(),
    val success: Boolean = false
)
