package com.example.fitapp.core.health

import com.example.fitapp.core.threading.DispatcherProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.HealthStatusDao
import com.example.fitapp.data.db.HealthStatusEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for HealthStatusRepository
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HealthStatusRepositoryTest {

    private lateinit var repository: HealthStatusRepository
    private lateinit var mockDatabase: AppDatabase
    private lateinit var mockDao: HealthStatusDao
    private lateinit var testDispatcher: StandardTestDispatcher
    private lateinit var mockDispatcherProvider: DispatcherProvider

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        mockDispatcherProvider = object : DispatcherProvider {
            override val main = testDispatcher
            override val io = testDispatcher
            override val default = testDispatcher
            override val unconfined = testDispatcher
        }
        
        mockDatabase = mockk()
        mockDao = mockk()
        
        coEvery { mockDatabase.healthStatusDao() } returns mockDao
        
        repository = HealthStatusRepository(mockDatabase, mockDispatcherProvider)
    }

    @Test
    fun `saveHealthStatus should call DAO upsert`() = runTest(testDispatcher) {
        // Given
        val healthStatus = HealthStatus(
            isHealthy = true,
            provider = "Test Provider",
            responseTimeMs = 100L
        )
        
        coEvery { mockDao.upsertHealthStatus(any()) } returns Unit
        
        // When
        repository.saveHealthStatus(healthStatus)
        
        // Then
        coVerify { mockDao.upsertHealthStatus(any()) }
    }

    @Test
    fun `getHealthStatus should return mapped domain object`() = runTest(testDispatcher) {
        // Given
        val entity = HealthStatusEntity(
            provider = "Test Provider",
            isHealthy = true,
            responseTimeMs = 150L,
            errorMessage = null,
            lastChecked = System.currentTimeMillis()
        )
        
        coEvery { mockDao.getHealthStatus("Test Provider") } returns entity
        
        // When
        val result = repository.getHealthStatus("Test Provider")
        
        // Then
        assertNotNull(result)
        assertEquals("Test Provider", result?.provider)
        assertTrue(result?.isHealthy ?: false)
        assertEquals(150L, result?.responseTimeMs)
    }

    @Test
    fun `getHealthSummary should calculate correct statistics`() = runTest(testDispatcher) {
        // Given
        val entities = listOf(
            HealthStatusEntity("Provider1", true, 100L, null, 1000L),
            HealthStatusEntity("Provider2", false, null, "Error", 2000L),
            HealthStatusEntity("Provider3", true, 6000L, null, 3000L) // Degraded due to slow response
        )
        
        coEvery { mockDao.getAllHealthStatus() } returns entities
        
        // When
        val summary = repository.getHealthSummary()
        
        // Then
        assertEquals(3, summary.totalProviders)
        assertEquals(1, summary.healthyCount) // Only Provider1 is truly healthy
        assertEquals(1, summary.degradedCount) // Provider3 is degraded (slow)
        assertEquals(1, summary.downCount) // Provider2 is down
        assertEquals(3000L, summary.lastUpdated) // Most recent timestamp
    }

    @Test
    fun `status enum should correctly classify health status`() {
        // OK status
        val healthyStatus = HealthStatus(true, "Provider", 100L)
        assertEquals(HealthStatus.Status.OK, healthyStatus.status)
        
        // DOWN status
        val downStatus = HealthStatus(false, "Provider", null, "Error")
        assertEquals(HealthStatus.Status.DOWN, downStatus.status)
        
        // DEGRADED status (slow response)
        val degradedStatus = HealthStatus(true, "Provider", 6000L)
        assertEquals(HealthStatus.Status.DEGRADED, degradedStatus.status)
    }

    @Test
    fun `health summary should calculate correct overall status`() {
        // All healthy
        val allHealthy = HealthSummary(3, 3, 0, 0, 1000L)
        assertEquals(HealthStatus.Status.OK, allHealthy.overallStatus)
        
        // Some degraded
        val someDegraded = HealthSummary(3, 2, 1, 0, 1000L)
        assertEquals(HealthStatus.Status.DEGRADED, someDegraded.overallStatus)
        
        // Some down
        val someDown = HealthSummary(3, 1, 1, 1, 1000L)
        assertEquals(HealthStatus.Status.DOWN, someDown.overallStatus)
    }

    @Test
    fun `clearAllHealthStatus should call DAO delete`() = runTest(testDispatcher) {
        // Given
        coEvery { mockDao.deleteAllHealthStatus() } returns Unit
        
        // When
        repository.clearAllHealthStatus()
        
        // Then
        coVerify { mockDao.deleteAllHealthStatus() }
    }
}