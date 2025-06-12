package com.app.data.repository

import android.util.Log
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
            // Fetch data from the remote API. It returns a List<UserHoldingDto>.
            val remoteData = apiService.getPortfolios()

            // Transform the DTO list into a domain entity list using the mapper.
            val domainData = remoteData.map { it.toDomain() }

            // Save the fresh domain data to the local cache.
            localDataSource.savePortfolios(domainData)

            // Return the successful result with the fresh domain data.
            Result.success(domainData)
        } catch (e: Exception) {
            Log.d("PortfolioRepositoryImpl", "getPortfolios: ${e.message}")
            // If the network call fails, attempt to fetch from the local cache.
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
