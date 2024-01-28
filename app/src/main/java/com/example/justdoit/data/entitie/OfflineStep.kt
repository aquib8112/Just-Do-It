package com.example.justdoit.data.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_steps")
data class OfflineStep(
    @PrimaryKey(autoGenerate = true)
    val stepId: Long = 0,
    val name: String,
    val check: Boolean,
    val taskId: Long,
    val isDeleted: Boolean = false,
    val isUpdated: Boolean = false
)