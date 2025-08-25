package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.fitapp.data.db.AiLog
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AppAi"

/**
 * Public API for AI functionality with automatic logging to Room database.
 * This is a wrapper around AiCore that adds logging and context management.
 */
object AppAi {

    private var appContext: Context? = null
    private val database get() = appContext?.let { AppDatabase.get(it) }

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    // ---------- Public API ----------

    suspend fun generate12WeekPlan(
        goal: String,
        daysPerWeek: Int,
        intensity: String,
        equipment: List<String>,
        provider: AiProvider = AiProvider.OpenAI
    ): String {
        val prompt = """
            Erstelle einen durchgehenden 12‑Wochen-Trainingsplan (Kalender-Format, Woche 1–12).
            Parameter:
            - Ziel: $goal
            - Einheiten pro Woche: $daysPerWeek
            - Intensität: $intensity
            - Equipment: ${equipment.joinToString()}
            
            Format (Markdown, **ohne** Codeblock):
            ## Woche 1
            - Tag 1 (Dauer: 45): Übung A – Sätze×Wdh …
            …
            ## Woche 12
            …
        """.trimIndent()
        
        return callTextModelWithLogging(prompt, provider, "12week_plan")
    }

    suspend fun suggestAlternativeAndLog(
        todaysPlanMarkdown: String,
        constraints: String,
        provider: AiProvider = AiProvider.OpenAI
    ): String {
        val prompt = """
            Nutzer möchte eine Alternative zum heutigen Plan, die sich gut einfügt.
            Bedingungen: $constraints
            Ursprünglicher Plan (Markdown):
            $todaysPlanMarkdown

            Gib NUR einen kurzen Ersatz-Block (Markdown, ohne Codeblock) + kurze Begründung in 1 Satz.
        """.trimIndent()
        
        return callTextModelWithLogging(prompt, provider, "alternative_plan")
    }

    suspend fun generateRecipes(
        preferences: String,
        count: Int = 10,
        provider: AiProvider = AiProvider.OpenAI
    ): String {
        val prompt = """
            Generiere $count abwechslungsreiche Rezepte passend zu:
            $preferences
            Ausgabe als Markdown-Liste:
            ### Titel – (Zeit min / kcal geschätzt)
            Zutaten:
            - …
            Schritte:
            1. …
        """.trimIndent()
        
        return callTextModelWithLogging(prompt, provider, "recipes")
    }

    suspend fun analyzeImageForCalories(
        bitmap: Bitmap,
        provider: AiProvider = AiProvider.OpenAI
    ): String {
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = AiCore.analyzeImageForCalories(bitmap, provider)
            
            if (result.isSuccess) {
                val analysis = result.getOrThrow()
                val response = """
                    🍽️ Erkannte Lebensmittel:
                    ${analysis.foods.joinToString("\n") { "• $it" }}
                    
                    📊 Geschätzte Kalorien: ${analysis.totalCalories} kcal
                    🎯 Konfidenz: ${analysis.confidence}%
                    
                    💡 ${analysis.explanation}
                """.trimIndent()
                
                logInteraction(
                    provider = provider.name,
                    requestType = "vision",
                    prompt = "Food calorie analysis from image",
                    response = response,
                    isSuccess = true,
                    confidenceScore = analysis.confidence
                )
                
                response
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                logInteraction(
                    provider = provider.name,
                    requestType = "vision",
                    prompt = "Food calorie analysis from image",
                    response = "",
                    isSuccess = false,
                    errorMessage = error
                )
                "❌ Fehler bei der Bildanalyse: $error"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in image analysis", e)
            logInteraction(
                provider = provider.name,
                requestType = "vision",
                prompt = "Food calorie analysis from image",
                response = "",
                isSuccess = false,
                errorMessage = e.message
            )
            "❌ Fehler bei der Bildanalyse: ${e.message}"
        }
    }

    // ---------- Core Methods ----------

    private suspend fun callTextModelWithLogging(
        prompt: String,
        provider: AiProvider,
        requestType: String
    ): String {
        return try {
            val result = AiCore.generateText(prompt, provider)
            
            if (result.isSuccess) {
                val response = result.getOrThrow()
                logInteraction(
                    provider = provider.name,
                    requestType = requestType,
                    prompt = prompt,
                    response = response,
                    isSuccess = true
                )
                response
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                logInteraction(
                    provider = provider.name,
                    requestType = requestType,
                    prompt = prompt,
                    response = "",
                    isSuccess = false,
                    errorMessage = error
                )
                "❌ Fehler bei $provider: $error"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in text generation", e)
            logInteraction(
                provider = provider.name,
                requestType = requestType,
                prompt = prompt,
                response = "",
                isSuccess = false,
                errorMessage = e.message
            )
            "❌ Fehler bei $provider: ${e.message}"
        }
    }

    private fun logInteraction(
        provider: String,
        requestType: String,
        prompt: String,
        response: String,
        isSuccess: Boolean,
        errorMessage: String? = null,
        confidenceScore: Int? = null
    ) {
        database?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.aiLogDao().insert(
                        AiLog(
                            provider = provider,
                            requestType = requestType,
                            prompt = prompt.take(500), // Limit prompt length
                            response = response.take(2000), // Limit response length
                            isSuccess = isSuccess,
                            errorMessage = errorMessage,
                            confidenceScore = confidenceScore
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error logging AI interaction", e)
                }
            }
        }
    }
}