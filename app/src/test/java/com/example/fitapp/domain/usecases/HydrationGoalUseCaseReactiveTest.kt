package com.example.fitapp.domain.usecases

import com.example.fitapp.data.db.DailyGoalEntity
import com.example.fitapp.data.prefs.NutritionPreferences
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.LocalDate

/**
 * Tests for reactive flow behavior in HydrationGoalUseCase.
 * Verifies that UI updates immediately when hydration goals change.
 */
class HydrationGoalUseCaseReactiveTest {

    @Mock
    private lateinit var nutritionRepository: NutritionRepository

    @Mock
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private lateinit var hydrationGoalUseCase: HydrationGoalUseCase
    
    private val testDate = LocalDate.of(2025, 1, 15)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        hydrationGoalUseCase = HydrationGoalUseCase(nutritionRepository, userPreferencesRepository, null)
    }

    @Test
    fun `reactive flow emits initial value immediately`() = runTest {
        // Given: Initial daily goal and preferences
        val dailyGoal = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = 2500
        )
        `when`(nutritionRepository.goalFlow(testDate)).thenReturn(MutableStateFlow(dailyGoal))
        
        val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.0)
        `when`(userPreferencesRepository.nutritionPreferences).thenReturn(MutableStateFlow(nutritionPrefs))

        // When: Subscribing to reactive flow
        val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(testDate)
        val firstValue = flow.first()

        // Then: Should emit initial value immediately
        assertEquals(2500, firstValue)
    }

    @Test
    fun `reactive flow updates when daily goal changes`() = runTest {
        // Given: Mutable flows for testing updates
        val dailyGoalFlow = MutableStateFlow<DailyGoalEntity?>(null)
        val nutritionPrefsFlow = MutableStateFlow(NutritionPreferences(dailyWaterGoalLiters = 2.0))
        
        `when`(nutritionRepository.goalFlow(testDate)).thenReturn(dailyGoalFlow)
        `when`(userPreferencesRepository.nutritionPreferences).thenReturn(nutritionPrefsFlow)

        // When: Starting flow subscription
        val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(testDate)
        val values = mutableListOf<Int>()
        
        // Collect first value (fallback to preferences)
        values.add(flow.first())
        
        // Update daily goal
        val newDailyGoal = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = 3000
        )
        dailyGoalFlow.value = newDailyGoal
        
        // Collect updated value
        values.add(flow.first())

        // Then: Should see both initial and updated values
        assertEquals(2000, values[0]) // Initial: preferences (2.0L = 2000ml)
        assertEquals(3000, values[1]) // Updated: daily goal takes priority
    }

    @Test
    fun `reactive flow updates when user preferences change`() = runTest {
        // Given: No daily goal, only preferences
        val dailyGoalFlow = MutableStateFlow<DailyGoalEntity?>(null)
        val nutritionPrefsFlow = MutableStateFlow(NutritionPreferences(dailyWaterGoalLiters = 2.0))
        
        `when`(nutritionRepository.goalFlow(testDate)).thenReturn(dailyGoalFlow)
        `when`(userPreferencesRepository.nutritionPreferences).thenReturn(nutritionPrefsFlow)

        val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(testDate)
        val values = mutableListOf<Int>()
        
        // Collect initial value
        values.add(flow.first())
        
        // Update user preferences
        nutritionPrefsFlow.value = NutritionPreferences(dailyWaterGoalLiters = 3.5)
        
        // Collect updated value
        values.add(flow.first())

        // Then: Should reflect preference changes
        assertEquals(2000, values[0]) // Initial: 2.0L = 2000ml
        assertEquals(3500, values[1]) // Updated: 3.5L = 3500ml
    }

    @Test
    fun `reactive flow prioritizes daily goal over preferences when both change`() = runTest {
        // Given: Both sources that can change
        val dailyGoalFlow = MutableStateFlow<DailyGoalEntity?>(null)
        val nutritionPrefsFlow = MutableStateFlow(NutritionPreferences(dailyWaterGoalLiters = 2.0))
        
        `when`(nutritionRepository.goalFlow(testDate)).thenReturn(dailyGoalFlow)
        `when`(userPreferencesRepository.nutritionPreferences).thenReturn(nutritionPrefsFlow)

        val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(testDate)
        val values = mutableListOf<Int>()
        
        // Initial state: no daily goal, preferences only
        values.add(flow.first())
        
        // Change both at the same time
        val newDailyGoal = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = 2800
        )
        dailyGoalFlow.value = newDailyGoal
        nutritionPrefsFlow.value = NutritionPreferences(dailyWaterGoalLiters = 4.0) // 4000ml
        
        values.add(flow.first())

        // Then: Daily goal should take priority over preferences
        assertEquals(2000, values[0]) // Initial: preferences only
        assertEquals(2800, values[1]) // Updated: daily goal wins over higher preference
    }

    @Test
    fun `reactive flow falls back to preferences when daily goal removed`() = runTest {
        // Given: Initial daily goal that gets removed
        val initialGoal = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = 2600
        )
        val dailyGoalFlow = MutableStateFlow<DailyGoalEntity?>(initialGoal)
        val nutritionPrefsFlow = MutableStateFlow(NutritionPreferences(dailyWaterGoalLiters = 2.3))
        
        `when`(nutritionRepository.goalFlow(testDate)).thenReturn(dailyGoalFlow)
        `when`(userPreferencesRepository.nutritionPreferences).thenReturn(nutritionPrefsFlow)

        val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(testDate)
        val values = mutableListOf<Int>()
        
        // Initial: daily goal exists
        values.add(flow.first())
        
        // Remove daily goal
        dailyGoalFlow.value = null
        values.add(flow.first())

        // Then: Should fall back to preferences
        assertEquals(2600, values[0]) // Initial: daily goal
        assertEquals(2300, values[1]) // Fallback: preferences (2.3L = 2300ml)
    }

    @Test
    fun `reactive flow uses default when all sources are empty`() = runTest {
        // Given: No daily goal and empty preferences
        val dailyGoalFlow = MutableStateFlow<DailyGoalEntity?>(null)
        val nutritionPrefsFlow = MutableStateFlow(NutritionPreferences(dailyWaterGoalLiters = 0.0))
        
        `when`(nutritionRepository.goalFlow(testDate)).thenReturn(dailyGoalFlow)
        `when`(userPreferencesRepository.nutritionPreferences).thenReturn(nutritionPrefsFlow)

        // When: Getting reactive flow
        val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(testDate)
        val result = flow.first()

        // Then: Should use default value
        assertEquals(HydrationGoalUseCase.DEFAULT_DAILY_WATER_GOAL_ML, result)
    }

    @Test
    fun `todays reactive flow uses timezone-safe date calculation`() = runTest {
        // Given: Current system date setup
        val dailyGoalFlow = MutableStateFlow<DailyGoalEntity?>(null)
        val nutritionPrefsFlow = MutableStateFlow(NutritionPreferences(dailyWaterGoalLiters = 2.4))
        
        // Note: We can't easily mock LocalDate.now() without more complex setup,
        // but we can verify the flow works with whatever "today" is
        `when`(nutritionRepository.goalFlow(org.mockito.kotlin.any<LocalDate>())).thenReturn(dailyGoalFlow)
        `when`(userPreferencesRepository.nutritionPreferences).thenReturn(nutritionPrefsFlow)

        // When: Getting today's reactive flow
        val flow = hydrationGoalUseCase.getTodaysHydrationGoalMlFlow()
        val result = flow.first()

        // Then: Should use preferences since no daily goal
        assertEquals(2400, result)
    }

    @Test
    fun `reactive flow handles zero water goal in daily goal entity`() = runTest {
        // Given: Daily goal with zero water target
        val dailyGoalWithZeroWater = DailyGoalEntity(
            dateIso = testDate.toString(),
            targetKcal = 2000,
            targetWaterMl = 0
        )
        val dailyGoalFlow = MutableStateFlow<DailyGoalEntity?>(dailyGoalWithZeroWater)
        val nutritionPrefsFlow = MutableStateFlow(NutritionPreferences(dailyWaterGoalLiters = 2.7))
        
        `when`(nutritionRepository.goalFlow(testDate)).thenReturn(dailyGoalFlow)
        `when`(userPreferencesRepository.nutritionPreferences).thenReturn(nutritionPrefsFlow)

        // When: Getting reactive flow
        val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(testDate)
        val result = flow.first()

        // Then: Should fall back to preferences when daily goal water is 0
        assertEquals(2700, result) // 2.7L = 2700ml
    }
}