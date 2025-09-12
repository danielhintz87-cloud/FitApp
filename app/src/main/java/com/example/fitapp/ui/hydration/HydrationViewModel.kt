package com.example.fitapp.ui.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.domain.usecases.HydrationGoalUseCase
import com.example.fitapp.domain.usecases.HydrationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for hydration tracking screen demonstrating reactive flows.
 * Updates immediately when hydration goals or water intake change.
 */
@HiltViewModel
class HydrationViewModel @Inject constructor(
    private val hydrationGoalUseCase: HydrationGoalUseCase
) : ViewModel() {
    
    /**
     * Current hydration status as reactive state
     */
    val hydrationStatus: StateFlow<HydrationStatus?> = hydrationGoalUseCase
        .hydrationStatus
        .catch { emit(null) } // Handle errors gracefully in UI
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    /**
     * Current hydration goal in liters
     */
    val hydrationGoal: StateFlow<Double> = hydrationGoalUseCase
        .hydrationGoal
        .catch { emit(2.0) } // Fallback to 2 liters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2.0
        )
    
    /**
     * UI state combining multiple flows
     */
    data class UiState(
        val status: HydrationStatus?,
        val goal: Double,
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    val uiState: StateFlow<UiState> = combine(
        hydrationStatus,
        hydrationGoal
    ) { status, goal ->
        UiState(
            status = status,
            goal = goal,
            isLoading = status == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(null, 2.0, isLoading = true)
    )
    
    /**
     * Adds water consumption and immediately updates the UI through reactive flows
     */
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            try {
                hydrationGoalUseCase.addWaterConsumption(amountMl)
                // UI will update automatically via the reactive flows
            } catch (e: Exception) {
                // Handle error - in a real app you'd emit error state
            }
        }
    }
    
    /**
     * Updates the daily hydration goal
     */
    fun updateGoal(goalLiters: Double) {
        viewModelScope.launch {
            try {
                hydrationGoalUseCase.updateHydrationGoal(goalLiters)
                // Goal flow will update automatically
                // Status flow will recalculate progress automatically
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Clears today's water entries
     */
    fun clearTodaysWater() {
        viewModelScope.launch {
            try {
                val today = com.example.fitapp.util.time.TimeZoneUtils.getCurrentLocalDate()
                hydrationGoalUseCase.clearWaterForDate(today)
                // UI will update automatically via reactive flows
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Gets hydration history for the chart
     */
    fun getWeeklyHistory() = flow {
        try {
            val history = hydrationGoalUseCase.getHydrationProgressHistory(7)
            emit(history)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(viewModelScope.coroutineContext)
}