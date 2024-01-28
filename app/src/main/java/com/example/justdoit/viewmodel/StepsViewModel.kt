package com.example.justdoit.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.justdoit.data.fireStore.FireStoreCO
import com.example.justdoit.data.entitie.Step
import com.example.justdoit.data.entitie.TaskWithSteps
import com.example.justdoit.repository.StepsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StepsViewModel(private val stepsRepo: StepsRepo) : ViewModel(){

    fun syncStep(context: Context){
        viewModelScope.launch {
            FireStoreCO().syncStep(context)
        }
    }

    fun insertStep(step:Step,context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            stepsRepo.insertStep(step,context)
        }
    }

    fun getStep(id: Long): LiveData<TaskWithSteps> {
        return stepsRepo.getStep(id)
    }

}