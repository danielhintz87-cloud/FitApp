package com.example.fitapp.ml

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MLResult sealed class
 */
class MLResultTest {
    
    @Test
    fun `Success should contain data`() {
        // Given
        val data = "test_data"
        val result = MLResult.Success(data)
        
        // Then
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertFalse(result.isDegraded)
        assertEquals(data, result.getOrNull())
        assertEquals(data, result.getOrDefault("default"))
        assertNull(result.getExceptionOrNull())
    }
    
    @Test
    fun `Error should contain exception and fallback info`() {
        // Given
        val exception = RuntimeException("test error")
        val result = MLResult.Error(exception, fallbackAvailable = true, message = "test message")
        
        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertFalse(result.isDegraded)
        assertNull(result.getOrNull())
        assertEquals("default", result.getOrDefault("default"))
        assertEquals(exception, result.getExceptionOrNull())
        assertTrue(result.fallbackAvailable)
        assertEquals("test message", result.message)
    }
    
    @Test
    fun `Degraded should contain exception and optional result`() {
        // Given
        val exception = OutOfMemoryError("low memory")
        val degradedData = "degraded_result"
        val result = MLResult.Degraded(exception, degradedData, "degraded mode")
        
        // Then
        assertFalse(result.isSuccess)
        assertFalse(result.isError)
        assertTrue(result.isDegraded)
        assertEquals(degradedData, result.getOrNull())
        assertEquals(degradedData, result.getOrDefault("default"))
        assertEquals(exception, result.getExceptionOrNull())
        assertEquals("degraded mode", result.message)
    }
    
    @Test
    fun `map should transform successful result`() {
        // Given
        val result = MLResult.Success("5")
        
        // When
        val mapped = result.map { it.toInt() }
        
        // Then
        assertTrue(mapped.isSuccess)
        assertEquals(5, mapped.getOrNull())
    }
    
    @Test
    fun `map should preserve error result`() {
        // Given
        val exception = RuntimeException("error")
        val result = MLResult.error(exception)
        
        // When
        val mapped = result.map { "should not execute" }
        
        // Then
        assertTrue(mapped.isError)
        assertEquals(exception, mapped.getExceptionOrNull())
    }
    
    @Test
    fun `map should transform degraded result`() {
        // Given
        val exception = OutOfMemoryError("low memory")
        val result = MLResult.Degraded(exception, "5", "degraded")
        
        // When
        val mapped = result.map { it.toInt() }
        
        // Then
        assertTrue(mapped.isDegraded)
        assertEquals(5, mapped.getOrNull())
        assertEquals(exception, mapped.getExceptionOrNull())
    }
    
    @Test
    fun `flatMap should chain successful results`() {
        // Given
        val result = MLResult.Success(5)
        
        // When
        val flatMapped = result.flatMap { value ->
            if (value > 0) MLResult.Success(value * 2)
            else MLResult.error(RuntimeException("negative"))
        }
        
        // Then
        assertTrue(flatMapped.isSuccess)
        assertEquals(10, flatMapped.getOrNull())
    }
    
    @Test
    fun `flatMap should propagate error`() {
        // Given
        val exception = RuntimeException("original error")
        val result = MLResult.error(exception)
        
        // When
        val flatMapped = result.flatMap { MLResult.Success("should not execute") }
        
        // Then
        assertTrue(flatMapped.isError)
        assertEquals(exception, flatMapped.getExceptionOrNull())
    }
    
    @Test
    fun `onSuccess should execute action for successful result`() {
        // Given
        var executed = false
        val result = MLResult.Success("data")
        
        // When
        result.onSuccess { executed = true }
        
        // Then
        assertTrue(executed)
    }
    
    @Test
    fun `onSuccess should not execute action for error result`() {
        // Given
        var executed = false
        val result = MLResult.error(RuntimeException())
        
        // When
        result.onSuccess { executed = true }
        
        // Then
        assertFalse(executed)
    }
    
    @Test
    fun `onError should execute action for error result`() {
        // Given
        var executed = false
        val exception = RuntimeException("test")
        val result = MLResult.error(exception, fallbackAvailable = true)
        
        // When
        result.onError { ex, fallback ->
            executed = true
            assertEquals(exception, ex)
            assertTrue(fallback)
        }
        
        // Then
        assertTrue(executed)
    }
    
