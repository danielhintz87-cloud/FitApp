package com.example.fitapp.data.ai

import android.content.Context

class AiRouter(context: Context) {
    private val openai = OpenAIProvider(context)
    private val gemini = GeminiProvider(context)
    private val deepseek = DeepSeekProvider(context)

    var defaultChat: String = openai.id
    var defaultFoodVision: String = gemini.id
    var defaultWebAnswer: String = "perplexity"

    suspend fun run(task: AiTask, prompt: String): String {
        val provider: AiProvider = when (task) {
            AiTask.ChatCoach -> if (defaultChat == deepseek.id) deepseek else openai
            AiTask.RecipeIdeas -> openai
            AiTask.FoodPhotoAnalyze -> gemini
            AiTask.WebAnswer -> openai
        }
        return provider.ask(prompt)
    }
}
