package com.app.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.presentation.model.PortfolioUiModel
import com.app.usecase.GetPortfolioUseCase
import kotlinx.coroutines.launch

class PortfolioViewModel(private val getPortfoliosUseCase: GetPortfolioUseCase) : ViewModel() {
    private val _portfolioData = MutableLiveData<UiState<PortfolioUiModel>>()
    val portfolioData: LiveData<UiState<PortfolioUiModel>> = _portfolioData

    init {
        fetchPortfolios()
    }

    fun fetchPortfolios() {
        _portfolioData.value = UiState.Loading
        viewModelScope.launch {
            val result = getPortfoliosUseCase()
            result.onSuccess { holdings ->
                val totalCurrentValue = holdings.sumOf { it.currentValue }
                val totalInvestment = holdings.sumOf { it.investmentValue }
                val todaysPandL = holdings.sumOf { it.todaysPnL }
                val totalPandL = totalCurrentValue - totalInvestment
                val percentage = if (totalInvestment != 0.0) {
                    (totalPandL / totalInvestment) * 100
                } else {
                    0.0
                }
                val uiModel = PortfolioUiModel(
                    holdings = holdings,
                    totalCurrentValue = totalCurrentValue,
                    totalInvestment = totalInvestment,
                    totalPandL = totalCurrentValue - totalInvestment,
                    todaysPandL = todaysPandL,
                    totalPandLPercentage = percentage
                )
                _portfolioData.value = UiState.Success(uiModel)
            }.onFailure { err ->
                _portfolioData.value = UiState.Error(err.message ?: "Unknown error")
            }
        }
    }
}