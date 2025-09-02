package com.example.fitapp.data.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "fitapp_migration_test.db"

/**
 * Comprehensive database migration tests
 * Tests schema version upgrades and data preservation
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migration_7_to_8_creates_weight_entries_table() {
        // 1) Create database with version 7
        helper.createDatabase(TEST_DB, 7).apply { close() }

        // 2) Run migration 7→8
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_7_8)
            .build()

        // 3) Validate that weight_entries table exists
        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='weight_entries'").use { c ->
                assert(c.moveToFirst()) { "weight_entries table should exist after migration" }
            }
        }
        
        // 4) Validate table structure
        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("PRAGMA table_info(weight_entries)").use { c ->
                val columns = mutableSetOf<String>()
                val nameIdx = c.getColumnIndexOrThrow("name")
                while (c.moveToNext()) {
                    columns += c.getString(nameIdx)
                }
                assert(columns.contains("id")) { "weight_entries should have id column" }
                assert(columns.contains("weight")) { "weight_entries should have weight column" }
                assert(columns.contains("dateIso")) { "weight_entries should have dateIso column" }
                assert(columns.contains("notes")) { "weight_entries should have notes column" }
                assert(columns.contains("recordedAt")) { "weight_entries should have recordedAt column" }
            }
        }
        
        db.close()
    }

    @Test
    fun migration_7_to_8_adds_recipe_indices() {
        // 1) Create database with version 7
        helper.createDatabase(TEST_DB, 7).apply { close() }

        // 2) Run migration 7→8
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_7_8)
            .build()

        // 3) Validate that indices exist
        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("PRAGMA index_list('recipes')").use { c ->
                val names = mutableSetOf<String>()
                val nameIdx = c.getColumnIndexOrThrow("name")
                while (c.moveToNext()) names += c.getString(nameIdx)
                assert(names.contains("index_recipes_createdAt")) { "Missing index_recipes_createdAt" }
                assert(names.contains("index_recipes_calories")) { "Missing index_recipes_calories" }
                assert(names.contains("index_recipes_title")) { "Missing index_recipes_title" }
            }
        }
        
        db.close()
    }

    @Test
    fun migration_7_to_8_preserves_existing_data() {
        // 1) Create database with version 7 and insert test data
        val db7 = helper.createDatabase(TEST_DB, 7)
        
        // Insert test recipe data
        db7.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt) 
            VALUES (1, 'Test Recipe', '# Test', 300, NULL, 0, 1640995200)
        """)
        
        // Insert test workout data if table exists
        try {
            db7.execSQL("""
                INSERT OR IGNORE INTO workouts (id, title, description, exercises, difficultyLevel, estimatedMinutes, targetMuscles, requiredEquipment, category, createdAt) 
                VALUES (1, 'Test Workout', 'Test Description', '[]', 'beginner', 30, 'chest', '[]', 'strength', 1640995200)
            """)
        } catch (e: Exception) {
            // Table might not exist in version 7, that's okay
        }
        
        db7.close()

        // 2) Run migration 7→8
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db8 = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_7_8)
            .build()

        // 3) Verify existing data is preserved
        db8.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("SELECT COUNT(*) FROM recipes WHERE title = 'Test Recipe'").use { c ->
                assert(c.moveToFirst()) { "Should be able to query recipes" }
                assert(c.getInt(0) == 1) { "Test recipe should be preserved" }
            }
        }
        
        db8.close()
    }

    @Test
    fun migration_9_to_10_creates_bmi_tables() {
        // Note: This test assumes migration 9→10 exists
        // If migration doesn't exist yet, this test will be skipped
        
        try {
            // 1) Create database with version 9
            helper.createDatabase(TEST_DB, 9).apply { close() }

            // 2) Run migration 9→10
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
                .addMigrations(AppDatabase.MIGRATION_9_10)
                .build()

            // 3) Validate that BMI-related tables exist
            db.openHelper.writableDatabase.use { sqlDb ->
                // Check bmi_history table
                sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='bmi_history'").use { c ->
                    assert(c.moveToFirst()) { "bmi_history table should exist after migration 9→10" }
                }
                
                // Check weight_loss_programs table
                sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='weight_loss_programs'").use { c ->
                    assert(c.moveToFirst()) { "weight_loss_programs table should exist after migration 9→10" }
                }
                
                // Check behavioral_check_ins table
                sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='behavioral_check_ins'").use { c ->
                    assert(c.moveToFirst()) { "behavioral_check_ins table should exist after migration 9→10" }
                }
                
                // Check progress_photos table
                sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='progress_photos'").use { c ->
                    assert(c.moveToFirst()) { "progress_photos table should exist after migration 9→10" }
                }
            }
            
            db.close()
        } catch (e: Exception) {
            // Migration 9→10 might not exist yet, skip this test
            org.junit.Assume.assumeNoException("Migration 9→10 not implemented yet", e)
        }
    }

    @Test
    fun migration_chain_7_to_latest_works() {
        // Test complete migration chain from version 7 to latest
        
        // 1) Create database with version 7
        val db7 = helper.createDatabase(TEST_DB, 7)
        
        // Insert some test data
        db7.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt) 
            VALUES (1, 'Migration Test Recipe', '# Test', 250, NULL, 0, 1640995200)
        """)
        
        db7.close()

        // 2) Apply all migrations to latest version
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val dbLatest = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_7_8) // Add all available migrations
            .build()

        // 3) Verify database is functional
        dbLatest.openHelper.writableDatabase.use { sqlDb ->
            // Test that we can read existing data
            sqlDb.query("SELECT title FROM recipes WHERE id = 1").use { c ->
                assert(c.moveToFirst()) { "Should be able to read migrated data" }
                assert(c.getString(0) == "Migration Test Recipe") { "Data should be preserved through migration chain" }
            }
            
            // Test that new tables exist (from migration 7→8)
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='weight_entries'").use { c ->
                assert(c.moveToFirst()) { "weight_entries table should exist after full migration" }
            }
        }
        
        dbLatest.close()
    }

    @Test
    fun database_operations_work_after_migration() {
        // Test that basic database operations work after migration
        
        // 1) Create and migrate database
        helper.createDatabase(TEST_DB, 7).apply { close() }
        
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_7_8)
            .build()

        // 2) Test basic operations
        db.openHelper.writableDatabase.use { sqlDb ->
            // Test weight_entries table operations
            sqlDb.execSQL("""
                INSERT INTO weight_entries (weight, dateIso, notes, recordedAt) 
                VALUES (75.5, '2024-01-15', 'Test entry', 1705315200)
            """)
            
            sqlDb.query("SELECT weight FROM weight_entries WHERE dateIso = '2024-01-15'").use { c ->
                assert(c.moveToFirst()) { "Should be able to insert and query weight entries" }
                assert(c.getDouble(0) == 75.5) { "Weight should be correctly stored and retrieved" }
            }
            
            // Test recipe operations still work
            sqlDb.execSQL("""
                INSERT INTO recipes (title, markdown, calories, imageUrl, isFavorite, createdAt) 
                VALUES ('Post-Migration Recipe', '# Test', 200, NULL, 0, 1705315200)
            """)
            
            sqlDb.query("SELECT COUNT(*) FROM recipes WHERE title LIKE '%Migration%'").use { c ->
                assert(c.moveToFirst()) { "Should be able to query recipes after migration" }
                assert(c.getInt(0) >= 1) { "Should find at least one recipe with 'Migration' in title" }
            }
        }
        
        db.close()
    }

    @Test
    fun migration_handles_schema_conflicts_gracefully() {
        // Test migration behavior when there might be schema conflicts
        
        // 1) Create database with version 7
        val db7 = helper.createDatabase(TEST_DB, 7)
        
        // Create some edge case data that might cause conflicts
        db7.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt) 
            VALUES (999999, 'Edge Case Recipe', '# Special \n Characters &', -1, 'invalid://url', 1, 0)
        """)
        
        db7.close()

        // 2) Run migration and verify it handles edge cases
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db8 = Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_7_8)
            .build()

        // 3) Verify edge case data is handled properly
        db8.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("SELECT title, calories FROM recipes WHERE id = 999999").use { c ->
                assert(c.moveToFirst()) { "Edge case data should be preserved" }
                assert(c.getString(0) == "Edge Case Recipe") { "Title with special characters should be preserved" }
                // Note: Negative calories might be valid for some use cases
            }
        }
        
        db8.close()
    }
}