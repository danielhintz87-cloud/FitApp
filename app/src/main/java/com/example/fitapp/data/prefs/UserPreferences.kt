package com.example.fitapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.first

/**
 * Interface for user preferences operations
 */
interface UserPreferences {
    suspend fun clearWorkoutPreferences()
    suspend fun clearNutritionPreferences()
    suspend fun clearUserPreferences()
    suspend fun clearAchievementPreferences()
    suspend fun clearAllPreferences()
    
    // Equipment functions
    suspend fun getSelectedEquipment(): Set<String>
    suspend fun saveSelectedEquipment(equipment: Set<String>)
}

/**
 * DataStore-based implementation of UserPreferences
 * Provides type-safe, async preference storage with reactive updates
 */
class UserPreferencesDataStoreImpl(
    private val repository: UserPreferencesRepository
) : UserPreferences {
    
    override suspend fun clearWorkoutPreferences() {
        repository.clearWorkoutPreferences()
    }
    
    override suspend fun clearNutritionPreferences() {
        repository.clearNutritionPreferences()
    }
    
    override suspend fun clearUserPreferences() {
        repository.clearUserPreferences()
    }
    
    override suspend fun clearAchievementPreferences() {
        repository.clearAchievementPreferences()
    }
    
    override suspend fun clearAllPreferences() {
        repository.clearAllPreferences()
    }
    
    override suspend fun getSelectedEquipment(): Set<String> {
        return repository.selectedEquipment.first()
    }
    
    override suspend fun saveSelectedEquipment(equipment: Set<String>) {
        repository.updateSelectedEquipment(equipment)
    }
}

/**
 * Simple implementation of UserPreferences using SharedPreferences
 * DEPRECATED: Use UserPreferencesDataStoreImpl instead
 * Kept for backward compatibility during migration
 */
@Deprecated("Use UserPreferencesDataStoreImpl instead")
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
    
    override suspend fun getSelectedEquipment(): Set<String> {
        val stored = getSharedPrefs().getString("selected_equipment", "") ?: ""
        return if (stored.isBlank()) emptySet() else stored.split(",").filter { it.isNotBlank() }.toSet()
    }
    
    override suspend fun saveSelectedEquipment(equipment: Set<String>) {
        getSharedPrefs().edit().putString("selected_equipment", equipment.joinToString(",")).apply()
    }
}

/**
 * Legacy helper for storing and retrieving user preferences from SharedPreferences
 * DEPRECATED: Use UserPreferencesRepository instead
 * Kept for backward compatibility during migration
 */
@Deprecated("Use UserPreferencesRepository instead")
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