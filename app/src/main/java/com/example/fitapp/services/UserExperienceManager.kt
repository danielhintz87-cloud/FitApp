package com.example.fitapp.services

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.fitapp.data.prefs.UserPreferencesProto
import com.example.fitapp.data.prefs.UserPreferencesSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 🚀 User Experience Manager
 * 
 * Manages user onboarding, preferences, and first-time experience using Proto DataStore
 */
class UserExperienceManager(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: UserExperienceManager? = null
        
        fun getInstance(context: Context): UserExperienceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserExperienceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Use the same proto DataStore instance
    private val Context.dataStore: DataStore<UserPreferencesProto> by dataStore(
        fileName = "user_preferences.pb",
        serializer = UserPreferencesSerializer
    )
    
    private val _userExperienceState = MutableStateFlow(getUserExperienceState())
    val userExperienceState: StateFlow<UserExperienceState> = _userExperienceState.asStateFlow()
    
    private fun getUserExperienceState(): UserExperienceState {
        return UserExperienceState(
            isFirstLaunch = true, // Will be loaded from DataStore
            hasCompletedOnboarding = false,
            hasSeenUnifiedDashboard = false,
            discoveredFeatures = emptySet(),
            lastSeenAppVersion = "1.0.0"
        )
    }
    
    /**
     * Load user experience state from Proto DataStore
     */
    suspend fun loadUserExperienceState(): UserExperienceState {
        val prefs = context.dataStore.data.first()
        return UserExperienceState(
            isFirstLaunch = prefs.firstLaunch,
            hasCompletedOnboarding = prefs.onboardingCompleted,
            hasSeenUnifiedDashboard = prefs.unifiedDashboardShown,
            discoveredFeatures = prefs.featuresDiscoveredList.toSet(),
            lastSeenAppVersion = prefs.appVersionSeen.ifEmpty { "1.0.0" }
        )
    }
    
    /**
     * Initialize and load state from DataStore
     */
    suspend fun initialize() {
        val state = loadUserExperienceState()
        _userExperienceState.value = state
    }
    
    /**
     * Mark onboarding as completed
     */
    suspend fun markOnboardingCompleted() {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setOnboardingCompleted(true)
                .setFirstLaunch(false)
                .build()
        }
        _userExperienceState.value = _userExperienceState.value.copy(
            hasCompletedOnboarding = true,
            isFirstLaunch = false
        )
    }
    
    /**
     * Mark app launch (no longer first launch)
     */
    suspend fun markAppLaunched() {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setFirstLaunch(false)
                .build()
        }
        _userExperienceState.value = _userExperienceState.value.copy(isFirstLaunch = false)
    }
    
    /**
     * Mark unified dashboard as seen
     */
    suspend fun markUnifiedDashboardSeen() {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setUnifiedDashboardShown(true)
                .build()
        }
        _userExperienceState.value = _userExperienceState.value.copy(hasSeenUnifiedDashboard = true)
    }
    
    /**
     * Add a discovered feature
     */
    suspend fun addDiscoveredFeature(feature: String) {
        val currentFeatures = _userExperienceState.value.discoveredFeatures
        if (!currentFeatures.contains(feature)) {
            val newFeatures = currentFeatures + feature
            context.dataStore.updateData { prefs ->
                prefs.toBuilder()
                    .clearFeaturesDiscovered()
                    .addAllFeaturesDiscovered(newFeatures)
                    .build()
            }
            _userExperienceState.value = _userExperienceState.value.copy(
                discoveredFeatures = newFeatures
            )
        }
    }
    
    /**
     * Update last seen app version
     */
    suspend fun updateLastSeenAppVersion(version: String) {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setAppVersionSeen(version)
                .build()
        }
        _userExperienceState.value = _userExperienceState.value.copy(lastSeenAppVersion = version)
    }
    
    /**
     * Reset all user experience data
     */
    suspend fun reset() {
        context.dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setOnboardingCompleted(false)
                .setFirstLaunch(true)
                .setUnifiedDashboardShown(false)
                .clearFeaturesDiscovered()
                .setAppVersionSeen("1.0.0")
                .build()
        }
        _userExperienceState.value = getUserExperienceState()
    }
    
    // Flow-based access for reactive UI
    val isFirstLaunchFlow = context.dataStore.data.map { it.firstLaunch }
    val hasCompletedOnboardingFlow = context.dataStore.data.map { it.onboardingCompleted }
    val hasSeenUnifiedDashboardFlow = context.dataStore.data.map { it.unifiedDashboardShown }
}

