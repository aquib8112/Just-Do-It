package com.example.justdoit.data.entitie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Step(
    @PrimaryKey(autoGenerate = true)
    var stepId: Long =  0,
    var name: String,
    var check: Boolean,
    val taskId : Long
){
    // Add a no-argument constructor
    constructor() : this(0, "", false, 0)
}
