package com.example.fitapp.services

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.example.fitapp.shared.*
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Phone-side service for synchronizing data with Wear OS device
 */
class PhoneWearSyncService(
    private val context: Context,
    private val database: AppDatabase
) : DataClient.OnDataChangedListener {
    
    companion object {
        private const val TAG = "PhoneWearSyncService"
    }
    
    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // Current workout state to sync with watch
    private val _workoutState = MutableStateFlow(WearWorkoutState())
    private val workoutState: StateFlow<WearWorkoutState> = _workoutState.asStateFlow()
    
    init {
        dataClient.addListener(this)
        Log.d(TAG, "PhoneWearSyncService initialized")
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                handleWearRequest(dataItem)
            }
        }
    }
    
    private fun handleWearRequest(dataItem: DataItem) {
        when (dataItem.uri.path) {
            WearDataPaths.ACTIONS -> {
                val data = DataMapItem.fromDataItem(dataItem).dataMap
                val jsonString = data.getString("action")
                jsonString?.let {
                    try {
                        val action = json.decodeFromString<WearWorkoutAction>(it)
                        handleWearAction(action)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing wear action", e)
                    }
                }
            }
            
            WearDataPaths.SYNC_REQUEST -> {
                Log.d(TAG, "Sync request received from watch")
                syncAllDataToWatch()
            }
        }
    }
    
    private fun handleWearAction(action: WearWorkoutAction) {
        Log.d(TAG, "Handling wear action: ${action.action}")
        
        // Here you would integrate with existing workout execution manager
        // For now, we'll just log the actions
        when (action.action) {
            WearActionType.START_WORKOUT -> {
                // Trigger workout start in phone app
                Log.d(TAG, "Watch requested workout start")
            }
            WearActionType.PAUSE_WORKOUT -> {
                // Pause current workout
                Log.d(TAG, "Watch requested workout pause")
            }
            WearActionType.RESUME_WORKOUT -> {
                // Resume current workout
                Log.d(TAG, "Watch requested workout resume")
            }
            WearActionType.COMPLETE_WORKOUT -> {
                // Complete current workout
                Log.d(TAG, "Watch requested workout completion")
            }
            WearActionType.COMPLETE_SET -> {
                // Mark set as complete
                Log.d(TAG, "Watch completed set: ${action.setIndex} with ${action.reps} reps")
            }
            WearActionType.SKIP_REST -> {
                // Skip rest timer
                Log.d(TAG, "Watch skipped rest")
            }
            WearActionType.UPDATE_HEART_RATE -> {
                // Update heart rate data
                Log.d(TAG, "Heart rate from watch: ${action.reps} BPM")
            }
            WearActionType.SYNC_REQUEST -> {
                syncAllDataToWatch()
            }
        }
    }
    
    /**
     * Update workout state and sync to watch
     */
    fun updateWorkoutState(
        isActive: Boolean = false,
        exerciseName: String = "",
        currentSet: Int = 0,
        totalSets: Int = 0,
        currentReps: Int = 0,
        targetReps: Int = 0,
        restTimeRemaining: Int = 0,
        isResting: Boolean = false,
        elapsedTime: Long = 0L,
        heartRate: Int = 0,
        caloriesBurned: Int = 0,
        workoutId: String = ""
    ) {
        val newState = WearWorkoutState(
            isActive = isActive,
            exerciseName = exerciseName,
            currentSet = currentSet,
            totalSets = totalSets,
            currentReps = currentReps,
            targetReps = targetReps,
            restTimeRemaining = restTimeRemaining,
            isResting = isResting,
            elapsedTime = elapsedTime,
            heartRate = heartRate,
            caloriesBurned = caloriesBurned,
            workoutId = workoutId
        )
        
        _workoutState.value = newState
        sendWorkoutStateToWatch(newState)
    }
    
    private fun sendWorkoutStateToWatch(state: WearWorkoutState) {
        scope.launch {
            try {
                val jsonString = json.encodeToString(state)
                val request = PutDataMapRequest.create(WearDataPaths.WORKOUT_STATE).apply {
                    dataMap.putString("workout_state", jsonString)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                
                val putDataTask = dataClient.putDataItem(request.asPutDataRequest())
                Tasks.await(putDataTask)
                Log.d(TAG, "Workout state sent to watch: ${state.exerciseName}")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending workout state to watch", e)
            }
        }
    }
    
    /**
     * Send progress data to watch
     */
    fun sendProgressDataToWatch(progressData: WearProgressData) {
        scope.launch {
            try {
                val jsonString = json.encodeToString(progressData)
                val request = PutDataMapRequest.create(WearDataPaths.PROGRESS_DATA).apply {
                    dataMap.putString("progress_data", jsonString)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                
                val putDataTask = dataClient.putDataItem(request.asPutDataRequest())
                Tasks.await(putDataTask)
                Log.d(TAG, "Progress data sent to watch")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending progress data to watch", e)
            }
        }
    }
    
    /**
     * Send notifications to watch
     */
    fun sendNotificationsToWatch(notifications: List<WearNotification>) {
        scope.launch {
            try {
                val jsonString = json.encodeToString(notifications)
                val request = PutDataMapRequest.create(WearDataPaths.NOTIFICATIONS).apply {
                    dataMap.putString("notifications", jsonString)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                
                val putDataTask = dataClient.putDataItem(request.asPutDataRequest())
                Tasks.await(putDataTask)
                Log.d(TAG, "Notifications sent to watch: ${notifications.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending notifications to watch", e)
            }
        }
    }
    
    /**
     * Sync all data to watch
     */
    private fun syncAllDataToWatch() {
        scope.launch {
            try {
                // Send current workout state
                sendWorkoutStateToWatch(_workoutState.value)
                
                // Get and send progress data from database
                // This would integrate with your existing database queries
                val mockProgress = WearProgressData(
                    totalWorkouts = 42,
                    weeklyWorkouts = 3,
                    currentStreak = 7,
                    personalRecords = listOf(
                        WearPersonalRecord("Bench Press", 100f, "kg", "2024-01-15"),
                        WearPersonalRecord("Squat", 120f, "kg", "2024-01-10")
                    ),
                    lastWorkoutDate = "Yesterday",
                    weeklyCaloriesBurned = 1250
                )
                sendProgressDataToWatch(mockProgress)
                
                // Send empty notifications for now
                sendNotificationsToWatch(emptyList())
                
                Log.d(TAG, "Full sync completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during full sync", e)
            }
        }
    }
    
    fun cleanup() {
        try {
            dataClient.removeListener(this)
            Log.d(TAG, "PhoneWearSyncService cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}