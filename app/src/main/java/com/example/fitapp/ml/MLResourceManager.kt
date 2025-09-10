package com.example.fitapp.ml

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tensorflow.lite.Interpreter
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Central ML Resource Manager for efficient memory management and resource lifecycle control.
 * 
 * Features:
 * - Manages TensorFlow Lite Interpreter instances lifecycle
 * - Provides bitmap pooling to reduce allocations
 * - Ensures proper cleanup of native resources
 * - Thread-safe operations with mutex protection
 * - Memory pressure monitoring and adaptive cleanup
 */
class MLResourceManager private constructor() {
    
    companion object {
        private const val TAG = "MLResourceManager"
        private const val DEFAULT_BITMAP_POOL_SIZE = 8
        private const val MAX_BITMAP_POOL_SIZE = 15
        private const val MEMORY_PRESSURE_THRESHOLD = 0.75f
        
        @Volatile
        private var INSTANCE: MLResourceManager? = null
        
        fun getInstance(): MLResourceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MLResourceManager().also { INSTANCE = it }
            }
        }
    }
    
    private val interpreterRegistry = mutableMapOf<String, Interpreter>()
    private val interpreterMutex = Mutex()
    private val isShutdown = AtomicBoolean(false)
    
    // Bitmap pool for memory efficiency
    private val bitmapPool = ConcurrentLinkedQueue<Bitmap>()
    private val poolSize = AtomicInteger(0)
    private val maxPoolSize = AtomicInteger(DEFAULT_BITMAP_POOL_SIZE)
    
    /**
     * Register a TensorFlow Lite Interpreter for managed lifecycle
     */
    suspend fun registerInterpreter(key: String, interpreter: Interpreter) {
        interpreterMutex.withLock {
            if (isShutdown.get()) {
                Log.w(TAG, "Cannot register interpreter - manager is shutdown")
                return
            }
            
            // Close existing interpreter if present
            interpreterRegistry[key]?.close()
            interpreterRegistry[key] = interpreter
            Log.d(TAG, "Registered interpreter: $key")
        }
    }
    
    /**
     * Use an interpreter safely with automatic cleanup
     */
    suspend fun <T> useInterpreter(key: String, block: suspend (Interpreter) -> T): MLResult<T> {
        return try {
            interpreterMutex.withLock {
                val interpreter = interpreterRegistry[key]
                    ?: return MLResult.Error(
                        IllegalStateException("Interpreter not found: $key"),
                        fallbackAvailable = false
                    )
                
                if (isShutdown.get()) {
                    return MLResult.Error(
                        IllegalStateException("Manager is shutdown"),
                        fallbackAvailable = false
                    )
                }
                
                MLResult.Success(block(interpreter))
            }
        } catch (outOfMemory: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError during interpreter usage", outOfMemory)
            handleMemoryPressure()
            MLResult.Degraded(
                exception = outOfMemory,
                degradedResult = null,
                message = "Switched to low-memory mode"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error using interpreter: $key", e)
            MLResult.Error(e, fallbackAvailable = true)
        }
    }
    
    /**
     * Borrow a bitmap from the pool or create new one
     */
    fun borrowBitmap(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
        if (isShutdown.get()) return null
        
        // Try to reuse existing bitmap with matching dimensions
        val iterator = bitmapPool.iterator()
        while (iterator.hasNext()) {
            val bitmap = iterator.next()
            if (!bitmap.isRecycled && 
                bitmap.width == width && 
                bitmap.height == height && 
                bitmap.config == config) {
                iterator.remove()
                poolSize.decrementAndGet()
                Log.v(TAG, "Reused bitmap from pool: ${width}x${height}")
                return bitmap
            }
        }
        
        // Create new bitmap if pool miss
        return try {
            val bitmap = Bitmap.createBitmap(width, height, config)
            Log.v(TAG, "Created new bitmap: ${width}x${height}")
            bitmap
        } catch (outOfMemory: OutOfMemoryError) {
            Log.w(TAG, "Failed to create bitmap due to memory pressure")
            handleMemoryPressure()
            null
        }
    }
    
    /**
     * Return a bitmap to the pool for reuse
     */
    fun returnBitmap(bitmap: Bitmap?) {
        if (bitmap == null || bitmap.isRecycled || isShutdown.get()) return
        
        // Only keep bitmaps in pool if under size limit
        if (poolSize.get() < maxPoolSize.get()) {
            bitmapPool.offer(bitmap)
            poolSize.incrementAndGet()
            Log.v(TAG, "Returned bitmap to pool: ${bitmap.width}x${bitmap.height}")
        } else {
            // Pool is full, recycle the bitmap
            bitmap.recycle()
            Log.v(TAG, "Recycled bitmap - pool full")
        }
    }
    
    /**
     * Check current memory pressure and adjust pool size accordingly
     */
    fun checkMemoryPressure(): Float {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryPressure = usedMemory.toFloat() / maxMemory.toFloat()
        
        // Adjust pool size based on memory pressure
        when {
            memoryPressure > MEMORY_PRESSURE_THRESHOLD -> {
                maxPoolSize.set(maxOf(2, maxPoolSize.get() - 1))
                handleMemoryPressure()
            }
            memoryPressure < 0.5f -> {
                maxPoolSize.set(minOf(MAX_BITMAP_POOL_SIZE, maxPoolSize.get() + 1))
            }
        }
        
        return memoryPressure
    }
    
    /**
     * Handle memory pressure by cleaning up resources
     */
    private fun handleMemoryPressure() {
        Log.i(TAG, "Handling memory pressure - cleaning up resources")
        
        // Clear excess bitmaps from pool
        val targetSize = maxOf(2, maxPoolSize.get() / 2)
        while (poolSize.get() > targetSize && !bitmapPool.isEmpty()) {
            val bitmap = bitmapPool.poll()
            bitmap?.recycle()
            poolSize.decrementAndGet()
        }
        
        // Suggest garbage collection
        System.gc()
        
        Log.i(TAG, "Memory pressure handling completed")
    }
    
    /**
     * Get current resource statistics
     */
    fun getResourceStats(): ResourceStats {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        return ResourceStats(
            interpreterCount = interpreterRegistry.size,
            bitmapPoolSize = poolSize.get(),
            maxBitmapPoolSize = maxPoolSize.get(),
            memoryUsageMB = usedMemory / (1024 * 1024),
            maxMemoryMB = maxMemory / (1024 * 1024),
            memoryPressure = usedMemory.toFloat() / maxMemory.toFloat()
        )
    }
    
    /**
     * Cleanup all resources
     */
    suspend fun cleanup() {
        if (!isShutdown.compareAndSet(false, true)) {
            return // Already shutdown
        }
        
        Log.i(TAG, "Starting ML resource cleanup...")
        
        interpreterMutex.withLock {
            // Close all interpreters
            interpreterRegistry.values.forEach { interpreter ->
                try {
                    interpreter.close()
                } catch (e: Exception) {
                    Log.w(TAG, "Error closing interpreter", e)
                }
            }
            interpreterRegistry.clear()
        }
        
        // Clear bitmap pool
        while (!bitmapPool.isEmpty()) {
            val bitmap = bitmapPool.poll()
            try {
                bitmap?.recycle()
            } catch (e: Exception) {
                Log.w(TAG, "Error recycling bitmap", e)
            }
        }
        poolSize.set(0)
        
        Log.i(TAG, "ML resource cleanup completed")
    }
    
    /**
     * Check if manager is healthy and operational
     */
    fun isHealthy(): Boolean {
        return !isShutdown.get() && interpreterRegistry.isNotEmpty()
    }
}

/**
 * Data class for resource statistics
 */
data class ResourceStats(
    val interpreterCount: Int,
    val bitmapPoolSize: Int,
    val maxBitmapPoolSize: Int,
    val memoryUsageMB: Long,
    val maxMemoryMB: Long,
    val memoryPressure: Float
)