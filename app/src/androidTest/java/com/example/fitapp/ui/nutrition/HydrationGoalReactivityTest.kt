package com.example.fitapp.ui.nutrition

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.domain.usecases.HydrationGoalUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId

/**
 * Instrumentation test for HydrationGoalUseCase UI reactivity.
 *
 * Validates that UI components automatically update when hydration goals change,
 * demonstrating the reactive Flow/StateFlow integration.
 */
@RunWith(AndroidJUnit4::class)
class HydrationGoalReactivityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hydrationGoal_reactiveUI_updatesWhenGoalChanges() =
        runTest {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val hydrationGoalUseCase = HydrationGoalUseCase.create(context)
            val userPreferencesRepository = UserPreferencesRepository(context)

            // Start with a known state
            userPreferencesRepository.updateNutritionPreferences(dailyWaterGoalLiters = 2.0)

            composeTestRule.setContent {
                MaterialTheme {
                    HydrationGoalReactiveComponent(hydrationGoalUseCase = hydrationGoalUseCase)
                }
            }

            // Verify initial state shows 2000ml
            composeTestRule
                .onNodeWithText("Goal: 2000ml")
                .assertIsDisplayed()

            // Update the goal
            userPreferencesRepository.updateNutritionPreferences(dailyWaterGoalLiters = 3.5)

            // Wait for UI to update reactively (should happen automatically)
            composeTestRule.waitForIdle()

            // Verify UI updated to show new goal
            composeTestRule
                .onNodeWithText("Goal: 3500ml")
                .assertIsDisplayed()

            // Verify old value is no longer displayed
            composeTestRule
                .onNodeWithText("Goal: 2000ml")
                .assertDoesNotExist()
        }

    @Test
    fun hydrationGoal_reactiveUI_updatesFromDailyGoalPriority() =
        runTest {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val hydrationGoalUseCase = HydrationGoalUseCase.create(context)
            val userPreferencesRepository = UserPreferencesRepository(context)
            val database = AppDatabase.get(context)
            val nutritionRepository = NutritionRepository(database)

            val today = LocalDate.now(ZoneId.systemDefault())

            // Set up preference as fallback
            userPreferencesRepository.updateNutritionPreferences(dailyWaterGoalLiters = 2.5)

            composeTestRule.setContent {
                MaterialTheme {
                    HydrationGoalReactiveComponent(hydrationGoalUseCase = hydrationGoalUseCase)
                }
            }

            // Initially shows preference value
            composeTestRule
                .onNodeWithText("Goal: 2500ml")
                .assertIsDisplayed()

            // Add a daily goal (higher priority)
            nutritionRepository.addOrUpdateDailyGoal(today, targetWaterMl = 4000)

            // Wait for reactive update
            composeTestRule.waitForIdle()

            // Should now show daily goal value (higher priority)
            composeTestRule
                .onNodeWithText("Goal: 4000ml")
                .assertIsDisplayed()

            // Remove daily goal
            nutritionRepository.deleteDailyGoal(today)

            // Wait for reactive update
            composeTestRule.waitForIdle()

            // Should fall back to preference value
            composeTestRule
                .onNodeWithText("Goal: 2500ml")
                .assertIsDisplayed()
        }

    @Test
    fun hydrationGoal_reactiveUI_handlesMultipleComponents() =
        runTest {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val hydrationGoalUseCase = HydrationGoalUseCase.create(context)
            val userPreferencesRepository = UserPreferencesRepository(context)

            // Set initial goal
            userPreferencesRepository.updateNutritionPreferences(dailyWaterGoalLiters = 1.5)

            composeTestRule.setContent {
                MaterialTheme {
                    // Multiple components using the same UseCase
                    HydrationGoalReactiveComponent(
                        hydrationGoalUseCase = hydrationGoalUseCase,
                        label = "Component 1",
                    )
                    HydrationGoalReactiveComponent(
                        hydrationGoalUseCase = hydrationGoalUseCase,
                        label = "Component 2",
                    )
                }
            }

            // Both components show initial value
            composeTestRule
                .onNodeWithText("Component 1: 1500ml")
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText("Component 2: 1500ml")
                .assertIsDisplayed()

            // Update goal
            userPreferencesRepository.updateNutritionPreferences(dailyWaterGoalLiters = 2.8)

            // Wait for updates
            composeTestRule.waitForIdle()

            // Both components should update reactively
            composeTestRule
                .onNodeWithText("Component 1: 2800ml")
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText("Component 2: 2800ml")
                .assertIsDisplayed()
        }
}

/**
 * Simple reactive component for testing hydration goal updates.
 */
@Composable
private fun HydrationGoalReactiveComponent(
    hydrationGoalUseCase: HydrationGoalUseCase,
    label: String = "Goal",
) {
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }

    // This should update automatically when goals change
    val hydrationGoal by hydrationGoalUseCase.getHydrationGoalMlFlow(today)
        .collectAsState(initial = 0)

    androidx.compose.material3.Text(
        text = "$label: ${hydrationGoal}ml",
    )
}
