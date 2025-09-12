package com.example.fitapp.core.health

import com.example.fitapp.ai.executeSuspending
import com.example.fitapp.core.threading.DispatcherProvider
import com.example.fitapp.data.prefs.ApiKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

/**
 * Health checker for Gemini AI provider
 */
class GeminiHealthChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
    private val dispatchers: DispatcherProvider
) : HealthCheckable {
    
    override val providerName: String = "Gemini AI"
    
    override suspend fun checkHealth(): HealthStatus = withContext(dispatchers.io) {
        val startTime = System.currentTimeMillis()
        try {
            val apiKey = ApiKeys.getGeminiKey(context)
            if (apiKey.isBlank()) {
                return@withContext HealthStatus(
                    isHealthy = false,
                    provider = providerName,
                    errorMessage = "API key not configured",
                    responseTimeMs = System.currentTimeMillis() - startTime
                )
            }
            
            // Simple health check with timeout
            val result = withTimeoutOrNull(10_000) {
                // Basic API endpoint check (models list is a lightweight endpoint)
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
                    .get()
                    .build()
                
                httpClient.newCall(request).executeSuspending().use { response ->
                    response.isSuccessful
                }
            }
            
            val responseTime = System.currentTimeMillis() - startTime
            
            if (result == true) {
                HealthStatus(
                    isHealthy = true,
                    provider = providerName,
                    responseTimeMs = responseTime
                )
            } else {
                HealthStatus(
                    isHealthy = false,
                    provider = providerName,
                    errorMessage = "Health check failed or timed out",
                    responseTimeMs = responseTime
                )
            }
        } catch (e: Exception) {
            HealthStatus(
                isHealthy = false,
                provider = providerName,
                errorMessage = e.message ?: "Unknown error",
                responseTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }
    
    override fun healthStatusFlow(): Flow<HealthStatus> = flow {
        emit(checkHealth())
    }.flowOn(dispatchers.io)
}