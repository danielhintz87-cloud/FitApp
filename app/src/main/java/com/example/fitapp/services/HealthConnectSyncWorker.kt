package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.network.healthconnect.HealthConnectManager
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class HealthConnectSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val healthConnectManager = HealthConnectManager(context)
    private val database = AppDatabase.get(context)
    
    override suspend fun doWork(): Result {
        return try {
            if (!healthConnectManager.isAvailable()) {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Health Connect not available, skipping sync"
                )
                return Result.success()
            }
            
            if (!healthConnectManager.hasPermissions()) {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Health Connect permissions not granted, skipping sync"
                )
                return Result.success()
            }
            
            syncHealthData()
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Health Connect sync completed successfully"
            )
            
            Result.success()
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Health Connect sync failed: ${e.message}",
                mapOf("error" to (e.message ?: "Unknown error"))
            )
            Result.retry()
        }
    }
    
    private suspend fun syncHealthData() = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val startDate = today.minusDays(7) // Sync last 7 days
        
        try {
            // Sync daily data for the past week
            val healthDataList = healthConnectManager.syncDateRange(startDate, today)
            
            healthDataList.forEach { healthData ->
                // Process steps data
                if (healthData.steps > 0) {
                    saveStepsData(healthData)
                }
                
                // Process exercise sessions
                if (healthData.exerciseSessions.isNotEmpty()) {
                    saveExerciseData(healthData)
                }
                
                // Process calories
                if (healthData.activeCalories > 0) {
                    saveCalorieData(healthData)
                }
            }
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Synced ${healthDataList.size} days of health data"
            )
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to sync health data",
                mapOf("error" to (e.message ?: "Unknown error"))
            )
            throw e
        }
    }
    
    private suspend fun saveStepsData(healthData: com.example.fitapp.network.healthconnect.HealthSyncData) {
        // TODO: Save steps data to database
        // This would require creating a steps table or integrating with existing tracking
        StructuredLogger.info(
            StructuredLogger.LogCategory.SYNC,
            TAG,
            "Steps data for ${healthData.date}: ${healthData.steps} steps"
        )
    }
    
    private suspend fun saveExerciseData(healthData: com.example.fitapp.network.healthconnect.HealthSyncData) {
        // TODO: Save exercise data to database
        // This would integrate with the existing workout tracking system
        healthData.exerciseSessions.forEach { exercise ->
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Exercise data for ${healthData.date}: ${exercise.title} (${exercise.durationMinutes} min)"
            )
        }
    }
    
    private suspend fun saveCalorieData(healthData: com.example.fitapp.network.healthconnect.HealthSyncData) {
        // TODO: Save calorie data to database
        // This would integrate with nutrition tracking
        StructuredLogger.info(
            StructuredLogger.LogCategory.SYNC,
            TAG,
            "Calorie data for ${healthData.date}: ${healthData.activeCalories} kcal"
        )
    }
    
    companion object {
        private const val TAG = "HealthConnectSyncWorker"
        private const val SYNC_WORK_NAME = "health_connect_sync"
        private const val PERIODIC_SYNC_WORK_NAME = "health_connect_periodic_sync"
        
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val periodicSyncRequest = PeriodicWorkRequestBuilder<HealthConnectSyncWorker>(
                15, TimeUnit.MINUTES // Sync every 15 minutes
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Periodic Health Connect sync scheduled"
            )
        }
        
        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            val immediateSyncRequest = OneTimeWorkRequestBuilder<HealthConnectSyncWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateSyncRequest
            )
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Immediate Health Connect sync triggered"
            )
        }
        
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Health Connect sync cancelled"
            )
        }
    }
}