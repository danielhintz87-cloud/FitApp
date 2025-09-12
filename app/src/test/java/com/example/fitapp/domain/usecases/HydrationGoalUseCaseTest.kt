package com.example.fitapp.domain.usecases

import com.example.fitapp.core.threading.TestDispatcherProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.WaterEntryDao
import com.example.fitapp.data.db.WaterEntryEntity
import com.example.fitapp.data.prefs.*
import com.example.fitapp.util.time.TimeZoneUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*
import java.time.LocalDate

/**
 * Unit tests for HydrationGoalUseCase focused on business logic without Android dependencies.
 * Tests reactive flows, timezone-safe day boundaries, and DataStore integration.
 */
@OptIn(ExperimentalCoroutinesApi::class) 
class HydrationGoalUseCaseTest {
    
    private lateinit var mockDatabase: AppDatabase
    private lateinit var mockWaterEntryDao: WaterEntryDao
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var testDispatcherProvider: TestDispatcherProvider
    private lateinit var hydrationGoalUseCase: HydrationGoalUseCase
    private lateinit var testScheduler: TestCoroutineScheduler
    
    @Before
    fun setup() {
        testScheduler = TestCoroutineScheduler()
        testDispatcherProvider = TestDispatcherProvider(testScheduler)
        
        // Mock database and DAO
        mockDatabase = mock()
        mockWaterEntryDao = mock()
        whenever(mockDatabase.waterEntryDao()).thenReturn(mockWaterEntryDao)
        
        // Mock UserPreferencesRepository
        userPreferencesRepository = mock()
        
        // Setup default mock behavior
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(
            flowOf(
                NutritionPreferences(
                    dailyCalorieGoal = 2000,
                    dailyWaterGoalLiters = 2.5,
                    nutritionRemindersEnabled = true
                )
            )
        )
        
        // Setup DAO mocks with default empty behavior
        whenever(mockWaterEntryDao.getTotalWaterForDateFlow(any())).thenReturn(flowOf(0))
        
        hydrationGoalUseCase = HydrationGoalUseCase(
            userPreferencesRepository = userPreferencesRepository,
            database = mockDatabase,
            dispatchers = testDispatcherProvider
        )
    }
    
    @After 
    fun tearDown() {
        // Nothing to clean up for unit tests
    }
    
    @Test
    fun `hydrationGoal flow emits current goal from preferences`() = runTest(testScheduler) {
        // Act
        val goal = hydrationGoalUseCase.hydrationGoal.first()
        
        // Assert
        assertEquals(2.5, goal, 0.01)
    }
    
    @Test
    fun `updateHydrationGoal updates preferences`() = runTest(testScheduler) {
        // Act
        hydrationGoalUseCase.updateHydrationGoal(3.0)
        testScheduler.advanceUntilIdle()
        
        // Assert
        verify(userPreferencesRepository).updateNutritionPreferences(
            dailyWaterGoalLiters = 3.0
        )
    }
    
    @Test
    fun `addWaterConsumption calls DAO insert`() = runTest(testScheduler) {
        // Act
        hydrationGoalUseCase.addWaterConsumption(500)
        testScheduler.advanceUntilIdle()
        
        // Assert
        verify(mockWaterEntryDao).insert(any<WaterEntryEntity>())
    }
    
    @Test
    fun `getWaterIntakeForDate calls DAO with correct date`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val expectedDateString = TimeZoneUtils.formatDate(testDate)
        whenever(mockWaterEntryDao.getTotalWaterForDate(expectedDateString)).thenReturn(1250)
        
        // Act
        val intake = hydrationGoalUseCase.getWaterIntakeForDate(testDate)
        
        // Assert
        assertEquals(1250, intake)
        verify(mockWaterEntryDao).getTotalWaterForDate(expectedDateString)
    }
    
    @Test
    fun `getWaterIntakeForDate returns zero when DAO returns null`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val expectedDateString = TimeZoneUtils.formatDate(testDate)
        whenever(mockWaterEntryDao.getTotalWaterForDate(expectedDateString)).thenReturn(null)
        
        // Act
        val intake = hydrationGoalUseCase.getWaterIntakeForDate(testDate)
        
        // Assert
        assertEquals(0, intake)
    }
    
    @Test
    fun `clearWaterForDate calls DAO with correct date`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val expectedDateString = TimeZoneUtils.formatDate(testDate)
        
        // Act
        hydrationGoalUseCase.clearWaterForDate(testDate)
        testScheduler.advanceUntilIdle()
        
        // Assert
        verify(mockWaterEntryDao).clearForDate(expectedDateString)
    }
    
    @Test
    fun `deleteWaterEntry calls DAO delete`() = runTest(testScheduler) {
        // Act
        hydrationGoalUseCase.deleteWaterEntry(123L)
        testScheduler.advanceUntilIdle()
        
        // Assert
        verify(mockWaterEntryDao).delete(123L)
    }
    
    @Test
    fun `updateWaterEntry calls DAO update`() = runTest(testScheduler) {
        // Arrange
        val entry = WaterEntryEntity(id = 1, date = "2024-03-15", amountMl = 500)
        
        // Act
        hydrationGoalUseCase.updateWaterEntry(entry)
        testScheduler.advanceUntilIdle()
        
        // Assert
        verify(mockWaterEntryDao).update(entry)
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
    fun `getHydrationStatusForDate calculates correct status`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val dateString = TimeZoneUtils.formatDate(testDate)
        whenever(mockWaterEntryDao.getTotalWaterForDate(dateString)).thenReturn(1250)
        
        // Act
        val status = hydrationGoalUseCase.getHydrationStatusForDate(testDate)
        
        // Assert
        assertEquals(testDate, status.currentDate)
        assertEquals(2.5, status.goalLiters, 0.01)
        assertEquals(1250, status.consumedMl)
        assertEquals(50f, status.progressPercentage, 0.01f)
        assertEquals(1250, status.remainingMl)
        assertFalse(status.isGoalReached)
    }
    
    @Test
    fun `getHydrationStatusForDate shows goal reached when consumption meets goal`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val dateString = TimeZoneUtils.formatDate(testDate)
        whenever(mockWaterEntryDao.getTotalWaterForDate(dateString)).thenReturn(2500) // Exactly meets goal
        
        // Act
        val status = hydrationGoalUseCase.getHydrationStatusForDate(testDate)
        
        // Assert
        assertEquals(2500, status.consumedMl)
        assertEquals(100f, status.progressPercentage, 0.01f)
        assertEquals(0, status.remainingMl)
        assertTrue(status.isGoalReached)
    }
    
    @Test
    fun `getHydrationStatusForDate shows goal exceeded when consumption exceeds goal`() = runTest(testScheduler) {
        // Arrange
        val testDate = LocalDate.of(2024, 3, 15)
        val dateString = TimeZoneUtils.formatDate(testDate)
        whenever(mockWaterEntryDao.getTotalWaterForDate(dateString)).thenReturn(3000) // Exceeds goal
        
        // Act
        val status = hydrationGoalUseCase.getHydrationStatusForDate(testDate)
        
        // Assert
        assertEquals(3000, status.consumedMl)
        assertEquals(120f, status.progressPercentage, 0.01f)
        assertEquals(0, status.remainingMl)
        assertTrue(status.isGoalReached)
    }
}