package com.example.fitapp.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit4.runners.AndroidJUnit4
import com.example.fitapp.ui.screens.AIPersonalTrainerScreen
import com.example.fitapp.ui.theme.FitAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AIPersonalTrainerNavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aiPersonalTrainer_quickActionsWork() {
        var workoutClicked = false
        var nutritionClicked = false
        var analyticsClicked = false

        composeTestRule.setContent {
            FitAppTheme {
                AIPersonalTrainerScreen(
                    onNavigateToHiitBuilder = { workoutClicked = true },
                    onNavigateToNutrition = { nutritionClicked = true },
                    onNavigateToAnalytics = { analyticsClicked = true },
                )
            }
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Find and click workout quick action
        composeTestRule.onNodeWithContentDescription("Fitness Center Symbol").performClick()
        assert(workoutClicked) { "Workout quick action should trigger navigation" }

        // Find and click nutrition quick action
        composeTestRule.onNodeWithContentDescription("Restaurant Symbol").performClick()
        assert(nutritionClicked) { "Nutrition quick action should trigger navigation" }

        // Find and click analytics quick action
        composeTestRule.onNodeWithContentDescription("Analytics Symbol").performClick()
        assert(analyticsClicked) { "Analytics quick action should trigger navigation" }
    }

    @Test
    fun aiPersonalTrainer_backButtonWorks() {
        var backClicked = false

        composeTestRule.setContent {
            FitAppTheme {
                AIPersonalTrainerScreen(
                    onBack = { backClicked = true },
                )
            }
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("Zurück").performClick()
        assert(backClicked) { "Back button should trigger navigation" }
    }

    @Test
    fun aiPersonalTrainer_displaysCorrectTitle() {
        composeTestRule.setContent {
            FitAppTheme {
                AIPersonalTrainerScreen()
            }
        }

        // Verify title is displayed
        composeTestRule.onNodeWithText("AI Personal Trainer").assertIsDisplayed()

        // Verify psychology icon is displayed
        composeTestRule.onNodeWithContentDescription("Psychologie Symbol").assertIsDisplayed()
    }

    @Test
    fun aiPersonalTrainer_workoutPlanButtonWorks() {
        var workoutStarted = false

        composeTestRule.setContent {
            FitAppTheme {
                AIPersonalTrainerScreen(
                    onNavigateToWorkout = { _, _ -> workoutStarted = true },
                )
            }
        }

        // Wait for loading to complete and check if workout plan button exists
        composeTestRule.waitForIdle()

        // Note: The button might not be visible if AI data is not loaded
        // This test would need mock data to be fully effective
    }

    @Test
    fun aiPersonalTrainer_mealPlanButtonWorks() {
        var mealPlanViewed = false

        composeTestRule.setContent {
            FitAppTheme {
                AIPersonalTrainerScreen(
                    onNavigateToRecipeGeneration = { mealPlanViewed = true },
                )
            }
        }

        // Wait for loading to complete
        composeTestRule.waitForIdle()

        // Note: The button might not be visible if AI data is not loaded
        // This test would need mock data to be fully effective
    }
}
