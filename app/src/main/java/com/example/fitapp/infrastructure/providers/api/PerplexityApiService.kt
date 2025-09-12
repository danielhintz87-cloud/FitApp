package com.example.fitapp.infrastructure.providers.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit service interface for Perplexity AI API
 */
interface PerplexityApiService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String = "fitapp/1.0",
        @Body body: RequestBody,
    ): Response<ResponseBody>
}
