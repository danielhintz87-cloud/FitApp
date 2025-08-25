package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap

object AppAi {
    private lateinit var aiCore: AiCore
    
    fun initialize(context: Context) {
        aiCore = AiCore(context)
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
        
        return aiCore.generateText(prompt, "plan", provider).getOrElse { 
            "❌ Fehler bei Plan-Generierung: ${it.message}"
        }
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
        
        return aiCore.generateText(prompt, "alternative", provider).getOrElse {
            "❌ Fehler bei Alternative: ${it.message}"
        }
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
        
        return aiCore.generateText(prompt, "recipe", provider).getOrElse {
            "❌ Fehler bei Rezept-Generierung: ${it.message}"
        }
    }

    /**
     * Analyze food image for calorie estimation
     */
    suspend fun analyzeFoodImage(
        bitmap: Bitmap,
        provider: AiProvider = AiProvider.OpenAI
    ): CalorieEstimation? {
        return aiCore.analyzeImageForCalories(bitmap, provider).getOrNull()
    }
}