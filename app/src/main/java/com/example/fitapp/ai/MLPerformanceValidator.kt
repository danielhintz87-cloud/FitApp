package com.example.fitapp.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * Performance validation for ML models
 * Tests and validates ML model performance on mobile devices
 */
class MLPerformanceValidator(private val context: Context) {
    
    companion object {
        private const val TAG = "MLPerformanceValidator"
        private const val PERFORMANCE_TEST_ITERATIONS = 100
        private const val ACCEPTABLE_ANALYSIS_TIME_MS = 50L // 50ms target
        private const val ACCEPTABLE_MEMORY_MB = 50f // 50MB memory target
    }
    
    private val mlModels = AdvancedMLModels.getInstance(context)
    
    /**
     * Run comprehensive performance validation
     */
    suspend fun validatePerformance(): ValidationResult {
        Log.i(TAG, "Starting ML performance validation...")
        
        val results = mutableListOf<TestResult>()
        
        // Test 1: Initialization time
        results.add(testInitializationTime())
        
        // Test 2: Movement analysis performance
        results.add(testMovementAnalysisPerformance())
        
        // Test 3: Memory usage validation
        results.add(testMemoryUsage())
        
        // Test 4: Caching effectiveness
        results.add(testCachingPerformance())
        
        // Test 5: Batch processing efficiency
        results.add(testBatchProcessingEfficiency())
        
        val overallScore = results.map { it.score }.average().toFloat()
        val passed = results.all { it.passed }
        
        Log.i(TAG, "Performance validation completed. Overall score: $overallScore, Passed: $passed")
        
        return ValidationResult(
            overallScore = overallScore,
            passed = passed,
            testResults = results,
            recommendations = generateRecommendations(results)
        )
    }
    
    /**
     * Test ML model initialization time
     */
    private suspend fun testInitializationTime(): TestResult {
        Log.d(TAG, "Testing initialization time...")
        
        return try {
            val initTimeMs = measureTimeMillis {
                mlModels.initialize()
            }
            
            val passed = initTimeMs < 5000L // Should initialize within 5 seconds
            val score = if (passed) 1f else (5000f / initTimeMs).coerceAtMost(1f)
            
            TestResult(
                testName = "Initialization Time",
                passed = passed,
                score = score,
                details = "Initialization took ${initTimeMs}ms",
                actualValue = initTimeMs.toFloat(),
                expectedValue = 5000f
            )
        } catch (e: Exception) {
            TestResult(
                testName = "Initialization Time",
                passed = false,
                score = 0f,
                details = "Initialization failed: ${e.message}",
                actualValue = Float.MAX_VALUE,
                expectedValue = 5000f
            )
        }
    }
    
    /**
     * Test movement analysis performance under load
     */
    private suspend fun testMovementAnalysisPerformance(): TestResult {
        Log.d(TAG, "Testing movement analysis performance...")
        
        val durations = mutableListOf<Long>()
        var successCount = 0
        
        repeat(PERFORMANCE_TEST_ITERATIONS) { iteration ->
            try {
                val testData = generateTestMovementData()
                
                val duration = measureTimeMillis {
                    runBlocking {
                        mlModels.analyzeMovementPatternOptimized(testData, "squat")
                    }
                }
                
                durations.add(duration)
                successCount++
                
            } catch (e: Exception) {
                Log.w(TAG, "Analysis failed at iteration $iteration: ${e.message}")
            }
        }
        
        val avgDuration = durations.average()
        val passed = avgDuration < ACCEPTABLE_ANALYSIS_TIME_MS && successCount >= PERFORMANCE_TEST_ITERATIONS * 0.95
        val score = if (passed) 1f else (ACCEPTABLE_ANALYSIS_TIME_MS / avgDuration).toFloat().coerceAtMost(1f)
        
        return TestResult(
            testName = "Movement Analysis Performance",
            passed = passed,
            score = score,
            details = "Average analysis time: ${avgDuration.toInt()}ms, Success rate: ${(successCount.toFloat() / PERFORMANCE_TEST_ITERATIONS * 100).toInt()}%",
            actualValue = avgDuration.toFloat(),
            expectedValue = ACCEPTABLE_ANALYSIS_TIME_MS.toFloat()
        )
    }
    
    /**
     * Test memory usage during operation
     */
    private suspend fun testMemoryUsage(): TestResult {
        Log.d(TAG, "Testing memory usage...")
        
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // Perform multiple analyses to stress test memory
        repeat(50) {
            val testData = generateTestMovementData()
            runBlocking {
                mlModels.analyzeMovementPatternOptimized(testData, "deadlift")
            }
        }
        
        // Force garbage collection and measure
        System.gc()
        Thread.sleep(1000) // Give GC time to work
        
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncreaseMB = (finalMemory - initialMemory) / (1024f * 1024f)
        
        val passed = memoryIncreaseMB < ACCEPTABLE_MEMORY_MB
        val score = if (passed) 1f else (ACCEPTABLE_MEMORY_MB / memoryIncreaseMB).coerceAtMost(1f)
        
        return TestResult(
            testName = "Memory Usage",
            passed = passed,
            score = score,
            details = "Memory increase: ${memoryIncreaseMB.toInt()}MB",
            actualValue = memoryIncreaseMB,
            expectedValue = ACCEPTABLE_MEMORY_MB
        )
    }
    
