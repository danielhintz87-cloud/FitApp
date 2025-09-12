package com.example.fitapp.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Interface for user preferences service with clear methods
 */
interface UserPreferencesServiceInterface {
    suspend fun clearWorkoutPreferences()
    suspend fun clearNutritionPreferences()
    suspend fun clearUserPreferences()
    suspend fun clearAchievementPreferences()
    suspend fun clearAllPreferences()
    suspend fun getSelectedEquipment(): Set<String>
    suspend fun saveSelectedEquipment(equipment: Set<String>)
}

/**
 * Proto DataStore based implementation for UserPreferences.
 * Migrated from SharedPreferences and Preferences DataStore to Proto DataStore.
 */
class UserDataStore(
    private val context: Context
) : UserPreferencesServiceInterface {
    
    // Use the same proto DataStore as UserPreferencesRepository
    private val Context.dataStore: DataStore<UserPreferencesProto> by dataStore(
        fileName = "user_preferences.pb",
        serializer = UserPreferencesSerializer
    )

    override suspend fun clearWorkoutPreferences() {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .clearSelectedEquipment()
                .setNotificationsEnabled(true) // Reset to defaults
                .setDefaultRestTimeSeconds(60)
                .setSoundEnabled(true)
                .setVibrationEnabled(true)
                .build()
        }
    }

    override suspend fun clearNutritionPreferences() {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setDailyCalorieGoal(2000) // Reset to defaults
                .setDailyWaterGoalLiters(2.0)
                .setNutritionRemindersEnabled(true)
                .build()
        }
    }

    override suspend fun clearUserPreferences() {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setUserName("")
                .setAge(0)
                .setWeightKg(0.0)
                .setHeightCm(0.0)
                .build()
        }
    }

    override suspend fun clearAchievementPreferences() {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setAchievementNotificationsEnabled(true) // Reset to default
                .build()
        }
    }

    override suspend fun clearAllPreferences() {
        context.dataStore.updateData {
            UserPreferencesProto.getDefaultInstance()
        }
    }

    override suspend fun getSelectedEquipment(): Set<String> {
        return context.dataStore.data.map { prefs ->
            prefs.selectedEquipmentList.toSet()
        }.first()
    }

    override suspend fun saveSelectedEquipment(equipment: Set<String>) {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .clearSelectedEquipment()
                .addAllSelectedEquipment(equipment)
                .build()
        }
    }

    /**
     * Helper Flow for UI reactivity (optional use)
     */
    val selectedEquipmentFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs.selectedEquipmentList.toSet()
    }
}

/**
 * Factory Method for migration assistance: now always uses Proto DataStore
 * @deprecated Use UserPreferencesRepository with Dependency Injection instead
 */
@Deprecated("Use UserPreferencesRepository with Dependency Injection instead")
object UserPreferencesFactory {
    fun create(context: Context, preferDataStore: Boolean = true): UserPreferencesServiceInterface {
        return UserDataStore(context)
    }
}
