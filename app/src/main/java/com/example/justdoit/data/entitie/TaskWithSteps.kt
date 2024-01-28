package com.example.justdoit.data.entitie

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSteps(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val steps: List<Step>
)