package com.example.fitapp.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.shared.*
import com.example.fitapp.wear.services.WearDataSyncService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WearWorkoutViewModel : ViewModel() {
    
    private lateinit var dataSyncService: WearDataSyncService
    
    private val _workoutState = MutableStateFlow(WearWorkoutState())
    val workoutState: StateFlow<WearWorkoutState> = _workoutState.asStateFlow()
    
    private val _progressData = MutableStateFlow(WearProgressData())
    val progressData: StateFlow<WearProgressData> = _progressData.asStateFlow()
    
    private val _notifications = MutableStateFlow<List<WearNotification>>(emptyList())
    val notifications: StateFlow<List<WearNotification>> = _notifications.asStateFlow()
    
    init {
        dataSyncService = WearDataSyncService()
        // Listen for data updates from phone
        viewModelScope.launch {
            dataSyncService.workoutStateFlow.collect { state ->
                _workoutState.value = state ?: WearWorkoutState()
            }
        }
        
        viewModelScope.launch {
            dataSyncService.progressDataFlow.collect { progress ->
                _progressData.value = progress ?: WearProgressData()
            }
        }
        
        viewModelScope.launch {
            dataSyncService.notificationsFlow.collect { notifications ->
                _notifications.value = notifications
            }
        }
        
        // Request initial sync
        syncWithPhone()
    }
    
    fun pauseWorkout() {
        viewModelScope.launch {
            dataSyncService.sendAction(
                WearWorkoutAction(
                    action = WearActionType.PAUSE_WORKOUT,
                    workoutId = _workoutState.value.workoutId
                )
            )
        }
    }
    
    fun resumeWorkout() {
        viewModelScope.launch {
            dataSyncService.sendAction(
                WearWorkoutAction(
                    action = WearActionType.RESUME_WORKOUT,
                    workoutId = _workoutState.value.workoutId
                )
            )
        }
    }
    
    fun completeCurrentSet() {
        val currentState = _workoutState.value
        viewModelScope.launch {
            dataSyncService.sendAction(
                WearWorkoutAction(
                    action = WearActionType.COMPLETE_SET,
                    workoutId = currentState.workoutId,
                    setIndex = currentState.currentSet,
                    reps = currentState.currentReps
                )
            )
        }
    }
    
    fun skipRest() {
        viewModelScope.launch {
            dataSyncService.sendAction(
                WearWorkoutAction(
                    action = WearActionType.SKIP_REST,
                    workoutId = _workoutState.value.workoutId
                )
            )
        }
    }
    
    fun completeWorkout() {
        viewModelScope.launch {
            dataSyncService.sendAction(
                WearWorkoutAction(
                    action = WearActionType.COMPLETE_WORKOUT,
                    workoutId = _workoutState.value.workoutId
                )
            )
        }
    }
    
    fun requestWorkoutStart() {
        viewModelScope.launch {
            dataSyncService.sendAction(
                WearWorkoutAction(
                    action = WearActionType.START_WORKOUT
                )
            )
        }
    }
    
    fun updateHeartRate(heartRate: Int) {
        viewModelScope.launch {
            dataSyncService.sendAction(
                WearWorkoutAction(
                    action = WearActionType.UPDATE_HEART_RATE,
                    workoutId = _workoutState.value.workoutId,
                    reps = heartRate // Using reps field to pass heart rate value
                )
            )
        }
    }
    
    fun syncWithPhone() {
        viewModelScope.launch {
            dataSyncService.requestSync()
        }
    }
    
    fun dismissNotification(notificationId: String) {
        _notifications.value = _notifications.value.filter { it.id != notificationId }
    }
    
    override fun onCleared() {
        super.onCleared()
        dataSyncService.cleanup()
    }
}