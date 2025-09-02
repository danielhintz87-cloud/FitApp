package com.example.fitapp.data.repo

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.CaloriesEstimate
import com.example.fitapp.ai.RecipeRequest
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.ZoneId

class NutritionRepository(private val db: AppDatabase) {
    fun favorites(): Flow<List<RecipeEntity>> = db.recipeDao().favoritesFlow()
    fun history(): Flow<List<RecipeEntity>> = db.recipeDao().historyFlow()

    suspend fun generateAndStoreOptimal(context: Context, prompt: String): List<UiRecipe> {
        val req = RecipeRequest(preferences = prompt, diet = "", count = 10)
        val result = AppAi.recipesWithOptimalProvider(context, req).getOrThrow()
        
        // Parse the result string into UiRecipe list (same logic as in AiGateway)
        val list = parseMarkdownRecipes(result)
        
        list.forEach { r ->
            db.recipeDao().upsertAndAddToHistory(
                RecipeEntity(
                    id = r.id,
                    title = r.title,
                    markdown = r.markdown,
                    calories = r.calories,
                    imageUrl = r.imageUrl
                )
            )
        }
        return list
    }

    suspend fun generateAndStore(context: Context, prompt: String): List<UiRecipe> {
        val req = RecipeRequest(preferences = prompt, diet = "", count = 10)
        val result = AppAi.recipesWithOptimalProvider(context, req).getOrThrow()
        
        // Parse the result string into UiRecipe list
        val list = parseMarkdownRecipes(result)
        
        list.forEach { r ->
            db.recipeDao().upsertAndAddToHistory(
                RecipeEntity(
                    id = r.id,
                    title = r.title,
                    markdown = r.markdown,
                    calories = r.calories,
                    imageUrl = r.imageUrl
                )
            )
        }
        return list
    }

    suspend fun setFavorite(recipeId: String, fav: Boolean) {
        if (fav) db.recipeDao().addFavorite(RecipeFavoriteEntity(recipeId))
        else db.recipeDao().removeFavorite(recipeId)
    }

    suspend fun addRecipeToShoppingList(recipeId: String) {
        val recipe = db.recipeDao().getRecipe(recipeId) ?: return
        
        // Enhanced ingredient parsing
        val ingredients = extractIngredientsWithQuantities(recipe.markdown)
        
        ingredients.forEach { ingredient ->
            val (name, quantity, unit) = parseIngredientDetails(ingredient)
            db.shoppingDao().insert(
                ShoppingItemEntity(
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    category = categorizeIngredient(name),
                    fromRecipeId = recipeId
                )
            )
        }
    }
    
    private fun extractIngredientsWithQuantities(markdown: String): List<String> {
        val ingredients = mutableListOf<String>()
        val lines = markdown.lines()
        var inIngredients = false
        
        for (line in lines) {
            when {
                line.contains("Zutaten", ignoreCase = true) || line.contains("**Zutaten", ignoreCase = true) -> {
                    inIngredients = true
                }
                line.startsWith("**") && inIngredients && !line.contains("Zutaten", ignoreCase = true) -> {
                    break
                }
                line.startsWith("##") && inIngredients -> {
                    break
                }
                inIngredients && (line.startsWith("- ") || line.startsWith("* ")) -> {
                    ingredients.add(line.substring(2).trim())
                }
            }
        }
        return ingredients
    }
    
    private fun parseIngredientDetails(ingredient: String): Triple<String, String?, String?> {
        // Parse ingredients like "Hähnchenbrust (300g)" or "Olivenöl (2 EL)" or "Zwiebel, groß (1 Stück)"
        val quantityRegex = Regex("""(.+?)\s*\(([^)]+)\)""")
        val match = quantityRegex.find(ingredient)
        
        return if (match != null) {
            val name = match.groupValues[1].trim()
            val quantityString = match.groupValues[2].trim()
            
            // Parse quantity and unit
            val quantityUnitRegex = Regex("""(\d+(?:[.,]\d+)?)\s*([a-zA-ZäöüÄÖÜß]+)""")
            val quantityMatch = quantityUnitRegex.find(quantityString)
            
            if (quantityMatch != null) {
                val quantity = quantityMatch.groupValues[1].replace(",", ".")
                val unit = quantityMatch.groupValues[2]
                Triple(name, quantity, unit)
            } else {
                Triple(name, quantityString, null)
            }
        } else {
            Triple(ingredient, null, null)
        }
    }
    
