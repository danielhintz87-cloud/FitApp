package com.example.fitapp.core.health

/**
 * Health status data class for API health checks
 */
data class HealthStatus(
    val isHealthy: Boolean,
    val provider: String,
    val responseTimeMs: Long? = null,
    val errorMessage: String? = null,
    val lastChecked: Long = System.currentTimeMillis(),
    val additionalData: Map<String, Any> = emptyMap()
) {
    val status: String get() = if (isHealthy) "OK" else "ERROR"
}