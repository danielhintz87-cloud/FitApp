package com.example.fitapp.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResetSettingsViewModel @Inject constructor(
    private val resetManager: ResetManager
) : ViewModel() {
    suspend fun clearWorkout() = resetManager.clearWorkoutPreferences()
    suspend fun clearNutrition() = resetManager.clearNutritionPreferences()
    suspend fun clearUser() = resetManager.clearUserPreferences()
    suspend fun clearAchievements() = resetManager.clearAchievementPreferences()
    suspend fun clearAll() = resetManager.clearAllPreferences()
}