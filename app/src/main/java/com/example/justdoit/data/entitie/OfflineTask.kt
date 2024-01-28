package com.example.justdoit.data.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_tasks")
data class OfflineTask(
    @PrimaryKey(autoGenerate = false)
    val taskId: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val isDeleted: Boolean = false,
    val isUpdated: Boolean = false
)