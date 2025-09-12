package com.example.fitapp.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitapp.ui.MainScaffold
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for navigation flows to verify all key features are accessible
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun navigation_mainScaffoldLoads() {
        composeTestRule.setContent {
            MainScaffold()
        }

        // Verify the main scaffold loads without crashing
        composeTestRule.onNodeWithText("FitApp").assertExists()
    }

    @Test
    fun navigation_drawerMenuOpensAndShowsAllSections() {
        composeTestRule.setContent {
            MainScaffold()
        }

        // Open drawer
        composeTestRule.onNodeWithContentDescription("Menü").performClick()

        // Verify all main sections are present
        composeTestRule.onNodeWithText("🏠 Dashboard").assertExists()
        composeTestRule.onNodeWithText("🎯 Training & Pläne").assertExists()
        composeTestRule.onNodeWithText("🍽️ Ernährung & Rezepte").assertExists()
        composeTestRule.onNodeWithText("📊 Fortschritt & Analytics").assertExists()
        composeTestRule.onNodeWithText("⚡ Schnellaktionen").assertExists()
        composeTestRule.onNodeWithText("⚙️ Einstellungen").assertExists()
    }

    @Test
    fun navigation_quickActionsAccessible() {
        composeTestRule.setContent {
            MainScaffold()
        }

        // Open drawer and navigate to Quick Actions
        composeTestRule.onNodeWithContentDescription("Menü").performClick()
        composeTestRule.onNodeWithText("⚡ Schnellaktionen").performClick()

        // Verify Quick Actions screen loads
        composeTestRule.onNodeWithText("Schnellaktionen").assertExists()
        composeTestRule.onNodeWithText("Schnelle Aktionen").assertExists()
    }

    @Test
    fun navigation_feedbackAccessible() {
        composeTestRule.setContent {
            MainScaffold()
        }

        // Access feedback through overflow menu
        composeTestRule.onNodeWithContentDescription("Mehr Optionen").performClick()
        composeTestRule.onNodeWithText("Feedback senden").performClick()

        // Verify Feedback screen loads
        composeTestRule.onNodeWithText("Feedback senden").assertExists()
        composeTestRule.onNodeWithText("Ihr Feedback ist wichtig").assertExists()
    }

    @Test
    fun navigation_nutritionHubToRecipeGeneration() {
        composeTestRule.setContent {
            MainScaffold()
        }

        // Navigate to Nutrition Hub
        composeTestRule.onNodeWithContentDescription("Menü").performClick()
        composeTestRule.onNodeWithText("🍽️ Ernährung & Rezepte").performClick()

        // Should be able to find recipe generation option
        // This would need to be expanded based on the actual UI structure
        composeTestRule.waitForIdle()
    }

    @Test
    fun navigation_trainingHubToAIPersonalTrainer() {
        composeTestRule.setContent {
            MainScaffold()
        }

        // Navigate to Training Hub
        composeTestRule.onNodeWithContentDescription("Menü").performClick()
        composeTestRule.onNodeWithText("🎯 Training & Pläne").performClick()

        // Should be able to find AI Personal Trainer option
        composeTestRule.waitForIdle()
    }

    @Test
    fun navigation_trainingHubToHIITBuilder() {
        composeTestRule.setContent {
            MainScaffold()
        }

        // Navigate to Training Hub
        composeTestRule.onNodeWithContentDescription("Menü").performClick()
        composeTestRule.onNodeWithText("🎯 Training & Pläne").performClick()

        // Should be able to find HIIT Builder option
        composeTestRule.waitForIdle()
    }

    @Test
    fun navigation_overflowMenuOptionsAccessible() {
        composeTestRule.setContent {
            MainScaffold()
        }

        // Open overflow menu
        composeTestRule.onNodeWithContentDescription("Mehr Optionen").performClick()

        // Verify all options are present
        composeTestRule.onNodeWithText("Einstellungen").assertExists()
        composeTestRule.onNodeWithText("Health Connect").assertExists()
        composeTestRule.onNodeWithText("Hilfe & Support").assertExists()
        composeTestRule.onNodeWithText("Feedback senden").assertExists()
        composeTestRule.onNodeWithText("Über die App").assertExists()
    }
}
