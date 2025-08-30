package com.smartbus360.app.data.model.request

data class LoginRequest(
    val email: String,
    val password: String
)
data class AdminLoginRequest(
    val email: String,
    val password: String,
    val client_id: String,
    val client_secret: String
)
data class QrExchangeRequest(
    val token: String
)

