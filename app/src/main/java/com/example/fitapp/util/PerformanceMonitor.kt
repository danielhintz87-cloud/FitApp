package com.example.fitapp.util

import android.content.Context
import android.os.Debug
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

/**
 * Comprehensive performance monitoring for FitApp
 * Tracks app performance, memory usage, and frame rates
 */
object PerformanceMonitor {
    
    const val TAG = "FitApp_Performance"
    
    // Performance metrics
    private val operationTimes = ConcurrentHashMap<String, MutableList<Long>>()
    private val memoryUsage = AtomicLong(0)
    private val frameDrops = AtomicInteger(0)
    private val apiCallCount = AtomicInteger(0)
    private val databaseCallCount = AtomicInteger(0)
    
    // Memory thresholds (in MB)
    private const val MEMORY_WARNING_THRESHOLD = 100
    private const val MEMORY_CRITICAL_THRESHOLD = 200
    
    /**
     * Measure execution time of an operation
     */
    suspend fun <T> measureOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        val result: T
        val time = measureTimeMillis {
            result = operation()
        }
        
        recordOperationTime(operationName, time)
        
        if (time > 1000) { // Log slow operations
            Log.w(TAG, "Slow operation detected: $operationName took ${time}ms")
        }
        
        return result
    }
    
    /**
     * Measure execution time of a synchronous operation
     */
    fun <T> measureOperationSync(
        operationName: String,
        operation: () -> T
    ): T {
        val result: T
        val time = measureTimeMillis {
            result = operation()
        }
        
        recordOperationTime(operationName, time)
        
        if (time > 500) { // Log slow sync operations
            Log.w(TAG, "Slow sync operation detected: $operationName took ${time}ms")
        }
        
        return result
    }
    
    /**
     * Record API call for monitoring
     */
    fun recordApiCall(provider: String, success: Boolean, responseTimeMs: Long) {
        apiCallCount.incrementAndGet()
        recordOperationTime("api_call_$provider", responseTimeMs)
        
        if (responseTimeMs > 5000) {
            Log.w(TAG, "Slow API call: $provider took ${responseTimeMs}ms")
        }
        
        if (!success) {
            Log.w(TAG, "Failed API call: $provider")
        }
    }
    
    /**
     * Record database operation for monitoring
     */
    fun recordDatabaseOperation(operation: String, timeMs: Long) {
        databaseCallCount.incrementAndGet()
        recordOperationTime("db_$operation", timeMs)
        
        if (timeMs > 100) {
            Log.w(TAG, "Slow database operation: $operation took ${timeMs}ms")
        }
    }
    
    /**
     * Monitor memory usage
     */
    fun checkMemoryUsage(context: Context) {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 // MB
        memoryUsage.set(usedMemory)
        
        when {
            usedMemory > MEMORY_CRITICAL_THRESHOLD -> {
                Log.e(TAG, "Critical memory usage: ${usedMemory}MB")
                // Suggest garbage collection
                System.gc()
            }
            usedMemory > MEMORY_WARNING_THRESHOLD -> {
                Log.w(TAG, "High memory usage: ${usedMemory}MB")
            }
        }
    }
    
    /**
     * Get memory usage info
     */
    fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        
        return MemoryInfo(
            usedMB = usedMemory,
            maxMB = maxMemory,
            totalMB = totalMemory,
            availableMB = maxMemory - usedMemory,
            usagePercentage = (usedMemory.toDouble() / maxMemory * 100).toInt()
        )
    }
    
    /**
     * Record frame drop for UI performance monitoring
     */
    fun recordFrameDrop() {
        frameDrops.incrementAndGet()
    }
    
    /**
     * Get performance statistics
     */
    fun getPerformanceStats(): PerformanceStats {
        val stats = mutableMapOf<String, OperationStats>()
        
        operationTimes.forEach { (operation, times) ->
            if (times.isNotEmpty()) {
                val avg = times.average()
                val max = times.maxOrNull() ?: 0L
                val min = times.minOrNull() ?: 0L
                stats[operation] = OperationStats(
                    averageMs = avg.toLong(),
                    maxMs = max,
                    minMs = min,
                    callCount = times.size
                )
            }
        }
        
        return PerformanceStats(
            operationStats = stats,
            memoryInfo = getMemoryInfo(),
            frameDrops = frameDrops.get(),
            apiCallCount = apiCallCount.get(),
            databaseCallCount = databaseCallCount.get()
        )
    }
    
    /**
     * Log comprehensive performance report
     */
    fun logPerformanceReport() {
        val stats = getPerformanceStats()
        
        Log.i(TAG, "=== Performance Report ===")
        Log.i(TAG, "Memory: ${stats.memoryInfo.usedMB}MB/${stats.memoryInfo.maxMB}MB (${stats.memoryInfo.usagePercentage}%)")
        Log.i(TAG, "Frame drops: ${stats.frameDrops}")
        Log.i(TAG, "API calls: ${stats.apiCallCount}")
        Log.i(TAG, "Database calls: ${stats.databaseCallCount}")
        
        stats.operationStats.forEach { (operation, opStats) ->
            if (opStats.callCount > 0) {
                Log.i(TAG, "$operation: avg=${opStats.averageMs}ms, max=${opStats.maxMs}ms, calls=${opStats.callCount}")
            }
        }
        
        // Detect performance issues
        detectPerformanceIssues(stats)
    }
    
    /**
     * Clear all performance data
     */
    fun clearPerformanceData() {
        operationTimes.clear()
        frameDrops.set(0)
        apiCallCount.set(0)
        databaseCallCount.set(0)
        Log.i(TAG, "Performance data cleared")
    }
    
    /**
     * Start performance monitoring for the session
     */
    fun startMonitoring(context: Context, scope: CoroutineScope) {
        Log.i(TAG, "Starting performance monitoring")
        
        // Monitor memory usage periodically
        scope.launch {
            while (true) {
                checkMemoryUsage(context)
                delay(30_000) // Check every 30 seconds
            }
        }
        
        // Log performance report periodically
        scope.launch {
            while (true) {
                delay(300_000) // Log report every 5 minutes
                logPerformanceReport()
            }
        }
    }
    
    // Helper methods
    
    fun recordOperationTime(operation: String, timeMs: Long) {
        operationTimes.computeIfAbsent(operation) { mutableListOf() }.add(timeMs)
        
        // Keep only last 100 measurements to prevent memory growth
        val times = operationTimes[operation]
        if (times != null && times.size > 100) {
            times.removeAt(0)
        }
    }
    
    private fun detectPerformanceIssues(stats: PerformanceStats) {
        // Memory issues
        if (stats.memoryInfo.usagePercentage > 80) {
            Log.w(TAG, "Performance Issue: High memory usage (${stats.memoryInfo.usagePercentage}%)")
        }
        
        // Frame drops
        if (stats.frameDrops > 10) {
            Log.w(TAG, "Performance Issue: High frame drops (${stats.frameDrops})")
        }
        
        // Slow operations
        stats.operationStats.forEach { (operation, opStats) ->
            when {
                operation.startsWith("api_") && opStats.averageMs > 3000 -> {
                    Log.w(TAG, "Performance Issue: Slow API calls for $operation (avg: ${opStats.averageMs}ms)")
                }
                operation.startsWith("db_") && opStats.averageMs > 50 -> {
                    Log.w(TAG, "Performance Issue: Slow database operations for $operation (avg: ${opStats.averageMs}ms)")
                }
                opStats.averageMs > 1000 -> {
                    Log.w(TAG, "Performance Issue: Slow operation $operation (avg: ${opStats.averageMs}ms)")
                }
            }
        }
    }
}

/**
 * Data classes for performance monitoring
 */
data class MemoryInfo(
    val usedMB: Long,
    val maxMB: Long,
    val totalMB: Long,
    val availableMB: Long,
    val usagePercentage: Int
)

data class OperationStats(
    val averageMs: Long,
    val maxMs: Long,
    val minMs: Long,
    val callCount: Int
)

data class PerformanceStats(
    val operationStats: Map<String, OperationStats>,
    val memoryInfo: MemoryInfo,
    val frameDrops: Int,
    val apiCallCount: Int,
    val databaseCallCount: Int
)

/**
 * Extension functions for easy performance monitoring
 */
suspend fun <T> withPerformanceMonitoring(
    operationName: String,
    operation: suspend () -> T
): T = PerformanceMonitor.measureOperation(operationName, operation)

fun <T> withPerformanceMonitoringSync(
    operationName: String,
    operation: () -> T
): T = PerformanceMonitor.measureOperationSync(operationName, operation)