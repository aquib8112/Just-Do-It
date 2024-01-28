package com.example.justdoit.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.justdoit.data.entitie.OfflineTask

@Dao
interface OfflineTaskDAO {

    @Insert
    suspend fun insertOfflineTask(task: OfflineTask)

    @Delete
    suspend fun deleteOfflineTask(task: OfflineTask)

    @Upsert
    suspend fun updateOfflineTask (task: OfflineTask)

    @Query("SELECT * FROM offline_tasks")
    suspend fun getAllOfflineTasks(): List<OfflineTask>

    @Query("SELECT * FROM offline_tasks WHERE taskId = :taskId")
    suspend fun getOfflineTaskById(taskId: Long): OfflineTask?

}