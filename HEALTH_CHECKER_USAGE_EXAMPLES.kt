package com.example.fitapp.example

import kotlinx.coroutines.runBlocking
import com.example.fitapp.core.health.*
import com.example.fitapp.services.HealthCheckWorker

/**
 * Example usage of the API Health Checker system
 * This demonstrates how to integrate and use the health checking components
 */
class HealthCheckerUsageExample {

    /**
     * Example: Schedule periodic health checks in Application onCreate
     */
    fun scheduleHealthChecks(context: android.content.Context) {
        // Schedule periodic health checks (runs every hour)
        HealthCheckWorker.schedulePeriodicHealthCheck(context)
        
        println("✅ Periodic health checks scheduled")
    }

    /**
     * Example: Trigger immediate health check (e.g., from settings screen)
     */
    fun performManualHealthCheck(context: android.content.Context) {
        HealthCheckWorker.triggerImmediateHealthCheck(context)
        
        println("🔄 Manual health check triggered")
    }

    /**
     * Example: Monitor health status in a ViewModel or Service
     */
    fun monitorHealthStatus() = runBlocking {
        // Mock repository for demonstration
        val mockRepository = createMockRepository()
        
        // Get current health summary
        val summary = mockRepository.getHealthSummary()
        
        println("📊 Health Summary:")
        println("   Total Providers: ${summary.totalProviders}")
        println("   Healthy: ${summary.healthyCount}")
        println("   Degraded: ${summary.degradedCount}")
        println("   Down: ${summary.downCount}")
        println("   Overall Status: ${summary.overallStatus}")
        println("   Health Percentage: ${(summary.healthPercentage * 100).toInt()}%")
        
        // Monitor real-time changes
        mockRepository.getHealthSummaryFlow().collect { updatedSummary ->
            if (updatedSummary.overallStatus == HealthStatus.Status.DOWN) {
                println("🚨 ALERT: One or more providers are down!")
            }
        }
    }

    /**
     * Example: Create custom health checker for new provider
     */
    class CustomApiHealthChecker(
        private val apiUrl: String
    ) : HealthCheckable {
        
        override val providerName: String = "Custom API"
        
        override suspend fun checkHealth(): HealthStatus {
            return try {
                val startTime = System.currentTimeMillis()
                
                // Perform actual health check (simplified)
                val isHealthy = performApiCall(apiUrl)
                val responseTime = System.currentTimeMillis() - startTime
                
                HealthStatus(
                    isHealthy = isHealthy,
                    provider = providerName,
                    responseTimeMs = responseTime,
                    lastChecked = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                HealthStatus(
                    isHealthy = false,
                    provider = providerName,
                    errorMessage = e.message ?: "Unknown error",
                    lastChecked = System.currentTimeMillis()
                )
            }
        }
        
        override fun healthStatusFlow() = kotlinx.coroutines.flow.flow {
            emit(checkHealth())
        }
        
        private suspend fun performApiCall(url: String): Boolean {
            // Simulate API call
            kotlinx.coroutines.delay(100)
            return true // Simulate successful response
        }
    }

    /**
     * Example: Health status interpretation
     */
    fun interpretHealthStatus(status: HealthStatus) {
        when (status.status) {
            HealthStatus.Status.OK -> {
                println("✅ ${status.provider} is healthy (${status.responseTimeMs}ms)")
            }
            HealthStatus.Status.DEGRADED -> {
                println("⚠️ ${status.provider} is degraded - slow response (${status.responseTimeMs}ms)")
            }
            HealthStatus.Status.DOWN -> {
                println("❌ ${status.provider} is down: ${status.errorMessage}")
            }
        }
    }

    /**
     * Example: Integration with notification system
     */
    fun handleHealthAlerts(summary: HealthSummary) {
        when (summary.overallStatus) {
            HealthStatus.Status.OK -> {
                // All systems operational
                clearHealthAlerts()
            }
            HealthStatus.Status.DEGRADED -> {
                // Some systems slow
                showWarningNotification("Some services are experiencing delays")
            }
            HealthStatus.Status.DOWN -> {
                // Critical systems down
                showCriticalNotification("Critical services are unavailable")
            }
        }
    }

    /**
     * Example: Dashboard data preparation
     */
    fun prepareDashboardData(statuses: List<HealthStatus>): DashboardData {
        val groupedByStatus = statuses.groupBy { it.status }
        
        return DashboardData(
            totalProviders = statuses.size,
            healthyProviders = groupedByStatus[HealthStatus.Status.OK] ?: emptyList(),
            degradedProviders = groupedByStatus[HealthStatus.Status.DEGRADED] ?: emptyList(),
            downProviders = groupedByStatus[HealthStatus.Status.DOWN] ?: emptyList(),
            averageResponseTime = statuses.mapNotNull { it.responseTimeMs }.average().toInt(),
            lastUpdated = statuses.maxOfOrNull { it.lastChecked } ?: 0L
        )
    }

    // Helper methods for demonstration
    private fun createMockRepository(): HealthStatusRepository {
        // This would normally be injected
        throw NotImplementedError("Mock repository for demonstration only")
    }
    
    private fun clearHealthAlerts() { /* Clear any existing alerts */ }
    private fun showWarningNotification(message: String) { /* Show warning notification */ }
    private fun showCriticalNotification(message: String) { /* Show critical notification */ }
}

/**
 * Data class for dashboard display
 */
data class DashboardData(
    val totalProviders: Int,
    val healthyProviders: List<HealthStatus>,
    val degradedProviders: List<HealthStatus>,
    val downProviders: List<HealthStatus>,
    val averageResponseTime: Int,
    val lastUpdated: Long
)