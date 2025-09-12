package com.example.fitapp.domain.usecases

import com.example.fitapp.core.threading.TestDispatcherProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.DailyGoalEntity
import com.example.fitapp.data.db.WaterEntryDao
import com.example.fitapp.data.db.WaterEntryEntity
import com.example.fitapp.data.prefs.*
import com.example.fitapp.data.repo.NutritionRepository
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
 * Tests reactive flows, timezone-safe day boundaries, DataStore integration, and priority logic.
 */
@OptIn(ExperimentalCoroutinesApi::class) 
class HydrationGoalUseCaseTest {
    
    private lateinit var mockDatabase: AppDatabase
    private lateinit var mockWaterEntryDao: WaterEntryDao
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var nutritionRepository: NutritionRepository
    private lateinit var testDispatcherProvider: TestDispatcherProvider
    private lateinit var hydrationGoalUseCase: HydrationGoalUseCase
    private lateinit var testScheduler: TestCoroutineScheduler
    
    private val testDate = LocalDate.of(2025, 1, 15)
    
    @Before
    fun setup() {
        testScheduler = TestCoroutineScheduler()
        testDispatcherProvider = TestDispatcherProvider(testScheduler)
        
        // Mock database and DAO
        mockDatabase = mock()
        mockWaterEntryDao = mock()
        whenever(mockDatabase.waterEntryDao()).thenReturn(mockWaterEntryDao)
        
        // Mock repositories
        userPreferencesRepository = mock()
        nutritionRepository = mock()
        
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
        whenever(nutritionRepository.goalFlow(any())).thenReturn(flowOf(null))
        
        hydrationGoalUseCase = HydrationGoalUseCase(
            userPreferencesRepository = userPreferencesRepository,
            nutritionRepository = nutritionRepository,
            database = mockDatabase,
            dispatchers = testDispatcherProvider
        )
    }
    
    @After 
    fun tearDown() {
        // Nothing to clean up for unit tests
    }
    
    // Tests for reactive flows and core functionality
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
    
    // Tests for hydration goal priority logic
    @Test
    fun `getHydrationGoalMl returns daily goal when available`() = runTest(testScheduler) {
        // Given: Daily goal with water target exists
        val dailyGoal = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = 2500,
        )
        whenever(nutritionRepository.goalFlow(testDate)).thenReturn(flowOf(dailyGoal))

        val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 3.0) // 3000ml
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

        // When: Getting hydration goal
        val result = hydrationGoalUseCase.getHydrationGoalMl(testDate)

        // Then: Daily goal takes priority (2500ml)
        assertEquals(2500, result)
    }

    @Test
    fun `getHydrationGoalMl returns user preference when daily goal is zero`() = runTest(testScheduler) {
        // Given: Daily goal exists but water target is zero
        val dailyGoal = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = 0,
        )
        whenever(nutritionRepository.goalFlow(testDate)).thenReturn(flowOf(dailyGoal))

        val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.5) // 2500ml
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

        // When: Getting hydration goal
        val result = hydrationGoalUseCase.getHydrationGoalMl(testDate)

        // Then: User preference is used (2500ml)
        assertEquals(2500, result)
    }

    @Test
    fun `getHydrationGoalMl returns user preference when daily goal is null`() = runTest(testScheduler) {
        // Given: Daily goal exists but water target is null
        val dailyGoal = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = null,
        )
        whenever(nutritionRepository.goalFlow(testDate)).thenReturn(flowOf(dailyGoal))

        val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 3.5) // 3500ml
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

        // When: Getting hydration goal
        val result = hydrationGoalUseCase.getHydrationGoalMl(testDate)

        // Then: User preference is used (3500ml)
        assertEquals(3500, result)
    }

    @Test
    fun `getHydrationGoalMl returns user preference when no daily goal`() = runTest(testScheduler) {
        // Given: No daily goal exists
        whenever(nutritionRepository.goalFlow(testDate)).thenReturn(flowOf(null))

        val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.8) // 2800ml
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

        // When: Getting hydration goal
        val result = hydrationGoalUseCase.getHydrationGoalMl(testDate)

        // Then: User preference is used (2800ml)
        assertEquals(2800, result)
    }

    @Test
    fun `getHydrationGoalMl returns default when user preference is zero`() = runTest(testScheduler) {
        // Given: No daily goal and user preference is zero
        whenever(nutritionRepository.goalFlow(testDate)).thenReturn(flowOf(null))

        val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 0.0)
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

        // When: Getting hydration goal
        val result = hydrationGoalUseCase.getHydrationGoalMl(testDate)

        // Then: Default is used (2000ml)
        assertEquals(HydrationGoalUseCase.DEFAULT_DAILY_WATER_GOAL_ML, result)
    }

    @Test
    fun `getTodaysHydrationGoalMl returns correct value for today`() = runTest(testScheduler) {
        // Given: Setup for today's date
        val today = LocalDate.now()
        val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.2) // 2200ml
        whenever(nutritionRepository.goalFlow(today)).thenReturn(flowOf(null))
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

        // When: Getting today's hydration goal
        val result = hydrationGoalUseCase.getTodaysHydrationGoalMl()

        // Then: User preference is used (2200ml)
        assertEquals(2200, result)
    }
    
    // Tests for water entry management
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
    
    // Tests for day rollover logic
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
    
    // Tests for hydration status calculation
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

    @Test
    fun `hydration goal priority is consistent`() = runTest(testScheduler) {
        // Test case: Daily goal takes priority even if lower than user preference
        val dailyGoalWithWater = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = 1800, // Lower than user pref, but still priority
        )
        whenever(nutritionRepository.goalFlow(testDate)).thenReturn(flowOf(dailyGoalWithWater))

        val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 3.0) // 3000ml, higher
        whenever(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

        val result = hydrationGoalUseCase.getHydrationGoalMl(testDate)
        assertEquals(1800, result) // Daily goal wins even if lower
    }
}
