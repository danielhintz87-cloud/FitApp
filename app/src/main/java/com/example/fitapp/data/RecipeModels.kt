package com.example.fitapp.data

/**
 * Nutzerpräferenzen fürs Rezepte-Generating (MVP).
 * Diese Datei enthält KEINE Recipe-Klasse mehr (vermeidet Redeclaration).
 */
data class RecipePrefs(
    val lowCarb: Boolean = false,
    val highProtein: Boolean = false,
    val vegetarian: Boolean = false,
    val vegan: Boolean = false
)

/** Ergebnis einer groben Foto-Kalorien-Schätzung. */
data class CalorieEstimate(
    val title: String,
    val calories: Int,
    val confidence: Float,
    val note: String? = null
)
