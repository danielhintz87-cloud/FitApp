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
 * ViewModel for Plan Screen
 * Manages equipment selection state for workout plan generation
 */
@HiltViewModel
class PlanViewModel
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

        fun clearEquipment() {
            viewModelScope.launch {
                userPreferencesRepository.updateSelectedEquipment(emptySet())
                _selectedEquipment.value = emptySet()
            }
        }

        fun refreshEquipment() {
            loadEquipment()
        }
    }
