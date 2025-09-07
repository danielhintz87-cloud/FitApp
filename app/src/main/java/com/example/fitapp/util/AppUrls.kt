package com.example.fitapp.util

/**
 * Configuration for app URLs and external resources
 */
object AppUrls {
    
    // Privacy and Legal
    const val PRIVACY_POLICY_URL = "https://github.com/fitapp/privacy-policy"
    const val TERMS_OF_SERVICE_URL = "https://github.com/fitapp/terms-of-service"
    const val OPEN_SOURCE_LICENSES_URL = "https://github.com/fitapp/fitapp-android/blob/main/LICENSE"
    
    // Help and Support
    const val HELP_CENTER_URL = "https://github.com/fitapp/fitapp-android/wiki"
    const val FAQ_URL = "https://github.com/fitapp/fitapp-android/wiki/FAQ"
    const val TROUBLESHOOTING_URL = "https://github.com/fitapp/fitapp-android/wiki/Troubleshooting"
    
    // Community and Social
    const val GITHUB_REPOSITORY_URL = "https://github.com/fitapp/fitapp-android"
    const val GITHUB_ISSUES_URL = "https://github.com/fitapp/fitapp-android/issues"
    const val COMMUNITY_FORUM_URL = "https://github.com/fitapp/fitapp-android/discussions"
    
    // Health Connect specific
    const val HEALTH_CONNECT_PRIVACY_URL = "https://developer.android.com/health-and-fitness/guides/health-connect/privacy"
    const val HEALTH_CONNECT_HELP_URL = "https://support.google.com/health-connect"
    
    // Documentation
    const val API_DOCUMENTATION_URL = "https://github.com/fitapp/fitapp-android/wiki/API-Documentation"
    const val DEVELOPER_GUIDE_URL = "https://github.com/fitapp/fitapp-android/wiki/Developer-Guide"
    
    // Third-party acknowledgments
    const val MEDIAPIPE_LICENSE_URL = "https://github.com/google/mediapipe/blob/master/LICENSE"
    const val ML_KIT_TERMS_URL = "https://developers.google.com/ml-kit/terms"
    const val OPEN_FOOD_FACTS_URL = "https://openfoodfacts.org"
    
    /**
     * Get URL for specific feature help
     */
    fun getFeatureHelpUrl(feature: String): String {
        return when (feature) {
            "barcode_scanner" -> "$HELP_CENTER_URL/Barcode-Scanner"
            "voice_input" -> "$HELP_CENTER_URL/Voice-Input"
            "health_connect" -> HEALTH_CONNECT_HELP_URL
            "workout_tracking" -> "$HELP_CENTER_URL/Workout-Tracking"
            "nutrition" -> "$HELP_CENTER_URL/Nutrition-Tracking"
            "pose_detection" -> "$HELP_CENTER_URL/Pose-Detection"
            else -> HELP_CENTER_URL
        }
    }
    
    /**
     * Check if URL is available (for fallback scenarios)
     */
    fun isUrlAvailable(url: String): Boolean {
        return try {
            // Basic URL validation
            android.webkit.URLUtil.isValidUrl(url)
        } catch (e: Exception) {
            false
        }
    }
}
