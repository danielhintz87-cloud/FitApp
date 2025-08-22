package com.example.fitapp.ui.model

import com.example.fitapp.data.Recipe

data class UiRecipe(
    val id: String,
    val title: String,
    val calories: Int,
    val tagsLine: String,
    val ingredientsText: String,
    val stepsText: String,
    val markdown: String?
)

fun Recipe.toUi(): UiRecipe =
    UiRecipe(
        id = id,
        title = title,
        calories = calories,
        tagsLine = if (tags.isEmpty()) "" else tags.joinToString(" · "),
        ingredientsText = ingredients.joinToString("\n") { "• ${it.name}: ${it.amount}" },
        stepsText = steps.sortedBy { it.step }.joinToString("\n") { "${it.step}. ${it.text}" },
        markdown = markdown
    )
