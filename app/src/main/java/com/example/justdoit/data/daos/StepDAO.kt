package com.example.justdoit.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.justdoit.data.entitie.Step
import com.example.justdoit.data.entitie.TaskWithSteps

@Dao
interface StepDAO {
    @Upsert
    suspend fun insertStep (step: Step)

    @Upsert
    suspend fun updateStep (step: Step)

    @Delete
    suspend fun deleteStep (step: Step)

    @Query("SELECT * FROM Step ORDER BY stepId DESC LIMIT 1")
    suspend fun getLastStep(): Step

    @Query("SELECT * FROM Task WHERE taskId = :taskId")
    fun getTaskWithStep(taskId: Long): LiveData<TaskWithSteps>

}