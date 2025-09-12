package com.example.fitapp.domain

/**
 * Enum representing different time periods for analytics data
 */
enum class AnalyticsPeriod(val days: Int, val displayName: String) {
    WEEK(7, "Woche"),
    MONTH(30, "Monat"),
    QUARTER(90, "Quartal"),
    YEAR(365, "Jahr"),
}
