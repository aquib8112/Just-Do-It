package com.example.justdoit.data.fireStore

import android.content.Context
import com.example.justdoit.data.entitie.Step
import com.example.justdoit.data.entitie.Task

class FireStoreCO : FireStoreDAO {
    override suspend fun addTaskInFirestore(task: Task) {
        super.addTaskInFirestore(task)
    }

    override suspend fun deleteTaskAndStepsInFirestore(taskId: Long) {
        super.deleteTaskAndStepsInFirestore(taskId)
    }

    override suspend fun updateTaskInFirestore(task: Task) {
        super.updateTaskInFirestore(task)
    }

    override suspend fun syncTask(context: Context) {
        super.syncTask(context)
    }

    override suspend fun addStepInFirestore(step: Step, context: Context) {
        super.addStepInFirestore(step, context)
    }

    override suspend fun deleteStepInFirestore(taskId: Long, stepId: Long) {
        super.deleteStepInFirestore(taskId, stepId)
    }

    override suspend fun updateStepInFirestore(step: Step) {
        super.updateStepInFirestore(step)
    }

    override suspend fun syncStep(context: Context) {
        super.syncStep(context)
    }

    override suspend fun downloadUserData(context: Context) {
        super.downloadUserData(context)
    }

    override suspend fun userExist(uid: String): String? {
        return super.userExist(uid)
    }
}