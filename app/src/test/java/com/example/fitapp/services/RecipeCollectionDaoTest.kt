package com.example.fitapp.services

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.RecipeCollectionEntity
import com.example.fitapp.data.db.RecipeCollectionItemEntity
import com.example.fitapp.data.db.RecipeEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class RecipeCollectionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var recipeFavoritesManager: RecipeFavoritesManager

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        recipeFavoritesManager = RecipeFavoritesManager(database)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun `insertCollection should persist collection entity`() = runTest {
        // Given
        val collection = RecipeCollectionEntity(
            id = "test-collection-1",
            name = "Vegetarian Delights",
            description = "Healthy vegetarian recipes",
            isOfficial = false,
            isPremium = false,
            sortOrder = 1,
            createdAt = System.currentTimeMillis() / 1000
        )

        // When
        database.recipeCollectionDao().insertCollection(collection)

        // Then
        val retrievedCollection = database.recipeCollectionDao().getCollectionById("test-collection-1")
        assertNotNull(retrievedCollection)
        assertEquals("Vegetarian Delights", retrievedCollection?.name)
        assertEquals("Healthy vegetarian recipes", retrievedCollection?.description)
        assertEquals(false, retrievedCollection?.isPremium)
    }

    @Test
    fun `addRecipeToCollection should create collection item`() = runTest {
        // Given
        val collection = RecipeCollectionEntity(
            id = "collection-1",
            name = "Quick Meals",
            description = "Fast and easy recipes"
        )
        
        val recipe = RecipeEntity(
            id = "recipe-1",
            title = "Quick Pasta",
            description = "Fast pasta recipe",
            markdown = "## Ingredients\n- Pasta\n- Sauce",
            imageUrl = null,
            prepTime = 15,
            cookTime = 10,
            servings = 2,
            difficulty = "easy",
            categories = "quick,italian",
            calories = 400
        )

        database.recipeCollectionDao().insertCollection(collection)
        database.recipeDao().upsertRecipe(recipe)

        // When
        recipeFavoritesManager.addToCollection("recipe-1", "collection-1")

        // Then
        val recipesInCollection = database.recipeCollectionDao().getRecipesInCollection("collection-1")
        assertEquals(1, recipesInCollection.size)
        assertEquals("recipe-1", recipesInCollection[0].id)
        assertEquals("Quick Pasta", recipesInCollection[0].title)
    }

    @Test
    fun `removeFromCollection should remove collection item`() = runTest {
        // Given
        val collection = RecipeCollectionEntity(
            id = "collection-2",
            name = "Desserts",
            description = "Sweet treats"
        )
        
        val item = RecipeCollectionItemEntity(
            collectionId = "collection-2",
            recipeId = "recipe-dessert-1",
            sortOrder = 0,
            addedAt = System.currentTimeMillis() / 1000
        )

        database.recipeCollectionDao().insertCollection(collection)
        database.recipeCollectionDao().insertCollectionItem(item)

        // When
        recipeFavoritesManager.removeFromCollection("recipe-dessert-1", "collection-2")

        // Then
        val recipesInCollection = database.recipeCollectionDao().getRecipesInCollection("collection-2")
        assertEquals(0, recipesInCollection.size)
    }

    @Test
    fun `createCollection via manager should persist collection and items`() = runTest {
        // Given
        val recipeIds = listOf("recipe-1", "recipe-2")

        // When
        val collection = recipeFavoritesManager.createCollection(
            name = "Meal Prep Favorites",
            description = "Perfect for weekly preparation",
            recipeIds = recipeIds
        )

        // Then
        assertNotNull(collection)
        assertEquals("Meal Prep Favorites", collection.name)
        assertEquals("Perfect for weekly preparation", collection.description)
        assertEquals(2, collection.recipes.size)

        // Verify in database
        val storedCollection = database.recipeCollectionDao().getCollectionById(collection.id)
        assertNotNull(storedCollection)
        assertEquals("Meal Prep Favorites", storedCollection?.name)

        val items = database.recipeCollectionDao().getCollectionItems(collection.id)
        assertEquals(2, items.size)
        assertEquals("recipe-1", items[0].recipeId)
        assertEquals("recipe-2", items[1].recipeId)
    }

    @Test
    fun `getAllCollections should return collections sorted correctly`() = runTest {
        // Given
        val collection1 = RecipeCollectionEntity(
            id = "collection-1",
            name = "High Priority",
            sortOrder = 1,
            createdAt = System.currentTimeMillis() / 1000 - 100
        )
        
        val collection2 = RecipeCollectionEntity(
            id = "collection-2", 
            name = "Low Priority",
            sortOrder = 2,
            createdAt = System.currentTimeMillis() / 1000
        )

        database.recipeCollectionDao().insertCollection(collection2) // Insert second first
        database.recipeCollectionDao().insertCollection(collection1)

        // When
        val collections = database.recipeCollectionDao().getAllCollections()

        // Then
        assertEquals(2, collections.size)
        assertEquals("High Priority", collections[0].name) // Should be first due to lower sortOrder
        assertEquals("Low Priority", collections[1].name)
    }

    @Test
    fun `isRecipeInCollection should return correct status`() = runTest {
        // Given
        val collection = RecipeCollectionEntity(id = "collection-test", name = "Test Collection")
        val item = RecipeCollectionItemEntity(
            collectionId = "collection-test",
            recipeId = "recipe-existing",
            sortOrder = 0
        )

        database.recipeCollectionDao().insertCollection(collection)
        database.recipeCollectionDao().insertCollectionItem(item)

        // When & Then
        assertTrue(database.recipeCollectionDao().isRecipeInCollection("collection-test", "recipe-existing"))
        assertFalse(database.recipeCollectionDao().isRecipeInCollection("collection-test", "recipe-nonexistent"))
    }

    @Test
    fun `getCollectionRecipeCount should return accurate count`() = runTest {
        // Given
        val collection = RecipeCollectionEntity(id = "collection-count", name = "Count Test")
        database.recipeCollectionDao().insertCollection(collection)

        // Add 3 items
        repeat(3) { index ->
            val item = RecipeCollectionItemEntity(
                collectionId = "collection-count",
                recipeId = "recipe-$index",
                sortOrder = index
            )
            database.recipeCollectionDao().insertCollectionItem(item)
        }

        // When
        val count = database.recipeCollectionDao().getCollectionRecipeCount("collection-count")

        // Then
        assertEquals(3, count)
    }
}