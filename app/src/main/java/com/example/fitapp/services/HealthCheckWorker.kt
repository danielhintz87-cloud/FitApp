package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.core.health.ApiHealthRegistry
import com.example.fitapp.core.health.GeminiHealthChecker
import com.example.fitapp.core.health.HealthStatusRepository
import com.example.fitapp.core.threading.DefaultDispatcherProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.util.StructuredLogger
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Simplified Worker for health checks that doesn't rely on Hilt
 * This provides a working implementation while avoiding DI compilation issues
 */
class HealthCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            StructuredLogger.info(
                StructuredLogger.LogCategory.HEALTH_CHECK,
                TAG,
                "Starting API health checks"
            )

            // Create dependencies manually (in production, use proper DI)
            val database = AppDatabase.get(applicationContext)
            val dispatchers = DefaultDispatcherProvider()
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
            
            val geminiHealthChecker = GeminiHealthChecker(applicationContext, httpClient, dispatchers)
            val apiHealthRegistry = ApiHealthRegistry(geminiHealthChecker)
            val healthStatusRepository = HealthStatusRepository(database, dispatchers)
            
            // Perform health checks
            val allStatus = apiHealthRegistry.checkAllHealth()
            
            // Persist results
            healthStatusRepository.saveHealthStatuses(allStatus.values.toList())

            val healthyCount = allStatus.values.count { it.isHealthy }
            val totalCount = allStatus.size

            StructuredLogger.info(
                StructuredLogger.LogCategory.HEALTH_CHECK,
                TAG,
                "Health check completed",
                mapOf(
                    "healthy_count" to healthyCount,
                    "total_count" to totalCount,
                    "health_percentage" to if (totalCount > 0) (healthyCount * 100.0 / totalCount) else 0.0
                )
            )

            Result.success()
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.HEALTH_CHECK,
                TAG,
                "Health check failed",
                mapOf("error" to (e.message ?: "Unknown error")),
                e
            )
            
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "HealthCheckWorker"
        private const val WORK_NAME = "health_check_periodic"
        private const val IMMEDIATE_WORK_NAME = "health_check_immediate"

        /**
         * Schedule periodic health checks
         */
        fun schedulePeriodicHealthCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<HealthCheckWorker>(
                1, TimeUnit.HOURS // Check every hour
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )

            StructuredLogger.info(
                StructuredLogger.LogCategory.HEALTH_CHECK,
                TAG,
                "Periodic health checks scheduled"
            )
        }

        /**
         * Trigger immediate health check
         */
        fun triggerImmediateHealthCheck(context: Context) {
              val constraints = Constraints.Builder().build()

            val immediateRequest = OneTimeWorkRequestBuilder<HealthCheckWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )

            StructuredLogger.info(
                StructuredLogger.LogCategory.HEALTH_CHECK,
                TAG,
                "Immediate health check triggered"
            )
        }

        /**
         * Cancel all health check work
         */
        fun cancelHealthChecks(context: Context) {
            WorkManager.getInstance(context).apply {
                cancelUniqueWork(WORK_NAME)
                cancelUniqueWork(IMMEDIATE_WORK_NAME)
            }

            StructuredLogger.info(
                StructuredLogger.LogCategory.HEALTH_CHECK,
                TAG,
                "Health checks cancelled"
            )
        }
    }
}