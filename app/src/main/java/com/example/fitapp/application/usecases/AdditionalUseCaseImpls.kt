package com.example.fitapp.application.usecases

import com.example.fitapp.domain.entities.*
import com.example.fitapp.domain.repositories.AiProviderRepository
import com.example.fitapp.domain.usecases.*

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
            provider = AiProvider.Gemini, // Temporarily use Gemini instead of Perplexity
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
            provider = AiProvider.Gemini,
            taskType = TaskType.TRAINING_PLAN
        )
        
        return repository.generateText(aiRequest).mapCatching { response ->
            response
        }
    }
}

/**
 * Implementation of personalized workout generation use case
 */
class GeneratePersonalizedWorkoutUseCaseImpl(
    private val repository: AiProviderRepository
) : GeneratePersonalizedWorkoutUseCase {
    
    override suspend fun execute(request: AIPersonalTrainerRequest): Result<WorkoutPlan> {
        val equipmentString = if (request.equipment.isEmpty()) "Nur Körpergewicht" else request.equipment.joinToString(", ")
        val goalsString = request.goals.joinToString(", ")
        
        val prompt = """
Erstelle einen personalisierten Trainingsplan in strukturiertem Format für:

**Benutzer-Profil:**
- Alter: ${request.userProfile.age} Jahre
- Geschlecht: ${request.userProfile.gender}
- Größe: ${request.userProfile.height}cm
- Aktuelles Gewicht: ${request.userProfile.currentWeight}kg
- Zielgewicht: ${request.userProfile.targetWeight}kg
- Aktivitätslevel: ${request.userProfile.activityLevel}

**Fitness-Level:**
- Krafttraining: ${request.fitnessLevel.strength}
- Ausdauer: ${request.fitnessLevel.cardio}
- Flexibilität: ${request.fitnessLevel.flexibility}
- Erfahrung: ${request.fitnessLevel.experience}

**Trainingsziele:** $goalsString
**Verfügbare Zeit:** ${request.availableTime} Minuten
**Verfügbare Geräte:** $equipmentString

**Ausgabeformat:**
TITEL: [Workout-Name]
BESCHREIBUNG: [Kurze Beschreibung des Trainings]
DAUER: [Geschätzte Minuten]
SCHWIERIGKEIT: [Anfänger/Fortgeschritten/Experte]

ÜBUNGEN:
1. [Übungsname] - [Sets] Sätze à [Reps] Wiederholungen - [Pausenzeit] - [Anleitung]
2. [Übungsname] - [Sets] Sätze à [Reps] Wiederholungen - [Pausenzeit] - [Anleitung]
[...]

Erstelle 5-8 Übungen, die auf das Fitness-Level und die Ziele abgestimmt sind.
        """.trimIndent()
        
        val aiRequest = AiRequest(
            prompt = prompt,
            provider = AiProvider.Gemini,
            taskType = TaskType.WORKOUT_GENERATION
        )
        
        return repository.generateText(aiRequest).mapCatching { response ->
            parseWorkoutPlan(response)
        }
    }
    
    private fun parseWorkoutPlan(response: String): WorkoutPlan {
        val lines = response.lines()
        var title = "Personalisiertes Workout"
        var description = "KI-generiertes personalisiertes Training"
        var estimatedDuration = 45
        var difficulty = "Mittel"
        val exercises = mutableListOf<Exercise>()
        
        for (line in lines) {
            when {
                line.startsWith("TITEL:") -> title = line.substringAfter("TITEL:").trim()
                line.startsWith("BESCHREIBUNG:") -> description = line.substringAfter("BESCHREIBUNG:").trim()
                line.startsWith("DAUER:") -> {
                    val durationStr = line.substringAfter("DAUER:").trim()
                    estimatedDuration = Regex("\\d+").find(durationStr)?.value?.toIntOrNull() ?: 45
                }
                line.startsWith("SCHWIERIGKEIT:") -> difficulty = line.substringAfter("SCHWIERIGKEIT:").trim()
                line.matches(Regex("\\d+\\..*")) -> {
                    val exercise = parseExercise(line)
                    if (exercise != null) exercises.add(exercise)
                }
            }
        }
        
        return WorkoutPlan(
            title = title,
            description = description,
            exercises = exercises,
            estimatedDuration = estimatedDuration,
            difficulty = difficulty,
            equipment = emptyList() // Will be filled from original request
        )
    }
    
    private fun parseExercise(line: String): Exercise? {
        return try {
            val parts = line.split(" - ")
            if (parts.size >= 3) {
                val nameAndSets = parts[0].substringAfter(". ").trim()
                val setsInfo = parts[1].trim()
                val restTime = parts.getOrNull(2)?.trim() ?: "60 Sekunden"
                val instructions = parts.getOrNull(3)?.trim() ?: "Standard Ausführung"
                
                // Extract sets and reps from format like "3 Sätze à 15 Wiederholungen"
                val setsMatch = Regex("(\\d+)\\s*Sätze").find(setsInfo)
                val repsMatch = Regex("(\\d+)\\s*Wiederholungen?").find(setsInfo) 
                    ?: Regex("(\\d+\\s*Sekunden?)").find(setsInfo)
                
                val sets = setsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 3
                val reps = repsMatch?.groupValues?.get(1) ?: "10"
                
                Exercise(
                    name = nameAndSets,
                    sets = sets,
                    reps = reps,
                    restTime = restTime,
                    instructions = instructions
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Implementation of nutrition advice generation use case
 */
class GenerateNutritionAdviceUseCaseImpl(
    private val repository: AiProviderRepository
) : GenerateNutritionAdviceUseCase {
    
    override suspend fun execute(userProfile: UserProfile, goals: List<String>): Result<PersonalizedMealPlan> {
        val goalsString = goals.joinToString(", ")
        
        val prompt = """
Erstelle einen personalisierten Ernährungsplan für:

**Benutzer-Profil:**
- Alter: ${userProfile.age} Jahre
- Geschlecht: ${userProfile.gender}
- Größe: ${userProfile.height}cm
- Aktuelles Gewicht: ${userProfile.currentWeight}kg
- Zielgewicht: ${userProfile.targetWeight}kg
- Aktivitätslevel: ${userProfile.activityLevel}

**Ziele:** $goalsString

**Ausgabeformat:**
TITEL: [Ernährungsplan-Name]
TÄGLICHE_KALORIEN: [Zahl]
PROTEIN: [Gramm]
KOHLENHYDRATE: [Gramm]
FETT: [Gramm]

MAHLZEITEN:
FRÜHSTÜCK: [Name] - [Kalorien] kcal - [Beschreibung]
MITTAGESSEN: [Name] - [Kalorien] kcal - [Beschreibung]
ABENDESSEN: [Name] - [Kalorien] kcal - [Beschreibung]
SNACK: [Name] - [Kalorien] kcal - [Beschreibung]

Berechne die Kalorien basierend auf dem Grundumsatz und Aktivitätslevel.
        """.trimIndent()
        
        val aiRequest = AiRequest(
            prompt = prompt,
            provider = AiProvider.Gemini,
            taskType = TaskType.NUTRITION_ADVICE
        )
        
        return repository.generateText(aiRequest).mapCatching { response ->
            parseMealPlan(response)
        }
    }
    
    private fun parseMealPlan(response: String): PersonalizedMealPlan {
        val lines = response.lines()
        var title = "Personalisierter Ernährungsplan"
        var dailyCalories = 2000
        var protein = 150
        var carbs = 200
        var fat = 80
        val meals = mutableListOf<MealPlan>()
        
        for (line in lines) {
            when {
                line.startsWith("TITEL:") -> title = line.substringAfter("TITEL:").trim()
                line.startsWith("TÄGLICHE_KALORIEN:") -> {
                    val caloriesStr = line.substringAfter("TÄGLICHE_KALORIEN:").trim()
                    dailyCalories = Regex("\\d+").find(caloriesStr)?.value?.toIntOrNull() ?: 2000
                }
                line.startsWith("PROTEIN:") -> {
                    val proteinStr = line.substringAfter("PROTEIN:").trim()
                    protein = Regex("\\d+").find(proteinStr)?.value?.toIntOrNull() ?: 150
                }
                line.startsWith("KOHLENHYDRATE:") -> {
                    val carbsStr = line.substringAfter("KOHLENHYDRATE:").trim()
                    carbs = Regex("\\d+").find(carbsStr)?.value?.toIntOrNull() ?: 200
                }
                line.startsWith("FETT:") -> {
                    val fatStr = line.substringAfter("FETT:").trim()
                    fat = Regex("\\d+").find(fatStr)?.value?.toIntOrNull() ?: 80
                }
                line.startsWith("FRÜHSTÜCK:") -> {
                    val meal = parseMeal(line.substringAfter("FRÜHSTÜCK:"), "breakfast")
                    if (meal != null) meals.add(meal)
                }
                line.startsWith("MITTAGESSEN:") -> {
                    val meal = parseMeal(line.substringAfter("MITTAGESSEN:"), "lunch")
                    if (meal != null) meals.add(meal)
                }
                line.startsWith("ABENDESSEN:") -> {
                    val meal = parseMeal(line.substringAfter("ABENDESSEN:"), "dinner")
                    if (meal != null) meals.add(meal)
                }
                line.startsWith("SNACK:") -> {
                    val meal = parseMeal(line.substringAfter("SNACK:"), "snack")
                    if (meal != null) meals.add(meal)
                }
            }
        }
        
        return PersonalizedMealPlan(
            title = title,
            dailyCalories = dailyCalories,
            macroTargets = MacroTargets(protein, carbs, fat),
            meals = meals
        )
    }
    
    private fun parseMeal(mealText: String, type: String): MealPlan? {
        return try {
            val parts = mealText.split(" - ")
            if (parts.size >= 3) {
                val name = parts[0].trim()
                val caloriesText = parts[1].trim()
                val description = parts[2].trim()
                
                val calories = Regex("(\\d+)\\s*kcal").find(caloriesText)?.groupValues?.get(1)?.toIntOrNull() ?: 500
                
                MealPlan(
                    type = type,
                    name = name,
                    calories = calories,
                    description = description
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Implementation of progress analysis use case
 */
class AnalyzeProgressUseCaseImpl(
    private val repository: AiProviderRepository
) : AnalyzeProgressUseCase {
    
    override suspend fun execute(progressData: List<WeightEntry>, userProfile: UserProfile): Result<ProgressAnalysis> {
        val progressString = progressData.take(30).joinToString("\n") { "${it.date}: ${it.weight}kg" }
        val currentWeight = progressData.firstOrNull()?.weight ?: userProfile.currentWeight
        val weightChange = currentWeight - (progressData.lastOrNull()?.weight ?: currentWeight)
        
        val prompt = """
Analysiere die Gewichtsentwicklung eines Benutzers:

**Benutzer-Profil:**
- Zielgewicht: ${userProfile.targetWeight}kg
- Aktuelles Gewicht: ${currentWeight}kg

**Gewichtsverlauf (neueste zuerst):**
$progressString

**Gewichtsveränderung:** ${String.format("%.1f", weightChange)}kg

**Ausgabeformat:**
TREND: [AUFWÄRTS/ABWÄRTS/STABIL]
ADHERENCE_SCORE: [0-100]
INSIGHTS:
- [Einsicht 1]
- [Einsicht 2]
- [Einsicht 3]
RECOMMENDATIONS:
- [Empfehlung 1]
- [Empfehlung 2]
- [Empfehlung 3]

Analysiere den Trend und gib realistische Einschätzungen und Empfehlungen.
        """.trimIndent()
        
        val aiRequest = AiRequest(
            prompt = prompt,
            provider = AiProvider.Gemini,
            taskType = TaskType.PROGRESS_ANALYSIS
        )
        
        return repository.generateText(aiRequest).mapCatching { response ->
            parseProgressAnalysis(response)
        }
    }
    
    private fun parseProgressAnalysis(response: String): ProgressAnalysis {
        val lines = response.lines()
        var weightTrend = "STABIL"
        var adherenceScore = 50f
        val insights = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        var currentSection = ""
        
        for (line in lines) {
            when {
                line.startsWith("TREND:") -> weightTrend = line.substringAfter("TREND:").trim()
                line.startsWith("ADHERENCE_SCORE:") -> {
                    val scoreStr = line.substringAfter("ADHERENCE_SCORE:").trim()
                    adherenceScore = Regex("\\d+").find(scoreStr)?.value?.toFloatOrNull() ?: 50f
                }
                line.startsWith("INSIGHTS:") -> currentSection = "insights"
                line.startsWith("RECOMMENDATIONS:") -> currentSection = "recommendations"
                line.startsWith("- ") -> {
                    val item = line.substringAfter("- ").trim()
                    when (currentSection) {
                        "insights" -> insights.add(item)
                        "recommendations" -> recommendations.add(item)
                    }
                }
            }
        }
        
        return ProgressAnalysis(
            weightTrend = weightTrend,
            adherenceScore = adherenceScore / 100f,
            insights = insights,
            recommendations = recommendations
        )
    }
}

/**
 * Implementation of motivation generation use case
 */
class GenerateMotivationUseCaseImpl(
    private val repository: AiProviderRepository
) : GenerateMotivationUseCase {
    
    override suspend fun execute(userProfile: UserProfile, progressData: List<WeightEntry>): Result<MotivationalMessage> {
        val recentProgress = progressData.take(7)
        val weightChange = if (recentProgress.size >= 2) {
            recentProgress.first().weight - recentProgress.last().weight
        } else 0f
        
        val prompt = """
Erstelle eine motivierende Nachricht für einen Fitness-App Benutzer:

**Benutzer-Info:**
- Zielgewicht: ${userProfile.targetWeight}kg
- Aktuelles Gewicht: ${userProfile.currentWeight}kg
- Gewichtsveränderung letzte Woche: ${String.format("%.1f", weightChange)}kg

**Ausgabeformat:**
TITEL: [Motivierender Titel]
NACHRICHT: [Motivierende Nachricht, 2-3 Sätze]
TYP: [encouragement/challenge/tip]
AKTION: [Vorgeschlagene konkrete Aktion]

Erstelle eine positive, ermutigende Nachricht basierend auf dem Fortschritt.
        """.trimIndent()
        
        val aiRequest = AiRequest(
            prompt = prompt,
            provider = AiProvider.Gemini,
            taskType = TaskType.MOTIVATIONAL_COACHING
        )
        
        return repository.generateText(aiRequest).mapCatching { response ->
            parseMotivationalMessage(response)
        }
    }
    
    private fun parseMotivationalMessage(response: String): MotivationalMessage {
        val lines = response.lines()
        var title = "Bleib dran!"
        var message = "Du machst großartige Fortschritte. Weiter so!"
        var type = "encouragement"
        var actionSuggestion: String? = null
        
        for (line in lines) {
            when {
                line.startsWith("TITEL:") -> title = line.substringAfter("TITEL:").trim()
                line.startsWith("NACHRICHT:") -> message = line.substringAfter("NACHRICHT:").trim()
                line.startsWith("TYP:") -> type = line.substringAfter("TYP:").trim()
                line.startsWith("AKTION:") -> actionSuggestion = line.substringAfter("AKTION:").trim()
            }
        }
        
        return MotivationalMessage(
            title = title,
            message = message,
            type = type,
            actionSuggestion = actionSuggestion
        )
    }
}

/**
 * Implementation of comprehensive personalized recommendations use case
 */
class GetPersonalizedRecommendationsUseCaseImpl(
    private val workoutUseCase: GeneratePersonalizedWorkoutUseCase,
    private val nutritionUseCase: GenerateNutritionAdviceUseCase,
    private val progressUseCase: AnalyzeProgressUseCase,
    private val motivationUseCase: GenerateMotivationUseCase
) : GetPersonalizedRecommendationsUseCase {
    
    override suspend fun execute(userContext: UserContext): Result<AIPersonalTrainerResponse> {
        return try {
            val workoutRequest = AIPersonalTrainerRequest(
                userProfile = userContext.profile,
                fitnessLevel = userContext.fitnessLevel,
                availableTime = 45,
                equipment = userContext.availableEquipment,
                goals = userContext.currentGoals
            )
            
            val workoutResult = workoutUseCase.execute(workoutRequest)
            val nutritionResult = nutritionUseCase.execute(userContext.profile, userContext.currentGoals)
            val progressResult = progressUseCase.execute(userContext.recentProgress, userContext.profile)
            val motivationResult = motivationUseCase.execute(userContext.profile, userContext.recentProgress)
            
            val recommendations = generateBasicRecommendations(userContext)
            
            Result.success(AIPersonalTrainerResponse(
                workoutPlan = workoutResult.getOrNull(),
                mealPlan = nutritionResult.getOrNull(),
                progressAnalysis = progressResult.getOrNull(),
                motivation = motivationResult.getOrNull(),
                recommendations = recommendations
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateBasicRecommendations(userContext: UserContext): List<AIRecommendation> {
        val recommendations = mutableListOf<AIRecommendation>()
        
        // Water intake recommendation
        recommendations.add(AIRecommendation(
            title = "Hydration",
            description = "Trinke mindestens 2-3 Liter Wasser täglich",
            type = "habit",
            priority = "high",
            actionRequired = false
        ))
        
        // Sleep recommendation
        recommendations.add(AIRecommendation(
            title = "Schlafqualität",
            description = "Versuche 7-9 Stunden qualitätsvollen Schlaf zu bekommen",
            type = "habit",
            priority = "high",
            actionRequired = false
        ))
        
        // Progress tracking
        if (userContext.recentProgress.size < 7) {
            recommendations.add(AIRecommendation(
                title = "Gewichtstracking",
                description = "Wiege dich regelmäßig zur gleichen Tageszeit",
                type = "habit",
                priority = "medium",
                actionRequired = true
            ))
        }
        
        return recommendations
    }
}