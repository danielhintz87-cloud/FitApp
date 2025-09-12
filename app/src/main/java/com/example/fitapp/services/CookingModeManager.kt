package com.example.fitapp.services

import com.example.fitapp.data.db.*
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Comprehensive Cooking Mode Manager
 * Handles recipe flow, step navigation, timers, and cooking experience
 */
class CookingModeManager(
    private val database: AppDatabase,
) {
    companion object {
        private const val TAG = "CookingModeManager"
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    // Flow states for reactive UI
    private val _cookingFlow = MutableStateFlow<CookingFlow?>(null)
    val cookingFlow: StateFlow<CookingFlow?> = _cookingFlow.asStateFlow()

    private val _currentStep = MutableStateFlow<CookingStep?>(null)
    val currentStep: StateFlow<CookingStep?> = _currentStep.asStateFlow()

    private val _isInCookingMode = MutableStateFlow(false)
    val isInCookingMode: StateFlow<Boolean> = _isInCookingMode.asStateFlow()

    private val _stepTimers = MutableStateFlow<Map<Int, StepTimer>>(emptyMap())
    val stepTimers: StateFlow<Map<Int, StepTimer>> = _stepTimers.asStateFlow()

    data class CookingStep(
        val stepNumber: Int,
        val instruction: String,
        val duration: Int? = null, // Timer für zeitbasierte Schritte in Sekunden
        val ingredients: List<Ingredient> = emptyList(), // Für diesen Schritt
        val temperature: String? = null,
        val tips: List<String> = emptyList(),
        val image: String? = null,
        val estimatedTime: Int? = null, // Geschätzte Zeit für den Schritt
        val isCompleted: Boolean = false,
    )

    data class Ingredient(
        val name: String,
        val quantity: String,
        val unit: String,
        val isOptional: Boolean = false,
    )

    data class CookingFlow(
        val sessionId: String,
        val recipeId: String,
        val recipeTitle: String,
        val steps: List<CookingStep>,
        val currentStepIndex: Int = 0,
        val startTime: Long,
        val estimatedTotalTime: Int? = null,
        val servings: Int = 1,
        val difficulty: String? = null,
        val status: String = "active", // "active", "paused", "completed", "cancelled"
    )

    data class StepTimer(
        val stepIndex: Int,
        val name: String,
        val totalDuration: Int, // in seconds
        val remainingTime: Int,
        val isActive: Boolean = false,
        val isPaused: Boolean = false,
        val isCompleted: Boolean = false,
    )

    /**
     * Start a new cooking flow with comprehensive step management
     */
    suspend fun startCookingMode(recipe: SavedRecipeEntity): CookingFlow {
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis() / 1000

        // Parse recipe into cooking steps
        val cookingSteps = parseRecipeSteps(recipe)

        // Create cooking session in database
        val session =
            CookingSessionEntity(
                id = sessionId,
                recipeId = recipe.id,
                startTime = startTime,
                totalSteps = cookingSteps.size,
                estimatedDuration = calculateEstimatedDuration(cookingSteps).toLong(),
            )

        database.cookingSessionDao().insert(session)

        val flow =
            CookingFlow(
                sessionId = sessionId,
                recipeId = recipe.id,
                recipeTitle = recipe.title,
                steps = cookingSteps,
                startTime = startTime,
                estimatedTotalTime = calculateEstimatedDuration(cookingSteps),
                servings = recipe.servings ?: 1,
                difficulty = recipe.difficulty,
            )

        _cookingFlow.value = flow
        _isInCookingMode.value = true
        _currentStep.value = cookingSteps.firstOrNull()

        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Started cooking mode for recipe ${recipe.title} with ${cookingSteps.size} steps",
        )

        return flow
    }

    /**
     * Navigate to the next step in the cooking process
     */
    fun navigateToNextStep(): CookingStep? {
        val currentFlow = _cookingFlow.value ?: return null
        val nextIndex = currentFlow.currentStepIndex + 1

        if (nextIndex < currentFlow.steps.size) {
            val nextStep = currentFlow.steps[nextIndex]

            // Mark current step as completed
            val updatedSteps =
                currentFlow.steps.mapIndexed { index, step ->
                    if (index == currentFlow.currentStepIndex) {
                        step.copy(isCompleted = true)
                    } else {
                        step
                    }
                }

            val updatedFlow =
                currentFlow.copy(
                    currentStepIndex = nextIndex,
                    steps = updatedSteps,
                )

            _cookingFlow.value = updatedFlow
            _currentStep.value = nextStep

            // Update database
            scope.launch {
                database.cookingSessionDao().updateCurrentStep(currentFlow.sessionId, nextIndex)
            }

            return nextStep
        }

        return null // Cooking completed
    }

    /**
     * Navigate to the previous step
     */
    fun navigateToPreviousStep(): CookingStep? {
        val currentFlow = _cookingFlow.value ?: return null
        val prevIndex = currentFlow.currentStepIndex - 1

        if (prevIndex >= 0) {
            val prevStep = currentFlow.steps[prevIndex]

            val updatedFlow = currentFlow.copy(currentStepIndex = prevIndex)
            _cookingFlow.value = updatedFlow
            _currentStep.value = prevStep

            // Update database
            scope.launch {
                database.cookingSessionDao().updateCurrentStep(currentFlow.sessionId, prevIndex)
            }

            return prevStep
        }

        return null
    }

    /**
     * Start a timer for a specific step
     */
    suspend fun startStepTimer(
        stepIndex: Int,
        duration: Int,
        name: String = "Timer",
    ) {
        val currentFlow = _cookingFlow.value ?: return

        val timer =
            StepTimer(
                stepIndex = stepIndex,
                name = name,
                totalDuration = duration,
                remainingTime = duration,
                isActive = true,
            )

        // Save timer to database
        val timerEntity =
            CookingTimerEntity(
                sessionId = currentFlow.sessionId,
                stepIndex = stepIndex,
                name = name,
                durationSeconds = duration.toLong(),
                remainingSeconds = duration.toLong(),
                isActive = true,
                startTime = System.currentTimeMillis() / 1000,
            )

        database.cookingTimerDao().insert(timerEntity)

        // Update local state
        val currentTimers = _stepTimers.value.toMutableMap()
        currentTimers[stepIndex] = timer
        _stepTimers.value = currentTimers

        // Start countdown
        startTimerCountdown(timerEntity.id, duration)

        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Started timer '$name' for step $stepIndex: ${duration}s",
        )
    }

    /**
     * Pause/Resume a step timer
     */
    suspend fun toggleStepTimer(stepIndex: Int) {
        val currentTimers = _stepTimers.value
        val timer = currentTimers[stepIndex] ?: return

        val updatedTimer =
            timer.copy(
                isPaused = !timer.isPaused,
                isActive = !timer.isPaused,
            )

        val updatedTimers = currentTimers.toMutableMap()
        updatedTimers[stepIndex] = updatedTimer
        _stepTimers.value = updatedTimers

        // Update database
        database.cookingTimerDao().updatePauseState(
            stepIndex = stepIndex,
            isPaused = updatedTimer.isPaused,
        )
    }

    /**
     * Mark the current step as complete
     */
    suspend fun markStepComplete() {
        val currentFlow = _cookingFlow.value ?: return
        val currentStepIndex = currentFlow.currentStepIndex

        // Mark step as completed
        val updatedSteps =
            currentFlow.steps.mapIndexed { index, step ->
                if (index == currentStepIndex) {
                    step.copy(isCompleted = true)
                } else {
                    step
                }
            }

        val updatedFlow = currentFlow.copy(steps = updatedSteps)
        _cookingFlow.value = updatedFlow

        // Stop any active timers for this step
        val currentTimers = _stepTimers.value.toMutableMap()
        currentTimers[currentStepIndex]?.let { timer ->
            currentTimers[currentStepIndex] =
                timer.copy(
                    isActive = false,
                    isCompleted = true,
                )
            _stepTimers.value = currentTimers
        }
    }

    /**
     * Pause the cooking session
     */
    suspend fun pauseCooking() {
        val currentFlow = _cookingFlow.value ?: return

        val updatedFlow = currentFlow.copy(status = "paused")
        _cookingFlow.value = updatedFlow

        // Pause all active timers
        val currentTimers =
            _stepTimers.value.mapValues { (_, timer) ->
                if (timer.isActive) {
                    timer.copy(isPaused = true, isActive = false)
                } else {
                    timer
                }
            }
        _stepTimers.value = currentTimers

        // Update database
        database.cookingSessionDao().updateStatus(currentFlow.sessionId, "paused")
    }

    /**
     * Resume the cooking session
     */
    suspend fun resumeCooking() {
        val currentFlow = _cookingFlow.value ?: return

        val updatedFlow = currentFlow.copy(status = "active")
        _cookingFlow.value = updatedFlow

        // Resume paused timers
        val currentTimers =
            _stepTimers.value.mapValues { (_, timer) ->
                if (timer.isPaused) {
                    timer.copy(isPaused = false, isActive = true)
                } else {
                    timer
                }
            }
        _stepTimers.value = currentTimers

        // Update database
        database.cookingSessionDao().updateStatus(currentFlow.sessionId, "active")
    }

    /**
     * Finish the cooking session
     */
    suspend fun finishCooking() {
        val currentFlow = _cookingFlow.value ?: return
        val endTime = System.currentTimeMillis() / 1000

        // Update session in database
        database.cookingSessionDao().completeCookingSession(
            sessionId = currentFlow.sessionId,
            endTime = endTime,
            actualDuration = endTime - currentFlow.startTime,
        )

        // Update recipe last cooked timestamp
        database.savedRecipeDao().markAsCooked(currentFlow.recipeId, endTime)

        // Reset state
        _cookingFlow.value = null
        _currentStep.value = null
        _isInCookingMode.value = false
        _stepTimers.value = emptyMap()

        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Finished cooking session ${currentFlow.sessionId} for recipe ${currentFlow.recipeTitle}",
        )
    }

    // Private helper methods

    private fun parseRecipeSteps(recipe: SavedRecipeEntity): List<CookingStep> {
        // Parse markdown content into structured cooking steps
        val steps = mutableListOf<CookingStep>()
        val lines = recipe.markdown.split("\n")
        var currentStepNumber = 1
        var currentInstruction = ""
        var currentIngredients = mutableListOf<Ingredient>()
        var currentTips = mutableListOf<String>()

        for (line in lines) {
            val trimmedLine = line.trim()

            when {
                // Step header (e.g., "## Schritt 1:" or "1.")
                trimmedLine.matches(Regex("^#{1,3}\\s*[Ss]chritt\\s+(\\d+).*")) ||
                    trimmedLine.matches(Regex("^(\\d+)\\s*\\.\\s*.*")) -> {
                    // Save previous step if exists
                    if (currentInstruction.isNotEmpty()) {
                        steps.add(
                            createCookingStep(
                                currentStepNumber - 1,
                                currentInstruction,
                                currentIngredients.toList(),
                                currentTips.toList(),
                            ),
                        )
                    }

                    // Start new step
                    currentInstruction =
                        trimmedLine.replace(Regex("^#{1,3}\\s*[Ss]chritt\\s+\\d+:?\\s*"), "")
                            .replace(Regex("^\\d+\\.\\s*"), "")
                    currentIngredients.clear()
                    currentTips.clear()
                    currentStepNumber++
                }

                // Ingredient line (starts with - or *)
                trimmedLine.startsWith("-") || trimmedLine.startsWith("*") -> {
                    val ingredientText = trimmedLine.substring(1).trim()
                    val ingredient = parseIngredient(ingredientText)
                    if (ingredient != null) {
                        currentIngredients.add(ingredient)
                    }
                }

                // Tip line (starts with > or contains "Tipp:")
                trimmedLine.startsWith(">") || trimmedLine.contains("Tipp:", ignoreCase = true) -> {
                    val tip = trimmedLine.replace(">", "").replace("Tipp:", "").trim()
                    if (tip.isNotEmpty()) {
                        currentTips.add(tip)
                    }
                }

                // Regular instruction text
                trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#") -> {
                    if (currentInstruction.isNotEmpty()) {
                        currentInstruction += "\n"
                    }
                    currentInstruction += trimmedLine
                }
            }
        }

        // Add the last step
        if (currentInstruction.isNotEmpty()) {
            steps.add(
                createCookingStep(
                    currentStepNumber - 1,
                    currentInstruction,
                    currentIngredients.toList(),
                    currentTips.toList(),
                ),
            )
        }

        // If no structured steps found, create simple steps from content
        if (steps.isEmpty()) {
            val simpleSteps = recipe.markdown.split("\n\n").filter { it.trim().isNotEmpty() }
            simpleSteps.forEachIndexed { index, step ->
                steps.add(createCookingStep(index + 1, step.trim(), emptyList(), emptyList()))
            }
        }

        return steps
    }

    private fun createCookingStep(
        stepNumber: Int,
        instruction: String,
        ingredients: List<Ingredient>,
        tips: List<String>,
    ): CookingStep {
        // Extract timer duration from instruction
        val timerRegex = Regex("(\\d+)\\s*(min|minuten|sek|sekunden)", RegexOption.IGNORE_CASE)
        val timerMatch = timerRegex.find(instruction)
        val duration =
            timerMatch?.let { match ->
                val value = match.groupValues[1].toInt()
                val unit = match.groupValues[2].lowercase()
                when {
                    unit.startsWith("min") -> value * 60
                    unit.startsWith("sek") -> value
                    else -> null
                }
            }

        // Extract temperature from instruction
        val tempRegex = Regex("(\\d+)\\s*°?[CcFf]?", RegexOption.IGNORE_CASE)
        val tempMatch = tempRegex.find(instruction)
        val temperature = tempMatch?.let { "${it.groupValues[1]}°C" }

        return CookingStep(
            stepNumber = stepNumber,
            instruction = instruction,
            duration = duration,
            ingredients = ingredients,
            temperature = temperature,
            tips = tips,
            estimatedTime = estimateStepTime(instruction),
        )
    }

    private fun parseIngredient(text: String): Ingredient? {
        // Parse ingredient text like "200g Mehl" or "1 EL Olivenöl"
        val ingredientRegex = Regex("(\\d+(?:[.,]\\d+)?)\\s*(\\w+)?\\s+(.+)")
        val match = ingredientRegex.find(text)

        return if (match != null) {
            val quantity = match.groupValues[1].replace(",", ".")
            val unit = match.groupValues[2].ifEmpty { "Stück" }
            val name = match.groupValues[3].trim()
            val isOptional =
                text.contains("optional", ignoreCase = true) ||
                    text.contains("nach belieben", ignoreCase = true)

            Ingredient(
                name = name,
                quantity = quantity,
                unit = unit,
                isOptional = isOptional,
            )
        } else {
            // Fallback for ingredients without quantity
            Ingredient(
                name = text,
                quantity = "1",
                unit = "Stück",
                isOptional = text.contains("optional", ignoreCase = true),
            )
        }
    }

    private fun estimateStepTime(instruction: String): Int {
        // Simple heuristic to estimate step time based on instruction content
        val wordCount = instruction.split("\\s+".toRegex()).size
        val baseTime =
            when {
                instruction.contains("backen", ignoreCase = true) ||
                    instruction.contains("garen", ignoreCase = true) -> 10 * 60 // 10 minutes
                instruction.contains("braten", ignoreCase = true) ||
                    instruction.contains("anbraten", ignoreCase = true) -> 5 * 60 // 5 minutes
                instruction.contains("schneiden", ignoreCase = true) ||
                    instruction.contains("hacken", ignoreCase = true) -> 3 * 60 // 3 minutes
                instruction.contains("mischen", ignoreCase = true) ||
                    instruction.contains("verrühren", ignoreCase = true) -> 2 * 60 // 2 minutes
                else -> 60 // 1 minute default
            }

        // Adjust based on instruction length
        return (baseTime + (wordCount * 2)).coerceIn(30, 30 * 60) // 30 seconds to 30 minutes
    }

    private fun calculateEstimatedDuration(steps: List<CookingStep>): Int {
        return steps.sumOf { step ->
            step.duration ?: step.estimatedTime ?: 60
        }
    }

    private fun startTimerCountdown(
        timerId: String,
        initialDuration: Int,
    ) {
        scope.launch {
            var remainingTime = initialDuration

            while (remainingTime > 0) {
                delay(1000)
                remainingTime--

                // Update timer in database
                database.cookingTimerDao().updateRemainingTime(timerId, remainingTime.toLong())

                // Update local state
                val currentTimers = _stepTimers.value.toMutableMap()
                currentTimers.values.find { it.stepIndex == remainingTime }?.let { timer ->
                    val stepIndex = timer.stepIndex
                    currentTimers[stepIndex] = timer.copy(remainingTime = remainingTime)
                    _stepTimers.value = currentTimers
                }
            }

            // Timer completed
            database.cookingTimerDao().completeTimer(timerId, System.currentTimeMillis() / 1000)

            // Update local state
            val currentTimers = _stepTimers.value.toMutableMap()
            currentTimers.values.find { timer ->
                database.cookingTimerDao().getTimerById(timerId)?.stepIndex == timer.stepIndex
            }?.let { timer ->
                val stepIndex = timer.stepIndex
                currentTimers[stepIndex] =
                    timer.copy(
                        remainingTime = 0,
                        isActive = false,
                        isCompleted = true,
                    )
                _stepTimers.value = currentTimers
            }
        }
    }
}
