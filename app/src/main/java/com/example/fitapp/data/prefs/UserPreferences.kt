package com.example.fitapp.data.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper for storing and retrieving user preferences from SharedPreferences
 */
object UserPreferences {
    private const val PREFS_NAME = "user_preferences"
    private const val KEY_SELECTED_EQUIPMENT = "selected_equipment"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSelectedEquipment(context: Context, equipment: List<String>) {
        getPrefs(context).edit().putString(KEY_SELECTED_EQUIPMENT, equipment.joinToString(",")).apply()
    }

    fun getSelectedEquipment(context: Context): List<String> {
        val stored = getPrefs(context).getString(KEY_SELECTED_EQUIPMENT, "") ?: ""
        return if (stored.isBlank()) emptyList() else stored.split(",").filter { it.isNotBlank() }
    }
}