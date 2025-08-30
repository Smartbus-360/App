package com.smartbus360.app.data.model.response

data class NotificationX(
    val busId: Int,
    val createdAt: String,
    val expiryDate: String,
    val id: Int,
    val isMandatory: Int,
    val message: String,
    val status: String,
    val updatedAt: String
)