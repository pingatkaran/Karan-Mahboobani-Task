package com.app.stocksportfolio

import android.app.Application
import com.app.data.repository.PortfolioRepositoryImpl
import com.app.data.source.local.DbHelper
import com.app.data.source.local.LocalDataSourceImpl
import com.app.data.source.remote.ApiServiceImpl
import com.app.presentation.viewmodel.PortfolioViewModelFactory
import com.app.presentation.viewmodel.ViewModelFactoryProvider
import com.app.repository.PortfolioRepository
import com.app.usecase.GetPortfolioUseCase

class MyApplication : Application(), ViewModelFactoryProvider {

    private val portfolioApiService by lazy { ApiServiceImpl() }
    private val dbHelper by lazy { DbHelper(this) }
    private val localDataSource by lazy { LocalDataSourceImpl(dbHelper) }

    private val portfolioRepository: PortfolioRepository by lazy {
        PortfolioRepositoryImpl(portfolioApiService, localDataSource)
    }
    private val getPortfoliosUseCase: GetPortfolioUseCase by lazy {
        GetPortfolioUseCase(portfolioRepository)
    }

    private val portfolioViewModelFactory: PortfolioViewModelFactory by lazy {
        PortfolioViewModelFactory(getPortfoliosUseCase)
    }

    override fun providePortfolioViewModelFactory(): PortfolioViewModelFactory {
        return portfolioViewModelFactory
    }
}
