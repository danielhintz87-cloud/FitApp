package com.example.fitapp.domain.entities

import java.util.UUID

/**
 * YAZIO-style Recipe and Cooking domain entities
 * Based on YAZIO app specification for comprehensive recipe management
 */

// Recipe Difficulty levels
enum class RecipeDifficulty {
    EASY, MEDIUM, HARD
}

// Recipe categories as per YAZIO specification
enum class RecipeCategory {
    LOW_CARB,
    VEGETARIAN,
    VEGAN,
    DESSERTS,
    PIZZA,
    SALADS,
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACKS,
    APPETIZERS,
    MAIN_COURSE,
    SIDE_DISHES,
    SOUPS,
    BEVERAGES,
    HEALTHY,
    QUICK_MEALS,
    HIGH_PROTEIN,
    GLUTEN_FREE,
    DAIRY_FREE,
    KETO,
    PALEO,
    MEDITERRANEAN
}

// Store layout categories for smart grocery lists
enum class GroceryCategory(val displayName: String, val sortOrder: Int) {
    FRUITS_VEGETABLES("Obst & Gemüse", 1),
    MEAT_FISH("Fleisch & Fisch", 2),
    DAIRY("Milchprodukte", 3),
    BAKERY("Bäckerei", 4),
    PANTRY("Vorratskammer", 5),
    FROZEN("Tiefkühl", 6),
    BEVERAGES("Getränke", 7),
    SNACKS("Snacks", 8),
    HOUSEHOLD("Haushalt", 9),
    PHARMACY("Drogerie", 10),
    OTHER("Sonstiges", 99)
}

// Recipe search and filter criteria (YAZIO-style)
data class RecipeFilters(
    val searchQuery: String? = null,
    val categories: List<RecipeCategory> = emptyList(),
    val difficulty: RecipeDifficulty? = null,
    val maxPrepTime: Int? = null, // minutes
    val maxCookTime: Int? = null, // minutes
    val maxCalories: Int? = null,
    val minProtein: Float? = null,
    val maxCarbs: Float? = null,
    val maxFat: Float? = null,
    val isOfficial: Boolean? = null, // true for YAZIO official recipes
    val isVegetarian: Boolean? = null,
    val isVegan: Boolean? = null,
    val isGlutenFree: Boolean? = null,
    val isDairyFree: Boolean? = null,
    val servings: Int? = null,
    val createdByMe: Boolean? = null,
    val isFavorite: Boolean? = null,
    val sortBy: RecipeSortOption = RecipeSortOption.CREATED_AT,
    val sortDirection: SortDirection = SortDirection.DESC
)

enum class RecipeSortOption {
    NAME, CREATED_AT, PREP_TIME, COOK_TIME, CALORIES, DIFFICULTY, RATING
}

enum class SortDirection {
    ASC, DESC
}

// Complete Recipe domain object (YAZIO-style)
data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val prepTime: Int? = null, // minutes
    val cookTime: Int? = null, // minutes
    val servings: Int? = null,
    val difficulty: RecipeDifficulty? = null,
    val categories: List<RecipeCategory> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val nutrition: RecipeNutrition? = null,
    val isOfficial: Boolean = false, // YAZIO official recipe
    val rating: Float = 0f, // 0-5 stars
    val ratingCount: Int = 0,
    val isLocalOnly: Boolean = true,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val tags: List<String> = emptyList(), // "quick", "budget-friendly", etc.
    val cookingTips: List<String> = emptyList()
)

// Recipe ingredient with detailed measurement
data class RecipeIngredient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Float,
    val unit: String, // "g", "ml", "cups", "tbsp", "pieces", etc.
    val isOptional: Boolean = false,
    val preparationNote: String? = null, // "diced", "chopped", "grated"
    val category: String? = null, // "protein", "vegetables", "spices"
    val order: Int = 0
)

// Recipe cooking step with timing and instructions
data class RecipeStep(
    val id: String = UUID.randomUUID().toString(),
    val instruction: String,
    val estimatedTimeMinutes: Int? = null,
    val temperature: String? = null, // "180°C", "medium heat"
    val timerName: String? = null,
    val timerDurationSeconds: Int? = null,
    val imageUrl: String? = null,
    val tips: String? = null,
    val order: Int = 0
)

// Detailed nutrition information
data class RecipeNutrition(
    val calories: Int,
    val protein: Float, // grams
    val carbs: Float, // grams
    val fat: Float, // grams
    val fiber: Float? = null, // grams
    val sugar: Float? = null, // grams
    val sodium: Float? = null, // mg
    val saturatedFat: Float? = null, // grams
    val cholesterol: Float? = null, // mg
    val potassium: Float? = null, // mg
    val iron: Float? = null, // mg
    val calcium: Float? = null, // mg
    val vitaminA: Float? = null, // IU
    val vitaminC: Float? = null // mg
)

