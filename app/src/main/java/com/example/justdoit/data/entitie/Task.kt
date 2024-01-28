package com.example.justdoit.data.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Task")

data class Task (
    @PrimaryKey(autoGenerate = true)
    var taskId:Long = 0,
    var title: String,
    var description: String,
    var date: String,
){
    // Add a no-argument constructor
    constructor() : this(0, "", "", "")
}