package com.example.fitapp.settings

import com.example.fitapp.data.datastore.UserPreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResetManager @Inject constructor(
    private val userPrefs: UserPreferencesDataStore
) {
    suspend fun clearWorkoutPreferences() = userPrefs.clearWorkoutPreferences()
    suspend fun clearNutritionPreferences() = userPrefs.clearNutritionPreferences()
    suspend fun clearUserPreferences() = userPrefs.clearUserPreferences()
    suspend fun clearAchievementPreferences() = userPrefs.clearAchievementPreferences()
    suspend fun clearAllPreferences() = userPrefs.clearAllPreferences()
}