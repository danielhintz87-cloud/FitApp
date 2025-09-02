package com.example.fitapp.data.repo

import com.example.fitapp.data.db.*
import com.example.fitapp.domain.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.time.LocalDate

/**
 * Unit tests for WeightLossRepository
 * Tests weight tracking calculations and BMI management
 */
class WeightLossRepositoryTest {

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var bmiHistoryDao: BMIHistoryDao

    @Mock
    private lateinit var weightLossProgramDao: WeightLossProgramDao

    @Mock
    private lateinit var behavioralCheckInDao: BehavioralCheckInDao

    @Mock
    private lateinit var progressPhotoDao: ProgressPhotoDao

    private lateinit var repository: WeightLossRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        whenever(database.bmiHistoryDao()).thenReturn(bmiHistoryDao)
        whenever(database.weightLossProgramDao()).thenReturn(weightLossProgramDao)
        whenever(database.behavioralCheckInDao()).thenReturn(behavioralCheckInDao)
        whenever(database.progressPhotoDao()).thenReturn(progressPhotoDao)
        
        repository = WeightLossRepository(database)
    }

    // BMI Management Tests

    @Test
    fun `saveBMIHistory stores BMI entry correctly`() = runTest {
        // Given: A BMI history entry
        val bmiEntry = BMIHistoryEntity(
            weight = 75.0,
            height = 1.75,
            bmi = 24.5,
            category = "Normal",
            date = "2024-01-15",
            recordedAt = System.currentTimeMillis() / 1000
        )
        
        whenever(bmiHistoryDao.insert(bmiEntry)).thenReturn(1L)
        
        // When: Saving BMI history
        val result = repository.saveBMIHistory(bmiEntry)
        
        // Then: Should return the inserted ID
        assertEquals(1L, result)
        verify(bmiHistoryDao).insert(bmiEntry)
    }

    @Test
    fun `updateBMIHistory modifies existing entry`() = runTest {
        // Given: An existing BMI entry with updates
        val updatedEntry = BMIHistoryEntity(
            id = 1L,
            weight = 76.0, // Updated weight
            height = 1.75,
            bmi = 24.8,
            category = "Normal",
            date = "2024-01-15",
            recordedAt = System.currentTimeMillis() / 1000
        )
        
        // When: Updating BMI history
        repository.updateBMIHistory(updatedEntry)
        
        // Then: Should call update on DAO
        verify(bmiHistoryDao).update(updatedEntry)
    }

    @Test
    fun `deleteBMIHistory removes entry by ID`() = runTest {
        // Given: An entry ID to delete
        val entryId = 1L
        
        // When: Deleting BMI history
        repository.deleteBMIHistory(entryId)
        
        // Then: Should call delete on DAO
        verify(bmiHistoryDao).delete(entryId)
    }

    @Test
    fun `getBMIHistoryByDate returns entry for specific date`() = runTest {
        // Given: A BMI entry for a specific date
        val date = "2024-01-15"
        val expectedEntry = BMIHistoryEntity(
            weight = 75.0,
            height = 1.75,
            bmi = 24.5,
            category = "Normal",
            date = date,
            recordedAt = System.currentTimeMillis() / 1000
        )
        
        whenever(bmiHistoryDao.getByDate(date)).thenReturn(expectedEntry)
        
        // When: Getting BMI history by date
        val result = repository.getBMIHistoryByDate(date)
        
        // Then: Should return the correct entry
        assertEquals(expectedEntry, result)
        verify(bmiHistoryDao).getByDate(date)
    }

    @Test
    fun `getAllBMIHistory returns all entries`() = runTest {
        // Given: Multiple BMI entries
        val entries = listOf(
            BMIHistoryEntity(weight = 75.0, height = 1.75, bmi = 24.5, category = "Normal", date = "2024-01-15", recordedAt = 1),
            BMIHistoryEntity(weight = 74.0, height = 1.75, bmi = 24.2, category = "Normal", date = "2024-01-14", recordedAt = 2)
        )
        
        whenever(bmiHistoryDao.getAll()).thenReturn(entries)
        
        // When: Getting all BMI history
        val result = repository.getAllBMIHistory()
        
        // Then: Should return all entries
        assertEquals(entries, result)
        verify(bmiHistoryDao).getAll()
    }

    // Weight Loss Program Tests

    @Test
    fun `createWeightLossProgram generates program with correct calculations`() = runTest {
        // Given: User parameters for weight loss program
        val currentWeight = 80.0
        val targetWeight = 70.0
        val heightCm = 175
        val age = 30
        val gender = "male"
        val activityLevel = ActivityLevel.MODERATELY_ACTIVE
        
        whenever(weightLossProgramDao.insert(any())).thenReturn(1L)
        
        // When: Creating weight loss program
        val result = repository.createWeightLossProgram(
            currentWeight, targetWeight, heightCm, age, gender, activityLevel
        )
        
        // Then: Should return a valid program
        assertNotNull(result)
        assertEquals(currentWeight, result.currentWeight, 0.01)
        assertEquals(targetWeight, result.targetWeight, 0.01)
        assertTrue(result.dailyCalorieTarget > 0)
        assertTrue(result.weeklyGoalKg in 0.5..1.0) // Safe weight loss rate
        assertTrue(result.estimatedWeeks > 0)
        
        // Verify program was saved to database
        verify(weightLossProgramDao).insert(any())
    }

    @Test
    fun `weight loss program enforces safe weight loss rates`() = runTest {
        // Given: Parameters that would result in aggressive weight loss
        val currentWeight = 100.0
        val targetWeight = 60.0 // 40kg loss - should be limited
        val heightCm = 175
        val age = 25
        val gender = "female"
        val activityLevel = ActivityLevel.LIGHTLY_ACTIVE
        
        whenever(weightLossProgramDao.insert(any())).thenReturn(1L)
        
        // When: Creating weight loss program
        val result = repository.createWeightLossProgram(
            currentWeight, targetWeight, heightCm, age, gender, activityLevel
        )
        
        // Then: Weekly goal should be limited to safe rates (max 1kg/week)
        assertTrue("Weekly goal should be safe", result.weeklyGoalKg <= 1.0)
        assertTrue("Weekly goal should be positive", result.weeklyGoalKg > 0)
    }

    @Test
    fun `macro targets are calculated correctly for weight loss`() = runTest {
        // Given: A weight loss program with known calorie target
        val dailyCalories = 1500.0
        
        // When: Calculating macro targets
        val macros = MacroTargets.forWeightLoss(dailyCalories)
        
        // Then: Macros should follow weight loss distribution
        // Typically: 30% protein, 40% carbs, 30% fat
        val expectedProtein = dailyCalories * 0.30 / 4 // 4 cal/g protein
        val expectedCarbs = dailyCalories * 0.40 / 4 // 4 cal/g carbs
        val expectedFat = dailyCalories * 0.30 / 9 // 9 cal/g fat
        
        assertEquals(expectedProtein, macros.proteinGrams, 5.0)
        assertEquals(expectedCarbs, macros.carbsGrams, 5.0)
        assertEquals(expectedFat, macros.fatGrams, 2.0)
    }

    @Test
    fun `BMR calculation uses correct formula for gender`() = runTest {
        // Test BMR calculation for male
        val maleBMR = repository.calculateBMR(
            weight = 80.0,
            heightCm = 180,
            age = 30,
            gender = "male"
        )
        
        // Harris-Benedict equation for male:
        // BMR = 88.362 + (13.397 × weight) + (4.799 × height) - (5.677 × age)
        val expectedMaleBMR = 88.362 + (13.397 * 80.0) + (4.799 * 180) - (5.677 * 30)
        assertEquals(expectedMaleBMR, maleBMR, 1.0)
        
        // Test BMR calculation for female
        val femaleBMR = repository.calculateBMR(
            weight = 65.0,
            heightCm = 165,
            age = 25,
            gender = "female"
        )
        
        // Harris-Benedict equation for female:
        // BMR = 447.593 + (9.247 × weight) + (3.098 × height) - (4.330 × age)
        val expectedFemaleBMR = 447.593 + (9.247 * 65.0) + (3.098 * 165) - (4.330 * 25)
        assertEquals(expectedFemaleBMR, femaleBMR, 1.0)
    }

    @Test
    fun `activity level multipliers are applied correctly`() = runTest {
        // Given: Base BMR
        val baseBMR = 1500.0
        
        // When: Applying different activity levels
        val sedentaryTDEE = repository.calculateTDEE(baseBMR, ActivityLevel.SEDENTARY)
        val lightlyActiveTDEE = repository.calculateTDEE(baseBMR, ActivityLevel.LIGHTLY_ACTIVE)
        val moderatelyActiveTDEE = repository.calculateTDEE(baseBMR, ActivityLevel.MODERATELY_ACTIVE)
        val veryActiveTDEE = repository.calculateTDEE(baseBMR, ActivityLevel.VERY_ACTIVE)
        val extraActiveTDEE = repository.calculateTDEE(baseBMR, ActivityLevel.EXTRA_ACTIVE)
        
        // Then: TDEE should increase with activity level
        assertTrue(sedentaryTDEE < lightlyActiveTDEE)
        assertTrue(lightlyActiveTDEE < moderatelyActiveTDEE)
        assertTrue(moderatelyActiveTDEE < veryActiveTDEE)
        assertTrue(veryActiveTDEE < extraActiveTDEE)
        
        // Check approximate multipliers
        assertEquals(baseBMR * 1.2, sedentaryTDEE, 10.0) // 1.2x for sedentary
        assertEquals(baseBMR * 1.375, lightlyActiveTDEE, 10.0) // 1.375x for lightly active
    }

    @Test
    fun `weekly milestones are generated correctly`() = runTest {
        // Given: A weight loss program
        val program = WeightLossProgram(
            id = 1L,
            currentWeight = 80.0,
            targetWeight = 75.0,
            weeklyGoalKg = 0.5,
            estimatedWeeks = 10,
            dailyCalorieTarget = 1800,
            dailyCalorieDeficit = 500,
            macroTargets = MacroTargets.forWeightLoss(1800.0),
            activityLevel = ActivityLevel.MODERATELY_ACTIVE,
            startDate = LocalDate.now(),
            isActive = true,
            createdAt = System.currentTimeMillis() / 1000,
            updatedAt = System.currentTimeMillis() / 1000
        )
        
        // When: Generating milestones
        val milestones = repository.generateWeeklyMilestones(program)
        
        // Then: Should have correct number of milestones
        assertEquals(10, milestones.size)
        
        // Check first milestone
        val firstMilestone = milestones.first()
        assertEquals(79.5, firstMilestone.targetWeight, 0.01) // 80.0 - 0.5
        assertEquals(1, firstMilestone.weekNumber)
        
        // Check last milestone
        val lastMilestone = milestones.last()
        assertEquals(75.0, lastMilestone.targetWeight, 0.01) // Target weight
        assertEquals(10, lastMilestone.weekNumber)
    }

    @Test
    fun `program progress tracking updates correctly`() = runTest {
        // Given: A weight loss program and new weight
        val programId = 1L
        val newWeight = 78.0
        val date = LocalDate.now()
        
        whenever(weightLossProgramDao.getActiveProgram()).thenReturn(
            WeightLossProgramEntity(
                id = programId,
                currentWeight = 80.0,
                targetWeight = 75.0,
                weeklyGoalKg = 0.5,
                estimatedWeeks = 10,
                dailyCalorieTarget = 1800,
                dailyCalorieDeficit = 500,
                proteinGrams = 135.0,
                carbsGrams = 180.0,
                fatGrams = 60.0,
                activityLevel = "moderately_active",
                startDate = "2024-01-01",
                isActive = true,
                createdAt = 1,
                updatedAt = 1
            )
        )
        
        // When: Updating program progress
        repository.updateProgramProgress(programId, newWeight, date)
        
        // Then: Program should be updated with new current weight
        verify(weightLossProgramDao).updateCurrentWeight(programId, newWeight)
        verify(weightLossProgramDao).updateLastUpdated(eq(programId), any())
    }
}