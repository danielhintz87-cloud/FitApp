package com.example.fitapp.application.usecases

import com.example.fitapp.domain.entities.*
import com.example.fitapp.domain.repositories.AiProviderRepository
import com.example.fitapp.domain.usecases.EstimateCaloriesForManualEntryUseCase
import com.example.fitapp.domain.usecases.GenerateDailyWorkoutStepsUseCase

/**
 * Implementation of manual calorie estimation use case
 */
class EstimateCaloriesForManualEntryUseCaseImpl(
    private val repository: AiProviderRepository
) : EstimateCaloriesForManualEntryUseCase {
    
    override suspend fun execute(foodDescription: String): Result<Int> {
        val prompt = "Schätze die Kalorien für: '$foodDescription'. Antworte nur mit einer Zahl (kcal) ohne zusätzlichen Text."
        
        val aiRequest = AiRequest(
            prompt = prompt,
            provider = com.example.fitapp.domain.entities.AiProvider.Gemini, // Temporarily use Gemini instead of Perplexity
            taskType = TaskType.CALORIE_ESTIMATION
        )
        
        return repository.generateText(aiRequest).mapCatching { response ->
            // Extract number from response
            val kcalRegex = Regex("\\d+")
            val kcal = kcalRegex.find(response)?.value?.toIntOrNull() ?: 0
            if (kcal == 0) throw IllegalArgumentException("Keine gültige Kalorienzahl gefunden")
            kcal
        }
    }
}

/**
 * Implementation of daily workout steps generation use case
 */
class GenerateDailyWorkoutStepsUseCaseImpl(
    private val repository: AiProviderRepository
) : GenerateDailyWorkoutStepsUseCase {
    
    override suspend fun execute(goal: String, minutes: Int, equipment: List<String>): Result<String> {
        val equipmentString = if (equipment.isEmpty()) "Nur Körpergewicht" else equipment.joinToString(", ")
        
        val prompt = """
Erstelle ein ${minutes}-minütiges tägliches Workout für das Ziel "$goal" mit folgenden verfügbaren Geräten: $equipmentString.

Format: Gib die Übungen als einfache Liste zurück, eine Übung pro Zeile, mit | als Trenner zwischen Übung und Beschreibung:

Übungsname | Kurze Anleitung (Wiederholungen/Zeit)

Beispiel:
Kniebeugen | 3 Sätze à 15 Wiederholungen
Liegestütze | 2 Sätze à 10 Wiederholungen  
Plank | 3x 30 Sekunden halten
Pause | 60 Sekunden Erholung

Erstelle 8-12 Übungen inklusive Pausen. Keine zusätzlichen Texte oder Disclaimern.
        """.trimIndent()
        
        val aiRequest = AiRequest(
            prompt = prompt,
            provider = com.example.fitapp.domain.entities.AiProvider.Gemini,
            taskType = TaskType.TRAINING_PLAN
        )
        
        return repository.generateText(aiRequest).mapCatching { response ->
            response
        }
    }
}