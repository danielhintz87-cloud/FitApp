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
                
                // Process heart rate data (for each day)
                syncHeartRateData(healthData.date)
                
                // Process sleep data (for each day)
                syncSleepData(healthData.date)
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
        try {
            val stepsDao = database.healthStepsDao()
            val dateStr = healthData.date.toString()
            
            // Check if we already have steps data for this date from Health Connect
            val existing = stepsDao.getByDateAndSource(dateStr, "health_connect")
            
            if (existing != null) {
                // Update existing entry if steps count is different
                if (existing.steps != healthData.steps) {
                    val updated = existing.copy(
                        steps = healthData.steps,
                        lastModified = System.currentTimeMillis() / 1000
                    )
                    stepsDao.update(updated)
                    
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYNC,
                        TAG,
                        "Updated steps data for $dateStr: ${existing.steps} -> ${healthData.steps} steps"
                    )
                }
            } else {
                // Insert new steps entry
                val stepsEntity = com.example.fitapp.data.db.HealthStepsEntity(
                    date = dateStr,
                    steps = healthData.steps,
                    source = "health_connect",
                    syncedAt = System.currentTimeMillis() / 1000,
                    lastModified = System.currentTimeMillis() / 1000
                )
                stepsDao.insert(stepsEntity)
                
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Saved new steps data for $dateStr: ${healthData.steps} steps"
                )
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to save steps data for ${healthData.date}",
                mapOf(
                    "error" to (e.message ?: "Unknown error"),
                    "steps" to healthData.steps.toString()
                )
            )
        }
    }
    
    private suspend fun saveExerciseData(healthData: com.example.fitapp.network.healthconnect.HealthSyncData) {
        try {
            val exerciseDao = database.healthExerciseSessionDao()
            val dateStr = healthData.date.toString()
            
            healthData.exerciseSessions.forEach { exercise ->
                // Create a unique session ID based on date, title, and duration
                val sessionId = "hc_${dateStr}_${exercise.title.hashCode()}_${exercise.durationMinutes}"
                
                // Check if this exercise session already exists
                val existing = exerciseDao.getBySessionId(sessionId)
                
                if (existing == null) {
                    // Insert new exercise session
                    val exerciseEntity = com.example.fitapp.data.db.HealthExerciseSessionEntity(
                        sessionId = sessionId,
                        date = dateStr,
                        startTime = healthData.date.atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond(),
                        endTime = healthData.date.atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond() + (exercise.durationMinutes * 60),
                        durationMinutes = exercise.durationMinutes,
                        exerciseType = exercise.exerciseType,
                        title = exercise.title,
                        source = "health_connect",
                        syncedAt = System.currentTimeMillis() / 1000,
                        lastModified = System.currentTimeMillis() / 1000
                    )
                    exerciseDao.insert(exerciseEntity)
                    
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYNC,
                        TAG,
                        "Saved new exercise session for $dateStr: ${exercise.title} (${exercise.durationMinutes} min)"
                    )
                } else {
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYNC,
                        TAG,
                        "Exercise session already exists for $dateStr: ${exercise.title}"
                    )
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to save exercise data for ${healthData.date}",
                mapOf(
                    "error" to (e.message ?: "Unknown error"),
                    "sessionCount" to healthData.exerciseSessions.size.toString()
                )
            )
        }
    }
    
    private suspend fun saveCalorieData(healthData: com.example.fitapp.network.healthconnect.HealthSyncData) {
        try {
            val calorieDao = database.healthCalorieDao()
            val dateStr = healthData.date.toString()
            
            // Save active calories data
            if (healthData.activeCalories > 0) {
                val existing = calorieDao.getByDateTypeAndSource(dateStr, "active", "health_connect")
                
                if (existing != null) {
                    // Update existing entry if calories are different
                    if (existing.calories != healthData.activeCalories) {
                        val updated = existing.copy(
                            calories = healthData.activeCalories,
                            lastModified = System.currentTimeMillis() / 1000
                        )
                        calorieDao.update(updated)
                        
                        StructuredLogger.info(
                            StructuredLogger.LogCategory.SYNC,
                            TAG,
                            "Updated active calories for $dateStr: ${existing.calories} -> ${healthData.activeCalories} kcal"
                        )
                    }
                } else {
                    // Insert new calorie entry
                    val calorieEntity = com.example.fitapp.data.db.HealthCalorieEntity(
                        date = dateStr,
                        calories = healthData.activeCalories,
                        calorieType = "active",
                        source = "health_connect",
                        syncedAt = System.currentTimeMillis() / 1000,
                        lastModified = System.currentTimeMillis() / 1000
                    )
                    calorieDao.insert(calorieEntity)
                    
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYNC,
                        TAG,
                        "Saved new active calories for $dateStr: ${healthData.activeCalories} kcal"
                    )
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to save calorie data for ${healthData.date}",
                mapOf(
                    "error" to (e.message ?: "Unknown error"),
                    "activeCalories" to healthData.activeCalories.toString()
                )
            )
        }
    }
    
    private suspend fun syncHeartRateData(date: LocalDate) {
        try {
            val heartRateDao = database.healthHeartRateDao()
            val dateStr = date.toString()
            
            // Create time range for the specific date
            val startTime = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
            val endTime = date.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
            
            // Get heart rate data from Health Connect
            val heartRateDataList = healthConnectManager.syncHeartRateData(startTime, endTime)
            
            heartRateDataList.forEach { heartRateData ->
                // Check if this heart rate reading already exists
                val existing = heartRateDao.getByTimeRange(
                    heartRateData.timestamp - 60, // 1 minute tolerance
                    heartRateData.timestamp + 60
                ).firstOrNull { it.heartRate == heartRateData.heartRate }
                
                if (existing == null) {
                    // Insert new heart rate reading
                    val heartRateEntity = com.example.fitapp.data.db.HealthHeartRateEntity(
                        timestamp = heartRateData.timestamp,
                        date = dateStr,
                        heartRate = heartRateData.heartRate,
                        source = "health_connect",
                        syncedAt = System.currentTimeMillis() / 1000
                    )
                    heartRateDao.insert(heartRateEntity)
                }
            }
            
            if (heartRateDataList.isNotEmpty()) {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Synced ${heartRateDataList.size} heart rate readings for $dateStr"
                )
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to sync heart rate data for $date",
                mapOf("error" to (e.message ?: "Unknown error"))
            )
        }
    }
    
    private suspend fun syncSleepData(date: LocalDate) {
        try {
            val sleepDao = database.healthSleepDao()
            val dateStr = date.toString()
            
            // Create time range for the specific date (including previous night)
            val startTime = date.minusDays(1).atTime(18, 0).atZone(java.time.ZoneId.systemDefault()).toInstant()
            val endTime = date.plusDays(1).atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant()
            
            // Get sleep data from Health Connect
            val sleepDataList = healthConnectManager.syncSleepData(startTime, endTime)
            
            sleepDataList.forEach { sleepData ->
                // Check if this sleep session already exists
                val existing = sleepDao.getByTimeRange(
                    sleepData.startTime - 300, // 5 minute tolerance
                    sleepData.startTime + 300
                ).firstOrNull { 
                    it.endTime == sleepData.endTime && it.sleepStage == sleepData.stage 
                }
                
                if (existing == null) {
                    // Insert new sleep session
                    val sleepEntity = com.example.fitapp.data.db.HealthSleepEntity(
                        date = dateStr,
                        startTime = sleepData.startTime,
                        endTime = sleepData.endTime,
                        durationMinutes = sleepData.durationMinutes,
                        sleepStage = sleepData.stage,
                        source = "health_connect",
                        syncedAt = System.currentTimeMillis() / 1000
                    )
                    sleepDao.insert(sleepEntity)
                }
            }
            
            if (sleepDataList.isNotEmpty()) {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Synced ${sleepDataList.size} sleep sessions for $dateStr"
                )
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to sync sleep data for $date",
                mapOf("error" to (e.message ?: "Unknown error"))
            )
        }
    }
    
    companion object {
        private const val TAG = "HealthConnectSyncWorker"
        private const val SYNC_WORK_NAME = "health_connect_sync"
        private const val PERIODIC_SYNC_WORK_NAME = "health_connect_periodic_sync"
        
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                // Remove network constraint - health sync can work with any connection
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
                // Remove network constraint - health sync can work with any connection
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