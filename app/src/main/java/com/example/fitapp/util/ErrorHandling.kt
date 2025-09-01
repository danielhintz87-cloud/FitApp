package com.example.fitapp.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.random.Random

/**
 * Comprehensive error handling utilities for FitApp
 * Provides consistent error handling, logging, and user-friendly messages
 */
object ErrorHandling {
    
    const val TAG = "FitApp_ErrorHandling"
    
    /**
     * Safe execution wrapper that catches all exceptions and provides user-friendly error messages
     */
    inline fun <T> safeCall(
        context: String = "Operation",
        operation: () -> T
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: CancellationException) {
            // Don't catch cancellation exceptions - these are intentional
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in $context", e)
            Result.failure(e)
        }
    }
    
    /**
     * Safe execution with retry logic for network operations
     */
    suspend fun <T> safeCallWithRetry(
        context: String = "Operation",
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        operation: suspend () -> T
    ): Result<T> {
        var lastException: Exception? = null
        
        for (attempt in 0 until maxRetries) {
            try {
                return Result.success(operation())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Attempt ${attempt + 1} failed for $context: ${e.message}")
                
                if (isRetriableError(e) && attempt < maxRetries - 1) {
                    val delay = calculateBackoffDelay(attempt, initialDelayMs)
                    Log.i(TAG, "Retrying $context in ${delay}ms")
                    delay(delay)
                }
            }
        }
        
        Log.e(TAG, "All retries failed for $context", lastException)
        return Result.failure(lastException ?: Exception("Max retries exceeded"))
    }
    
    /**
     * Convert exception to user-friendly error message
     */
    fun getUserFriendlyMessage(exception: Throwable): String {
        return when (exception) {
            is TimeoutCancellationException -> 
                "❌ Zeitüberschreitung. Bitte versuchen Sie es erneut."
            is SocketTimeoutException -> 
                "❌ Netzwerk-Zeitüberschreitung. Prüfen Sie Ihre Internetverbindung."
            is UnknownHostException -> 
                "❌ Keine Internetverbindung verfügbar."
            is IllegalArgumentException -> 
                "❌ Ungültige Eingabe: ${exception.message}"
            is SecurityException -> 
                "❌ Berechtigung fehlt: ${exception.message}"
            else -> {
                val message = exception.message
                when {
                    message?.contains("API", ignoreCase = true) == true -> 
                        "❌ API-Fehler. Prüfen Sie Ihre API-Schlüssel."
                    message?.contains("network", ignoreCase = true) == true -> 
                        "❌ Netzwerkfehler. Prüfen Sie Ihre Internetverbindung."
                    message?.contains("database", ignoreCase = true) == true -> 
                        "❌ Datenbankfehler. Versuchen Sie einen App-Neustart."
                    message?.contains("permission", ignoreCase = true) == true -> 
                        "❌ Berechtigung fehlt. Prüfen Sie die App-Einstellungen."
                    message.isNullOrBlank() -> 
                        "❌ Ein unerwarteter Fehler ist aufgetreten."
                    else -> 
                        "❌ Fehler: ${message.take(100)}"
                }
            }
        }
    }
    
    /**
     * Safe nullable property access with fallback
     */
    inline fun <T, R> T?.safeAccess(fallback: R, block: (T) -> R): R {
        return try {
            this?.let(block) ?: fallback
        } catch (e: Exception) {
            Log.w(TAG, "Safe access failed, using fallback", e)
            fallback
        }
    }
    
    /**
     * Safe string parsing with fallback
     */
    fun String?.toIntSafe(fallback: Int = 0): Int {
        return try {
            this?.toIntOrNull() ?: fallback
        } catch (e: Exception) {
            Log.w(TAG, "String to int conversion failed: '$this'", e)
            fallback
        }
    }
    
    /**
     * Safe string parsing with fallback
     */
    fun String?.toDoubleSafe(fallback: Double = 0.0): Double {
        return try {
            this?.toDoubleOrNull() ?: fallback
        } catch (e: Exception) {
            Log.w(TAG, "String to double conversion failed: '$this'", e)
            fallback
        }
    }
    
    /**
     * Validate and sanitize user input
     */
    fun sanitizeInput(input: String?, maxLength: Int = 1000): String {
        return try {
            input?.trim()
                ?.take(maxLength)
                ?.replace(Regex("[\\p{Cntrl}&&[^\r\n\t]]"), "") // Remove control characters except newlines and tabs
                ?: ""
        } catch (e: Exception) {
            Log.w(TAG, "Input sanitization failed", e)
            ""
        }
    }
    
    /**
     * Log user action for debugging
     */
    fun logUserAction(action: String, details: Map<String, Any> = emptyMap()) {
        try {
            val detailsStr = details.entries.joinToString(", ") { "${it.key}=${it.value}" }
            Log.i(TAG, "User Action: $action${if (detailsStr.isNotEmpty()) " [$detailsStr]" else ""}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log user action", e)
        }
    }
    
    /**
     * Memory-safe list operations
     */
    fun <T> List<T>?.safeForEach(action: (T) -> Unit) {
        try {
            this?.forEach(action)
        } catch (e: Exception) {
            Log.w(TAG, "Safe forEach failed", e)
        }
    }
    
    /**
     * Memory-safe collection operations
     */
    fun <T> Collection<T>?.safeSize(): Int {
        return try {
            this?.size ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Safe size check failed", e)
            0
        }
    }
    
    // Private helper methods
    
    private fun isRetriableError(exception: Exception): Boolean {
        return when (exception) {
            is SocketTimeoutException,
            is UnknownHostException,
            is TimeoutCancellationException -> true
            else -> {
                val message = exception.message?.lowercase(java.util.Locale.ROOT)
                message?.contains("timeout") == true ||
                message?.contains("connection") == true ||
                message?.contains("network") == true
            }
        }
    }
    
    private fun calculateBackoffDelay(attempt: Int, baseDelay: Long): Long {
        val exponentialDelay = baseDelay * (1L shl attempt) // 2^attempt
        val jitter = Random.nextLong(0, baseDelay / 2) // Add jitter to prevent thundering herd
        return (exponentialDelay + jitter).coerceAtMost(30_000) // Max 30 seconds
    }
}

/**
 * Extension function for safe coroutine execution
 */
suspend fun <T> runSafely(
    context: String = "Operation",
    block: suspend () -> T
): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Log.e(ErrorHandling.TAG, "Error in $context", e)
    Result.failure(e)
}

/**
 * Extension function for safe coroutine execution with retry
 */
suspend fun <T> runSafelyWithRetry(
    context: String = "Operation",
    maxRetries: Int = 3,
    block: suspend () -> T
): Result<T> = ErrorHandling.safeCallWithRetry(context, maxRetries, 1000, block)