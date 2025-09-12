package com.example.fitapp.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesService
    @Inject
    constructor(
        private val context: Context,
    ) {
        private val dataStore = context.dataStore

        companion object {
            val API_KEY_GEMINI = stringPreferencesKey("api_key_gemini")
            val API_KEY_PERPLEXITY = stringPreferencesKey("api_key_perplexity")
            val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
            val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        }

        val apiKeyGemini: Flow<String?> =
            dataStore.data.map { preferences ->
                preferences[API_KEY_GEMINI]
            }

        val apiKeyPerplexity: Flow<String?> =
            dataStore.data.map { preferences ->
                preferences[API_KEY_PERPLEXITY]
            }

        val isFirstLaunch: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[FIRST_LAUNCH] ?: true
            }

        val isOnboardingCompleted: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[ONBOARDING_COMPLETED] ?: false
            }

        suspend fun setApiKeyGemini(apiKey: String) {
            dataStore.edit { preferences ->
                preferences[API_KEY_GEMINI] = apiKey
            }
        }

        suspend fun setApiKeyPerplexity(apiKey: String) {
            dataStore.edit { preferences ->
                preferences[API_KEY_PERPLEXITY] = apiKey
            }
        }

        suspend fun setFirstLaunch(isFirst: Boolean) {
            dataStore.edit { preferences ->
                preferences[FIRST_LAUNCH] = isFirst
            }
        }

        suspend fun setOnboardingCompleted(completed: Boolean) {
            dataStore.edit { preferences ->
                preferences[ONBOARDING_COMPLETED] = completed
            }
        }

        // Legacy compatibility methods for ResetManager
        suspend fun clearWorkoutPreferences() {
            // For compatibility - no specific workout preferences to clear
        }

        suspend fun clearNutritionPreferences() {
            // For compatibility - no specific nutrition preferences to clear
        }

        suspend fun clearUserPreferences() {
            // For compatibility - no specific user preferences to clear
        }

        suspend fun clearAchievementPreferences() {
            // For compatibility - no specific achievement preferences to clear
        }

        suspend fun clearAllPreferences() {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