    @Test
    fun `onDegraded should execute action for degraded result`() {
        // Given
        var executed = false
        val exception = OutOfMemoryError("test")
        val result = MLResult.Degraded(exception, "data", "degraded")
        
        // When
        result.onDegraded { ex, data, message ->
            executed = true
            assertEquals(exception, ex)
            assertEquals("data", data)
            assertEquals("degraded", message)
        }
        
        // Then
        assertTrue(executed)
    }
    
    @Test
    fun `fold should handle all result types`() {
        // Test Success
        val successResult = MLResult.Success("data")
        val successFolded = successResult.fold(
            onSuccess = { "success: $it" },
            onError = { _, _ -> "error" },
            onDegraded = { _, _, _ -> "degraded" }
        )
        assertEquals("success: data", successFolded)
        
        // Test Error
        val errorResult = MLResult.error(RuntimeException("test"), true)
        val errorFolded = errorResult.fold(
            onSuccess = { "success" },
            onError = { ex, fallback -> "error: ${ex.message}, fallback: $fallback" },
            onDegraded = { _, _, _ -> "degraded" }
        )
        assertEquals("error: test, fallback: true", errorFolded)
        
        // Test Degraded
        val degradedResult = MLResult.Degraded(OutOfMemoryError("oom"), "partial", "degraded")
        val degradedFolded = degradedResult.fold(
            onSuccess = { "success" },
            onError = { _, _ -> "error" },
            onDegraded = { ex, data, message -> "degraded: $message with data: $data" }
        )
        assertEquals("degraded: degraded with data: partial", degradedFolded)
    }
    
    @Test
    fun `catching should wrap successful operation`() {
        // When
        val result = MLResult.catching { "success" }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }
    
    @Test
    fun `catching should wrap exception as error`() {
        // When
        val result = MLResult.catching(fallbackAvailable = true) { 
            throw RuntimeException("test error")
        }
        
        // Then
        assertTrue(result.isError)
        assertEquals("test error", result.getExceptionOrNull()?.message)
        assertTrue((result as MLResult.Error).fallbackAvailable)
    }
    
    @Test
    fun `catching should wrap OutOfMemoryError as degraded`() {
        // When
        val result = MLResult.catching { 
            throw OutOfMemoryError("low memory")
        }
        
        // Then
        assertTrue(result.isDegraded)
        assertEquals("Out of memory - switched to degraded mode", (result as MLResult.Degraded).message)
    }
    
    @Test
    fun `combine should return success for all successful results`() {
        // Given
        val results = listOf(
            MLResult.Success("a"),
            MLResult.Success("b"),
            MLResult.Success("c")
        )
        
        // When
        val combined = MLResult.combine(results)
        
        // Then
        assertTrue(combined.isSuccess)
        assertEquals(listOf("a", "b", "c"), combined.getOrNull())
    }
    
    @Test
    fun `combine should return error if any result is error`() {
        // Given
        val results = listOf(
            MLResult.Success("a"),
            MLResult.error(RuntimeException("error")),
            MLResult.Success("c")
        )
        
        // When
        val combined = MLResult.combine(results)
        
        // Then
        assertTrue(combined.isError)
    }
    
    @Test
    fun `combine should return degraded if any result is degraded but no errors`() {
        // Given
        val results = listOf(
            MLResult.Success("a"),
            MLResult.Degraded(OutOfMemoryError("oom"), "b", "degraded"),
            MLResult.Success("c")
        )
        
        // When
        val combined = MLResult.combine(results)
        
        // Then
        assertTrue(combined.isDegraded)
        assertEquals(listOf("a", "b", "c"), combined.getOrNull())
    }
    
    @Test
    fun `companion object factory methods should work correctly`() {
        // Test success factory
        val success = MLResult.success("data")
        assertTrue(success.isSuccess)
        assertEquals("data", success.getOrNull())
        
        // Test error factory
        val error = MLResult.error(RuntimeException("test"), true, "message")
        assertTrue(error.isError)
        assertEquals("test", error.getExceptionOrNull()?.message)
        
        // Test degraded factory
        val degraded = MLResult.degraded(OutOfMemoryError("oom"), "partial", "degraded")
        assertTrue(degraded.isDegraded)
        assertEquals("partial", degraded.getOrNull())
    }
}