package com.example.fitapp.ai

import android.content.Context
import com.example.fitapp.data.db.WeightLossProgramEntity
import com.example.fitapp.domain.BMICategory
import com.example.fitapp.domain.entities.*

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
    val isMale: Boolean,
)

/**
 * AI request for personalized weight loss tips
 */
data class PersonalizedTipsRequest(
    val progressData: List<WeightLossProgressData>,
    val adherencePattern: AdherencePattern,
    val strugglingAreas: List<String>,
    val currentProgram: WeightLossProgramEntity?,
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
    val exerciseMinutes: Int?,
)

/**
 * Adherence pattern analysis
 */
data class AdherencePattern(
    val averageAdherence: Float,
    val weeklyTrend: String, // "improving", "declining", "stable"
    val bestDays: List<String>, // days of week
    val challengingDays: List<String>,
    val motivationalFactors: List<String>,
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
    val motivationalStrategies: List<String>,
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
    val difficultyLevel: String, // "easy", "medium", "hard"
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
    val nextWeekFocus: String,
)

/**
 * Extension functions for weight loss AI features
 */
suspend fun AppAi.generateWeightLossPlan(
    context: Context,
    request: WeightLossAiRequest,
): Result<WeightLossPlan> {
    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Gesunden Gewichtsverlust erreichen: ${request.currentWeight}kg → ${request.targetWeight}kg",
                    weeks = request.timeframeWeeks,
                    sessionsPerWeek = getRecommendedSessionsPerWeek(request.activityLevel),
                    minutesPerSession = getRecommendedMinutesPerSession(request.activityLevel),
                    equipment = request.preferredExerciseTypes,
                ),
            )

        response.map { parseWeightLossPlan(it) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun AppAi.generatePersonalizedTips(
    context: Context,
    request: PersonalizedTipsRequest,
): Result<List<PersonalizedTip>> {
    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Personalisierte Abnehm-Tipps für ${request.strugglingAreas.joinToString(
                        ", ",
                    ).ifEmpty { "allgemeine Bereiche" }}",
                    weeks = 1,
                    sessionsPerWeek = 3,
                    minutesPerSession = 30,
                    equipment = emptyList(),
                ),
            )

        response.map { parsePersonalizedTips(it) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun AppAi.generateWeightLossInsights(
    context: Context,
    progressData: List<WeightLossProgressData>,
    currentProgram: WeightLossProgramEntity?,
): Result<WeightLossInsights> {
    val programInfo = currentProgram?.let { "Programm: ${it.startWeight}kg → ${it.targetWeight}kg" } ?: "Kein Programm"
    val dataPoints = progressData.size

    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Gewichtsverlust-Insights für $dataPoints Datenpunkte, $programInfo",
                    weeks = 1,
                    sessionsPerWeek = 1,
                    minutesPerSession = 15,
                    equipment = emptyList(),
                ),
            )

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
    val programInfo =
        request.currentProgram?.let {
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
    currentProgram: WeightLossProgramEntity?,
): String {
    val recentData = progressData.takeLast(14)
    val programInfo =
        currentProgram?.let {
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
        motivationalStrategies = extractListItems(response, "motivation", "strategie"),
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
                    content.contains(
                        "Schwierigkeit",
                    ) -> currentTip!!["difficultyLevel"] = content.substringAfter(":").trim()
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
                difficultyLevel = "easy",
            ),
        )
    }
}

private fun parseWeightLossInsights(response: String): WeightLossInsights {
    return WeightLossInsights(
        progressSummary =
            extractSection(response, "fortschritt", "zusammenfassung")
                ?: "Dein Gewichtsverlust-Journey zeigt positive Entwicklungen.",
        keySuccessFactors = extractListItems(response, "erfolg", "faktor"),
        improvementAreas = extractListItems(response, "verbesserung", "bereich"),
        predictedTimeToGoal =
            extractSection(response, "prognose", "ziel")
                ?: "Bleibe konsequent für beste Ergebnisse",
        motivationalMessage =
            extractSection(response, "motivation", "botschaft")
                ?: "Du machst großartige Fortschritte! Weiter so!",
        nextWeekFocus =
            extractSection(response, "fokus", "woche")
                ?: "Konzentriere dich auf Konsistenz bei Kalorienziel und Training",
    )
}

