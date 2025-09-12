package com.example.fitapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.core.health.ApiHealthRegistry
import com.example.fitapp.core.health.HealthStatus
import com.example.fitapp.core.health.HealthStatusRepository
import com.example.fitapp.core.health.HealthSummary
import com.example.fitapp.services.HealthCheckWorker
import com.example.fitapp.util.StructuredLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for health status screen
 */
@HiltViewModel
class HealthStatusViewModel @Inject constructor(
    private val apiHealthRegistry: ApiHealthRegistry,
    private val healthStatusRepository: HealthStatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthStatusUiState())
    val uiState: StateFlow<HealthStatusUiState> = _uiState.asStateFlow()

    init {
        observeHealthData()
        loadInitialData()
    }

    private fun observeHealthData() {
        viewModelScope.launch {
            combine(
                healthStatusRepository.getAllHealthStatusesFlow(),
                healthStatusRepository.getHealthSummaryFlow()
            ) { statuses, summary ->
                _uiState.value = _uiState.value.copy(
                    providerStatuses = statuses,
                    summary = summary,
                    isRefreshing = false
                )
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
                
                // Load cached data first
                val cachedStatuses = healthStatusRepository.getAllHealthStatuses()
                val cachedSummary = healthStatusRepository.getHealthSummary()
                
                _uiState.value = _uiState.value.copy(
                    providerStatuses = cachedStatuses,
                    summary = cachedSummary
                )
                
                // If no cached data, trigger immediate health check
                if (cachedStatuses.isEmpty()) {
                    performHealthCheck()
                } else {
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.HEALTH_CHECK,
                    "HealthStatusViewModel",
                    "Failed to load initial data",
                    mapOf("error" to (e.message ?: "Unknown error")),
                    e
                )
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load health data: ${e.message}",
                    isRefreshing = false
                )
            }
        }
    }

    fun triggerHealthCheck() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
                
                StructuredLogger.info(
                    StructuredLogger.LogCategory.HEALTH_CHECK,
                    "HealthStatusViewModel",
                    "Manual health check triggered"
                )
                
                performHealthCheck()
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.HEALTH_CHECK,
                    "HealthStatusViewModel",
                    "Manual health check failed",
                    mapOf("error" to (e.message ?: "Unknown error")),
                    e
                )
                _uiState.value = _uiState.value.copy(
                    error = "Health check failed: ${e.message}",
                    isRefreshing = false
                )
            }
        }
    }

    private suspend fun performHealthCheck() {
        try {
            // Perform health checks on all providers
            val healthStatuses = apiHealthRegistry.checkAllHealth()
            
            // Save to repository (which will trigger the Flow observers)
            healthStatusRepository.saveHealthStatuses(healthStatuses.values.toList())
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.HEALTH_CHECK,
                "HealthStatusViewModel",
                "Health check completed successfully",
                mapOf(
                    "provider_count" to healthStatuses.size,
                    "healthy_count" to healthStatuses.values.count { it.isHealthy }
                )
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun clearHealthData() {
        viewModelScope.launch {
            try {
                healthStatusRepository.clearAllHealthStatus()
                
                _uiState.value = _uiState.value.copy(
                    providerStatuses = emptyList(),
                    summary = HealthSummary(
                        totalProviders = 0,
                        healthyCount = 0,
                        degradedCount = 0,
                        downCount = 0,
                        lastUpdated = 0L
                    ),
                    error = null
                )
                
                StructuredLogger.info(
                    StructuredLogger.LogCategory.HEALTH_CHECK,
                    "HealthStatusViewModel",
                    "Health data cleared"
                )
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.HEALTH_CHECK,
                    "HealthStatusViewModel",
                    "Failed to clear health data",
                    mapOf("error" to (e.message ?: "Unknown error")),
                    e
                )
                _uiState.value = _uiState.value.copy(
                    error = "Failed to clear data: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for health status screen
 */
data class HealthStatusUiState(
    val providerStatuses: List<HealthStatus> = emptyList(),
    val summary: HealthSummary? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null
)