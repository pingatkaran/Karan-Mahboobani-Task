package com.app.data.model

import com.app.domain.Portfolio

data class UserHoldingDto(
    val symbol: String,
    val quantity: Int,
    val ltp: Double,
    val avgPrice: Double,
    val close: Double
)

fun UserHoldingDto.toDomain(): Portfolio {
    return Portfolio(
        symbol = this.symbol,
        quantity = this.quantity,
        ltp = this.ltp,
        avgPrice = this.avgPrice,
        close = this.close
    )
}