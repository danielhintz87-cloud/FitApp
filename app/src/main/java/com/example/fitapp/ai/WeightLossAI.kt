package com.example.fitapp.ai

import android.content.Context
import com.example.fitapp.data.db.WeightLossProgramEntity
import com.example.fitapp.data.db.BMIHistoryEntity
import com.example.fitapp.data.db.BehavioralCheckInEntity
import com.example.fitapp.domain.ActivityLevel
import com.example.fitapp.domain.BMICategory

/**
 * AI request for weight loss plan generation
 */
data class WeightLossAiRequest(
    val currentBMI: Float,
    val targetBMI: Float,
    val timeframeWeeks: Int,
    val dietaryRestrictions: List<String>,
    val activityLevel: String,
    val preferredExerciseTypes: List<String>,
    val previousAttempts: List<String>? = null,
    val currentChallenges: List<String>? = null,
    val currentWeight: Float,
    val targetWeight: Float,
    val age: Int,
    val isMale: Boolean
)

/**
 * AI request for personalized weight loss tips
 */
data class PersonalizedTipsRequest(
    val progressData: List<WeightLossProgressData>,
    val adherencePattern: AdherencePattern,
    val strugglingAreas: List<String>,
    val currentProgram: WeightLossProgramEntity?
)

/**
 * Progress data for AI analysis
 */
data class WeightLossProgressData(
    val date: String,
    val weight: Float,
    val bmi: Float,
    val caloriesConsumed: Int?,
    val caloriesTarget: Int,
    val adherenceScore: Float, // 0.0-1.0
    val moodScore: Int?, // 1-10
    val stressLevel: Int?, // 1-10
    val exerciseMinutes: Int?
)

/**
 * Adherence pattern analysis
 */
data class AdherencePattern(
    val averageAdherence: Float,
    val weeklyTrend: String, // "improving", "declining", "stable"
    val bestDays: List<String>, // days of week
    val challengingDays: List<String>,
    val motivationalFactors: List<String>
)

/**
 * AI-generated weight loss plan
 */
data class WeightLossPlan(
    val summary: String,
    val dailyCalorieTarget: Int,
    val macroRecommendations: String,
    val exerciseRecommendations: List<String>,
    val mealSuggestions: List<String>,
    val behavioralTips: List<String>,
    val weeklyGoals: List<String>,
    val potentialChallenges: List<String>,
    val motivationalStrategies: List<String>
)

/**
 * AI-generated personalized tip
 */
data class PersonalizedTip(
    val category: String, // "nutrition", "exercise", "mindset", "habit"
    val title: String,
    val description: String,
    val actionSteps: List<String>,
    val expectedBenefit: String,
    val difficultyLevel: String // "easy", "medium", "hard"
)

/**
 * Weight loss insights from AI analysis
 */
data class WeightLossInsights(
    val progressSummary: String,
    val keySuccessFactors: List<String>,
    val improvementAreas: List<String>,
    val predictedTimeToGoal: String,
    val motivationalMessage: String,
    val nextWeekFocus: String
)

/**
 * Extension functions for weight loss AI features
 */
