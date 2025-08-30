package com.smartbus360.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [AlertStatusEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertStatusDao(): AlertStatusDao
}
