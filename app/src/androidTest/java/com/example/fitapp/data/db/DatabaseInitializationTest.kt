package com.example.fitapp.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseInitializationTest {
    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        // Test database initialization with in-memory database
        database =
            Room.inMemoryDatabaseBuilder(
                context, AppDatabase::class.java,
            ).build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testDatabaseCreationSucceeds() {
        // This test verifies that the database can be created without SQLite errors
        val db = database.openHelper.writableDatabase

        // Verify database is accessible
        assert(db.isOpen) { "Database should be open" }

        // Test basic database operations don't throw SQLite exceptions
        db.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
            assert(cursor.count > 0) { "Database should have tables" }
        }
    }

    @Test
    fun testPerformanceIndicesCreated() {
        val db = database.openHelper.writableDatabase

        // Check if some key indices exist
        db.query("PRAGMA index_list('recipes')").use { cursor ->
            val indices = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                val indexName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                indices.add(indexName)
            }

            // Should have at least some indices created
            assert(indices.isNotEmpty()) { "Recipes table should have indices" }
        }
    }

    @Test
    fun testDatabaseOperationsWork() =
        runBlocking {
            // Test that basic DAO operations work without crashes
            val recipeDao = database.recipeDao()

            val testRecipe =
                RecipeEntity(
                    id = "test-1",
                    title = "Test Recipe",
                    markdown = "# Test Recipe\\nIngredients: test",
                    calories = 500,
                    imageUrl = null,
                    createdAt = System.currentTimeMillis() / 1000,
                )

            // This should not throw any SQLite exceptions
            recipeDao.upsertRecipe(testRecipe)
            val retrieved = recipeDao.getRecipe("test-1")

            assert(retrieved != null) { "Recipe should be retrievable after insert" }
            assert(retrieved?.title == "Test Recipe") { "Recipe title should match" }
        }

    @Test
    fun testDatabaseFallbackMechanism() {
        // Test that the database initialization includes fallback handling
        // by attempting to create the database using the static get method
        try {
            val testDb = AppDatabase.get(context)
            assert(testDb != null) { "Database should be created successfully" }
        } catch (e: Exception) {
            // If there's an exception, it should be handled gracefully
            assert(false) { "Database creation should not throw unhandled exceptions: ${e.message}" }
        }
    }
}