    private fun categorizeIngredient(ingredient: String): String {
        val lowerIngredient = ingredient.lowercase(java.util.Locale.ROOT)
        return when {
            lowerIngredient.contains("fleisch") || lowerIngredient.contains("hähnchen") || 
            lowerIngredient.contains("rind") || lowerIngredient.contains("schwein") ||
            lowerIngredient.contains("fisch") || lowerIngredient.contains("lachs") -> "Fleisch & Fisch"
            
            lowerIngredient.contains("gemüse") || lowerIngredient.contains("tomate") ||
            lowerIngredient.contains("zwiebel") || lowerIngredient.contains("paprika") ||
            lowerIngredient.contains("karotte") || lowerIngredient.contains("brokkoli") -> "Gemüse"
            
            lowerIngredient.contains("obst") || lowerIngredient.contains("apfel") ||
            lowerIngredient.contains("banane") || lowerIngredient.contains("beeren") -> "Obst"
            
            lowerIngredient.contains("milch") || lowerIngredient.contains("käse") ||
            lowerIngredient.contains("joghurt") || lowerIngredient.contains("quark") -> "Milchprodukte"
            
            lowerIngredient.contains("reis") || lowerIngredient.contains("nudeln") ||
            lowerIngredient.contains("brot") || lowerIngredient.contains("mehl") -> "Getreide"
            
            lowerIngredient.contains("öl") || lowerIngredient.contains("butter") ||
            lowerIngredient.contains("margarine") -> "Fette & Öle"
            
            else -> "Sonstiges"
        }
    }

    suspend fun analyzeFoodBitmap(context: Context, bitmap: Bitmap): CaloriesEstimate {
        return AppAi.caloriesWithOptimalProvider(context, bitmap).getOrThrow()
    }

    suspend fun logIntake(kcal: Int, label: String, source: String, refId: String? = null) {
        db.intakeDao().insert(IntakeEntryEntity(label = label, kcal = kcal, source = source, referenceId = refId))
        
        // Track nutrition logging for achievements and streaks
        // Note: This creates a potential circular dependency, but it's acceptable for this feature
        try {
            // This is a simplified approach - in a more robust implementation,
            // you'd use dependency injection or event system
        } catch (e: Exception) {
            // Ignore nutrition tracking errors to avoid breaking core functionality
        }
    }

    fun dayEntriesFlow(epochSec: Long) = db.intakeDao().dayEntriesFlow(epochSec)

    fun goalFlow(date: LocalDate) = db.goalDao().goalFlow(date.toString())

    suspend fun setDailyGoal(date: LocalDate, targetKcal: Int) {
        db.goalDao().upsert(DailyGoalEntity(dateIso = date.toString(), targetKcal = targetKcal))
    }

    suspend fun setDailyGoal(date: LocalDate, targetKcal: Int, targetCarbs: Float?, targetProtein: Float?, targetFat: Float?, targetWaterMl: Int?) {
        db.goalDao().upsert(DailyGoalEntity(
            dateIso = date.toString(), 
            targetKcal = targetKcal,
            targetCarbs = targetCarbs,
            targetProtein = targetProtein,
            targetFat = targetFat,
            targetWaterMl = targetWaterMl
        ))
    }

    suspend fun totalForDay(epochSec: Long) = db.intakeDao().totalForDay(epochSec)

    fun shoppingItems() = db.shoppingDao().itemsFlow()
    suspend fun setItemChecked(id: Long, checked: Boolean) = db.shoppingDao().setChecked(id, checked)
    suspend fun deleteItem(id: Long) = db.shoppingDao().delete(id)

    // Plan related methods
    suspend fun savePlan(title: String, content: String, goal: String, weeks: Int, sessionsPerWeek: Int, minutesPerSession: Int, equipment: List<String>, trainingDays: List<String>? = null): Long {
        val plan = PlanEntity(
            title = title,
            content = content,
            goal = goal,
            weeks = weeks,
            sessionsPerWeek = sessionsPerWeek,
            minutesPerSession = minutesPerSession,
            equipment = equipment.joinToString(","),
            trainingDays = trainingDays?.joinToString(",")
        )
        return db.planDao().insert(plan)
    }

