package com.example.fitapp.util.time

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
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
}

/**
 * Parameterized tests for DST transitions across different timezones.
 */
@RunWith(Parameterized::class)
class TimeZoneUtilsDstTest(
    private val zoneId: ZoneId,
    private val dstTransitionDate: LocalDate,
    private val isDstTransition: Boolean
) {
    
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} on {1}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                // Spring forward (DST starts) - 23 hours
                arrayOf(ZoneId.of("Europe/Berlin"), LocalDate.of(2024, 3, 31), true),
                arrayOf(ZoneId.of("America/New_York"), LocalDate.of(2024, 3, 10), true),
                
                // Fall back (DST ends) - 25 hours  
                arrayOf(ZoneId.of("Europe/Berlin"), LocalDate.of(2024, 10, 27), true),
                arrayOf(ZoneId.of("America/New_York"), LocalDate.of(2024, 11, 3), true),
                
                // Normal days - 24 hours
                arrayOf(ZoneId.of("Europe/Berlin"), LocalDate.of(2024, 6, 15), false),
                arrayOf(ZoneId.of("America/New_York"), LocalDate.of(2024, 7, 4), false),
                
                // UTC never has DST transitions
                arrayOf(ZoneId.of("UTC"), LocalDate.of(2024, 3, 31), false),
                arrayOf(ZoneId.of("UTC"), LocalDate.of(2024, 10, 27), false)
            )
        }
    }
    
    @Test
    fun `isDstTransitionDay correctly identifies DST transitions`() {
        val result = TimeZoneUtils.isDstTransitionDay(dstTransitionDate, zoneId)
        assertEquals("DST transition detection for $zoneId on $dstTransitionDate", isDstTransition, result)
    }
    
    @Test
    fun `getDayDuration returns correct duration for DST transitions`() {
        val duration = TimeZoneUtils.getDayDuration(dstTransitionDate, zoneId)
        
        when {
            isDstTransition && isSpringForward() -> {
                // Spring forward: day is 23 hours
                assertEquals("Spring forward day should be 23 hours", Duration.ofHours(23), duration)
            }
            isDstTransition && !isSpringForward() -> {
                // Fall back: day is 25 hours
                assertEquals("Fall back day should be 25 hours", Duration.ofHours(25), duration)
            }
            else -> {
                // Normal day: 24 hours
                assertEquals("Normal day should be 24 hours", Duration.ofHours(24), duration)
            }
        }
    }
    
    @Test
    fun `day boundaries are consistent during DST transitions`() {
        val startOfDay = TimeZoneUtils.getStartOfDay(dstTransitionDate, zoneId)
        val endOfDay = TimeZoneUtils.getEndOfDay(dstTransitionDate, zoneId)
        
        // Start should always be before end
        assertTrue("Start of day should be before end of day", startOfDay.isBefore(endOfDay))
        
        // Both should be on the same date
        assertEquals("Start and end should be on same date", 
            startOfDay.toLocalDate(), 
            endOfDay.toLocalDate())
    }
    
    private fun isSpringForward(): Boolean {
        // Spring forward typically happens in March/April in Northern Hemisphere
        return dstTransitionDate.monthValue in 3..4
    }
}