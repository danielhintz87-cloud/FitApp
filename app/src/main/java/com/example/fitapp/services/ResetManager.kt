package com.example.fitapp.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferences
import com.example.fitapp.util.StructuredLogger
import android.content.Context

/**
 * Comprehensive Reset Manager
 * Handles different types of data resets with granular control
 */
class ResetManager(
    private val context: Context,
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) {
    companion object {
        private const val TAG = "ResetManager"
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Flow states for reset progress
    private val _resetProgress = MutableStateFlow<ResetProgress?>(null)
    val resetProgress: StateFlow<ResetProgress?> = _resetProgress.asStateFlow()

    data class ResetProgress(
        val operation: String,
        val progress: Float, // 0.0 to 1.0
        val currentStep: String,
        val isCompleted: Boolean = false,
        val hasError: Boolean = false,
        val errorMessage: String? = null
    )

    enum class ResetType {
        WORKOUT_DATA,
        NUTRITION_DATA,
        USER_PROFILE,
        ACHIEVEMENTS,
        PERSONAL_RECORDS,
        SHOPPING_LIST,
        COOKING_SESSIONS,
        COMPLETE_RESET
    }

    /**
     * Reset workout data including sessions, performance, and progressions
     */
    suspend fun resetWorkoutData() {
        _resetProgress.value = ResetProgress("Workout-Daten", 0f, "Starte Reset...")
        
        try {
            // Delete workout sessions
            _resetProgress.value = ResetProgress("Workout-Daten", 0.2f, "Lösche Trainingseinheiten...")
            database.workoutSessionDao().deleteAll()
            
            // Delete workout performance data
            _resetProgress.value = ResetProgress("Workout-Daten", 0.4f, "Lösche Leistungsdaten...")
            database.workoutPerformanceDao().deleteAll()
            
            // Delete exercise progressions
            _resetProgress.value = ResetProgress("Workout-Daten", 0.6f, "Lösche Progression-Daten...")
            database.exerciseProgressionDao().deleteAll()
            
            // Delete today workouts
            _resetProgress.value = ResetProgress("Workout-Daten", 0.8f, "Lösche heutige Trainings...")
            database.todayWorkoutDao().deleteAll()
            
            // Clear workout preferences
            _resetProgress.value = ResetProgress("Workout-Daten", 0.9f, "Lösche Einstellungen...")
            userPreferences.clearWorkoutPreferences()
            
            _resetProgress.value = ResetProgress("Workout-Daten", 1.0f, "Abgeschlossen", isCompleted = true)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Successfully reset workout data"
            )
            
        } catch (e: Exception) {
            _resetProgress.value = ResetProgress(
                "Workout-Daten", 0f, "Fehler", 
                hasError = true, 
                errorMessage = e.message
            )
            
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Error resetting workout data: ${e.message}",
                e
            )
        }
    }

    /**
     * Reset nutrition data including meals, recipes, and food items
     */
    suspend fun resetNutritionData() {
        _resetProgress.value = ResetProgress("Ernährungs-Daten", 0f, "Starte Reset...")
        
        try {
            // Delete meal entries
            _resetProgress.value = ResetProgress("Ernährungs-Daten", 0.15f, "Lösche Mahlzeiten...")
            database.mealEntryDao().deleteAll()
            
            // Delete water entries
            _resetProgress.value = ResetProgress("Ernährungs-Daten", 0.3f, "Lösche Wasser-Logs...")
            database.waterEntryDao().deleteAll()
            
            // Delete intake entries
            _resetProgress.value = ResetProgress("Ernährungs-Daten", 0.45f, "Lösche Kalorie-Logs...")
            database.intakeDao().deleteAll()
            
            // Delete saved recipes
            _resetProgress.value = ResetProgress("Ernährungs-Daten", 0.6f, "Lösche gespeicherte Rezepte...")
            database.savedRecipeDao().deleteAll()
            
            // Delete cooking sessions
            _resetProgress.value = ResetProgress("Ernährungs-Daten", 0.75f, "Lösche Koch-Sessions...")
            database.cookingSessionDao().deleteAll()
            database.cookingTimerDao().deleteAll()
            
            // Clear nutrition preferences
            _resetProgress.value = ResetProgress("Ernährungs-Daten", 0.9f, "Lösche Einstellungen...")
            userPreferences.clearNutritionPreferences()
            
            _resetProgress.value = ResetProgress("Ernährungs-Daten", 1.0f, "Abgeschlossen", isCompleted = true)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Successfully reset nutrition data"
            )
            
        } catch (e: Exception) {
            _resetProgress.value = ResetProgress(
                "Ernährungs-Daten", 0f, "Fehler", 
                hasError = true, 
                errorMessage = e.message
            )
            
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Error resetting nutrition data: ${e.message}",
                e
            )
        }
    }

    /**
     * Reset user profile data including BMI, weight, and behavioral data
     */
    suspend fun resetUserProfile() {
        _resetProgress.value = ResetProgress("Benutzer-Profil", 0f, "Starte Reset...")
        
        try {
            // Delete weight entries
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.2f, "Lösche Gewichts-Daten...")
            database.weightDao().deleteAll()
            
            // Delete BMI history
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.4f, "Lösche BMI-Verlauf...")
            database.bmiHistoryDao().deleteAll()
            
            // Delete weight loss programs
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.6f, "Lösche Abnehm-Programme...")
            database.weightLossProgramDao().deleteAll()
            
            // Delete behavioral check-ins
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.8f, "Lösche Verhaltens-Daten...")
            database.behavioralCheckInDao().deleteAll()
            
            // Clear user preferences
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.9f, "Lösche Einstellungen...")
            userPreferences.clearUserPreferences()
            
            _resetProgress.value = ResetProgress("Benutzer-Profil", 1.0f, "Abgeschlossen", isCompleted = true)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Successfully reset user profile data"
            )
            
        } catch (e: Exception) {
            _resetProgress.value = ResetProgress(
                "Benutzer-Profil", 0f, "Fehler", 
                hasError = true, 
                errorMessage = e.message
            )
            
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Error resetting user profile: ${e.message}",
                e
            )
        }
    }

    /**
     * Reset achievements and streak data
     */
    suspend fun resetAchievements() {
        _resetProgress.value = ResetProgress("Erfolge & Streaks", 0f, "Starte Reset...")
        
        try {
            // Reset personal achievements
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.25f, "Setze Erfolge zurück...")
            database.personalAchievementDao().resetAllAchievements()
            
            // Reset personal streaks
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.5f, "Setze Streaks zurück...")
            database.personalStreakDao().resetAllStreaks()
            
            // Reset personal records
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.75f, "Lösche persönliche Rekorde...")
            database.personalRecordDao().deleteAll()
            
            // Clear achievement preferences
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.9f, "Lösche Einstellungen...")
            userPreferences.clearAchievementPreferences()
            
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 1.0f, "Abgeschlossen", isCompleted = true)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Successfully reset achievements and streaks"
            )
            
        } catch (e: Exception) {
            _resetProgress.value = ResetProgress(
                "Erfolge & Streaks", 0f, "Fehler", 
                hasError = true, 
                errorMessage = e.message
            )
            
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Error resetting achievements: ${e.message}",
                e
            )
        }
    }

    /**
     * Reset shopping list data
     */
    suspend fun resetShoppingList() {
        _resetProgress.value = ResetProgress("Einkaufsliste", 0f, "Starte Reset...")
        
        try {
            // Delete shopping items
            _resetProgress.value = ResetProgress("Einkaufsliste", 0.5f, "Lösche Einkaufsartikel...")
            database.shoppingDao().deleteAll()
            
            // Reset categories to default
            _resetProgress.value = ResetProgress("Einkaufsliste", 0.8f, "Setze Kategorien zurück...")
            resetShoppingCategoriesToDefault()
            
            _resetProgress.value = ResetProgress("Einkaufsliste", 1.0f, "Abgeschlossen", isCompleted = true)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Successfully reset shopping list"
            )
            
        } catch (e: Exception) {
            _resetProgress.value = ResetProgress(
                "Einkaufsliste", 0f, "Fehler", 
                hasError = true, 
                errorMessage = e.message
            )
            
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Error resetting shopping list: ${e.message}",
                e
            )
        }
    }

    /**
     * Complete app reset - WARNING: This will delete ALL data
     */
    suspend fun performCompleteReset() {
        _resetProgress.value = ResetProgress("Vollständiger Reset", 0f, "Starte vollständigen Reset...")
        
        try {
            // Reset in specific order to handle dependencies
            
            // 1. Reset workout data
            _resetProgress.value = ResetProgress("Vollständiger Reset", 0.1f, "Lösche Trainings-Daten...")
            resetWorkoutDataSilent()
            
            // 2. Reset nutrition data
            _resetProgress.value = ResetProgress("Vollständiger Reset", 0.3f, "Lösche Ernährungs-Daten...")
            resetNutritionDataSilent()
            
            // 3. Reset achievements
            _resetProgress.value = ResetProgress("Vollständiger Reset", 0.5f, "Lösche Erfolge...")
            resetAchievementsSilent()
            
            // 4. Reset user profile
            _resetProgress.value = ResetProgress("Vollständiger Reset", 0.7f, "Lösche Benutzer-Profil...")
            resetUserProfileSilent()
            
            // 5. Reset shopping list
            _resetProgress.value = ResetProgress("Vollständiger Reset", 0.85f, "Lösche Einkaufsliste...")
            resetShoppingListSilent()
            
            // 6. Clear all preferences
            _resetProgress.value = ResetProgress("Vollständiger Reset", 0.95f, "Lösche alle Einstellungen...")
            userPreferences.clearAllPreferences()
            
            _resetProgress.value = ResetProgress("Vollständiger Reset", 1.0f, "Abgeschlossen", isCompleted = true)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Successfully performed complete app reset"
            )
            
        } catch (e: Exception) {
            _resetProgress.value = ResetProgress(
                "Vollständiger Reset", 0f, "Fehler", 
                hasError = true, 
                errorMessage = e.message
            )
            
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Error performing complete reset: ${e.message}",
                e
            )
        }
    }

    /**
     * Get reset confirmation message for specific reset type
     */
    fun getResetConfirmationMessage(resetType: ResetType): String {
        return when (resetType) {
            ResetType.WORKOUT_DATA -> 
                "Alle Trainings-Daten werden gelöscht:\n" +
                "• Trainingseinheiten\n" +
                "• Leistungsdaten\n" +
                "• Progression-Verfolgung\n" +
                "• Training-Einstellungen\n\n" +
                "Diese Aktion kann nicht rückgängig gemacht werden."
                
            ResetType.NUTRITION_DATA -> 
                "Alle Ernährungs-Daten werden gelöscht:\n" +
                "• Mahlzeiten-Logs\n" +
                "• Wasser-Aufnahme\n" +
                "• Gespeicherte Rezepte\n" +
                "• Koch-Sessions\n" +
                "• Ernährungs-Einstellungen\n\n" +
                "Diese Aktion kann nicht rückgängig gemacht werden."
                
            ResetType.USER_PROFILE -> 
                "Alle Profil-Daten werden gelöscht:\n" +
                "• Gewichts-Verlauf\n" +
                "• BMI-Daten\n" +
                "• Abnehm-Programme\n" +
                "• Verhaltens-Daten\n" +
                "• Benutzer-Einstellungen\n\n" +
                "Diese Aktion kann nicht rückgängig gemacht werden."
                
            ResetType.ACHIEVEMENTS -> 
                "Alle Erfolge werden zurückgesetzt:\n" +
                "• Persönliche Erfolge\n" +
                "• Streak-Zähler\n" +
                "• Persönliche Rekorde\n" +
                "• Meilenstein-Fortschritt\n\n" +
                "Diese Aktion kann nicht rückgängig gemacht werden."
                
            ResetType.SHOPPING_LIST -> 
                "Die Einkaufsliste wird geleert:\n" +
                "• Alle Einkaufsartikel\n" +
                "• Kategorien werden zurückgesetzt\n\n" +
                "Diese Aktion kann nicht rückgängig gemacht werden."
                
            ResetType.COMPLETE_RESET -> 
                "⚠️ WARNUNG: VOLLSTÄNDIGER RESET ⚠️\n\n" +
                "ALLE App-Daten werden gelöscht:\n" +
                "• Trainings-Daten\n" +
                "• Ernährungs-Daten\n" +
                "• Benutzer-Profil\n" +
                "• Erfolge & Streaks\n" +
                "• Einkaufsliste\n" +
                "• Alle Einstellungen\n\n" +
                "Die App wird in den Werkszustand zurückgesetzt.\n" +
                "Diese Aktion kann NICHT rückgängig gemacht werden!"
                
            else -> "Ausgewählte Daten werden gelöscht. Diese Aktion kann nicht rückgängig gemacht werden."
        }
    }

    /**
     * Clear reset progress
     */
    fun clearResetProgress() {
        _resetProgress.value = null
    }

    // Private helper methods for silent resets (used in complete reset)
    
    private suspend fun resetWorkoutDataSilent() {
        database.workoutSessionDao().deleteAll()
        database.workoutPerformanceDao().deleteAll()
        database.exerciseProgressionDao().deleteAll()
        database.todayWorkoutDao().deleteAll()
        userPreferences.clearWorkoutPreferences()
    }
    
    private suspend fun resetNutritionDataSilent() {
        database.mealEntryDao().deleteAll()
        database.waterEntryDao().deleteAll()
        database.intakeDao().deleteAll()
        database.savedRecipeDao().deleteAll()
        database.cookingSessionDao().deleteAll()
        database.cookingTimerDao().deleteAll()
        userPreferences.clearNutritionPreferences()
    }
    
    private suspend fun resetUserProfileSilent() {
        database.weightDao().deleteAll()
        database.bmiHistoryDao().deleteAll()
        database.weightLossProgramDao().deleteAll()
        database.behavioralCheckInDao().deleteAll()
        userPreferences.clearUserPreferences()
    }
    
    private suspend fun resetAchievementsSilent() {
        database.personalAchievementDao().resetAllAchievements()
        database.personalStreakDao().resetAllStreaks()
        database.personalRecordDao().deleteAll()
        userPreferences.clearAchievementPreferences()
    }
    
    private suspend fun resetShoppingListSilent() {
        database.shoppingDao().deleteAll()
        resetShoppingCategoriesToDefault()
    }
    
    private suspend fun resetShoppingCategoriesToDefault() {
        // First clear existing categories
        database.shoppingCategoryDao().deleteAll()
        
        // Then add default categories
        val defaultCategories = listOf(
            "Obst & Gemüse" to 1,
            "Fleisch & Fisch" to 2,
            "Milchprodukte" to 3,
            "Getreide & Backwaren" to 4,
            "Tiefkühl" to 5,
            "Getränke" to 6,
            "Gewürze & Kräuter" to 7,
            "Sonstiges" to 8
        )
        
        defaultCategories.forEach { (name, order) ->
            database.shoppingCategoryDao().insert(
                com.example.fitapp.data.db.ShoppingCategoryEntity(name = name, order = order)
            )
        }
    }
}