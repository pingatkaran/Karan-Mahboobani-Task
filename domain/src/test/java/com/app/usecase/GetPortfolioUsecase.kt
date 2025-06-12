package com.app.usecase

import com.app.domain.Portfolio
import com.app.repository.PortfolioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GetPortfolioUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var portfolioRepository: PortfolioRepository

    private lateinit var getPortfolioUseCase: GetPortfolioUseCase

    @Before
    fun setUp() {
        getPortfolioUseCase = GetPortfolioUseCase(portfolioRepository)
    }

    @Test
    fun `invoke should call repository and return its success result`() = runTest {
        // Given
        val mockPortfolioList = listOf(Portfolio("TCS", 100, 3500.0, 3400.0, 3300.0))
        val successResult = Result.success(mockPortfolioList)

        coEvery { portfolioRepository.getPortfolios() } returns successResult

        // When
        val result = getPortfolioUseCase()

        // Then
        coVerify(exactly = 1) { portfolioRepository.getPortfolios() }

        assertTrue(result.isSuccess)
        assertEquals(mockPortfolioList, result.getOrNull())
    }

    @Test
    fun `invoke should call repository and return its failure result`() = runTest {
        // Given
        val exception = RuntimeException("Network Error")
        val failureResult = Result.failure<List<Portfolio>>(exception)

        coEvery { portfolioRepository.getPortfolios() } returns failureResult

        // When
        val result = getPortfolioUseCase()

        // Then
        coVerify(exactly = 1) { portfolioRepository.getPortfolios() }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
