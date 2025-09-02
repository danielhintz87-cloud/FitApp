package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * MealPlanningManager for weekly meal plan generation and management
 * Handles portion scaling, dietary restrictions, and prep time optimization
 */
class MealPlanningManager(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.get(context)
) {
    
    companion object {
        private const val TAG = "MealPlanningManager"
        private const val MEALS_PER_DAY = 4
        private const val DEFAULT_PREP_TIME_MINUTES = 30
        private const val MAX_DAILY_PREP_TIME = 120
    }
    
    /**
     * Generate weekly meal plans based on macro targets and preferences
     */
    suspend fun generateWeeklyMealPlan(
        dailyMacros: MacroCalculationResult,
        dietaryRestrictions: List<DietaryRestriction>,
        cookingSkillLevel: CookingSkillLevel,
        availablePrepTime: Int,
        mealPreferences: MealPreferences
    ): WeeklyMealPlan = withContext(Dispatchers.IO) {
        try {
            require(availablePrepTime > 0) { "Available prep time must be positive" }
            
            val weeklyPlan = mutableMapOf<DayOfWeek, DailyMealPlan>()
            
            DayOfWeek.values().forEach { day ->
                val dailyPlan = generateDailyMealPlan(
                    dailyMacros = dailyMacros,
                    dietaryRestrictions = dietaryRestrictions,
                    cookingSkillLevel = cookingSkillLevel,
                    availablePrepTime = availablePrepTime,
                    preferences = mealPreferences,
                    dayOfWeek = day
                )
                weeklyPlan[day] = dailyPlan
            }
            
            WeeklyMealPlan(
                weekPlan = weeklyPlan,
                totalPrepTime = calculateTotalPrepTime(weeklyPlan),
                shoppingList = generateShoppingList(weeklyPlan),
                nutritionSummary = calculateWeeklyNutritionSummary(weeklyPlan),
                mealPrepTips = generateMealPrepTips(weeklyPlan, cookingSkillLevel)
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to generate weekly meal plan",
                exception = e
            )
            WeeklyMealPlan.empty()
        }
    }
    
    /**
     * Adjust portions for different serving sizes
     */
    fun adjustPortionsForServingSize(
        recipe: Recipe,
        targetServings: Int
    ): ScaledRecipe {
        return try {
            require(targetServings > 0) { "Target servings must be positive" }
            require(recipe.servings > 0) { "Recipe servings must be positive" }
            
            val scalingFactor = targetServings.toFloat() / recipe.servings
            
            val scaledIngredients = recipe.ingredients.map { ingredient ->
                ScaledIngredient(
                    name = ingredient.name,
                    originalAmount = ingredient.amount,
                    scaledAmount = ingredient.amount * scalingFactor,
                    unit = ingredient.unit,
                    category = ingredient.category
                )
            }
            
            val scaledNutrition = NutritionInfo(
                calories = (recipe.nutrition.calories * scalingFactor).roundToInt(),
                protein = (recipe.nutrition.protein * scalingFactor).roundToInt(),
                carbs = (recipe.nutrition.carbs * scalingFactor).roundToInt(),
                fat = (recipe.nutrition.fat * scalingFactor).roundToInt(),
                fiber = (recipe.nutrition.fiber * scalingFactor).roundToInt(),
                sugar = (recipe.nutrition.sugar * scalingFactor).roundToInt()
            )
            
            ScaledRecipe(
                originalRecipe = recipe,
                targetServings = targetServings,
                scalingFactor = scalingFactor,
                scaledIngredients = scaledIngredients,
                scaledNutrition = scaledNutrition,
                adjustedCookingTime = adjustCookingTimeForServings(recipe.cookingTime, scalingFactor),
                portionNotes = generatePortionNotes(scalingFactor)
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to adjust portions",
                exception = e
            )
            ScaledRecipe.error(recipe)
        }
    }
    
    /**
     * Handle dietary restrictions in meal planning
     */
    fun filterRecipesForDietaryRestrictions(
        recipes: List<Recipe>,
        restrictions: List<DietaryRestriction>
    ): List<Recipe> {
        return try {
            if (restrictions.isEmpty()) return recipes
            
            recipes.filter { recipe ->
                restrictions.all { restriction ->
                    when (restriction) {
                        DietaryRestriction.VEGETARIAN -> !recipe.containsMeat()
                        DietaryRestriction.VEGAN -> !recipe.containsAnimalProducts()
                        DietaryRestriction.GLUTEN_FREE -> !recipe.containsGluten()
                        DietaryRestriction.DAIRY_FREE -> !recipe.containsDairy()
                        DietaryRestriction.LOW_SODIUM -> recipe.nutrition.sodium <= 600 // mg per serving
                        DietaryRestriction.KETO -> recipe.nutrition.carbs <= 10 // net carbs
                        DietaryRestriction.PALEO -> recipe.isPaleoCompliant()
                        DietaryRestriction.LOW_CARB -> recipe.nutrition.carbs <= 30
                        DietaryRestriction.HIGH_PROTEIN -> (recipe.nutrition.protein * 4) >= (recipe.nutrition.calories * 0.25)
                    }
                }
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to filter recipes for dietary restrictions",
                exception = e
            )
            emptyList()
        }
    }
    
    /**
     * Optimize prep time scheduling across the week
     */
    suspend fun optimizePrepTimeScheduling(
        weeklyPlan: WeeklyMealPlan,
        maxDailyPrepTime: Int = MAX_DAILY_PREP_TIME
    ): PrepTimeOptimization = withContext(Dispatchers.IO) {
        try {
            val dailyPrepTimes = weeklyPlan.weekPlan.mapValues { (_, dailyPlan) ->
                dailyPlan.meals.sumOf { it.recipe.prepTime + it.recipe.cookingTime }
            }
            
            val totalPrepTime = dailyPrepTimes.values.sum()
            val averageDailyPrepTime = totalPrepTime / 7
            
            val batchCookingOpportunities = findBatchCookingOpportunities(weeklyPlan)
            val mealPrepDays = suggestMealPrepDays(dailyPrepTimes, maxDailyPrepTime)
            
            PrepTimeOptimization(
                originalPrepTimes = dailyPrepTimes,
                optimizedSchedule = createOptimizedSchedule(weeklyPlan, batchCookingOpportunities),
                batchCookingOpportunities = batchCookingOpportunities,
                mealPrepDays = mealPrepDays,
                timeSavings = calculateTimeSavings(batchCookingOpportunities),
                recommendations = generatePrepTimeRecommendations(dailyPrepTimes, maxDailyPrepTime)
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to optimize prep time scheduling",
                exception = e
            )
            PrepTimeOptimization.empty()
        }
    }
    
    /**
     * Generate shopping list from meal plan
     */
    private fun generateShoppingList(weeklyPlan: Map<DayOfWeek, DailyMealPlan>): ShoppingList {
        val ingredientTotals = mutableMapOf<String, ShoppingItem>()
        
        weeklyPlan.values.forEach { dailyPlan ->
            dailyPlan.meals.forEach { meal ->
                meal.recipe.ingredients.forEach { ingredient ->
                    val key = "${ingredient.name}_${ingredient.unit}"
                    if (ingredientTotals.containsKey(key)) {
                        val existing = ingredientTotals[key]!!
                        ingredientTotals[key] = existing.copy(
                            amount = existing.amount + ingredient.amount
                        )
                    } else {
                        ingredientTotals[key] = ShoppingItem(
                            name = ingredient.name,
                            amount = ingredient.amount,
                            unit = ingredient.unit,
                            category = ingredient.category,
                            isPurchased = false
                        )
                    }
                }
            }
        }
        
        return ShoppingList(
            items = ingredientTotals.values.sortedBy { it.category },
            totalEstimatedCost = estimateShoppingCost(ingredientTotals.values.toList()),
            estimatedShoppingTime = estimateShoppingTime(ingredientTotals.size)
        )
    }
    
    private fun generateDailyMealPlan(
        dailyMacros: MacroCalculationResult,
        dietaryRestrictions: List<DietaryRestriction>,
        cookingSkillLevel: CookingSkillLevel,
        availablePrepTime: Int,
        preferences: MealPreferences,
        dayOfWeek: DayOfWeek
    ): DailyMealPlan {
        // Simplified meal plan generation - in real implementation would use recipe database
        val meals = listOf(
            PlannedMeal(
                mealType = MealType.BREAKFAST,
                recipe = createSampleRecipe("Oatmeal with Berries", 350, 12, 50, 8),
                targetMacros = MacroTarget(
                    calories = dailyMacros.targetCalories / 4,
                    protein = dailyMacros.proteinGrams / 4,
                    carbs = dailyMacros.carbsGrams / 4,
                    fat = dailyMacros.fatGrams / 4
                )
            ),
            PlannedMeal(
                mealType = MealType.LUNCH,
                recipe = createSampleRecipe("Chicken Salad", 450, 35, 20, 25),
                targetMacros = MacroTarget(
                    calories = dailyMacros.targetCalories / 3,
                    protein = dailyMacros.proteinGrams / 3,
                    carbs = dailyMacros.carbsGrams / 3,
                    fat = dailyMacros.fatGrams / 3
                )
            )
        )
        
        return DailyMealPlan(
            dayOfWeek = dayOfWeek,
            meals = meals,
            totalNutrition = calculateDailyNutrition(meals),
            totalPrepTime = meals.sumOf { it.recipe.prepTime + it.recipe.cookingTime },
            macroBalance = calculateMacroBalance(meals, dailyMacros)
        )
    }
    
    private fun createSampleRecipe(name: String, calories: Int, protein: Int, carbs: Int, fat: Int): Recipe {
        return Recipe(
            id = "sample_${name.hashCode()}",
            name = name,
            servings = 1,
            prepTime = 15,
            cookingTime = 20,
            difficulty = CookingSkillLevel.BEGINNER,
            ingredients = listOf(
                RecipeIngredient(name, 1.0f, "serving", IngredientCategory.PROTEIN)
            ),
            instructions = listOf("Sample instruction"),
            nutrition = NutritionInfo(calories, protein, carbs, fat, 5, 10, 200),
            tags = emptyList()
        )
    }
    
    private fun calculateDailyNutrition(meals: List<PlannedMeal>): NutritionInfo {
        return NutritionInfo(
            calories = meals.sumOf { it.recipe.nutrition.calories },
            protein = meals.sumOf { it.recipe.nutrition.protein },
            carbs = meals.sumOf { it.recipe.nutrition.carbs },
            fat = meals.sumOf { it.recipe.nutrition.fat },
            fiber = meals.sumOf { it.recipe.nutrition.fiber },
            sugar = meals.sumOf { it.recipe.nutrition.sugar },
            sodium = meals.sumOf { it.recipe.nutrition.sodium }
        )
    }
    
    private fun calculateMacroBalance(meals: List<PlannedMeal>, target: MacroCalculationResult): MacroBalance {
        val actual = calculateDailyNutrition(meals)
        return MacroBalance(
            calorieBalance = actual.calories - target.targetCalories,
            proteinBalance = actual.protein - target.proteinGrams,
            carbBalance = actual.carbs - target.carbsGrams,
            fatBalance = actual.fat - target.fatGrams,
            isBalanced = kotlin.math.abs(actual.calories - target.targetCalories) <= target.targetCalories * 0.1
        )
    }
    
    private fun calculateTotalPrepTime(weeklyPlan: Map<DayOfWeek, DailyMealPlan>): Int {
        return weeklyPlan.values.sumOf { it.totalPrepTime }
    }
    
    private fun calculateWeeklyNutritionSummary(weeklyPlan: Map<DayOfWeek, DailyMealPlan>): WeeklyNutritionSummary {
        val dailyNutritions = weeklyPlan.values.map { it.totalNutrition }
        return WeeklyNutritionSummary(
            averageCalories = dailyNutritions.map { it.calories }.average().toInt(),
            averageProtein = dailyNutritions.map { it.protein }.average().toInt(),
            averageCarbs = dailyNutritions.map { it.carbs }.average().toInt(),
            averageFat = dailyNutritions.map { it.fat }.average().toInt(),
            weeklyVariability = calculateNutritionVariability(dailyNutritions)
        )
    }
    
    private fun calculateNutritionVariability(nutritions: List<NutritionInfo>): NutritionVariability {
        val calories = nutritions.map { it.calories }
        val calorieVariance = calories.map { (it - calories.average()).let { diff -> diff * diff } }.average()
        
        return NutritionVariability(
            calorieStandardDeviation = kotlin.math.sqrt(calorieVariance).toInt(),
            isConsistent = calorieVariance < (calories.average() * 0.1).let { it * it }
        )
    }
    
    private fun generateMealPrepTips(weeklyPlan: Map<DayOfWeek, DailyMealPlan>, skillLevel: CookingSkillLevel): List<String> {
        val tips = mutableListOf<String>()
        
        when (skillLevel) {
            CookingSkillLevel.BEGINNER -> {
                tips.add("Bereite einfache Zutaten am Wochenende vor")
                tips.add("Verwende einen Slow Cooker für einfache Mahlzeiten")
            }
            CookingSkillLevel.INTERMEDIATE -> {
                tips.add("Batch-Cooking für Proteine am Sonntag")
                tips.add("Schneide Gemüse im Voraus")
            }
            CookingSkillLevel.ADVANCED -> {
                tips.add("Komplexe Saucen können eingefroren werden")
                tips.add("Sous-vide Meal Prep für optimale Textur")
            }
        }
        
        return tips
    }
    
    private fun adjustCookingTimeForServings(originalTime: Int, scalingFactor: Float): Int {
        // Cooking time doesn't scale linearly with servings
        return when {
            scalingFactor <= 1f -> originalTime
            scalingFactor <= 2f -> (originalTime * 1.2f).roundToInt()
            scalingFactor <= 4f -> (originalTime * 1.4f).roundToInt()
            else -> (originalTime * 1.6f).roundToInt()
        }
    }
    
    private fun generatePortionNotes(scalingFactor: Float): List<String> {
        val notes = mutableListOf<String>()
        
        when {
            scalingFactor < 0.5f -> notes.add("Sehr kleine Portionen - Kochzeit verkürzen")
            scalingFactor > 3f -> notes.add("Große Portionen - eventuell in Chargen kochen")
            scalingFactor != 1f -> notes.add("Portionsgrößen angepasst - Geschmack prüfen")
        }
        
        return notes
    }
    
    private fun findBatchCookingOpportunities(weeklyPlan: WeeklyMealPlan): List<BatchCookingOpportunity> {
        // Simplified implementation - would analyze similar recipes/ingredients
        return listOf(
            BatchCookingOpportunity(
                ingredient = "Chicken Breast",
                totalAmount = 2000f,
                unit = "g",
                usageDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                estimatedTimeSaving = 45
            )
        )
    }
    
    private fun suggestMealPrepDays(dailyPrepTimes: Map<DayOfWeek, Int>, maxDailyPrepTime: Int): List<DayOfWeek> {
        return dailyPrepTimes.filterValues { it > maxDailyPrepTime }.keys.toList()
    }
    
    private fun createOptimizedSchedule(
        weeklyPlan: WeeklyMealPlan,
        batchOpportunities: List<BatchCookingOpportunity>
    ): Map<DayOfWeek, Int> {
        // Simplified - would redistribute prep time based on batch cooking
        return weeklyPlan.weekPlan.mapValues { (_, dailyPlan) -> dailyPlan.totalPrepTime }
    }
    
    private fun calculateTimeSavings(batchOpportunities: List<BatchCookingOpportunity>): Int {
        return batchOpportunities.sumOf { it.estimatedTimeSaving }
    }
    
    private fun generatePrepTimeRecommendations(
        dailyPrepTimes: Map<DayOfWeek, Int>,
        maxDailyPrepTime: Int
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        val overTimeDays = dailyPrepTimes.filterValues { it > maxDailyPrepTime }
        if (overTimeDays.isNotEmpty()) {
            recommendations.add("Meal Prep an ${overTimeDays.keys.joinToString()} empfohlen")
        }
        
        if (dailyPrepTimes.values.maxOrNull() ?: 0 > maxDailyPrepTime * 1.5) {
            recommendations.add("Batch Cooking für Proteine implementieren")
        }
        
        return recommendations
    }
    
    private fun estimateShoppingCost(items: List<ShoppingItem>): Float {
        // Simplified cost estimation
        return items.sumOf { 
            when (it.category) {
                IngredientCategory.PROTEIN -> it.amount * 0.02f // €0.02 per gram
                IngredientCategory.VEGETABLE -> it.amount * 0.005f
                IngredientCategory.GRAIN -> it.amount * 0.003f
                IngredientCategory.DAIRY -> it.amount * 0.008f
                IngredientCategory.SPICE -> it.amount * 0.05f
                IngredientCategory.OTHER -> it.amount * 0.01f
            }.toDouble()
        }.toFloat()
    }
    
    private fun estimateShoppingTime(itemCount: Int): Int {
        return (itemCount * 2) + 20 // 2 minutes per item + 20 minutes base time
    }
}

