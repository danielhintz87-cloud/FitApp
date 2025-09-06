package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`

/**
 * Unit tests for AdvancedMLModels functionality
 */
class AdvancedMLModelsTest {

    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockApplicationContext: Context

    private lateinit var mlModels: AdvancedMLModels

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.applicationContext).thenReturn(mockApplicationContext)
        mlModels = AdvancedMLModels.getInstance(mockContext)
    }

    @Test
    fun testModelTypeSelection() {
        val modelTypes = AdvancedMLModels.PoseModelType.values().toSet()
        assertEquals(3, modelTypes.size)
        assertTrue(modelTypes.contains(AdvancedMLModels.PoseModelType.MOVENET_THUNDER))
        assertTrue(modelTypes.contains(AdvancedMLModels.PoseModelType.MOVENET_LIGHTNING))
        assertTrue(modelTypes.contains(AdvancedMLModels.PoseModelType.BLAZEPOSE))
    }

    @Test
    fun testDefaultModelType() {
        assertEquals(AdvancedMLModels.PoseModelType.MOVENET_THUNDER, mlModels.getCurrentModelType())
    }

    @Test
    fun testBatchProcessingEmptyList() = runBlocking {
        // Test batch processing with empty list
        val results = mlModels.analyzeBatch(emptyList())
        assertTrue("Empty batch should return empty results", results.isEmpty())
    }

    @Test
    fun testPerformanceMetrics() {
        // Test that performance metrics are available
        val metrics = mlModels.getPerformanceMetrics()
        assertNotNull("Performance metrics should not be null", metrics)
        
        // Test that metrics have reasonable default values
        assertTrue("Memory usage should be non-negative", metrics.memoryUsageMB >= 0)
        assertTrue("Average pose analysis time should be non-negative", metrics.avgPoseAnalysisTime >= 0)
        assertTrue("Average movement analysis time should be non-negative", metrics.avgMovementAnalysisTime >= 0)
    }
}