package com.example.fitapp.util.time

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Utilities for timezone-safe date calculations, specifically handling DST transitions
 * and ensuring consistent day boundaries across time zone changes.
 */
object TimeZoneUtils {
    
    /**
     * Date formatter for consistent string representation
     */
    private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Gets the current local date using the system default timezone.
     * This ensures day boundaries are consistent with user's local time.
     */
    fun getCurrentLocalDate(): LocalDate {
        return LocalDate.now(ZoneId.systemDefault())
    }
    
    /**
     * Gets the current local date for a specific timezone.
     */
    fun getCurrentLocalDate(zoneId: ZoneId): LocalDate {
        return LocalDate.now(zoneId)
    }
    
    /**
     * Converts a LocalDate to the start of day (00:00:00) in the system timezone.
     * This handles DST transitions correctly.
     */
    fun getStartOfDay(date: LocalDate): ZonedDateTime {
        return date.atStartOfDay(ZoneId.systemDefault())
    }
    
    /**
     * Converts a LocalDate to the start of day (00:00:00) in a specific timezone.
     */
    fun getStartOfDay(date: LocalDate, zoneId: ZoneId): ZonedDateTime {
        return date.atStartOfDay(zoneId)
    }
    
    /**
     * Gets the end of day (23:59:59.999999999) for a LocalDate in the system timezone.
     */
    fun getEndOfDay(date: LocalDate): ZonedDateTime {
        return date.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault())
    }
    
    /**
     * Gets the end of day (23:59:59.999999999) for a LocalDate in a specific timezone.
     */
    fun getEndOfDay(date: LocalDate, zoneId: ZoneId): ZonedDateTime {
        return date.atTime(23, 59, 59, 999_999_999).atZone(zoneId)
    }
    
    /**
     * Checks if a given ZonedDateTime falls within the day boundaries of a LocalDate.
     * Handles DST transitions correctly.
     */
    fun isWithinDay(dateTime: ZonedDateTime, date: LocalDate): Boolean {
        val startOfDay = getStartOfDay(date, dateTime.zone)
        val endOfDay = getEndOfDay(date, dateTime.zone)
        return !dateTime.isBefore(startOfDay) && !dateTime.isAfter(endOfDay)
    }
    
    /**
     * Checks if a timestamp (in milliseconds) falls within a specific local date.
     * Uses system default timezone.
     */
    fun isWithinDay(timestampMillis: Long, date: LocalDate): Boolean {
        val zonedDateTime = Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault())
        return isWithinDay(zonedDateTime, date)
    }
    
    /**
     * Converts a LocalDate to a string representation for storage.
     */
    fun formatDate(date: LocalDate): String {
        return date.format(DATE_FORMATTER)
    }
    
    /**
     * Parses a date string back to LocalDate.
     */
    fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, DATE_FORMATTER)
    }
    
    /**
     * Gets the number of days between two LocalDates.
     */
    fun daysBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, endDate)
    }
    
    /**
     * Checks if we've crossed a day boundary since the last check.
     * This is DST-safe and handles timezone changes.
     */
    fun hasDayChanged(lastCheckDate: LocalDate, currentDate: LocalDate = getCurrentLocalDate()): Boolean {
        return lastCheckDate != currentDate
    }
    
    /**
     * Gets the next day boundary timestamp for scheduling.
     * Returns the exact millisecond when the next day starts.
     */
    fun getNextDayBoundaryMillis(): Long {
        val tomorrow = getCurrentLocalDate().plusDays(1)
        return getStartOfDay(tomorrow).toInstant().toEpochMilli()
    }
    
    /**
     * Gets the next day boundary timestamp for a specific timezone.
     */
    fun getNextDayBoundaryMillis(zoneId: ZoneId): Long {
        val tomorrow = getCurrentLocalDate(zoneId).plusDays(1)
        return getStartOfDay(tomorrow, zoneId).toInstant().toEpochMilli()
    }
    
    /**
     * Handles DST transition by checking if the day length is not 24 hours.
     * Returns true if we're in a DST transition day.
     */
    fun isDstTransitionDay(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
        val startOfDay = getStartOfDay(date, zoneId)
        val startOfNextDay = getStartOfDay(date.plusDays(1), zoneId)
        val duration = Duration.between(startOfDay, startOfNextDay)
        return duration != Duration.ofHours(24)
    }
    
    /**
     * Gets the actual duration of a specific day, accounting for DST transitions.
     */
    fun getDayDuration(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Duration {
        val startOfDay = getStartOfDay(date, zoneId)
        val startOfNextDay = getStartOfDay(date.plusDays(1), zoneId)
        return Duration.between(startOfDay, startOfNextDay)
    }
}