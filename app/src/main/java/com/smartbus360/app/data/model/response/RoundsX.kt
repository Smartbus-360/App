package com.smartbus360.app.data.model.response

data class RoundsX(
    val afternoon: List<AfternoonX> = emptyList(),
    val evening: List<EveningX> = emptyList(),
    val morning: List<MorningX> = emptyList()
)