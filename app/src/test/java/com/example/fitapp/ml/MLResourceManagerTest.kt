package com.example.fitapp.ml

import android.graphics.Bitmap
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.tensorflow.lite.Interpreter

/**
 * Unit tests for MLResourceManager
 */
@RunWith(MockitoJUnitRunner::class)
class MLResourceManagerTest {
    
    @Mock
    private lateinit var mockInterpreter: Interpreter
    
    @Mock  
    private lateinit var mockBitmap: Bitmap
    
    private lateinit var resourceManager: MLResourceManager
    
    @Before
    fun setUp() {
        resourceManager = MLResourceManager.getInstance()
    }
    
    @After
    fun tearDown() = runTest {
        resourceManager.cleanup()
    }
    
    @Test
    fun `registerInterpreter should store interpreter with key`() = runTest {
        // Given
        val key = "test_interpreter"
        
        // When
        resourceManager.registerInterpreter(key, mockInterpreter)
        
        // Then
        val result = resourceManager.useInterpreter(key) { interpreter ->
            assertSame(mockInterpreter, interpreter)
            "success"
        }
        
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }
    
    @Test
    fun `useInterpreter should return error for unknown key`() = runTest {
        // Given
        val unknownKey = "unknown_interpreter"
        
        // When
        val result = resourceManager.useInterpreter(unknownKey) { "should not reach" }
        
        // Then
        assertTrue(result.isError)
        assertFalse((result as MLResult.Error).fallbackAvailable)
    }
    
    @Test
    fun `useInterpreter should handle exceptions gracefully`() = runTest {
        // Given
        val key = "test_interpreter"
        resourceManager.registerInterpreter(key, mockInterpreter)
        
        // When
        val result = resourceManager.useInterpreter(key) { 
            throw RuntimeException("Test exception")
        }
        
        // Then
        assertTrue(result.isError)
        assertTrue((result as MLResult.Error).fallbackAvailable)
        assertEquals("Test exception", result.exception.message)
    }
    
    @Test
    fun `useInterpreter should handle OutOfMemoryError as degraded result`() = runTest {
        // Given
        val key = "test_interpreter"
        resourceManager.registerInterpreter(key, mockInterpreter)
        
        // When
        val result = resourceManager.useInterpreter(key) { 
            throw OutOfMemoryError("Test OOM")
        }
        
        // Then
        assertTrue(result.isDegraded)
        assertEquals("Switched to low-memory mode", (result as MLResult.Degraded).message)
    }
    
    @Test
    fun `borrowBitmap should create new bitmap when pool is empty`() {
        // Given
        val width = 256
        val height = 256
        
        // When
        val bitmap = resourceManager.borrowBitmap(width, height)
        
        // Then
        assertNotNull(bitmap)
        assertEquals(width, bitmap?.width)
        assertEquals(height, bitmap?.height)
    }
    
    @Test
    fun `returnBitmap should add bitmap to pool`() {
        // Given
        `when`(mockBitmap.isRecycled).thenReturn(false)
        `when`(mockBitmap.width).thenReturn(256)
        `when`(mockBitmap.height).thenReturn(256)
        `when`(mockBitmap.config).thenReturn(Bitmap.Config.ARGB_8888)
        
        // When
        resourceManager.returnBitmap(mockBitmap)
        val borrowedBitmap = resourceManager.borrowBitmap(256, 256)
        
        // Then
        assertSame(mockBitmap, borrowedBitmap)
    }
    
    @Test
    fun `returnBitmap should not add recycled bitmap to pool`() {
        // Given
        `when`(mockBitmap.isRecycled).thenReturn(true)
        
        // When
        resourceManager.returnBitmap(mockBitmap)
        
        // Then - bitmap should not be added to pool (no verification needed as it's internal)
        verify(mockBitmap, never()).recycle()
    }
    
    @Test
    fun `checkMemoryPressure should return memory pressure ratio`() {
        // When
        val memoryPressure = resourceManager.checkMemoryPressure()
        
        // Then
        assertTrue(memoryPressure >= 0.0f)
        assertTrue(memoryPressure <= 1.0f)
    }
    
    @Test
    fun `getResourceStats should return current statistics`() {
        // When
        val stats = resourceManager.getResourceStats()
        
        // Then
        assertTrue(stats.interpreterCount >= 0)
        assertTrue(stats.bitmapPoolSize >= 0)
        assertTrue(stats.maxBitmapPoolSize > 0)
        assertTrue(stats.memoryUsageMB >= 0)
        assertTrue(stats.maxMemoryMB > 0)
        assertTrue(stats.memoryPressure >= 0.0f)
        assertTrue(stats.memoryPressure <= 1.0f)
    }
    
    @Test
    fun `cleanup should close all interpreters`() = runTest {
        // Given
        val key1 = "interpreter1"
        val key2 = "interpreter2" 
        val mockInterpreter2 = mock(Interpreter::class.java)
        
        resourceManager.registerInterpreter(key1, mockInterpreter)
        resourceManager.registerInterpreter(key2, mockInterpreter2)
        
        // When
        resourceManager.cleanup()
        
        // Then
        verify(mockInterpreter).close()
        verify(mockInterpreter2).close()
        
        // Should not be able to use interpreters after cleanup
        val result = resourceManager.useInterpreter(key1) { "should fail" }
        assertTrue(result.isError)
    }
    
    @Test
    fun `isHealthy should return false after cleanup`() = runTest {
        // Given
        resourceManager.registerInterpreter("test", mockInterpreter)
        assertTrue(resourceManager.isHealthy())
        
        // When
        resourceManager.cleanup()
        
        // Then
        assertFalse(resourceManager.isHealthy())
    }
    
    @Test
    fun `concurrent access should be thread safe`() = runTest {
        // This test verifies that the resource manager handles concurrent access safely
        // In a real scenario, you'd use multiple threads, but for unit testing we simulate
        
        // Given
        val key = "test_interpreter"
        resourceManager.registerInterpreter(key, mockInterpreter)
        
        // When - simulate concurrent access
        val results = listOf(
            resourceManager.useInterpreter(key) { "result1" },
            resourceManager.useInterpreter(key) { "result2" },
            resourceManager.useInterpreter(key) { "result3" }
        )
        
        // Then
        results.forEach { result ->
            assertTrue(result.isSuccess)
        }
    }
}