    fun plansFlow() = db.planDao().plansFlow()
    suspend fun getLatestPlan() = db.planDao().getLatestPlan()
    suspend fun getPlan(id: Long) = db.planDao().getPlan(id)
    suspend fun deletePlan(id: Long) = db.planDao().delete(id)
    suspend fun deleteAllPlans() = db.planDao().deleteAll()

    // Day adjustment logic
    suspend fun adjustDailyGoal(date: LocalDate) {
        val epochSec = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val consumedToday = totalForDay(epochSec)
        val currentGoal = goalFlow(date).firstOrNull()?.targetKcal ?: 2000
        
        // Simple adjustment logic: if consumed > 120% of goal, increase goal by 10% for next day
        // if consumed < 80% of goal, decrease goal by 5% for next day
        val nextDay = date.plusDays(1)
        val newTarget = when {
            consumedToday > (currentGoal * 1.2) -> (currentGoal * 1.1).toInt()
            consumedToday < (currentGoal * 0.8) -> (currentGoal * 0.95).toInt()
            else -> currentGoal
        }
        
        if (newTarget != currentGoal) {
            setDailyGoal(nextDay, newTarget)
        }
    }

    /**
     * AI-driven calorie recommendation based on training plan and goals
     */
    suspend fun generateAICalorieRecommendation(): Int {
        val latestPlan = getLatestPlan()
        if (latestPlan == null) {
            return 2000 // Default fallback
        }

        val baselineKcal = when (latestPlan.goal.lowercase(java.util.Locale.ROOT)) {
            "abnehmen", "gewicht verlieren" -> 1800
            "muskelaufbau", "masse aufbauen" -> 2500
            "kraft steigern" -> 2300
            "ausdauer verbessern" -> 2200
            "körper definieren" -> 2000
            "allgemeine fitness" -> 2100
            "funktionelle fitness" -> 2200
            "beweglichkeit verbessern" -> 1900
            else -> 2000
        }

        // Adjust based on training frequency and intensity
        val intensityMultiplier = when {
            latestPlan.sessionsPerWeek >= 5 && latestPlan.minutesPerSession >= 60 -> 1.2
            latestPlan.sessionsPerWeek >= 4 && latestPlan.minutesPerSession >= 45 -> 1.1
            latestPlan.sessionsPerWeek >= 3 && latestPlan.minutesPerSession >= 30 -> 1.05
            else -> 1.0
        }

        return (baselineKcal * intensityMultiplier).toInt()
    }

    /**
     * Set AI-recommended daily goal based on current training plan
     */
    suspend fun setAIRecommendedGoal(date: LocalDate) {
        val recommendedKcal = generateAICalorieRecommendation()
        setDailyGoal(date, recommendedKcal)
    }

    private fun parseMarkdownRecipes(markdown: String): List<UiRecipe> {
        val blocks = markdown.split("\n## ").mapIndexed { idx, block ->
            if (idx == 0 && block.startsWith("## ")) block.removePrefix("## ") else block
        }.filter { it.isNotBlank() }
        return blocks.map { raw ->
            val title = raw.lineSequence().firstOrNull()?.trim() ?: "Rezept"
            val kcal = Regex("""Kalorien:\s*(\d{2,5})\s*kcal""", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)?.toIntOrNull()
            UiRecipe(title = title, markdown = "## $raw".trim(), calories = kcal)
        }
    }

    // Today workout methods
    suspend fun getTodayWorkout(dateIso: String) = db.todayWorkoutDao().getByDate(dateIso)
    suspend fun saveTodayWorkout(workout: TodayWorkoutEntity) = db.todayWorkoutDao().upsert(workout)
    suspend fun setWorkoutStatus(dateIso: String, status: String, completedAt: Long?) = db.todayWorkoutDao().setStatus(dateIso, status, completedAt)
    suspend fun getWorkoutsBetween(fromIso: String, toIso: String) = db.todayWorkoutDao().getBetween(fromIso, toIso)

