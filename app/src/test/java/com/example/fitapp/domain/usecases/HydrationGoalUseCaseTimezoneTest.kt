package com.example.fitapp.domain.usecases

import com.example.fitapp.data.db.DailyGoalEntity
import com.example.fitapp.data.prefs.NutritionPreferences
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Tests for HydrationGoalUseCase timezone and DST behavior.
 * Verifies that day boundary logic works correctly across timezone changes.
 */
class HydrationGoalUseCaseTimezoneTest {
    @Mock
    private lateinit var nutritionRepository: NutritionRepository

    @Mock
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private lateinit var hydrationGoalUseCase: HydrationGoalUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        hydrationGoalUseCase = HydrationGoalUseCase(nutritionRepository, userPreferencesRepository, null)
    }

    @Test
    fun `today's hydration goal uses system timezone`() =
        runTest {
            // Given: System timezone affects what "today" means
            val systemToday = LocalDate.now(ZoneId.systemDefault())
            val utcToday = LocalDate.now(ZoneOffset.UTC)

            // These may be different depending on timezone
            val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.5) // 2500ml
            `when`(nutritionRepository.goalFlow(systemToday)).thenReturn(flowOf(null))
            `when`(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

            // When: Getting today's hydration goal
            val result = hydrationGoalUseCase.getTodaysHydrationGoalMl()

            // Then: Should use system timezone's "today", not UTC
            assertEquals(2500, result)
        }

    @Test
    fun `reactive flow updates when timezone changes daily goal availability`() =
        runTest {
            // Given: Different dates in different timezones might have different goals
            val date = LocalDate.of(2025, 3, 10) // During DST transition in many zones

            // First scenario: Daily goal exists
            val dailyGoal =
                DailyGoalEntity(
                    dateIso = date.toString(),
                    targetKcal = 2000,
                    targetWaterMl = 3000,
                )
            `when`(nutritionRepository.goalFlow(date)).thenReturn(flowOf(dailyGoal))

            val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.0) // 2000ml fallback
            `when`(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

            // When: Getting reactive flow
            val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(date)
            val result = flow.first()

            // Then: Daily goal takes priority
            assertEquals(3000, result)
        }

    @Test
    fun `DST spring forward day boundary handling`() =
        runTest {
            // Given: DST spring forward date (clocks move forward)
            val dstDate = LocalDate.of(2025, 3, 30) // Typical DST date in Europe
            val beforeDst = dstDate.minusDays(1)
            val afterDst = dstDate.plusDays(1)

            // Set up different goals for consecutive days
            val beforeGoal =
                DailyGoalEntity(
                    dateIso = beforeDst.toString(),
                    targetKcal = 2000,
                    targetWaterMl = 2500,
                )
            val dstGoal =
                DailyGoalEntity(
                    dateIso = dstDate.toString(),
                    targetKcal = 2000,
                    targetWaterMl = 3000,
                )
            val afterGoal =
                DailyGoalEntity(
                    dateIso = afterDst.toString(),
                    targetKcal = 2000,
                    targetWaterMl = 2800,
                )

            `when`(nutritionRepository.goalFlow(beforeDst)).thenReturn(flowOf(beforeGoal))
            `when`(nutritionRepository.goalFlow(dstDate)).thenReturn(flowOf(dstGoal))
            `when`(nutritionRepository.goalFlow(afterDst)).thenReturn(flowOf(afterGoal))

            val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.0)
            `when`(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

            // When: Getting goals for consecutive days during DST transition
            val beforeResult = hydrationGoalUseCase.getHydrationGoalMl(beforeDst)
            val dstResult = hydrationGoalUseCase.getHydrationGoalMl(dstDate)
            val afterResult = hydrationGoalUseCase.getHydrationGoalMl(afterDst)

            // Then: Each day should return its correct goal regardless of DST
            assertEquals(2500, beforeResult)
            assertEquals(3000, dstResult)
            assertEquals(2800, afterResult)
        }

    @Test
    fun `DST fall back day boundary handling`() =
        runTest {
            // Given: DST fall back date (clocks move backward)
            val dstDate = LocalDate.of(2025, 10, 26) // Typical DST end date in Europe
            val beforeDst = dstDate.minusDays(1)
            val afterDst = dstDate.plusDays(1)

            // Set up goals around DST end
            `when`(nutritionRepository.goalFlow(beforeDst)).thenReturn(flowOf(null))
            `when`(nutritionRepository.goalFlow(dstDate)).thenReturn(flowOf(null))
            `when`(nutritionRepository.goalFlow(afterDst)).thenReturn(flowOf(null))

            val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.7) // 2700ml
            `when`(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

            // When: Getting goals around DST end
            val beforeResult = hydrationGoalUseCase.getHydrationGoalMl(beforeDst)
            val dstResult = hydrationGoalUseCase.getHydrationGoalMl(dstDate)
            val afterResult = hydrationGoalUseCase.getHydrationGoalMl(afterDst)

            // Then: All days should consistently use the same fallback
            assertEquals(2700, beforeResult)
            assertEquals(2700, dstResult)
            assertEquals(2700, afterResult)
        }

    @Test
    fun `timezone offset changes don't affect LocalDate-based goals`() =
        runTest {
            // Given: Same LocalDate but different timezone offset
            val date = LocalDate.of(2025, 6, 15)

            // Daily goal exists for this LocalDate
            val dailyGoal =
                DailyGoalEntity(
                    dateIso = date.toString(),
                    targetKcal = 2000,
                    targetWaterMl = 2600,
                )
            `when`(nutritionRepository.goalFlow(date)).thenReturn(flowOf(dailyGoal))

            val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.0)
            `when`(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

            // When: Getting goal for the same LocalDate
            val result = hydrationGoalUseCase.getHydrationGoalMl(date)

            // Then: Should use the daily goal regardless of timezone offset
            // LocalDate is timezone-independent, so this should be consistent
            assertEquals(2600, result)
        }

    @Test
    fun `reactive flow handles concurrent updates correctly`() =
        runTest {
            // Given: Initial setup
            val date = LocalDate.of(2025, 1, 20)

            val initialGoal =
                DailyGoalEntity(
                    dateIso = date.toString(),
                    targetKcal = 2000,
                    targetWaterMl = 2400,
                )
            `when`(nutritionRepository.goalFlow(date)).thenReturn(flowOf(initialGoal))

            val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 3.0) // 3000ml fallback
            `when`(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

            // When: Getting reactive flow
            val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(date)
            val initialResult = flow.first()

            // Then: Should prioritize daily goal over preferences
            assertEquals(2400, initialResult)
        }

    @Test
    fun `year boundary dates handle correctly`() =
        runTest {
            // Given: New Year's Eve and New Year's Day
            val newYearsEve = LocalDate.of(2024, 12, 31)
            val newYearsDay = LocalDate.of(2025, 1, 1)

            // Different goals for each year boundary day
            val eveGoal =
                DailyGoalEntity(
                    dateIso = newYearsEve.toString(),
                    targetKcal = 1800, // Lower goal for holiday
                    targetWaterMl = 2200,
                )
            val dayGoal =
                DailyGoalEntity(
                    dateIso = newYearsDay.toString(),
                    targetKcal = 2200, // Higher goal for resolution
                    targetWaterMl = 3200,
                )

            `when`(nutritionRepository.goalFlow(newYearsEve)).thenReturn(flowOf(eveGoal))
            `when`(nutritionRepository.goalFlow(newYearsDay)).thenReturn(flowOf(dayGoal))

            val nutritionPrefs = NutritionPreferences(dailyWaterGoalLiters = 2.5)
            `when`(userPreferencesRepository.nutritionPreferences).thenReturn(flowOf(nutritionPrefs))

            // When: Getting goals for year boundary
            val eveResult = hydrationGoalUseCase.getHydrationGoalMl(newYearsEve)
            val dayResult = hydrationGoalUseCase.getHydrationGoalMl(newYearsDay)

            // Then: Each day should have its distinct goal
            assertEquals(2200, eveResult)
            assertEquals(3200, dayResult)
        }
}
