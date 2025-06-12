
package com.app.presentation.viewmodel

interface ViewModelFactoryProvider {
    /**
     * Provides an instance of [PortfolioViewModelFactory].
     */
    fun providePortfolioViewModelFactory(): PortfolioViewModelFactory
}