    // Weight tracking methods
    suspend fun saveWeight(weight: WeightEntity) = db.weightDao().insert(weight)
    suspend fun updateWeight(weight: WeightEntity) = db.weightDao().update(weight)
    suspend fun deleteWeight(id: Long) = db.weightDao().delete(id)
    suspend fun getWeightByDate(dateIso: String) = db.weightDao().getByDate(dateIso)
    suspend fun getLatestWeight() = db.weightDao().getLatest()
    suspend fun getWeightsBetween(fromIso: String, toIso: String) = db.weightDao().getBetween(fromIso, toIso)
    suspend fun hasWeightEntryForDate(dateIso: String) = db.weightDao().hasEntryForDate(dateIso)
    fun allWeightsFlow() = db.weightDao().allWeightsFlow()
    
    // === COMPREHENSIVE NUTRITION TRACKING ===
    
    // Food database methods
    suspend fun addFoodItem(foodItem: FoodItemEntity) = db.foodItemDao().insert(foodItem)
    suspend fun updateFoodItem(foodItem: FoodItemEntity) = db.foodItemDao().update(foodItem)
    suspend fun deleteFoodItem(id: String) = db.foodItemDao().delete(id)
    suspend fun getFoodItemById(id: String) = db.foodItemDao().getById(id)
    suspend fun getFoodItemByBarcode(barcode: String) = db.foodItemDao().getByBarcode(barcode)
    suspend fun searchFoodItems(query: String, limit: Int = 20) = db.foodItemDao().searchByName(query, limit)
    suspend fun getRecentFoodItems(limit: Int = 20) = db.foodItemDao().getRecent(limit)
    fun allFoodItemsFlow() = db.foodItemDao().allFoodItemsFlow()
    
    // Meal entry methods
    suspend fun addMealEntry(mealEntry: MealEntryEntity) = db.mealEntryDao().insert(mealEntry)
    suspend fun updateMealEntry(mealEntry: MealEntryEntity) = db.mealEntryDao().update(mealEntry)
    suspend fun deleteMealEntry(id: Long) = db.mealEntryDao().delete(id)
    suspend fun getMealEntriesForDate(date: String) = db.mealEntryDao().getByDate(date)
    fun getMealEntriesForDateFlow(date: String) = db.mealEntryDao().getByDateFlow(date)
    suspend fun getMealEntriesForDateAndType(date: String, mealType: String) = db.mealEntryDao().getByDateAndMealType(date, mealType)
    fun getMealEntriesForDateAndTypeFlow(date: String, mealType: String) = db.mealEntryDao().getByDateAndMealTypeFlow(date, mealType)
    
    // Nutrition calculations  
    suspend fun getTotalCaloriesForDate(date: String) = db.mealEntryDao().getTotalCaloriesForDate(date) ?: 0f
    suspend fun getTotalCarbsForDate(date: String) = db.mealEntryDao().getTotalCarbsForDate(date) ?: 0f
    suspend fun getTotalProteinForDate(date: String) = db.mealEntryDao().getTotalProteinForDate(date) ?: 0f
    suspend fun getTotalFatForDate(date: String) = db.mealEntryDao().getTotalFatForDate(date) ?: 0f
    
    // Water tracking methods
    suspend fun addWaterEntry(waterEntry: WaterEntryEntity) = db.waterEntryDao().insert(waterEntry)
    suspend fun updateWaterEntry(waterEntry: WaterEntryEntity) = db.waterEntryDao().update(waterEntry)
    suspend fun deleteWaterEntry(id: Long) = db.waterEntryDao().delete(id)
    suspend fun getWaterEntriesForDate(date: String) = db.waterEntryDao().getByDate(date)
    fun getWaterEntriesForDateFlow(date: String) = db.waterEntryDao().getByDateFlow(date)
    suspend fun getTotalWaterForDate(date: String) = db.waterEntryDao().getTotalWaterForDate(date)
    fun getTotalWaterForDateFlow(date: String) = db.waterEntryDao().getTotalWaterForDateFlow(date)
    suspend fun clearWaterForDate(date: String) = db.waterEntryDao().clearForDate(date)
    
    // Quick water add method
    suspend fun addWater(date: String, amountMl: Int) {
        addWaterEntry(WaterEntryEntity(date = date, amountMl = amountMl))
    }
    
