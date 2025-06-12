package com.app.presentation.model

import com.app.domain.Portfolio

data class PortfolioUiModel(
    val holdings: List<Portfolio> = emptyList(),
    val totalCurrentValue: Double = 0.0,
    val totalInvestment: Double = 0.0,
    val totalPandL: Double = 0.0,
    val todaysPandL: Double = 0.0
)