package com.example.fitapp.core.health

import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.HealthStatusEntity
import com.example.fitapp.core.threading.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing health status data
 * Bridges domain models and data layer persistence
 */
@Singleton
class HealthStatusRepository @Inject constructor(
    private val database: AppDatabase,
    private val dispatchers: DispatcherProvider
) {

    private val healthStatusDao = database.healthStatusDao()

    /**
     * Save health status to local cache
     */
    suspend fun saveHealthStatus(status: HealthStatus) = withContext(dispatchers.io) {
        val entity = HealthStatusEntity.fromHealthStatus(status)
        healthStatusDao.upsertHealthStatus(entity)
    }

    /**
     * Save multiple health statuses
     */
    suspend fun saveHealthStatuses(statuses: List<HealthStatus>) = withContext(dispatchers.io) {
        statuses.forEach { status ->
            val entity = HealthStatusEntity.fromHealthStatus(status)
            healthStatusDao.upsertHealthStatus(entity)
        }
    }

    /**
     * Get health status for a specific provider
     */
    suspend fun getHealthStatus(provider: String): HealthStatus? = withContext(dispatchers.io) {
        healthStatusDao.getHealthStatus(provider)?.toHealthStatus()
    }

    /**
     * Flow of health status for a specific provider
     */
    fun getHealthStatusFlow(provider: String): Flow<HealthStatus?> {
        return healthStatusDao.getHealthStatusFlow(provider)
            .map { it?.toHealthStatus() }
    }

    /**
     * Get all health statuses
     */
    suspend fun getAllHealthStatuses(): List<HealthStatus> = withContext(dispatchers.io) {
        healthStatusDao.getAllHealthStatus().map { it.toHealthStatus() }
    }

    /**
     * Flow of all health statuses
     */
    fun getAllHealthStatusesFlow(): Flow<List<HealthStatus>> {
        return healthStatusDao.getAllHealthStatusFlow()
            .map { entities -> entities.map { it.toHealthStatus() } }
    }

    /**
     * Get count of healthy providers
     */
    suspend fun getHealthyCount(): Int = withContext(dispatchers.io) {
        healthStatusDao.getHealthyCount()
    }

    /**
     * Get count of unhealthy providers
     */
    suspend fun getUnhealthyCount(): Int = withContext(dispatchers.io) {
        healthStatusDao.getUnhealthyCount()
    }

    /**
     * Delete health status for a specific provider
     */
    suspend fun deleteHealthStatus(provider: String) = withContext(dispatchers.io) {
        healthStatusDao.deleteHealthStatus(provider)
    }

    /**
     * Clear all health status data
     */
    suspend fun clearAllHealthStatus() = withContext(dispatchers.io) {
        healthStatusDao.deleteAllHealthStatus()
    }

    /**
     * Get aggregated health summary
     */
    suspend fun getHealthSummary(): HealthSummary = withContext(dispatchers.io) {
        val allStatuses = getAllHealthStatuses()
        val totalCount = allStatuses.size
        val healthyCount = allStatuses.count { it.isHealthy }
        val degradedCount = allStatuses.count { it.status == HealthStatus.Status.DEGRADED }
        val downCount = allStatuses.count { it.status == HealthStatus.Status.DOWN }

        HealthSummary(
            totalProviders = totalCount,
            healthyCount = healthyCount,
            degradedCount = degradedCount,
            downCount = downCount,
            lastUpdated = allStatuses.maxOfOrNull { it.lastChecked } ?: 0L
        )
    }

    /**
     * Flow of aggregated health summary
     */
    fun getHealthSummaryFlow(): Flow<HealthSummary> {
        return getAllHealthStatusesFlow()
            .map { statuses ->
                val totalCount = statuses.size
                val healthyCount = statuses.count { it.isHealthy }
                val degradedCount = statuses.count { it.status == HealthStatus.Status.DEGRADED }
                val downCount = statuses.count { it.status == HealthStatus.Status.DOWN }

                HealthSummary(
                    totalProviders = totalCount,
                    healthyCount = healthyCount,
                    degradedCount = degradedCount,
                    downCount = downCount,
                    lastUpdated = statuses.maxOfOrNull { it.lastChecked } ?: 0L
                )
            }
    }
}

/**
 * Summary of overall health status
 */
data class HealthSummary(
    val totalProviders: Int,
    val healthyCount: Int,
    val degradedCount: Int,
    val downCount: Int,
    val lastUpdated: Long
) {
    val healthPercentage: Float
        get() = if (totalProviders > 0) healthyCount.toFloat() / totalProviders else 0f
    
    val overallStatus: HealthStatus.Status
        get() = when {
            downCount > 0 -> HealthStatus.Status.DOWN
            degradedCount > 0 -> HealthStatus.Status.DEGRADED
            else -> HealthStatus.Status.OK
        }
}