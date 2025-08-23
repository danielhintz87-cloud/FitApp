package com.example.fitapp.ui.model

import com.example.fitapp.data.Recipe

data class UiRecipe(
    val id: String,
    val title: String,
    val calories: Int,
    val tagsLine: String,
    val ingredientsText: String,
    val stepsText: String
)

fun Recipe.toUi(): UiRecipe {
    val tags = if (tags.isEmpty()) "Alltagstauglich" else tags.joinToString(" · ")
    val ing = if (ingredients.isEmpty()) "–" else ingredients.joinToString("\n") { "• ${it.name} — ${it.amount}" }
    val stepsStr = if (steps.isEmpty()) "–" else steps.sortedBy { it.step }.joinToString("\n") { "${it.step}. ${it.text}" }
    return UiRecipe(
        id = id,
        title = title,
        calories = calories,
        tagsLine = tags,
        ingredientsText = ing,
        stepsText = stepsStr
    )
}