// Meal (quick food combination - YAZIO distinction)
data class Meal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val mealType: MealType,
    val foods: List<MealFood> = emptyList(),
    val totalNutrition: RecipeNutrition,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val lastUsedAt: Long? = null
)

data class MealFood(
    val foodItemId: String,
    val name: String,
    val quantity: Float, // in grams
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}

// Smart Grocery List (YAZIO-style)
data class GroceryList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val items: List<GroceryItem> = emptyList(),
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val lastModifiedAt: Long = System.currentTimeMillis() / 1000,
    val completedAt: Long? = null
)

data class GroceryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Float? = null,
    val unit: String? = null,
    val category: GroceryCategory,
    val checked: Boolean = false,
    val fromRecipeId: String? = null,
    val fromRecipeName: String? = null,
    val estimatedPrice: Float? = null,
    val notes: String? = null,
    val addedAt: Long = System.currentTimeMillis() / 1000,
    val checkedAt: Long? = null
)

// Cooking session with step-by-step guidance
data class CookingSession(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String,
    val recipe: Recipe? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val status: CookingStatus = CookingStatus.ACTIVE,
    val currentStep: Int = 0,
    val completedSteps: Set<Int> = emptySet(),
    val timers: List<CookingTimer> = emptyList(),
    val notes: String? = null,
    val actualDuration: Long? = null
)

enum class CookingStatus {
    ACTIVE, PAUSED, COMPLETED, CANCELLED
}

data class CookingTimer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val durationSeconds: Long,
    val remainingSeconds: Long,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val stepIndex: Int,
    val startTime: Long? = null,
    val completedAt: Long? = null
)

// Recipe analytics for usage tracking
data class RecipeAnalytics(
    val recipeId: String,
    val viewCount: Int = 0,
    val cookingStartedCount: Int = 0,
    val cookingCompletedCount: Int = 0,
    val favoritedCount: Int = 0,
    val groceryListAdditions: Int = 0,
    val averageRating: Float = 0f,
    val completionRate: Float = 0f, // percentage of cooking sessions completed
    val lastViewed: Long? = null,
    val lastCooked: Long? = null
)

// PRO feature management
data class ProFeature(
    val featureName: String,
    val displayName: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val usageCount: Int = 0,
    val maxUsage: Int? = null, // for trial features
    val category: ProFeatureCategory
)

enum class ProFeatureCategory {
    RECIPES, COOKING, GROCERY_LISTS, ANALYTICS, MEAL_PLANNING
}

// Recipe collections (curated lists)
data class RecipeCollection(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val recipes: List<Recipe> = emptyList(),
    val isOfficial: Boolean = false, // YAZIO curated
    val isPremium: Boolean = false, // requires PRO
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

// Recipe-to-diary integration
data class RecipeDiaryEntry(
    val recipeId: String,
    val recipeName: String,
    val mealCategory: MealType,
    val servings: Float, // can be fractional like 0.5 servings
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val date: String, // ISO date
    val timestamp: Long = System.currentTimeMillis() / 1000
)

// Recipe creation/editing helpers
data class RecipeBuilder(
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val prepTime: Int? = null,
    val cookTime: Int? = null,
    val servings: Int? = null,
    val difficulty: RecipeDifficulty? = null,
    val categories: List<RecipeCategory> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val tags: List<String> = emptyList()
) {
    fun isValid(): Boolean {
        return title.isNotBlank() && 
               ingredients.size >= 2 && // YAZIO requirement: minimum 2 ingredients
               steps.isNotEmpty()
    }
    
    fun build(): Recipe {
        require(isValid()) { "Recipe must have title, at least 2 ingredients, and cooking steps" }
        
        return Recipe(
            title = title.trim(),
            description = description.trim(),
            imageUrl = imageUrl,
            prepTime = prepTime,
            cookTime = cookTime,
            servings = servings,
            difficulty = difficulty,
            categories = categories,
            ingredients = ingredients.mapIndexed { index, ingredient ->
                ingredient.copy(order = index)
            },
            steps = steps.mapIndexed { index, step ->
                step.copy(order = index)
            },
            tags = tags
        )
    }
}

// AI Photo Recognition result (YAZIO latest feature)
data class FoodRecognitionResult(
    val recognizedFoods: List<RecognizedFood>,
    val confidence: Float,
    val suggestedRecipes: List<Recipe> = emptyList(),
    val nutritionEstimate: RecipeNutrition? = null
)

data class RecognizedFood(
    val name: String,
    val confidence: Float,
    val boundingBox: BoundingBox? = null,
    val estimatedWeight: Float? = null, // grams
    val nutritionInfo: RecipeNutrition? = null
)

data class BoundingBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)