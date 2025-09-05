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
 * Unit tests for MealPlanningManager
 * Tests weekly meal plans, portion scaling, and dietary restrictions
 */
class MealPlanningManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var database: AppDatabase

    private lateinit var mealPlanningManager: MealPlanningManager

    private val sampleMacros = MacroCalculationResult(
        targetCalories = 2000,
        proteinGrams = 150,
        carbsGrams = 200,
        fatGrams = 70,
        bmr = 1600,
        tdee = 2000,
        goal = NutritionGoal.MAINTENANCE
    )

    private val sampleRecipe = Recipe(
        id = "recipe_1",
        name = "Chicken and Rice",
        servings = 2,
        prepTime = 15,
        cookingTime = 25,
        difficulty = CookingSkillLevel.BEGINNER,
        ingredients = listOf(
            RecipeIngredient("Chicken Breast", 300f, "g", IngredientCategory.PROTEIN),
            RecipeIngredient("Rice", 150f, "g", IngredientCategory.GRAIN),
            RecipeIngredient("Broccoli", 200f, "g", IngredientCategory.VEGETABLE)
        ),
        instructions = listOf("Cook chicken", "Prepare rice", "Steam broccoli"),
        nutrition = NutritionInfo(450, 35, 40, 12, 3, 2, 300),
        tags = listOf("meat", "gluten-free")
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mealPlanningManager = MealPlanningManager(context, database)
    }

    // Weekly Meal Plan Generation Tests

    @Test
    fun `should generate weekly meal plans`() = runTest {
        // Given: Meal planning parameters
        val dietaryRestrictions = listOf(DietaryRestriction.GLUTEN_FREE)
        val cookingSkillLevel = CookingSkillLevel.INTERMEDIATE
        val availablePrepTime = 60
        val mealPreferences = MealPreferences(
            favoriteProteins = listOf("chicken", "fish"),
            dislikedIngredients = listOf("mushrooms"),
            preferredCuisines = listOf("mediterranean"),
            spiceLevel = SpiceLevel.MEDIUM
        )

        // When: Generating weekly meal plan
        val result = mealPlanningManager.generateWeeklyMealPlan(
            sampleMacros, dietaryRestrictions, cookingSkillLevel, availablePrepTime, mealPreferences
        )

        // Then: Should generate complete weekly plan
        assertNotNull("Weekly plan should not be null", result)
        assertEquals("Should have 7 days", 7, result.weekPlan.size)
        assertTrue("Should have reasonable total prep time", result.totalPrepTime > 0)
        assertNotNull("Should have shopping list", result.shoppingList)
        assertNotNull("Should have nutrition summary", result.nutritionSummary)
        assertTrue("Should have meal prep tips", result.mealPrepTips.isNotEmpty())
        
        // Verify all days are covered
        DayOfWeek.values().forEach { day ->
            assertTrue("Should have plan for $day", result.weekPlan.containsKey(day))
            val dailyPlan = result.weekPlan[day]!!
            assertTrue("Should have meals for $day", dailyPlan.meals.isNotEmpty())
        }
    }

    @Test
    fun `should generate realistic daily meal plans`() = runTest {
        // Given: Meal planning parameters
        val dietaryRestrictions = emptyList<DietaryRestriction>()
        val cookingSkillLevel = CookingSkillLevel.BEGINNER
        val availablePrepTime = 30
        val mealPreferences = MealPreferences(
            favoriteProteins = listOf("chicken"),
            dislikedIngredients = emptyList(),
            preferredCuisines = listOf("simple"),
            spiceLevel = SpiceLevel.MILD
        )

        // When: Generating weekly meal plan
        val result = mealPlanningManager.generateWeeklyMealPlan(
            sampleMacros, dietaryRestrictions, cookingSkillLevel, availablePrepTime, mealPreferences
        )

        // Then: Daily plans should be realistic
        result.weekPlan.values.forEach { dailyPlan ->
            assertTrue("Daily prep time should be reasonable", dailyPlan.totalPrepTime <= 120) // Max 2 hours
            assertTrue("Should have at least 2 meals", dailyPlan.meals.size >= 2)
            assertTrue("Daily calories should be reasonable", 
                dailyPlan.totalNutrition.calories in 1500..2500)
            assertTrue("Should have adequate protein", dailyPlan.totalNutrition.protein >= 100)
        }
    }

    @Test
    fun `generateWeeklyMealPlan should validate input parameters`() = runTest {
        val dietaryRestrictions = emptyList<DietaryRestriction>()
        val cookingSkillLevel = CookingSkillLevel.BEGINNER
        val mealPreferences = MealPreferences(
            favoriteProteins = listOf("chicken"),
            dislikedIngredients = emptyList(),
            preferredCuisines = listOf("simple"),
            spiceLevel = SpiceLevel.MILD
        )

        // Test negative prep time
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                mealPlanningManager.generateWeeklyMealPlan(
                    sampleMacros, dietaryRestrictions, cookingSkillLevel, -30, mealPreferences
                )
            }
        }
    }

    // Portion Scaling Tests

    @Test
    fun `should adjust portions for serving sizes correctly`() {
        // Given: Recipe for 2 servings, want 4 servings
        val targetServings = 4

        // When: Adjusting portions
        val result = mealPlanningManager.adjustPortionsForServingSize(sampleRecipe, targetServings)

        // Then: Should scale everything proportionally
        assertEquals("Original recipe should be preserved", sampleRecipe, result.originalRecipe)
        assertEquals("Target servings should match", targetServings, result.targetServings)
        assertEquals("Scaling factor should be 2.0", 2.0f, result.scalingFactor, 0.01f)
        
        // Check ingredients scaling
        assertEquals("Should have same number of ingredients", 
            sampleRecipe.ingredients.size, result.scaledIngredients.size)
        
        result.scaledIngredients.forEachIndexed { index, scaledIngredient ->
            val originalIngredient = sampleRecipe.ingredients[index]
            assertEquals("Ingredient name should match", originalIngredient.name, scaledIngredient.name)
            assertEquals("Amount should be doubled", 
                originalIngredient.amount * 2, scaledIngredient.scaledAmount, 0.01f)
            assertEquals("Unit should remain same", originalIngredient.unit, scaledIngredient.unit)
        }
        
        // Check nutrition scaling
        assertEquals("Calories should be doubled", 
            sampleRecipe.nutrition.calories * 2, result.scaledNutrition.calories)
        assertEquals("Protein should be doubled", 
            sampleRecipe.nutrition.protein * 2, result.scaledNutrition.protein)
        assertEquals("Carbs should be doubled", 
            sampleRecipe.nutrition.carbs * 2, result.scaledNutrition.carbs)
        assertEquals("Fat should be doubled", 
            sampleRecipe.nutrition.fat * 2, result.scaledNutrition.fat)
    }

    @Test
    fun `should adjust cooking time appropriately for larger servings`() {
        // Given: Recipe with 40 minutes cooking time, scaling to 8 servings
        val targetServings = 8 // 4x scaling

        // When: Adjusting portions
        val result = mealPlanningManager.adjustPortionsForServingSize(sampleRecipe, targetServings)

        // Then: Cooking time should increase but not linearly
        assertTrue("Cooking time should increase", result.adjustedCookingTime > sampleRecipe.cookingTime)
        assertTrue("Cooking time should not quadruple", 
            result.adjustedCookingTime < sampleRecipe.cookingTime * 4)
    }

    @Test
    fun `should provide portion notes for significant scaling`() {
        // Test large scaling
        val largeResult = mealPlanningManager.adjustPortionsForServingSize(sampleRecipe, 10)
        assertTrue("Should have notes for large portions", largeResult.portionNotes.isNotEmpty())
        
        // Test small scaling
        val smallResult = mealPlanningManager.adjustPortionsForServingSize(sampleRecipe, 1)
        assertTrue("Should have notes for small portions", smallResult.portionNotes.isNotEmpty())
    }

    @Test
    fun `adjustPortionsForServingSize should validate input parameters`() {
        // Test negative target servings
        assertThrows(IllegalArgumentException::class.java) {
            mealPlanningManager.adjustPortionsForServingSize(sampleRecipe, -2)
        }

        // Test zero target servings
        assertThrows(IllegalArgumentException::class.java) {
            mealPlanningManager.adjustPortionsForServingSize(sampleRecipe, 0)
        }
    }

    // Dietary Restrictions Tests

    @Test
    fun `should handle dietary restrictions correctly - vegetarian`() {
        // Given: Mix of vegetarian and meat recipes
        val meatRecipe = sampleRecipe // Contains "meat" tag
        val vegetarianRecipe = sampleRecipe.copy(
            id = "veg_recipe",
            name = "Vegetable Curry",
            tags = listOf("vegetarian"),
            ingredients = listOf(
                RecipeIngredient("Tofu", 200f, "g", IngredientCategory.PROTEIN),
                RecipeIngredient("Vegetables", 300f, "g", IngredientCategory.VEGETABLE)
            )
        )
        val recipes = listOf(meatRecipe, vegetarianRecipe)
        val restrictions = listOf(DietaryRestriction.VEGETARIAN)

        // When: Filtering recipes
        val result = mealPlanningManager.filterRecipesForDietaryRestrictions(recipes, restrictions)

        // Then: Should only return vegetarian recipes
        assertEquals("Should filter out meat recipes", 1, result.size)
        assertEquals("Should keep vegetarian recipe", vegetarianRecipe.id, result.first().id)
    }

    @Test
    fun `should handle dietary restrictions correctly - vegan`() {
        // Given: Recipes with different animal product contents
        val veganRecipe = Recipe(
            id = "vegan_recipe",
            name = "Quinoa Salad", 
            servings = 2,
            prepTime = 15,
            cookingTime = 0,
            difficulty = CookingSkillLevel.BEGINNER,
            ingredients = listOf(
                RecipeIngredient("Quinoa", 100f, "g", IngredientCategory.GRAIN),
                RecipeIngredient("Vegetables", 200f, "g", IngredientCategory.VEGETABLE)
            ),
            instructions = listOf("Mix ingredients"),
            nutrition = NutritionInfo(300, 12, 45, 8, 5, 3, 200),
            tags = listOf("vegan")
        )
        
        val dairyRecipe = Recipe(
            id = "dairy_recipe",
            name = "Cheese Pasta",
            servings = 2,
            prepTime = 10,
            cookingTime = 15,
            difficulty = CookingSkillLevel.BEGINNER,
            ingredients = listOf(
                RecipeIngredient("Pasta", 200f, "g", IngredientCategory.GRAIN),
                RecipeIngredient("Cheese", 100f, "g", IngredientCategory.DAIRY)
            ),
            instructions = listOf("Cook pasta", "Add cheese"),
            nutrition = NutritionInfo(400, 18, 50, 15, 2, 4, 400),
            tags = emptyList()
        )
        
        val recipes = listOf(veganRecipe, dairyRecipe)
        val restrictions = listOf(DietaryRestriction.VEGAN)

        // When: Filtering recipes
        val result = mealPlanningManager.filterRecipesForDietaryRestrictions(recipes, restrictions)

        // Then: Should only return vegan recipes
        assertEquals("Should filter out dairy recipes", 1, result.size)
        assertEquals("Should keep vegan recipe", veganRecipe.id, result.first().id)
    }

    @Test
    fun `should handle dietary restrictions correctly - gluten free`() {
        // Given: Recipes with and without gluten
        val glutenFreeRecipe = sampleRecipe // Has "gluten-free" tag
        val glutenRecipe = sampleRecipe.copy(
            id = "gluten_recipe",
            name = "Wheat Pasta",
            ingredients = listOf(
                RecipeIngredient("Wheat Flour", 200f, "g", IngredientCategory.GRAIN)
            ),
            tags = emptyList()
        )
        val recipes = listOf(glutenFreeRecipe, glutenRecipe)
        val restrictions = listOf(DietaryRestriction.GLUTEN_FREE)

        // When: Filtering recipes
        val result = mealPlanningManager.filterRecipesForDietaryRestrictions(recipes, restrictions)

        // Then: Should only return gluten-free recipes
        assertEquals("Should filter out gluten recipes", 1, result.size)
        assertEquals("Should keep gluten-free recipe", glutenFreeRecipe.id, result.first().id)
    }

    @Test
    fun `should handle multiple dietary restrictions`() {
        // Given: Recipe that meets multiple restrictions
        val compliantRecipe = Recipe(
            id = "compliant_recipe",
            name = "Vegan Gluten-Free Bowl",
            servings = 1,
            prepTime = 20,
            cookingTime = 0,
            difficulty = CookingSkillLevel.INTERMEDIATE,
            ingredients = listOf(
                RecipeIngredient("Quinoa", 100f, "g", IngredientCategory.GRAIN),
                RecipeIngredient("Vegetables", 200f, "g", IngredientCategory.VEGETABLE)
            ),
            instructions = listOf("Prepare bowl"),
            nutrition = NutritionInfo(350, 15, 50, 10, 8, 5, 150),
            tags = listOf("vegan", "gluten-free")
        )
        
        val nonCompliantRecipe = sampleRecipe // Contains meat and may have gluten
        val recipes = listOf(compliantRecipe, nonCompliantRecipe)
        val restrictions = listOf(DietaryRestriction.VEGAN, DietaryRestriction.GLUTEN_FREE)

        // When: Filtering recipes
        val result = mealPlanningManager.filterRecipesForDietaryRestrictions(recipes, restrictions)

        // Then: Should only return recipes meeting all restrictions
        assertEquals("Should filter strictly", 1, result.size)
        assertEquals("Should keep compliant recipe", compliantRecipe.id, result.first().id)
    }

    @Test
    fun `should handle keto dietary restriction`() {
        // Given: Low-carb and high-carb recipes
        val ketoRecipe = sampleRecipe.copy(
            id = "keto_recipe",
            nutrition = NutritionInfo(400, 25, 8, 30, 3, 2, 200) // 8g carbs - keto compliant
        )
        val highCarbRecipe = sampleRecipe.copy(
            id = "high_carb_recipe", 
            nutrition = NutritionInfo(400, 20, 60, 10, 5, 10, 300) // 60g carbs - not keto
        )
        val recipes = listOf(ketoRecipe, highCarbRecipe)
        val restrictions = listOf(DietaryRestriction.KETO)

        // When: Filtering recipes
        val result = mealPlanningManager.filterRecipesForDietaryRestrictions(recipes, restrictions)

        // Then: Should only return keto recipes
        assertEquals("Should filter out high-carb recipes", 1, result.size)
        assertEquals("Should keep keto recipe", ketoRecipe.id, result.first().id)
    }

    @Test
    fun `should return all recipes when no restrictions`() {
        // Given: Multiple recipes and no restrictions
        val recipes = listOf(sampleRecipe, sampleRecipe.copy(id = "recipe_2"))
        val restrictions = emptyList<DietaryRestriction>()

        // When: Filtering recipes
        val result = mealPlanningManager.filterRecipesForDietaryRestrictions(recipes, restrictions)

        // Then: Should return all recipes
        assertEquals("Should return all recipes", 2, result.size)
    }

    // Prep Time Optimization Tests

    @Test
    fun `should optimize prep time scheduling`() = runTest {
        // Given: Weekly meal plan with varying prep times
        val weeklyPlan = createSampleWeeklyPlan()
        val maxDailyPrepTime = 60

        // When: Optimizing prep time
        val result = mealPlanningManager.optimizePrepTimeScheduling(weeklyPlan, maxDailyPrepTime)

        // Then: Should provide optimization insights
        assertNotNull("Optimization result should not be null", result)
        assertEquals("Should analyze all 7 days", 7, result.originalPrepTimes.size)
        assertTrue("Should identify batch opportunities", result.batchCookingOpportunities.isNotEmpty())
        assertTrue("Should suggest meal prep days if needed", result.mealPrepDays.size >= 0)
        assertTrue("Should provide recommendations", result.recommendations.isNotEmpty())
        assertTrue("Should calculate time savings", result.timeSavings >= 0)
    }

    @Test
    fun `should identify days exceeding prep time limits`() = runTest {
        // Given: Weekly plan with some high prep time days
        val weeklyPlan = createWeeklyPlanWithHighPrepDays()
        val maxDailyPrepTime = 45

        // When: Optimizing prep time
        val result = mealPlanningManager.optimizePrepTimeScheduling(weeklyPlan, maxDailyPrepTime)

        // Then: Should identify problematic days
        assertTrue("Should identify meal prep days", result.mealPrepDays.isNotEmpty())
        assertTrue("Should recommend solutions", 
            result.recommendations.any { it.contains("Meal Prep") })
    }

    @Test
    fun `should find batch cooking opportunities`() = runTest {
        // Given: Weekly plan with repeated ingredients
        val weeklyPlan = createWeeklyPlanWithRepeatedIngredients()

        // When: Optimizing prep time
        val result = mealPlanningManager.optimizePrepTimeScheduling(weeklyPlan, 60)

        // Then: Should identify batch cooking opportunities
        assertTrue("Should find batch opportunities", result.batchCookingOpportunities.isNotEmpty())
        val batchOpp = result.batchCookingOpportunities.first()
        assertTrue("Should specify ingredient", batchOpp.ingredient.isNotEmpty())
        assertTrue("Should specify usage days", batchOpp.usageDays.isNotEmpty())
        assertTrue("Should estimate time savings", batchOpp.estimatedTimeSaving > 0)
    }

    // Helper methods for creating test data

    private fun createSampleWeeklyPlan(): WeeklyMealPlan {
        val dailyPlan = DailyMealPlan(
            dayOfWeek = DayOfWeek.MONDAY,
            meals = listOf(
                PlannedMeal(
                    mealType = MealType.BREAKFAST,
                    recipe = sampleRecipe,
                    targetMacros = MacroTarget(500, 25, 50, 20)
                )
            ),
            totalNutrition = NutritionInfo(500, 25, 50, 20, 5, 10, 300),
            totalPrepTime = 40,
            macroBalance = MacroBalance(0, 0, 0, 0, true)
        )
        
        val weekPlan = DayOfWeek.values().associateWith { dailyPlan.copy(dayOfWeek = it) }
        
        return WeeklyMealPlan(
            weekPlan = weekPlan,
            totalPrepTime = 280, // 40 * 7 days
            shoppingList = ShoppingList(emptyList(), 0f, 0),
            nutritionSummary = WeeklyNutritionSummary(500, 25, 50, 20, NutritionVariability(10, true)),
            mealPrepTips = listOf("Sample tip")
        )
    }

    private fun createWeeklyPlanWithHighPrepDays(): WeeklyMealPlan {
        val highPrepPlan = DailyMealPlan(
            dayOfWeek = DayOfWeek.SUNDAY,
            meals = listOf(
                PlannedMeal(
                    mealType = MealType.DINNER,
                    recipe = sampleRecipe.copy(prepTime = 60, cookingTime = 40), // 100 minutes total
                    targetMacros = MacroTarget(600, 30, 60, 25)
                )
            ),
            totalNutrition = NutritionInfo(600, 30, 60, 25, 5, 10, 300),
            totalPrepTime = 100,
            macroBalance = MacroBalance(0, 0, 0, 0, true)
        )
        
        val normalPlan = DailyMealPlan(
            dayOfWeek = DayOfWeek.MONDAY,
            meals = listOf(
                PlannedMeal(
                    mealType = MealType.LUNCH,
                    recipe = sampleRecipe,
                    targetMacros = MacroTarget(450, 25, 45, 20)
                )
            ),
            totalNutrition = NutritionInfo(450, 25, 45, 20, 5, 10, 300),
            totalPrepTime = 40,
            macroBalance = MacroBalance(0, 0, 0, 0, true)
        )
        
        val weekPlan = mapOf(
            DayOfWeek.SUNDAY to highPrepPlan,
            DayOfWeek.MONDAY to normalPlan,
            DayOfWeek.TUESDAY to normalPlan.copy(dayOfWeek = DayOfWeek.TUESDAY),
            DayOfWeek.WEDNESDAY to normalPlan.copy(dayOfWeek = DayOfWeek.WEDNESDAY),
            DayOfWeek.THURSDAY to normalPlan.copy(dayOfWeek = DayOfWeek.THURSDAY),
            DayOfWeek.FRIDAY to normalPlan.copy(dayOfWeek = DayOfWeek.FRIDAY),
            DayOfWeek.SATURDAY to normalPlan.copy(dayOfWeek = DayOfWeek.SATURDAY)
        )
        
        return WeeklyMealPlan(
            weekPlan = weekPlan,
            totalPrepTime = 340,
            shoppingList = ShoppingList(emptyList(), 0f, 0),
            nutritionSummary = WeeklyNutritionSummary(480, 26, 48, 21, NutritionVariability(15, true)),
            mealPrepTips = listOf("High prep day tip")
        )
    }

    private fun createWeeklyPlanWithRepeatedIngredients(): WeeklyMealPlan {
        val chickenRecipe = sampleRecipe // Contains chicken breast
        val dailyPlan = DailyMealPlan(
            dayOfWeek = DayOfWeek.MONDAY,
            meals = listOf(
                PlannedMeal(
                    mealType = MealType.DINNER,
                    recipe = chickenRecipe,
                    targetMacros = MacroTarget(450, 25, 45, 20)
                )
            ),
            totalNutrition = NutritionInfo(450, 25, 45, 20, 5, 10, 300),
            totalPrepTime = 40,
            macroBalance = MacroBalance(0, 0, 0, 0, true)
        )
        
        // Use chicken recipe for multiple days
        val weekPlan = mapOf(
            DayOfWeek.MONDAY to dailyPlan,
            DayOfWeek.WEDNESDAY to dailyPlan.copy(dayOfWeek = DayOfWeek.WEDNESDAY),
            DayOfWeek.FRIDAY to dailyPlan.copy(dayOfWeek = DayOfWeek.FRIDAY),
            DayOfWeek.TUESDAY to dailyPlan.copy(dayOfWeek = DayOfWeek.TUESDAY),
            DayOfWeek.THURSDAY to dailyPlan.copy(dayOfWeek = DayOfWeek.THURSDAY),
            DayOfWeek.SATURDAY to dailyPlan.copy(dayOfWeek = DayOfWeek.SATURDAY),
            DayOfWeek.SUNDAY to dailyPlan.copy(dayOfWeek = DayOfWeek.SUNDAY)
        )
        
        return WeeklyMealPlan(
            weekPlan = weekPlan,
            totalPrepTime = 280,
            shoppingList = ShoppingList(emptyList(), 0f, 0),
            nutritionSummary = WeeklyNutritionSummary(450, 25, 45, 20, NutritionVariability(5, true)),
            mealPrepTips = listOf("Batch cooking tip")
        )
    }
}