/**
 * Check if user should see onboarding (suspending function)
 */
suspend fun shouldShowOnboarding(): Boolean {
    val prefs = context.dataStore.data.first()
    return !prefs.onboardingCompleted
}

/**
 * Check if this is a first launch (suspending function)
 */
suspend fun isFirstLaunch(): Boolean {
    val prefs = context.dataStore.data.first()
    return prefs.firstLaunch
}

/**
 * Get user's preferred starting screen
 */
suspend fun getPreferredStartScreen(): String {
    val prefs = context.dataStore.data.first()
    
    return when {
        !prefs.onboardingCompleted -> "onboarding"
        !prefs.unifiedDashboardShown -> "unified_dashboard"
        else -> "unified_dashboard" // Default to unified experience
    }
}

/**
 * Get personalized recommendations based on discovered features
 */
suspend fun getPersonalizedRecommendations(): List<FeatureRecommendation> {
    val prefs = context.dataStore.data.first()
    val discoveredFeatures = prefs.featuresDiscoveredList.toSet()
    val recommendations = mutableListOf<FeatureRecommendation>()
    
    // Recommend based on what user hasn't discovered yet
    if (!discoveredFeatures.contains("bmi_calculator")) {
        recommendations.add(
            FeatureRecommendation(
                feature = "bmi_calculator",
                title = "BMI Calculator entdecken",
                description = "Verfolge deine Gewichtsziele mit unserem smarten BMI Rechner",
                priority = 8
            )
        )
    }
    
    if (!discoveredFeatures.contains("intervallfasten")) {
        recommendations.add(
            FeatureRecommendation(
                feature = "fasting",
                title = "Intervallfasten ausprobieren",
                description = "6 professionelle Fasten-Protokolle für optimale Gesundheit",
                priority = 9
            )
        )
    }
    
    if (!discoveredFeatures.contains("ai_personal_trainer")) {
        recommendations.add(
            FeatureRecommendation(
                feature = "ai_personal_trainer",
                title = "AI Personal Trainer",
                description = "Personalisierte Trainingspläne powered by KI",
                priority = 10
            )
        )
    }
    
    if (!discoveredFeatures.contains("barcode_scanner")) {
        recommendations.add(
            FeatureRecommendation(
                feature = "barcode_scanner",
                title = "Barcode Scanner nutzen",
                description = "Scanne Produkte für instant Nährwert-Information",
                priority = 7
            )
        )
    }
    
    if (!discoveredFeatures.contains("recipes")) {
        recommendations.add(
            FeatureRecommendation(
                feature = "recipes",
                title = "Gesunde Rezepte",
                description = "AI-generierte Rezepte passend zu deinen Zielen",
                priority = 6
            )
        )
    }
    
    return recommendations.sortedByDescending { it.priority }
}

/**
 * State representing user's experience progress
 */
data class UserExperienceState(
    val isFirstLaunch: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val hasSeenUnifiedDashboard: Boolean = false,
    val discoveredFeatures: Set<String> = emptySet(),
    val lastSeenAppVersion: String = "1.0.0"
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
    POWER_USER
}

/**
 * Feature recommendation for personalized suggestions
 */
data class FeatureRecommendation(
    val feature: String,
    val title: String,
    val description: String,
    val priority: Int
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
