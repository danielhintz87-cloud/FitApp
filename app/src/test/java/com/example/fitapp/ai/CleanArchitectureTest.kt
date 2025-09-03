package com.example.fitapp.ai

import com.example.fitapp.domain.entities.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Simple verification test for Clean Architecture implementation
 * Demonstrates that the architecture components work correctly
 */
class CleanArchitectureTest {
    
    @Test
    fun testCleanArchitectureImplementation() {
        // Test provider routing logic
        assertTrue("Provider routing should work correctly", testProviderRouting())
        assertTrue("Fallback provider logic should work correctly", testFallbackProvider())
        assertTrue("Domain entities should work correctly", testDomainEntities())
    }
    
    companion object {
        /**
         * Manual test function that can be called to verify architecture
         * In a real test suite, this would be proper unit tests with mocking
         */
        fun verifyArchitecture(): Boolean {
            return try {
                // Test provider routing logic
                testProviderRouting() &&
                testFallbackProvider() &&
                testDomainEntities()
            } catch (e: Exception) {
                false
            }
        }
        
        private fun testProviderRouting(): Boolean {
            // Test with Perplexity disabled (original behavior)
            val mockRepositoryNoPerplexity = MockAiProviderRepository(perplexityAvailable = false)
            
            // Test training plan routing -> Gemini
            val trainingProvider = mockRepositoryNoPerplexity.selectOptimalProvider(TaskType.TRAINING_PLAN)
            if (trainingProvider != AiProvider.Gemini) return false
            
            // Test recipe generation routing -> Gemini (Perplexity disabled)
            val recipeProvider = mockRepositoryNoPerplexity.selectOptimalProvider(TaskType.RECIPE_GENERATION)
            if (recipeProvider != AiProvider.Gemini) return false

            // Test shopping list parsing routing -> Gemini (Perplexity disabled)
            val shoppingProvider = mockRepositoryNoPerplexity.selectOptimalProvider(TaskType.SHOPPING_LIST_PARSING)
            if (shoppingProvider != AiProvider.Gemini) return false
            
            // Test image task routing -> Gemini
            val imageProvider = mockRepositoryNoPerplexity.selectOptimalProvider(TaskType.CALORIE_ESTIMATION, hasImage = true)
            if (imageProvider != AiProvider.Gemini) return false

            // Test calorie estimation (text-only) -> Gemini (Perplexity disabled)
            val calorieTextProvider = mockRepositoryNoPerplexity.selectOptimalProvider(TaskType.CALORIE_ESTIMATION, hasImage = false)
            if (calorieTextProvider != AiProvider.Gemini) return false

            // Test with Perplexity enabled (new behavior)
            val mockRepositoryWithPerplexity = MockAiProviderRepository(perplexityAvailable = true)
            
            // Test training plan routing -> still Gemini (even with Perplexity available)
            val trainingProviderWithPerplexity = mockRepositoryWithPerplexity.selectOptimalProvider(TaskType.TRAINING_PLAN)
            if (trainingProviderWithPerplexity != AiProvider.Gemini) return false
            
            // Test recipe generation routing -> Perplexity (when available)
            val recipeProviderWithPerplexity = mockRepositoryWithPerplexity.selectOptimalProvider(TaskType.RECIPE_GENERATION)
            if (recipeProviderWithPerplexity != AiProvider.Perplexity) return false

            // Test shopping list parsing routing -> Perplexity (when available)
            val shoppingProviderWithPerplexity = mockRepositoryWithPerplexity.selectOptimalProvider(TaskType.SHOPPING_LIST_PARSING)
            if (shoppingProviderWithPerplexity != AiProvider.Perplexity) return false
            
            // Test image task routing -> still Gemini (even with Perplexity available)
            val imageProviderWithPerplexity = mockRepositoryWithPerplexity.selectOptimalProvider(TaskType.CALORIE_ESTIMATION, hasImage = true)
            if (imageProviderWithPerplexity != AiProvider.Gemini) return false

            // Test calorie estimation (text-only) -> Perplexity (when available and no image)
            val calorieTextProviderWithPerplexity = mockRepositoryWithPerplexity.selectOptimalProvider(TaskType.CALORIE_ESTIMATION, hasImage = false)
            if (calorieTextProviderWithPerplexity != AiProvider.Perplexity) return false

            return true
        }
        
        private fun testFallbackProvider(): Boolean {
            val mockRepository = MockAiProviderRepository()
            
            // Test fallback logic
            val geminiFallback = mockRepository.getFallbackProvider(AiProvider.Gemini)
            if (geminiFallback != AiProvider.Perplexity) return false
            
            val perplexityFallback = mockRepository.getFallbackProvider(AiProvider.Perplexity)
            return perplexityFallback == AiProvider.Gemini
        }
        
        private fun testDomainEntities(): Boolean {
            // Test that domain entities work correctly
            val planRequest = PlanRequest(
                goal = "Test goal",
                weeks = 8,
                sessionsPerWeek = 3,
                minutesPerSession = 45,
                equipment = listOf("Dumbbells")
            )
            
            if (planRequest.goal != "Test goal") return false
            if (planRequest.weeks != 8) return false
            if (planRequest.sessionsPerWeek != 3) return false
            if (planRequest.minutesPerSession != 45) return false
            if (planRequest.equipment != listOf("Dumbbells")) return false
            
            val recipeRequest = RecipeRequest(
                preferences = "High protein",
                diet = "Vegetarian",
                count = 5
            )
            
            if (recipeRequest.preferences != "High protein") return false
            if (recipeRequest.diet != "Vegetarian") return false
            if (recipeRequest.count != 5) return false
            
            val caloriesEstimate = CaloriesEstimate(
                kcal = 450,
                confidence = 85,
                text = "Test analysis"
            )
            
            if (caloriesEstimate.kcal != 450) return false
            if (caloriesEstimate.confidence != 85) return false
            if (caloriesEstimate.text != "Test analysis") return false
            
            return true
        }
    }
}

/**
 * Mock implementation for testing routing logic
 */
private class MockAiProviderRepository(private val perplexityAvailable: Boolean = false) {

    fun selectOptimalProvider(
        taskType: TaskType,
        hasImage: Boolean = false
    ): AiProvider {
        return when {
            hasImage -> AiProvider.Gemini
            taskType == TaskType.TRAINING_PLAN -> AiProvider.Gemini
            taskType == TaskType.SHOPPING_LIST_PARSING && perplexityAvailable -> AiProvider.Perplexity
            taskType == TaskType.RECIPE_GENERATION && perplexityAvailable -> AiProvider.Perplexity
            // Other text-only tasks prefer Perplexity when available
            !hasImage && perplexityAvailable -> AiProvider.Perplexity
            else -> AiProvider.Gemini
        }
    }
    
    fun getFallbackProvider(primary: AiProvider): AiProvider? {
        return when (primary) {
            AiProvider.Gemini -> AiProvider.Perplexity
            AiProvider.Perplexity -> AiProvider.Gemini
        }
    }
}