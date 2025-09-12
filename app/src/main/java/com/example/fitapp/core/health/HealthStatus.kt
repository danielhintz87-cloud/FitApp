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
    val additionalData: Map<String, Any> = emptyMap(),
) {
    /**
     * Health status levels
     */
    enum class Status {
        OK,       // Fully operational
        DEGRADED, // Partially operational or slow
        DOWN      // Not operational
    }

    /**
     * Determine status based on health and response time
     */
    val status: Status
        get() = when {
            !isHealthy -> Status.DOWN
            responseTimeMs != null && responseTimeMs > 5000 -> Status.DEGRADED
            else -> Status.OK
        }

    /**
     * Legacy string status for backward compatibility
     */
    val statusString: String get() = if (isHealthy) "OK" else "ERROR"
}
