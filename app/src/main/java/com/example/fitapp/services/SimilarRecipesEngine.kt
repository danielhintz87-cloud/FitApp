package com.example.fitapp.services

import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.fitapp.data.db.*
import com.example.fitapp.ai.AppAi
import com.example.fitapp.domain.entities.RecipeRequest
import com.example.fitapp.util.StructuredLogger
import kotlin.math.sqrt

/**
 * AI-Powered Similar Recipes Engine
 * Generates recipe variations and alternatives when users are not satisfied
 */
class SimilarRecipesEngine(
    private val context: Context,
    private val database: AppDatabase
) {
    companion object {
        private const val TAG = "SimilarRecipesEngine"
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Flow states for reactive UI
    private val _similarRecipes = MutableStateFlow<List<SimilarRecipeResult>>(emptyList())
    val similarRecipes: StateFlow<List<SimilarRecipeResult>> = _similarRecipes.asStateFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    data class SimilarRecipeResult(
        val recipe: SavedRecipeEntity,
        val similarityScore: Float,
        val similarityReasons: List<String>,
        val variationType: VariationType,
        val isGenerated: Boolean = false
    )
    
    enum class VariationType {
        SIMILAR_INGREDIENTS,      // Ähnliche Zutaten
        SAME_CUISINE,            // Gleiche Küche
        SAME_TYPE,               // Gleicher Typ (Dessert, Hauptgang, etc.)
        HEALTHIER_VERSION,       // Gesündere Variante
        EASIER_VERSION,          // Einfachere Variante
        DIFFERENT_PROTEIN,       // Andere Proteinquelle
        DIETARY_ADAPTATION,      // Diätetische Anpassung (vegan, glutenfrei, etc.)
        SEASONAL_VARIATION,      // Saisonale Variante
        AI_GENERATED_VARIATION   // KI-generierte Variation
    }

    /**
     * Find similar recipes to a given recipe
     */
    suspend fun findSimilarRecipes(
        targetRecipe: SavedRecipeEntity,
        maxResults: Int = 10,
        includeGenerated: Boolean = true
    ): List<SimilarRecipeResult> {
        _isGenerating.value = true
        
        try {
            val results = mutableListOf<SimilarRecipeResult>()
            
            // 1. Find existing similar recipes in database
            val existingSimilar = findExistingSimilarRecipes(targetRecipe, maxResults / 2)
            results.addAll(existingSimilar)
            
            // 2. Generate AI variations if requested and needed
            if (includeGenerated && results.size < maxResults) {
                val generated = generateAIVariations(
                    targetRecipe, 
                    maxResults - results.size
                )
                results.addAll(generated)
            }
            
            // 3. Sort by similarity score
            val sortedResults = results.sortedByDescending { it.similarityScore }
            
            _similarRecipes.value = sortedResults
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Found ${sortedResults.size} similar recipes for '${targetRecipe.title}'"
            )
            
            return sortedResults
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Error finding similar recipes for '${targetRecipe.title}'",
                exception = e
            )
            return emptyList()
        } finally {
            _isGenerating.value = false
        }
    }

    /**
     * Generate recipe variations when user is not satisfied
     */
    suspend fun generateVariationsForUnsatisfied(
        originalRecipe: SavedRecipeEntity,
        feedback: UserFeedback
    ): List<SimilarRecipeResult> {
        _isGenerating.value = true
        
        try {
            val variations = mutableListOf<SimilarRecipeResult>()
            
            // Analyze feedback to determine what variations to generate
            val variationTypes = determineVariationTypes(feedback)
            
            for (variationType in variationTypes) {
                val variation = generateSpecificVariation(originalRecipe, variationType, feedback)
                if (variation != null) {
                    variations.add(variation)
                }
            }
            
            // Save generated variations to database
            variations.forEach { result ->
                if (result.isGenerated) {
                    database.savedRecipeDao().insert(result.recipe)
                }
            }
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Generated ${variations.size} variations for '${originalRecipe.title}' based on feedback"
            )
            
            return variations
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Error generating variations for '${originalRecipe.title}'",
                exception = e
            )
            return emptyList()
        } finally {
            _isGenerating.value = false
        }
    }

    data class UserFeedback(
        val tooComplicated: Boolean = false,
        val tooTimeConsuming: Boolean = false,
        val dislikedIngredients: List<String> = emptyList(),
        val wantHealthier: Boolean = false,
        val wantVegan: Boolean = false,
        val wantGlutenFree: Boolean = false,
        val preferredCuisine: String? = null,
        val maxPrepTime: Int? = null,
        val additionalNotes: String = ""
    )

    /**
     * Get recipe recommendations based on dietary preferences
     */
    suspend fun getRecipeRecommendations(
        preferences: DietaryPreferences,
        excludeRecipeIds: List<String> = emptyList()
    ): List<SimilarRecipeResult> {
        val allRecipes = database.savedRecipeDao().allRecipesFlow().first()
        val recommendations = mutableListOf<SimilarRecipeResult>()
        
        allRecipes.filter { it.id !in excludeRecipeIds }.forEach { recipe ->
            val score = calculatePreferenceScore(recipe, preferences)
            
            if (score > 0.3f) {
                val reasons = generatePreferenceReasons(recipe, preferences)
                
                recommendations.add(
                    SimilarRecipeResult(
                        recipe = recipe,
                        similarityScore = score,
                        similarityReasons = reasons,
                        variationType = VariationType.DIETARY_ADAPTATION,
                        isGenerated = false
                    )
                )
            }
        }
        
        return recommendations.sortedByDescending { it.similarityScore }.take(10)
    }

    data class DietaryPreferences(
        val isVegetarian: Boolean = false,
        val isVegan: Boolean = false,
        val isGlutenFree: Boolean = false,
        val isLowCarb: Boolean = false,
        val isHighProtein: Boolean = false,
        val maxCalories: Int? = null,
        val maxPrepTime: Int? = null,
        val preferredCuisines: List<String> = emptyList(),
        val dislikedIngredients: List<String> = emptyList(),
        val allergies: List<String> = emptyList()
    )

    // Private implementation methods
    
    private suspend fun findExistingSimilarRecipes(
        targetRecipe: SavedRecipeEntity,
        maxResults: Int
    ): List<SimilarRecipeResult> {
        val allRecipes = database.savedRecipeDao().allRecipesFlow().first()
        val results = mutableListOf<SimilarRecipeResult>()
        
        allRecipes.filter { it.id != targetRecipe.id }.forEach { recipe ->
            val similarity = calculateSimilarity(targetRecipe, recipe)
            
            if (similarity.score > 0.3f) {
                results.add(
                    SimilarRecipeResult(
                        recipe = recipe,
                        similarityScore = similarity.score,
                        similarityReasons = similarity.reasons,
                        variationType = similarity.type,
                        isGenerated = false
                    )
                )
            }
        }
        
        return results.sortedByDescending { it.similarityScore }.take(maxResults)
    }
    
    private suspend fun generateAIVariations(
        originalRecipe: SavedRecipeEntity,
        count: Int
    ): List<SimilarRecipeResult> {
        val variations = mutableListOf<SimilarRecipeResult>()
        
        // Generate different types of variations
        val variationTypes = listOf(
            VariationType.HEALTHIER_VERSION,
            VariationType.EASIER_VERSION,
            VariationType.DIFFERENT_PROTEIN,
            VariationType.DIETARY_ADAPTATION
        ).take(count)
        
        for (variationType in variationTypes) {
            try {
                val variationPrompt = createVariationPrompt(originalRecipe, variationType)
                
                val request = RecipeRequest(
                    count = 1,
                    dietType = getDietTypeForVariation(variationType),
                    cuisineStyle = extractCuisineStyle(originalRecipe),
                    ingredients = extractKeyIngredients(originalRecipe),
                    maxCalories = adjustCaloriesForVariation(originalRecipe.calories, variationType),
                    maxPrepTime = adjustPrepTimeForVariation(originalRecipe.prepTime, variationType)
                )
                
                val result = AppAi.recipesWithOptimalProvider(context, request)
                
                if (result.isSuccess) {
                    val generatedRecipe = parseGeneratedRecipe(
                        result.getOrNull() ?: "",
                        originalRecipe,
                        variationType
                    )
                    
                    if (generatedRecipe != null) {
                        variations.add(
                            SimilarRecipeResult(
                                recipe = generatedRecipe,
                                similarityScore = 0.8f, // AI generated should have high relevance
                                similarityReasons = listOf(getVariationDescription(variationType)),
                                variationType = variationType,
                                isGenerated = true
                            )
                        )
                    }
                }
                
            } catch (e: Exception) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.NUTRITION,
                    TAG,
                    "Failed to generate AI variation of type $variationType",
                    exception = e
                )
            }
        }
        
        return variations
    }
    
    private suspend fun generateSpecificVariation(
        originalRecipe: SavedRecipeEntity,
        variationType: VariationType,
        feedback: UserFeedback
    ): SimilarRecipeResult? {
        try {
            val prompt = createFeedbackBasedPrompt(originalRecipe, variationType, feedback)
            
            val request = RecipeRequest(
                count = 1,
                dietType = when {
                    feedback.wantVegan -> "vegan"
                    feedback.wantHealthier -> "healthy"
                    else -> null
                },
                cuisineStyle = feedback.preferredCuisine ?: extractCuisineStyle(originalRecipe),
                ingredients = extractKeyIngredients(originalRecipe)
                    .filter { it !in feedback.dislikedIngredients },
                maxCalories = if (feedback.wantHealthier) originalRecipe.calories?.let { it * 0.8 }?.toInt() else originalRecipe.calories,
                maxPrepTime = feedback.maxPrepTime ?: if (feedback.tooTimeConsuming) originalRecipe.prepTime?.let { it / 2 } else originalRecipe.prepTime
            )
            
            val result = AppAi.recipesWithOptimalProvider(context, request)
            
            if (result.isSuccess) {
                val generatedRecipe = parseGeneratedRecipe(
                    result.getOrNull() ?: "",
                    originalRecipe,
                    variationType
                )
                
                if (generatedRecipe != null) {
                    return SimilarRecipeResult(
                        recipe = generatedRecipe,
                        similarityScore = 0.9f,
                        similarityReasons = listOf(
                            getVariationDescription(variationType),
                            "Basierend auf Ihrem Feedback angepasst"
                        ),
                        variationType = variationType,
                        isGenerated = true
                    )
                }
            }
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Error generating specific variation",
                exception = e
            )
        }
        
        return null
    }
    
    private fun calculateSimilarity(recipe1: SavedRecipeEntity, recipe2: SavedRecipeEntity): SimilarityResult {
        var score = 0f
        val reasons = mutableListOf<String>()
        var variationType = VariationType.SIMILAR_INGREDIENTS
        
        // Tag similarity
        val tags1 = recipe1.tags.split(",").map { it.trim().lowercase() }
        val tags2 = recipe2.tags.split(",").map { it.trim().lowercase() }
        val commonTags = tags1.intersect(tags2.toSet())
        
        if (commonTags.isNotEmpty()) {
            score += commonTags.size * 0.2f
            reasons.add("Gemeinsame Tags: ${commonTags.joinToString()}")
            variationType = VariationType.SAME_TYPE
        }
        
        // Ingredient similarity (simplified)
        val ingredients1 = extractIngredientsFromJson(recipe1.ingredients)
        val ingredients2 = extractIngredientsFromJson(recipe2.ingredients)
        val commonIngredients = ingredients1.intersect(ingredients2.toSet())
        
        if (commonIngredients.isNotEmpty()) {
            score += (commonIngredients.size.toFloat() / maxOf(ingredients1.size, ingredients2.size)) * 0.4f
            reasons.add("Ähnliche Zutaten: ${commonIngredients.take(3).joinToString()}")
            variationType = VariationType.SIMILAR_INGREDIENTS
        }
        
        // Cooking time similarity
        if (recipe1.prepTime != null && recipe2.prepTime != null) {
            val timeDiff = kotlin.math.abs(recipe1.prepTime - recipe2.prepTime)
            if (timeDiff <= 15) {
                score += 0.15f
                reasons.add("Ähnliche Zubereitungszeit")
            }
        }
        
        // Calorie similarity
        if (recipe1.calories != null && recipe2.calories != null) {
            val calorieDiff = kotlin.math.abs(recipe1.calories - recipe2.calories)
            if (calorieDiff <= 100) {
                score += 0.1f
                reasons.add("Ähnlicher Kaloriengehalt")
            }
        }
        
        // Difficulty similarity
        if (recipe1.difficulty == recipe2.difficulty && recipe1.difficulty != null) {
            score += 0.15f
            reasons.add("Gleiche Schwierigkeit")
        }
        
        return SimilarityResult(score, reasons, variationType)
    }
    
    private data class SimilarityResult(
        val score: Float,
        val reasons: List<String>,
        val type: VariationType
    )
    
    private fun determineVariationTypes(feedback: UserFeedback): List<VariationType> {
        val types = mutableListOf<VariationType>()
        
        if (feedback.tooComplicated) types.add(VariationType.EASIER_VERSION)
        if (feedback.tooTimeConsuming) types.add(VariationType.EASIER_VERSION)
        if (feedback.wantHealthier) types.add(VariationType.HEALTHIER_VERSION)
        if (feedback.wantVegan || feedback.wantGlutenFree) types.add(VariationType.DIETARY_ADAPTATION)
        if (feedback.dislikedIngredients.isNotEmpty()) types.add(VariationType.DIFFERENT_PROTEIN)
        if (feedback.preferredCuisine != null) types.add(VariationType.SAME_CUISINE)
        
        // Default variations if no specific feedback
        if (types.isEmpty()) {
            types.addAll(listOf(
                VariationType.SIMILAR_INGREDIENTS,
                VariationType.HEALTHIER_VERSION,
                VariationType.EASIER_VERSION
            ))
        }
        
        return types.take(3) // Limit to 3 variations
    }
    
    private fun createVariationPrompt(recipe: SavedRecipeEntity, variationType: VariationType): String {
        val basePrompt = "Basierend auf diesem Rezept: '${recipe.title}'"
        
        return when (variationType) {
            VariationType.HEALTHIER_VERSION -> 
                "$basePrompt - Erstelle eine gesündere Variante mit weniger Kalorien und mehr Nährstoffen."
            VariationType.EASIER_VERSION -> 
                "$basePrompt - Erstelle eine einfachere Variante mit weniger Schritten und kürzerer Zubereitungszeit."
            VariationType.DIFFERENT_PROTEIN -> 
                "$basePrompt - Erstelle eine Variante mit einer anderen Proteinquelle."
            VariationType.DIETARY_ADAPTATION -> 
                "$basePrompt - Erstelle eine vegane und glutenfreie Variante."
            VariationType.SEASONAL_VARIATION -> 
                "$basePrompt - Erstelle eine saisonale Variante mit aktuellen Zutaten."
            else -> 
                "$basePrompt - Erstelle eine ähnliche, aber interessante Variation."
        }
    }
    
    private fun createFeedbackBasedPrompt(
        recipe: SavedRecipeEntity,
        variationType: VariationType,
        feedback: UserFeedback
    ): String {
        var prompt = "Rezept: '${recipe.title}'\n"
        prompt += "Benutzerfeedback:\n"
        
        if (feedback.tooComplicated) prompt += "- Zu kompliziert\n"
        if (feedback.tooTimeConsuming) prompt += "- Zu zeitaufwendig\n"
        if (feedback.dislikedIngredients.isNotEmpty()) {
            prompt += "- Nicht mögen: ${feedback.dislikedIngredients.joinToString()}\n"
        }
        if (feedback.wantHealthier) prompt += "- Soll gesünder sein\n"
        if (feedback.wantVegan) prompt += "- Soll vegan sein\n"
        if (feedback.wantGlutenFree) prompt += "- Soll glutenfrei sein\n"
        
        prompt += "\nErstelle eine verbesserte Variante die diesem Feedback entspricht."
        
        return prompt
    }
    
    // Helper methods for AI request parameters
    private fun getDietTypeForVariation(variationType: VariationType): String? {
        return when (variationType) {
            VariationType.HEALTHIER_VERSION -> "healthy"
            VariationType.DIETARY_ADAPTATION -> "vegan"
            else -> null
        }
    }
    
    private fun extractCuisineStyle(recipe: SavedRecipeEntity): String? {
        val tags = recipe.tags.lowercase()
        return when {
            tags.contains("italienisch") -> "italienisch"
            tags.contains("asiatisch") -> "asiatisch"
            tags.contains("mediterran") -> "mediterran"
            tags.contains("deutsch") -> "deutsch"
            else -> null
        }
    }
    
    private fun extractKeyIngredients(recipe: SavedRecipeEntity): List<String> {
        return extractIngredientsFromJson(recipe.ingredients).take(5)
    }
    
    private fun extractIngredientsFromJson(ingredientsJson: String): List<String> {
        // Simplified extraction - in production, use proper JSON parsing
        return ingredientsJson.split(",").map { it.trim().lowercase() }
    }
    
    private fun adjustCaloriesForVariation(originalCalories: Int?, variationType: VariationType): Int? {
        return when (variationType) {
            VariationType.HEALTHIER_VERSION -> originalCalories?.let { (it * 0.8).toInt() }
            else -> originalCalories
        }
    }
    
    private fun adjustPrepTimeForVariation(originalPrepTime: Int?, variationType: VariationType): Int? {
        return when (variationType) {
            VariationType.EASIER_VERSION -> originalPrepTime?.let { (it * 0.7).toInt() }
            else -> originalPrepTime
        }
    }
    
    private fun parseGeneratedRecipe(
        aiResponse: String,
        originalRecipe: SavedRecipeEntity,
        variationType: VariationType
    ): SavedRecipeEntity? {
        // Parse AI response and create new recipe entity
        // This is simplified - in production, use proper markdown parsing
        
        val lines = aiResponse.split("\n")
        val title = lines.find { it.startsWith("##") }?.removePrefix("## ")?.trim() 
            ?: "${originalRecipe.title} - ${getVariationDescription(variationType)}"
        
        return SavedRecipeEntity(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            markdown = aiResponse,
            calories = adjustCaloriesForVariation(originalRecipe.calories, variationType),
            imageUrl = null,
            ingredients = originalRecipe.ingredients, // Simplified - should be extracted from AI response
            tags = originalRecipe.tags + ",${variationType.name.lowercase()}",
            prepTime = adjustPrepTimeForVariation(originalRecipe.prepTime, variationType),
            difficulty = if (variationType == VariationType.EASIER_VERSION) "easy" else originalRecipe.difficulty,
            servings = originalRecipe.servings,
            isFavorite = false,
            createdAt = System.currentTimeMillis() / 1000
        )
    }
    
    private fun getVariationDescription(variationType: VariationType): String {
        return when (variationType) {
            VariationType.SIMILAR_INGREDIENTS -> "Ähnliche Zutaten"
            VariationType.SAME_CUISINE -> "Gleiche Küche"
            VariationType.SAME_TYPE -> "Gleicher Typ"
            VariationType.HEALTHIER_VERSION -> "Gesündere Variante"
            VariationType.EASIER_VERSION -> "Einfachere Variante"
            VariationType.DIFFERENT_PROTEIN -> "Andere Proteinquelle"
            VariationType.DIETARY_ADAPTATION -> "Diätetische Anpassung"
            VariationType.SEASONAL_VARIATION -> "Saisonale Variante"
            VariationType.AI_GENERATED_VARIATION -> "KI-generierte Variation"
        }
    }
    
    private fun calculatePreferenceScore(recipe: SavedRecipeEntity, preferences: DietaryPreferences): Float {
        var score = 0f
        
        val tags = recipe.tags.lowercase()
        
        if (preferences.isVegetarian && tags.contains("vegetarian")) score += 0.3f
        if (preferences.isVegan && tags.contains("vegan")) score += 0.4f
        if (preferences.isGlutenFree && tags.contains("gluten-free")) score += 0.3f
        if (preferences.isLowCarb && tags.contains("low-carb")) score += 0.3f
        if (preferences.isHighProtein && tags.contains("high-protein")) score += 0.3f
        
        preferences.maxCalories?.let { maxCal ->
            recipe.calories?.let { calories ->
                if (calories <= maxCal) score += 0.2f
            }
        }
        
        preferences.maxPrepTime?.let { maxTime ->
            recipe.prepTime?.let { prepTime ->
                if (prepTime <= maxTime) score += 0.2f
            }
        }
        
        return kotlin.math.min(score, 1.0f)
    }
    
    private fun generatePreferenceReasons(recipe: SavedRecipeEntity, preferences: DietaryPreferences): List<String> {
        val reasons = mutableListOf<String>()
        val tags = recipe.tags.lowercase()
        
        if (preferences.isVegetarian && tags.contains("vegetarian")) reasons.add("Vegetarisch")
        if (preferences.isVegan && tags.contains("vegan")) reasons.add("Vegan")
        if (preferences.isGlutenFree && tags.contains("gluten-free")) reasons.add("Glutenfrei")
        if (preferences.isLowCarb && tags.contains("low-carb")) reasons.add("Low Carb")
        if (preferences.isHighProtein && tags.contains("high-protein")) reasons.add("Proteinreich")
        
        preferences.maxCalories?.let { maxCal ->
            recipe.calories?.let { calories ->
                if (calories <= maxCal) reasons.add("Passt zu Ihrem Kalorienziel")
            }
        }
        
        preferences.maxPrepTime?.let { maxTime ->
            recipe.prepTime?.let { prepTime ->
                if (prepTime <= maxTime) reasons.add("Schnell zubereitet")
            }
        }
        
        return reasons
    }
}
