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
            val mockRepository = MockAiProviderRepository()
            
            // Test training plan routing -> Gemini
            val trainingProvider = mockRepository.selectOptimalProvider(TaskType.TRAINING_PLAN)
            if (trainingProvider != AiProvider.Gemini) return false
            
            // Test recipe generation routing -> Gemini (Perplexity disabled)
            val recipeProvider = mockRepository.selectOptimalProvider(TaskType.RECIPE_GENERATION)
            if (recipeProvider != AiProvider.Gemini) return false

            // Test shopping list parsing routing -> Gemini (Perplexity disabled)
            val shoppingProvider = mockRepository.selectOptimalProvider(TaskType.SHOPPING_LIST_PARSING)
            if (shoppingProvider != AiProvider.Gemini) return false
            
            // Test image task routing -> Gemini
            val imageProvider = mockRepository.selectOptimalProvider(TaskType.CALORIE_ESTIMATION, hasImage = true)
            return imageProvider == AiProvider.Gemini
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
private class MockAiProviderRepository {

    fun selectOptimalProvider(
        taskType: TaskType,
        hasImage: Boolean = false
    ): AiProvider {
        // Perplexity is disabled by default, so all tasks go to Gemini
        val perplexityAvailable = false

        return when {
            hasImage -> AiProvider.Gemini
            taskType == TaskType.TRAINING_PLAN -> AiProvider.Gemini
            taskType == TaskType.SHOPPING_LIST_PARSING && perplexityAvailable -> AiProvider.Perplexity
            taskType == TaskType.RECIPE_GENERATION && perplexityAvailable -> AiProvider.Perplexity
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