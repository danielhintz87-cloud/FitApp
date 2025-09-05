package com.example.fitapp.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.services.CloudSyncManager
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.os.Build

class CloudSyncSettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val cloudSyncManager = CloudSyncManager(application)
    private val database = AppDatabase.get(application)
    private val userProfileDao = database.userProfileDao()
    private val syncConflictDao = database.syncConflictDao()
    
    private val _uiState = MutableStateFlow(CloudSyncUiState())
    val uiState: StateFlow<CloudSyncUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val TAG = "CloudSyncSettingsVM"
    }
    
    init {
        observeUserProfile()
        observePendingConflicts()
    }
    
    private fun observeUserProfile() {
        viewModelScope.launch {
            // Observe active user profile changes
            try {
                // Simple polling approach for demo - in production would use Flow
                refreshUserProfile()
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Failed to observe user profile",
                    exception = e
                )
            }
        }
    }
    
    private fun observePendingConflicts() {
        viewModelScope.launch {
            syncConflictDao.getPendingConflictCount().collect { count ->
                _uiState.value = _uiState.value.copy(pendingConflicts = count)
            }
        }
    }
    
    private suspend fun refreshUserProfile() {
        try {
            val activeUser = userProfileDao.getActiveUserProfile()
            if (activeUser != null) {
                val preferences = try {
                    val json = JSONObject(activeUser.syncPreferences)
                    mapOf(
                        "sync_achievements" to json.optBoolean("sync_achievements", true),
                        "sync_workouts" to json.optBoolean("sync_workouts", true),
                        "sync_nutrition" to json.optBoolean("sync_nutrition", true),
                        "sync_weight" to json.optBoolean("sync_weight", true),
                        "sync_settings" to json.optBoolean("sync_settings", true)
                    )
                } catch (e: Exception) {
                    // Default preferences if parsing fails
                    mapOf(
                        "sync_achievements" to true,
                        "sync_workouts" to true,
                        "sync_nutrition" to true,
                        "sync_weight" to true,
                        "sync_settings" to true
                    )
                }
                
                val lastSyncTime = if (activeUser.lastSyncTime > 0) {
                    formatTimestamp(activeUser.lastSyncTime * 1000) // Convert to milliseconds
                } else {
                    null
                }
                
                _uiState.value = _uiState.value.copy(
                    isSignedIn = true,
                    userEmail = activeUser.email,
                    deviceName = activeUser.deviceName,
                    syncPreferences = preferences,
                    lastSyncTime = lastSyncTime
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSignedIn = false,
                    userEmail = null,
                    deviceName = getDeviceName(),
                    syncPreferences = getDefaultPreferences(),
                    lastSyncTime = null
                )
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to refresh user profile",
                exception = e
            )
        }
    }
    
    fun signIn(email: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSigningIn = true)
                
                // Generate a simple user ID from email for demo purposes
                val userId = "user_${email.hashCode().toString().replace("-", "")}"
                
                val result = cloudSyncManager.initializeCloudSync(
                    userId = userId,
                    email = email,
                    displayName = email.substringBefore("@")
                )
                
                if (result.isSuccess) {
                    refreshUserProfile()
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYNC,
                        TAG,
                        "User signed in successfully: $email"
                    )
                } else {
                    StructuredLogger.error(
                        StructuredLogger.LogCategory.SYNC,
                        TAG,
                        "Failed to sign in user: $email",
                        exception = result.exceptionOrNull()
                    )
                }
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Error during sign in",
                    exception = e
                )
            } finally {
                _uiState.value = _uiState.value.copy(isSigningIn = false)
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                // Deactivate all user profiles (sign out)
                userProfileDao.deactivateAllProfiles()
                
                // Cancel cloud sync
                CloudSyncManager.cancelCloudSync(getApplication())
                
                refreshUserProfile()
                
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "User signed out successfully"
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Error during sign out",
                    exception = e
                )
            }
        }
    }
    
    fun updateSyncPreference(key: String, value: Boolean) {
        viewModelScope.launch {
            try {
                val currentPreferences = _uiState.value.syncPreferences.toMutableMap()
                currentPreferences[key] = value
                
                // Update UI state immediately
                _uiState.value = _uiState.value.copy(syncPreferences = currentPreferences)
                
                // Update in database
                cloudSyncManager.updateSyncPreferences(currentPreferences)
                
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Updated sync preference: $key = $value"
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Failed to update sync preference",
                    exception = e
                )
            }
        }
    }
    
    fun triggerManualSync() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSyncing = true)
                
                // Trigger cloud sync
                CloudSyncManager.triggerImmediateCloudSync(getApplication())
                
                // Wait a bit and then refresh status
                kotlinx.coroutines.delay(2000)
                refreshUserProfile()
                
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Manual sync triggered"
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Failed to trigger manual sync",
                    exception = e
                )
            } finally {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }
    
    fun navigateToConflicts() {
        // This would navigate to a conflict resolution screen
        // For now, just log the action
        StructuredLogger.info(
            StructuredLogger.LogCategory.SYNC,
            TAG,
            "Navigate to conflicts requested"
        )
    }
    
    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    }
    
    private fun getDefaultPreferences(): Map<String, Boolean> {
        return mapOf(
            "sync_achievements" to true,
            "sync_workouts" to true,
            "sync_nutrition" to true,
            "sync_weight" to true,
            "sync_settings" to true
        )
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            when {
                diff < 60_000 -> "Gerade eben"
                diff < 3_600_000 -> "${diff / 60_000} min"
                diff < 86_400_000 -> "${diff / 3_600_000} Std"
                diff < 604_800_000 -> "${diff / 86_400_000} Tage"
                else -> "${diff / 604_800_000} Wochen"
            }
        } catch (e: Exception) {
            "Unbekannt"
        }
    }
}

data class CloudSyncUiState(
    val isSignedIn: Boolean = false,
    val isSigningIn: Boolean = false,
    val isSyncing: Boolean = false,
    val userEmail: String? = null,
    val deviceName: String = "",
    val syncPreferences: Map<String, Boolean> = emptyMap(),
    val lastSyncTime: String? = null,
    val pendingConflicts: Int = 0
)