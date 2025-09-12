package com.example.fitapp.infrastructure.providers.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit service interface for Gemini AI API
 */
interface GeminiApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Body body: RequestBody,
    ): Response<ResponseBody>
}
