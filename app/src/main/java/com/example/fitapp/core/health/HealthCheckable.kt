package com.example.fitapp.core.health

import kotlinx.coroutines.flow.Flow

/**
 * Interface for health checkable components
 */
interface HealthCheckable {
    /**
     * Provider name for identification
     */
    val providerName: String

    /**
     * Perform a health check
     * @return HealthStatus with the result
     */
    suspend fun checkHealth(): HealthStatus

    /**
     * Continuous health monitoring as a Flow
     * @return Flow of HealthStatus updates
     */
    fun healthStatusFlow(): Flow<HealthStatus>
}
