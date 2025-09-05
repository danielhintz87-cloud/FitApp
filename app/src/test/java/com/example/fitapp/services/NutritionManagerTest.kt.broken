package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for NutritionManager
 * Tests macro calculations, meal logging, and calorie balance logic
 */
class NutritionManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var database: AppDatabase

    private lateinit var nutritionManager: NutritionManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        nutritionManager = NutritionManager(context, database)
    }

    // Daily Macro Calculation Tests

    @Test
    fun `should calculate daily macros correctly for weight loss`() = runTest {
        // Given: Male user wanting to lose weight
        val bodyWeight = 80f
        val height = 175f
        val age = 30
        val gender = Gender.MALE
        val activityLevel = ActivityLevel.MODERATELY_ACTIVE
        val goal = NutritionGoal.WEIGHT_LOSS

        // When: Calculating daily macros
        val result = nutritionManager.calculateDailyMacros(
            bodyWeight, height, age, gender, activityLevel, goal
        )

        // Then: Should return reasonable macro targets
        assertTrue("Target calories should be positive", result.targetCalories > 0)
        assertTrue("Protein should be adequate", result.proteinGrams >= (bodyWeight * 1.6).toInt())
        assertTrue("Carbs should be positive", result.carbsGrams >= 0)
        assertTrue("Fat should be reasonable", result.fatGrams > 0)
        assertTrue("BMR should be calculated", result.bmr > 0)
        assertTrue("TDEE should be higher than BMR", result.tdee > result.bmr)
        assertEquals("Goal should match", goal, result.goal)
        
        // Calories for weight loss should be below TDEE
        assertTrue("Weight loss calories should be below TDEE", result.targetCalories < result.tdee)
    }

    @Test
    fun `should calculate daily macros correctly for weight gain`() = runTest {
        // Given: Female user wanting to gain weight
        val bodyWeight = 60f
        val height = 165f
        val age = 25
        val gender = Gender.FEMALE
        val activityLevel = ActivityLevel.VERY_ACTIVE
        val goal = NutritionGoal.WEIGHT_GAIN

        // When: Calculating daily macros
        val result = nutritionManager.calculateDailyMacros(
            bodyWeight, height, age, gender, activityLevel, goal
        )

        // Then: Should return higher calorie target
        assertTrue("Target calories should be positive", result.targetCalories > 0)
        assertTrue("Weight gain calories should be above TDEE", result.targetCalories > result.tdee)
        assertTrue("Protein should be adequate for muscle gain", result.proteinGrams >= (bodyWeight * 1.6).toInt())
        assertEquals("Goal should match", goal, result.goal)
    }

    @Test
    fun `should calculate daily macros correctly for maintenance`() = runTest {
        // Given: User wanting to maintain weight
        val bodyWeight = 70f
        val height = 170f
        val age = 35
        val gender = Gender.MALE
        val activityLevel = ActivityLevel.LIGHTLY_ACTIVE
        val goal = NutritionGoal.MAINTENANCE

        // When: Calculating daily macros
        val result = nutritionManager.calculateDailyMacros(
            bodyWeight, height, age, gender, activityLevel, goal
        )

        // Then: Should return maintenance calories
        assertTrue("Target calories should be positive", result.targetCalories > 0)
        assertEquals("Maintenance calories should equal TDEE", result.tdee, result.targetCalories)
        assertEquals("Goal should match", goal, result.goal)
    }

    @Test
    fun `calculateDailyMacros should validate input parameters`() = runTest {
        // Test negative weight
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                nutritionManager.calculateDailyMacros(-70f, 170f, 25, Gender.MALE, ActivityLevel.MODERATELY_ACTIVE, NutritionGoal.MAINTENANCE)
            }
        }

        // Test negative height
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                nutritionManager.calculateDailyMacros(70f, -170f, 25, Gender.MALE, ActivityLevel.MODERATELY_ACTIVE, NutritionGoal.MAINTENANCE)
            }
        }

        // Test negative age
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                nutritionManager.calculateDailyMacros(70f, 170f, -25, Gender.MALE, ActivityLevel.MODERATELY_ACTIVE, NutritionGoal.MAINTENANCE)
            }
        }
    }

    // Meal Logging Tests

    @Test
    fun `should track meal logging accurately`() = runTest {
        // Given: Valid meal with foods
        val foods = listOf(
            FoodItem("Chicken Breast", 165.0, 31.0, 0.0, 3.6, 100.0, "g"),
            FoodItem("Rice", 130.0, 2.7, 28.0, 0.3, 100.0, "g"),
            FoodItem("Broccoli", 34.0, 2.8, 7.0, 0.4, 100.0, "g")
        )
        val mealType = MealType.LUNCH

        // When: Logging meal
        val result = nutritionManager.logMeal(mealType, foods)

        // Then: Should log meal successfully
        assertNotNull("Meal ID should be generated", result.mealId)
        assertEquals("Meal type should match", mealType, result.mealType)
        assertTrue("Timestamp should be recent", result.timestamp > 0)
        assertEquals("Total calories should be sum", 329, result.totalCalories)
        assertEquals("Total protein should be sum", 36, result.totalProtein)
        assertEquals("Total carbs should be sum", 35, result.totalCarbs)
        assertEquals("Total fat should be sum", 4, result.totalFat)
        assertEquals("Foods should match", foods.size, result.foods.size)
        assertTrue("Accuracy should be reasonable", result.accuracy > 0.8f)
        assertTrue("Should be valid", result.isValid)
    }

    @Test
    fun `logMeal should validate nutritional consistency`() = runTest {
        // Given: Foods with inconsistent calorie data
        val inconsistentFoods = listOf(
            FoodItem("Test Food", 1000.0, 10.0, 10.0, 10.0, 100.0, "g") // 1000 cal claimed, but macros only add up to 170 cal
        )

        // When: Logging inconsistent meal
        val result = nutritionManager.logMeal(MealType.DINNER, inconsistentFoods)

        // Then: Should detect inconsistency
        assertTrue("Accuracy should be low", result.accuracy < 0.5f)
        assertFalse("Should not be valid", result.isValid)
    }

    @Test
    fun `logMeal should require non-empty foods list`() = runTest {
        // Test empty foods list
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                nutritionManager.logMeal(MealType.BREAKFAST, emptyList())
            }
        }
    }

    // Meal Timing Optimization Tests

    @Test
    fun `should suggest meal timing optimization`() {
        // Given: Workout at 18:00 (6 PM) for 60 minutes, 4 meals total
        val workoutTime = 18
        val workoutDuration = 60
        val totalMeals = 4

        // When: Suggesting meal timing
        val result = nutritionManager.suggestMealTiming(workoutTime, workoutDuration, totalMeals)

        // Then: Should provide optimized meal timing
        assertEquals("Should suggest correct number of meals", totalMeals, result.recommendedTimes.size)
        assertTrue("Should include breakfast", result.recommendedTimes.contains(7))
        assertNotNull("Should suggest post-workout meal", result.postWorkoutMealTime)
        assertTrue("Post-workout meal should be after workout", result.postWorkoutMealTime > workoutTime)
        assertNotNull("Should provide reasoning", result.reasoning)
        assertTrue("Reasoning should mention workout time", result.reasoning.contains("18:00"))
    }

    @Test
    fun `meal timing should include pre-workout meal when appropriate`() {
        // Given: Morning workout at 8:00
        val workoutTime = 8
        val workoutDuration = 45
        val totalMeals = 5

        // When: Suggesting meal timing
        val result = nutritionManager.suggestMealTiming(workoutTime, workoutDuration, totalMeals)

        // Then: Should suggest pre-workout meal
        assertNotNull("Should suggest pre-workout meal for morning workout", result.preWorkoutMealTime)
        assertTrue("Pre-workout meal should be before workout", result.preWorkoutMealTime!! < workoutTime)
    }

    @Test
    fun `suggestMealTiming should validate input parameters`() {
        // Test invalid workout time
        assertThrows(IllegalArgumentException::class.java) {
            nutritionManager.suggestMealTiming(25, 60, 4)
        }

        // Test negative workout duration
        assertThrows(IllegalArgumentException::class.java) {
            nutritionManager.suggestMealTiming(18, -30, 4)
        }

        // Test invalid meal count
        assertThrows(IllegalArgumentException::class.java) {
            nutritionManager.suggestMealTiming(18, 60, 7)
        }
    }

    // Calorie Balance Calculation Tests

    @Test
    fun `should handle calorie surplus-deficit calculations`() {
        // Test calorie surplus
        val surplusResult = nutritionManager.calculateCalorieBalance(2000, 2300, 100)
        assertEquals("Target should match", 2000, surplusResult.targetCalories)
        assertEquals("Net calories should be consumed minus burned", 2200, surplusResult.netCalories)
        assertEquals("Balance should be positive for surplus", 200, surplusResult.balance)
        assertTrue("Balance percentage should be positive", surplusResult.balancePercentage > 0)
        
        // Test calorie deficit
        val deficitResult = nutritionManager.calculateCalorieBalance(2000, 1700, 50)
        assertEquals("Net calories should be consumed minus burned", 1650, deficitResult.netCalories)
        assertEquals("Balance should be negative for deficit", -350, deficitResult.balance)
        assertTrue("Balance percentage should be negative", deficitResult.balancePercentage < 0)
    }

    @Test
    fun `should categorize calorie balance types correctly`() {
        // Test balanced
        val balancedResult = nutritionManager.calculateCalorieBalance(2000, 2000, 0)
        assertEquals("Should be balanced", BalanceType.BALANCED, balancedResult.balanceType)

        // Test small surplus
        val smallSurplusResult = nutritionManager.calculateCalorieBalance(2000, 2100, 0)
        assertEquals("Should be small surplus", BalanceType.SMALL_SURPLUS, smallSurplusResult.balanceType)

        // Test large surplus
        val largeSurplusResult = nutritionManager.calculateCalorieBalance(2000, 2500, 0)
        assertEquals("Should be large surplus", BalanceType.LARGE_SURPLUS, largeSurplusResult.balanceType)

        // Test small deficit
        val smallDeficitResult = nutritionManager.calculateCalorieBalance(2000, 1800, 0)
        assertEquals("Should be small deficit", BalanceType.SMALL_DEFICIT, smallDeficitResult.balanceType)

        // Test large deficit
        val largeDeficitResult = nutritionManager.calculateCalorieBalance(2000, 1500, 0)
        assertEquals("Should be large deficit", BalanceType.LARGE_DEFICIT, largeDeficitResult.balanceType)
    }

    @Test
    fun `calorie balance should provide appropriate recommendations`() {
        // Test large surplus recommendation
        val surplusResult = nutritionManager.calculateCalorieBalance(2000, 2500, 0)
        assertTrue("Should recommend reducing intake for large surplus", 
            surplusResult.recommendation.contains("Reduziere") || 
            surplusResult.recommendation.contains("hoch"))

        // Test large deficit recommendation
        val deficitResult = nutritionManager.calculateCalorieBalance(2000, 1500, 0)
        assertTrue("Should recommend increasing intake for large deficit", 
            surplusResult.recommendation.contains("Erhöhe") || 
            deficitResult.recommendation.contains("hoch"))
    }

    // Nutrition Goal Validation Tests

    @Test
    fun `should validate nutrition goals`() {
        // Test realistic weight loss goal
        val realisticLossValidation = nutritionManager.validateNutritionGoals(
            currentWeight = 80f,
            targetWeight = 75f,
            timeFrameWeeks = 10,
            goal = NutritionGoal.WEIGHT_LOSS
        )
        assertTrue("Should be realistic", realisticLossValidation.isRealistic)
        assertEquals("Time frame should remain", 10, realisticLossValidation.recommendedTimeFrameWeeks)
        assertTrue("Weekly change should be reasonable", realisticLossValidation.weeklyWeightChange >= -1.0f)

        // Test unrealistic weight loss goal
        val unrealisticLossValidation = nutritionManager.validateNutritionGoals(
            currentWeight = 80f,
            targetWeight = 60f,
            timeFrameWeeks = 5,
            goal = NutritionGoal.WEIGHT_LOSS
        )
        assertFalse("Should not be realistic", unrealisticLossValidation.isRealistic)
        assertTrue("Should suggest longer timeframe", unrealisticLossValidation.recommendedTimeFrameWeeks > 5)
        assertTrue("Should have warnings", unrealisticLossValidation.warnings.isNotEmpty())
    }

    @Test
    fun `should validate weight gain goals`() {
        // Test realistic weight gain goal
        val realisticGainValidation = nutritionManager.validateNutritionGoals(
            currentWeight = 60f,
            targetWeight = 65f,
            timeFrameWeeks = 15,
            goal = NutritionGoal.WEIGHT_GAIN
        )
        assertTrue("Should be realistic", realisticGainValidation.isRealistic)
        assertTrue("Weekly change should be reasonable", realisticGainValidation.weeklyWeightChange <= 0.5f)
        assertTrue("Should have recommendations", realisticGainValidation.recommendations.isNotEmpty())
    }

    @Test
    fun `should validate maintenance goals`() {
        // Test maintenance goal
        val maintenanceValidation = nutritionManager.validateNutritionGoals(
            currentWeight = 70f,
            targetWeight = 70.5f,
            timeFrameWeeks = 12,
            goal = NutritionGoal.MAINTENANCE
        )
        assertTrue("Should be realistic for maintenance", maintenanceValidation.isRealistic)
        assertTrue("Weekly change should be minimal", kotlin.math.abs(maintenanceValidation.weeklyWeightChange) <= 0.1f)
    }

    @Test
    fun `validateNutritionGoals should validate input parameters`() {
        // Test negative current weight
        assertThrows(IllegalArgumentException::class.java) {
            nutritionManager.validateNutritionGoals(-70f, 65f, 10, NutritionGoal.WEIGHT_LOSS)
        }

        // Test negative target weight
        assertThrows(IllegalArgumentException::class.java) {
            nutritionManager.validateNutritionGoals(70f, -65f, 10, NutritionGoal.WEIGHT_LOSS)
        }

        // Test negative time frame
        assertThrows(IllegalArgumentException::class.java) {
            nutritionManager.validateNutritionGoals(70f, 65f, -10, NutritionGoal.WEIGHT_LOSS)
        }
    }

    @Test
    fun `BMR calculation should differ by gender`() = runTest {
        // Given: Same stats for male and female
        val bodyWeight = 70f
        val height = 170f
        val age = 30

        // When: Calculating macros for both genders
        val maleResult = nutritionManager.calculateDailyMacros(
            bodyWeight, height, age, Gender.MALE, ActivityLevel.SEDENTARY, NutritionGoal.MAINTENANCE
        )
        val femaleResult = nutritionManager.calculateDailyMacros(
            bodyWeight, height, age, Gender.FEMALE, ActivityLevel.SEDENTARY, NutritionGoal.MAINTENANCE
        )

        // Then: Male should have higher BMR
        assertTrue("Male BMR should be higher than female", maleResult.bmr > femaleResult.bmr)
        assertTrue("Male TDEE should be higher than female", maleResult.tdee > femaleResult.tdee)
    }

    @Test
    fun `activity level should affect TDEE`() = runTest {
        // Given: Same user with different activity levels
        val bodyWeight = 75f
        val height = 175f
        val age = 28
        val gender = Gender.MALE

        // When: Calculating with different activity levels
        val sedentaryResult = nutritionManager.calculateDailyMacros(
            bodyWeight, height, age, gender, ActivityLevel.SEDENTARY, NutritionGoal.MAINTENANCE
        )
        val activeResult = nutritionManager.calculateDailyMacros(
            bodyWeight, height, age, gender, ActivityLevel.VERY_ACTIVE, NutritionGoal.MAINTENANCE
        )

        // Then: Higher activity should result in higher TDEE
        assertEquals("BMR should be same", sedentaryResult.bmr, activeResult.bmr)
        assertTrue("Very active TDEE should be higher", activeResult.tdee > sedentaryResult.tdee)
        assertTrue("Very active calories should be higher", activeResult.targetCalories > sedentaryResult.targetCalories)
    }
}