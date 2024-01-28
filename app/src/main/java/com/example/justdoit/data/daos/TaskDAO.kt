package com.example.justdoit.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.justdoit.data.entitie.Task

@Dao
interface TaskDAO {
    @Upsert
    suspend fun insertTask (task: Task)

    @Upsert
    suspend fun updateTask (task: Task)

    @Delete
    suspend fun deleteTask (task: Task)

    @Query("DELETE FROM Task")
    suspend fun clearAllTask()

    @Query("SELECT * FROM task")
    fun getAllTask(): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<Task>)

    @Query("SELECT * FROM Task WHERE taskId = :taskId")
    suspend fun getTaskById(taskId: Long): Task

    @Query("SELECT * FROM Task ORDER BY taskId DESC LIMIT 1")
    suspend fun getLastTask(): Task

    @Query("DELETE FROM Step WHERE taskId = :taskId")
    suspend fun deleteStepsForTask(taskId: Long)

    @Query("SELECT * FROM Task WHERE date = :selectedDate ORDER BY title ASC")
    fun getTasksByDate(selectedDate: String): LiveData<List<Task>>

}
