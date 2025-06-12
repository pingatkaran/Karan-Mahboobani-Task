package com.app.data.source.remote

import com.app.data.model.UserHoldingDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiServiceImpl : ApiService {

    private val apiUrl = "https://35dee773a9ec441e9f38d5fc249406ce.api.mockbin.io/"

    override suspend fun getPortfolios(): List<UserHoldingDto> = withContext(Dispatchers.IO) {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000 // 15 seconds
        connection.readTimeout = 15000  // 15 seconds

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                parseJsonResponse(response)
            } else {
                throw IOException("API request failed with response code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseJsonResponse(jsonString: String): List<UserHoldingDto> {
        val holdingsList = mutableListOf<UserHoldingDto>()

        val rootObject = JSONObject(jsonString)

        // "data" -> "userHolding".
        val dataObject = rootObject.getJSONObject("data")
        val userHoldingArray = dataObject.getJSONArray("userHolding")

        for (i in 0 until userHoldingArray.length()) {
            val holdingObject = userHoldingArray.getJSONObject(i)

            val holdingDto = UserHoldingDto(
                symbol = holdingObject.getString("symbol"),
                quantity = holdingObject.getInt("quantity"),
                ltp = holdingObject.getDouble("ltp"),
                avgPrice = holdingObject.getDouble("avgPrice"),
                close = holdingObject.getDouble("close")
            )
            holdingsList.add(holdingDto)
        }

        return holdingsList
    }
}