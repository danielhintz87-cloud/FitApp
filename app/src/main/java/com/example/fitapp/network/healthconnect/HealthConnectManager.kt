package com.example.fitapp.network.healthconnect

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Health Connect integration manager
 *
 * Provides integration with Android Health Connect for syncing health data
 * across different apps and devices.
 */
class HealthConnectManager(private val context: Context) {
    companion object {
        private const val TAG = "HealthConnectManager"

        // Health Connect permissions
        val REQUIRED_PERMISSIONS =
            setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
                HealthPermission.getReadPermission(ExerciseSessionRecord::class),
                HealthPermission.getReadPermission(HeartRateRecord::class),
                HealthPermission.getReadPermission(SleepSessionRecord::class),
                HealthPermission.getWritePermission(ExerciseSessionRecord::class),
                HealthPermission.getWritePermission(StepsRecord::class),
            )
    }

    private val healthConnectClient: HealthConnectClient? by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    /**
     * Check if Health Connect is available and supported
     */
    fun isAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            Log.w(TAG, "Health Connect not available: ${e.message}")
            false
        }
    }

    /**
     * Check if all required permissions are granted
     */
    suspend fun hasPermissions(): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val client = healthConnectClient ?: return@withContext false
                val grantedPermissions = client.permissionController.getGrantedPermissions()
                REQUIRED_PERMISSIONS.all { it in grantedPermissions }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions", e)
                false
            }
        }

    /**
     * Get the permission controller for requesting permissions
     */
    fun getPermissionController(): PermissionController? {
        return healthConnectClient?.permissionController
    }

    /**
     * Sync daily activity data for a specific date
     *
     * @param date The date to sync data for
     * @return HealthSyncData containing steps, active calories, and exercise data
     */
    suspend fun syncDailyData(date: LocalDate): HealthSyncData? =
        withContext(Dispatchers.IO) {
            Log.i(TAG, "Syncing health data for date: $date")

            try {
                val client = healthConnectClient ?: return@withContext null

                // Create time range for the specific date
                val startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                val endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
                val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

                // Sync steps data
                val steps = syncStepsData(client, timeRangeFilter)

                // Sync active calories
                val activeCalories = syncActiveCaloriesData(client, timeRangeFilter)

                // Sync exercise sessions
                val exerciseSessions = syncExerciseSessionData(client, timeRangeFilter)

                val healthData =
                    HealthSyncData(
                        date = date,
                        steps = steps,
                        activeCalories = activeCalories,
                        exerciseSessions = exerciseSessions,
                    )

                Log.i(TAG, "Successfully synced data: ${healthData.getSummary()}")
                return@withContext healthData
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync health data for $date", e)
                return@withContext null
            }
        }

    private suspend fun syncStepsData(
        client: HealthConnectClient,
        timeRangeFilter: TimeRangeFilter,
    ): Int {
        return try {
            val request =
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = timeRangeFilter,
                )
            val response = client.readRecords(request)
            response.records.sumOf { it.count.toInt() }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading steps data", e)
            0
        }
    }

    private suspend fun syncActiveCaloriesData(
        client: HealthConnectClient,
        timeRangeFilter: TimeRangeFilter,
    ): Double {
        return try {
            val request =
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = timeRangeFilter,
                )
            val response = client.readRecords(request)
            response.records.sumOf { it.energy.inKilocalories }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading active calories data", e)
            0.0
        }
    }

    private suspend fun syncExerciseSessionData(
        client: HealthConnectClient,
        timeRangeFilter: TimeRangeFilter,
    ): List<ExerciseInfo> {
        return try {
            val request =
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = timeRangeFilter,
                )
            val response = client.readRecords(request)

            response.records.map { session ->
                ExerciseInfo(
                    title = session.title ?: "Exercise Session",
                    exerciseType = session.exerciseType.toString(),
                    durationMinutes = ((session.endTime.epochSecond - session.startTime.epochSecond) / 60).toInt(),
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading exercise session data", e)
            emptyList()
        }
    }

    /**
     * Sync heart rate data for a specific time range
     */
    suspend fun syncHeartRateData(
        startTime: Instant,
        endTime: Instant,
    ): List<HeartRateData> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val client = healthConnectClient ?: return@withContext emptyList()
                val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

                val request =
                    ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = timeRangeFilter,
                    )
                val response = client.readRecords(request)

                response.records.flatMap { record ->
                    record.samples.map { sample ->
                        HeartRateData(
                            timestamp = sample.time.epochSecond,
                            heartRate = sample.beatsPerMinute.toInt(),
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading heart rate data", e)
                emptyList()
            }
        }

    /**
     * Sync sleep data for a specific time range
     */
    suspend fun syncSleepData(
        startTime: Instant,
        endTime: Instant,
    ): List<SleepData> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val client = healthConnectClient ?: return@withContext emptyList()
                val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

                val request =
                    ReadRecordsRequest(
                        recordType = SleepSessionRecord::class,
                        timeRangeFilter = timeRangeFilter,
                    )
                val response = client.readRecords(request)

                response.records.flatMap { session ->
                    session.stages.map { stage ->
                        SleepData(
                            startTime = stage.startTime.epochSecond,
                            endTime = stage.endTime.epochSecond,
                            stage = stage.stage.toString(),
                            durationMinutes = ((stage.endTime.epochSecond - stage.startTime.epochSecond) / 60).toInt(),
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading sleep data", e)
                emptyList()
            }
        }

    /**
     * Sync data for a date range
     *
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of HealthSyncData for each day
     */
    suspend fun syncDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<HealthSyncData> =
        withContext(Dispatchers.IO) {
            Log.i(TAG, "Syncing health data from $startDate to $endDate")

            try {
                val healthDataList = mutableListOf<HealthSyncData>()
                var currentDate = startDate

                while (!currentDate.isAfter(endDate)) {
                    syncDailyData(currentDate)?.let { healthData ->
                        healthDataList.add(healthData)
                    }
                    currentDate = currentDate.plusDays(1)
                }

                Log.i(TAG, "Successfully synced ${healthDataList.size} days of health data")
                return@withContext healthDataList
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync health data range", e)
                return@withContext emptyList()
            }
        }
}

/**
 * Data class representing synced health data for a single day
 */
data class HealthSyncData(
    val date: LocalDate,
    val steps: Int,
    val activeCalories: Double, // in kcal
    val exerciseSessions: List<ExerciseInfo>,
) {
    /**
     * Get total exercise duration in minutes
     */
    fun getTotalExerciseMinutes(): Int {
        return exerciseSessions.sumOf { it.durationMinutes }
    }

    /**
     * Get formatted summary string
     */
    fun getSummary(): String {
        return "Schritte: $steps, Kalorien: ${activeCalories.toInt()}, Übungen: ${exerciseSessions.size}"
    }
}

/**
 * Data class representing exercise session information
 */
data class ExerciseInfo(
    val title: String,
    val exerciseType: String = "unknown",
    val durationMinutes: Int,
) {
    /**
     * Get localized exercise type name in German
     */
    fun getLocalizedType(): String {
        return when (exerciseType.lowercase()) {
            "walking" -> "Gehen"
            "running" -> "Laufen"
            "cycling" -> "Radfahren"
            "swimming" -> "Schwimmen"
            "workout" -> "Training"
            "yoga" -> "Yoga"
            "pilates" -> "Pilates"
            "weightlifting" -> "Krafttraining"
            "cardio" -> "Cardio"
            else -> title.ifBlank { "Unbekannte Übung" }
        }
    }
}

/**
 * Data class representing heart rate data point
 */
data class HeartRateData(
    val timestamp: Long, // Epoch timestamp in seconds
    val heartRate: Int, // BPM
)

/**
 * Data class representing sleep data
 */
data class SleepData(
    val startTime: Long, // Epoch timestamp in seconds
    val endTime: Long, // Epoch timestamp in seconds
    val stage: String, // "light", "deep", "rem", "awake", "unknown"
    val durationMinutes: Int,
)
