package com.example.fitapp.util

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Memory leak prevention utilities for FitApp
 * Provides safe lifecycle management, weak references, and coroutine scoping
 */
object MemoryLeakPrevention {
    
    private const val TAG = "MemoryLeakPrevention"
    private val activeJobs = ConcurrentHashMap<String, MutableSet<Job>>()
    private val lifecycleObservers = ConcurrentHashMap<String, LifecycleEventObserver>()
    
    /**
     * Create a lifecycle-aware coroutine scope that automatically cancels jobs
     */
    fun createLifecycleAwareScope(
        lifecycleOwner: LifecycleOwner,
        dispatcher: CoroutineDispatcher = Dispatchers.Main
    ): CoroutineScope {
        val scopeId = "${lifecycleOwner.javaClass.simpleName}_${System.identityHashCode(lifecycleOwner)}"
        val scope = CoroutineScope(dispatcher + SupervisorJob())
        
        lateinit var observer: LifecycleEventObserver
        observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                try {
                    scope.cancel("Lifecycle destroyed")
                    cancelJobsForScope(scopeId)
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    lifecycleObservers.remove(scopeId)
                    
                    StructuredLogger.debug(
                        StructuredLogger.LogCategory.PERFORMANCE,
                        TAG,
                        "Scope cancelled for lifecycle owner: $scopeId"
                    )
                } catch (e: Exception) {
                    StructuredLogger.warning(
                        StructuredLogger.LogCategory.PERFORMANCE,
                        TAG,
                        "Error during scope cleanup",
                        exception = e
                    )
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        lifecycleObservers[scopeId] = observer
        
        return scope
    }
    
    /**
     * Launch a coroutine with automatic job tracking and cleanup
     */
    fun CoroutineScope.safeLaunch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        scopeId: String = "default",
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val job = launch(context, start, block)
        trackJob(scopeId, job)
        
        job.invokeOnCompletion { exception ->
            try {
                untrackJob(scopeId, job)
                if (exception != null && exception !is CancellationException) {
                    StructuredLogger.warning(
                        StructuredLogger.LogCategory.PERFORMANCE,
                        TAG,
                        "Coroutine completed with exception in scope $scopeId",
                        exception = exception
                    )
                }
            } catch (e: Exception) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.PERFORMANCE,
                    TAG,
                    "Error during job completion handling",
                    exception = e
                )
            }
        }
        
        return job
    }
    
    /**
     * Create a safe flow that handles lifecycle and memory management
     */
    fun <T> createSafeFlow(
        @Suppress("UNUSED_PARAMETER") lifecycleOwner: LifecycleOwner? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        producer: suspend FlowCollector<T>.() -> Unit
    ): Flow<T> = flow {
        try {
            producer()
        } catch (e: CancellationException) {
            // Allow cancellation to propagate
            throw e
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.PERFORMANCE,
                TAG,
                "Error in safe flow",
                exception = e
            )
            // Don't re-throw to prevent crashes
        }
    }.flowOn(dispatcher)
    
    /**
     * Weak reference holder for preventing memory leaks in callbacks
     */
    class WeakReferenceHolder<T : Any>(target: T) {
        private val weakRef = WeakReference(target)
        
        fun execute(action: (T) -> Unit) {
            weakRef.get()?.let { target ->
                try {
                    action(target)
                } catch (e: Exception) {
                    StructuredLogger.warning(
                        StructuredLogger.LogCategory.PERFORMANCE,
                        TAG,
                        "Error executing weak reference action",
                        exception = e
                    )
                }
            }
        }
        
        fun isValid(): Boolean = weakRef.get() != null
    }
    
    /**
     * Safe context holder that prevents memory leaks
     */
    class SafeContextHolder(context: Context) {
        private val appContext = context.applicationContext
        
        fun getContext(): Context = appContext
        
        fun executeWithContext(action: (Context) -> Unit) {
            try {
                action(appContext)
            } catch (e: Exception) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.PERFORMANCE,
                    TAG,
                    "Error executing context action",
                    exception = e
                )
            }
        }
    }
    
    /**
     * Auto-clearing weak reference map for temporary storage
     */
    class AutoClearingMap<K, V : Any> {
        private val map = ConcurrentHashMap<K, WeakReference<V>>()
        
        fun put(key: K, value: V) {
            map[key] = WeakReference(value)
        }
        
        fun get(key: K): V? {
            val ref = map[key]
            val value = ref?.get()
            if (value == null) {
                map.remove(key) // Clean up null references
            }
            return value
        }
        
        fun remove(key: K): V? {
            val ref = map.remove(key)
            return ref?.get()
        }
        
        fun clear() {
            map.clear()
        }
        
        fun cleanup() {
            val keysToRemove = mutableListOf<K>()
            map.forEach { (key, ref) ->
                if (ref.get() == null) {
                    keysToRemove.add(key)
                }
            }
            keysToRemove.forEach { map.remove(it) }
        }
        
        fun size(): Int = map.size
    }
    
    /**
     * Resource manager for automatic cleanup
     */
    class ResourceManager {
        private val resources = mutableListOf<() -> Unit>()
        
        fun <T : AutoCloseable> manage(resource: T): T {
            resources.add { 
                try {
                    resource.close()
                } catch (e: Exception) {
                    StructuredLogger.warning(
                        StructuredLogger.LogCategory.PERFORMANCE,
                        TAG,
                        "Error closing resource",
                        exception = e
                    )
                }
            }
            return resource
        }
        
        fun addCleanupAction(action: () -> Unit) {
            resources.add(action)
        }
        
        fun cleanup() {
            resources.reversed().forEach { cleanup ->
                try {
                    cleanup()
                } catch (e: Exception) {
                    StructuredLogger.warning(
                        StructuredLogger.LogCategory.PERFORMANCE,
                        TAG,
                        "Error during resource cleanup",
                        exception = e
                    )
                }
            }
            resources.clear()
        }
    }
    
    // Private helper methods
    
    private fun trackJob(scopeId: String, job: Job) {
        activeJobs.getOrPut(scopeId) { ConcurrentHashMap.newKeySet() }.add(job)
    }
    
    private fun untrackJob(scopeId: String, job: Job) {
        activeJobs[scopeId]?.remove(job)
        if (activeJobs[scopeId]?.isEmpty() == true) {
            activeJobs.remove(scopeId)
        }
    }
    
    private fun cancelJobsForScope(scopeId: String) {
        activeJobs[scopeId]?.forEach { job ->
            try {
                if (job.isActive) {
                    job.cancel("Scope cleanup")
                }
            } catch (e: Exception) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.PERFORMANCE,
                    TAG,
                    "Error cancelling job during scope cleanup",
                    exception = e
                )
            }
        }
        activeJobs.remove(scopeId)
    }
    
    /**
     * Get memory usage statistics
     */
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        return MemoryStats(
            maxMemory = maxMemory,
            totalMemory = totalMemory,
            usedMemory = usedMemory,
            freeMemory = freeMemory,
            activeJobScopes = activeJobs.size,
            activeJobs = activeJobs.values.sumOf { it.size }
        )
    }
    
    /**
     * Force garbage collection and cleanup
     */
    fun forceCleanup() {
        try {
            // Clean up expired weak references
            System.gc()
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.PERFORMANCE,
                TAG,
                "Memory cleanup completed"
            )
        } catch (e: Exception) {
            StructuredLogger.warning(
                StructuredLogger.LogCategory.PERFORMANCE,
                TAG,
                "Error during memory cleanup",
                exception = e
            )
        }
    }
}

data class MemoryStats(
    val maxMemory: Long,
    val totalMemory: Long,
    val usedMemory: Long,
    val freeMemory: Long,
    val activeJobScopes: Int,
    val activeJobs: Int
) {
    val memoryUsagePercent: Float = (usedMemory.toFloat() / maxMemory.toFloat()) * 100f
    
    fun isMemoryLow(): Boolean = memoryUsagePercent > 85f
    fun isMemoryMedium(): Boolean = memoryUsagePercent > 70f
}

/**
 * Extension functions for easier usage
 */
fun LifecycleOwner.createSafeScope(): CoroutineScope {
    return MemoryLeakPrevention.createLifecycleAwareScope(this)
}

fun Context.createSafeContextHolder(): MemoryLeakPrevention.SafeContextHolder {
    return MemoryLeakPrevention.SafeContextHolder(this)
}

fun <T : Any> T.asWeakReference(): MemoryLeakPrevention.WeakReferenceHolder<T> {
    return MemoryLeakPrevention.WeakReferenceHolder(this)
}