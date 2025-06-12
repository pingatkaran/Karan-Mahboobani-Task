package com.app.usecase

import com.app.domain.Portfolio
import com.app.repository.PortfolioRepository

class GetPortfolioUseCase(private val portfolioRepository: PortfolioRepository) {
    suspend operator fun invoke(): Result<List<Portfolio>> {
        return portfolioRepository.getPortfolios()
    }
}