package com.example.fitapp.ml

/**
 * Sealed result type for ML operations that provides graceful error handling and degraded modes.
 * 
 * This pattern allows ML operations to:
 * - Return successful results with data
 * - Handle errors gracefully with fallback options
 * - Operate in degraded modes when resources are limited
 * - Provide clear error information for debugging
 */
sealed class MLResult<out T> {
    
    /**
     * Successful ML operation with result data
     */
    data class Success<T>(val data: T) : MLResult<T>()
    
    /**
     * ML operation failed with error information
     * @param exception The underlying exception that caused the failure
     * @param fallbackAvailable Whether a fallback operation is possible
     * @param message Optional human-readable error message
     */
    data class Error(
        val exception: Throwable,
        val fallbackAvailable: Boolean,
        val message: String? = null
    ) : MLResult<Nothing>()
    
    /**
     * ML operation running in degraded mode (e.g., low memory, thermal throttling)
     * @param exception The exception that triggered degraded mode
     * @param degradedResult Optional partial result available in degraded mode
     * @param message Description of the degradation
     */
    data class Degraded<T>(
        val exception: Throwable,
        val degradedResult: T?,
        val message: String
    ) : MLResult<T>()
    
    /**
     * Check if the result represents a successful operation
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Check if the result represents an error
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Check if the result represents degraded operation
     */
    val isDegraded: Boolean
        get() = this is Degraded
    
    /**
     * Get the data if successful, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Degraded -> degradedResult
        is Error -> null
    }
    
    /**
     * Get the data if successful, or return default value
     */
    fun getOrDefault(default: @UnsafeVariance T): @UnsafeVariance T = when (this) {
        is Success -> data
        is Degraded -> degradedResult ?: default
        is Error -> default
    }
    
    /**
     * Get the exception if this is an error or degraded result
     */
    fun getExceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        is Degraded -> exception
        is Success -> null
    }
    
    /**
     * Transform the successful result using the provided function
     */
    inline fun <R> map(transform: (T) -> R): MLResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Degraded -> Degraded(
            exception = exception,
            degradedResult = degradedResult?.let(transform),
            message = message
        )
    }
    
    /**
     * Flat map transformation for chaining ML operations
     */
    inline fun <R> flatMap(transform: (T) -> MLResult<R>): MLResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Degraded -> {
            if (degradedResult != null) {
                when (val result = transform(degradedResult)) {
                    is Success -> Degraded(exception, result.data, message)
                    is Error -> result
                    is Degraded -> result
                }
            } else {
                Degraded(exception, null, message)
            }
        }
    }
    
    /**
     * Execute an action only if the result is successful
     */
    inline fun onSuccess(action: (T) -> Unit): MLResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Execute an action only if the result is an error
     */
    inline fun onError(action: (Throwable, Boolean) -> Unit): MLResult<T> {
        if (this is Error) action(exception, fallbackAvailable)
        return this
    }
    
    /**
     * Execute an action only if the result is degraded
     */
    inline fun onDegraded(action: (Throwable, T?, String) -> Unit): MLResult<T> {
        if (this is Degraded) action(exception, degradedResult, message)
        return this
    }
    
    /**
     * Fold the result into a single value using the provided functions
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Throwable, Boolean) -> R,
        onDegraded: (Throwable, T?, String) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(exception, fallbackAvailable)
        is Degraded -> onDegraded(exception, degradedResult, message)
    }
    
    companion object {
        /**
         * Create a success result
         */
        fun <T> success(data: T): MLResult<T> = Success(data)
        
        /**
         * Create an error result
         */
        fun error(
            exception: Throwable,
            fallbackAvailable: Boolean = false,
            message: String? = null
        ): MLResult<Nothing> = Error(exception, fallbackAvailable, message)
        
        /**
         * Create a degraded result
         */
        fun <T> degraded(
            exception: Throwable,
            degradedResult: T? = null,
            message: String
        ): MLResult<T> = Degraded(exception, degradedResult, message)
        
        /**
         * Wrap a potentially throwing operation in an MLResult
         */
        inline fun <T> catching(
            fallbackAvailable: Boolean = false,
            operation: () -> T
        ): MLResult<T> = try {
            Success(operation())
        } catch (outOfMemory: OutOfMemoryError) {
            Degraded(
                exception = outOfMemory,
                degradedResult = null,
                message = "Out of memory - switched to degraded mode"
            )
        } catch (e: Exception) {
            Error(e, fallbackAvailable)
        }
        
        /**
         * Combine multiple ML results into a single result
         * Returns success only if all results are successful
         */
        fun <T> combine(results: List<MLResult<T>>): MLResult<List<T>> {
            val successes = mutableListOf<T>()
            val errors = mutableListOf<Throwable>()
            val degraded = mutableListOf<Pair<Throwable, String>>()
            
            for (result in results) {
                when (result) {
                    is Success -> successes.add(result.data)
                    is Error -> errors.add(result.exception)
                    is Degraded -> {
                        result.degradedResult?.let { successes.add(it) }
                        degraded.add(result.exception to result.message)
                    }
                }
            }
            
            return when {
                errors.isNotEmpty() -> Error(
                    exception = RuntimeException("Multiple errors: ${errors.map { it.message }}"),
                    fallbackAvailable = true
                )
                degraded.isNotEmpty() -> Degraded(
                    exception = degraded.first().first,
                    degradedResult = successes.takeIf { it.isNotEmpty() },
                    message = "Some operations degraded: ${degraded.map { it.second }}"
                )
                else -> Success(successes)
            }
        }
    }
}