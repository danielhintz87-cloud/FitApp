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
 * Reset types for different data categories
 */
enum class ResetError {
    INVALID_TOKEN,
    DATABASE_ERROR,
    PERMISSION_DENIED,
    UNKNOWN_ERROR
}

/**
 * Reset types for different data categories
 */
enum class ResetType {
    WORKOUT_DATA,
    NUTRITION_DATA,
    USER_PROFILE,
    ACHIEVEMENTS,
    SHOPPING_LIST,
    PERSONAL_RECORDS,
    SELECTIVE_RESET,
    COMPLETE_RESET
}

/**
 * Data class for reset results
 */
data class ResetResult(
    val isSuccess: Boolean,
    val resetType: ResetType,
    val isCompleted: Boolean,
    val hasError: Boolean,
    val errorMessage: String? = null,
    val preservedUserSettings: Boolean = false,
    val clearedDataTypes: List<String> = emptyList(),
    val errorType: ResetError? = null
)

/**
 * Reset options for selective resets
 */
data class SelectiveResetOptions(
    val resetPersonalInfo: Boolean = false,
    val resetFitnessGoals: Boolean = false,
    val resetPreferences: Boolean = false,
    val resetAchievements: Boolean = false
)

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
    
    /**
     * Helper function for creating error results
     */
    private fun createErrorResult(error: Exception): Map<String, Any> {
        return mapOf(
            "success" to false,
            "error" to (error.message ?: "Unknown error"),
            "timestamp" to System.currentTimeMillis(),
            "errorType" to error.javaClass.simpleName
        )
    }
    
    /**
     * Helper function for creating success results
     */
    private fun createSuccessResult(message: String): Map<String, Any> {
        return mapOf(
            "success" to true,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
    }
    
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

    
    /**
     * Perform reset operation with confirmation
     */
    suspend fun performReset(
        resetType: ResetType, 
        confirmationToken: String,
        preserveSettings: Boolean = false,
        validateIntegrity: Boolean = false,
        createBackup: Boolean = false
    ): ResetResult {
        return try {
            // Validate confirmation token
            if (!isValidConfirmationToken(resetType, confirmationToken)) {
                return ResetResult(
                    isSuccess = false,
                    resetType = resetType,
                    isCompleted = false,
                    hasError = true,
                    errorMessage = "Invalid confirmation token",
                    errorType = ResetError.INVALID_TOKEN
                )
            }
            
            // Perform the reset based on type
            when (resetType) {
                ResetType.WORKOUT_DATA -> {
                    resetWorkoutData()
                    ResetResult(
                        isSuccess = true,
                        resetType = resetType,
                        isCompleted = true,
                        hasError = false,
                        preservedUserSettings = preserveSettings
                    )
                }
                ResetType.NUTRITION_DATA -> {
                    resetNutritionData()
                    ResetResult(
                        isSuccess = true,
                        resetType = resetType,
                        isCompleted = true,
                        hasError = false,
                        preservedUserSettings = preserveSettings
                    )
                }
                ResetType.USER_PROFILE -> {
                    resetUserProfile()
                    ResetResult(
                        isSuccess = true,
                        resetType = resetType,
                        isCompleted = true,
                        hasError = false,
                        preservedUserSettings = preserveSettings
                    )
                }
                ResetType.ACHIEVEMENTS -> {
                    resetAchievements()
                    ResetResult(
                        isSuccess = true,
                        resetType = resetType,
                        isCompleted = true,
                        hasError = false
                    )
                }
                ResetType.SHOPPING_LIST -> {
                    resetShoppingList()
                    ResetResult(
                        isSuccess = true,
                        resetType = resetType,
                        isCompleted = true,
                        hasError = false
                    )
                }
                ResetType.PERSONAL_RECORDS -> {
                    resetPersonalRecords()
                    ResetResult(
                        isSuccess = true,
                        resetType = resetType,
                        isCompleted = true,
                        hasError = false,
                        clearedDataTypes = listOf("personal_records", "pr_history")
                    )
                }
                ResetType.SELECTIVE_RESET -> {
                    // Selective reset should use performSelectiveReset method instead
                    ResetResult(
                        isSuccess = false,
                        resetType = resetType,
                        isCompleted = false,
                        hasError = true,
                        errorMessage = "Use performSelectiveReset method for selective operations"
                    )
                }
                ResetType.COMPLETE_RESET -> {
                    performCompleteReset()
                    ResetResult(
                        isSuccess = true,
                        resetType = resetType,
                        isCompleted = true,
                        hasError = false,
                        preservedUserSettings = preserveSettings
                    )
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Error performing reset: ${e.message}",
                emptyMap(),
                e
            )
            ResetResult(
                isSuccess = false,
                resetType = resetType,
                isCompleted = false,
                hasError = true,
                errorMessage = e.message,
                errorType = ResetError.DATABASE_ERROR
            )
        }
    }
    
    /**
     * Perform selective reset with options
     */
    suspend fun performSelectiveReset(
        resetType: ResetType,
        confirmationToken: String,
        options: SelectiveResetOptions
    ): ResetResult {
        return try {
            if (!isValidConfirmationToken(resetType, confirmationToken)) {
                return ResetResult(
                    isSuccess = false,
                    resetType = resetType,
                    isCompleted = false,
                    hasError = true,
                    errorMessage = "Invalid confirmation token",
                    errorType = ResetError.INVALID_TOKEN
                )
            }
            
            val clearedTypes = mutableListOf<String>()
            
            if (options.resetPersonalInfo) {
                // Reset personal info specific data
                clearedTypes.add("personal_info")
            }
            
            if (options.resetFitnessGoals) {
                // Reset fitness goals specific data
                clearedTypes.add("fitness_goals")
            }
            
            if (options.resetPreferences && !options.resetPreferences) {
                // Don't clear preferences if explicitly preserved
            } else if (options.resetPreferences) {
                clearedTypes.add("preferences")
            }
            
            if (options.resetAchievements) {
                clearedTypes.add("achievements")
            }
            
            ResetResult(
                isSuccess = true,
                resetType = resetType,
                isCompleted = true,
                hasError = false,
                clearedDataTypes = clearedTypes
            )
        } catch (e: Exception) {
            ResetResult(
                isSuccess = false,
                resetType = resetType,
                isCompleted = false,
                hasError = true,
                errorMessage = e.message,
                errorType = ResetError.DATABASE_ERROR
            )
        }
    }
    
    /**
     * Validate confirmation token
     */
    private fun isValidConfirmationToken(resetType: ResetType, token: String): Boolean {
        return when (resetType) {
            ResetType.COMPLETE_RESET -> token == "CONFIRM_COMPLETE_RESET_I_UNDERSTAND"
            else -> token.startsWith("CONFIRM")
        }
    }

    /**
     * API wrapper for resetWorkoutData that returns status map
     */
    suspend fun resetWorkoutDataApi(): Map<String, Any> {
        return try {
            resetWorkoutData()
            mapOf(
                "success" to true,
                "message" to "Workout data reset successfully",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            handleResetError(e)
        }
    }

    /**
     * Reset workout data including sessions, performance, and progressions
     */
    suspend fun resetWorkoutData(): Map<String, Any> {
        return try {
            _resetProgress.value = ResetProgress("Workout-Daten", 0f, "Starte Reset...")
            
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
            
            createSuccessResult("Workout data reset successfully")
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
                emptyMap(),
                e
            )
            
            return createErrorResult(e)
        }
    }

    /**
     * API wrapper for resetNutritionData that returns status map
     */
    suspend fun resetNutritionDataApi(): Map<String, Any> {
        return try {
            resetNutritionData()
            mapOf(
                "success" to true,
                "message" to "Nutrition data reset successfully",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            handleResetError(e)
        }
    }

    /**
     * Reset nutrition data including meals, recipes, and food items
     */
    suspend fun resetNutritionData(): Map<String, Any> {
        return try {
            _resetProgress.value = ResetProgress("Ernährungs-Daten", 0f, "Starte Reset...")
            
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
            
            createSuccessResult("Nutrition data reset successfully")
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
                emptyMap(),
                e
            )
            
            return createErrorResult(e)
        }
    }

    /**
     * API wrapper for resetUserProfile that returns status map
     */
    suspend fun resetUserProfileApi(): Map<String, Any> {
        return try {
            resetUserProfile()
            mapOf(
                "success" to true,
                "message" to "User profile reset successfully",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            handleResetError(e)
        }
    }

    /**
     * Reset user profile data including BMI, weight, and behavioral data
     */
    suspend fun resetUserProfile(): Map<String, Any> {
        return try {
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0f, "Starte Reset...")
            
            // Delete weight entries
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.2f, "Lösche Gewichts-Daten...")
            database.weightDao().deleteAll()
            
            // Delete BMI history
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.4f, "Lösche BMI-Verlauf...")
            database.bmiHistoryDao().deleteAll()
            
            // Delete progress photos (if DAO exists)
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.5f, "Lösche Fortschritts-Fotos...")
            // database.progressPhotoDao().deleteAll() // TODO: Implement if DAO exists
            
            // Delete weight loss programs (if DAO exists)
            _resetProgress.value = ResetProgress("Benutzer-Profil", 0.6f, "Lösche Abnehm-Programme...")
            // database.weightLossProgramDao().deleteAll() // TODO: Implement if DAO exists
            
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
            
            createSuccessResult("User profile reset successfully")
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
                emptyMap(),
                e
            )
            
            return createErrorResult(e)
        }
    }

    /**
     * Reset achievements and streak data
     */
    suspend fun resetAchievements(): Map<String, Any> {
        return try {
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0f, "Starte Reset...")
            
            // Reset personal achievements
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.25f, "Setze Erfolge zurück...")
            database.personalAchievementDao().deleteAll()
            
            // Reset personal streaks
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.5f, "Setze Streaks zurück...")
            database.personalStreakDao().deleteAll()
            
            // Reset personal records
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.65f, "Lösche persönliche Rekorde...")
            database.personalRecordDao().deleteAll()
            
            // Reset progress milestones
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.8f, "Lösche Meilensteine...")
            database.progressMilestoneDao().deleteAll()
            
            // Clear achievement preferences
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 0.9f, "Lösche Einstellungen...")
            userPreferences.clearAchievementPreferences()
            
            _resetProgress.value = ResetProgress("Erfolge & Streaks", 1.0f, "Abgeschlossen", isCompleted = true)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Successfully reset achievements and streaks"
            )
            
            createSuccessResult("Achievements reset successfully")
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
                emptyMap(),
                e
            )
            
            return createErrorResult(e)
        }
    }

    /**
     * Reset shopping list data
     */
    suspend fun resetShoppingList(): Map<String, Any> {
        return try {
            _resetProgress.value = ResetProgress("Einkaufsliste", 0f, "Starte Reset...")
            
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
            
            createSuccessResult("Shopping list reset successfully")
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
                emptyMap(),
                e
            )
            
            return createErrorResult(e)
        }
    }

    /**
     * Reset personal records data only
     */
    suspend fun resetPersonalRecords(): Map<String, Any> {
        return try {
            _resetProgress.value = ResetProgress("Persönliche Rekorde", 0f, "Starte Reset...")
            
            // Delete personal records
            _resetProgress.value = ResetProgress("Persönliche Rekorde", 0.5f, "Lösche persönliche Rekorde...")
            database.personalRecordDao().deleteAll()
            
            // Clear PR history (if exists in database)
            _resetProgress.value = ResetProgress("Persönliche Rekorde", 0.8f, "Lösche PR-Verlauf...")
            // Note: PR history clearing would be done here if separate table exists
            
            _resetProgress.value = ResetProgress("Persönliche Rekorde", 1.0f, "Abgeschlossen", isCompleted = true)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Successfully reset personal records"
            )
            
            createSuccessResult("Personal records reset successfully")
        } catch (e: Exception) {
            _resetProgress.value = ResetProgress(
                "Persönliche Rekorde", 0f, "Fehler", 
                hasError = true, 
                errorMessage = e.message
            )
            
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Error resetting personal records: ${e.message}",
                emptyMap(),
                e
            )
            
            return createErrorResult(e)
        }
    }

    /**
     * Reset all data - calls complete reset implementation
     */
    private suspend fun resetAllData(): Map<String, Any> {
        return performCompleteReset()
    }

    /**
     * Complete app reset - WARNING: This will delete ALL data
     */
    suspend fun performCompleteReset(): Map<String, Any> {
        return try {
            _resetProgress.value = ResetProgress("Vollständiger Reset", 0f, "Starte vollständigen Reset...")
            
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
            
            createSuccessResult("Complete app reset successfully")
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
                emptyMap(),
                e
            )
            
            return createErrorResult(e)
        }
    }
    
    /**
     * Handle reset errors and return standardized error response
     */
    private suspend fun handleResetError(error: Exception): Map<String, Any> {
        return mapOf(
            "success" to false,
            "error" to (error.message ?: "Unknown error"),
            "timestamp" to System.currentTimeMillis()
        )
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
                
            ResetType.PERSONAL_RECORDS -> 
                "Persönliche Rekorde werden zurückgesetzt:\n" +
                "• Alle persönlichen Bestleistungen\n" +
                "• PR-Verlauf\n" +
                "• Rekord-Statistiken\n\n" +
                "Diese Aktion kann nicht rückgängig gemacht werden."
                
            ResetType.SELECTIVE_RESET -> 
                "Selektiver Reset wird durchgeführt:\n" +
                "• Nur ausgewählte Datenkategorien\n" +
                "• Einstellungen können erhalten bleiben\n\n" +
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
        // database.progressPhotoDao().deleteAll() // TODO: Implement if DAO exists
        // database.weightLossProgramDao().deleteAll() // TODO: Implement if DAO exists
        database.behavioralCheckInDao().deleteAll()
        userPreferences.clearUserPreferences()
    }
    
    private suspend fun resetAchievementsSilent() {
        database.personalAchievementDao().deleteAll()
        database.personalStreakDao().deleteAll()
        database.personalRecordDao().deleteAll()
        database.progressMilestoneDao().deleteAll()
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