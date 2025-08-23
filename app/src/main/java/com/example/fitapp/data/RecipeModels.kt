package com.example.fitapp.data

data class RecipeIngredient(val name: String, val amount: String)
data class RecipeInstruction(val step: Int, val text: String)

data class Recipe(
    val id: String,
    val title: String,
    val calories: Int,
    val tags: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<RecipeInstruction> = emptyList(),
    val markdown: String? = null
)

data class RecipePrefs(
    val vegetarian: Boolean = false,
    val highProtein: Boolean = false,
    val lowCarb: Boolean = false,
    val avoid: List<String> = emptyList(),
    val targetCalories: Int? = null
)
