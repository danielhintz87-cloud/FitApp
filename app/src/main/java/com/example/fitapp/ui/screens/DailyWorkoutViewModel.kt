package com.example.fitapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.data.prefs.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Daily Workout Screen
 * Manages equipment selection for workout generation
 */
@HiltViewModel
class DailyWorkoutViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val _selectedEquipment = MutableStateFlow<Set<String>>(emptySet())
        val selectedEquipment: StateFlow<Set<String>> = _selectedEquipment.asStateFlow()

        init {
            loadEquipment()
        }

        private fun loadEquipment() {
            viewModelScope.launch {
                _selectedEquipment.value = userPreferencesRepository.selectedEquipment.first()
            }
        }

        suspend fun getEquipmentList(): List<String> {
            return userPreferencesRepository.selectedEquipment.first().toList()
        }
    }
