package com.example.fitapp.util.time

import org.junit.Test
import org.junit.Assert.*
import java.time.*

/**
 * Tests for TimeZoneUtils with focus on DST transitions and timezone safety.
 */
class TimeZoneUtilsTest {
    
    @Test
    fun `getCurrentLocalDate returns correct date for system timezone`() {
        val result = TimeZoneUtils.getCurrentLocalDate()
        val expected = LocalDate.now(ZoneId.systemDefault())
        assertEquals(expected, result)
    }
    
    @Test
    fun `getCurrentLocalDate returns correct date for specific timezone`() {
        val berlinZone = ZoneId.of("Europe/Berlin")
        val result = TimeZoneUtils.getCurrentLocalDate(berlinZone)
        val expected = LocalDate.now(berlinZone)
        assertEquals(expected, result)
    }
    
    @Test
    fun `getStartOfDay handles system timezone correctly`() {
        val date = LocalDate.of(2024, 3, 15)
        val result = TimeZoneUtils.getStartOfDay(date)
        val expected = date.atStartOfDay(ZoneId.systemDefault())
        assertEquals(expected, result)
    }
    
    @Test
    fun `getStartOfDay handles specific timezone correctly`() {
        val date = LocalDate.of(2024, 3, 15)
        val zoneId = ZoneId.of("America/New_York")
        val result = TimeZoneUtils.getStartOfDay(date, zoneId)
        val expected = date.atStartOfDay(zoneId)
        assertEquals(expected, result)
    }
    
    @Test
    fun `getEndOfDay returns correct end time`() {
        val date = LocalDate.of(2024, 3, 15)
        val result = TimeZoneUtils.getEndOfDay(date)
        val expected = date.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault())
        assertEquals(expected, result)
    }
    
    @Test
    fun `isWithinDay returns true for datetime within day boundaries`() {
        val date = LocalDate.of(2024, 3, 15)
        val zonedDateTime = date.atTime(14, 30).atZone(ZoneId.systemDefault())
        assertTrue(TimeZoneUtils.isWithinDay(zonedDateTime, date))
    }
    
    @Test
    fun `isWithinDay returns false for datetime outside day boundaries`() {
        val date = LocalDate.of(2024, 3, 15)
        val nextDay = date.plusDays(1)
        val zonedDateTime = nextDay.atTime(1, 0).atZone(ZoneId.systemDefault())
        assertFalse(TimeZoneUtils.isWithinDay(zonedDateTime, date))
    }
    
    @Test
    fun `formatDate and parseDate are symmetric`() {
        val originalDate = LocalDate.of(2024, 3, 15)
        val formatted = TimeZoneUtils.formatDate(originalDate)
        val parsed = TimeZoneUtils.parseDate(formatted)
        assertEquals(originalDate, parsed)
    }
    
    @Test
    fun `daysBetween calculates correct number of days`() {
        val start = LocalDate.of(2024, 3, 15)
        val end = LocalDate.of(2024, 3, 20)
        val result = TimeZoneUtils.daysBetween(start, end)
        assertEquals(5L, result)
    }
    
    @Test
    fun `hasDayChanged returns true when days are different`() {
        val lastCheck = LocalDate.of(2024, 3, 15)
        val current = LocalDate.of(2024, 3, 16)
        assertTrue(TimeZoneUtils.hasDayChanged(lastCheck, current))
    }
    
    @Test
    fun `hasDayChanged returns false when days are same`() {
        val lastCheck = LocalDate.of(2024, 3, 15)
        val current = LocalDate.of(2024, 3, 15)
        assertFalse(TimeZoneUtils.hasDayChanged(lastCheck, current))
    }
    
    @Test
    fun `getNextDayBoundaryMillis returns correct timestamp`() {
        val tomorrow = TimeZoneUtils.getCurrentLocalDate().plusDays(1)
        val expectedMillis = TimeZoneUtils.getStartOfDay(tomorrow).toInstant().toEpochMilli()
        val result = TimeZoneUtils.getNextDayBoundaryMillis()
        assertEquals(expectedMillis, result)
    }
    
    @Test
    fun `isDstTransitionDay correctly identifies spring forward transition`() {
        val berlinZone = ZoneId.of("Europe/Berlin")
        val springForwardDate = LocalDate.of(2024, 3, 31) // DST starts in Europe
        val result = TimeZoneUtils.isDstTransitionDay(springForwardDate, berlinZone)
        assertTrue("Spring forward should be detected as DST transition", result)
    }
    
    @Test
    fun `isDstTransitionDay correctly identifies fall back transition`() {
        val berlinZone = ZoneId.of("Europe/Berlin")
        val fallBackDate = LocalDate.of(2024, 10, 27) // DST ends in Europe
        val result = TimeZoneUtils.isDstTransitionDay(fallBackDate, berlinZone)
        assertTrue("Fall back should be detected as DST transition", result)
    }
    
    @Test
    fun `isDstTransitionDay returns false for normal days`() {
        val berlinZone = ZoneId.of("Europe/Berlin")
        val normalDate = LocalDate.of(2024, 6, 15) // Summer day, no transition
        val result = TimeZoneUtils.isDstTransitionDay(normalDate, berlinZone)
        assertFalse("Normal day should not be detected as DST transition", result)
    }
    
    @Test
    fun `getDayDuration returns 23 hours for spring forward`() {
        val berlinZone = ZoneId.of("Europe/Berlin")
        val springForwardDate = LocalDate.of(2024, 3, 31)
        val duration = TimeZoneUtils.getDayDuration(springForwardDate, berlinZone)
        assertEquals("Spring forward day should be 23 hours", Duration.ofHours(23), duration)
    }
    
    @Test
    fun `getDayDuration returns 25 hours for fall back`() {
        val berlinZone = ZoneId.of("Europe/Berlin")
        val fallBackDate = LocalDate.of(2024, 10, 27)
        val duration = TimeZoneUtils.getDayDuration(fallBackDate, berlinZone)
        assertEquals("Fall back day should be 25 hours", Duration.ofHours(25), duration)
    }
    
    @Test
    fun `getDayDuration returns 24 hours for normal days`() {
        val berlinZone = ZoneId.of("Europe/Berlin")
        val normalDate = LocalDate.of(2024, 6, 15)
        val duration = TimeZoneUtils.getDayDuration(normalDate, berlinZone)
        assertEquals("Normal day should be 24 hours", Duration.ofHours(24), duration)
    }
    
    @Test
    fun `day boundaries are consistent during DST transitions`() {
        val berlinZone = ZoneId.of("Europe/Berlin")
        val springForwardDate = LocalDate.of(2024, 3, 31)
        
        val startOfDay = TimeZoneUtils.getStartOfDay(springForwardDate, berlinZone)
        val endOfDay = TimeZoneUtils.getEndOfDay(springForwardDate, berlinZone)
        
        // Start should always be before end
        assertTrue("Start of day should be before end of day", startOfDay.isBefore(endOfDay))
        
        // Both should be on the same date
        assertEquals("Start and end should be on same date", 
            startOfDay.toLocalDate(), 
            endOfDay.toLocalDate())
    }
}