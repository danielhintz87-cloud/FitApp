package com.example.fitapp.data.ai

sealed interface AiTask {
    object ChatCoach : AiTask
    object RecipeIdeas : AiTask
    object FoodPhotoAnalyze : AiTask
    object WebAnswer : AiTask
}

interface AiProvider {
    val id: String
    suspend fun ask(prompt: String): String
}
