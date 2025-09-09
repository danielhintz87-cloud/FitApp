package com.example.fitapp.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val USER_PREFS_NAME = "user_prefs"
private val Context.userPrefsDataStore by preferencesDataStore(name = USER_PREFS_NAME)

@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesDataStore {

    private val ds get() = context.userPrefsDataStore

    private val KEY_SELECTED_EQUIPMENT = stringSetPreferencesKey("selected_equipment")

    private val WORKOUT_KEYS: Set<Preferences.Key<*>> = setOf(
        KEY_SELECTED_EQUIPMENT
    )
    private val NUTRITION_KEYS: Set<Preferences.Key<*>> = emptySet()
    private val USER_KEYS: Set<Preferences.Key<*>> = emptySet()
    private val ACHIEVEMENT_KEYS: Set<Preferences.Key<*>> = emptySet()

    override suspend fun clearWorkoutPreferences() {
        ds.edit { prefs -> 
            WORKOUT_KEYS.forEach { key -> 
                prefs -= key 
            } 
        }
    }

    override suspend fun clearNutritionPreferences() {
        ds.edit { prefs -> 
            NUTRITION_KEYS.forEach { key -> 
                prefs -= key 
            } 
        }
    }

    override suspend fun clearUserPreferences() {
        ds.edit { prefs -> 
            USER_KEYS.forEach { key -> 
                prefs -= key 
            } 
        }
    }

    override suspend fun clearAchievementPreferences() {
        ds.edit { prefs -> 
            ACHIEVEMENT_KEYS.forEach { key -> 
                prefs -= key 
            } 
        }
    }

    override suspend fun clearAllPreferences() {
        ds.edit { it.clear() }
    }

    override fun getSelectedEquipment(): Flow<Set<String>> =
        ds.data.map { it[KEY_SELECTED_EQUIPMENT] ?: emptySet() }

    override suspend fun saveSelectedEquipment(equipment: Set<String>) {
        ds.edit { it[KEY_SELECTED_EQUIPMENT] = equipment }
    }
}