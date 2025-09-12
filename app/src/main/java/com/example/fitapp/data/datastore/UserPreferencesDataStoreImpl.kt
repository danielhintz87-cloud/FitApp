package com.example.fitapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.fitapp.data.prefs.UserPreferencesProto
import com.example.fitapp.data.prefs.UserPreferencesSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_FILE_NAME = "user_preferences.pb"

// DataStore instance using Proto
private val Context.userPreferencesStore: DataStore<UserPreferencesProto> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = UserPreferencesSerializer
)

@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesDataStore {

    private val dataStore = context.userPreferencesStore

    override suspend fun clearWorkoutPreferences() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .clearSelectedEquipment()
                .setNotificationsEnabled(true) // Reset to default
                .setDefaultRestTimeSeconds(60) // Reset to default
                .setSoundEnabled(true) // Reset to default
                .setVibrationEnabled(true) // Reset to default
                .build()
        }
    }

    override suspend fun clearNutritionPreferences() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setDailyCalorieGoal(2000) // Reset to default
                .setDailyWaterGoalLiters(2.0) // Reset to default
                .setNutritionRemindersEnabled(true) // Reset to default
                .build()
        }
    }

    override suspend fun clearUserPreferences() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setUserName("") // Clear user data
                .setAge(0)
                .setWeightKg(0.0)
                .setHeightCm(0.0)
                .build()
        }
    }

    override suspend fun clearAchievementPreferences() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setAchievementNotificationsEnabled(true) // Reset to default
                .build()
        }
    }

    override suspend fun clearAllPreferences() {
        dataStore.updateData {
            UserPreferencesProto.getDefaultInstance()
        }
    }

    override fun getSelectedEquipment(): Flow<Set<String>> =
        dataStore.data.map { prefs -> prefs.selectedEquipmentList.toSet() }

    override suspend fun saveSelectedEquipment(equipment: Set<String>) {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .clearSelectedEquipment()
                .addAllSelectedEquipment(equipment)
                .build()
        }
    }
}