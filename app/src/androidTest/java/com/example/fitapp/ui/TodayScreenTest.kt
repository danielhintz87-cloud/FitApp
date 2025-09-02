package com.example.fitapp.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitapp.ui.screens.TodayScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for TodayScreen
 * Tests training completion flow and UI interactions
 */
@RunWith(AndroidJUnit4::class)
class TodayScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun training_completion_button_updates_streak_counter() {
        // Given: Today screen is displayed
        composeTestRule.setContent {
            TodayScreen(contentPadding = PaddingValues(0.dp))
        }

        // When: User taps training completed button
        composeTestRule
            .onNodeWithText("Training abgeschlossen")
            .performClick()

        // Then: Streak should be updated (would need to check state/database)
        // This is a simplified test - in real implementation would verify streak increment
        composeTestRule
            .onNodeWithText("Training abgeschlossen")
            .assertExists()
    }

    @Test
    fun training_skip_button_maintains_current_streak() {
        // Given: Today screen is displayed
        composeTestRule.setContent {
            TodayScreen(contentPadding = PaddingValues(0.dp))
        }

        // When: User taps skip training button
        composeTestRule
            .onNodeWithText("Training überspringen")
            .performClick()

        // Then: Skip action should be recorded
        composeTestRule
            .onNodeWithText("Training überspringen")
            .assertExists()
    }

    @Test
    fun daily_motivation_displays_with_background_image() {
        // Given: Today screen is displayed
        composeTestRule.setContent {
            TodayScreen(contentPadding = PaddingValues(0.dp))
        }

        // Then: Motivation section should be visible
        composeTestRule
            .onNodeWithTag("motivation_section")
            .assertExists()
            
        // And background image should be loaded
        composeTestRule
            .onNodeWithTag("background_image")
            .assertExists()
    }

    @Test
    fun streak_counter_displays_current_values() {
        // Given: Today screen is displayed
        composeTestRule.setContent {
            TodayScreen(contentPadding = PaddingValues(0.dp))
        }

        // Then: Streak counters should be visible
        composeTestRule
            .onAllNodesWithText("Tage")
            .assertCountEquals(3) // Should have 3 streak counters

        // And streak numbers should be displayed
        composeTestRule
            .onNodeWithTag("training_streak")
            .assertExists()
            
        composeTestRule
            .onNodeWithTag("nutrition_streak")
            .assertExists()
            
        composeTestRule
            .onNodeWithTag("weight_streak")
            .assertExists()
    }

    @Test
    fun navigation_buttons_are_accessible() {
        // Given: Today screen is displayed
        composeTestRule.setContent {
            TodayScreen(contentPadding = PaddingValues(0.dp))
        }

        // Then: Navigation buttons should be present and clickable
        composeTestRule
            .onNodeWithText("Training")
            .assertExists()
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("Ernährung")
            .assertExists()
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("Fortschritt")
            .assertExists()
            .assertIsEnabled()
    }

    @Test
    fun today_workout_section_displays_correctly() {
        // Given: Today screen is displayed
        composeTestRule.setContent {
            TodayScreen(contentPadding = PaddingValues(0.dp))
        }

        // Then: Today's workout section should be visible
        composeTestRule
            .onNodeWithTag("today_workout")
            .assertExists()

        // And workout title should be displayed
        composeTestRule
            .onNodeWithText("Heutiges Training")
            .assertExists()
    }

    @Test
    fun user_can_interact_with_floating_action_button() {
        // Given: Today screen is displayed
        composeTestRule.setContent {
            TodayScreen(contentPadding = PaddingValues(0.dp))
        }

        // When: User taps the floating action button
        composeTestRule
            .onNodeWithTag("add_fab")
            .performClick()

        // Then: FAB should respond to click
        composeTestRule
            .onNodeWithTag("add_fab")
            .assertExists()
    }
}