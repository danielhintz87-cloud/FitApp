package com.example.fitapp.services

import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.*
import com.example.fitapp.domain.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * YAZIO-style Recipe Management Service
 * Implements comprehensive recipe features as per YAZIO specification
 */
class YazioRecipeManager(private val db: AppDatabase) {
    
    private val recipeDao = db.recipeDao()
    private val mealDao = db.mealDao()
    
    suspend fun createRecipe(builder: RecipeBuilder): Recipe {
        require(builder.isValid()) { "Recipe must have title, at least 2 ingredients, and cooking steps" }
        
        val recipe = builder.build()
        val entity = recipe.toEntity()
        
        // Transaction to insert recipe with ingredients and steps
        db.runInTransaction {
            recipeDao.upsertRecipe(entity)
            
            // Insert ingredients
            recipe.ingredients.forEach { ingredient ->
                recipeDao.insertRecipeIngredient(ingredient.toEntity(recipe.id))
            }
            
            // Insert cooking steps
            recipe.steps.forEach { step ->
                recipeDao.insertRecipeStep(step.toEntity(recipe.id))
            }
            
            // Track analytics
            trackRecipeEvent(recipe.id, "created")
        }
        
        return recipe
    }
    
    suspend fun updateRecipe(recipe: Recipe) {
        val entity = recipe.toEntity()
        
        db.runInTransaction {
            recipeDao.upsertRecipe(entity)
            
            // Delete existing ingredients and steps
            recipeDao.deleteRecipeIngredients(recipe.id)
            recipeDao.deleteRecipeSteps(recipe.id)
            
            // Insert updated ingredients and steps
            recipe.ingredients.forEach { ingredient ->
                recipeDao.insertRecipeIngredient(ingredient.toEntity(recipe.id))
            }
            
            recipe.steps.forEach { step ->
                recipeDao.insertRecipeStep(step.toEntity(recipe.id))
            }
        }
    }
    
    suspend fun getRecipe(id: String): Recipe? {
        val entity = recipeDao.getRecipe(id) ?: return null
        val ingredients = recipeDao.getRecipeIngredients(id).map { it.toDomain() }
        val steps = recipeDao.getRecipeSteps(id).map { it.toDomain() }
        
        return entity.toDomain(ingredients, steps)
    }
    
    suspend fun searchRecipes(filters: RecipeFilters): List<Recipe> {
        return recipeDao.searchRecipesFiltered(
            searchQuery = filters.searchQuery,
            difficulty = filters.difficulty?.name?.lowercase(),
            maxPrepTime = filters.maxPrepTime,
            maxCookTime = filters.maxCookTime,
            maxCalories = filters.maxCalories,
            isOfficial = filters.isOfficial,
            sortBy = filters.sortBy.name.lowercase()
        ).map { entity ->
            val ingredients = recipeDao.getRecipeIngredients(entity.id).map { it.toDomain() }
            val steps = recipeDao.getRecipeSteps(entity.id).map { it.toDomain() }
            entity.toDomain(ingredients, steps)
        }
    }
    
    suspend fun addToFavorites(recipeId: String, category: String = "general") {
        recipeDao.addToFavorites(RecipeFavoriteEntity(recipeId, category))
        trackRecipeEvent(recipeId, "added_to_favorites")
    }
    
    suspend fun removeFromFavorites(recipeId: String) {
        recipeDao.removeFromAllFavoriteCategories(recipeId)
    }
    
    fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.favoritesFlow().map { entities ->
            entities.map { entity ->
                val ingredients = recipeDao.getRecipeIngredients(entity.id).map { it.toDomain() }
                val steps = recipeDao.getRecipeSteps(entity.id).map { it.toDomain() }
                entity.toDomain(ingredients, steps)
            }
        }
    }
    
    suspend fun rateRecipe(recipeId: String, rating: Float, comment: String? = null) {
        require(rating in 1f..5f) { "Rating must be between 1 and 5" }
        
        val ratingEntity = RecipeRatingEntity(
            id = UUID.randomUUID().toString(),
            recipeId = recipeId,
            rating = rating,
            comment = comment
        )
        
        // This would be implemented in the DAO
        // recipeRatingDao.insertRating(ratingEntity)
        
        // Update recipe average rating
        updateRecipeRating(recipeId)
        trackRecipeEvent(recipeId, "rated")
    }
    
    private suspend fun updateRecipeRating(recipeId: String) {
        // Calculate new average rating and update recipe entity
        // This would involve querying all ratings for the recipe
        // and updating the recipe's rating and ratingCount fields
    }
    
    suspend fun addRecipeToGroceryList(recipeId: String, groceryListId: String, servings: Float = 1f) {
        val recipe = getRecipe(recipeId) ?: return
        val groceryDao = db.groceryDao()
        
        recipe.ingredients.forEach { ingredient ->
            val adjustedAmount = ingredient.amount * servings
            
            // Check for existing similar items to merge quantities
            val existingItems = groceryDao.findSimilarItems(
                groceryListId, 
                ingredient.name, 
                ingredient.unit
            )
            
            if (existingItems.isNotEmpty()) {
                // Merge with existing item
                val existingItem = existingItems.first()
                val newQuantity = (existingItem.quantity ?: 0f) + adjustedAmount
                groceryDao.updateGroceryItem(
                    existingItem.copy(quantity = newQuantity)
                )
            } else {
                // Add new item
                val groceryItem = GroceryItemEntity(
                    listId = groceryListId,
                    name = ingredient.name,
                    quantity = adjustedAmount,
                    unit = ingredient.unit,
                    category = categorizeIngredient(ingredient.name),
                    fromRecipeId = recipeId,
                    fromRecipeName = recipe.title
                )
                groceryDao.insertGroceryItem(groceryItem)
            }
        }
        
        trackRecipeEvent(recipeId, "added_to_grocery_list")
    }
    
    private fun categorizeIngredient(ingredientName: String): String {
        // Smart categorization based on ingredient name
        val name = ingredientName.lowercase()
        return when {
            name.contains(Regex("apple|banana|orange|tomato|onion|carrot|potato|lettuce|spinach")) -> 
                GroceryCategory.FRUITS_VEGETABLES.displayName
            name.contains(Regex("chicken|beef|pork|fish|salmon|tuna|ham")) -> 
                GroceryCategory.MEAT_FISH.displayName
            name.contains(Regex("milk|cheese|yogurt|butter|cream")) -> 
                GroceryCategory.DAIRY.displayName
            name.contains(Regex("bread|flour|pasta|rice|oats|cereal")) -> 
                GroceryCategory.PANTRY.displayName
            name.contains(Regex("frozen|ice")) -> 
                GroceryCategory.FROZEN.displayName
            else -> GroceryCategory.OTHER.displayName
        }
    }
    
    suspend fun trackRecipeEvent(recipeId: String, eventType: String, metadata: String? = null) {
        val analyticsEntity = RecipeAnalyticsEntity(
            recipeId = recipeId,
            eventType = eventType,
            metadata = metadata
        )
        // recipeAnalyticsDao.insertEvent(analyticsEntity)
    }
    
    // Meal management (YAZIO distinction)
    suspend fun createMeal(name: String, mealType: MealType, foods: List<MealFood>): Meal {
        require(foods.isNotEmpty()) { "Meal must contain at least one food item" }
        
        val totalNutrition = calculateTotalNutrition(foods)
        val meal = Meal(
            name = name,
            mealType = mealType,
            foods = foods,
            totalNutrition = totalNutrition
        )
        
        val entity = meal.toEntity()
        mealDao.insertMeal(entity)
        
        return meal
    }
    
    private fun calculateTotalNutrition(foods: List<MealFood>): RecipeNutrition {
        val totalCalories = foods.sumOf { it.calories }
        val totalProtein = foods.sumOf { it.protein.toDouble() }.toFloat()
        val totalCarbs = foods.sumOf { it.carbs.toDouble() }.toFloat()
        val totalFat = foods.sumOf { it.fat.toDouble() }.toFloat()
        
        return RecipeNutrition(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat
        )
    }
    
    fun getMealsByType(mealType: MealType): Flow<List<Meal>> {
        return mealDao.getMealsByType(mealType.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

/**
 * Smart Grocery List Manager (YAZIO-style)
 */
class SmartGroceryListManager(private val db: AppDatabase) {
    
    private val groceryDao = db.groceryDao()
    
    suspend fun createGroceryList(name: String, description: String? = null, isDefault: Boolean = false): GroceryList {
        val list = GroceryList(name = name, description = description, isDefault = isDefault)
        val entity = list.toEntity()
        groceryDao.insertGroceryList(entity)
        return list
    }
    
    suspend fun addItem(listId: String, name: String, quantity: Float? = null, unit: String? = null, category: GroceryCategory? = null): GroceryItem {
        val item = GroceryItem(
            name = name,
            quantity = quantity,
            unit = unit,
            category = category ?: categorizeItem(name)
        )
        val entity = item.toEntity(listId)
        groceryDao.insertGroceryItem(entity)
        return item
    }
    
    private fun categorizeItem(itemName: String): GroceryCategory {
        val name = itemName.lowercase()
        return when {
            name.contains(Regex("apple|banana|orange|tomato|onion|carrot|potato|lettuce|spinach|fruit|vegetable")) -> 
                GroceryCategory.FRUITS_VEGETABLES
            name.contains(Regex("chicken|beef|pork|fish|salmon|tuna|ham|meat")) -> 
                GroceryCategory.MEAT_FISH
            name.contains(Regex("milk|cheese|yogurt|butter|cream|dairy")) -> 
                GroceryCategory.DAIRY
            name.contains(Regex("bread|flour|pasta|rice|oats|cereal")) -> 
                GroceryCategory.PANTRY
            name.contains(Regex("frozen|ice")) -> 
                GroceryCategory.FROZEN
            name.contains(Regex("water|juice|soda|beer|wine|coffee|tea")) -> 
                GroceryCategory.BEVERAGES
            else -> GroceryCategory.OTHER
        }
    }
    
    suspend fun checkItem(itemId: String, checked: Boolean) {
        groceryDao.updateItemChecked(itemId, checked)
    }
    
    fun getActiveGroceryLists(): Flow<List<GroceryList>> {
        return groceryDao.getActiveLists().map { entities ->
            entities.map { entity ->
                val items = groceryDao.getListItems(entity.id).map { itemEntities ->
                    itemEntities.map { it.toDomain() }
                }
                entity.toDomain(items)
            }
        }
    }
    
    suspend fun getItemsByCategory(listId: String): Map<GroceryCategory, List<GroceryItem>> {
        val allCategories = GroceryCategory.entries
        val result = mutableMapOf<GroceryCategory, List<GroceryItem>>()
        
        allCategories.forEach { category ->
            val items = groceryDao.getItemsByCategory(listId, category.displayName)
                .map { it.toDomain() }
            if (items.isNotEmpty()) {
                result[category] = items
            }
        }
        
        return result.toSortedMap(compareBy { it.sortOrder })
    }
}

/**
 * Cooking Mode Manager with step-by-step guidance
 */
class EnhancedCookingModeManager(private val db: AppDatabase) {
    
    private val cookingSessionDao = db.cookingSessionDao()
    private val cookingTimerDao = db.cookingTimerDao()
    
    suspend fun startCookingSession(recipeId: String): CookingSession {
        // Get recipe to count total steps
        val recipeDao = db.recipeDao()
        val steps = recipeDao.getRecipeSteps(recipeId)
        
        val session = CookingSessionEntity(
            recipeId = recipeId,
            startTime = System.currentTimeMillis() / 1000,
            totalSteps = steps.size
        )
        
        cookingSessionDao.insert(session)
        
        return CookingSession(
            id = session.id,
            recipeId = recipeId,
            startTime = session.startTime,
            status = CookingStatus.valueOf(session.status.uppercase()),
            currentStep = session.currentStep,
            completedSteps = emptySet() // Parse from entity if needed
        )
    }
    
    suspend fun updateCookingStep(sessionId: String, currentStep: Int) {
        val session = cookingSessionDao.getSession(sessionId) ?: return
        val updatedSession = session.copy(currentStep = currentStep)
        cookingSessionDao.update(updatedSession)
    }
    
    suspend fun pauseCookingSession(sessionId: String) {
        val session = cookingSessionDao.getSession(sessionId) ?: return
        val updatedSession = session.copy(status = "paused")
        cookingSessionDao.update(updatedSession)
    }
    
    suspend fun resumeCookingSession(sessionId: String) {
        val session = cookingSessionDao.getSession(sessionId) ?: return
        val updatedSession = session.copy(status = "active")
        cookingSessionDao.update(updatedSession)
    }
    
    suspend fun completeCookingSession(sessionId: String) {
        val session = cookingSessionDao.getSession(sessionId) ?: return
        val endTime = System.currentTimeMillis() / 1000
        val actualDuration = endTime - session.startTime
        
        val updatedSession = session.copy(
            status = "completed",
            endTime = endTime,
            actualDuration = actualDuration
        )
        cookingSessionDao.update(updatedSession)
        
        // Track analytics
        // trackRecipeEvent(session.recipeId, "cooking_completed")
    }
    
    suspend fun startTimer(sessionId: String, stepIndex: Int, name: String, durationSeconds: Long): CookingTimer {
        val timer = CookingTimerEntity(
            sessionId = sessionId,
            stepIndex = stepIndex,
            name = name,
            durationSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            isActive = true,
            startTime = System.currentTimeMillis() / 1000
        )
        
        cookingTimerDao.insert(timer)
        
        return CookingTimer(
            id = timer.id,
            name = name,
            durationSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            isActive = true,
            stepIndex = stepIndex,
            startTime = timer.startTime
        )
    }
    
    suspend fun pauseTimer(timerId: String) {
        val timer = cookingTimerDao.getTimer(timerId) ?: return
        val updatedTimer = timer.copy(isPaused = true)
        cookingTimerDao.update(updatedTimer)
    }
    
    suspend fun resumeTimer(timerId: String) {
        val timer = cookingTimerDao.getTimer(timerId) ?: return
        val updatedTimer = timer.copy(isPaused = false)
        cookingTimerDao.update(updatedTimer)
    }
    
    suspend fun completeTimer(timerId: String) {
        val timer = cookingTimerDao.getTimer(timerId) ?: return
        val updatedTimer = timer.copy(
            isActive = false,
            completedAt = System.currentTimeMillis() / 1000
        )
        cookingTimerDao.update(updatedTimer)
    }
}

// Extension functions for entity conversion
private fun Recipe.toEntity(): RecipeEntity {
    return RecipeEntity(
        id = id,
        title = title,
        description = description,
        markdown = steps.joinToString("\n\n") { "**${it.order + 1}.** ${it.instruction}" },
        imageUrl = imageUrl,
        prepTime = prepTime,
        cookTime = cookTime,
        servings = servings,
        difficulty = difficulty?.name?.lowercase(),
        categories = JSONArray(categories.map { it.name }).toString(),
        calories = nutrition?.calories,
        protein = nutrition?.protein,
        carbs = nutrition?.carbs,
        fat = nutrition?.fat,
        fiber = nutrition?.fiber,
        sugar = nutrition?.sugar,
        sodium = nutrition?.sodium,
        isOfficial = isOfficial,
        rating = rating,
        ratingCount = ratingCount,
        isLocalOnly = isLocalOnly,
        createdAt = createdAt
    )
}

private fun RecipeEntity.toDomain(ingredients: List<RecipeIngredient>, steps: List<RecipeStep>): Recipe {
    val categories = try {
        val jsonArray = JSONArray(categories ?: "[]")
        (0 until jsonArray.length()).map { 
            RecipeCategory.valueOf(jsonArray.getString(it))
        }
    } catch (e: Exception) { emptyList() }
    
    val nutrition = if (calories != null) {
        RecipeNutrition(
            calories = calories,
            protein = protein ?: 0f,
            carbs = carbs ?: 0f,
            fat = fat ?: 0f,
            fiber = fiber,
            sugar = sugar,
            sodium = sodium
        )
    } else null
    
    return Recipe(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        prepTime = prepTime,
        cookTime = cookTime,
        servings = servings,
        difficulty = difficulty?.let { RecipeDifficulty.valueOf(it.uppercase()) },
        categories = categories,
        ingredients = ingredients,
        steps = steps,
        nutrition = nutrition,
        isOfficial = isOfficial,
        rating = rating,
        ratingCount = ratingCount,
        isLocalOnly = isLocalOnly,
        createdAt = createdAt
    )
}

private fun RecipeIngredient.toEntity(recipeId: String): RecipeIngredientEntity {
    return RecipeIngredientEntity(
        id = id,
        recipeId = recipeId,
        name = name,
        amount = amount,
        unit = unit,
        ingredientOrder = order,
        isOptional = isOptional,
        preparationNote = preparationNote,
        category = category
    )
}

private fun RecipeIngredientEntity.toDomain(): RecipeIngredient {
    return RecipeIngredient(
        id = id,
        name = name,
        amount = amount,
        unit = unit,
        isOptional = isOptional,
        preparationNote = preparationNote,
        category = category,
        order = ingredientOrder
    )
}

private fun RecipeStep.toEntity(recipeId: String): RecipeStepEntity {
    return RecipeStepEntity(
        id = id,
        recipeId = recipeId,
        stepOrder = order,
        instruction = instruction,
        estimatedTimeMinutes = estimatedTimeMinutes,
        temperature = temperature,
        timerName = timerName,
        timerDurationSeconds = timerDurationSeconds,
        imageUrl = imageUrl,
        tips = tips
    )
}

private fun RecipeStepEntity.toDomain(): RecipeStep {
    return RecipeStep(
        id = id,
        instruction = instruction,
        estimatedTimeMinutes = estimatedTimeMinutes,
        temperature = temperature,
        timerName = timerName,
        timerDurationSeconds = timerDurationSeconds,
        imageUrl = imageUrl,
        tips = tips,
        order = stepOrder
    )
}

private fun Meal.toEntity(): MealEntity {
    return MealEntity(
        id = id,
        name = name,
        description = description,
        mealType = mealType.name,
        foods = JSONArray(foods.map { food ->
            JSONObject().apply {
                put("foodItemId", food.foodItemId)
                put("name", food.name)
                put("quantity", food.quantity)
                put("calories", food.calories)
                put("protein", food.protein)
                put("carbs", food.carbs)
                put("fat", food.fat)
            }
        }).toString(),
        totalCalories = totalNutrition.calories,
        totalProtein = totalNutrition.protein,
        totalCarbs = totalNutrition.carbs,
        totalFat = totalNutrition.fat,
        createdAt = createdAt,
        lastUsedAt = lastUsedAt
    )
}

private fun MealEntity.toDomain(): Meal {
    val foods = try {
        val jsonArray = JSONArray(foods)
        (0 until jsonArray.length()).map { index ->
            val foodObj = jsonArray.getJSONObject(index)
            MealFood(
                foodItemId = foodObj.getString("foodItemId"),
                name = foodObj.getString("name"),
                quantity = foodObj.getDouble("quantity").toFloat(),
                calories = foodObj.getInt("calories"),
                protein = foodObj.getDouble("protein").toFloat(),
                carbs = foodObj.getDouble("carbs").toFloat(),
                fat = foodObj.getDouble("fat").toFloat()
            )
        }
    } catch (e: Exception) { emptyList() }
    
    return Meal(
        id = id,
        name = name,
        description = description,
        mealType = MealType.valueOf(mealType),
        foods = foods,
        totalNutrition = RecipeNutrition(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat
        ),
        createdAt = createdAt,
        lastUsedAt = lastUsedAt
    )
}

private fun GroceryList.toEntity(): GroceryListEntity {
    return GroceryListEntity(
        id = id,
        name = name,
        description = description,
        isActive = isActive,
        isDefault = isDefault,
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt,
        completedAt = completedAt
    )
}

private fun GroceryListEntity.toDomain(items: Flow<List<GroceryItem>>): GroceryList {
    return GroceryList(
        id = id,
        name = name,
        description = description,
        items = emptyList(), // Would need to collect items flow
        isActive = isActive,
        isDefault = isDefault,
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt,
        completedAt = completedAt
    )
}

private fun GroceryItem.toEntity(listId: String): GroceryItemEntity {
    return GroceryItemEntity(
        id = id,
        listId = listId,
        name = name,
        quantity = quantity,
        unit = unit,
        category = category.displayName,
        checked = checked,
        fromRecipeId = fromRecipeId,
        fromRecipeName = fromRecipeName,
        estimatedPrice = estimatedPrice,
        notes = notes,
        addedAt = addedAt,
        checkedAt = checkedAt
    )
}

private fun GroceryItemEntity.toDomain(): GroceryItem {
    val groceryCategory = GroceryCategory.entries.find { it.displayName == category } ?: GroceryCategory.OTHER
    
    return GroceryItem(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        category = groceryCategory,
        checked = checked,
        fromRecipeId = fromRecipeId,
        fromRecipeName = fromRecipeName,
        estimatedPrice = estimatedPrice,
        notes = notes,
        addedAt = addedAt,
        checkedAt = checkedAt
    )
}