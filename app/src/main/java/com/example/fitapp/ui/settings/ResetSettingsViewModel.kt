package com.example.fitapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.data.prefs.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Reset Settings Screen
 * Manages reset operations using UserPreferencesRepository
 */
@HiltViewModel
class ResetSettingsViewModel
    @Inject
    constructor(
        val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        fun resetAllPreferences() {
            viewModelScope.launch {
                userPreferencesRepository.clearAllPreferences()
            }
        }

        fun resetWorkoutPreferences() {
            viewModelScope.launch {
                userPreferencesRepository.clearWorkoutPreferences()
            }
        }

        fun resetNutritionPreferences() {
            viewModelScope.launch {
                userPreferencesRepository.clearNutritionPreferences()
            }
        }

        fun resetUserProfile() {
            viewModelScope.launch {
                userPreferencesRepository.clearUserPreferences()
            }
        }

        fun resetAchievements() {
            viewModelScope.launch {
                userPreferencesRepository.clearAchievementPreferences()
            }
        }
    }
