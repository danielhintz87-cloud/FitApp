package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.domain.entities.BodyweightCategory
import com.example.fitapp.domain.entities.BodyweightExercise
import com.example.fitapp.domain.entities.BodyweightProgression
import com.example.fitapp.domain.entities.HIITBuilder
import com.example.fitapp.domain.entities.HIITDifficulty
import com.example.fitapp.domain.entities.HIITWorkout
import com.example.fitapp.domain.entities.ProgressionRecommendation
import com.example.fitapp.domain.entities.ProgressionType
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bodyweight Exercise Progression Manager
 * Handles progression logic specifically for bodyweight exercises and HIIT workouts
 */
class BodyweightProgressionManager(private val context: Context) {
    companion object {
        private const val TAG = "BodyweightProgressionManager"
        private const val MIN_FORM_SCORE = 0.7f
        private const val MAX_PROGRESSION_WEEKS = 2
    }

    private val database = AppDatabase.get(context)

    /**
     * Calculate progression for bodyweight exercises
     */
    suspend fun calculateBodyweightProgression(
        exerciseId: String,
        currentReps: Int?,
        currentTime: Int?, // seconds
        currentDifficulty: Int,
        formScore: Float,
        rpeScore: Float,
        exerciseCategory: BodyweightCategory,
    ): ProgressionRecommendation =
        withContext(Dispatchers.IO) {
            try {
                require(formScore in 0f..1f) { "Form score must be between 0 and 1" }
                require(rpeScore in 1f..10f) { "RPE score must be between 1 and 10" }
                require(currentDifficulty in 1..5) { "Difficulty must be between 1 and 5" }

                val canProgress = formScore >= MIN_FORM_SCORE && rpeScore <= 8.0f
                val isEasyEffort = rpeScore <= 6.0f && formScore >= 0.8f

                when {
                    !canProgress ->
                        ProgressionRecommendation.maintain(
                            0f,
                            currentReps ?: 0,
                            1,
                            "Fokus auf Bewegungsqualität und Form",
                        )

                    isEasyEffort ->
                        calculateAggressiveBodyweightProgression(
                            currentReps,
                            currentTime,
                            currentDifficulty,
                            exerciseCategory,
                        )

                    else ->
                        calculateConservativeBodyweightProgression(
                            currentReps,
                            currentTime,
                            currentDifficulty,
                            exerciseCategory,
                        )
                }
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.AI,
                    TAG,
                    "Failed to calculate bodyweight progression for exercise: $exerciseId",
                    exception = e,
                )
                ProgressionRecommendation.maintain(
                    0f,
                    currentReps ?: 0,
                    1,
                    "Fehler bei Progressionsberechnung",
                )
            }
        }

    private fun calculateAggressiveBodyweightProgression(
        currentReps: Int?,
        currentTime: Int?,
        currentDifficulty: Int,
        category: BodyweightCategory,
    ): ProgressionRecommendation {
        return when (category) {
            BodyweightCategory.CARDIO, BodyweightCategory.CORE -> {
                // Time-based progression for cardio and core
                if (currentTime != null && currentTime < 60) {
                    ProgressionRecommendation(
                        type = ProgressionType.REP_INCREASE,
                        weightIncrease = null,
                        repIncrease = currentTime + 10,
                        description = "Zeit um 10 Sekunden erhöhen - ${currentTime + 10}s",
                        confidence = 0.8f,
                        nextEvaluationWeeks = 1,
                    )
                } else {
                    increaseDifficulty(currentDifficulty)
                }
            }
            else -> {
                // Rep-based progression for strength exercises
                if (currentReps != null && currentReps < 20) {
                    ProgressionRecommendation(
                        type = ProgressionType.REP_INCREASE,
                        weightIncrease = null,
                        repIncrease = currentReps + 2,
                        description = "Wiederholungen erhöhen auf ${currentReps + 2}",
                        confidence = 0.8f,
                        nextEvaluationWeeks = 1,
                    )
                } else {
                    increaseDifficulty(currentDifficulty)
                }
            }
        }
    }

    private fun calculateConservativeBodyweightProgression(
        currentReps: Int?,
        currentTime: Int?,
        currentDifficulty: Int,
        category: BodyweightCategory,
    ): ProgressionRecommendation {
        return when (category) {
            BodyweightCategory.CARDIO, BodyweightCategory.CORE -> {
                if (currentTime != null && currentTime < 45) {
                    ProgressionRecommendation(
                        type = ProgressionType.REP_INCREASE,
                        weightIncrease = null,
                        repIncrease = currentTime + 5,
                        description = "Zeit um 5 Sekunden erhöhen - ${currentTime + 5}s",
                        confidence = 0.7f,
                        nextEvaluationWeeks = 2,
                    )
                } else {
                    ProgressionRecommendation.maintain(
                        0f,
                        currentReps ?: 0,
                        1,
                        "Aktuelle Zeit beibehalten, Fokus auf Form",
                    )
                }
            }
            else -> {
                if (currentReps != null && currentReps < 15) {
                    ProgressionRecommendation(
                        type = ProgressionType.REP_INCREASE,
                        weightIncrease = null,
                        repIncrease = currentReps + 1,
                        description = "Wiederholungen erhöhen auf ${currentReps + 1}",
                        confidence = 0.7f,
                        nextEvaluationWeeks = 2,
                    )
                } else {
                    ProgressionRecommendation.maintain(
                        0f,
                        currentReps ?: 0,
                        1,
                        "Aktuelle Wiederholungen beibehalten",
                    )
                }
            }
        }
    }

    private fun increaseDifficulty(currentDifficulty: Int): ProgressionRecommendation {
        return if (currentDifficulty < 5) {
            ProgressionRecommendation(
                type = ProgressionType.REP_INCREASE,
                weightIncrease = null,
                repIncrease = currentDifficulty + 1,
                description = "Schwierigkeitsgrad erhöhen - Level ${currentDifficulty + 1}",
                confidence = 0.9f,
                nextEvaluationWeeks = 2,
            )
        } else {
            ProgressionRecommendation.maintain(
                0f,
                0,
                1,
                "Maximale Schwierigkeit erreicht - Fokus auf Perfektion",
            )
        }
    }

    /**
     * Get predefined bodyweight exercises
     */
    fun getDefaultBodyweightExercises(): List<BodyweightExercise> {
        return listOf(
            // Push exercises
            BodyweightExercise(
                name = "Liegestütze",
                category = BodyweightCategory.PUSH,
                difficultyLevel = 2,
                baseReps = 10,
                description = "Klassische Liegestütze",
                instructions =
                    listOf(
                        "Plank-Position einnehmen",
                        "Körper gerade halten",
                        "Kontrolliert absenken und hochdrücken",
                    ),
                progressionOptions =
                    listOf(
                        BodyweightProgression(
                            ProgressionType.REP_INCREASE,
                            "2-3 Wiederholungen",
                            "Mehr Wiederholungen",
                        ),
                        BodyweightProgression(
                            ProgressionType.DIFFICULTY_INCREASE,
                            "Nächste Schwierigkeit",
                            "Diamant-Liegestütze",
                            1,
                        ),
                    ),
            ),
            // Squat exercises
            BodyweightExercise(
                name = "Kniebeugen",
                category = BodyweightCategory.SQUAT,
                difficultyLevel = 1,
                baseReps = 15,
                description = "Grundlegende Kniebeugen",
                instructions =
                    listOf(
                        "Füße schulterbreit aufstellen",
                        "Knie über die Zehen beugen",
                        "Hüfte nach hinten schieben",
                    ),
            ),
            // Core exercises
            BodyweightExercise(
                name = "Plank",
                category = BodyweightCategory.CORE,
                difficultyLevel = 2,
                baseTime = 30,
                description = "Unterarmstütz",
                instructions =
                    listOf(
                        "Unterarmstütz-Position",
                        "Körper gerade halten",
                        "Bauch anspannen",
                    ),
            ),
            // Cardio exercises
            BodyweightExercise(
                name = "Burpees",
                category = BodyweightCategory.CARDIO,
                difficultyLevel = 3,
                baseReps = 8,
                description = "Ganzkörper-Cardio-Übung",
                instructions =
                    listOf(
                        "Kniebeuge in Plank",
                        "Liegestütz ausführen",
                        "Aufspringen mit Armen nach oben",
                    ),
            ),
            BodyweightExercise(
                name = "Mountain Climbers",
                category = BodyweightCategory.CARDIO,
                difficultyLevel = 2,
                baseTime = 20,
                description = "Bergsteiger in Plank-Position",
                instructions =
                    listOf(
                        "Plank-Position einnehmen",
                        "Knie abwechselnd zur Brust ziehen",
                        "Schnelle, kontrollierte Bewegung",
                    ),
            ),
            BodyweightExercise(
                name = "Jumping Jacks",
                category = BodyweightCategory.CARDIO,
                difficultyLevel = 1,
                baseReps = 20,
                description = "Hampelmann",
                instructions =
                    listOf(
                        "Aufrecht stehen",
                        "Sprung mit Beinen spreizen",
                        "Arme über Kopf schwingen",
                    ),
            ),
        )
    }

    /**
     * Create HIIT workout suggestions based on difficulty level
     */
    fun createDefaultHIITWorkouts(): List<HIITWorkout> {
        val exercises = getDefaultBodyweightExercises()

        return listOf(
            // Beginner HIIT
            HIITBuilder(
                selectedExercises =
                    listOf(
                        exercises.first { it.name == "Kniebeugen" },
                        exercises.first { it.name == "Liegestütze" },
                        exercises.first { it.name == "Jumping Jacks" },
                        exercises.first { it.name == "Plank" },
                    ),
                workInterval = 20,
                restInterval = 40,
                rounds = 3,
                difficulty = HIITDifficulty.BEGINNER,
            ).generateWorkout("Beginner HIIT Circuit"),
            // Intermediate HIIT
            HIITBuilder(
                selectedExercises =
                    listOf(
                        exercises.first { it.name == "Burpees" },
                        exercises.first { it.name == "Mountain Climbers" },
                        exercises.first { it.name == "Liegestütze" },
                        exercises.first { it.name == "Kniebeugen" },
                    ),
                workInterval = 30,
                restInterval = 30,
                rounds = 4,
                difficulty = HIITDifficulty.INTERMEDIATE,
            ).generateWorkout("Intermediate HIIT Blast"),
        )
    }
}
