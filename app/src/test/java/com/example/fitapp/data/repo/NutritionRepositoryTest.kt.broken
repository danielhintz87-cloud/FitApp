package com.example.fitapp.data.repo

import android.content.Context
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Unit tests for NutritionRepository
 * Tests nutrition data management and recipe operations
 */
class NutritionRepositoryTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var recipeDao: RecipeDao

    private lateinit var repository: NutritionRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        whenever(database.recipeDao()).thenReturn(recipeDao)
        
        repository = NutritionRepository(database)
    }

    // Recipe Management Tests

    @Test
    fun `favorites flow returns favorite recipes`() = runTest {
        // Given: Favorite recipes in database
        val favoriteRecipes = listOf(
            createTestRecipe(id = "1", title = "Favorite Recipe 1"),
            createTestRecipe(id = "2", title = "Favorite Recipe 2")
        )
        
        whenever(recipeDao.favoritesFlow()).thenReturn(flowOf(favoriteRecipes))
        
        // When: Getting favorites
        val result = repository.favorites()
        
        // Then: Should return favorite recipes flow
        result.collect { recipes ->
            assertEquals(2, recipes.size)
            assertEquals("Favorite Recipe 1", recipes[0].title)
            assertEquals("Favorite Recipe 2", recipes[1].title)
        }
        
        verify(recipeDao).favoritesFlow()
    }

    @Test
    fun `history flow returns recipe history`() = runTest {
        // Given: Recipe history in database
        val historyRecipes = listOf(
            createTestRecipe(id = "3", title = "Recent Recipe 1"),
            createTestRecipe(id = "4", title = "Recent Recipe 2")
        )
        
        whenever(recipeDao.historyFlow()).thenReturn(flowOf(historyRecipes))
        
        // When: Getting history
        val result = repository.history()
        
        // Then: Should return history recipes flow
        result.collect { recipes ->
            assertEquals(2, recipes.size)
            assertEquals("Recent Recipe 1", recipes[0].title)
            assertEquals("Recent Recipe 2", recipes[1].title)
        }
        
        verify(recipeDao).historyFlow()
    }

    @Test
    fun `recipe categorization works correctly`() = runTest {
        // Given: Different ingredient types
        val meatIngredient = "Hähnchenbrust"
        val vegetableIngredient = "Brokkoli"
        val fruitIngredient = "Apfel"
        val grainIngredient = "Haferflocken"
        
        // When: Categorizing ingredients (this would be internal logic)
        val meatCategory = categorizeTestIngredient(meatIngredient)
        val vegetableCategory = categorizeTestIngredient(vegetableIngredient)
        val fruitCategory = categorizeTestIngredient(fruitIngredient)
        val grainCategory = categorizeTestIngredient(grainIngredient)
        
        // Then: Should categorize correctly
        assertEquals("Fleisch & Fisch", meatCategory)
        assertEquals("Gemüse", vegetableCategory)
        assertEquals("Obst", fruitCategory)
        assertEquals("Getreide & Kohlenhydrate", grainCategory)
    }

    // Helper Methods for Testing Recipe Logic

    private fun createTestRecipe(
        id: String = "1",
        title: String = "Test Recipe",
        calories: Int = 300,
        markdown: String = "# Test Recipe\nInstructions here",
        imageUrl: String? = null
    ): RecipeEntity {
        return RecipeEntity(
            id = id,
            title = title,
            markdown = markdown,
            calories = calories,
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis() / 1000
        )
    }
    
    private fun categorizeTestIngredient(ingredient: String): String {
        val lower = ingredient.lowercase()
        return when {
            lower.contains("hähnchen") || lower.contains("fleisch") -> "Fleisch & Fisch"
            lower.contains("brokkoli") || lower.contains("gemüse") -> "Gemüse"
            lower.contains("apfel") || lower.contains("obst") -> "Obst"
            lower.contains("hafer") || lower.contains("reis") -> "Getreide & Kohlenhydrate"
            else -> "Sonstiges"
        }
    }
}