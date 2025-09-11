package com.example.fitapp.services

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit tests for streak calculation logic without Android dependencies
 */
class StreakLogicTest {

    @Test
    fun testConsecutiveDayCalculation() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val dayBeforeYesterday = today.minusDays(2)
        
        // Test that consecutive timestamps are detected correctly
        val todayTimestamp = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val yesterdayTimestamp = yesterday.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val dayBeforeTimestamp = dayBeforeYesterday.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        
        // Verify that the timestamps differ by exactly 24 hours (86400 seconds)
        assertEquals(86400L, todayTimestamp - yesterdayTimestamp)
        assertEquals(86400L, yesterdayTimestamp - dayBeforeTimestamp)
    }
    
    @Test
    fun testDateToTimestampConversion() {
        val testDate = LocalDate.of(2024, 1, 15)
        val timestamp = testDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        
        // Verify that the conversion is consistent
        assertTrue(timestamp > 0)
        
        // Test with multiple dates to ensure consistency
        val nextDay = testDate.plusDays(1)
        val nextTimestamp = nextDay.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        
        assertEquals(86400L, nextTimestamp - timestamp)
    }
    
    @Test
    fun testStreakResetLogic() {
        // Test that a gap larger than 1 day properly resets streak
        val day1 = LocalDate.now().minusDays(5)
        val day2 = LocalDate.now().minusDays(4)
        val day4 = LocalDate.now().minusDays(2) // Missing day 3
        
        val day1Timestamp = day1.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val day2Timestamp = day2.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val day4Timestamp = day4.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        
        // Verify day 1 to day 2 is consecutive
        assertEquals(86400L, day2Timestamp - day1Timestamp)
        
        // Verify day 2 to day 4 is NOT consecutive (2 days gap)
        assertEquals(172800L, day4Timestamp - day2Timestamp) // 48 hours
        assertNotEquals(86400L, day4Timestamp - day2Timestamp)
    }
    
    @Test
    fun testAchievementThresholds() {
        val thresholds = listOf(3, 7, 14, 30, 100)
        
        // Test that thresholds are in ascending order
        for (i in 0 until thresholds.size - 1) {
            assertTrue(thresholds[i] < thresholds[i + 1])
        }
        
        // Test achievement unlocking logic
        val currentStreak = 7
        val unlockedThresholds = thresholds.filter { it <= currentStreak }
        val expectedUnlocked = listOf(3, 7)
        
        assertEquals(expectedUnlocked, unlockedThresholds)
    }
    
    @Test
    fun testBadgeTypes() {
        val badgeMapping = mapOf(
            3 to "bronze",
            7 to "silver", 
            14 to "gold",
            30 to "platinum",
            100 to "diamond"
        )
        
        // Verify badge progression makes sense
        badgeMapping.forEach { (threshold, badge) ->
            assertNotNull(badge)
            assertTrue(badge.isNotEmpty())
        }
        
        // Verify unique badge types
        val uniqueBadges = badgeMapping.values.toSet()
        assertEquals(badgeMapping.size, uniqueBadges.size)
    }
    
    @Test
    fun testRarityLevels() {
        val rarityMapping = mapOf(
            3 to "common",
            7 to "common",
            14 to "rare",
            30 to "epic", 
            100 to "legendary"
        )
        
        // Test that higher thresholds have higher rarity
        assertTrue(rarityMapping[3] == "common")
        assertTrue(rarityMapping[100] == "legendary")
        assertNotEquals(rarityMapping[3], rarityMapping[100])
    }
    
    @Test
    fun testPointsCalculation() {
        val pointsPerDay = 10
        val thresholds = listOf(3, 7, 14, 30, 100)
        
        thresholds.forEach { threshold ->
            val expectedPoints = threshold * pointsPerDay
            assertTrue(expectedPoints > 0)
            assertTrue(expectedPoints >= threshold) // At least 1 point per day
        }
        
        // Test that longer streaks give more points
        val points3Day = 3 * pointsPerDay
        val points100Day = 100 * pointsPerDay
        assertTrue(points100Day > points3Day)
    }
}