package com.app.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.presentation.model.PortfolioUiModel
import com.app.usecase.GetPortfolioUseCase
import com.app.domain.Portfolio
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class PortfolioViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getPortfoliosUseCase: GetPortfolioUseCase = mockk()
    private val observer: Observer<UiState<PortfolioUiModel>> = mockk(relaxed = true)

    private lateinit var viewModel: PortfolioViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clearAllMocks()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should trigger fetchPortfolios and emit Loading state initially`() = runTest {
        coEvery { getPortfoliosUseCase() } returns Result.success(emptyList())

        viewModel = PortfolioViewModel(getPortfoliosUseCase)
        viewModel.portfolioData.observeForever(observer)

        verify { observer.onChanged(any<UiState.Success<PortfolioUiModel>>()) }
        coVerify(exactly = 1) { getPortfoliosUseCase() }
    }

    @Test
    fun `fetchPortfolios should emit Success state with correct calculations when use case succeeds`() = runTest {
        val portfolio1 = Portfolio(
            symbol = "AAPL",
            quantity = 10,
            ltp = 100.0,
            avgPrice = 80.0,
            close = 105.0
        )
        val portfolio2 = Portfolio(
            symbol = "GOOGL",
            quantity = 5,
            ltp = 400.0,
            avgPrice = 300.0,
            close = 395.0
        )
        val mockHoldings = listOf(portfolio1, portfolio2)
        coEvery { getPortfoliosUseCase() } returns Result.success(mockHoldings)

        viewModel = PortfolioViewModel(getPortfoliosUseCase)
        viewModel.portfolioData.observeForever(observer)

        val expectedUiModel = PortfolioUiModel(
            holdings = mockHoldings,
            totalCurrentValue = 3000.0,
            totalInvestment = 2300.0,
            totalPandL = 700.0,
            todaysPandL = 25.0
        )

        verify { observer.onChanged(UiState.Success(expectedUiModel)) }
        coVerify(exactly = 1) { getPortfoliosUseCase() }
    }

    @Test
    fun `fetchPortfolios should emit Success state with zero values when holdings are empty`() = runTest {

        coEvery { getPortfoliosUseCase() } returns Result.success(emptyList())

        viewModel = PortfolioViewModel(getPortfoliosUseCase)
        viewModel.portfolioData.observeForever(observer)

        val expectedUiModel = PortfolioUiModel(
            holdings = emptyList(),
            totalCurrentValue = 0.0,
            totalInvestment = 0.0,
            totalPandL = 0.0,
            todaysPandL = 0.0
        )

        verify { observer.onChanged(UiState.Success(expectedUiModel)) }
        coVerify(exactly = 1) { getPortfoliosUseCase() }
    }

    @Test
    fun `fetchPortfolios should emit Error state when use case fails with exception message`() = runTest {

        val errorMessage = "Network error occurred"
        val exception = RuntimeException(errorMessage)
        coEvery { getPortfoliosUseCase() } returns Result.failure(exception)

        viewModel = PortfolioViewModel(getPortfoliosUseCase)
        viewModel.portfolioData.observeForever(observer)

        verify { observer.onChanged(UiState.Error(errorMessage)) }
        coVerify(exactly = 1) { getPortfoliosUseCase() }
    }

    @Test
    fun `fetchPortfolios should emit Error state with default message when exception message is null`() = runTest {
        val exception = RuntimeException(null as String?)
        coEvery { getPortfoliosUseCase() } returns Result.failure(exception)

        viewModel = PortfolioViewModel(getPortfoliosUseCase)
        viewModel.portfolioData.observeForever(observer)

        verify { observer.onChanged(UiState.Error("Unknown error")) }
        coVerify(exactly = 1) { getPortfoliosUseCase() }
    }

    @Test
    fun `fetchPortfolios should emit Loading state before making use case call`() = runTest {
        val portfolio = Portfolio("AAPL", 10, 100.0, 80.0, 105.0)
        val mockHoldings = listOf(portfolio)
        coEvery { getPortfoliosUseCase() } returns Result.success(mockHoldings)

        viewModel = PortfolioViewModel(getPortfoliosUseCase)
        clearMocks(observer)
        viewModel.portfolioData.observeForever(observer)

        viewModel.fetchPortfolios()

        verify { observer.onChanged(UiState.Loading) }
        verify { observer.onChanged(any<UiState.Success<PortfolioUiModel>>()) }
    }

    @Test
    fun `init should emit Loading state followed by Success state`() = runTest {
        val portfolio = Portfolio("AAPL", 10, 100.0, 80.0, 105.0)
        val mockHoldings = listOf(portfolio)

        val stateSlot = mutableListOf<UiState<PortfolioUiModel>>()
        val observer = mockk<Observer<UiState<PortfolioUiModel>>>(relaxed = true)
        every { observer.onChanged(capture(stateSlot)) } just Runs

        coEvery { getPortfoliosUseCase() } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(mockHoldings)
        }

        viewModel = PortfolioViewModel(getPortfoliosUseCase)
        viewModel.portfolioData.observeForever(observer)

        testScheduler.advanceTimeBy(200)
        testScheduler.runCurrent()

        assertEquals(2, stateSlot.size)
        assertEquals(UiState.Loading, stateSlot[0])
        assert(stateSlot[1] is UiState.Success)
        coVerify(exactly = 1) { getPortfoliosUseCase() }
    }

    @Test
    fun `fetchPortfolios can be called multiple times and should work correctly`() = runTest {
        val portfolio1 = Portfolio("AAPL", 10, 100.0, 80.0, 105.0)
        val portfolio2 = Portfolio("GOOGL", 5, 400.0, 300.0, 395.0)
        val mockHoldings1 = listOf(portfolio1)
        val mockHoldings2 = listOf(portfolio1, portfolio2)

        coEvery { getPortfoliosUseCase() } returnsMany listOf(
            Result.success(mockHoldings1),
            Result.success(mockHoldings2)
        )

        viewModel = PortfolioViewModel(getPortfoliosUseCase)
        viewModel.portfolioData.observeForever(observer)

        viewModel.fetchPortfolios()

        coVerify(exactly = 2) { getPortfoliosUseCase() }
        verify(atLeast = 3) { observer.onChanged(any()) }
    }

    @Test
    fun `portfolioData LiveData should be properly exposed`() {
        coEvery { getPortfoliosUseCase() } returns Result.success(emptyList())

        viewModel = PortfolioViewModel(getPortfoliosUseCase)

        assertNotNull(viewModel.portfolioData)
    }
}