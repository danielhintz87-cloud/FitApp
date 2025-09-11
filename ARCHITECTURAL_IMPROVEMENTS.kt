// ARCHITECTURAL IMPROVEMENT SUGGESTIONS FOR FUTURE DEVELOPMENT
// This file demonstrates recommended patterns for network architecture

package com.example.fitapp.network.architecture

import android.content.Context
import com.example.fitapp.core.threading.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Unified Network Module - Demonstrates recommended approach for consistent network configuration
 */
@Module
@InstallIn(SingletonComponent::class)
object UnifiedNetworkModule {
    
    @Provides
    @Singleton
    @BaseOkHttpClient
    fun provideBaseOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(createLoggingInterceptor())
            .addInterceptor(createRetryInterceptor())
            .addInterceptor(createUserAgentInterceptor())
            .certificatePinner(createCertificatePinner())
            .build()
    }
    
    @Provides
    @Singleton
    @GeminiClient
    fun provideGeminiOkHttpClient(
        @BaseOkHttpClient baseClient: OkHttpClient
    ): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor(createGeminiAuthInterceptor())
            .build()
    }
    
    @Provides
    @Singleton
    @PerplexityClient  
    fun providePerplexityOkHttpClient(
        @BaseOkHttpClient baseClient: OkHttpClient
    ): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor(createPerplexityAuthInterceptor())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideGeminiApi(
        @GeminiClient okHttpClient: OkHttpClient
    ): GeminiApi {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
    
    @Provides
    @Singleton
    fun providePerplexityApi(
        @PerplexityClient okHttpClient: OkHttpClient
    ): PerplexityApi {
        return Retrofit.Builder()
            .baseUrl("https://api.perplexity.ai/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PerplexityApi::class.java)
    }
    
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }
    
    private fun createRetryInterceptor(): Interceptor {
        return Interceptor { chain ->
            var response = chain.proceed(chain.request())
            var retryCount = 0
            val maxRetries = 3
            
            while (!response.isSuccessful && retryCount < maxRetries) {
                if (response.code in listOf(429, 502, 503, 504)) {
                    retryCount++
                    response.close()
                    
                    // Exponential backoff
                    Thread.sleep((1000L * retryCount * retryCount))
                    response = chain.proceed(chain.request())
                } else {
                    break
                }
            }
            response
        }
    }
    
    private fun createUserAgentInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "FitApp/1.0 (Android)")
                .build()
            chain.proceed(request)
        }
    }
    
    private fun createCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("generativelanguage.googleapis.com", "sha256/...")
            .add("api.perplexity.ai", "sha256/...")
            .build()
    }
    
    private fun createGeminiAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val url = original.url.newBuilder()
                .addQueryParameter("key", BuildConfig.GEMINI_API_KEY)
                .build()
            
            val request = original.newBuilder()
                .url(url)
                .build()
            
            chain.proceed(request)
        }
    }
    
    private fun createPerplexityAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.PERPLEXITY_API_KEY}")
                .build()
            chain.proceed(request)
        }
    }
}

// Qualifier annotations for different HTTP clients
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiClient

@Qualifier  
@Retention(AnnotationRetention.BINARY)
annotation class PerplexityClient

/**
 * Retrofit API interfaces - Demonstrates type-safe network calls
 */
interface GeminiApi {
    
    @POST("/v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Body request: GeminiRequest
    ): GeminiResponse
    
    @POST("/v1beta/models/{model}:generateContent")
    suspend fun generateContentWithImage(
        @Path("model") model: String,
        @Body request: GeminiMultimodalRequest
    ): GeminiResponse
}

interface PerplexityApi {
    
    @POST("/chat/completions")
    suspend fun chatCompletions(
        @Body request: PerplexityRequest
    ): PerplexityResponse
}

/**
 * Improved AI Provider using Retrofit - Demonstrates clean architecture
 */
class ImprovedGeminiAiProvider @Inject constructor(
    private val geminiApi: GeminiApi,
    private val dispatchers: DispatcherProvider,
    private val circuitBreaker: CircuitBreaker
) : AiProvider {
    
    override suspend fun generateText(prompt: String, taskType: TaskType): String = 
        withContext(dispatchers.io) {
            circuitBreaker.execute {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.4,
                        maxOutputTokens = 4096
                    )
                )
                
                val response = geminiApi.generateContent(
                    model = selectModelForTask(taskType),
                    request = request
                )
                
                extractTextFromResponse(response)
            }
        }
    
    private fun selectModelForTask(taskType: TaskType): String {
        return when (taskType) {
            TaskType.CALORIE_ESTIMATION -> "gemini-pro-vision"
            TaskType.GENERAL -> "gemini-pro"
            else -> "gemini-pro"
        }
    }
    
    private fun extractTextFromResponse(response: GeminiResponse): String {
        return response.candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()
            ?.text ?: throw IllegalStateException("No response from Gemini")
    }
}

/**
 * Circuit Breaker Implementation - Demonstrates resilience patterns
 */
interface CircuitBreaker {
    suspend fun <T> execute(operation: suspend () -> T): T
}

class SimpleCircuitBreaker @Inject constructor() : CircuitBreaker {
    
    private var failureCount = 0
    private var lastFailureTime = 0L
    private val threshold = 5
    private val timeoutMs = 60_000L
    
    override suspend fun <T> execute(operation: suspend () -> T): T {
        if (failureCount >= threshold) {
            if (System.currentTimeMillis() - lastFailureTime < timeoutMs) {
                throw CircuitOpenException("Circuit breaker is open")
            } else {
                // Reset circuit breaker
                failureCount = 0
            }
        }
        
        return try {
            val result = operation()
            // Success - reset failure count
            failureCount = 0
            result
        } catch (e: Exception) {
            failureCount++
            lastFailureTime = System.currentTimeMillis()
            throw e
        }
    }
}

class CircuitOpenException(message: String) : Exception(message)

/**
 * Data classes for type-safe API requests/responses
 */
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    val mimeType: String,
    val data: String // Base64 encoded
)

data class GeminiGenerationConfig(
    val temperature: Double,
    val maxOutputTokens: Int
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

data class GeminiCandidate(
    val content: GeminiContent
)

data class GeminiMultimodalRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

data class PerplexityRequest(
    val model: String,
    val messages: List<PerplexityMessage>,
    val temperature: Double,
    val maxTokens: Int
)

data class PerplexityMessage(
    val role: String,
    val content: String
)

data class PerplexityResponse(
    val choices: List<PerplexityChoice>
)

data class PerplexityChoice(
    val message: PerplexityMessage
)

/**
 * Network monitoring and metrics collection
 */
@Singleton
class NetworkMetrics @Inject constructor() {
    
    fun recordApiCall(provider: String, endpoint: String, duration: Long, success: Boolean) {
        // Implementation for metrics collection
        // Could integrate with Firebase Analytics, custom analytics, etc.
    }
    
    fun recordError(provider: String, errorType: String, statusCode: Int?) {
        // Implementation for error tracking
    }
}