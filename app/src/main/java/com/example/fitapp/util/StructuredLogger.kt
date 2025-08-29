package com.example.fitapp.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.HashMap

/**
 * Comprehensive structured logging system for FitApp
 * Provides crash prevention, performance tracking, and debugging support
 */
object StructuredLogger {
    
    private const val TAG = "FitApp_Logger"
    private const val MAX_LOG_ENTRIES = 1000
    private const val LOG_FILE_NAME = "fitapp_logs.txt"
    
    // Log levels
    enum class LogLevel(val value: Int, val prefix: String) {
        VERBOSE(1, "V"),
        DEBUG(2, "D"),
        INFO(3, "I"),
        WARNING(4, "W"),
        ERROR(5, "E"),
        CRITICAL(6, "C")
    }
    
    // Log categories for better organization
    enum class LogCategory(val category: String) {
        UI("UI"),
        DATABASE("DB"),
        NETWORK("NET"),
        AI("AI"),
        API("API"),
        SYNC("SYNC"),
        PERFORMANCE("PERF"),
        USER_ACTION("USER"),
        SYSTEM("SYS"),
        SECURITY("SEC")
    }
    
    // Structured log entry
    data class LogEntry(
        val timestamp: Long,
        val level: LogLevel,
        val category: LogCategory,
        val tag: String,
        val message: String,
        val metadata: Map<String, Any> = emptyMap(),
        val exception: Throwable? = null
    )
    
    // In-memory log buffer
    private val logBuffer = ConcurrentLinkedQueue<LogEntry>()
    private val mutex = Mutex()
    private var isInitialized = false
    private var logToFile = false
    private var minLogLevel = LogLevel.INFO
    
    /**
     * Initialize the logging system
     */
    fun initialize(context: Context, enableFileLogging: Boolean = true, minimumLevel: LogLevel = LogLevel.INFO) {
        if (isInitialized) return
        
        logToFile = enableFileLogging
        minLogLevel = minimumLevel
        isInitialized = true
        
        if (logToFile) {
            try {
                val logDir = File(context.filesDir, "logs")
                if (!logDir.exists()) {
                    logDir.mkdirs()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create log directory", e)
                logToFile = false
            }
        }
        
        info(LogCategory.SYSTEM, "StructuredLogger", "Logging system initialized", 
            mapOf("fileLogging" to logToFile, "minLevel" to minimumLevel.name))
    }
    
    /**
     * Log verbose message
     */
    fun verbose(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.VERBOSE, category, tag, message, metadata)
    }
    