// Data classes for meal planning
data class WeeklyMealPlan(
    val weekPlan: Map<DayOfWeek, DailyMealPlan>,
    val totalPrepTime: Int,
    val shoppingList: ShoppingList,
    val nutritionSummary: WeeklyNutritionSummary,
    val mealPrepTips: List<String>
) {
    companion object {
        fun empty() = WeeklyMealPlan(
            weekPlan = emptyMap(),
            totalPrepTime = 0,
            shoppingList = ShoppingList(emptyList(), 0f, 0),
            nutritionSummary = WeeklyNutritionSummary(0, 0, 0, 0, NutritionVariability(0, true)),
            mealPrepTips = emptyList()
        )
    }
}

data class DailyMealPlan(
    val dayOfWeek: DayOfWeek,
    val meals: List<PlannedMeal>,
    val totalNutrition: NutritionInfo,
    val totalPrepTime: Int,
    val macroBalance: MacroBalance
)

data class PlannedMeal(
    val mealType: MealType,
    val recipe: Recipe,
    val targetMacros: MacroTarget
)

data class Recipe(
    val id: String,
    val name: String,
    val servings: Int,
    val prepTime: Int,
    val cookingTime: Int,
    val difficulty: CookingSkillLevel,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<String>,
    val nutrition: NutritionInfo,
    val tags: List<String>
) {
    fun containsMeat(): Boolean = tags.contains("meat") || ingredients.any { 
        it.name.lowercase().contains("chicken") || 
        it.name.lowercase().contains("beef") || 
        it.name.lowercase().contains("pork")
    }
    
    fun containsAnimalProducts(): Boolean = containsMeat() || containsDairy() || 
        ingredients.any { it.name.lowercase().contains("egg") }
    
    fun containsGluten(): Boolean = ingredients.any { 
        it.name.lowercase().contains("wheat") || 
        it.name.lowercase().contains("flour") ||
        it.name.lowercase().contains("bread")
    }
    
    fun containsDairy(): Boolean = ingredients.any { 
        it.name.lowercase().contains("milk") || 
        it.name.lowercase().contains("cheese") ||
        it.name.lowercase().contains("butter")
    }
    
    fun isPaleoCompliant(): Boolean = !containsGluten() && !containsDairy() && 
        !ingredients.any { it.name.lowercase().contains("bean") || it.name.lowercase().contains("rice") }
}

