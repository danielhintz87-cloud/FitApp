package com.example.fitapp.services

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🚀 User Experience Manager
 *
 * Manages user onboarding, preferences, and first-time experience
 */
class UserExperienceManager(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "fitapp_user_experience"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_UNIFIED_DASHBOARD_SHOWN = "unified_dashboard_shown"
        private const val KEY_FEATURES_DISCOVERED = "features_discovered"
        private const val KEY_APP_VERSION_SEEN = "app_version_seen"

        @Volatile
        private var INSTANCE: UserExperienceManager? = null

        fun getInstance(context: Context): UserExperienceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserExperienceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _userExperienceState = MutableStateFlow(getUserExperienceState())
    val userExperienceState: StateFlow<UserExperienceState> = _userExperienceState.asStateFlow()

    private fun getUserExperienceState(): UserExperienceState {
        return UserExperienceState(
            isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true),
            hasCompletedOnboarding = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false),
            hasSeenUnifiedDashboard = prefs.getBoolean(KEY_UNIFIED_DASHBOARD_SHOWN, false),
            discoveredFeatures = prefs.getStringSet(KEY_FEATURES_DISCOVERED, emptySet()) ?: emptySet(),
            lastSeenAppVersion = prefs.getString(KEY_APP_VERSION_SEEN, "1.0.0") ?: "1.0.0",
        )
    }

    /**
     * Mark onboarding as completed
     */
    fun completeOnboarding() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .putBoolean(KEY_FIRST_LAUNCH, false)
            .apply()

        _userExperienceState.value = getUserExperienceState()
    }

    /**
     * Mark unified dashboard as seen
     */
    fun markUnifiedDashboardSeen() {
        prefs.edit()
            .putBoolean(KEY_UNIFIED_DASHBOARD_SHOWN, true)
            .apply()

        _userExperienceState.value = getUserExperienceState()
    }

    /**
     * Track discovered features for personalization
     */
    fun markFeatureDiscovered(feature: String) {
        val currentFeatures = prefs.getStringSet(KEY_FEATURES_DISCOVERED, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentFeatures.add(feature)

        prefs.edit()
            .putStringSet(KEY_FEATURES_DISCOVERED, currentFeatures)
            .apply()

        _userExperienceState.value = getUserExperienceState()
    }

    /**
     * Check if user should see onboarding
     */
    fun shouldShowOnboarding(): Boolean {
        return !prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Check if this is a first launch
     */
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    /**
     * Get user's preferred starting screen
     */
    fun getPreferredStartScreen(): String {
        val hasSeenUnified = prefs.getBoolean(KEY_UNIFIED_DASHBOARD_SHOWN, false)
        val hasCompletedOnboarding = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

        return when {
            !hasCompletedOnboarding -> "onboarding"
            !hasSeenUnified -> "unified_dashboard"
            else -> "unified_dashboard" // Default to unified experience
        }
    }

    /**
     * Reset all user experience data (for testing/debugging)
     */
    fun resetUserExperience() {
        prefs.edit().clear().apply()
        _userExperienceState.value = getUserExperienceState()
    }

    /**
     * Get personalized recommendations based on discovered features
     */
    fun getPersonalizedRecommendations(): List<FeatureRecommendation> {
        val discoveredFeatures = prefs.getStringSet(KEY_FEATURES_DISCOVERED, emptySet()) ?: emptySet()
        val recommendations = mutableListOf<FeatureRecommendation>()

        // Recommend based on what user hasn't discovered yet
        if (!discoveredFeatures.contains("bmi_calculator")) {
            recommendations.add(
                FeatureRecommendation(
                    feature = "bmi_calculator",
                    title = "BMI Calculator entdecken",
                    description = "Verfolge deine Gewichtsziele mit unserem smarten BMI Rechner",
                    priority = 8,
                ),
            )
        }

        if (!discoveredFeatures.contains("intervallfasten")) {
            recommendations.add(
                FeatureRecommendation(
                    feature = "fasting",
                    title = "Intervallfasten ausprobieren",
                    description = "6 professionelle Fasten-Protokolle für optimale Gesundheit",
                    priority = 9,
                ),
            )
        }

        if (!discoveredFeatures.contains("ai_personal_trainer")) {
            recommendations.add(
                FeatureRecommendation(
                    feature = "ai_personal_trainer",
                    title = "AI Personal Trainer",
                    description = "Personalisierte Trainingspläne powered by KI",
                    priority = 10,
                ),
            )
        }

        if (!discoveredFeatures.contains("barcode_scanner")) {
            recommendations.add(
                FeatureRecommendation(
                    feature = "barcode_scanner",
                    title = "Barcode Scanner nutzen",
                    description = "Scanne Produkte für instant Nährwert-Information",
                    priority = 7,
                ),
            )
        }

        if (!discoveredFeatures.contains("recipes")) {
            recommendations.add(
                FeatureRecommendation(
                    feature = "recipes",
                    title = "Gesunde Rezepte",
                    description = "AI-generierte Rezepte passend zu deinen Zielen",
                    priority = 6,
                ),
            )
        }

        return recommendations.sortedByDescending { it.priority }
    }
}

/**
 * State representing user's experience progress
 */
data class UserExperienceState(
    val isFirstLaunch: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val hasSeenUnifiedDashboard: Boolean = false,
    val discoveredFeatures: Set<String> = emptySet(),
    val lastSeenAppVersion: String = "1.0.0",
) {
    /**
     * Calculate user engagement level based on discovered features
     */
    fun getEngagementLevel(): UserEngagementLevel {
        return when (discoveredFeatures.size) {
            0 -> UserEngagementLevel.NEW
            in 1..2 -> UserEngagementLevel.EXPLORING
            in 3..5 -> UserEngagementLevel.ACTIVE
            in 6..8 -> UserEngagementLevel.ENGAGED
            else -> UserEngagementLevel.POWER_USER
        }
    }

    /**
     * Get completion percentage for feature discovery
     */
    fun getFeatureDiscoveryProgress(): Float {
        val totalFeatures = 10 // Total major features in the app
        return (discoveredFeatures.size.toFloat() / totalFeatures).coerceAtMost(1.0f)
    }
}

/**
 * User engagement levels for personalization
 */
enum class UserEngagementLevel {
    NEW,
    EXPLORING,
    ACTIVE,
    ENGAGED,
    POWER_USER,
}

/**
 * Feature recommendation for personalized suggestions
 */
data class FeatureRecommendation(
    val feature: String,
    val title: String,
    val description: String,
    val priority: Int,
)

/**
 * Composable helper to access user experience state
 */
@Composable
fun rememberUserExperienceState(context: Context): UserExperienceState {
    val manager = remember(context) { UserExperienceManager.getInstance(context) }
    val state by manager.userExperienceState.collectAsState()
    return state
}

/**
 * Composable helper to access user experience manager
 */
@Composable
fun rememberUserExperienceManager(context: Context): UserExperienceManager {
    return remember(context) { UserExperienceManager.getInstance(context) }
}
