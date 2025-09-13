package com.example.fitapp.core.health

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Simple verification test for health status functionality
 * This validates the core logic without dependencies
 */
class HealthStatusLogicTest {

    @Test
    fun `health status enum logic works correctly`() {
        // Test OK status
        val healthyStatus = HealthStatus(
            isHealthy = true,
            provider = "Test Provider",
            responseTimeMs = 100L
        )
        assertEquals(HealthStatus.Status.OK, healthyStatus.status)
        assertEquals("OK", healthyStatus.statusString)

        // Test DOWN status
        val downStatus = HealthStatus(
            isHealthy = false,
            provider = "Test Provider",
            errorMessage = "Connection failed"
        )
        assertEquals(HealthStatus.Status.DOWN, downStatus.status)
        assertEquals("ERROR", downStatus.statusString)

        // Test DEGRADED status (slow response)
        val degradedStatus = HealthStatus(
            isHealthy = true,
            provider = "Test Provider",
            responseTimeMs = 6000L // > 5 seconds
        )
        assertEquals(HealthStatus.Status.DEGRADED, degradedStatus.status)
        assertEquals("OK", degradedStatus.statusString) // Legacy compatibility
    }

    @Test
    fun `health summary calculation works correctly`() {
        val summary = HealthSummary(
            totalProviders = 4,
            healthyCount = 2,
            degradedCount = 1,
            downCount = 1,
            lastUpdated = System.currentTimeMillis()
        )

        assertEquals(0.5f, summary.healthPercentage, 0.01f)
        assertEquals(HealthStatus.Status.DOWN, summary.overallStatus) // Has down providers
        
        // Test degraded overall status
        val degradedSummary = HealthSummary(
            totalProviders = 3,
            healthyCount = 2,
            degradedCount = 1,
            downCount = 0,
            lastUpdated = System.currentTimeMillis()
        )
        assertEquals(HealthStatus.Status.DEGRADED, degradedSummary.overallStatus)
        
        // Test healthy overall status
        val healthySummary = HealthSummary(
            totalProviders = 2,
            healthyCount = 2,
            degradedCount = 0,
            downCount = 0,
            lastUpdated = System.currentTimeMillis()
        )
        assertEquals(HealthStatus.Status.OK, healthySummary.overallStatus)
    }

    @Test
    fun `entity conversion works correctly`() {
        val originalStatus = HealthStatus(
            isHealthy = true,
            provider = "Test Provider",
            responseTimeMs = 150L,
            errorMessage = null,
            lastChecked = 1234567890L
        )

        // Convert to entity and back
        val entity = com.example.fitapp.data.db.HealthStatusEntity.fromHealthStatus(originalStatus)
        val convertedStatus = entity.toHealthStatus()

        assertEquals(originalStatus.isHealthy, convertedStatus.isHealthy)
        assertEquals(originalStatus.provider, convertedStatus.provider)
        assertEquals(originalStatus.responseTimeMs, convertedStatus.responseTimeMs)
        assertEquals(originalStatus.errorMessage, convertedStatus.errorMessage)
        assertEquals(originalStatus.lastChecked, convertedStatus.lastChecked)
    }

    @Test
    fun `health checkable interface contract validation`() {
        // Create a mock implementation to verify interface design
        val mockChecker = object : HealthCheckable {
            override val providerName: String = "Mock Provider"
            
            override suspend fun checkHealth(): HealthStatus {
                return HealthStatus(
                    isHealthy = true,
                    provider = providerName,
                    responseTimeMs = 100L
                )
            }
            
            override fun healthStatusFlow() = kotlinx.coroutines.flow.flowOf(
                HealthStatus(isHealthy = true, provider = providerName)
            )
        }

        // Test the contract
        assertEquals("Mock Provider", mockChecker.providerName)
        
        runBlocking {
            val status = mockChecker.checkHealth()
            assertEquals("Mock Provider", status.provider)
            assertTrue(status.isHealthy)
            assertEquals(HealthStatus.Status.OK, status.status)
        }
    }
}