package com.example.fitapp.data.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TEST_DB = "comprehensive_migration_test.db"

/**
 * Comprehensive Room database migration tests that validate:
 * - Schema integrity across all migrations
 * - Data preservation during migrations
 * - Index creation and consistency
 * - Full migration path from earliest to latest
 * - Deterministic migration behavior (no flakiness)
 */
@RunWith(AndroidJUnit4::class)
class ComprehensiveMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migration_5_to_6_creates_personal_tables() {
        // Start from version 5 
        val db5 = helper.createDatabase(TEST_DB, 5)
        db5.close()

        // Migrate to version 6
        val db6 = helper.runMigrationsAndValidate(
            TEST_DB,
            6,
            true,
            AppDatabase.MIGRATION_5_6
        )

        // Validate all expected tables exist
        val expectedTables = listOf(
            "personal_achievements",
            "personal_streaks", 
            "personal_records",
            "progress_milestones"
        )

        expectedTables.forEach { tableName ->
            db6.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'").use { cursor ->
                assertTrue(cursor.moveToFirst(), "$tableName table should exist after migration 5→6")
            }
        }

        // Validate personal_achievements table structure
        validateTableSchema(db6, "personal_achievements", mapOf(
            "id" to "INTEGER",
            "title" to "TEXT",
            "description" to "TEXT", 
            "category" to "TEXT",
            "iconName" to "TEXT",
            "targetValue" to "REAL",
            "currentValue" to "REAL",
            "unit" to "TEXT",
            "isCompleted" to "INTEGER",
            "completedAt" to "INTEGER",
            "createdAt" to "INTEGER"
        ))

        db6.close()
    }

    @Test
    fun migration_6_to_7_converts_date_to_timestamp() {
        // Create database version 6 with test data
        val db6 = helper.createDatabase(TEST_DB, 6)
        
        // Insert test streak with date string
        db6.execSQL("""
            INSERT INTO personal_streaks (name, description, category, currentStreak, longestStreak, lastActivityDate, isActive, createdAt)
            VALUES ('Test Streak', 'Test Description', 'workout', 5, 10, '2024-01-15', 1, 1705315200)
        """)
        
        db6.close()

        // Migrate to version 7
        val db7 = helper.runMigrationsAndValidate(
            TEST_DB,
            7,
            true,
            AppDatabase.MIGRATION_6_7
        )

        // Verify data conversion
        db7.query("SELECT lastActivityTimestamp FROM personal_streaks WHERE name = 'Test Streak'").use { cursor ->
            assertTrue(cursor.moveToFirst(), "Test streak should exist after migration")
            val timestamp = cursor.getLong(0)
            assertTrue(timestamp > 0, "lastActivityTimestamp should be converted to valid epoch seconds")
        }

        db7.close()
    }

    @Test
    fun migration_7_to_8_creates_weight_entries_and_indices() {
        val db7 = helper.createDatabase(TEST_DB, 7)
        
        // Insert test recipe data to verify indices are added
        db7.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt)
            VALUES ('test-id', 'Test Recipe', '# Test', 300, NULL, 0, 1705315200)
        """)
        
        db7.close()

        val db8 = helper.runMigrationsAndValidate(
            TEST_DB,
            8,
            true,
            AppDatabase.MIGRATION_7_8
        )

        // Validate weight_entries table exists and has correct structure
        validateTableSchema(db8, "weight_entries", mapOf(
            "id" to "INTEGER",
            "weight" to "REAL",
            "dateIso" to "TEXT",
            "notes" to "TEXT",
            "recordedAt" to "INTEGER"
        ))

        // Validate required indices exist
        val expectedIndices = listOf(
            "index_weight_entries_dateIso",
            "index_weight_entries_recordedAt",
            "index_recipes_createdAt",
            "index_recipes_calories",
            "index_recipes_title"
        )

        expectedIndices.forEach { indexName ->
            validateIndexExists(db8, indexName)
        }

        // Test weight_entries functionality
        db8.execSQL("""
            INSERT INTO weight_entries (weight, dateIso, notes, recordedAt)
            VALUES (75.5, '2024-01-15', 'Test weight', 1705315200)
        """)

        db8.query("SELECT weight FROM weight_entries WHERE dateIso = '2024-01-15'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(75.5, cursor.getDouble(0), 0.01)
        }

        db8.close()
    }

    @Test
    fun migration_8_to_9_extends_goals_and_creates_nutrition_tables() {
        val db8 = helper.createDatabase(TEST_DB, 8)
        
        // Insert test daily goal
        db8.execSQL("""
            INSERT INTO daily_goals (dateIso, targetKcal)
            VALUES ('2024-01-15', 2000)
        """)
        
        db8.close()

        val db9 = helper.runMigrationsAndValidate(
            TEST_DB,
            9,
            true,
            AppDatabase.MIGRATION_8_9
        )

        // Verify daily_goals has new columns
        validateTableSchema(db9, "daily_goals", mapOf(
            "dateIso" to "TEXT",
            "targetKcal" to "INTEGER",
            "targetCarbs" to "REAL",
            "targetProtein" to "REAL", 
            "targetFat" to "REAL",
            "targetWaterMl" to "INTEGER"
        ))

        // Verify existing data is preserved
        db9.query("SELECT targetKcal FROM daily_goals WHERE dateIso = '2024-01-15'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(2000, cursor.getInt(0))
        }

        // Verify new nutrition tables exist
        val nutritionTables = listOf("food_items", "meal_entries", "water_entries")
        nutritionTables.forEach { tableName ->
            db9.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'").use { cursor ->
                assertTrue(cursor.moveToFirst(), "$tableName should exist after migration 8→9")
            }
        }

        db9.close()
    }

    @Test
    fun migration_9_to_10_extends_food_items_and_creates_bmi_tables() {
        val db9 = helper.createDatabase(TEST_DB, 9)
        
        // Insert test food item
        db9.execSQL("""
            INSERT INTO food_items (id, name, calories, carbs, protein, fat, createdAt)
            VALUES ('test-food', 'Test Food', 100, 10.0, 5.0, 2.0, 1705315200)
        """)
        
        db9.close()

        val db10 = helper.runMigrationsAndValidate(
            TEST_DB,
            10,
            true,
            AppDatabase.MIGRATION_9_10
        )

        // Verify food_items has extended fields
        val expectedFoodColumns = mapOf(
            "id" to "TEXT",
            "name" to "TEXT",
            "calories" to "INTEGER",
            "carbs" to "REAL",
            "protein" to "REAL",
            "fat" to "REAL",
            "createdAt" to "INTEGER",
            "fiber" to "REAL",
            "sugar" to "REAL", 
            "sodium" to "REAL",
            "brands" to "TEXT",
            "categories" to "TEXT",
            "imageUrl" to "TEXT",
            "servingSize" to "TEXT",
            "ingredients" to "TEXT"
        )
        validateTableSchema(db10, "food_items", expectedFoodColumns)

        // Verify existing data is preserved
        db10.query("SELECT name, calories FROM food_items WHERE id = 'test-food'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Test Food", cursor.getString(0))
            assertEquals(100, cursor.getInt(1))
        }

        // Verify BMI and weight loss tables exist
        val bmiTables = listOf("bmi_history", "weight_loss_programs", "behavioral_check_ins", "progress_photos")
        bmiTables.forEach { tableName ->
            db10.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'").use { cursor ->
                assertTrue(cursor.moveToFirst(), "$tableName should exist after migration 9→10")
            }
        }

        db10.close()
    }

    @Test
    fun full_migration_path_from_earliest_to_latest() {
        // Test complete migration from version 5 to current version
        val db5 = helper.createDatabase(TEST_DB, 5)
        
        // Insert comprehensive test data across all tables
        insertTestDataVersion5(db5)
        
        db5.close()

        // Run all migrations in sequence
        val dbLatest = helper.runMigrationsAndValidate(
            TEST_DB,
            17, // Current latest version
            true,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
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

        // Verify all critical tables exist in latest version
        val criticalTables = listOf(
            "recipes", "personal_achievements", "personal_streaks", "weight_entries",
            "food_items", "meal_entries", "bmi_history", "workout_performance",
            "cooking_sessions", "health_connect_steps", "cloud_sync_metadata",
            "social_challenges", "recipe_collections"
        )

        criticalTables.forEach { tableName ->
            dbLatest.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'").use { cursor ->
                assertTrue(cursor.moveToFirst(), "$tableName should exist in final schema")
            }
        }

        // Verify data integrity through full migration
        verifyTestDataIntegrity(dbLatest)

        dbLatest.close()
    }

    @Test
    fun migration_is_deterministic_and_repeatable() {
        // Test that running migrations multiple times produces consistent results
        for (attempt in 1..3) {
            val testDbName = "${TEST_DB}_attempt_$attempt"
            
            val db7 = helper.createDatabase(testDbName, 7)
            
            // Insert identical test data
            db7.execSQL("""
                INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt)
                VALUES ('deterministic-test', 'Deterministic Recipe', '# Test', 300, NULL, 0, 1705315200)
            """)
            
            db7.close()

            // Run migration
            val db8 = helper.runMigrationsAndValidate(
                testDbName,
                8,
                true,
                AppDatabase.MIGRATION_7_8
            )

            // Verify consistent results
            db8.query("SELECT title, calories FROM recipes WHERE id = 'deterministic-test'").use { cursor ->
                assertTrue(cursor.moveToFirst(), "Data should be preserved consistently across attempts")
                assertEquals("Deterministic Recipe", cursor.getString(0))
                assertEquals(300, cursor.getInt(1))
            }

            // Verify weight_entries table was created consistently
            validateTableExists(db8, "weight_entries")
            
            db8.close()
        }
    }

    @Test
    fun migration_handles_edge_cases_gracefully() {
        val db7 = helper.createDatabase(TEST_DB, 7)
        
        // Insert edge case data
        db7.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt)
            VALUES ('edge-case', '', '', -1, '', 0, 0)
        """)
        
        db7.close()

        // Migration should complete without errors
        val db8 = helper.runMigrationsAndValidate(
            TEST_DB,
            8,
            true,
            AppDatabase.MIGRATION_7_8
        )

        // Edge case data should be preserved
        db8.query("SELECT COUNT(*) FROM recipes WHERE id = 'edge-case'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }

        db8.close()
    }

    // Helper functions for validation

    private fun validateTableSchema(db: SupportSQLiteDatabase, tableName: String, expectedColumns: Map<String, String>) {
        db.query("PRAGMA table_info($tableName)").use { cursor ->
            val actualColumns = mutableMapOf<String, String>()
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            val typeIndex = cursor.getColumnIndexOrThrow("type")
            
            while (cursor.moveToNext()) {
                val columnName = cursor.getString(nameIndex)
                val columnType = cursor.getString(typeIndex)
                actualColumns[columnName] = columnType
            }
            
            expectedColumns.forEach { (columnName, expectedType) ->
                assertTrue(
                    actualColumns.containsKey(columnName),
                    "Table $tableName should have column $columnName"
                )
                assertEquals(
                    expectedType,
                    actualColumns[columnName],
                    "Column $tableName.$columnName should have type $expectedType"
                )
            }
        }
    }

    private fun validateIndexExists(db: SupportSQLiteDatabase, indexName: String) {
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='$indexName'").use { cursor ->
            assertTrue(cursor.moveToFirst(), "Index $indexName should exist")
        }
    }

    private fun validateTableExists(db: SupportSQLiteDatabase, tableName: String) {
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'").use { cursor ->
            assertTrue(cursor.moveToFirst(), "Table $tableName should exist")
        }
    }

    private fun insertTestDataVersion5(db: SupportSQLiteDatabase) {
        // Insert base recipe data that should survive all migrations
        db.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt)
            VALUES ('migration-test-recipe', 'Migration Test Recipe', '# Test Recipe\nThis survives migrations', 
                    250, NULL, 0, 1705315200)
        """)
        
        // Insert intake entry
        db.execSQL("""
            INSERT INTO intake_entries (timestamp, label, kcal, source, referenceId)
            VALUES (1705315200, 'Test Meal', 300, 'manual', 'migration-test-recipe')
        """)
    }

    private fun verifyTestDataIntegrity(db: SupportSQLiteDatabase) {
        // Verify original recipe data survived
        db.query("SELECT title, calories FROM recipes WHERE id = 'migration-test-recipe'").use { cursor ->
            assertTrue(cursor.moveToFirst(), "Migration test recipe should survive full migration")
            assertEquals("Migration Test Recipe", cursor.getString(0))
            assertEquals(250, cursor.getInt(1))
        }
        
        // Verify intake entry survived
        db.query("SELECT label, kcal FROM intake_entries WHERE referenceId = 'migration-test-recipe'").use { cursor ->
            assertTrue(cursor.moveToFirst(), "Migration test intake entry should survive")
            assertEquals("Test Meal", cursor.getString(0))
            assertEquals(300, cursor.getInt(1))
        }
    }
}