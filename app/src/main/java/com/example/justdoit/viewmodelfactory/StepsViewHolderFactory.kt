package com.example.justdoit.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.justdoit.repository.StepsRepo
import com.example.justdoit.viewmodel.StepsViewModel

class StepsViewHolderFactory(private val stepsRepo: StepsRepo): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StepsViewModel(stepsRepo) as T
    }
}