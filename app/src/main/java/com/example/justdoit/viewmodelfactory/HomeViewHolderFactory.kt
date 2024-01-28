package com.example.justdoit.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.justdoit.repository.HomeRepo
import com.example.justdoit.viewmodel.HomeViewModel

class HomeViewHolderFactory(private val homeRepo: HomeRepo): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(homeRepo) as T
    }
}