/**
 * Helper extraction functions
 */
private fun extractCalorieTarget(text: String): Int {
    val regex = Regex("""(\d{4})\s*kcal""")
    return regex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 1500
}

private fun extractSection(
    text: String,
    vararg keywords: String,
): String? {
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

private fun extractListItems(
    text: String,
    vararg keywords: String,
): List<String> {
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
        difficultyLevel = tipMap["difficultyLevel"] ?: "medium",
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

/**
 * AI Personal Trainer Extension Functions
 */

suspend fun AppAi.generatePersonalizedWorkout(
    context: Context,
    request: AIPersonalTrainerRequest,
): Result<WorkoutPlan> {
    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Personalisiertes Workout für ${request.userProfile.fitnessGoals.joinToString(", ")}",
                    weeks = 1,
                    sessionsPerWeek = 3,
                    minutesPerSession = request.availableTime,
                    equipment = request.equipment,
                ),
            )

        response.map { parseWorkoutPlan(it, request.availableTime) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun AppAi.generateNutritionAdvice(
    context: Context,
    userProfile: UserProfile,
    goals: List<String>,
): Result<PersonalizedMealPlan> {
    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Ernährungsplan für ${goals.joinToString(
                        ", ",
                    )} - ${userProfile.currentWeight}kg → ${userProfile.targetWeight}kg",
                    weeks = 1,
                    sessionsPerWeek = 7, // Daily meals
                    minutesPerSession = 30,
                    equipment = emptyList(),
                ),
            )

        response.map { parseNutritionPlan(it, userProfile) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun AppAi.analyzeProgress(
    context: Context,
    progressData: List<WeightEntry>,
    userProfile: UserProfile,
): Result<ProgressAnalysis> {
    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Fortschritts-Analyse für ${progressData.size} Gewichtsmessungen",
                    weeks = 1,
                    sessionsPerWeek = 1,
                    minutesPerSession = 15,
                    equipment = emptyList(),
                ),
            )

        response.map { parseProgressAnalysis(it, progressData) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun AppAi.generateMotivation(
    context: Context,
    userProfile: UserProfile,
    recentProgress: List<WeightEntry>,
): Result<MotivationalMessage> {
    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Motivationsnachricht für Fitness-Journey",
                    weeks = 1,
                    sessionsPerWeek = 1,
                    minutesPerSession = 5,
                    equipment = emptyList(),
                ),
            )

        response.map { parseMotivationalMessage(it) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun AppAi.getPersonalizedRecommendations(
    context: Context,
    userContext: UserContext,
): Result<AIPersonalTrainerResponse> {
    return try {
        val workoutResult =
            generatePersonalizedWorkout(
                context,
                AIPersonalTrainerRequest(
                    userProfile = userContext.profile,
                    fitnessLevel = userContext.fitnessLevel,
                    availableTime = 45,
                    equipment = userContext.availableEquipment,
                    goals = userContext.currentGoals,
                ),
            )

        val nutritionResult = generateNutritionAdvice(context, userContext.profile, userContext.currentGoals)
        val progressResult = analyzeProgress(context, userContext.recentProgress, userContext.profile)
        val motivationResult = generateMotivation(context, userContext.profile, userContext.recentProgress)

        Result.success(
            AIPersonalTrainerResponse(
                workoutPlan = workoutResult.getOrNull(),
                mealPlan = nutritionResult.getOrNull(),
                progressAnalysis = progressResult.getOrNull(),
                motivation = motivationResult.getOrNull(),
                recommendations = generateBasicRecommendations(),
            ),
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Parsing Functions for AI Personal Trainer
 */

private fun parseWorkoutPlan(
    response: String,
    duration: Int,
): WorkoutPlan {
    val exercises = extractExercises(response)
    return WorkoutPlan(
        title = "Personalisiertes Workout",
        description = extractSection(response, "beschreibung") ?: "Auf dich zugeschnittenes Training",
        exercises = exercises,
        estimatedDuration = duration,
        difficulty = extractDifficulty(response),
        equipment = extractEquipment(response),
    )
}

private fun parseNutritionPlan(
    response: String,
    userProfile: UserProfile,
): PersonalizedMealPlan {
    val targetCalories = calculateTargetCalories(userProfile)
    return PersonalizedMealPlan(
        title = "Personalisierter Ernährungsplan",
        dailyCalories = targetCalories,
        macroTargets = calculateMacros(targetCalories),
        meals = extractMeals(response),
    )
}

private fun parseProgressAnalysis(
    response: String,
    progressData: List<WeightEntry>,
): ProgressAnalysis {
    return ProgressAnalysis(
        weightTrend = determineWeightTrend(progressData),
        adherenceScore = 0.8f, // Simplified for now
        insights = extractListItems(response, "insight", "erkenntnis"),
        recommendations = extractListItems(response, "empfehlung", "tipp"),
    )
}

private fun parseMotivationalMessage(response: String): MotivationalMessage {
    return MotivationalMessage(
        title = "Deine tägliche Motivation",
        message = extractSection(response, "motivation") ?: "Du machst großartige Fortschritte! Weiter so!",
        type = "encouragement",
        actionSuggestion = extractSection(response, "aktion"),
    )
}

/**
 * Helper Functions
 */

private fun extractExercises(text: String): List<Exercise> {
    val exercises = mutableListOf<Exercise>()
    val lines = text.split("\n")

    lines.forEach { line ->
        if (line.contains("Übung") || line.contains("Exercise") || line.startsWith("-")) {
            val exerciseName = line.replace(Regex("^-\\s*"), "").split(":").first().trim()
            if (exerciseName.isNotBlank()) {
                exercises.add(
                    Exercise(
                        name = exerciseName,
                        sets = 3,
                        reps = "10-12",
                        restTime = "60 Sekunden",
                        instructions = "Führe die Übung kontrolliert aus",
                    ),
                )
            }
        }
    }

    return exercises.ifEmpty {
        listOf(
            Exercise("Liegestütze", 3, "8-12", "60 Sekunden", "Klassische Liegestütze für Oberkörper"),
            Exercise("Kniebeugen", 3, "12-15", "60 Sekunden", "Grundlegende Beinübung"),
            Exercise("Plank", 3, "30-60 Sekunden", "30 Sekunden", "Halte die Position stabil"),
        )
    }
}

private fun extractDifficulty(text: String): String {
    return when {
        text.lowercase().contains("anfänger") -> "beginner"
        text.lowercase().contains("fortgeschritten") -> "advanced"
        else -> "intermediate"
    }
}

private fun extractEquipment(text: String): List<String> {
    val equipment = mutableListOf<String>()
    if (text.lowercase().contains("hantel")) equipment.add("Hanteln")
    if (text.lowercase().contains("matte")) equipment.add("Matte")
    if (text.lowercase().contains("körpergewicht")) equipment.add("Körpergewicht")
    return equipment.ifEmpty { listOf("Körpergewicht") }
}

private fun extractMeals(text: String): List<MealPlan> {
    return listOf(
        MealPlan("breakfast", "Protein-Frühstück", 400, "Haferflocken mit Beeren und Joghurt"),
        MealPlan("lunch", "Ausgewogenes Mittagessen", 500, "Hähnchen mit Gemüse und Reis"),
        MealPlan("dinner", "Leichtes Abendessen", 450, "Fisch mit gedünstetem Gemüse"),
        MealPlan("snack", "Gesunder Snack", 200, "Nüsse oder Obst"),
    )
}

private fun calculateTargetCalories(userProfile: UserProfile): Int {
    // Simplified BMR calculation
    val bmr =
        if (userProfile.gender.lowercase() == "male") {
            88.362 + (13.397 * userProfile.currentWeight) + (4.799 * userProfile.height) - (5.677 * userProfile.age)
        } else {
            447.593 + (9.247 * userProfile.currentWeight) + (3.098 * userProfile.height) - (4.330 * userProfile.age)
        }

    val activityMultiplier =
        when (userProfile.activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "lightly_active" -> 1.375
            "moderately_active" -> 1.55
            "very_active" -> 1.725
            "extra_active" -> 1.9
            else -> 1.55
        }

    val tdee = bmr * activityMultiplier
    val deficit = if (userProfile.targetWeight < userProfile.currentWeight) 500 else 0

    return (tdee - deficit).toInt()
}

private fun calculateMacros(calories: Int): MacroTargets {
    val protein = (calories * 0.3 / 4).toInt() // 30% protein
    val carbs = (calories * 0.4 / 4).toInt() // 40% carbs
    val fat = (calories * 0.3 / 9).toInt() // 30% fat

    return MacroTargets(protein, carbs, fat)
}

private fun determineWeightTrend(progressData: List<WeightEntry>): String {
    if (progressData.size < 2) return "insufficient_data"

    val recent = progressData.takeLast(3).map { it.weight }
    val older = progressData.take(3).map { it.weight }

    val recentAvg = recent.average()
    val olderAvg = older.average()

    return when {
        recentAvg < olderAvg - 0.5 -> "decreasing"
        recentAvg > olderAvg + 0.5 -> "increasing"
        else -> "stable"
    }
}

private fun generateBasicRecommendations(): List<AIRecommendation> {
    return listOf(
        AIRecommendation(
            title = "Tägliches Workout",
            description = "Bleibe mit deinem Trainingsplan konsistent",
            type = "workout",
            priority = "high",
        ),
        AIRecommendation(
            title = "Hydration Check",
            description = "Trinke mindestens 2 Liter Wasser täglich",
            type = "habit",
            priority = "medium",
        ),
        AIRecommendation(
            title = "Schlaf Optimierung",
            description = "7-9 Stunden Schlaf für beste Erholung",
            type = "habit",
            priority = "high",
        ),
    )
}

// Advanced Workout Execution Enhancement - Phase 1 AI Extensions

/**
 * Generate intelligent workout progression recommendations based on performance history
 */
suspend fun AppAi.generateWorkoutProgression(
    context: Context,
    exercise: com.example.fitapp.ui.screens.ExerciseStep,
    performanceHistory: List<com.example.fitapp.data.db.WorkoutPerformanceEntity>,
    userProfile: com.example.fitapp.ai.UserWorkoutProfile,
): Result<com.example.fitapp.ai.ProgressionSuggestion> {
    if (performanceHistory.isEmpty()) {
        return Result.success(createDefaultProgression(exercise, userProfile))
    }

    val prompt =
        """
        Als erfahrener Personal Trainer analysiere die Trainingsleistung und erstelle eine optimale Progression.
        
        Übung: ${exercise.name}
        Aktueller Status: ${exercise.value}
        
        Leistungshistorie (letzte 5 Einheiten):
        ${performanceHistory.takeLast(5).joinToString("\n") { perf ->
            "Gewicht: ${perf.weight}kg, Wiederholungen: ${perf.reps}, Volume: ${perf.volume}, Form: ${(perf.formQuality * 100).toInt()}%, RPE: ${perf.perceivedExertion}"
        }}
        
        Benutzerprofil:
        - Fitnesslevel: ${userProfile.fitnessLevel}
        - Ziele: ${userProfile.primaryGoals.joinToString(", ")}
        - Verfügbare Ausrüstung: ${userProfile.availableEquipment.joinToString(", ")}
        - Verletzungen: ${userProfile.injuries.joinToString(", ").ifEmpty { "Keine" }}
        
        Analysiere die Entwicklung und erstelle eine Empfehlung für die nächste Trainingseinheit:
        
        1. Progressionstrend analysieren (verbessernd, stagnierend, abnehmend)
        2. Gewichts- und Wiederholungsempfehlung
        3. Begründung der Empfehlung
        4. Alternative Ansätze
        5. Sicherheitshinweise
        
        Format:
        EMPFEHLUNG:
        Gewicht: [Zahl]kg
        Wiederholungen: [Zahl]
        Begründung: [Erklärung]
        
        ALTERNATIVEN:
        - Alternative 1: [Beschreibung]
        - Alternative 2: [Beschreibung]
        
        SICHERHEIT:
        - [Hinweis 1]
        - [Hinweis 2]
        """.trimIndent()

    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Workout Progression für ${exercise.name}",
                    weeks = 1,
                    sessionsPerWeek = 3,
                    minutesPerSession = 30,
                    equipment = userProfile.availableEquipment,
                ),
            )
        Result.success(parseProgressionResponse(response.getOrThrow(), exercise, performanceHistory))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Analyze movement form quality using AI and sensor data
 */
suspend fun AppAi.analyzeFormQuality(
    context: Context,
    sensorData: com.example.fitapp.ai.MovementData,
    exerciseType: String,
): Result<com.example.fitapp.ai.FormQualityAssessment> {
    val prompt =
        """
        Als Bewegungsanalyst bewerte die Übungsausführung basierend auf Sensordaten.
        
        Übung: $exerciseType
        
        Sensordaten:
        Beschleunigung: X=${sensorData.accelerometer.first}, Y=${sensorData.accelerometer.second}, Z=${sensorData.accelerometer.third}
        Gyroskop: X=${sensorData.gyroscope.first}, Y=${sensorData.gyroscope.second}, Z=${sensorData.gyroscope.third}
        
        Analysiere die Bewegungsqualität und gib Feedback:
        
        1. Gesamtbewertung (0-100%)
        2. Verbesserungsvorschläge
        3. Risikofaktoren
        4. Positive Aspekte
        
        Format:
        BEWERTUNG: [0-100]%
        
        VERBESSERUNGEN:
        - [Vorschlag 1]
        - [Vorschlag 2]
        
        RISIKEN:
        - [Risiko 1]
        - [Risiko 2]
        
        POSITIV:
        - [Aspekt 1]
        - [Aspekt 2]
        """.trimIndent()

    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Form Analysis für $exerciseType",
                    weeks = 1,
                    sessionsPerWeek = 1,
                    minutesPerSession = 10,
                    equipment = listOf("Sensoren"),
                ),
            )
        Result.success(parseFormAnalysisResponse(response.getOrThrow()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Generate real-time coaching tips based on current performance
 */
suspend fun AppAi.generateCoachingTips(
    context: Context,
    currentPerformance: com.example.fitapp.data.db.WorkoutPerformanceEntity,
    exerciseContext: com.example.fitapp.ai.WorkoutContext,
): Result<List<com.example.fitapp.ai.CoachingTip>> {
    val prompt =
        """
        Als Personal Trainer gib sofortige Coaching-Tipps für die aktuelle Übung.
        
        Aktuelle Übung: ${exerciseContext.currentExercise?.name}
        Aktuelle Leistung:
        - Gewicht: ${currentPerformance.weight}kg
        - Wiederholungen: ${currentPerformance.reps}
        - Form-Qualität: ${(currentPerformance.formQuality * 100).toInt()}%
        - RPE: ${currentPerformance.perceivedExertion}/10
        - Herzfrequenz: ${currentPerformance.heartRateAvg ?: "Unbekannt"}
        
        Session Fortschritt: ${(exerciseContext.sessionProgress * 100).toInt()}%
        Ermüdungslevel: ${exerciseContext.userFatigueLevel}
        
        Erstelle 2-3 präzise, motivierende Coaching-Tipps:
        
        Format pro Tipp:
        TYP: [FORM_IMPROVEMENT/REST_OPTIMIZATION/MOTIVATION/SAFETY_WARNING/TECHNIQUE_TIP]
        PRIORITÄT: [LOW/MEDIUM/HIGH/CRITICAL]
        NACHRICHT: [Kurzer, präziser Tipp]
        AKTION: [Optionale konkrete Handlung]
        """.trimIndent()

    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Coaching Tips für ${exerciseContext.currentExercise?.name}",
                    weeks = 1,
                    sessionsPerWeek = 1,
                    minutesPerSession = 5,
                    equipment = emptyList(),
                ),
            )
        Result.success(parseCoachingTipsResponse(response.getOrThrow()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Predict workout outcome and optimize performance
 */
suspend fun AppAi.predictWorkoutOutcome(
    context: Context,
    currentSession: com.example.fitapp.data.db.WorkoutSessionEntity,
    userHistory: com.example.fitapp.ai.UserWorkoutProfile,
): Result<com.example.fitapp.ai.PerformancePrediction> {
    val prompt =
        """
        Als Trainingsanalyst prognostiziere das Workout-Ergebnis basierend auf aktuellen Daten.
        
        Aktuelle Session:
        - Gesamt-Volume: ${currentSession.totalVolume}
        - Durchschnittliche Herzfrequenz: ${currentSession.averageHeartRate ?: "Unbekannt"}
        - Effizienz-Score: ${currentSession.workoutEfficiencyScore}
        - Ermüdungslevel: ${currentSession.fatigueLevel}
        - Fortschritt: ${currentSession.completionPercentage}%
        
        Benutzerhistorie:
        - Fitnesslevel: ${userHistory.fitnessLevel}
        - Bevorzugte Intensität: ${userHistory.preferredIntensity}
        - Max Herzfrequenz: ${userHistory.maxHeartRate}
        - Ruhe Herzfrequenz: ${userHistory.restingHeartRate}
        
        Erstelle eine Prognose mit Optimierungsempfehlungen:
        
        Format:
        ERWARTETES_VOLUME: [Zahl]
        ERWARTETE_DAUER: [Minuten]
        ERMÜDUNGS_PROGNOSE: [low/medium/high]
        REST_ANPASSUNG: [+/-Sekunden]
        VERTRAUEN: [0-100]%
        
        OPTIMIERUNGEN:
        - [Empfehlung 1]
        - [Empfehlung 2]
        """.trimIndent()

    return try {
        val response =
            planWithOptimalProvider(
                context,
                PlanRequest(
                    goal = "Performance Prediction für Training",
                    weeks = 1,
                    sessionsPerWeek = 1,
                    minutesPerSession = 60,
                    equipment = emptyList(),
                ),
            )
        Result.success(parsePerformancePredictionResponse(response.getOrThrow()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Private helper functions for parsing AI responses

private fun createDefaultProgression(
    exercise: com.example.fitapp.ui.screens.ExerciseStep,
    userProfile: com.example.fitapp.ai.UserWorkoutProfile,
): com.example.fitapp.ai.ProgressionSuggestion {
    return com.example.fitapp.ai.ProgressionSuggestion(
        exerciseId = exercise.name,
        exerciseName = exercise.name,
        currentWeight = 0f,
        recommendedWeight =
            when (userProfile.fitnessLevel) {
                "beginner" -> 5f
                "intermediate" -> 10f
                "advanced" -> 15f
                else -> 8f
            },
        currentReps = 8,
        recommendedReps = 10,
        reason = "Basis-Progression für ${userProfile.fitnessLevel} Level",
        confidence = 0.7f,
        alternatives =
            listOf(
                com.example.fitapp.ai.ProgressionAlternative(
                    type = "rep_increase",
                    description = "Erhöhe Wiederholungen statt Gewicht",
                    weight = null,
                    reps = 12,
                    difficulty = "easier",
                ),
            ),
    )
}

private fun parseProgressionResponse(
    response: String,
    exercise: com.example.fitapp.ui.screens.ExerciseStep,
    history: List<com.example.fitapp.data.db.WorkoutPerformanceEntity>,
): com.example.fitapp.ai.ProgressionSuggestion {
    val lines = response.lines()

    var recommendedWeight = history.lastOrNull()?.weight ?: 5f
    var recommendedReps = history.lastOrNull()?.reps ?: 10
    var reason = "AI-basierte Progression"

    for (line in lines) {
        when {
            line.startsWith("Gewicht:") -> {
                recommendedWeight = line.substringAfter(":").replace("kg", "").trim().toFloatOrNull() ?: recommendedWeight
            }
            line.startsWith("Wiederholungen:") -> {
                recommendedReps = line.substringAfter(":").trim().toIntOrNull() ?: recommendedReps
            }
            line.startsWith("Begründung:") -> {
                reason = line.substringAfter(":").trim()
            }
        }
    }

    return com.example.fitapp.ai.ProgressionSuggestion(
        exerciseId = exercise.name,
        exerciseName = exercise.name,
        currentWeight = history.lastOrNull()?.weight ?: 0f,
        recommendedWeight = recommendedWeight,
        currentReps = history.lastOrNull()?.reps ?: 8,
        recommendedReps = recommendedReps,
        reason = reason,
        confidence = 0.85f,
        alternatives = parseAlternatives(response),
    )
}

private fun parseFormAnalysisResponse(response: String): com.example.fitapp.ai.FormQualityAssessment {
    val lines = response.lines()

    var overallQuality = 0.8f
    val improvements = mutableListOf<String>()
    val risks = mutableListOf<String>()
    val positives = mutableListOf<String>()

    var currentSection = ""

    for (line in lines) {
        when {
            line.startsWith("BEWERTUNG:") -> {
                val percentage = line.substringAfter(":").replace("%", "").trim().toIntOrNull() ?: 80
                overallQuality = percentage / 100f
            }
            line.startsWith("VERBESSERUNGEN:") -> currentSection = "improvements"
            line.startsWith("RISIKEN:") -> currentSection = "risks"
            line.startsWith("POSITIV:") -> currentSection = "positives"
            line.startsWith("- ") -> {
                val item = line.substring(2).trim()
                when (currentSection) {
                    "improvements" -> improvements.add(item)
                    "risks" -> risks.add(item)
                    "positives" -> positives.add(item)
                }
            }
        }
    }

    return com.example.fitapp.ai.FormQualityAssessment(
        overallQuality = overallQuality,
        improvements = improvements,
        riskFactors = risks,
        positiveAspects = positives,
    )
}

private fun parseCoachingTipsResponse(response: String): List<com.example.fitapp.ai.CoachingTip> {
    val tips = mutableListOf<com.example.fitapp.ai.CoachingTip>()
    val lines = response.lines()

    var currentTip: MutableMap<String, String> = mutableMapOf()

    for (line in lines) {
        when {
            line.startsWith("TYP:") -> {
                if (currentTip.isNotEmpty()) {
                    tips.add(createCoachingTip(currentTip))
                    currentTip = mutableMapOf()
                }
                currentTip["type"] = line.substringAfter(":").trim()
            }
            line.startsWith("PRIORITÄT:") -> currentTip["priority"] = line.substringAfter(":").trim()
            line.startsWith("NACHRICHT:") -> currentTip["message"] = line.substringAfter(":").trim()
            line.startsWith("AKTION:") -> currentTip["action"] = line.substringAfter(":").trim()
        }
    }

    if (currentTip.isNotEmpty()) {
        tips.add(createCoachingTip(currentTip))
    }

    return tips.ifEmpty {
        listOf(
            com.example.fitapp.ai.CoachingTip(
                type = com.example.fitapp.ai.CoachingTipType.MOTIVATION,
                message = "Du machst das großartig! Bleib fokussiert.",
                priority = com.example.fitapp.ai.Priority.MEDIUM,
            ),
        )
    }
}

private fun createCoachingTip(tipData: Map<String, String>): com.example.fitapp.ai.CoachingTip {
    val type =
        when (tipData["type"]?.uppercase()) {
            "FORM_IMPROVEMENT" -> com.example.fitapp.ai.CoachingTipType.FORM_IMPROVEMENT
            "REST_OPTIMIZATION" -> com.example.fitapp.ai.CoachingTipType.REST_OPTIMIZATION
            "MOTIVATION" -> com.example.fitapp.ai.CoachingTipType.MOTIVATION
            "SAFETY_WARNING" -> com.example.fitapp.ai.CoachingTipType.SAFETY_WARNING
            "TECHNIQUE_TIP" -> com.example.fitapp.ai.CoachingTipType.TECHNIQUE_TIP
            else -> com.example.fitapp.ai.CoachingTipType.MOTIVATION
        }

    val priority =
        when (tipData["priority"]?.uppercase()) {
            "LOW" -> com.example.fitapp.ai.Priority.LOW
            "MEDIUM" -> com.example.fitapp.ai.Priority.MEDIUM
            "HIGH" -> com.example.fitapp.ai.Priority.HIGH
            "CRITICAL" -> com.example.fitapp.ai.Priority.CRITICAL
            else -> com.example.fitapp.ai.Priority.MEDIUM
        }

    return com.example.fitapp.ai.CoachingTip(
        type = type,
        message = tipData["message"] ?: "Bleib dran!",
        priority = priority,
        actionable = tipData["action"]?.isNotBlank() == true,
        action = tipData["action"],
    )
}

private fun parsePerformancePredictionResponse(response: String): com.example.fitapp.ai.PerformancePrediction {
    val lines = response.lines()

    var expectedVolume = 0f
    var expectedDuration = 60
    var fatigueForecast = "medium"
    var restAdjustment = 0
    var confidence = 0.75f

    for (line in lines) {
        when {
            line.startsWith("ERWARTETES_VOLUME:") -> {
                expectedVolume = line.substringAfter(":").trim().toFloatOrNull() ?: 0f
            }
            line.startsWith("ERWARTETE_DAUER:") -> {
                expectedDuration = line.substringAfter(":").replace("Minuten", "").trim().toIntOrNull() ?: 60
            }
            line.startsWith("ERMÜDUNGS_PROGNOSE:") -> {
                fatigueForecast = line.substringAfter(":").trim()
            }
            line.startsWith("REST_ANPASSUNG:") -> {
                val adjustment = line.substringAfter(":").replace("Sekunden", "").trim()
                restAdjustment = adjustment.toIntOrNull() ?: 0
            }
            line.startsWith("VERTRAUEN:") -> {
                val conf = line.substringAfter(":").replace("%", "").trim().toIntOrNull() ?: 75
                confidence = conf / 100f
            }
        }
    }

    return com.example.fitapp.ai.PerformancePrediction(
        expectedPerformanceChange = expectedVolume / 100f, // Convert volume to performance change percentage
        plateauRisk =
            if (fatigueForecast == "high") {
                0.8f
            } else if (fatigueForecast == "medium") {
                0.5f
            } else {
                0.2f
            },
        injuryRisk =
            if (restAdjustment > 120) {
                0.7f
            } else if (restAdjustment > 60) {
                0.4f
            } else {
                0.1f
            },
        recommendedActions =
            listOf(
                "Adjust training volume to $expectedVolume%",
                "Target duration: $expectedDuration minutes",
                if (restAdjustment > 0) "Increase rest by $restAdjustment seconds" else "Maintain current rest intervals",
            ).filter { it.isNotEmpty() },
        confidence = confidence,
        timeframe = "1-2 weeks",
    )
}

private fun parseAlternatives(response: String): List<com.example.fitapp.ai.ProgressionAlternative> {
    val alternatives = mutableListOf<com.example.fitapp.ai.ProgressionAlternative>()
    val lines = response.lines()

    var inAlternativesSection = false

    for (line in lines) {
        when {
            line.startsWith("ALTERNATIVEN:") -> inAlternativesSection = true
            line.startsWith("- ") && inAlternativesSection -> {
                val description = line.substring(2).trim()
                alternatives.add(
                    com.example.fitapp.ai.ProgressionAlternative(
                        type = "alternative",
                        description = description,
                        weight = null,
                        reps = null,
                        difficulty = "same",
                    ),
                )
            }
            line.startsWith("SICHERHEIT:") -> inAlternativesSection = false
        }
    }

    return alternatives
}
