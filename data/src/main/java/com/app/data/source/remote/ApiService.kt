package com.app.data.source.remote

import com.app.data.model.UserHoldingDto

interface ApiService {
    suspend fun getPortfolios(): List<UserHoldingDto>
}