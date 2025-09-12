package com.example.fitapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.io.File

/**
 * Comprehensive tests for Proto DataStore migration from SharedPreferences.
 * Tests migration from various SharedPreferences sources to ensure no data loss.
 */
@RunWith(AndroidJUnit4::class)
class UserPreferencesMigrationTest {

    private lateinit var context: Context
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userPreferencesRepository = UserPreferencesRepository(context)
        
        // Clear existing DataStore file to ensure clean test state
        cleanupDataStore()
    }
    
    @After
    fun tearDown() {
        cleanupDataStore()
        cleanupSharedPreferences()
    }
    
    private fun cleanupDataStore() {
        val dataStoreFile = File(context.filesDir, "datastore/user_preferences.pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
    }
    
    private fun cleanupSharedPreferences() {
        // Clear all SharedPreferences files used in migration
        val prefsFiles = listOf(
            "user_prefs",
            "user_preferences", 
            "fitapp_user_experience",
            "fasting_prefs"
        )
        
        prefsFiles.forEach { prefsName ->
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
    }
    
    @Test
    fun `should migrate workout preferences correctly`() = runTest {
        // Given: SharedPreferences with workout data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putBoolean("notifications_enabled", false)
            .putInt("default_rest_time", 90)
            .putBoolean("sound_enabled", false)
            .putBoolean("vibration_enabled", true)
            .putString("selected_equipment", "Dumbbells,Barbell,Bench")
            .apply()
        
        // When: Migration is performed
        val migrated = userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: Migration should succeed and data should be preserved
        assertTrue("Migration should succeed", migrated)
        
        val prefs = userPreferencesRepository.userPreferences.first()
        assertFalse("Notifications should be disabled", prefs.notificationsEnabled)
        assertEquals("Rest time should be 90 seconds", 90, prefs.defaultRestTimeSeconds)
        assertFalse("Sound should be disabled", prefs.soundEnabled)
        assertTrue("Vibration should be enabled", prefs.vibrationEnabled)
        
        val equipment = prefs.selectedEquipmentList.toSet()
        assertTrue("Should contain Dumbbells", equipment.contains("Dumbbells"))
        assertTrue("Should contain Barbell", equipment.contains("Barbell"))
        assertTrue("Should contain Bench", equipment.contains("Bench"))
    }
    
    @Test
    fun `should migrate nutrition preferences correctly`() = runTest {
        // Given: SharedPreferences with nutrition data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putInt("daily_calorie_goal", 2500)
            .putFloat("daily_water_goal", 3.5f)
            .putBoolean("nutrition_reminders", false)
            .apply()
        
        // When: Migration is performed
        userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: Nutrition data should be preserved
        val prefs = userPreferencesRepository.userPreferences.first()
        assertEquals("Calorie goal should be 2500", 2500, prefs.dailyCalorieGoal)
        assertEquals("Water goal should be 3.5L", 3.5, prefs.dailyWaterGoalLiters, 0.01)
        assertFalse("Nutrition reminders should be disabled", prefs.nutritionRemindersEnabled)
    }
    
    @Test
    fun `should migrate user profile correctly`() = runTest {
        // Given: SharedPreferences with user profile data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("user_name", "Test User")
            .putInt("user_age", 30)
            .putFloat("user_weight", 75.5f)
            .putFloat("user_height", 180.0f)
            .apply()
        
        // When: Migration is performed
        userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: User profile should be preserved
        val prefs = userPreferencesRepository.userPreferences.first()
        assertEquals("User name should be preserved", "Test User", prefs.userName)
        assertEquals("Age should be preserved", 30, prefs.age)
        assertEquals("Weight should be preserved", 75.5, prefs.weightKg, 0.01)
        assertEquals("Height should be preserved", 180.0, prefs.heightCm, 0.01)
    }
    
    @Test
    fun `should migrate app preferences correctly`() = runTest {
        // Given: SharedPreferences with app settings
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("theme_mode", "dark")
            .putString("language", "en")
            .putBoolean("achievement_notifications", false)
            .apply()
        
        // When: Migration is performed
        userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: App settings should be preserved
        val prefs = userPreferencesRepository.userPreferences.first()
        assertEquals("Theme mode should be dark", "dark", prefs.themeMode)
        assertEquals("Language should be en", "en", prefs.language)
        assertFalse("Achievement notifications should be disabled", prefs.achievementNotificationsEnabled)
    }
    
    @Test
    fun `should migrate user experience data correctly`() = runTest {
        // Given: SharedPreferences with user experience data
        val userExpPrefs = context.getSharedPreferences("fitapp_user_experience", Context.MODE_PRIVATE)
        userExpPrefs.edit()
            .putBoolean("onboarding_completed", true)
            .putBoolean("first_launch", false)
            .putBoolean("unified_dashboard_shown", true)
            .putStringSet("features_discovered", setOf("bmi_calculator", "barcode_scanner"))
            .putString("app_version_seen", "1.2.0")
            .apply()
        
        // When: Migration is performed
        userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: User experience data should be preserved
        val prefs = userPreferencesRepository.userPreferences.first()
        assertTrue("Onboarding should be completed", prefs.onboardingCompleted)
        assertFalse("Should not be first launch", prefs.firstLaunch)
        assertTrue("Unified dashboard should be shown", prefs.unifiedDashboardShown)
        assertEquals("App version should be preserved", "1.2.0", prefs.appVersionSeen)
        
        val discoveredFeatures = prefs.featuresDiscoveredList.toSet()
        assertTrue("Should contain bmi_calculator", discoveredFeatures.contains("bmi_calculator"))
        assertTrue("Should contain barcode_scanner", discoveredFeatures.contains("barcode_scanner"))
    }
    
    @Test
    fun `should migrate fasting data correctly`() = runTest {
        // Given: SharedPreferences with fasting data
        val fastingPrefs = context.getSharedPreferences("fasting_prefs", Context.MODE_PRIVATE)
        val fastStartTime = System.currentTimeMillis() / 1000 // Convert to seconds
        fastingPrefs.edit()
            .putBoolean("is_fasting", true)
            .putLong("fast_start_time", fastStartTime)
            .apply()
        
        // When: Migration is performed
        userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: Fasting data should be preserved
        val prefs = userPreferencesRepository.userPreferences.first()
        assertTrue("Fasting should be enabled", prefs.fastingEnabled)
        assertEquals("Fast start time should be preserved", 
            fastStartTime * 1000, // Convert back to milliseconds
            prefs.fastingStartTimeMillis)
        assertEquals("Default fasting duration should be 16 hours", 16, prefs.fastingDurationHours)
        assertTrue("Fasting notifications should be enabled by default", prefs.fastingNotificationsEnabled)
    }
    
    @Test
    fun `should use defaults when SharedPreferences are empty`() = runTest {
        // Given: Empty SharedPreferences
        // (no setup needed, SharedPreferences are empty by default)
        
        // When: Migration is performed
        userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: Default values should be used
        val prefs = userPreferencesRepository.userPreferences.first()
        
        // Workout defaults
        assertTrue("Notifications should default to enabled", prefs.notificationsEnabled)
        assertEquals("Rest time should default to 60", 60, prefs.defaultRestTimeSeconds)
        assertTrue("Sound should default to enabled", prefs.soundEnabled)
        assertTrue("Vibration should default to enabled", prefs.vibrationEnabled)
        
        // Nutrition defaults
        assertEquals("Calorie goal should default to 2000", 2000, prefs.dailyCalorieGoal)
        assertEquals("Water goal should default to 2.0L", 2.0, prefs.dailyWaterGoalLiters, 0.01)
        assertTrue("Nutrition reminders should default to enabled", prefs.nutritionRemindersEnabled)
        
        // App defaults
        assertEquals("Theme should default to system", "system", prefs.themeMode)
        assertEquals("Language should default to de", "de", prefs.language)
        assertTrue("Achievement notifications should default to enabled", prefs.achievementNotificationsEnabled)
        
        // User experience defaults
        assertTrue("Should default to first launch", prefs.firstLaunch)
        assertFalse("Onboarding should default to not completed", prefs.onboardingCompleted)
        assertFalse("Unified dashboard should default to not shown", prefs.unifiedDashboardShown)
        assertEquals("App version should default to 1.0.0", "1.0.0", prefs.appVersionSeen)
        
        // Migration metadata
        assertEquals("Preferences version should be 2", 2, prefs.preferencesVersion)
        assertTrue("Should be marked as migrated", prefs.migratedFromSharedPrefs)
    }
    
    @Test
    fun `should not migrate twice`() = runTest {
        // Given: SharedPreferences with some data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("user_name", "First Migration")
            .apply()
        
        // When: First migration
        val firstMigration = userPreferencesRepository.migrateFromSharedPreferences()
        
        // And: SharedPreferences are changed after first migration
        sharedPrefs.edit()
            .putString("user_name", "Second Migration")
            .apply()
        
        // And: Second migration attempt
        val secondMigration = userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: First migration should succeed, second should be skipped
        assertTrue("First migration should succeed", firstMigration)
        assertFalse("Second migration should be skipped", secondMigration)
        
        // And: Data from first migration should be preserved
        val prefs = userPreferencesRepository.userPreferences.first()
        assertEquals("Should preserve data from first migration", "First Migration", prefs.userName)
    }
    
    @Test
    fun `should handle migration with mixed SharedPreferences sources`() = runTest {
        // Given: Data spread across multiple SharedPreferences files
        val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userPrefs.edit()
            .putString("user_name", "John Doe")
            .putInt("daily_calorie_goal", 2200)
            .apply()
        
        val legacyPrefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        legacyPrefs.edit()
            .putString("selected_equipment", "Legacy,Equipment")
            .apply()
        
        val userExpPrefs = context.getSharedPreferences("fitapp_user_experience", Context.MODE_PRIVATE)
        userExpPrefs.edit()
            .putBoolean("onboarding_completed", true)
            .apply()
        
        val fastingPrefs = context.getSharedPreferences("fasting_prefs", Context.MODE_PRIVATE)
        fastingPrefs.edit()
            .putBoolean("is_fasting", true)
            .apply()
        
        // When: Migration is performed
        userPreferencesRepository.migrateFromSharedPreferences()
        
        // Then: Data from all sources should be combined
        val prefs = userPreferencesRepository.userPreferences.first()
        assertEquals("User name from main prefs", "John Doe", prefs.userName)
        assertEquals("Calorie goal from main prefs", 2200, prefs.dailyCalorieGoal)
        assertTrue("Onboarding from experience prefs", prefs.onboardingCompleted)
        assertTrue("Fasting from fasting prefs", prefs.fastingEnabled)
        
        val equipment = prefs.selectedEquipmentList.toSet()
        assertTrue("Legacy equipment should be migrated", equipment.contains("Legacy"))
        assertTrue("Legacy equipment should be migrated", equipment.contains("Equipment"))
    }
}