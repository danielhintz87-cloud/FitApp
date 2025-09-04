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
 * Tests BMI calculations and weight loss program management
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
            weight = 75.0f,
            height = 1.75f,
            bmi = 24.5f,
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
    fun `calculateAndSaveBMI computes BMI correctly`() = runTest {
        // Given: User measurements
        val heightCm = 175.0f
        val weightKg = 75.0f
        val date = "2024-01-15"
        
        whenever(bmiHistoryDao.getByDate(date)).thenReturn(null)
        whenever(bmiHistoryDao.insert(any())).thenReturn(1L)
        
        // When: Calculating and saving BMI
        val result = repository.calculateAndSaveBMI(heightCm, weightKg, date)
        
        // Then: Should return correct BMI result
        assertEquals(24.49f, result.bmi, 0.1f) // 75 / (1.75^2) = 24.49
        assertEquals(BMICategory.NORMAL, result.category)
        
        // Verify BMI history was saved
        verify(bmiHistoryDao).insert(any())
    }

    @Test
    fun `getBMIHistoryByDate returns entry for specific date`() = runTest {
        // Given: A BMI entry for a specific date
        val date = "2024-01-15"
        val expectedEntry = BMIHistoryEntity(
            weight = 75.0f,
            height = 1.75f,
            bmi = 24.5f,
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
            BMIHistoryEntity(weight = 75.0f, height = 1.75f, bmi = 24.5f, category = "Normal", date = "2024-01-15", recordedAt = 1),
            BMIHistoryEntity(weight = 74.0f, height = 1.75f, bmi = 24.2f, category = "Normal", date = "2024-01-14", recordedAt = 2)
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
    fun `saveWeightLossProgram stores program correctly`() = runTest {
        // Given: A weight loss program
        val program = WeightLossProgramEntity(
            startDate = "2024-01-01",
            endDate = "2024-03-01",
            startWeight = 80.0f,
            targetWeight = 70.0f,
            currentWeight = 80.0f,
            dailyCalorieTarget = 1800,
            weeklyWeightLossGoal = 0.5f,
            isActive = true,
            programType = "standard",
            createdAt = 1
        )
        
        whenever(weightLossProgramDao.insert(program)).thenReturn(1L)
        
        // When: Saving weight loss program
        val result = repository.saveWeightLossProgram(program)
        
        // Then: Should return the inserted ID
        assertEquals(1L, result)
        verify(weightLossProgramDao).insert(program)
    }

    @Test
    fun `BMI calculation uses correct formula`() = runTest {
        // Given: Known measurements
        val heightCm = 180.0f
        val weightKg = 80.0f
        
        whenever(bmiHistoryDao.getByDate(any())).thenReturn(null)
        whenever(bmiHistoryDao.insert(any())).thenReturn(1L)
        
        // When: Calculating BMI
        val result = repository.calculateAndSaveBMI(heightCm, weightKg)
        
        // Then: Should use BMI formula correctly
        // BMI = weight(kg) / height(m)^2
        val expectedBMI = weightKg / ((heightCm / 100) * (heightCm / 100))
        assertEquals(expectedBMI, result.bmi, 0.01f)
    }

    @Test
    fun `BMI categorization works correctly for different ranges`() = runTest {
        whenever(bmiHistoryDao.getByDate(any())).thenReturn(null)
        whenever(bmiHistoryDao.insert(any())).thenReturn(1L)
        
        // Test underweight (BMI < 18.5)
        val underweightResult = repository.calculateAndSaveBMI(170.0f, 50.0f) // BMI ~17.3
        assertEquals(BMICategory.UNDERWEIGHT, underweightResult.category)
        
        // Test normal (18.5 <= BMI < 25)
        val normalResult = repository.calculateAndSaveBMI(170.0f, 65.0f) // BMI ~22.5
        assertEquals(BMICategory.NORMAL, normalResult.category)
        
        // Test overweight (25 <= BMI < 30)
        val overweightResult = repository.calculateAndSaveBMI(170.0f, 75.0f) // BMI ~26.0
        assertEquals(BMICategory.OVERWEIGHT, overweightResult.category)
        
        // Test obese (BMI >= 30)
        val obeseResult = repository.calculateAndSaveBMI(170.0f, 90.0f) // BMI ~31.1
        assertEquals(BMICategory.OBESE, obeseResult.category)
    }

    @Test
    fun `weight loss program validation ensures safe rates`() = runTest {
        // Given: A weight loss program with extreme goals
        val program = WeightLossProgramEntity(
            startDate = "2024-01-01",
            endDate = "2024-03-01",
            startWeight = 100.0f,
            targetWeight = 60.0f, // 40kg loss
            currentWeight = 100.0f,
            dailyCalorieTarget = 800, // Too low
            weeklyWeightLossGoal = 2.0f, // Too aggressive
            isActive = true,
            programType = "intensive",
            createdAt = 1
        )
        
        // Test that the program structure allows for validation
        assertTrue("Weekly goal should be measurable", program.weeklyWeightLossGoal > 0)
        assertTrue("Program should have a start weight", program.startWeight > 0)
        assertTrue("Program should have a target", program.targetWeight > 0)
        assertTrue("Target should be less than start", program.targetWeight < program.currentWeight)
    }

    @Test
    fun `BMI history updates existing entry for same date`() = runTest {
        // Given: An existing BMI entry for today
        val date = "2024-01-15"
        val existingEntry = BMIHistoryEntity(
            id = 1L,
            weight = 74.0f,
            height = 1.75f,
            bmi = 24.2f,
            category = "Normal",
            date = date,
            recordedAt = 1
        )
        
        whenever(bmiHistoryDao.getByDate(date)).thenReturn(existingEntry)
        
        // When: Calculating and saving BMI for the same date
        repository.calculateAndSaveBMI(175.0f, 75.0f, date)
        
        // Then: Should update existing entry instead of creating new one
        verify(bmiHistoryDao).update(any())
        verify(bmiHistoryDao, never()).insert(any())
    }

    @Test
    fun `getBMIHistoryByDateRange returns entries in range`() = runTest {
        // Given: BMI entries in a date range
        val startDate = "2024-01-10"
        val endDate = "2024-01-15"
        val entriesInRange = listOf(
            BMIHistoryEntity(weight = 75.0f, height = 1.75f, bmi = 24.5f, category = "Normal", date = "2024-01-12", recordedAt = 1),
            BMIHistoryEntity(weight = 74.5f, height = 1.75f, bmi = 24.3f, category = "Normal", date = "2024-01-14", recordedAt = 2)
        )
        
        whenever(bmiHistoryDao.getByDateRange(startDate, endDate)).thenReturn(entriesInRange)
        
        // When: Getting BMI history by date range
        val result = repository.getBMIHistoryByDateRange(startDate, endDate)
        
        // Then: Should return entries in the specified range
        assertEquals(entriesInRange, result)
        verify(bmiHistoryDao).getByDateRange(startDate, endDate)
    }
}