package com.smartbus360.app.data.model.response

data class Rounds(
    val afternoon: List<Afternoon>,
    val evening: List<Evening>,
    val morning: List<Morning>
)