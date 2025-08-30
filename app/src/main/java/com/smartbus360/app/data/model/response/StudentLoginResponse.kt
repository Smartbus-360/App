package com.smartbus360.app.data.model.response

data class StudentLoginResponse(
    val email: String = "",
    val success: Boolean = false,
    val token: String = "",
    val userId: Int = 0,
    val userName: String = ""
)
