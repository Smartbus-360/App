package com.smartbus360.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertStatusDao {
    @Query("SELECT * FROM alert_status")
    fun getAlertStatuses(): Flow<List<AlertStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertStatus(alertStatus: AlertStatusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertStatuses(alertStatuses: List<AlertStatusEntity>)



//    @Query("DELETE FROM alert_status WHERE stopId = :stopId")
//    suspend fun deleteAlertStatus(stopId: Int)


    @Query("DELETE FROM alert_status WHERE stopId = :stopId")
    suspend fun deleteAlertStatus(stopId: Int)

    @Delete
    suspend fun deleteAlertStatus(alertStatus: AlertStatusEntity)

    @Query("DELETE FROM alert_status")
    suspend fun clearAllAlertStatuses()
}


