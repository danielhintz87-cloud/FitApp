package com.example.fitapp.services

import com.example.fitapp.data.db.*
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Enhanced Recipe Favorites Manager
 * Manages advanced favoriting with categories, collections, and smart recommendations
 */
class RecipeFavoritesManager(
    private val database: AppDatabase,
) {
    companion object {
        private const val TAG = "RecipeFavoritesManager"
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    // Flow states for reactive UI
    private val _favoriteRecipes = MutableStateFlow<List<SavedRecipeEntity>>(emptyList())
    val favoriteRecipes: StateFlow<List<SavedRecipeEntity>> = _favoriteRecipes.asStateFlow()

    private val _favoriteCategories = MutableStateFlow<List<FavoriteCategory>>(emptyList())
    val favoriteCategories: StateFlow<List<FavoriteCategory>> = _favoriteCategories.asStateFlow()

    private val _recipeCollections = MutableStateFlow<List<RecipeCollection>>(emptyList())
    val recipeCollections: StateFlow<List<RecipeCollection>> = _recipeCollections.asStateFlow()

    data class FavoriteCategory(
        val id: String,
        val name: String,
        val emoji: String,
        val color: String,
        val recipeCount: Int,
        val lastAdded: Long? = null,
    )

    data class RecipeCollection(
        val id: String,
        val name: String,
        val description: String,
        val recipes: List<String>, // Recipe IDs
        val isPublic: Boolean = false,
        val createdAt: Long,
        val coverImageUrl: String? = null,
    )

    data class SmartRecommendation(
        val recipe: SavedRecipeEntity,
        val reason: String,
        val confidence: Float,
        val category: String,
    )

    init {
        scope.launch {
            loadFavorites()
            loadCategories()
            loadCollections()
        }
    }

    /**
     * Toggle recipe favorite status with enhanced categorization
     */
    suspend fun toggleFavorite(
        recipeId: String,
        category: String = "general",
    ): Boolean {
        val currentRecipe = database.savedRecipeDao().getRecipeById(recipeId)

        if (currentRecipe != null) {
            val newFavoriteStatus = !currentRecipe.isFavorite

            // Update recipe favorite status
            database.savedRecipeDao().setFavorite(recipeId, newFavoriteStatus)

            if (newFavoriteStatus) {
                // Add to favorite category
                addToCategory(recipeId, category)

                StructuredLogger.info(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Added recipe $recipeId to favorites in category $category",
                )
            } else {
                // Remove from all categories
                removeFromAllCategories(recipeId)

                StructuredLogger.info(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Removed recipe $recipeId from favorites",
                )
            }

            refreshFavorites()
            return newFavoriteStatus
        }

        return false
    }

    /**
     * Add recipe to specific favorite category
     */
    suspend fun addToCategory(
        recipeId: String,
        categoryName: String,
    ) {
        val entity =
            RecipeFavoriteEntity(
                recipeId = recipeId,
                category = categoryName,
                addedAt = System.currentTimeMillis() / 1000,
            )

        database.recipeDao().addToFavorites(entity)
        refreshCategories()
    }

    /**
     * Remove recipe from specific category
     */
    suspend fun removeFromCategory(
        recipeId: String,
        categoryName: String,
    ) {
        database.recipeDao().removeFromFavoriteCategory(recipeId, categoryName)
        refreshCategories()
    }

    /**
     * Remove recipe from all categories
     */
    private suspend fun removeFromAllCategories(recipeId: String) {
        database.recipeDao().removeFromAllFavoriteCategories(recipeId)
        refreshCategories()
    }

    /**
     * Create a new favorite category
     */
    suspend fun createCategory(
        name: String,
        emoji: String = "⭐",
        color: String = "#2196F3",
    ): String {
        val categoryId = java.util.UUID.randomUUID().toString()

        // Create default categories if they don't exist
        initializeDefaultCategories()

        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Created new favorite category: $name",
        )

        refreshCategories()
        return categoryId
    }

    /**
     * Create a recipe collection
     */
    suspend fun createCollection(
        name: String,
        description: String,
        recipeIds: List<String> = emptyList(),
    ): RecipeCollection {
        val collectionEntity = RecipeCollectionEntity(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            description = description,
            isOfficial = false,
            isPremium = false,
            sortOrder = 0,
            createdAt = System.currentTimeMillis() / 1000
        )

        // Insert collection into database
        database.recipeCollectionDao().insertCollection(collectionEntity)

        // Add recipes to collection if provided
        recipeIds.forEachIndexed { index, recipeId ->
            val itemEntity = RecipeCollectionItemEntity(
                collectionId = collectionEntity.id,
                recipeId = recipeId,
                sortOrder = index,
                addedAt = System.currentTimeMillis() / 1000
            )
            database.recipeCollectionDao().insertCollectionItem(itemEntity)
        }

        val collection = RecipeCollection(
            id = collectionEntity.id,
            name = collectionEntity.name,
            description = collectionEntity.description ?: "",
            recipes = recipeIds,
            isPublic = !collectionEntity.isPremium,
            createdAt = collectionEntity.createdAt
        )

        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Created recipe collection: $name with ${recipeIds.size} recipes",
        )

        refreshCollections()
        return collection
    }

    /**
     * Add recipe to collection
     */
    suspend fun addToCollection(
        recipeId: String,
        collectionId: String,
    ) {
        try {
            // Check if collection exists
            val collection = database.recipeCollectionDao().getCollectionById(collectionId)
            if (collection == null) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Collection $collectionId not found"
                )
                return
            }

            // Check if recipe is already in collection
            val isAlreadyInCollection = database.recipeCollectionDao().isRecipeInCollection(collectionId, recipeId)
            if (isAlreadyInCollection) {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Recipe $recipeId is already in collection $collectionId"
                )
                return
            }

            // Get current item count for sort order
            val currentCount = database.recipeCollectionDao().getCollectionRecipeCount(collectionId)
            
            val itemEntity = RecipeCollectionItemEntity(
                collectionId = collectionId,
                recipeId = recipeId,
                sortOrder = currentCount,
                addedAt = System.currentTimeMillis() / 1000
            )
            
            database.recipeCollectionDao().insertCollectionItem(itemEntity)

            StructuredLogger.info(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Added recipe $recipeId to collection $collectionId",
            )

            refreshCollections()
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to add recipe to collection",
                exception = e
            )
        }
    }

    /**
     * Remove recipe from collection
     */
    suspend fun removeFromCollection(
        recipeId: String,
        collectionId: String,
    ) {
        try {
            database.recipeCollectionDao().removeRecipeFromCollection(collectionId, recipeId)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Removed recipe $recipeId from collection $collectionId",
            )

            refreshCollections()
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to remove recipe from collection",
                exception = e
            )
        }
    }

    /**
     * Delete a collection
     */
    suspend fun deleteCollection(collectionId: String) {
        try {
            database.recipeCollectionDao().deleteCollection(collectionId)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Deleted collection $collectionId",
            )

            refreshCollections()
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to delete collection",
                exception = e
            )
        }
    }

    /**
     * Get recipes in a specific collection
     */
    suspend fun getRecipesInCollection(collectionId: String): List<SavedRecipeEntity> {
        return try {
            val recipeEntities = database.recipeCollectionDao().getRecipesInCollection(collectionId)
            // Convert RecipeEntity to SavedRecipeEntity
            recipeEntities.map { recipe ->
                SavedRecipeEntity(
                    id = recipe.id,
                    title = recipe.title,
                    markdown = recipe.markdown,
                    calories = recipe.calories,
                    imageUrl = recipe.imageUrl,
                    ingredients = "", // Would need to fetch from RecipeIngredientEntity
                    tags = recipe.categories ?: "",
                    prepTime = recipe.prepTime,
                    difficulty = recipe.difficulty,
                    servings = recipe.servings,
                    isFavorite = true, // Assume recipes in collections are favorites
                    createdAt = recipe.createdAt
                )
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to get recipes in collection",
                exception = e
            )
            emptyList()
        }
    }

    /**
     * Get recipes by favorite category
     */
    suspend fun getRecipesByCategory(categoryName: String): List<SavedRecipeEntity> {
        val favoriteRecipeIds = database.recipeDao().getFavoriteRecipesByCategory(categoryName)
        return database.savedRecipeDao().getRecipesByIds(favoriteRecipeIds)
    }

    /**
     * Get smart recipe recommendations based on favorites
     */
    suspend fun getSmartRecommendations(): List<SmartRecommendation> {
        val favorites = database.savedRecipeDao().favoriteRecipesFlow().first()
        val allRecipes = database.savedRecipeDao().allRecipesFlow().first()

        val recommendations = mutableListOf<SmartRecommendation>()

        // Analyze favorite patterns
        val favoriteTags = favorites.flatMap { it.tags.split(",") }.map { it.trim() }
        val tagFrequency = favoriteTags.groupingBy { it }.eachCount()

        val favoriteIngredients = favorites.flatMap { extractIngredients(it.ingredients) }
        val ingredientFrequency = favoriteIngredients.groupingBy { it }.eachCount()

        // Find similar recipes
        allRecipes.filter { !it.isFavorite }.forEach { recipe ->
            var score = 0f
            var reason = ""

            // Tag-based similarity
            val recipeTags = recipe.tags.split(",").map { it.trim() }
            val commonTags = recipeTags.intersect(tagFrequency.keys).size
            if (commonTags > 0) {
                score += commonTags * 0.3f
                reason = "Ähnliche Tags: ${recipeTags.intersect(tagFrequency.keys).joinToString()}"
            }

            // Ingredient-based similarity
            val recipeIngredients = extractIngredients(recipe.ingredients)
            val commonIngredients = recipeIngredients.intersect(ingredientFrequency.keys).size
            if (commonIngredients > 0) {
                score += commonIngredients * 0.4f
                if (reason.isNotEmpty()) reason += " • "
                reason += "Ähnliche Zutaten: ${recipeIngredients.intersect(ingredientFrequency.keys).take(3).joinToString()}"
            }

            // Cooking time similarity
            val avgFavoritePrepTime = favorites.mapNotNull { it.prepTime }.average()
            recipe.prepTime?.let { prepTime ->
                if (kotlin.math.abs(prepTime - avgFavoritePrepTime) < 15) {
                    score += 0.2f
                    if (reason.isNotEmpty()) reason += " • "
                    reason += "Ähnliche Zubereitungszeit"
                }
            }

            // Difficulty similarity
            val favoriteDifficulties = favorites.mapNotNull { it.difficulty }
            if (recipe.difficulty in favoriteDifficulties) {
                score += 0.1f
                if (reason.isNotEmpty()) reason += " • "
                reason += "Passende Schwierigkeit"
            }

            if (score > 0.5f) {
                recommendations.add(
                    SmartRecommendation(
                        recipe = recipe,
                        reason = reason,
                        confidence = kotlin.math.min(score, 1.0f),
                        category = determineSuggestionCategory(recipe),
                    ),
                )
            }
        }

        return recommendations.sortedByDescending { it.confidence }.take(10)
    }

    /**
     * Get favorite statistics
     */
    fun getFavoriteStats(): FavoriteStats {
        val favorites = _favoriteRecipes.value
        val categories = _favoriteCategories.value

        return FavoriteStats(
            totalFavorites = favorites.size,
            categoriesCount = categories.size,
            mostFavoriteCategory = categories.maxByOrNull { it.recipeCount }?.name ?: "Keine",
            avgCalories = favorites.mapNotNull { it.calories }.average().takeIf { !it.isNaN() }?.toInt(),
            avgPrepTime = favorites.mapNotNull { it.prepTime }.average().takeIf { !it.isNaN() }?.toInt(),
            recentlyAdded =
                favorites.count {
                    val daysSinceAdded = (System.currentTimeMillis() / 1000 - it.createdAt) / (24 * 60 * 60)
                    daysSinceAdded <= 7
                },
        )
    }

    data class FavoriteStats(
        val totalFavorites: Int,
        val categoriesCount: Int,
        val mostFavoriteCategory: String,
        val avgCalories: Int?,
        val avgPrepTime: Int?,
        val recentlyAdded: Int,
    )

    // Private helper methods

    private suspend fun loadFavorites() {
        database.savedRecipeDao().favoriteRecipesFlow().collect { recipes ->
            _favoriteRecipes.value = recipes
        }
    }

    private suspend fun loadCategories() {
        // Load favorite categories and count recipes
        val favorites = database.recipeDao().getAllFavoriteCategories()
        val categoryCounts = favorites.groupingBy { it.category }.eachCount()

        val categories =
            categoryCounts.map { (name, count) ->
                val (emoji, color) = getCategoryInfo(name)
                FavoriteCategory(
                    id = name,
                    name = name,
                    emoji = emoji,
                    color = color,
                    recipeCount = count,
                    lastAdded = favorites.filter { it.category == name }.maxOfOrNull { it.addedAt },
                )
            }

        _favoriteCategories.value = categories
    }

    private suspend fun loadCollections() {
        try {
            val collectionsFromDb = database.recipeCollectionDao().getAllCollections()
            
            val collections = collectionsFromDb.map { entity ->
                val recipeIds = database.recipeCollectionDao().getCollectionItems(entity.id)
                    .map { it.recipeId }
                
                RecipeCollection(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description ?: "",
                    recipes = recipeIds,
                    isPublic = !entity.isPremium,
                    createdAt = entity.createdAt,
                    coverImageUrl = entity.imageUrl
                )
            }

            // Merge with default collections if database is empty
            val allCollections = if (collections.isEmpty()) {
                getDefaultCollections()
            } else {
                collections
            }
            
            _recipeCollections.value = allCollections
            
            StructuredLogger.debug(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Loaded collections: ${allCollections.size} total"
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Failed to load collections",
                exception = e
            )
            // Fallback to default collections
            _recipeCollections.value = getDefaultCollections()
        }
    }

    private suspend fun refreshFavorites() {
        loadFavorites()
    }

    private suspend fun refreshCategories() {
        loadCategories()
    }

    private suspend fun refreshCollections() {
        loadCollections()
    }

    private suspend fun initializeDefaultCategories() {
        val defaultCategories =
            listOf(
                "Hauptgerichte" to "🍽️",
                "Desserts" to "🍰",
                "Vegetarisch" to "🥬",
                "Schnelle Küche" to "⚡",
                "Comfort Food" to "🤗",
                "Gesund" to "💚",
            )

        // This would typically create default category entities in the database
    }

    private fun getCategoryInfo(categoryName: String): Pair<String, String> {
        return when (categoryName.lowercase()) {
            "hauptgerichte" -> Pair("🍽️", "#2196F3")
            "desserts" -> Pair("🍰", "#E91E63")
            "vegetarisch" -> Pair("🥬", "#4CAF50")
            "schnelle küche" -> Pair("⚡", "#FF5722")
            "comfort food" -> Pair("🤗", "#FF9800")
            "gesund" -> Pair("💚", "#8BC34A")
            "vorspeisen" -> Pair("🥗", "#00BCD4")
            "suppen" -> Pair("🍲", "#795548")
            else -> Pair("⭐", "#9C27B0")
        }
    }

    private fun extractIngredients(ingredientsJson: String): List<String> {
        // Parse JSON and extract ingredient names
        // This is a simplified version - in production, use proper JSON parsing
        return ingredientsJson.split(",").map { it.trim().lowercase() }
    }

    private fun determineSuggestionCategory(recipe: SavedRecipeEntity): String {
        val tags = recipe.tags.lowercase()
        return when {
            tags.contains("dessert") -> "Desserts"
            tags.contains("vegetarian") -> "Vegetarisch"
            tags.contains("quick") -> "Schnelle Küche"
            tags.contains("healthy") -> "Gesund"
            else -> "Allgemein"
        }
    }

    private fun getDefaultCollections(): List<RecipeCollection> {
        return listOf(
            RecipeCollection(
                id = "meal-prep",
                name = "Meal Prep",
                description = "Rezepte perfekt für die Wochenvorbereitung",
                recipes = emptyList(),
                createdAt = System.currentTimeMillis() / 1000,
            ),
            RecipeCollection(
                id = "party-food",
                name = "Party Essen",
                description = "Rezepte für Gäste und besondere Anlässe",
                recipes = emptyList(),
                createdAt = System.currentTimeMillis() / 1000,
            ),
            RecipeCollection(
                id = "kids-favorites",
                name = "Kinder-Favoriten",
                description = "Rezepte die auch den Kleinen schmecken",
                recipes = emptyList(),
                createdAt = System.currentTimeMillis() / 1000,
            ),
        )
    }
}
