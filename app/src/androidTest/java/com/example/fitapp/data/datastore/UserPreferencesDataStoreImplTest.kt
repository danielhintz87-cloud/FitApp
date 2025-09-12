package com.example.fitapp.data.datastore

import android.content.Context
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
 * Tests for UserPreferencesDataStoreImpl to ensure it properly uses Proto DataStore
 * instead of the old Preferences DataStore format.
 */
@RunWith(AndroidJUnit4::class)
class UserPreferencesDataStoreImplTest {

    private lateinit var context: Context
    private lateinit var dataStoreImpl: UserPreferencesDataStoreImpl
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataStoreImpl = UserPreferencesDataStoreImpl(context)
        
        // Clear existing DataStore file to ensure clean test state
        cleanupDataStore()
    }
    
    @After
    fun tearDown() {
        cleanupDataStore()
    }
    
    private fun cleanupDataStore() {
        val dataStoreFile = File(context.filesDir, "datastore/user_preferences.pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
    }
    
    @Test
    fun `should save and retrieve selected equipment`() = runTest {
        // Given: Equipment set
        val equipment = setOf("Dumbbells", "Barbell", "Treadmill")
        
        // When: Equipment is saved
        dataStoreImpl.saveSelectedEquipment(equipment)
        
        // Then: Equipment should be retrievable
        val retrievedEquipment = dataStoreImpl.getSelectedEquipment().first()
        assertEquals("Equipment should match", equipment, retrievedEquipment)
    }
    
    @Test
    fun `should return empty set when no equipment is saved`() = runTest {
        // When: No equipment has been saved
        val equipment = dataStoreImpl.getSelectedEquipment().first()
        
        // Then: Should return empty set
        assertTrue("Should return empty set", equipment.isEmpty())
    }
    
    @Test
    fun `should clear workout preferences to defaults`() = runTest {
        // Given: Some equipment is saved
        dataStoreImpl.saveSelectedEquipment(setOf("Dumbbells", "Barbell"))
        
        // When: Workout preferences are cleared
        dataStoreImpl.clearWorkoutPreferences()
        
        // Then: Equipment should be cleared and other workout settings reset to defaults
        val equipment = dataStoreImpl.getSelectedEquipment().first()
        assertTrue("Equipment should be cleared", equipment.isEmpty())
        
        // Note: We don't directly test other workout settings here since they're not exposed
        // by the interface, but they should be reset to defaults in the implementation
    }
    
    @Test
    fun `should clear all preferences`() = runTest {
        // Given: Some equipment is saved
        dataStoreImpl.saveSelectedEquipment(setOf("Dumbbells"))
        
        // When: All preferences are cleared
        dataStoreImpl.clearAllPreferences()
        
        // Then: Equipment should be empty
        val equipment = dataStoreImpl.getSelectedEquipment().first()
        assertTrue("Equipment should be empty after clearing all", equipment.isEmpty())
    }
    
    @Test
    fun `should handle equipment updates correctly`() = runTest {
        // Given: Initial equipment set
        val initialEquipment = setOf("Dumbbells", "Barbell")
        dataStoreImpl.saveSelectedEquipment(initialEquipment)
        
        // When: Equipment is updated
        val updatedEquipment = setOf("Treadmill", "Bike", "Rowing Machine")
        dataStoreImpl.saveSelectedEquipment(updatedEquipment)
        
        // Then: Should return updated equipment, not merged
        val retrievedEquipment = dataStoreImpl.getSelectedEquipment().first()
        assertEquals("Should return updated equipment", updatedEquipment, retrievedEquipment)
        assertFalse("Should not contain old equipment", retrievedEquipment.contains("Dumbbells"))
    }
    
    @Test
    fun `should handle empty equipment set`() = runTest {
        // Given: Some equipment is initially saved
        dataStoreImpl.saveSelectedEquipment(setOf("Dumbbells"))
        
        // When: Empty set is saved
        dataStoreImpl.saveSelectedEquipment(emptySet())
        
        // Then: Should return empty set
        val equipment = dataStoreImpl.getSelectedEquipment().first()
        assertTrue("Should handle empty set", equipment.isEmpty())
    }
    
    @Test
    fun `should handle special characters in equipment names`() = runTest {
        // Given: Equipment with special characters
        val specialEquipment = setOf(
            "Dumbbell (20kg)",
            "Barbell - Olympic",
            "Cable Machine #1",
            "Smith Machine & Safety Bar"
        )
        
        // When: Special equipment is saved
        dataStoreImpl.saveSelectedEquipment(specialEquipment)
        
        // Then: Should preserve special characters
        val retrievedEquipment = dataStoreImpl.getSelectedEquipment().first()
        assertEquals("Should preserve special characters", specialEquipment, retrievedEquipment)
    }
    
    @Test
    fun `should handle large equipment sets`() = runTest {
        // Given: Large equipment set
        val largeEquipment = (1..100).map { "Equipment Item $it" }.toSet()
        
        // When: Large set is saved
        dataStoreImpl.saveSelectedEquipment(largeEquipment)
        
        // Then: Should handle large sets correctly
        val retrievedEquipment = dataStoreImpl.getSelectedEquipment().first()
        assertEquals("Should handle large sets", largeEquipment.size, retrievedEquipment.size)
        assertTrue("Should contain all items", retrievedEquipment.containsAll(largeEquipment))
    }
    
    @Test
    fun `clearing individual preference categories should not affect equipment`() = runTest {
        // Given: Equipment is saved
        val equipment = setOf("Dumbbells", "Barbell")
        dataStoreImpl.saveSelectedEquipment(equipment)
        
        // When: Individual preference categories are cleared (but not workout preferences)
        dataStoreImpl.clearNutritionPreferences()
        dataStoreImpl.clearUserPreferences()
        dataStoreImpl.clearAchievementPreferences()
        
        // Then: Equipment should still be preserved
        val retrievedEquipment = dataStoreImpl.getSelectedEquipment().first()
        assertEquals("Equipment should be preserved when clearing other categories", 
            equipment, retrievedEquipment)
    }
}