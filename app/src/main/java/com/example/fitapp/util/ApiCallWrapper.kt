package com.example.fitapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.delay
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import okhttp3.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.random.Random

/**
 * Comprehensive wrapper for API calls with error handling, retry logic, and network monitoring
 */
object ApiCallWrapper {
    
    private const val TAG = "ApiCallWrapper"
    private const val DEFAULT_TIMEOUT_MS = 30_000L
    private const val MAX_RETRIES = 3
    private const val BASE_DELAY_MS = 1000L
    
    /**
     * Execute an API call with comprehensive error handling and retry logic
     */
    suspend fun <T> executeWithRetry(
        context: Context,
        operation: String = "API Call",
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        maxRetries: Int = MAX_RETRIES,
        apiCall: suspend () -> T
    ): Result<T> {
        if (!isNetworkAvailable(context)) {
            return Result.failure(
                NetworkException("Keine Internetverbindung verfügbar. Bitte prüfen Sie Ihre Netzwerkeinstellungen.")
            )
        }
        
        var lastException: Exception? = null
        
        for (attempt in 0 until maxRetries) {
            try {
                return withTimeout(timeoutMs) {
                    val startTime = System.currentTimeMillis()
                    val result = apiCall()
                    val duration = System.currentTimeMillis() - startTime
                    
                    // Log performance metrics
                    PerformanceMonitor.recordOperationTime("api_call_$operation", duration)
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.API,
                        "ApiCallWrapper",
                        "API call '$operation' completed successfully in ${duration}ms"
                    )
                    
                    Result.success(result)
                }
            } catch (e: Exception) {
                lastException = e
                val errorMessage = when (e) {
                    is TimeoutCancellationException -> "Zeitüberschreitung bei $operation"
                    is SocketTimeoutException -> "Netzwerk-Zeitüberschreitung bei $operation"
                    is UnknownHostException -> "Server nicht erreichbar bei $operation"
                    is NetworkException -> e.message ?: "Netzwerkfehler bei $operation"
                    else -> "Fehler bei $operation: ${e.message}"
                }
                
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.API,
                    "ApiCallWrapper",
                    "Attempt ${attempt + 1} failed for $operation: $errorMessage",
                    exception = e
                )
                
                // Only retry for retriable errors
                if (isRetriableError(e) && attempt < maxRetries - 1) {
                    val delay = calculateBackoffDelay(attempt)
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.API,
                        "ApiCallWrapper",
                        "Retrying $operation in ${delay}ms (attempt ${attempt + 2}/$maxRetries)"
                    )
                    delay(delay)
                } else {
                    break
                }
            }
        }
        
        val finalException = lastException ?: Exception("Unknown error in $operation")
        StructuredLogger.error(
            StructuredLogger.LogCategory.API,
            "ApiCallWrapper",
            "All retries failed for $operation",
            exception = finalException
        )
        
        return Result.failure(finalException)
    }
    
    /**
     * Execute an HTTP call with response validation
     */
    suspend fun executeHttpCall(
        context: Context,
        operation: String = "HTTP Call",
        httpCall: suspend () -> Response
    ): Result<Response> {
        return executeWithRetry(context, operation) {
            val response = httpCall()
            
            if (!response.isSuccessful) {
                val errorBody = try {
                    response.body?.string()?.take(200) ?: "No error body"
                } catch (e: Exception) {
                    "Error reading response body: ${e.message}"
                }
                
                throw ApiException(
                    "HTTP ${response.code}: $errorBody",
                    response.code,
                    errorBody
                )
            }
            
            response
        }
    }
    
    /**
     * Safe response body reading with null checks
     */
    fun safeReadResponseBody(response: Response): Result<String> {
        return try {
            val body = response.body
            if (body == null) {
                Result.failure(ApiException("Response body is null", response.code, ""))
            } else {
                val content = body.string()
                if (content.isBlank()) {
                    Result.failure(ApiException("Response body is empty", response.code, ""))
                } else {
                    Result.success(content)
                }
            }
        } catch (e: Exception) {
            Result.failure(ApiException("Error reading response body: ${e.message}", response.code, ""))
        }
    }
    
    /**
     * Check if network is available
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            StructuredLogger.warning(
                StructuredLogger.LogCategory.SYSTEM,
                "ApiCallWrapper",
                "Error checking network availability",
                exception = e
            )
            false
        }
    }
    
    /**
     * Get network type for monitoring
     */
    fun getNetworkType(context: Context): NetworkType {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            
            when {
                networkCapabilities == null -> NetworkType.NONE
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }
        } catch (e: Exception) {
            NetworkType.UNKNOWN
        }
    }
    
    private fun isRetriableError(exception: Exception): Boolean {
        return when (exception) {
            is TimeoutCancellationException,
            is SocketTimeoutException,
            is UnknownHostException -> true
            is ApiException -> exception.code in 500..599 || exception.code == 429 // Server errors and rate limiting
            else -> {
                val message = exception.message?.lowercase()
                message?.contains("timeout") == true ||
                message?.contains("connection") == true ||
                message?.contains("network") == true
            }
        }
    }
    
    private fun calculateBackoffDelay(attempt: Int): Long {
        val exponentialDelay = BASE_DELAY_MS * (1L shl attempt) // 2^attempt
        val jitter = Random.nextLong(0, BASE_DELAY_MS / 2) // Add jitter
        return (exponentialDelay + jitter).coerceAtMost(30_000) // Max 30 seconds
    }
}

/**
 * Custom exceptions for API handling
 */
class ApiException(
    message: String,
    val code: Int,
    val responseBody: String
) : Exception(message)

class NetworkException(message: String) : Exception(message)

enum class NetworkType {
    NONE, WIFI, CELLULAR, ETHERNET, OTHER, UNKNOWN
}

/**
 * Extension function for safe API calls
 */
suspend fun <T> safeApiCall(
    context: Context,
    operation: String = "API Operation",
    apiCall: suspend () -> T
): Result<T> = ApiCallWrapper.executeWithRetry(context, operation, apiCall = apiCall)