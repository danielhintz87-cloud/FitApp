package com.example.fitapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * DataStore-based repository for user preferences.
 * Provides migration from SharedPreferences and type-safe preference access.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val DATA_STORE_FILE_NAME = "user_preferences.pb"
        private const val SHARED_PREFS_NAME = "user_prefs"
        private const val LEGACY_PREFS_NAME = "user_preferences"
    }

    // DataStore instance
    private val Context.userPreferencesStore: DataStore<UserPreferencesProto> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer,
    )

    private val dataStore = context.userPreferencesStore

    // Flows for reactive data access
    val userPreferences: Flow<UserPreferencesProto> =
        dataStore.data
            .catch { exception ->
                // dataStore.data throws an IOException for serialization errors
                if (exception is IOException) {
                    android.util.Log.e("UserPrefsRepo", "Error reading preferences.", exception)
                    emit(UserPreferencesProto.getDefaultInstance())
                } else {
                    throw exception
                }
            }

    // Equipment preferences
    val selectedEquipment: Flow<Set<String>> =
        userPreferences.map { prefs ->
            prefs.selectedEquipmentList.toSet()
        }

    // Workout preferences
    val workoutPreferences: Flow<WorkoutPreferences> =
        userPreferences.map { prefs ->
            WorkoutPreferences(
                notificationsEnabled = prefs.notificationsEnabled,
                defaultRestTimeSeconds = prefs.defaultRestTimeSeconds,
                soundEnabled = prefs.soundEnabled,
                vibrationEnabled = prefs.vibrationEnabled,
            )
        }

    // Nutrition preferences
    val nutritionPreferences: Flow<NutritionPreferences> =
        userPreferences.map { prefs ->
            NutritionPreferences(
                dailyCalorieGoal = prefs.dailyCalorieGoal,
                dailyWaterGoalLiters = prefs.dailyWaterGoalLiters,
                nutritionRemindersEnabled = prefs.nutritionRemindersEnabled,
            )
        }

    // User profile
    val userProfile: Flow<UserProfile> =
        userPreferences.map { prefs ->
            UserProfile(
                userName = prefs.userName,
                age = prefs.age,
                weightKg = prefs.weightKg,
                heightCm = prefs.heightCm,
            )
        }

    // App preferences
    val appPreferences: Flow<AppPreferences> =
        userPreferences.map { prefs ->
            AppPreferences(
                themeMode = prefs.themeMode,
                language = prefs.language,
                achievementNotificationsEnabled = prefs.achievementNotificationsEnabled,
            )
        }

    // Sync timestamps
    val syncTimestamps: Flow<SyncTimestamps> =
        userPreferences.map { prefs ->
            SyncTimestamps(
                lastHealthConnectSyncMillis = prefs.lastHealthConnectSyncMillis,
                lastCloudSyncMillis = prefs.lastCloudSyncMillis,
            )
        }

    /**
     * Migration from SharedPreferences to DataStore.
     * Call this once when the app starts.
     */
    suspend fun migrateFromSharedPreferences(): Boolean {
        val currentPrefs = dataStore.data.first()

        // Skip if already migrated
        if (currentPrefs.migratedFromSharedPrefs) {
            return false
        }

        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)

        // Migrate data
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                // Equipment
                .addAllSelectedEquipment(migrateEquipment(sharedPrefs, legacyPrefs))
                // Workout settings (with defaults)
                .setNotificationsEnabled(sharedPrefs.getBoolean("notifications_enabled", true))
                .setDefaultRestTimeSeconds(sharedPrefs.getInt("default_rest_time", 60))
                .setSoundEnabled(sharedPrefs.getBoolean("sound_enabled", true))
                .setVibrationEnabled(sharedPrefs.getBoolean("vibration_enabled", true))
                // Nutrition settings
                .setDailyCalorieGoal(sharedPrefs.getInt("daily_calorie_goal", 2000))
                .setDailyWaterGoalLiters(sharedPrefs.getFloat("daily_water_goal", 2.0f).toDouble())
                .setNutritionRemindersEnabled(sharedPrefs.getBoolean("nutrition_reminders", true))
                // User profile
                .setUserName(sharedPrefs.getString("user_name", "") ?: "")
                .setAge(sharedPrefs.getInt("user_age", 0))
                .setWeightKg(sharedPrefs.getFloat("user_weight", 0f).toDouble())
                .setHeightCm(sharedPrefs.getFloat("user_height", 0f).toDouble())
                // App settings
                .setThemeMode(sharedPrefs.getString("theme_mode", "system") ?: "system")
                .setLanguage(sharedPrefs.getString("language", "de") ?: "de")
                .setAchievementNotificationsEnabled(sharedPrefs.getBoolean("achievement_notifications", true))
                // Migration metadata
                .setPreferencesVersion(1)
                .setMigratedFromSharedPrefs(true)
                .build()
        }

        android.util.Log.i("UserPrefsRepo", "Successfully migrated preferences from SharedPreferences to DataStore")
        return true
    }

    private fun migrateEquipment(
        sharedPrefs: SharedPreferences,
        legacyPrefs: SharedPreferences,
    ): List<String> {
        // Try current format first
        val current = sharedPrefs.getString("selected_equipment", "")
        if (!current.isNullOrBlank()) {
            return current.split(",").filter { it.isNotBlank() }
        }

        // Try legacy format
        val legacy = legacyPrefs.getString("selected_equipment", "")
        if (!legacy.isNullOrBlank()) {
            return legacy.split(",").filter { it.isNotBlank() }
        }

        return emptyList()
    }

    // Update methods
    suspend fun updateSelectedEquipment(equipment: Set<String>) {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .clearSelectedEquipment()
                .addAllSelectedEquipment(equipment)
                .build()
        }
    }

    suspend fun updateWorkoutPreferences(
        notificationsEnabled: Boolean? = null,
        defaultRestTimeSeconds: Int? = null,
        soundEnabled: Boolean? = null,
        vibrationEnabled: Boolean? = null,
    ) {
        dataStore.updateData { prefs ->
            val builder = prefs.toBuilder()
            notificationsEnabled?.let { builder.setNotificationsEnabled(it) }
            defaultRestTimeSeconds?.let { builder.setDefaultRestTimeSeconds(it) }
            soundEnabled?.let { builder.setSoundEnabled(it) }
            vibrationEnabled?.let { builder.setVibrationEnabled(it) }
            builder.build()
        }
    }

    suspend fun updateNutritionPreferences(
        dailyCalorieGoal: Int? = null,
        dailyWaterGoalLiters: Double? = null,
        nutritionRemindersEnabled: Boolean? = null,
    ) {
        dataStore.updateData { prefs ->
            val builder = prefs.toBuilder()
            dailyCalorieGoal?.let { builder.setDailyCalorieGoal(it) }
            dailyWaterGoalLiters?.let { builder.setDailyWaterGoalLiters(it) }
            nutritionRemindersEnabled?.let { builder.setNutritionRemindersEnabled(it) }
            builder.build()
        }
    }

    suspend fun updateUserProfile(
        userName: String? = null,
        age: Int? = null,
        weightKg: Double? = null,
        heightCm: Double? = null,
    ) {
        dataStore.updateData { prefs ->
            val builder = prefs.toBuilder()
            userName?.let { builder.setUserName(it) }
            age?.let { builder.setAge(it) }
            weightKg?.let { builder.setWeightKg(it) }
            heightCm?.let { builder.setHeightCm(it) }
            builder.build()
        }
    }

    suspend fun updateAppPreferences(
        themeMode: String? = null,
        language: String? = null,
        achievementNotificationsEnabled: Boolean? = null,
    ) {
        dataStore.updateData { prefs ->
            val builder = prefs.toBuilder()
            themeMode?.let { builder.setThemeMode(it) }
            language?.let { builder.setLanguage(it) }
            achievementNotificationsEnabled?.let { builder.setAchievementNotificationsEnabled(it) }
            builder.build()
        }
    }

    suspend fun updateShoppingListPreferences(sortingMode: String? = null) {
        dataStore.updateData { prefs ->
            val builder = prefs.toBuilder()
            sortingMode?.let { builder.setShoppingListSortingMode(it) }
            builder.build()
        }
    }

    suspend fun updateSyncTimestamps(
        lastHealthConnectSyncMillis: Long? = null,
        lastCloudSyncMillis: Long? = null,
    ) {
        dataStore.updateData { prefs ->
            val builder = prefs.toBuilder()
            lastHealthConnectSyncMillis?.let { builder.setLastHealthConnectSyncMillis(it) }
            lastCloudSyncMillis?.let { builder.setLastCloudSyncMillis(it) }
            builder.build()
        }
    }

    suspend fun clearAllPreferences() {
        dataStore.updateData {
            UserPreferencesProto.getDefaultInstance()
        }
    }

    suspend fun resetAll() {
        clearAllPreferences()
    }

    suspend fun clearWorkoutPreferences() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setNotificationsEnabled(true)
                .setDefaultRestTimeSeconds(60)
                .setSoundEnabled(true)
                .setVibrationEnabled(true)
                .build()
        }
    }

    suspend fun clearNutritionPreferences() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setDailyCalorieGoal(2000)
                .setDailyWaterGoalLiters(2.0)
                .setNutritionRemindersEnabled(true)
                .build()
        }
    }

    suspend fun clearUserPreferences() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setUserName("")
                .setAge(0)
                .setWeightKg(0.0)
                .setHeightCm(0.0)
                .build()
        }
    }

    suspend fun clearAchievementPreferences() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setAchievementNotificationsEnabled(true)
                .build()
        }
    }
}

// Data classes for structured access
data class WorkoutPreferences(
    val notificationsEnabled: Boolean = true,
    val defaultRestTimeSeconds: Int = 60,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
)

data class NutritionPreferences(
    val dailyCalorieGoal: Int = 2000,
    val dailyWaterGoalLiters: Double = 2.0,
    val nutritionRemindersEnabled: Boolean = true,
)

data class UserProfile(
    val userName: String = "",
    val age: Int = 0,
    val weightKg: Double = 0.0,
    val heightCm: Double = 0.0,
)

data class AppPreferences(
    val themeMode: String = "system", // "light", "dark", "system"
    val language: String = "de",
    val achievementNotificationsEnabled: Boolean = true,
)

data class SyncTimestamps(
    val lastHealthConnectSyncMillis: Long = 0L,
    val lastCloudSyncMillis: Long = 0L,
)
