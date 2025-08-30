package com.smartbus360.app.data.model.response



data class LoginResponse(  val success: Boolean,
                           val token: String,)

data class AdminLoginResponse(
    val accessToken: String,
    val user: AdminUser
)

data class AdminUser(
    val id: Int,
    val username: String,
    val email: String,
    val instituteId: Int
)
