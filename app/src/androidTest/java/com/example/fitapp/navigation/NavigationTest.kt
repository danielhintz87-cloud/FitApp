package com.example.fitapp.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitapp.ui.MainScaffold
import com.example.fitapp.ui.theme.FitAppTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            FitAppTheme {
                MainScaffold()
            }
        }
    }

    @Test
    fun navHost_verifyStartDestination() {
        composeTestRule
            .onNodeWithText("Dashboard")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_clickDashboard_navigatesToDashboard() {
        // Open drawer
        composeTestRule.onNodeWithContentDescription("Menü").performClick()

        // Click on Dashboard
        composeTestRule.onNodeWithText("🏠 Dashboard").performClick()

        // Verify we're on dashboard
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    @Test
    fun navHost_clickTraining_navigatesToTraining() {
        // Open drawer
        composeTestRule.onNodeWithContentDescription("Menü").performClick()

        // Click on Training & Pläne
        composeTestRule.onNodeWithText("🎯 Training & Pläne").performClick()

        // Verify we're on training screen
        composeTestRule.onNodeWithText("Training & Pläne").assertIsDisplayed()
    }

    @Test
    fun navHost_clickNutrition_navigatesToNutrition() {
        // Open drawer
        composeTestRule.onNodeWithContentDescription("Menü").performClick()

        // Click on Ernährung & Rezepte
        composeTestRule.onNodeWithText("🍽️ Ernährung & Rezepte").performClick()

        // Verify we're on nutrition screen
        composeTestRule.onNodeWithText("Ernährung & Rezepte").assertIsDisplayed()
    }

    @Test
    fun navHost_clickAnalytics_navigatesToAnalytics() {
        // Open drawer
        composeTestRule.onNodeWithContentDescription("Menü").performClick()

        // Click on Fortschritt & Analytics
        composeTestRule.onNodeWithText("📊 Fortschritt & Analytics").performClick()

        // Verify we're on analytics screen
        composeTestRule.onNodeWithText("Fortschritt & Analytics").assertIsDisplayed()
    }

    @Test
    fun navHost_clickSettings_navigatesToSettings() {
        // Open drawer
        composeTestRule.onNodeWithContentDescription("Menü").performClick()

        // Click on Einstellungen
        composeTestRule.onNodeWithText("⚙️ Einstellungen").performClick()

        // Verify we're on settings screen
        composeTestRule.onNodeWithText("Einstellungen").assertIsDisplayed()
    }

    @Test
    fun topBar_contextualActions_displayCorrectly() {
        // Navigate to training screen
        composeTestRule.onNodeWithContentDescription("Menü").performClick()
        composeTestRule.onNodeWithText("🎯 Training & Pläne").performClick()

        // Verify AI Trainer quick action is displayed
        composeTestRule.onNodeWithContentDescription("KI Trainer").assertIsDisplayed()

        // Verify HIIT Builder quick action is displayed
        composeTestRule.onNodeWithContentDescription("HIIT Builder").assertIsDisplayed()
    }

    @Test
    fun overflowMenu_displaysAllOptions() {
        // Click overflow menu
        composeTestRule.onNodeWithContentDescription("Mehr Optionen").performClick()

        // Verify menu items are displayed
        composeTestRule.onNodeWithText("Einstellungen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Health Connect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hilfe & Support").assertIsDisplayed()
        composeTestRule.onNodeWithText("Über die App").assertIsDisplayed()
    }
}
