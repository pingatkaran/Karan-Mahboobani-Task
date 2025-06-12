package com.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PortfolioTest {

    @Test
    fun `portfolio calculations are correct for a profitable holding`() {
        // Given
        val holding = Portfolio(
            symbol = "RELIANCE",
            quantity = 10,
            ltp = 2500.0,
            avgPrice = 2400.0,
            close = 2600.0
        )

        // Then
        assertEquals(25000.0, holding.currentValue, 0.0)
        assertEquals(24000.0, holding.investmentValue, 0.0)
        assertEquals(1000.0, holding.todaysPnL, 0.0)
        assertEquals(1000.0, holding.profitAndLoss, 0.0)
    }

    @Test
    fun `portfolio calculations are correct for a loss-making holding`() {
        // Given
        val holding = Portfolio(
            symbol = "AIRTEL",
            quantity = 100,
            ltp = 340.0,
            avgPrice = 370.0,
            close = 290.0
        )

        // Then
        assertEquals(34000.0, holding.currentValue, 0.0)
        assertEquals(37000.0, holding.investmentValue, 0.0)
        assertEquals(-5000.0, holding.todaysPnL, 0.0)
        assertEquals(-3000.0, holding.profitAndLoss, 0.0)
    }
}
