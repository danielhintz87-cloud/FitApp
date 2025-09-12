package com.example.fitapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.network.healthconnect.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for Health Connect Settings Screen
 * Manages Health Connect permissions, sync status and last sync time
 */
@HiltViewModel
class HealthConnectSettingsViewModel
    @Inject
    constructor(
        private val healthConnectManager: HealthConnectManager,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HealthConnectUiState())
        val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

        init {
            loadHealthConnectStatus()
            observeLastSyncTime()
        }

        fun loadHealthConnectStatus() {
            viewModelScope.launch {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)

                    val isAvailable = healthConnectManager.isAvailable()
                    val hasPermissions =
                        if (isAvailable) {
                            healthConnectManager.hasPermissions()
                        } else {
                            false
                        }

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            isAvailable = isAvailable,
                            hasPermissions = hasPermissions,
                            syncEnabled = hasPermissions,
                            error = null,
                        )
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = "Fehler beim Laden des Health Connect Status: ${e.message}",
                        )
                }
            }
        }

        private fun observeLastSyncTime() {
            viewModelScope.launch {
                userPreferencesRepository.syncTimestamps.collect { timestamps ->
                    val lastSyncTime =
                        if (timestamps.lastHealthConnectSyncMillis > 0) {
                            val dateTime =
                                LocalDateTime.ofEpochSecond(
                                    timestamps.lastHealthConnectSyncMillis / 1000,
                                    0,
                                    java.time.ZoneOffset.systemDefault().rules.getOffset(
                                        java.time.Instant.ofEpochMilli(timestamps.lastHealthConnectSyncMillis),
                                    ),
                                )
                            dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                        } else {
                            null
                        }

                    val syncStatus =
                        when {
                            !_uiState.value.isAvailable -> "Health Connect nicht verfügbar"
                            !_uiState.value.hasPermissions -> "Berechtigungen erforderlich"
                            lastSyncTime != null -> "Zuletzt synchronisiert: $lastSyncTime"
                            else -> "Noch nicht synchronisiert"
                        }

                    _uiState.value =
                        _uiState.value.copy(
                            lastSyncTime = lastSyncTime,
                            syncStatus = syncStatus,
                        )
                }
            }
        }

        fun requestPermissions() {
            if (!_uiState.value.isAvailable) {
                _uiState.value =
                    _uiState.value.copy(
                        error = "Health Connect ist auf diesem Gerät nicht verfügbar",
                    )
                return
            }

            val permissionController = healthConnectManager.getPermissionController()
            if (permissionController != null) {
                _uiState.value =
                    _uiState.value.copy(
                        permissionRequested = true,
                    )
            } else {
                _uiState.value =
                    _uiState.value.copy(
                        error = "Fehler beim Anfordern der Berechtigungen",
                    )
            }
        }

        fun onPermissionsResult(granted: Set<String>) {
            viewModelScope.launch {
                _uiState.value =
                    _uiState.value.copy(
                        permissionRequested = false,
                    )

                // Reload permission status
                val hasPermissions = healthConnectManager.hasPermissions()
                _uiState.value =
                    _uiState.value.copy(
                        hasPermissions = hasPermissions,
                        syncEnabled = hasPermissions,
                    )

                if (hasPermissions) {
                    // Trigger initial sync
                    triggerSync()
                }
            }
        }

        fun triggerSync() {
            if (!_uiState.value.hasPermissions) {
                _uiState.value =
                    _uiState.value.copy(
                        error = "Berechtigungen sind erforderlich für die Synchronisation",
                    )
                return
            }

            viewModelScope.launch {
                try {
                    _uiState.value =
                        _uiState.value.copy(
                            isSyncing = true,
                            error = null,
                        )

                    // Trigger sync via HealthConnectManager
                    val today = java.time.LocalDate.now()
                    val syncData = healthConnectManager.syncDailyData(today)

                    if (syncData != null) {
                        // Update last sync time
                        userPreferencesRepository.updateSyncTimestamps(
                            lastHealthConnectSyncMillis = System.currentTimeMillis(),
                        )

                        _uiState.value =
                            _uiState.value.copy(
                                isSyncing = false,
                                syncStatus = "Synchronisation erfolgreich",
                            )
                    } else {
                        _uiState.value =
                            _uiState.value.copy(
                                isSyncing = false,
                                error = "Keine Daten zum Synchronisieren gefunden",
                            )
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            isSyncing = false,
                            error = "Synchronisation fehlgeschlagen: ${e.message}",
                        )
                }
            }
        }

        fun toggleSyncEnabled(enabled: Boolean) {
            if (enabled && !_uiState.value.hasPermissions) {
                requestPermissions()
                return
            }

            _uiState.value =
                _uiState.value.copy(
                    syncEnabled = enabled,
                )

            // TODO: Save sync preference to DataStore
            viewModelScope.launch {
                // For now, we tie sync enabled to permissions
                // In future, could add separate sync preference
            }
        }

        fun clearError() {
            _uiState.value = _uiState.value.copy(error = null)
        }

        fun onNavigateBack() {
            // Handle navigation - could emit navigation event
            // For now, no-op as navigation is handled by parent
        }
    }

/**
 * UI State for Health Connect Settings
 */
data class HealthConnectUiState(
    val isLoading: Boolean = false,
    val isAvailable: Boolean = false,
    val hasPermissions: Boolean = false,
    val syncEnabled: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: String? = null,
    val syncStatus: String = "Nicht synchronisiert",
    val permissionRequested: Boolean = false,
    val error: String? = null,
)
