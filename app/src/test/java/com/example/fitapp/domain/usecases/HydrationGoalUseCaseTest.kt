package com.example.fitapp.domain.usecases

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitapp.core.threading.TestDispatcherProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.WaterEntryEntity
import com.example.fitapp.data.prefs.*
import com.example.fitapp.util.time.TimeZoneUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.mockito.kotlin.*
import java.time.LocalDate

/**
 * Unit tests for HydrationGoalUseCase.
 * Tests reactive flows, timezone-safe day boundaries, and DataStore integration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HydrationGoalUseCaseTest {
    
    private lateinit var database: AppDatabase
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var testDispatcherProvider: TestDispatcherProvider
    private lateinit var hydrationGoalUseCase: HydrationGoalUseCase
    private lateinit var testScheduler: TestCoroutineScheduler
    
    @Before
    fun setup() {
        testScheduler = TestCoroutineScheduler()
        testDispatcherProvider = TestDispatcherProvider(testScheduler)
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        // Mock UserPreferencesRepository
        userPreferencesRepository = mock()
        
        // Setup default mock behavior
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(
            kotlinx.coroutines.flow.flowOf(
                NutritionPreferences(
                    dailyCalorieGoal = 2000,
                    dailyWaterGoalLiters = 2.5,
                    nutritionRemindersEnabled = true
                )
            )
        )
        
        hydrationGoalUseCase = HydrationGoalUseCase(
            userPreferencesRepository = userPreferencesRepository,
            database = database,
            dispatchers = testDispatcherProvider
        )
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun `hydrationGoal flow emits current goal from preferences`() = runTest(testScheduler) {
        // Act
        val goal = hydrationGoalUseCase.hydrationGoal.first()
        
        // Assert
        assertEquals(2.5, goal, 0.01)
    }
    
    @Test
    fun `hydrationStatus calculates correct progress for empty day`() = runTest(testScheduler) {
        // Act
        val status = hydrationGoalUseCase.hydrationStatus.first()
        
        // Assert
        assertEquals(TimeZoneUtils.getCurrentLocalDate(), status.currentDate)
        assertEquals(2.5, status.goalLiters, 0.01)
        assertEquals(2500, status.goalMl)
        assertEquals(0, status.consumedMl)
        assertEquals(0f, status.progressPercentage, 0.01f)
        assertEquals(2500, status.remainingMl)
        assertFalse(status.isGoalReached)
    }
    
    @Test
    fun `hydrationStatus calculates correct progress with water entries`() = runTest(testScheduler) {
        // Arrange
        val today = TimeZoneUtils.getCurrentLocalDate()
        val dateString = TimeZoneUtils.formatDate(today)
        
        // Add water entries
        database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 500))
        database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 750))
        
        // Act
        val status = hydrationGoalUseCase.hydrationStatus.first()
        
        // Assert
        assertEquals(1250, status.consumedMl)
        assertEquals(50f, status.progressPercentage, 0.01f)
        assertEquals(1250, status.remainingMl)
        assertFalse(status.isGoalReached)
    }
    
    @Test
    fun `hydrationStatus shows goal reached when consumption exceeds goal`() = runTest(testScheduler) {
        // Arrange
        val today = TimeZoneUtils.getCurrentLocalDate()
        val dateString = TimeZoneUtils.formatDate(today)
        
        // Add water entry that exceeds goal
        database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 3000))
        
        // Act
        val status = hydrationGoalUseCase.hydrationStatus.first()
        
        // Assert
        assertEquals(3000, status.consumedMl)
        assertEquals(120f, status.progressPercentage, 0.01f)
        assertEquals(0, status.remainingMl)
        assertTrue(status.isGoalReached)
    }
    
    @Test
    fun `addWaterConsumption adds entry for today`() = runTest(testScheduler) {
        // Act
        hydrationGoalUseCase.addWaterConsumption(500)
        
        // Assert
        val today = TimeZoneUtils.getCurrentLocalDate()
        val entries = hydrationGoalUseCase.getWaterEntriesForDate(today)
        assertEquals(1, entries.size)
        assertEquals(500, entries[0].amountMl)
        assertEquals(TimeZoneUtils.formatDate(today), entries[0].date)
    }
    
    @Test
    fun `updateHydrationGoal updates preferences`() = runTest(testScheduler) {
        // Act
        hydrationGoalUseCase.updateHydrationGoal(3.0)
        
        // Assert
        verify(userPreferencesRepository).updateNutritionPreferences(
            dailyWaterGoalLiters = 3.0
        )
    }
    
    @Test
    fun `getWaterIntakeForDate returns correct amount for specific date`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val dateString = TimeZoneUtils.formatDate(testDate)
        
        database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 400))
        database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 600))
        
        // Act
        val intake = hydrationGoalUseCase.getWaterIntakeForDate(testDate)
        
        // Assert
        assertEquals(1000, intake)
    }
    
    @Test
    fun `getWaterIntakeForDate returns zero for date with no entries`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        
        // Act
        val intake = hydrationGoalUseCase.getWaterIntakeForDate(testDate)
        
        // Assert
        assertEquals(0, intake)
    }
    
    @Test
    fun `getHydrationStatusForDate calculates status for specific date`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val dateString = TimeZoneUtils.formatDate(testDate)
        
        database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 1250))
        
        // Act
        val status = hydrationGoalUseCase.getHydrationStatusForDate(testDate)
        
        // Assert
        assertEquals(testDate, status.currentDate)
        assertEquals(1250, status.consumedMl)
        assertEquals(50f, status.progressPercentage, 0.01f)
        assertEquals(1250, status.remainingMl)
        assertFalse(status.isGoalReached)
    }
    
    @Test
    fun `clearWaterForDate removes all entries for specific date`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val dateString = TimeZoneUtils.formatDate(testDate)
        
        database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 500))
        database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 750))
        
        // Act
        hydrationGoalUseCase.clearWaterForDate(testDate)
        
        // Assert
        val entries = hydrationGoalUseCase.getWaterEntriesForDate(testDate)
        assertTrue(entries.isEmpty())
    }
    
    @Test
    fun `handleDayRollover returns true for first check`() = runTest(testScheduler) {
        // Act
        val result = hydrationGoalUseCase.handleDayRollover(null)
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `handleDayRollover returns true when day has changed`() = runTest(testScheduler) {
        // Arrange
        val yesterday = TimeZoneUtils.getCurrentLocalDate().minusDays(1)
        
        // Act
        val result = hydrationGoalUseCase.handleDayRollover(yesterday)
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `handleDayRollover returns false when day has not changed`() = runTest(testScheduler) {
        // Arrange
        val today = TimeZoneUtils.getCurrentLocalDate()
        
        // Act
        val result = hydrationGoalUseCase.handleDayRollover(today)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `getHydrationProgressHistory returns correct history`() = runTest(testScheduler) {
        // Arrange
        val today = TimeZoneUtils.getCurrentLocalDate()
        val yesterday = today.minusDays(1)
        val dayBeforeYesterday = today.minusDays(2)
        
        // Add entries for different days
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(today), 
            amountMl = 2500
        ))
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(yesterday), 
            amountMl = 1800
        ))
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(dayBeforeYesterday), 
            amountMl = 2200
        ))
        
        // Act
        val history = hydrationGoalUseCase.getHydrationProgressHistory(3)
        
        // Assert
        assertEquals(3, history.size)
        
        // Check chronological order (oldest first)
        assertEquals(dayBeforeYesterday, history[0].currentDate)
        assertEquals(2200, history[0].consumedMl)
        
        assertEquals(yesterday, history[1].currentDate)
        assertEquals(1800, history[1].consumedMl)
        
        assertEquals(today, history[2].currentDate)
        assertEquals(2500, history[2].consumedMl)
        assertTrue(history[2].isGoalReached)
    }
    
    @Test
    fun `getAverageConsumption calculates correct average`() = runTest(testScheduler) {
        // Arrange
        val today = TimeZoneUtils.getCurrentLocalDate()
        val yesterday = today.minusDays(1)
        val dayBeforeYesterday = today.minusDays(2)
        
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(today), 
            amountMl = 2400
        ))
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(yesterday), 
            amountMl = 1800
        ))
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(dayBeforeYesterday), 
            amountMl = 2100
        ))
        
        // Act
        val average = hydrationGoalUseCase.getAverageConsumption(3)
        
        // Assert
        assertEquals(2100.0, average, 0.01)
    }
    
    @Test
    fun `getGoalAchievementRate calculates correct percentage`() = runTest(testScheduler) {
        // Arrange (goal is 2500ml)
        val today = TimeZoneUtils.getCurrentLocalDate()
        val yesterday = today.minusDays(1)
        val dayBeforeYesterday = today.minusDays(2)
        
        // 2 out of 3 days reach goal
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(today), 
            amountMl = 2600 // Reaches goal
        ))
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(yesterday), 
            amountMl = 1800 // Doesn't reach goal
        ))
        database.waterEntryDao().insert(WaterEntryEntity(
            date = TimeZoneUtils.formatDate(dayBeforeYesterday), 
            amountMl = 2500 // Reaches goal
        ))
        
        // Act
        val achievementRate = hydrationGoalUseCase.getGoalAchievementRate(3)
        
        // Assert
        assertEquals(66.67f, achievementRate, 0.01f)
    }
    
    @Test
    fun `deleteWaterEntry removes specific entry`() = runTest(testScheduler) {
        // Arrange
        val today = TimeZoneUtils.getCurrentLocalDate()
        val dateString = TimeZoneUtils.formatDate(today)
        
        val id1 = database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 500))
        val id2 = database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 750))
        
        // Act
        hydrationGoalUseCase.deleteWaterEntry(id1)
        
        // Assert
        val entries = hydrationGoalUseCase.getWaterEntriesForDate(today)
        assertEquals(1, entries.size)
        assertEquals(id2, entries[0].id)
        assertEquals(750, entries[0].amountMl)
    }
    
    @Test
    fun `updateWaterEntry modifies existing entry`() = runTest(testScheduler) {
        // Arrange
        val today = TimeZoneUtils.getCurrentLocalDate()
        val dateString = TimeZoneUtils.formatDate(today)
        
        val id = database.waterEntryDao().insert(WaterEntryEntity(date = dateString, amountMl = 500))
        val originalEntry = database.waterEntryDao().getByDate(dateString)[0]
        
        // Act
        val updatedEntry = originalEntry.copy(amountMl = 750)
        hydrationGoalUseCase.updateWaterEntry(updatedEntry)
        
        // Assert
        val entries = hydrationGoalUseCase.getWaterEntriesForDate(today)
        assertEquals(1, entries.size)
        assertEquals(750, entries[0].amountMl)
    }
}