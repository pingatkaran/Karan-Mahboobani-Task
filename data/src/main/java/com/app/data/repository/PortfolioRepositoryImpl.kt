package com.app.data.repository

import com.app.data.model.toDomain
import com.app.data.source.local.LocalDataSource
import com.app.data.source.remote.ApiService
import com.app.domain.Portfolio
import com.app.repository.PortfolioRepository
import java.io.IOException

class PortfolioRepositoryImpl(
    private val apiService: ApiService,
    private val localDataSource: LocalDataSource
) : PortfolioRepository {

    override suspend fun getPortfolios(): Result<List<Portfolio>> {
        return try {
            val remoteData = apiService.getPortfolios()
            val domainData = remoteData.map { it.toDomain() }
            localDataSource.savePortfolios(domainData)
            Result.success(domainData)
        } catch (e: Exception) {
            try {
                val localData = localDataSource.getPortfolios()
                if (localData.isNotEmpty()) {
                    Result.success(localData)
                } else {
                    Result.failure(IOException("Network error and no cached data available.", e))
                }
            } catch (cacheException: Exception) {
                Result.failure(cacheException)
            }
        }
    }
}
