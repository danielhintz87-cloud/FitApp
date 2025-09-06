package com.example.fitapp.services

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import com.example.fitapp.domain.entities.*

/**
 * Integration test for bodyweight progression HIIT builder workflow
 */
class BodyweightProgressionIntegrationTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val workoutManager = WorkoutManager(context)
    private val bodyweightManager = BodyweightProgressionManager(context)

    @Test
    fun testBodyweightExerciseLoading() {
        val exercises = workoutManager.getDefaultBodyweightExercises()
        
        assertTrue("Should have default bodyweight exercises", exercises.isNotEmpty())
        assertTrue("Should have push exercises", exercises.any { it.category == BodyweightCategory.PUSH })
        assertTrue("Should have cardio exercises", exercises.any { it.category == BodyweightCategory.CARDIO })
        assertTrue("Should have core exercises", exercises.any { it.category == BodyweightCategory.CORE })
        
        // Verify specific exercises exist
        assertTrue("Should have push-ups", exercises.any { it.name == "Liegestütze" })
        assertTrue("Should have squats", exercises.any { it.name == "Kniebeugen" })
        assertTrue("Should have burpees", exercises.any { it.name == "Burpees" })
    }

    @Test
    fun testHIITWorkoutGeneration() {
        val defaultWorkouts = workoutManager.createDefaultHIITWorkouts()
        
        assertTrue("Should have default HIIT workouts", defaultWorkouts.isNotEmpty())
        
        val beginnerWorkout = defaultWorkouts.find { it.difficulty == HIITDifficulty.BEGINNER }
        assertNotNull("Should have beginner workout", beginnerWorkout)
        
        beginnerWorkout?.let { workout ->
            assertEquals("Beginner should have 20s work intervals", 20, workout.workInterval)
            assertEquals("Beginner should have 40s rest intervals", 40, workout.restInterval)
            assertEquals("Beginner should have 3 rounds", 3, workout.rounds)
            assertTrue("Should have exercises", workout.exercises.isNotEmpty())
            assertTrue("Should have reasonable duration", workout.totalDuration > 0)
        }
    }

    @Test
    fun testBodyweightProgressionLogic() = runBlocking {
        // Test progression for push-ups (rep-based)
        val pushUpProgression = bodyweightManager.calculateBodyweightProgression(
            exerciseId = "pushups",
            currentReps = 10,
            currentTime = null,
            currentDifficulty = 2,
            formScore = 0.8f,
            rpeScore = 6.0f,
            exerciseCategory = BodyweightCategory.PUSH
        )
        
        assertNotNull("Should return progression recommendation", pushUpProgression)
        assertEquals("Should recommend rep increase", ProgressionType.REP_INCREASE, pushUpProgression.type)
        assertTrue("Should increase reps", (pushUpProgression.repIncrease ?: 0) > 10)
        
        // Test progression for plank (time-based)
        val plankProgression = bodyweightManager.calculateBodyweightProgression(
            exerciseId = "plank",
            currentReps = null,
            currentTime = 30,
            currentDifficulty = 2,
            formScore = 0.8f,
            rpeScore = 6.0f,
            exerciseCategory = BodyweightCategory.CORE
        )
        
        assertNotNull("Should return plank progression", plankProgression)
        assertTrue("Should increase time", (plankProgression.repIncrease ?: 0) > 30)
    }

    @Test
    fun testHIITBuilderWorkflow() {
        val exercises = workoutManager.getDefaultBodyweightExercises()
        val selectedExercises = exercises.take(4) // Select first 4 exercises
        
        val builder = HIITBuilder(
            selectedExercises = selectedExercises,
            workInterval = 30,
            restInterval = 30,
            rounds = 4,
            difficulty = HIITDifficulty.INTERMEDIATE
        )
        
        val workout = builder.generateWorkout("Test HIIT Workout")
        
        assertEquals("Should have correct name", "Test HIIT Workout", workout.name)
        assertEquals("Should have 4 rounds", 4, workout.rounds)
        assertEquals("Should have 30s work interval", 30, workout.workInterval)
        assertEquals("Should have 30s rest interval", 30, workout.restInterval)
        assertEquals("Should have 4 exercises", 4, workout.exercises.size)
        assertEquals("Should have intermediate difficulty", HIITDifficulty.INTERMEDIATE, workout.difficulty)
        
        // Verify total duration calculation
        val expectedDuration = 4 * (4 * 30 + 4 * 30) // rounds * (exercises * work + exercises * rest)
        assertEquals("Should have correct duration", expectedDuration, workout.totalDuration)
    }

    @Test
    fun testProgressionFormScoreValidation() = runBlocking {
        // Test low form score - should maintain
        val lowFormProgression = bodyweightManager.calculateBodyweightProgression(
            exerciseId = "pushups",
            currentReps = 10,
            currentTime = null,
            currentDifficulty = 2,
            formScore = 0.5f, // Low form score
            rpeScore = 6.0f,
            exerciseCategory = BodyweightCategory.PUSH
        )
        
        assertEquals("Low form should maintain", ProgressionType.MAINTAIN, lowFormProgression.type)
        assertTrue("Should focus on form", lowFormProgression.description.contains("Form"))
        
        // Test good form score - should progress
        val goodFormProgression = bodyweightManager.calculateBodyweightProgression(
            exerciseId = "pushups",
            currentReps = 10,
            currentTime = null,
            currentDifficulty = 2,
            formScore = 0.8f, // Good form score
            rpeScore = 6.0f,
            exerciseCategory = BodyweightCategory.PUSH
        )
        
        assertEquals("Good form should progress", ProgressionType.REP_INCREASE, goodFormProgression.type)
    }

    @Test
    fun testExerciseCategoryClassification() {
        val exercises = workoutManager.getDefaultBodyweightExercises()
        
        val pushExercises = exercises.filter { it.category == BodyweightCategory.PUSH }
        val cardioExercises = exercises.filter { it.category == BodyweightCategory.CARDIO }
        val coreExercises = exercises.filter { it.category == BodyweightCategory.CORE }
        val squatExercises = exercises.filter { it.category == BodyweightCategory.SQUAT }
        
        assertTrue("Should have push exercises", pushExercises.isNotEmpty())
        assertTrue("Should have cardio exercises", cardioExercises.isNotEmpty())
        assertTrue("Should have core exercises", coreExercises.isNotEmpty())
        assertTrue("Should have squat exercises", squatExercises.isNotEmpty())
        
        // Verify exercise types
        assertTrue("Push-ups should be in PUSH category", 
            pushExercises.any { it.name == "Liegestütze" })
        assertTrue("Burpees should be in CARDIO category", 
            cardioExercises.any { it.name == "Burpees" })
        assertTrue("Plank should be in CORE category", 
            coreExercises.any { it.name == "Plank" })
        assertTrue("Squats should be in SQUAT category", 
            squatExercises.any { it.name == "Kniebeugen" })
    }
}