package com.example.fitapp.data.datastore

import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataStore {
    suspend fun clearWorkoutPreferences()

    suspend fun clearNutritionPreferences()

    suspend fun clearUserPreferences()

    suspend fun clearAchievementPreferences()

    suspend fun clearAllPreferences()

    fun getSelectedEquipment(): Flow<Set<String>>

    suspend fun saveSelectedEquipment(equipment: Set<String>)
}