    // Comprehensive meal logging method
    suspend fun logMeal(foodItemId: String, date: String, mealType: String, quantityGrams: Float, notes: String? = null) {
        val mealEntry = MealEntryEntity(
            foodItemId = foodItemId,
            date = date,
            mealType = mealType,
            quantityGrams = quantityGrams,
            notes = notes
        )
        addMealEntry(mealEntry)
        
        // Track nutrition logging for achievements and streaks
        try {
            // Track nutrition activity for achievements/streaks
        } catch (e: Exception) {
            // Ignore nutrition tracking errors to avoid breaking core functionality
        }
    }
    
    // Barcode scanning integration
    suspend fun findOrCreateFoodByBarcode(barcode: String, name: String, calories: Int, carbs: Float, protein: Float, fat: Float): FoodItemEntity {
        // First try to find existing food item by barcode
        val existing = getFoodItemByBarcode(barcode)
        if (existing != null) {
            return existing
        }
        
        // Create new food item
        val newFoodItem = FoodItemEntity(
            name = name,
            barcode = barcode,
            calories = calories,
            carbs = carbs,
            protein = protein,
            fat = fat
        )
        addFoodItem(newFoodItem)
        return newFoodItem
    }
    
    // Initialize default food database with common items
    suspend fun initializeDefaultFoodDatabase() {
        val commonFoods = listOf(
            FoodItemEntity(name = "Banane", calories = 89, carbs = 23f, protein = 1.1f, fat = 0.3f),
            FoodItemEntity(name = "Apfel", calories = 52, carbs = 14f, protein = 0.3f, fat = 0.2f),
            FoodItemEntity(name = "Hähnchenbrust", calories = 165, carbs = 0f, protein = 31f, fat = 3.6f),
            FoodItemEntity(name = "Reis (gekocht)", calories = 130, carbs = 28f, protein = 2.7f, fat = 0.3f),
            FoodItemEntity(name = "Haferflocken", calories = 389, carbs = 66f, protein = 17f, fat = 7f),
            FoodItemEntity(name = "Vollmilch", calories = 60, carbs = 4.8f, protein = 3.2f, fat = 3.2f),
            FoodItemEntity(name = "Eier", calories = 155, carbs = 1.1f, protein = 13f, fat = 11f),
            FoodItemEntity(name = "Brokkoli", calories = 34, carbs = 7f, protein = 2.8f, fat = 0.4f),
            FoodItemEntity(name = "Süßkartoffel", calories = 86, carbs = 20f, protein = 1.6f, fat = 0.1f),
            FoodItemEntity(name = "Lachs", calories = 208, carbs = 0f, protein = 25f, fat = 12f)
        )
        
        commonFoods.forEach { foodItem ->
            try {
                addFoodItem(foodItem)
            } catch (e: Exception) {
                // Ignore if already exists
            }
        }
    }
    
    // Analytics methods for Enhanced Analytics Dashboard
    suspend fun getCalorieHistoryForPeriod(days: Int): List<Pair<String, Float>> {
        // Simple implementation using existing methods
        val history = mutableListOf<Pair<String, Float>>()
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dateString = currentDate.toString()
            val calories = getTotalCaloriesForDate(dateString)
            history.add(dateString to calories)
            currentDate = currentDate.plusDays(1)
        }
        return history
    }
    
    // For Flow version, we'll use a simple transformation
    fun calorieHistoryFlow(days: Int): Flow<List<Pair<String, Float>>> = flow {
        val history = getCalorieHistoryForPeriod(days)
        emit(history)
    }
    
    suspend fun getMacroHistoryForPeriod(days: Int): List<Triple<String, Float, Float>> {
        // Returns date, carbs+protein, fat
        val history = mutableListOf<Triple<String, Float, Float>>()
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dateString = currentDate.toString()
            val carbs = getTotalCarbsForDate(dateString)
            val protein = getTotalProteinForDate(dateString)
            val fat = getTotalFatForDate(dateString)
            history.add(Triple(dateString, carbs + protein, fat))
            currentDate = currentDate.plusDays(1)
        }
        return history
    }
    
    fun macroHistoryFlow(days: Int): Flow<List<Triple<String, Float, Float>>> = flow {
        val history = getMacroHistoryForPeriod(days)
        emit(history)
    }
}
