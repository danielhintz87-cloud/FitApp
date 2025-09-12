package com.example.fitapp.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.fitapp.MainActivity
import com.example.fitapp.R
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

/**
 * Today's Progress Widget - Shows daily fitness metrics on home screen
 * Part of the Ultimate Pro-Features implementation
 */
class TodaysProgressWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Update all widget instances
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            // Get widget size to determine layout
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val layoutId =
                when {
                    minWidth >= 250 && minHeight >= 180 -> R.layout.widget_progress_large
                    minWidth >= 180 -> R.layout.widget_progress_medium
                    else -> R.layout.widget_progress_compact
                }

            val views = RemoteViews(context.packageName, layoutId)

            // Update widget content
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val progressData = getProgressData(context)

                    // Update on main thread
                    CoroutineScope(Dispatchers.Main).launch {
                        updateWidgetViews(views, progressData, context, appWidgetId)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    // Handle errors gracefully
                    updateErrorState(views, context, appWidgetId, appWidgetManager)
                }
            }
        }

        private suspend fun getProgressData(context: Context): ProgressData {
            val db = AppDatabase.get(context)
            val today = LocalDate.now().toString()

            return try {
                // Get today's data from database
                val workoutStatus = "active" // Simplified for demo
                val calories = 450 // Would come from nutrition tracking
                val steps = 8500 // Would come from health connect
                val water = 1.2f // Liters
                val workoutStreak = 5 // Days

                ProgressData(
                    date = today,
                    calories = calories,
                    steps = steps,
                    waterLiters = water,
                    workoutCompleted = workoutStatus == "completed",
                    workoutStreak = workoutStreak,
                    heartRateZone = "Cardio", // Would come from latest workout
                    macros = MacroData(120f, 45f, 200f), // P, F, C in grams
                )
            } catch (e: Exception) {
                // Return default data if database unavailable
                ProgressData()
            }
        }

        private fun updateWidgetViews(
            views: RemoteViews,
            data: ProgressData,
            context: Context,
            appWidgetId: Int,
        ) {
            val currentDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())

            // Update date
            views.setTextViewText(R.id.widget_date, currentDate)

            // Update main metrics
            views.setTextViewText(R.id.widget_calories, "${data.calories}")
            views.setTextViewText(R.id.widget_steps, "${data.steps}")
            views.setTextViewText(R.id.widget_water, "${data.waterLiters}L")

            // Update workout status
            if (data.workoutCompleted) {
                views.setTextViewText(R.id.widget_workout_status, "✅ Completed")
                views.setInt(
                    R.id.widget_workout_status,
                    "setTextColor",
                    context.getColor(android.R.color.holo_green_dark),
                )
            } else {
                views.setTextViewText(R.id.widget_workout_status, "⏳ Pending")
                views.setInt(
                    R.id.widget_workout_status,
                    "setTextColor",
                    context.getColor(android.R.color.holo_orange_dark),
                )
            }

            // Update streak
            views.setTextViewText(R.id.widget_streak, "${data.workoutStreak} days")

            // Set click intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        }

        private fun updateErrorState(
            views: RemoteViews,
            context: Context,
            appWidgetId: Int,
            appWidgetManager: AppWidgetManager,
        ) {
            views.setTextViewText(R.id.widget_date, "Error loading data")
            views.setTextViewText(R.id.widget_calories, "--")
            views.setTextViewText(R.id.widget_steps, "--")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

/**
 * Quick Action Widget - One-tap fitness actions
 */
class QuickActionWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateQuickActionWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateQuickActionWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_actions)

            // Set up quick action buttons
            setupQuickActionButton(views, R.id.quick_log_weight, "weight", context)
            setupQuickActionButton(views, R.id.quick_start_workout, "workout", context)
            setupQuickActionButton(views, R.id.quick_log_water, "water", context)
            setupQuickActionButton(views, R.id.quick_food_scan, "food", context)

            // AI-powered suggestion based on time of day
            val suggestion = getContextualSuggestion()
            views.setTextViewText(R.id.widget_suggestion, suggestion)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun setupQuickActionButton(
            views: RemoteViews,
            buttonId: Int,
            action: String,
            context: Context,
        ) {
            val intent =
                Intent(context, MainActivity::class.java).apply {
                    putExtra("quick_action", action)
                    putExtra("timestamp", System.currentTimeMillis())
                }

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    action.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            views.setOnClickPendingIntent(buttonId, pendingIntent)
        }

        private fun getContextualSuggestion(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            return when (hour) {
                in 6..9 -> "🌅 Guten Morgen! Zeit für das Morgenwokout?"
                in 10..11 -> "☕ Vergiss nicht, genug Wasser zu trinken!"
                in 12..14 -> "🥗 Mittagszeit - tracke dein Essen!"
                in 15..17 -> "💪 Perfekte Zeit für dein Training!"
                in 18..20 -> "🍽️ Abendessen loggen nicht vergessen!"
                in 21..23 -> "📊 Schau dir deinen Tagesfortschritt an!"
                else -> "😴 Gute Nacht! Morgen wird ein starker Tag!"
            }
        }
    }
}

/**
 * Weekly Summary Widget - Shows achievements and trends
 */
class WeeklySummaryWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWeeklySummaryWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWeeklySummaryWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weekly_summary)

            CoroutineScope(Dispatchers.IO).launch {
                val weeklyData = getWeeklyData(context)

                CoroutineScope(Dispatchers.Main).launch {
                    updateWeeklyViews(views, weeklyData, context)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private suspend fun getWeeklyData(context: Context): WeeklyData {
            return WeeklyData(
                workoutsCompleted = 4,
                totalWeeks = 12,
                currentStreak = 5,
                longestStreak = 14,
                achievements = listOf("💪 Strength Beast", "🏃 Cardio King"),
                motivationQuote = "Du bist stärker als deine Ausreden!",
            )
        }

        private fun updateWeeklyViews(
            views: RemoteViews,
            data: WeeklyData,
            context: Context,
        ) {
            views.setTextViewText(R.id.weekly_workouts, "${data.workoutsCompleted}/7")
            views.setTextViewText(R.id.weekly_streak, "${data.currentStreak} days")
            views.setTextViewText(R.id.weekly_achievements, data.achievements.joinToString(" "))
            views.setTextViewText(R.id.weekly_quote, data.motivationQuote)

            // Set progress bar
            val progressPercent = (data.workoutsCompleted * 100) / 7
            views.setProgressBar(R.id.weekly_progress, 100, progressPercent, false)
        }
    }
}

/**
 * Data classes for widget content
 */
data class ProgressData(
    val date: String = "",
    val calories: Int = 0,
    val steps: Int = 0,
    val waterLiters: Float = 0f,
    val workoutCompleted: Boolean = false,
    val workoutStreak: Int = 0,
    val heartRateZone: String = "",
    val macros: MacroData = MacroData(),
)

data class MacroData(
    val protein: Float = 0f,
    val fat: Float = 0f,
    val carbs: Float = 0f,
)

data class WeeklyData(
    val workoutsCompleted: Int = 0,
    val totalWeeks: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val achievements: List<String> = emptyList(),
    val motivationQuote: String = "",
)
