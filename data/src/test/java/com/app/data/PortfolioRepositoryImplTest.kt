package com.app.data.repository

import com.app.data.model.UserHoldingDto
import com.app.data.source.local.LocalDataSource
import com.app.data.source.remote.ApiService
import com.app.domain.Portfolio
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class PortfolioRepositoryImplTest {

    private lateinit var repository: PortfolioRepositoryImpl
    private val apiService: ApiService = mockk()
    private val localDataSource: LocalDataSource = mockk()

    @Before
    fun setUp() {
        repository = PortfolioRepositoryImpl(apiService, localDataSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getPortfolios - success scenario - returns domain data and saves to cache`() = runTest {
        // Given
        val dto1 = UserHoldingDto(
            symbol = "AAPL",
            quantity = 10,
            ltp = 100.0,
            avgPrice = 80.0,
            close = 105.0
        )
        val dto2 = UserHoldingDto(
            symbol = "GOOGL",
            quantity = 5,
            ltp = 400.0,
            avgPrice = 300.0,
            close = 395.0
        )
        val remoteDtoList = listOf(dto1, dto2)

        val expectedDomainList = listOf(
            Portfolio(
                symbol = "AAPL",
                quantity = 10,
                ltp = 100.0,
                avgPrice = 80.0,
                close = 105.0
            ),
            Portfolio(
                symbol = "GOOGL",
                quantity = 5,
                ltp = 400.0,
                avgPrice = 300.0,
                close = 395.0
            )
        )

        coEvery { apiService.getPortfolios() } returns remoteDtoList
        coEvery { localDataSource.savePortfolios(expectedDomainList) } returns Unit

        // When
        val result = repository.getPortfolios()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedDomainList, result.getOrNull())

        coVerify { apiService.getPortfolios() }
        coVerify { localDataSource.savePortfolios(expectedDomainList) }
    }

    @Test
    fun `getPortfolios - network error with cached data - returns cached data`() = runTest {
        // Given
        val networkException = SocketTimeoutException("Network timeout")
        val cachedData = listOf(
            Portfolio(
                symbol = "AAPL",
                quantity = 10,
                ltp = 100.0,
                avgPrice = 80.0,
                close = 105.0
            )
        )

        coEvery { apiService.getPortfolios() } throws networkException
        coEvery { localDataSource.getPortfolios() } returns cachedData

        // When
        val result = repository.getPortfolios()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedData, result.getOrNull())

        coVerify { apiService.getPortfolios() }
        coVerify { localDataSource.getPortfolios() }
        coVerify(exactly = 0) { localDataSource.savePortfolios(any()) }
    }

    @Test
    fun `getPortfolios - network error with empty cached data - returns failure with custom IOException`() =
        runTest {
            // Given
            val networkException = RuntimeException("Network error")
            val emptyCachedData = emptyList<Portfolio>()

            coEvery { apiService.getPortfolios() } throws networkException
            coEvery { localDataSource.getPortfolios() } returns emptyCachedData

            // When
            val result = repository.getPortfolios()

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is IOException)
            assertEquals("Network error and no cached data available.", exception?.message)
            assertEquals(networkException, exception?.cause)

            coVerify { apiService.getPortfolios() }
            coVerify { localDataSource.getPortfolios() }
            coVerify(exactly = 0) { localDataSource.savePortfolios(any()) }
        }

    @Test
    fun `getPortfolios - network error and cache error - returns cache exception`() = runTest {
        // Given
        val networkException = IOException("Network failed")
        val cacheException = RuntimeException("Cache access failed")

        coEvery { apiService.getPortfolios() } throws networkException
        coEvery { localDataSource.getPortfolios() } throws cacheException

        // When
        val result = repository.getPortfolios()

        // Then
        assertTrue(result.isFailure)
        assertEquals(cacheException, result.exceptionOrNull())

        coVerify { apiService.getPortfolios() }
        coVerify { localDataSource.getPortfolios() }
        coVerify(exactly = 0) { localDataSource.savePortfolios(any()) }
    }

    @Test
    fun `getPortfolios - network success but save to cache fails - returns failure`() = runTest {
        // Given
        val dto = UserHoldingDto(
            symbol = "AAPL",
            quantity = 10,
            ltp = 100.0,
            avgPrice = 80.0,
            close = 105.0
        )
        val remoteDtoList = listOf(dto)
        val expectedDomainList = listOf(
            Portfolio(
                symbol = "AAPL",
                quantity = 10,
                ltp = 100.0,
                avgPrice = 80.0,
                close = 105.0
            )
        )
        val saveException = RuntimeException("Save failed")


        coEvery { apiService.getPortfolios() } returns remoteDtoList
        coEvery { localDataSource.savePortfolios(expectedDomainList) } throws saveException
        coEvery { localDataSource.getPortfolios() } returns emptyList()


        // When
        val result = repository.getPortfolios()

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IOException)
        assertEquals("Network error and no cached data available.", exception?.message)
        assertEquals(saveException, exception?.cause)


        coVerify { apiService.getPortfolios() }
        coVerify { localDataSource.savePortfolios(expectedDomainList) }
        coVerify { localDataSource.getPortfolios() }
    }

    @Test
    fun `getPortfolios - empty remote data - saves empty list and returns success`() = runTest {
        // Given
        val emptyRemoteData = emptyList<UserHoldingDto>()
        val emptyDomainData = emptyList<Portfolio>()

        coEvery { apiService.getPortfolios() } returns emptyRemoteData
        coEvery { localDataSource.savePortfolios(emptyDomainData) } returns Unit

        // When
        val result = repository.getPortfolios()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyDomainData, result.getOrNull())

        coVerify { apiService.getPortfolios() }
        coVerify { localDataSource.savePortfolios(emptyDomainData) }
    }

    @Test
    fun `getPortfolios - mapping transformation works correctly`() = runTest {
        // Given
        val remoteDtoList = listOf(
            UserHoldingDto(
                symbol = "AAPL",
                quantity = 10,
                ltp = 100.0,
                avgPrice = 80.0,
                close = 105.0
            ),
            UserHoldingDto(
                symbol = "GOOGL",
                quantity = 5,
                ltp = 400.0,
                avgPrice = 300.0,
                close = 395.0
            ),
        )

        val expectedDomainList = listOf(
            Portfolio(
                symbol = "AAPL",
                quantity = 10,
                ltp = 100.0,
                avgPrice = 80.0,
                close = 105.0
            ),
            Portfolio(
                symbol = "GOOGL",
                quantity = 5,
                ltp = 400.0,
                avgPrice = 300.0,
                close = 395.0
            )
        )

        coEvery { apiService.getPortfolios() } returns remoteDtoList
        coEvery { localDataSource.savePortfolios(any()) } returns Unit

        // When
        val result = repository.getPortfolios()

        // Then
        assertTrue(result.isSuccess)
        val resultData = result.getOrNull()
        assertEquals(2, resultData?.size)
        assertEquals(expectedDomainList, resultData)
    }
}
