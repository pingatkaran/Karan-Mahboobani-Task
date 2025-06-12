package com.app.data.source.local

import com.app.domain.Portfolio

interface LocalDataSource {
    suspend fun getPortfolios(): List<Portfolio>
    suspend fun savePortfolios(portfolios: List<Portfolio>)
}