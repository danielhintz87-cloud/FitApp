package com.example.fitapp.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.fitapp.core.health.ApiHealthRegistry
import com.example.fitapp.core.health.HealthStatusRepository
import com.example.fitapp.util.StructuredLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker to perform periodic health checks on all API providers
 */
@HiltWorker
class HealthCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiHealthRegistry: ApiHealthRegistry,
    private val healthStatusRepository: HealthStatusRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            StructuredLogger.info(
                StructuredLogger.LogCategory.HEALTH_CHECK,
                TAG,
                "Starting API health checks"
            )

            val allStatus = apiHealthRegistry.checkAllHealth()
            
            // Persist results to database
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
                .setRequiredNetworkType(NetworkType.CONNECTED)
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
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

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