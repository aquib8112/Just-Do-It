package com.example.justdoit.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.justdoit.data.fireStore.FireStoreCO
import com.example.justdoit.data.entitie.Task
import com.example.justdoit.repository.HomeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val homeRepo: HomeRepo) : ViewModel() {

    fun syncTask(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            FireStoreCO().syncTask(context)
        }
    }

    fun getTask() : LiveData<List<Task>>{
        return homeRepo.getTask()
    }

    fun insertTask(task:Task,context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            homeRepo.insertTask(task,context)
        }
    }

}