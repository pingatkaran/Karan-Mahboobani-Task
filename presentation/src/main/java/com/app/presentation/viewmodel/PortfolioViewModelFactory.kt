package com.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.usecase.GetPortfolioUseCase

class PortfolioViewModelFactory(private val useCase: GetPortfolioUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortfolioViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}