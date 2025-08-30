package com.smartbus360.app.data.model.response

data class BusNotificationResponse(
    val notifications: List<NotificationX> = emptyList(),
    val success: Boolean? = null
)