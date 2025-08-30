package com.smartbus360.app.data.model.response

data class NotificationResponse(
    val notifications: List<Notification> = emptyList(),
    val success: Boolean? = null
)