data class RecipeIngredient(
    val name: String,
    val amount: Float,
    val unit: String,
    val category: IngredientCategory
)

data class ScaledRecipe(
    val originalRecipe: Recipe,
    val targetServings: Int,
    val scalingFactor: Float,
    val scaledIngredients: List<ScaledIngredient>,
    val scaledNutrition: NutritionInfo,
    val adjustedCookingTime: Int,
    val portionNotes: List<String>
) {
    companion object {
        fun error(recipe: Recipe) = ScaledRecipe(
            originalRecipe = recipe,
            targetServings = recipe.servings,
            scalingFactor = 1f,
            scaledIngredients = emptyList(),
            scaledNutrition = recipe.nutrition,
            adjustedCookingTime = recipe.cookingTime,
            portionNotes = listOf("Fehler bei Portionsanpassung")
        )
    }
}

data class ScaledIngredient(
    val name: String,
    val originalAmount: Float,
    val scaledAmount: Float,
    val unit: String,
    val category: IngredientCategory
)

data class NutritionInfo(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val fiber: Int,
    val sugar: Int,
    val sodium: Int = 0
)

data class MacroTarget(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class MacroBalance(
    val calorieBalance: Int,
    val proteinBalance: Int,
    val carbBalance: Int,
    val fatBalance: Int,
    val isBalanced: Boolean
)

data class ShoppingList(
    val items: List<ShoppingItem>,
    val totalEstimatedCost: Float,
    val estimatedShoppingTime: Int
)

data class ShoppingItem(
    val name: String,
    val amount: Float,
    val unit: String,
    val category: IngredientCategory,
    val isPurchased: Boolean
)

data class WeeklyNutritionSummary(
    val averageCalories: Int,
    val averageProtein: Int,
    val averageCarbs: Int,
    val averageFat: Int,
    val weeklyVariability: NutritionVariability
)

data class NutritionVariability(
    val calorieStandardDeviation: Int,
    val isConsistent: Boolean
)

data class PrepTimeOptimization(
    val originalPrepTimes: Map<DayOfWeek, Int>,
    val optimizedSchedule: Map<DayOfWeek, Int>,
    val batchCookingOpportunities: List<BatchCookingOpportunity>,
    val mealPrepDays: List<DayOfWeek>,
    val timeSavings: Int,
    val recommendations: List<String>
) {
    companion object {
        fun empty() = PrepTimeOptimization(
            originalPrepTimes = emptyMap(),
            optimizedSchedule = emptyMap(),
            batchCookingOpportunities = emptyList(),
            mealPrepDays = emptyList(),
            timeSavings = 0,
            recommendations = emptyList()
        )
    }
}

data class BatchCookingOpportunity(
    val ingredient: String,
    val totalAmount: Float,
    val unit: String,
    val usageDays: List<DayOfWeek>,
    val estimatedTimeSaving: Int
)

data class MealPreferences(
    val favoriteProteins: List<String>,
    val dislikedIngredients: List<String>,
    val preferredCuisines: List<String>,
    val spiceLevel: SpiceLevel
)

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

enum class DietaryRestriction {
    VEGETARIAN,
    VEGAN,
    GLUTEN_FREE,
    DAIRY_FREE,
    LOW_SODIUM,
    KETO,
    PALEO,
    LOW_CARB,
    HIGH_PROTEIN
}

enum class CookingSkillLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class IngredientCategory {
    PRODUCE,      // Obst & Gemüse
    PROTEIN,      // Fleisch & Fisch
    DAIRY,        // Milchprodukte
    GRAINS,       // Getreide & Brot
    PANTRY,       // Vorräte
    FROZEN,       // Tiefkühlprodukte
    BEVERAGES,    // Getränke
    SNACKS,       // Snacks
    SPICES,       // Gewürze
    OTHER,        // Sonstiges
    // Legacy values for compatibility
    VEGETABLE,
    GRAIN,
    SPICE
}

enum class SpiceLevel {
    MILD,
    MEDIUM,
    HOT
}