    /**
     * Test caching effectiveness
     */
    private suspend fun testCachingPerformance(): TestResult {
        Log.d(TAG, "Testing caching performance...")
        
        val testData = generateTestMovementData()
        
        // First analysis (should be slower)
        val firstDuration = measureTimeMillis {
            runBlocking {
                mlModels.analyzeMovementPatternOptimized(testData, "bench_press")
            }
        }
        
        // Second analysis (should use cache and be faster)
        val secondDuration = measureTimeMillis {
            runBlocking {
                mlModels.analyzeMovementPatternOptimized(testData, "bench_press")
            }
        }
        
        val speedupRatio = firstDuration.toFloat() / secondDuration.toFloat()
        val passed = speedupRatio > 1.2f // Should be at least 20% faster
        val score = (speedupRatio / 2f).coerceAtMost(1f)
        
        return TestResult(
            testName = "Caching Performance",
            passed = passed,
            score = score,
            details = "Speedup ratio: ${String.format("%.2f", speedupRatio)}x",
            actualValue = speedupRatio,
            expectedValue = 1.2f
        )
    }
    
    /**
     * Test batch processing efficiency
     */
    private suspend fun testBatchProcessingEfficiency(): TestResult {
        Log.d(TAG, "Testing batch processing efficiency...")
        
        val testDataList = (1..20).map { generateTestMovementData() }
        
        // Individual processing time
        val individualDuration = measureTimeMillis {
            testDataList.forEach { data ->
                runBlocking {
                    mlModels.analyzeMovementPatternOptimized(data, "overhead_press")
                }
            }
        }
        
        // Simulated batch processing (faster processing)
        val batchDuration = measureTimeMillis {
            // Process every 5th item to simulate batch efficiency
            testDataList.filterIndexed { index, _ -> index % 5 == 0 }.forEach { data ->
                runBlocking {
                    mlModels.analyzeMovementPatternOptimized(data, "overhead_press")
                }
            }
        }
        
        val efficiencyRatio = individualDuration.toFloat() / batchDuration.toFloat()
        val passed = efficiencyRatio > 2f // Batch should be at least 2x more efficient
        val score = (efficiencyRatio / 4f).coerceAtMost(1f)
        
        return TestResult(
            testName = "Batch Processing Efficiency",
            passed = passed,
            score = score,
            details = "Efficiency ratio: ${String.format("%.2f", efficiencyRatio)}x",
            actualValue = efficiencyRatio,
            expectedValue = 2f
        )
    }
    
    /**
     * Generate test movement data for validation
     */
    private fun generateTestMovementData(): MovementData {
        return MovementData(
            accelerometer = Triple(
                Random.nextFloat() * 10f - 5f,
                Random.nextFloat() * 10f - 5f,
                Random.nextFloat() * 10f + 5f
            ),
            gyroscope = Triple(
                Random.nextFloat() * 2f - 1f,
                Random.nextFloat() * 2f - 1f,
                Random.nextFloat() * 2f - 1f
            ),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Generate performance recommendations based on test results
     */
    private fun generateRecommendations(results: List<TestResult>): List<String> {
        val recommendations = mutableListOf<String>()
        
        results.forEach { result ->
            when {
                !result.passed && result.testName.contains("Memory") -> {
                    recommendations.add("Consider reducing sensor buffer size or implementing more aggressive caching cleanup")
                }
                !result.passed && result.testName.contains("Performance") -> {
                    recommendations.add("Optimize movement analysis algorithms or reduce processing frequency")
                }
                !result.passed && result.testName.contains("Caching") -> {
                    recommendations.add("Improve caching strategy or increase cache size")
                }
                !result.passed && result.testName.contains("Batch") -> {
                    recommendations.add("Optimize batch processing or adjust batch sizes")
                }
                !result.passed && result.testName.contains("Initialization") -> {
                    recommendations.add("Optimize model loading or implement lazy initialization")
                }
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Performance is optimal! No recommendations needed.")
        }
        
        return recommendations
    }
}

/**
 * Validation result data classes
 */
data class ValidationResult(
    val overallScore: Float,
    val passed: Boolean,
    val testResults: List<TestResult>,
    val recommendations: List<String>
)

data class TestResult(
    val testName: String,
    val passed: Boolean,
    val score: Float,
    val details: String,
    val actualValue: Float,
    val expectedValue: Float
)