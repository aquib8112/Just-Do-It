package com.example.justdoit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.justdoit.data.daos.OfflineStepDAO
import com.example.justdoit.data.daos.OfflineTaskDAO
import com.example.justdoit.data.daos.StepDAO
import com.example.justdoit.data.daos.TaskDAO
import com.example.justdoit.data.entitie.OfflineStep
import com.example.justdoit.data.entitie.OfflineTask
import com.example.justdoit.data.entitie.Step
import com.example.justdoit.data.entitie.Task

@Database(entities = [Task::class, Step::class,OfflineTask::class,OfflineStep::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao() : TaskDAO
    abstract fun stepDao() : StepDAO
    abstract fun offlineTaskDao() : OfflineTaskDAO
    abstract fun offlineStepDao() : OfflineStepDAO

    companion object{
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context):TaskDatabase{
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        TaskDatabase::class.java,
                        "TaskDB"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}