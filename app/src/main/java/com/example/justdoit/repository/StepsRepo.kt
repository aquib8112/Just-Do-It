package com.example.justdoit.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.justdoit.data.daos.OfflineStepDAO
import com.example.justdoit.data.daos.StepDAO
import com.example.justdoit.data.fireStore.FireStoreCO
import com.example.justdoit.data.entitie.OfflineStep
import com.example.justdoit.data.entitie.Step
import com.example.justdoit.data.entitie.TaskWithSteps
import com.example.justdoit.util.isInternetAvailable

class StepsRepo(private val stepDAO: StepDAO, private val offlineStepDAO: OfflineStepDAO) {
    suspend fun insertStep(step: Step,context: Context){
        stepDAO.insertStep(step)
        val lastStep = stepDAO.getLastStep()
        val newStep = Step(lastStep.stepId,step.name,step.check,step.taskId)
        if (!isInternetAvailable(context)){
            val offlineStep = OfflineStep(lastStep.stepId,step.name,step.check,step.taskId,isDeleted = false,isUpdated = false)
            offlineStepDAO.insertOfflineStep(offlineStep)
        }else{
            FireStoreCO().addStepInFirestore(newStep,context)
        }

    }

    fun getStep(id: Long): LiveData<TaskWithSteps> {
        return stepDAO.getTaskWithStep(id)  // Directly store the LiveData
    }

}