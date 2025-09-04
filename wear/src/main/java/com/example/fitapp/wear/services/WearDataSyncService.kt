package com.example.fitapp.wear.services

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.example.fitapp.shared.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class WearDataSyncService : DataClient.OnDataChangedListener {
    
    companion object {
        private const val TAG = "WearDataSyncService"
    }
    
    private var context: Context? = null
    private var dataClient: DataClient? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _workoutStateFlow = MutableStateFlow<WearWorkoutState?>(null)
    val workoutStateFlow: StateFlow<WearWorkoutState?> = _workoutStateFlow.asStateFlow()
    
    private val _progressDataFlow = MutableStateFlow<WearProgressData?>(null)
    val progressDataFlow: StateFlow<WearProgressData?> = _progressDataFlow.asStateFlow()
    
    private val _notificationsFlow = MutableStateFlow<List<WearNotification>>(emptyList())
    val notificationsFlow: StateFlow<List<WearNotification>> = _notificationsFlow.asStateFlow()
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    fun initialize(context: Context) {
        this.context = context
        this.dataClient = Wearable.getDataClient(context)
        dataClient?.addListener(this)
        Log.d(TAG, "WearDataSyncService initialized")
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                handleDataUpdate(dataItem)
            }
        }
    }
    
    private fun handleDataUpdate(dataItem: DataItem) {
        when (dataItem.uri.path) {
            WearDataPaths.WORKOUT_STATE -> {
                val data = DataMapItem.fromDataItem(dataItem).dataMap
                val jsonString = data.getString("workout_state")
                jsonString?.let {
                    try {
                        val workoutState = json.decodeFromString<WearWorkoutState>(it)
                        _workoutStateFlow.value = workoutState
                        Log.d(TAG, "Received workout state update: ${workoutState.exerciseName}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing workout state", e)
                    }
                }
            }
            
            WearDataPaths.PROGRESS_DATA -> {
                val data = DataMapItem.fromDataItem(dataItem).dataMap
                val jsonString = data.getString("progress_data")
                jsonString?.let {
                    try {
                        val progressData = json.decodeFromString<WearProgressData>(it)
                        _progressDataFlow.value = progressData
                        Log.d(TAG, "Received progress data update: ${progressData.totalWorkouts} workouts")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing progress data", e)
                    }
                }
            }
            
            WearDataPaths.NOTIFICATIONS -> {
                val data = DataMapItem.fromDataItem(dataItem).dataMap
                val jsonString = data.getString("notifications")
                jsonString?.let {
                    try {
                        val notifications = json.decodeFromString<List<WearNotification>>(it)
                        _notificationsFlow.value = notifications
                        Log.d(TAG, "Received notifications update: ${notifications.size} notifications")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing notifications", e)
                    }
                }
            }
        }
    }
    
    fun sendAction(action: WearWorkoutAction) {
        scope.launch {
            try {
                val jsonString = json.encodeToString(action)
                val request = PutDataMapRequest.create(WearDataPaths.ACTIONS).apply {
                    dataMap.putString("action", jsonString)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                
                val putDataTask = dataClient?.putDataItem(request.asPutDataRequest())
                if (putDataTask != null) {
                    Tasks.await(putDataTask)
                }
                Log.d(TAG, "Sent action: ${action.action}")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending action", e)
            }
        }
    }
    
    fun requestSync() {
        scope.launch {
            try {
                val request = PutDataMapRequest.create(WearDataPaths.SYNC_REQUEST).apply {
                    dataMap.putLong("sync_request_time", System.currentTimeMillis())
                }
                
                val putDataTask = dataClient?.putDataItem(request.asPutDataRequest())
                if (putDataTask != null) {
                    Tasks.await(putDataTask)
                }
                Log.d(TAG, "Sync request sent")
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting sync", e)
            }
        }
    }
    
    fun sendHeartRateUpdate(heartRate: Int) {
        scope.launch {
            try {
                val action = WearWorkoutAction(
                    action = WearActionType.UPDATE_HEART_RATE,
                    reps = heartRate
                )
                sendAction(action)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending heart rate update", e)
            }
        }
    }
    
    fun cleanup() {
        try {
            dataClient?.removeListener(this)
            Log.d(TAG, "WearDataSyncService cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}