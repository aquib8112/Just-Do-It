package com.example.justdoit.repository


import android.content.Context
import androidx.lifecycle.LiveData
import com.example.justdoit.data.fireStore.FireStoreCO
import com.example.justdoit.data.daos.TaskDAO
import com.example.justdoit.data.entitie.OfflineTask
import com.example.justdoit.data.entitie.Task
import com.example.justdoit.data.daos.OfflineTaskDAO
import com.example.justdoit.util.isInternetAvailable

class HomeRepo(private val taskDao: TaskDAO, private val offlineTaskDAO: OfflineTaskDAO) {

    suspend fun insertTask(task: Task,context:Context) {
        taskDao.insertTask(task)
        val lastTask = taskDao.getLastTask()
        val newTask = Task(lastTask.taskId,task.title,task.description,task.date)
        if (!isInternetAvailable(context)){
            val offlineTask = OfflineTask(lastTask.taskId, task.title, task.description, task.date, isDeleted = false,isUpdated = false)
            offlineTaskDAO.insertOfflineTask(offlineTask)
        }else{
            FireStoreCO().addTaskInFirestore(newTask)
        }
    }

    fun getTask() : LiveData<List<Task>>{
        return taskDao.getAllTask()
    }

}
