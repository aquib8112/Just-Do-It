package com.example.justdoit.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.justdoit.data.entitie.OfflineStep

@Dao
interface OfflineStepDAO {

    @Insert
    suspend fun insertOfflineStep(step: OfflineStep)

    @Upsert
    suspend fun updateOfflineStep(step: OfflineStep)

    @Delete
    suspend fun deleteOfflineStep(step: OfflineStep)

    @Query("SELECT * FROM offline_steps")
    suspend fun getAllOfflineSteps(): List<OfflineStep>

    @Query("SELECT * FROM offline_steps WHERE stepId = :stepId")
    suspend fun getOfflineStepById(stepId: Long): OfflineStep?
}