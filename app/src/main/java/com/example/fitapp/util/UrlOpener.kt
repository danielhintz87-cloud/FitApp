package com.example.fitapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Utility for opening URLs with fallback options
 */
object UrlOpener {
    private const val TAG = "UrlOpener"

    /**
     * Open URL with Chrome Custom Tabs, fallback to browser
     */
    fun openUrl(
        context: Context,
        url: String,
        title: String = "",
    ) {
        try {
            if (!AppUrls.isUrlAvailable(url)) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.USER_ACTION,
                    TAG,
                    "Invalid URL attempted: $url",
                )
                return
            }

            val uri = Uri.parse(url)

            // Try Chrome Custom Tabs first
            try {
                val customTabsIntent =
                    CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                customTabsIntent.launchUrl(context, uri)

                StructuredLogger.info(
                    StructuredLogger.LogCategory.USER_ACTION,
                    TAG,
                    "Opened URL with Custom Tabs: $url",
                )
            } catch (e: Exception) {
                // Fallback to default browser
                val intent =
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(intent)

                StructuredLogger.info(
                    StructuredLogger.LogCategory.USER_ACTION,
                    TAG,
                    "Opened URL with default browser: $url",
                )
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Failed to open URL: $url",
                exception = e,
            )
        }
    }

    /**
     * Open privacy policy
     */
    fun openPrivacyPolicy(context: Context) {
        openUrl(context, AppUrls.PRIVACY_POLICY_URL, "Datenschutzerklärung")
    }

    /**
     * Open help for specific feature
     */
    fun openFeatureHelp(
        context: Context,
        feature: String,
    ) {
        val url = AppUrls.getFeatureHelpUrl(feature)
        openUrl(context, url, "Hilfe: $feature")
    }

    /**
     * Open GitHub repository
     */
    fun openGitHub(context: Context) {
        openUrl(context, AppUrls.GITHUB_REPOSITORY_URL, "FitApp GitHub")
    }

    /**
     * Open community forum
     */
    fun openCommunity(context: Context) {
        openUrl(context, AppUrls.COMMUNITY_FORUM_URL, "Community Forum")
    }

    /**
     * Open issue tracker
     */
    fun openIssueTracker(context: Context) {
        openUrl(context, AppUrls.GITHUB_ISSUES_URL, "Feedback & Issues")
    }

    /**
     * Share app with others
     */
    fun shareApp(
        context: Context,
        appName: String = "FitApp",
    ) {
        try {
            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, appName)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Schau dir $appName an - eine vollständige Fitness-App mit KI-Coach!\n\n${AppUrls.GITHUB_REPOSITORY_URL}",
                    )
                }

            val chooser = Intent.createChooser(shareIntent, "App teilen")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            StructuredLogger.info(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Shared app via system chooser",
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Failed to share app",
                exception = e,
            )
        }
    }
}
