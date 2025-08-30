package com.example.fitapp.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeIndicesTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun verifyRecipeIndicesExist() {
        val db = database.openHelper.writableDatabase
        
        // Check if recipe indices exist
        db.query("PRAGMA index_list('recipes')").use { cursor ->
            val indices = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                val indexName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                indices.add(indexName)
            }
            
            // Verify that our expected indices exist
            assert(indices.any { it.contains("createdAt") }) { "Missing createdAt index" }
            assert(indices.any { it.contains("calories") }) { "Missing calories index" }
            assert(indices.any { it.contains("title") }) { "Missing title index" }
        }
    }

    @Test
    fun testRecipeQueryPerformance() {
        val recipeDao = database.recipeDao()
        
        // Insert test data
        val testRecipe = RecipeEntity(
            id = "test-1",
            title = "Test Recipe",
            markdown = "# Test Recipe\nIngredients: ...",
            calories = 500,
            imageUrl = null,
            createdAt = System.currentTimeMillis() / 1000
        )
        
        // This test verifies the database can be used without errors
        // The indices should make queries faster automatically
        runBlocking {
            recipeDao.upsertRecipe(testRecipe)
            val retrieved = recipeDao.getRecipe("test-1")
            assert(retrieved != null) { "Recipe should be retrievable" }
            assert(retrieved?.title == "Test Recipe") { "Recipe title should match" }
        }
    }
}

private suspend fun runBlocking(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking {
        block()
    }
}