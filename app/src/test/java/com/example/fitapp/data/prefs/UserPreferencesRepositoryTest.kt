package com.example.fitapp.data.prefs

import android.content.Context
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

/**
 * Tests for DataStore migration and UserPreferencesRepository
 */
@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryTest {
    
    private lateinit var context: Context
    private lateinit var repository: UserPreferencesRepository
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = UserPreferencesRepository(context)
        
        // Clear any existing preferences
        clearSharedPreferences()
    }
    
    @After
    fun tearDown() {
        clearSharedPreferences()
    }
    
    private fun clearSharedPreferences() {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
    
    @Test
    fun testDefaultValues() = runTest {
        val preferences = repository.userPreferences.first()
        
        assertEquals(false, preferences.migratedFromSharedPrefs)
        assertEquals(0, preferences.preferencesVersion)
        assertTrue(preferences.selectedEquipmentList.isEmpty())
        assertEquals(true, preferences.notificationsEnabled)
        assertEquals(0, preferences.defaultRestTimeSeconds)
    }
    
    @Test
    fun testEquipmentMigration() = runTest {
        // Setup SharedPreferences data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("selected_equipment", "Hanteln,Matte,Widerstandsband")
            .apply()
        
        // Trigger migration
        val migrated = repository.migrateFromSharedPreferences()
        assertTrue("Migration should return true for first run", migrated)
        
        // Verify migrated data
        val equipment = repository.selectedEquipment.first()
        assertEquals(setOf("Hanteln", "Matte", "Widerstandsband"), equipment)
        
        // Verify migration flag
        val preferences = repository.userPreferences.first()
        assertTrue(preferences.migratedFromSharedPrefs)
        assertEquals(1, preferences.preferencesVersion)
    }
    
    @Test
    fun testLegacyEquipmentMigration() = runTest {
        // Setup legacy SharedPreferences data
        val legacyPrefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        legacyPrefs.edit()
            .putString("selected_equipment", "Kettlebell,Yoga Block")
            .apply()
        
        // Trigger migration
        repository.migrateFromSharedPreferences()
        
        // Verify migrated data
        val equipment = repository.selectedEquipment.first()
        assertEquals(setOf("Kettlebell", "Yoga Block"), equipment)
    }
    
    @Test
    fun testWorkoutPreferencesMigration() = runTest {
        // Setup SharedPreferences data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putBoolean("notifications_enabled", false)
            .putInt("default_rest_time", 90)
            .putBoolean("sound_enabled", false)
            .putBoolean("vibration_enabled", true)
            .apply()
        
        // Trigger migration
        repository.migrateFromSharedPreferences()
        
        // Verify migrated data
        val workoutPrefs = repository.workoutPreferences.first()
        assertEquals(false, workoutPrefs.notificationsEnabled)
        assertEquals(90, workoutPrefs.defaultRestTimeSeconds)
        assertEquals(false, workoutPrefs.soundEnabled)
        assertEquals(true, workoutPrefs.vibrationEnabled)
    }
    
    @Test
    fun testNutritionPreferencesMigration() = runTest {
        // Setup SharedPreferences data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putInt("daily_calorie_goal", 2500)
            .putFloat("daily_water_goal", 3.5f)
            .putBoolean("nutrition_reminders", false)
            .apply()
        
        // Trigger migration
        repository.migrateFromSharedPreferences()
        
        // Verify migrated data
        val nutritionPrefs = repository.nutritionPreferences.first()
        assertEquals(2500, nutritionPrefs.dailyCalorieGoal)
        assertEquals(3.5, nutritionPrefs.dailyWaterGoalLiters, 0.01)
        assertEquals(false, nutritionPrefs.nutritionRemindersEnabled)
    }
    
    @Test
    fun testUserProfileMigration() = runTest {
        // Setup SharedPreferences data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("user_name", "Test User")
            .putInt("user_age", 30)
            .putFloat("user_weight", 75.5f)
            .putFloat("user_height", 180.0f)
            .apply()
        
        // Trigger migration
        repository.migrateFromSharedPreferences()
        
        // Verify migrated data
        val userProfile = repository.userProfile.first()
        assertEquals("Test User", userProfile.userName)
        assertEquals(30, userProfile.age)
        assertEquals(75.5, userProfile.weightKg, 0.01)
        assertEquals(180.0, userProfile.heightCm, 0.01)
    }
    
    @Test
    fun testAppPreferencesMigration() = runTest {
        // Setup SharedPreferences data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("theme_mode", "dark")
            .putString("language", "en")
            .putBoolean("achievement_notifications", false)
            .apply()
        
        // Trigger migration
        repository.migrateFromSharedPreferences()
        
        // Verify migrated data
        val appPrefs = repository.appPreferences.first()
        assertEquals("dark", appPrefs.themeMode)
        assertEquals("en", appPrefs.language)
        assertEquals(false, appPrefs.achievementNotificationsEnabled)
    }
    
    @Test
    fun testMigrationIdempotent() = runTest {
        // Setup initial data
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("selected_equipment", "Test Equipment")
            .apply()
        
        // First migration
        val firstMigration = repository.migrateFromSharedPreferences()
        assertTrue("First migration should return true", firstMigration)
        
        // Second migration should be skipped
        val secondMigration = repository.migrateFromSharedPreferences()
        assertFalse("Second migration should return false", secondMigration)
        
        // Data should still be correct
        val equipment = repository.selectedEquipment.first()
        assertEquals(setOf("Test Equipment"), equipment)
    }
    
    @Test
    fun testUpdateEquipment() = runTest {
        val newEquipment = setOf("Barbell", "Bench", "Dumbbells")
        
        repository.updateSelectedEquipment(newEquipment)
        
        val retrievedEquipment = repository.selectedEquipment.first()
        assertEquals(newEquipment, retrievedEquipment)
    }
    
    @Test
    fun testUpdateWorkoutPreferences() = runTest {
        repository.updateWorkoutPreferences(
            notificationsEnabled = false,
            defaultRestTimeSeconds = 120,
            soundEnabled = true,
            vibrationEnabled = false
        )
        
        val workoutPrefs = repository.workoutPreferences.first()
        assertEquals(false, workoutPrefs.notificationsEnabled)
        assertEquals(120, workoutPrefs.defaultRestTimeSeconds)
        assertEquals(true, workoutPrefs.soundEnabled)
        assertEquals(false, workoutPrefs.vibrationEnabled)
    }
    
    @Test
    fun testClearAllPreferences() = runTest {
        // Setup some data
        repository.updateSelectedEquipment(setOf("Test"))
        repository.updateWorkoutPreferences(notificationsEnabled = false)
        
        // Clear all
        repository.clearAllPreferences()
        
        // Verify everything is back to defaults
        val preferences = repository.userPreferences.first()
        assertTrue(preferences.selectedEquipmentList.isEmpty())
        assertEquals(true, preferences.notificationsEnabled) // Default value
    }
}
