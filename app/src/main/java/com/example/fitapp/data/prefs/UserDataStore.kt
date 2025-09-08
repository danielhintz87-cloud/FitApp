package com.example.fitapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Jetpack DataStore basierte Implementierung für UserPreferences.
 * Migriert sukzessive von SharedPreferences (Legacy) auf DataStore.
 */
class UserDataStore(private val context: Context) : UserPreferencesService {
    private val Context.dataStore by preferencesDataStore(name = "user_prefs")

    private object Keys {
        val SELECTED_EQUIPMENT = stringSetPreferencesKey("selected_equipment")
    }

    override suspend fun clearWorkoutPreferences() { clearAllPreferences() }
    override suspend fun clearNutritionPreferences() { clearAllPreferences() }
    override suspend fun clearUserPreferences() { clearAllPreferences() }
    override suspend fun clearAchievementPreferences() { clearAllPreferences() }
    override suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }

    override suspend fun getSelectedEquipment(): Set<String> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.SELECTED_EQUIPMENT] ?: emptySet()
        }.first()
    }

    override suspend fun saveSelectedEquipment(equipment: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_EQUIPMENT] = equipment
        }
    }

    /**
     * Helfer Flow für UI Reaktivität (optional verwendbar)
     */
    val selectedEquipmentFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_EQUIPMENT] ?: emptySet()
    }
}

/**
 * Factory Methode um sukzessive Migration zu erleichtern: versucht DataStore, fällt auf Legacy zurück
 */
object UserPreferencesFactory {
    fun create(context: Context, preferDataStore: Boolean = true): UserPreferencesService {
        return if (preferDataStore) UserDataStore(context) else UserPreferencesLegacyImpl(context)
    }
}
