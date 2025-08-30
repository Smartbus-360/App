package com.smartbus360.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_status")
data class AlertStatusEntity(
    @PrimaryKey val stopId: Int,
    val isEnabled: Boolean
)