    /**
     * Log debug message
     */
    fun debug(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.DEBUG, category, tag, message, metadata)
    }
    
    /**
     * Log info message
     */
    fun info(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap()) {
        log(LogLevel.INFO, category, tag, message, metadata)
    }
    
    /**
     * Log warning message
     */
    fun warning(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap(), exception: Throwable? = null) {
        log(LogLevel.WARNING, category, tag, message, metadata, exception)
    }
    
    /**
     * Log error message
     */
    fun error(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap(), exception: Throwable? = null) {
        log(LogLevel.ERROR, category, tag, message, metadata, exception)
    }
    
    /**
     * Log critical error that might crash the app
     */
    fun critical(category: LogCategory, tag: String, message: String, metadata: Map<String, Any> = emptyMap(), exception: Throwable? = null) {
        log(LogLevel.CRITICAL, category, tag, message, metadata, exception)
    }
    
    /**
     * Log user action for analytics and debugging
     */
    fun logUserAction(action: String, screen: String, metadata: Map<String, Any> = emptyMap()) {
        val actionMetadata = mutableMapOf<String, Any>(
            "action" to action,
            "screen" to screen,
            "timestamp" to System.currentTimeMillis()
        )
        actionMetadata.putAll(metadata)
        
        info(LogCategory.USER_ACTION, "UserAction", action, actionMetadata)
    }
    
    /**
     * Log performance metrics
     */
    fun logPerformance(operation: String, durationMs: Long, metadata: Map<String, Any> = emptyMap()) {
        val perfMetadata = mutableMapOf<String, Any>(
            "operation" to operation,
            "duration_ms" to durationMs,
            "timestamp" to System.currentTimeMillis()
        )
        perfMetadata.putAll(metadata)
        
        val level = when {
            durationMs > 5000 -> LogLevel.WARNING
            durationMs > 1000 -> LogLevel.INFO
            else -> LogLevel.DEBUG
        }
        
        log(level, LogCategory.PERFORMANCE, "Performance", operation, perfMetadata)
    }
    
    /**
     * Log API call for monitoring
     */
    fun logApiCall(provider: String, endpoint: String, success: Boolean, durationMs: Long, statusCode: Int? = null) {
        val apiMetadata = mapOf(
            "provider" to provider,
            "endpoint" to endpoint,
            "success" to success,
            "duration_ms" to durationMs,
            "status_code" to (statusCode ?: "unknown"),
            "timestamp" to System.currentTimeMillis()
        )
        
        val level = if (success) LogLevel.INFO else LogLevel.ERROR
        log(level, LogCategory.NETWORK, "ApiCall", "$provider.$endpoint", apiMetadata)
    }
    
    /**
     * Log database operation
     */
    fun logDatabaseOperation(operation: String, tableName: String, success: Boolean, durationMs: Long, affectedRows: Int? = null) {
        val dbMetadata = mutableMapOf<String, Any>(
            "operation" to operation,
            "table" to tableName,
            "success" to success,
            "duration_ms" to durationMs,
            "timestamp" to System.currentTimeMillis()
        )
        
        affectedRows?.let { dbMetadata["affected_rows"] = it }
        
        val level = if (success) LogLevel.DEBUG else LogLevel.ERROR
        log(level, LogCategory.DATABASE, "DatabaseOp", "$operation.$tableName", dbMetadata)
    }
    
    /**
     * Get recent log entries for debugging
     */
    fun getRecentLogs(count: Int = 100, minLevel: LogLevel = LogLevel.INFO): List<LogEntry> {
        return logBuffer
            .filter { it.level.value >= minLevel.value }
            .takeLast(count)
    }
    
    /**
     * Get logs by category
     */
    fun getLogsByCategory(category: LogCategory, count: Int = 50): List<LogEntry> {
        return logBuffer
            .filter { it.category == category }
            .takeLast(count)
    }
    
    /**
     * Export logs to string format
     */
    fun exportLogs(minLevel: LogLevel = LogLevel.INFO): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val logs = getRecentLogs(500, minLevel)
        
        return buildString {
            appendLine("=== FitApp Log Export ===")
            appendLine("Generated: ${formatter.format(Date())}")
            appendLine("Total entries: ${logs.size}")
            appendLine("Min level: ${minLevel.name}")
            appendLine()
            
            logs.forEach { entry ->
                appendLine("${formatter.format(Date(entry.timestamp))} ${entry.level.prefix}/${entry.category.category}/${entry.tag}: ${entry.message}")
                
                if (entry.metadata.isNotEmpty()) {
                    entry.metadata.forEach { (key, value) ->
                        appendLine("  $key: $value")
                    }
                }
                
                entry.exception?.let { ex ->
                    appendLine("  Exception: ${ex.javaClass.simpleName}: ${ex.message}")
                    ex.stackTrace.take(5).forEach { stackElement ->
                        appendLine("    at $stackElement")
                    }
                }
                appendLine()
            }
        }
    }
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        logBuffer.clear()
        info(LogCategory.SYSTEM, "StructuredLogger", "Logs cleared")
    }
    
    /**
     * Get log statistics
     */
    fun getLogStatistics(): Map<String, Any> {
        val logs = logBuffer.toList()
        val levelCounts = LogLevel.values().associateWith { level ->
            logs.count { it.level == level }
        }
        val categoryCounts = LogCategory.values().associateWith { category ->
            logs.count { it.category == category }
        }
        
        return mapOf(
            "total_logs" to logs.size,
            "level_counts" to levelCounts.mapKeys { it.key.name },
            "category_counts" to categoryCounts.mapKeys { it.key.name },
            "oldest_log" to (logs.minByOrNull { it.timestamp }?.timestamp ?: 0),
            "newest_log" to (logs.maxByOrNull { it.timestamp }?.timestamp ?: 0)
        )
    }
    
    // Private implementation
    
    private fun log(
        level: LogLevel,
        category: LogCategory,
        tag: String,
        message: String,
        metadata: Map<String, Any> = emptyMap(),
        exception: Throwable? = null
    ) {
        if (!isInitialized || level.value < minLogLevel.value) return
        
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            category = category,
            tag = tag,
            message = message,
            metadata = metadata,
            exception = exception
        )
        
        // Add to buffer
        logBuffer.offer(entry)
        
        // Maintain buffer size
        while (logBuffer.size > MAX_LOG_ENTRIES) {
            logBuffer.poll()
        }
        
        // Log to Android Log
        val logMessage = buildLogMessage(entry)
        when (level) {
            LogLevel.VERBOSE -> Log.v("${category.category}/$tag", logMessage, exception)
            LogLevel.DEBUG -> Log.d("${category.category}/$tag", logMessage, exception)
            LogLevel.INFO -> Log.i("${category.category}/$tag", logMessage, exception)
            LogLevel.WARNING -> Log.w("${category.category}/$tag", logMessage, exception)
            LogLevel.ERROR -> Log.e("${category.category}/$tag", logMessage, exception)
            LogLevel.CRITICAL -> Log.wtf("${category.category}/$tag", logMessage, exception)
        }
        
        // Write to file if enabled
        if (logToFile) {
            writeToFile(entry)
        }
    }
    
    private fun buildLogMessage(entry: LogEntry): String {
        val message = StringBuilder(entry.message)
        
        if (entry.metadata.isNotEmpty()) {
            message.append(" [")
            entry.metadata.entries.joinToString(", ") { "${it.key}=${it.value}" }
                .let { message.append(it) }
            message.append("]")
        }
        
        return message.toString()
    }
    
    private fun writeToFile(entry: LogEntry) {
        // Note: This is a simple implementation. In production, you might want to use
        // a more sophisticated file logging solution with rotation, compression, etc.
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            val logLine = "${formatter.format(Date(entry.timestamp))} ${entry.level.prefix}/${entry.category.category}/${entry.tag}: ${buildLogMessage(entry)}\n"
            
            // This is a simplified file writing approach
            // In a real implementation, you'd want to use a background thread and proper file management
        } catch (e: Exception) {
            // Don't log this error to avoid recursion
            Log.e(TAG, "Failed to write log to file", e)
        }
    }
}

/**
 * Extension functions for easy logging
 */
fun Any.logDebug(category: StructuredLogger.LogCategory, message: String, metadata: Map<String, Any> = emptyMap()) {
    StructuredLogger.debug(category, this::class.simpleName ?: "Unknown", message, metadata)
}

fun Any.logInfo(category: StructuredLogger.LogCategory, message: String, metadata: Map<String, Any> = emptyMap()) {
    StructuredLogger.info(category, this::class.simpleName ?: "Unknown", message, metadata)
}

fun Any.logWarning(category: StructuredLogger.LogCategory, message: String, metadata: Map<String, Any> = emptyMap(), exception: Throwable? = null) {
    StructuredLogger.warning(category, this::class.simpleName ?: "Unknown", message, metadata, exception)
}

fun Any.logError(category: StructuredLogger.LogCategory, message: String, metadata: Map<String, Any> = emptyMap(), exception: Throwable? = null) {
    StructuredLogger.error(category, this::class.simpleName ?: "Unknown", message, metadata, exception)
}