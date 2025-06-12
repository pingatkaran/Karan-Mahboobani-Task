package com.app.domain

data class Portfolio(
    val symbol: String,
    val quantity: Int,
    val ltp: Double,
    val avgPrice: Double,
    val close: Double
) {
    val currentValue: Double
        get() = ltp * quantity

    val investmentValue: Double
        get() = avgPrice * quantity

    val todaysPnL: Double
        get() = (close - ltp) * quantity

    val profitAndLoss: Double
        get() = currentValue - investmentValue
}
