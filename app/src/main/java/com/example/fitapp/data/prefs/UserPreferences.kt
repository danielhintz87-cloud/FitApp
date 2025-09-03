package com.example.fitapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import javax.inject.Inject

/**
 * Interface for user preferences operations
 */
interface UserPreferences {
    suspend fun clearWorkoutPreferences()
    suspend fun clearNutritionPreferences()
    suspend fun clearUserPreferences()
    suspend fun clearAchievementPreferences()
    suspend fun clearAllPreferences()
}

/**
 * Simple implementation of UserPreferences using SharedPreferences
 * TODO: Migrate to DataStore when DataStore setup is complete
 */
class UserPreferencesImpl(private val context: Context) : UserPreferences {
    
    private fun getSharedPrefs(): SharedPreferences {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }
    
    override suspend fun clearWorkoutPreferences() {
        // Clear workout-related preferences
        getSharedPrefs().edit().clear().apply()
    }
    
    override suspend fun clearNutritionPreferences() {
        // Clear nutrition-related preferences
        getSharedPrefs().edit().clear().apply()
    }
    
    override suspend fun clearUserPreferences() {
        // Clear user-related preferences
        getSharedPrefs().edit().clear().apply()
    }
    
    override suspend fun clearAchievementPreferences() {
        // Clear achievement-related preferences
        getSharedPrefs().edit().clear().apply()
    }
    
    override suspend fun clearAllPreferences() {
        getSharedPrefs().edit().clear().apply()
    }
}

/**
 * Legacy helper for storing and retrieving user preferences from SharedPreferences
 * TODO: Migrate to DataStore implementation above
 */
object UserPreferencesLegacy {
    private const val PREFS_NAME = "user_preferences"
    private const val KEY_SELECTED_EQUIPMENT = "selected_equipment"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSelectedEquipment(context: Context, equipment: List<String>) {
        getPrefs(context).edit().putString(KEY_SELECTED_EQUIPMENT, equipment.joinToString(",")).apply()
    }

    fun getSelectedEquipment(context: Context): List<String> {
        val stored = getPrefs(context).getString(KEY_SELECTED_EQUIPMENT, "") ?: ""
        return if (stored.isBlank()) emptyList() else stored.split(",").filter { it.isNotBlank() }
    }
}