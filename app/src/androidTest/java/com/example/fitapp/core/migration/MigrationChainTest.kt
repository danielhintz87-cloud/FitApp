package com.example.fitapp.core.migration

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitapp.data.db.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "fitapp_migration_chain_test.db"

/**
 * Test to verify the complete migration chain from v14 to v17
 * This ensures no migration gaps exist that could cause data loss
 */
@RunWith(AndroidJUnit4::class)
class MigrationChainTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migration_8_to_17_complete_chain_completes_successfully() {
        // 1) Create database with version 8 (oldest available schema)
        helper.createDatabase(TEST_DB, 8).apply { close() }

        // 2) Run complete migration chain 8→9→10→11→12→13→14→15→16→17
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16,
                AppDatabase.MIGRATION_16_17
            )
            .build()

        // 3) Verify database opens successfully at version 17 with detailed RecipeEntity validation
        db.openHelper.writableDatabase.use { sqlDb ->
            // Basic schema validation - ensure core tables exist
            val tables = mutableSetOf<String>()
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { c ->
                val nameIdx = c.getColumnIndexOrThrow("name")
                while (c.moveToNext()) {
                    tables += c.getString(nameIdx)
                }
            }
            
            // Verify key tables exist after migration
            val requiredTables = setOf(
                "recipes", "ai_logs", "personal_achievements", 
                "cooking_sessions", "recipe_ingredients", "meal_entries"
            )
            
            for (table in requiredTables) {
                assert(tables.contains(table)) { 
                    "Required table '$table' missing after migration. Found tables: $tables" 
                }
            }
            
            // **Enhanced RecipeEntity schema validation**
            validateRecipeEntitySchema(sqlDb)
        }
        
        db.close()
    }

    @Test
    fun migration_14_to_17_chain_completes_successfully() {
        // 1) Create database with version 14 (known stable version from logs)
        helper.createDatabase(TEST_DB, 14).apply { close() }

        // 2) Run complete migration chain 14→15→16→17
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16,
                AppDatabase.MIGRATION_16_17
            )
            .build()

        // 3) Verify database opens successfully at version 17
        db.openHelper.writableDatabase.use { sqlDb ->
            // Basic schema validation - ensure core tables exist
            val tables = mutableSetOf<String>()
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { c ->
                val nameIdx = c.getColumnIndexOrThrow("name")
                while (c.moveToNext()) {
                    tables += c.getString(nameIdx)
                }
            }
            
            // Verify key tables exist after migration
            val requiredTables = setOf(
                "recipes", "ai_logs", "personal_achievements", 
                "cooking_sessions", "recipe_ingredients", "meal_entries"
            )
            
            for (table in requiredTables) {
                assert(tables.contains(table)) { 
                    "Required table '$table' missing after migration. Found tables: $tables" 
                }
            }
            
            // **Enhanced RecipeEntity schema validation**
            validateRecipeEntitySchema(sqlDb)
        }
        
        db.close()
    }

    @Test
    fun migration_15_to_17_yazio_features_added() {
        // Test that YAZIO-style features are properly added in 15→16→17
        helper.createDatabase(TEST_DB, 15).apply { close() }
        
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_15_16, AppDatabase.MIGRATION_16_17)
            .build()
            
        db.openHelper.writableDatabase.use { sqlDb ->
            // Verify YAZIO tables were created
            val yazioTables = setOf(
                "recipe_ingredients", "recipe_steps", "grocery_lists", 
                "grocery_items", "recipe_collections"
            )
            
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { c ->
                val foundTables = mutableSetOf<String>()
                val nameIdx = c.getColumnIndexOrThrow("name")
                while (c.moveToNext()) {
                    foundTables += c.getString(nameIdx)
                }
                
                for (table in yazioTables) {
                    assert(foundTables.contains(table)) { 
                        "YAZIO table '$table' missing after migration 15→17" 
                    }
                }
            }
            
            // **Enhanced RecipeEntity schema validation after YAZIO migration**
            validateRecipeEntitySchema(sqlDb)
        }
        
        db.close()
    }
    
    /**
     * Validates that the RecipeEntity schema matches expected structure from v17 JSON schema.
     * Ensures no schema drift between entity definitions and actual database structure.
     */
    private fun validateRecipeEntitySchema(db: SupportSQLiteDatabase) {
        // Verify recipes table columns match RecipeEntity exactly
        val recipeColumns = mutableMapOf<String, String>()
        
        db.query("PRAGMA table_info(recipes)").use { cursor ->
            while (cursor.moveToNext()) {
                val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val columnType = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                recipeColumns[columnName] = columnType
            }
        }
        
        // Expected columns from RecipeEntity based on v17 schema
        val expectedColumns = mapOf(
            "id" to "TEXT",
            "title" to "TEXT", 
            "description" to "TEXT",
            "markdown" to "TEXT",
            "imageUrl" to "TEXT",
            "prepTime" to "INTEGER",
            "cookTime" to "INTEGER", 
            "servings" to "INTEGER",
            "difficulty" to "TEXT",
            "categories" to "TEXT",
            "calories" to "INTEGER",
            "protein" to "REAL",
            "carbs" to "REAL",
            "fat" to "REAL",
            "fiber" to "REAL",
            "sugar" to "REAL", 
            "sodium" to "REAL",
            "createdAt" to "INTEGER",
            "isOfficial" to "INTEGER",
            "rating" to "REAL",
            "ratingCount" to "INTEGER",
            "isLocalOnly" to "INTEGER"
        )
        
        // Validate each expected column exists with correct type
        for ((columnName, expectedType) in expectedColumns) {
            assert(recipeColumns.containsKey(columnName)) {
                "Missing column '$columnName' in recipes table. Found columns: ${recipeColumns.keys}"
            }
            
            val actualType = recipeColumns[columnName]!!.uppercase()
            val expectedTypeUpper = expectedType.uppercase()
            
            assert(actualType == expectedTypeUpper) {
                "Column '$columnName' has type '$actualType', expected '$expectedTypeUpper'"
            }
        }
        
        // Verify no extra columns exist that shouldn't be there
        val extraColumns = recipeColumns.keys - expectedColumns.keys
        assert(extraColumns.isEmpty()) {
            "Unexpected columns in recipes table: $extraColumns"
        }
        
        // Verify all required RecipeEntity indices exist
        val requiredIndices = setOf(
            "index_recipes_createdAt",
            "index_recipes_calories", 
            "index_recipes_title",
            "index_recipes_difficulty",
            "index_recipes_prepTime",
            "index_recipes_cookTime"
        )
        
        val actualIndices = mutableSetOf<String>()
        db.query("PRAGMA index_list(recipes)").use { cursor ->
            while (cursor.moveToNext()) {
                val indexName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                actualIndices.add(indexName)
            }
        }
        
        for (requiredIndex in requiredIndices) {
            assert(actualIndices.contains(requiredIndex)) {
                "Missing required index '$requiredIndex' on recipes table. Found indices: $actualIndices"
            }
        }
        
        // Verify primary key constraint
        db.query("PRAGMA table_info(recipes)").use { cursor ->
            var foundPrimaryKey = false
            while (cursor.moveToNext()) {
                val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val isPrimaryKey = cursor.getInt(cursor.getColumnIndexOrThrow("pk")) > 0
                
                if (isPrimaryKey) {
                    assert(columnName == "id") {
                        "Primary key should be 'id', but found '$columnName'"
                    }
                    foundPrimaryKey = true
                }
            }
            assert(foundPrimaryKey) { "No primary key found on recipes table" }
        }
    }
    
    /**
     * **Nice to have: Focused single-step test for migration 16→17 (latest)**
     * Tests recipe support addition to meal_entries for faster feedback during development.
     */
    @Test
    fun migration_16_to_17_recipe_support_in_meal_entries() {
        // Create database at version 16
        helper.createDatabase(TEST_DB, 16).apply { close() }
        
        // Apply only migration 16→17
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_16_17)
            .build()
        
        db.openHelper.writableDatabase.use { sqlDb ->
            // Verify meal_entries table was updated to support recipes
            val mealEntryColumns = mutableMapOf<String, String>()
            
            sqlDb.query("PRAGMA table_info(meal_entries)").use { cursor ->
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val columnType = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                    mealEntryColumns[columnName] = columnType
                }
            }
            
            // Verify new recipe-related columns exist
            assert(mealEntryColumns.containsKey("recipeId")) {
                "meal_entries should have recipeId column after migration 16→17"
            }
            assert(mealEntryColumns.containsKey("servings")) {
                "meal_entries should have servings column after migration 16→17"
            }
            
            // Verify original columns still exist
            val requiredColumns = setOf("id", "foodItemId", "date", "mealType", "quantityGrams", "notes")
            for (column in requiredColumns) {
                assert(mealEntryColumns.containsKey(column)) {
                    "meal_entries missing original column '$column' after migration 16→17"
                }
            }
        }
        
        db.close()
    }
    
    /**
     * **Nice to have: Focused single-step test for migration 15→16 (YAZIO features)**
     * Tests YAZIO-style enhancement to recipes table for faster feedback.
     */
    @Test
    fun migration_15_to_16_yazio_recipe_enhancement() {
        // Create database at version 15
        helper.createDatabase(TEST_DB, 15).apply { close() }
        
        // Apply only migration 15→16
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_15_16)
            .build()
        
        db.openHelper.writableDatabase.use { sqlDb ->
            // Verify enhanced recipes table matches RecipeEntity schema
            validateRecipeEntitySchema(sqlDb)
            
            // Verify YAZIO-specific tables were created
            val yazioTables = setOf(
                "recipe_ingredients", "recipe_steps", "grocery_lists",
                "grocery_items", "recipe_analytics", "recipe_ratings",
                "pro_feature_access", "recipe_collections", "recipe_collection_items"
            )
            
            val actualTables = mutableSetOf<String>()
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
                while (cursor.moveToNext()) {
                    actualTables.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
            }
            
            for (table in yazioTables) {
                assert(actualTables.contains(table)) {
                    "YAZIO table '$table' missing after migration 15→16"
                }
            }
        }
        
        db.close()
    }
}