package com.app.repository

import com.app.domain.Portfolio

interface PortfolioRepository {
    suspend fun getPortfolios(): Result<List<Portfolio>>
}