suspend fun AppAi.generateWeightLossPlan(
    context: Context,
    request: WeightLossAiRequest
): Result<WeightLossPlan> {
    val prompt = buildWeightLossPlanPrompt(request)
    
    return try {
        val response = planWithOptimalProvider(context, PlanRequest(
            goal = "Gesunden Gewichtsverlust erreichen: ${request.currentWeight}kg → ${request.targetWeight}kg",
            weeks = request.timeframeWeeks,
            sessionsPerWeek = getRecommendedSessionsPerWeek(request.activityLevel),
            minutesPerSession = getRecommendedMinutesPerSession(request.activityLevel),
            equipment = request.preferredExerciseTypes
        ))
        
        response.map { parseWeightLossPlan(it) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun AppAi.generatePersonalizedTips(
    context: Context,
    request: PersonalizedTipsRequest
): Result<List<PersonalizedTip>> {
    val prompt = buildPersonalizedTipsPrompt(request)
    
    return try {
        val response = planWithOptimalProvider(context, PlanRequest(
            goal = "Personalisierte Abnehm-Tipps basierend auf Fortschritt",
            weeks = 1,
            sessionsPerWeek = 3,
            minutesPerSession = 30,
            equipment = emptyList()
        ))
        
        response.map { parsePersonalizedTips(it) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun AppAi.generateWeightLossInsights(
    context: Context,
    progressData: List<WeightLossProgressData>,
    currentProgram: WeightLossProgramEntity?
): Result<WeightLossInsights> {
    val prompt = buildInsightsPrompt(progressData, currentProgram)
    
    return try {
        val response = planWithOptimalProvider(context, PlanRequest(
            goal = "Gewichtsverlust-Insights und Fortschrittsanalyse",
            weeks = 1,
            sessionsPerWeek = 1,
            minutesPerSession = 15,
            equipment = emptyList()
        ))
        
        response.map { parseWeightLossInsights(it) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Helper functions for prompt building
 */
private fun buildWeightLossPlanPrompt(request: WeightLossAiRequest): String {
    return """
        Erstelle einen detaillierten, wissenschaftlich fundierten Gewichtsverlust-Plan für:
        
        Aktuelle Situation:
        - BMI: ${request.currentBMI} (${BMICategory.fromBMI(request.currentBMI).germanName})
        - Ziel-BMI: ${request.targetBMI}
        - Aktuelles Gewicht: ${request.currentWeight}kg → Zielgewicht: ${request.targetWeight}kg
        - Zeitrahmen: ${request.timeframeWeeks} Wochen
        - Alter: ${request.age}, Geschlecht: ${if (request.isMale) "männlich" else "weiblich"}
        - Aktivitätslevel: ${request.activityLevel}
        
        Präferenzen:
        - Diätbeschränkungen: ${request.dietaryRestrictions.joinToString(", ").ifEmpty { "Keine" }}
        - Bevorzugte Übungen: ${request.preferredExerciseTypes.joinToString(", ").ifEmpty { "Offen für alles" }}
        - Bisherige Versuche: ${request.previousAttempts?.joinToString(", ") ?: "Keine Angabe"}
        - Aktuelle Herausforderungen: ${request.currentChallenges?.joinToString(", ") ?: "Keine Angabe"}
        
        Bitte erstelle einen realistischen, nachhaltigen Plan mit:
        1. Täglichem Kalorienziel und Makroverteilung
        2. Wöchentlichen Trainingsempfehlungen
        3. Mahlzeiten-Vorschlägen
        4. Verhaltenstipps für nachhaltigen Erfolg
        5. Wöchentlichen Zwischenzielen
        6. Strategien gegen häufige Hindernisse
        7. Motivations- und Durchhalte-Strategien
        
        Berücksichtige deutsche Essgewohnheiten und verfügbare Lebensmittel.
    """.trimIndent()
}

private fun buildPersonalizedTipsPrompt(request: PersonalizedTipsRequest): String {
    val programInfo = request.currentProgram?.let {
        "Aktuelles Programm: ${it.dailyCalorieTarget}kcal/Tag, Ziel: ${it.weeklyWeightLossGoal}kg/Woche"
    } ?: "Kein aktives Programm"
    
    return """
        Analysiere das Gewichtsverlust-Verhalten und erstelle personalisierte Verbesserungstipps:
        
        $programInfo
        
        Einhaltungsverhalten:
        - Durchschnittliche Einhaltung: ${(request.adherencePattern.averageAdherence * 100).toInt()}%
        - Trend: ${request.adherencePattern.weeklyTrend}
        - Beste Tage: ${request.adherencePattern.bestDays.joinToString(", ")}
        - Schwierige Tage: ${request.adherencePattern.challengingDays.joinToString(", ")}
        - Motivationsfaktoren: ${request.adherencePattern.motivationalFactors.joinToString(", ")}
        
        Problembereiche: ${request.strugglingAreas.joinToString(", ")}
        
        Fortschrittsdaten der letzten Wochen:
        ${request.progressData.takeLast(7).joinToString("\n") { data ->
            "${data.date}: ${data.weight}kg, BMI ${data.bmi}, Einhaltung ${(data.adherenceScore * 100).toInt()}%"
        }}
        
        Erstelle 5-7 spezifische, umsetzbare Tipps in den Kategorien:
        - Ernährung
        - Training
        - Mindset/Motivation
        - Gewohnheiten
        
        Jeder Tipp sollte enthalten:
        - Kurzen Titel
        - Beschreibung des Problems/der Chance
        - 2-3 konkrete Handlungsschritte
        - Erwarteten Nutzen
        - Schwierigkeitsgrad
    """.trimIndent()
}

private fun buildInsightsPrompt(
    progressData: List<WeightLossProgressData>,
    currentProgram: WeightLossProgramEntity?
): String {
    val recentData = progressData.takeLast(14)
    val programInfo = currentProgram?.let {
        "Programm: ${it.startWeight}kg → ${it.targetWeight}kg, ${it.dailyCalorieTarget}kcal/Tag"
    } ?: "Kein aktives Programm"
    
    return """
        Analysiere den Gewichtsverlust-Fortschritt und erstelle aufschlussreiche Insights:
        
        $programInfo
        
        Fortschrittsdaten der letzten 2 Wochen:
        ${recentData.joinToString("\n") { data ->
            "${data.date}: ${data.weight}kg (BMI ${data.bmi}), " +
            "Kalorien: ${data.caloriesConsumed}/${data.caloriesTarget}, " +
            "Einhaltung: ${(data.adherenceScore * 100).toInt()}%, " +
            "Stimmung: ${data.moodScore ?: "k.A."}/10, " +
            "Stress: ${data.stressLevel ?: "k.A."}/10"
        }}
        
        Bitte analysiere und erstelle:
        1. Zusammenfassung des Fortschritts
        2. Erfolgsfaktoren identifizieren
        3. Verbesserungsbereiche
        4. Realistische Prognose für Zielerreichung
        5. Motivierende Botschaft
        6. Fokus für die nächste Woche
        
        Sei ehrlich aber motivierend. Erkenne Fortschritte an, auch wenn sie klein sind.
    """.trimIndent()
}

/**
 * Helper functions for response parsing
 */
private fun parseWeightLossPlan(response: String): WeightLossPlan {
    // Simple parsing - in a real implementation, this would be more sophisticated
    val sections = response.split("\n\n")
    
    return WeightLossPlan(
        summary = sections.firstOrNull() ?: "Individueller Gewichtsverlust-Plan erstellt",
        dailyCalorieTarget = extractCalorieTarget(response),
        macroRecommendations = extractSection(response, "makro", "verteilung") ?: "Ausgewogene Makronährstoff-Verteilung empfohlen",
        exerciseRecommendations = extractListItems(response, "training", "übung"),
        mealSuggestions = extractListItems(response, "mahlzeit", "essen"),
        behavioralTips = extractListItems(response, "tipp", "verhalten"),
        weeklyGoals = extractListItems(response, "ziel", "woche"),
        potentialChallenges = extractListItems(response, "hindernis", "herausforderung"),
        motivationalStrategies = extractListItems(response, "motivation", "strategie")
    )
}

private fun parsePersonalizedTips(response: String): List<PersonalizedTip> {
    // Simple parsing - extract tips from response
    val tips = mutableListOf<PersonalizedTip>()
    val sections = response.split("\n")
    
    var currentTip: MutableMap<String, String>? = null
    
    sections.forEach { line ->
        when {
            line.contains("Tipp") && line.contains(":") -> {
                currentTip?.let { tip ->
                    tips.add(createTipFromMap(tip))
                }
                currentTip = mutableMapOf("title" to line.substringAfter(":").trim())
            }
            line.startsWith("-") && currentTip != null -> {
                val content = line.substring(1).trim()
                when {
                    content.contains("Kategorie") -> currentTip!!["category"] = content.substringAfter(":").trim()
                    content.contains("Beschreibung") -> currentTip!!["description"] = content.substringAfter(":").trim()
                    content.contains("Schritte") -> currentTip!!["actionSteps"] = content.substringAfter(":").trim()
                    content.contains("Nutzen") -> currentTip!!["expectedBenefit"] = content.substringAfter(":").trim()
                    content.contains("Schwierigkeit") -> currentTip!!["difficultyLevel"] = content.substringAfter(":").trim()
                }
            }
        }
    }
    
    currentTip?.let { tip ->
        tips.add(createTipFromMap(tip))
    }
    
    return tips.ifEmpty {
        // Fallback tips if parsing fails
        listOf(
            PersonalizedTip(
                category = "nutrition",
                title = "Regelmäßige Mahlzeiten",
                description = "Halte feste Essenszeiten ein",
                actionSteps = listOf("3 Hauptmahlzeiten planen", "Snacks begrenzen"),
                expectedBenefit = "Bessere Kalorienkontrolle",
                difficultyLevel = "easy"
            )
        )
    }
}

private fun parseWeightLossInsights(response: String): WeightLossInsights {
    return WeightLossInsights(
        progressSummary = extractSection(response, "fortschritt", "zusammenfassung") 
            ?: "Dein Gewichtsverlust-Journey zeigt positive Entwicklungen.",
        keySuccessFactors = extractListItems(response, "erfolg", "faktor"),
        improvementAreas = extractListItems(response, "verbesserung", "bereich"),
        predictedTimeToGoal = extractSection(response, "prognose", "ziel") 
            ?: "Bleibe konsequent für beste Ergebnisse",
        motivationalMessage = extractSection(response, "motivation", "botschaft") 
            ?: "Du machst großartige Fortschritte! Weiter so!",
        nextWeekFocus = extractSection(response, "fokus", "woche") 
            ?: "Konzentriere dich auf Konsistenz bei Kalorienziel und Training"
    )
}

/**
 * Helper extraction functions
 */
private fun extractCalorieTarget(text: String): Int {
    val regex = Regex("""(\d{4})\s*kcal""")
    return regex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 1500
}

private fun extractSection(text: String, vararg keywords: String): String? {
    val lines = text.split("\n")
    var inSection = false
    val sectionContent = mutableListOf<String>()
    
    lines.forEach { line ->
        if (keywords.any { line.lowercase().contains(it) }) {
            inSection = true
            sectionContent.add(line)
        } else if (inSection && line.isBlank()) {
            inSection = false
        } else if (inSection) {
            sectionContent.add(line)
        }
    }
    
    return sectionContent.joinToString("\n").takeIf { it.isNotBlank() }
}

private fun extractListItems(text: String, vararg keywords: String): List<String> {
    val lines = text.split("\n")
    val items = mutableListOf<String>()
    
    lines.forEach { line ->
        if (line.startsWith("-") || line.startsWith("•") || line.startsWith("*")) {
            val item = line.substring(1).trim()
            if (keywords.isEmpty() || keywords.any { item.lowercase().contains(it) }) {
                items.add(item)
            }
        }
    }
    
    return items.take(5) // Limit to 5 items
}

private fun createTipFromMap(tipMap: Map<String, String>): PersonalizedTip {
    return PersonalizedTip(
        category = tipMap["category"] ?: "general",
        title = tipMap["title"] ?: "Tipp",
        description = tipMap["description"] ?: "Verbesserungsvorschlag",
        actionSteps = tipMap["actionSteps"]?.split(",")?.map { it.trim() } ?: listOf("Umsetzen"),
        expectedBenefit = tipMap["expectedBenefit"] ?: "Positive Auswirkung",
        difficultyLevel = tipMap["difficultyLevel"] ?: "medium"
    )
}

private fun getRecommendedSessionsPerWeek(activityLevel: String): Int {
    return when (activityLevel.lowercase()) {
        "sedentary" -> 3
        "lightly_active" -> 4
        "moderately_active" -> 5
        "very_active" -> 6
        "extra_active" -> 7
        else -> 4
    }
}

private fun getRecommendedMinutesPerSession(activityLevel: String): Int {
    return when (activityLevel.lowercase()) {
        "sedentary" -> 30
        "lightly_active" -> 40
        "moderately_active" -> 50
        "very_active" -> 60
        "extra_active" -> 75
        else -> 45
    }
}