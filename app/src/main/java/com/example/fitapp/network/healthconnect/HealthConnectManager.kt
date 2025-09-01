package com.example.fitapp.network.healthconnect

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Health Connect integration manager for syncing activity calories and health data
 * 
 * Provides seamless integration with Google Health Connect to automatically sync
 * activity data, steps, and exercise sessions for comprehensive calorie tracking.
 */
class HealthConnectManager(private val context: Context) {
    
    companion object {
        private const val TAG = "HealthConnectManager"
        
        // Required permissions for reading health data
        private val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        )
    }
    
    private val healthConnectClient: HealthConnectClient? by lazy {
        if (HealthConnectClient.isAvailable(context)) {
            HealthConnectClient.getOrCreate(context)
        } else {
            Log.w(TAG, "Health Connect is not available on this device")
            null
        }
    }
    
    /**
     * Check if Health Connect is available and supported
     */
    fun isAvailable(): Boolean {
        return HealthConnectClient.isAvailable(context)
    }
    
    /**
     * Check if all required permissions are granted
     */
    suspend fun hasPermissions(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val client = healthConnectClient ?: return@withContext false
            val granted = client.permissionController.getGrantedPermissions()
            PERMISSIONS.all { it in granted }
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
     * Get the required permissions set
     */
    fun getRequiredPermissions(): Set<String> {
        return PERMISSIONS
    }
    
    /**
     * Sync daily activity data for a specific date
     * 
     * @param date The date to sync data for
     * @return HealthSyncData containing steps, active calories, and exercise data
     */
    suspend fun syncDailyData(date: LocalDate): HealthSyncData? = withContext(Dispatchers.IO) {
        return@withContext try {
            val client = healthConnectClient ?: run {
                Log.w(TAG, "Health Connect client not available")
                return@withContext null
            }
            
            if (!hasPermissions()) {
                Log.w(TAG, "Health Connect permissions not granted")
                return@withContext null
            }
            
            val timeRange = createDayTimeRange(date)
            
            // Fetch steps data
            val stepsData = fetchStepsData(client, timeRange)
            
            // Fetch active calories data
            val activeCaloriesData = fetchActiveCaloriesData(client, timeRange)
            
            // Fetch exercise sessions
            val exerciseData = fetchExerciseData(client, timeRange)
            
            Log.d(TAG, "Synced data for $date: $stepsData steps, $activeCaloriesData kcal, ${exerciseData.size} exercises")
            
            HealthSyncData(
                date = date,
                steps = stepsData,
                activeCalories = activeCaloriesData,
                exerciseSessions = exerciseData
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing daily data for $date", e)
            null
        }
    }
    
    /**
     * Sync data for a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of HealthSyncData for each day
     */
    suspend fun syncDateRange(startDate: LocalDate, endDate: LocalDate): List<HealthSyncData> = withContext(Dispatchers.IO) {
        val results = mutableListOf<HealthSyncData>()
        var currentDate = startDate
        
        while (!currentDate.isAfter(endDate)) {
            syncDailyData(currentDate)?.let { data ->
                results.add(data)
            }
            currentDate = currentDate.plusDays(1)
        }
        
        Log.d(TAG, "Synced ${results.size} days of data from $startDate to $endDate")
        return@withContext results
    }
    
    private suspend fun fetchStepsData(client: HealthConnectClient, timeRange: TimeRangeFilter): Int {
        return try {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRange
            )
            val response = client.readRecords(request)
            response.records.sumOf { it.count.toInt() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching steps data", e)
            0
        }
    }
    
    private suspend fun fetchActiveCaloriesData(client: HealthConnectClient, timeRange: TimeRangeFilter): Double {
        return try {
            val request = ReadRecordsRequest(
                recordType = ActiveCaloriesBurnedRecord::class,
                timeRangeFilter = timeRange
            )
            val response = client.readRecords(request)
            response.records.sumOf { it.energy.inKilocalories }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active calories data", e)
            0.0
        }
    }
    
    private suspend fun fetchExerciseData(client: HealthConnectClient, timeRange: TimeRangeFilter): List<ExerciseInfo> {
        return try {
            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = timeRange
            )
            val response = client.readRecords(request)
            response.records.map { session ->
                ExerciseInfo(
                    title = session.title ?: "Unknown Exercise",
                    exerciseType = session.exerciseType.toString(),
                    startTime = session.startTime,
                    endTime = session.endTime,
                    durationMinutes = ((session.endTime.epochSecond - session.startTime.epochSecond) / 60).toInt()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exercise data", e)
            emptyList()
        }
    }
    
    private fun createDayTimeRange(date: LocalDate): TimeRangeFilter {
        val startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        return TimeRangeFilter.between(startOfDay, endOfDay)
    }
}

/**
 * Data class representing synced health data for a single day
 */
data class HealthSyncData(
    val date: LocalDate,
    val steps: Int,
    val activeCalories: Double, // in kcal
    val exerciseSessions: List<ExerciseInfo>
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
    val exerciseType: String,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int
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