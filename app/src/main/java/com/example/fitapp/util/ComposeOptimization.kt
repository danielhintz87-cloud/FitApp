package com.example.fitapp.util

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Compose performance optimization utilities
 * Helps prevent unnecessary recompositions and memory leaks
 */

/**
 * Remember a value that is stable across recompositions
 * Useful for expensive computations or object creation
 */
@Composable
fun <T> rememberStable(
    vararg keys: Any?,
    calculation: () -> T,
): T {
    return remember(*keys) { calculation() }
}

/**
 * Remember a mutable state that is lifecycle-aware
 * Automatically clears state when lifecycle is destroyed
 */
@Composable
fun <T> rememberLifecycleAwareState(
    initialValue: T,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
): MutableState<T> {
    val state = remember { mutableStateOf(initialValue) }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    // Reset to initial value to prevent memory leaks
                    state.value = initialValue
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return state
}

/**
 * Remember a coroutine scope that is automatically cancelled when the composable is disposed
 */
@Composable
fun rememberSafeCoroutineScope(): CoroutineScope {
    val scope = rememberCoroutineScope()
    val jobs = remember { mutableListOf<Job>() }

    DisposableEffect(Unit) {
        onDispose {
            jobs.forEach { it.cancel() }
            jobs.clear()
        }
    }

    // Return the original scope, jobs will be tracked separately
    return scope
}

/**
 * Debounced state for user input to prevent excessive recompositions
 */
@Composable
fun rememberDebouncedState(
    initialValue: String,
    delayMs: Long = 300L,
): Pair<String, (String) -> Unit> {
    var currentValue by remember { mutableStateOf(initialValue) }
    var debouncedValue by remember { mutableStateOf(initialValue) }
    val scope = rememberCoroutineScope()

    val updateValue: (String) -> Unit =
        remember {
            { newValue: String ->
                currentValue = newValue
                scope.launch {
                    delay(delayMs)
                    if (currentValue == newValue) {
                        debouncedValue = newValue
                    }
                }
                Unit // Explicit return Unit
            }
        }

    return debouncedValue to updateValue
}

/**
 * Throttled state for high-frequency updates
 */
@Composable
fun rememberThrottledState(
    initialValue: String,
    intervalMs: Long = 100L,
): Pair<String, (String) -> Unit> {
    var value by remember { mutableStateOf(initialValue) }
    var lastUpdateTime by remember { mutableLongStateOf(0L) }

    val updateValue =
        remember {
            { newValue: String ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= intervalMs) {
                    value = newValue
                    lastUpdateTime = currentTime
                }
            }
        }

    return value to updateValue
}

/**
 * Remember a derivedStateOf value for computed properties
 * Automatically optimizes recompositions when dependencies change
 */
@Composable
fun <T> rememberDerivedState(
    vararg keys: Any?,
    calculation: () -> T,
): State<T> {
    return remember(*keys) {
        derivedStateOf { calculation() }
    }
}

/**
 * Remember an immutable list to prevent unnecessary recompositions
 */
@Composable
fun <T> rememberImmutableList(
    vararg keys: Any?,
    calculation: () -> List<T>,
): List<T> {
    return remember(*keys) {
        calculation().toList() // Create immutable copy
    }
}

/**
 * Remember an immutable map to prevent unnecessary recompositions
 */
@Composable
fun <K, V> rememberImmutableMap(
    vararg keys: Any?,
    calculation: () -> Map<K, V>,
): Map<K, V> {
    return remember(*keys) {
        calculation().toMap() // Create immutable copy
    }
}

/**
 * Stable wrapper for lambda functions to prevent recomposition
 */
@Stable
class StableCallback<T>(val callback: (T) -> Unit)

/**
 * Remember a stable callback that doesn't cause recomposition
 */
@Composable
fun <T> rememberStableCallback(callback: (T) -> Unit): StableCallback<T> {
    return remember { StableCallback(callback) }
}

/**
 * Performance-optimized lazy loading state
 */
@Composable
fun <T> rememberLazyState(
    key: Any? = null,
    loader: suspend () -> T,
): State<T?> {
    var state by remember(key) { mutableStateOf<T?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key) {
        scope.launch {
            try {
                state = loader()
            } catch (e: Exception) {
                PerformanceMonitor.recordOperationTime("lazy_load_failed", System.currentTimeMillis())
                android.util.Log.w("ComposeOptimization", "Lazy load failed", e)
            }
        }
    }

    return remember { derivedStateOf { state } }
}

/**
 * Auto-clearing state for temporary UI states
 */
@Composable
fun rememberAutoClearing(
    initialValue: String = "",
    clearAfterMs: Long = 5000L,
): MutableState<String> {
    val state = remember { mutableStateOf(initialValue) }
    val scope = rememberCoroutineScope()

    // Auto-clear after specified time
    LaunchedEffect(state.value) {
        if (state.value.isNotEmpty()) {
            scope.launch {
                delay(clearAfterMs)
                state.value = ""
            }
        }
    }

    return state
}

/**
 * Memory-safe image state for loading images without memory leaks
 */
@Composable
fun rememberImageState(imageUrl: String?): State<String?> {
    var currentUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUrl) {
        currentUrl = imageUrl
    }

    DisposableEffect(Unit) {
        onDispose {
            currentUrl = null // Clear reference on dispose
        }
    }

    return remember { derivedStateOf { currentUrl } }
}

/**
 * Batch state updates to reduce recompositions
 */
class BatchStateUpdater {
    private val updates = mutableMapOf<String, () -> Unit>()

    fun addUpdate(
        key: String,
        update: () -> Unit,
    ) {
        updates[key] = update
    }

    fun executeAll() {
        updates.values.forEach { it() }
        updates.clear()
    }
}

/**
 * Remember a batch state updater for grouping multiple state changes
 */
@Composable
fun rememberBatchStateUpdater(): BatchStateUpdater {
    return remember { BatchStateUpdater() }
}

/**
 * Optimized list state for large datasets
 */
@Composable
fun <T> rememberOptimizedListState(
    list: List<T>,
    maxVisible: Int = 50,
): State<List<T>> {
    return remember(list) {
        derivedStateOf {
            if (list.size > maxVisible) {
                list.take(maxVisible) // Only keep visible items
            } else {
                list
            }
        }
    }
}
