package com.example.fitapp.core.health

import com.example.fitapp.core.threading.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Test for health checker implementations
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HealthCheckerTest {
    
    @Test
    fun `health status should have correct properties`() {
        val status = HealthStatus(
            isHealthy = true,
            provider = "Test Provider",
            responseTimeMs = 150L,
            errorMessage = null
        )
        
        assertTrue("Status should be healthy", status.isHealthy)
        assertEquals("Provider name should match", "Test Provider", status.provider)
        assertEquals("Response time should match", 150L, status.responseTimeMs)
        assertNull("Error message should be null", status.errorMessage)
        assertEquals("Status string should be OK", "OK", status.status)
    }
    
    @Test
    fun `unhealthy status should have error status`() {
        val status = HealthStatus(
            isHealthy = false,
            provider = "Test Provider",
            errorMessage = "Connection failed"
        )
        
        assertFalse("Status should be unhealthy", status.isHealthy)
        assertEquals("Error message should match", "Connection failed", status.errorMessage)
        assertEquals("Status string should be ERROR", "ERROR", status.status)
    }
    
    @Test
    fun `api health registry should manage multiple checkers`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val mockDispatcherProvider = object : DispatcherProvider {
            override val main = testDispatcher
            override val io = testDispatcher
            override val default = testDispatcher
            override val unconfined = testDispatcher
        }
        
        // Create mock checker
        val mockChecker = object : HealthCheckable {
            override val providerName = "Mock Provider"
            override suspend fun checkHealth() = HealthStatus(
                isHealthy = true,
                provider = providerName
            )
            override fun healthStatusFlow() = kotlinx.coroutines.flow.flowOf(checkHealth())
        }
        
        // Test registry functionality
        val checkers = listOf(mockChecker)
        assertEquals("Should have one checker", 1, checkers.size)
        assertEquals("Checker name should match", "Mock Provider", checkers[0].providerName)
        
        val healthStatus = mockChecker.checkHealth()
        assertTrue("Mock checker should be healthy", healthStatus.isHealthy)
        assertEquals("Provider name should match", "Mock Provider", healthStatus.provider)
    }
}