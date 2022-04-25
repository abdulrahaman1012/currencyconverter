package api

import model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import utils.CONVERT_URL

interface ApiService {

    @GET(CONVERT_URL)
    suspend fun convertCurrency(
        @Query("api_key") access_key: String,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("amount") amount: Double
    ): ApiResponse
}