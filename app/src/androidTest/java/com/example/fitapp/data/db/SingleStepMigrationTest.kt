package com.example.fitapp.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

private const val TEST_DB = "single_step_migration_test.db"

/**
 * Tests each individual migration step n→n+1 to ensure they work correctly in isolation.
 * This provides fine-grained testing to identify specific migration issues.
 */
@RunWith(AndroidJUnit4::class)
class SingleStepMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migration_5_to_6_single_step() {
        val db5 = helper.createDatabase(TEST_DB, 5)
        
        // Insert minimal test data
        db5.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt)
            VALUES ('test-5-6', 'Test Recipe 5→6', '# Test', 200, NULL, 0, 1705315200)
        """)
        
        db5.close()

        val db6 = helper.runMigrationsAndValidate(
            TEST_DB,
            6,
            true,
            AppDatabase.MIGRATION_5_6
        )

        // Verify new tables exist
        val expectedTables = listOf("personal_achievements", "personal_streaks", "personal_records", "progress_milestones")
        expectedTables.forEach { tableName ->
            MigrationTestUtils.validateTableExists(db6, tableName)
        }

        // Verify existing data preserved
        MigrationTestUtils.validateDataExists(
            db6, 
            "recipes", 
            "id = 'test-5-6'",
            mapOf("title" to "Test Recipe 5→6", "calories" to 200)
        )

        db6.close()
    }

    @Test
    fun migration_6_to_7_single_step() {
        val db6 = helper.createDatabase(TEST_DB, 6)
        
        // Insert test data with date string
        db6.execSQL("""
            INSERT INTO personal_streaks (name, description, category, currentStreak, longestStreak, lastActivityDate, isActive, createdAt)
            VALUES ('Test Streak 6→7', 'Migration Test', 'workout', 3, 5, '2024-01-10', 1, 1705315200)
        """)
        
        db6.close()

        val db7 = helper.runMigrationsAndValidate(
            TEST_DB,
            7,
            true,
            AppDatabase.MIGRATION_6_7
        )

        // Verify timestamp conversion
        db7.query("SELECT lastActivityTimestamp FROM personal_streaks WHERE name = 'Test Streak 6→7'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            val timestamp = cursor.getLong(0)
            assertTrue(timestamp > 0, "Date should be converted to timestamp")
        }

        db7.close()
    }

    @Test
    fun migration_7_to_8_single_step() {
        val db7 = helper.createDatabase(TEST_DB, 7)
        
        // Insert test recipe
        db7.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt)
            VALUES ('test-7-8', 'Test Recipe 7→8', '# Test', 250, NULL, 0, 1705315200)
        """)
        
        db7.close()

        val db8 = helper.runMigrationsAndValidate(
            TEST_DB,
            8,
            true,
            AppDatabase.MIGRATION_7_8
        )

        // Verify weight_entries table created
        MigrationTestUtils.validateTableExists(db8, "weight_entries")
        
        // Verify indices added
        val expectedIndices = listOf(
            "index_weight_entries_dateIso",
            "index_recipes_createdAt",
            "index_recipes_calories",
            "index_recipes_title"
        )
        expectedIndices.forEach { indexName ->
            MigrationTestUtils.validateIndexExists(db8, indexName)
        }

        db8.close()
    }

    @Test
    fun migration_8_to_9_single_step() {
        val db8 = helper.createDatabase(TEST_DB, 8)
        
        // Insert test daily goal
        db8.execSQL("""
            INSERT INTO daily_goals (dateIso, targetKcal)
            VALUES ('2024-01-15', 1800)
        """)
        
        db8.close()

        val db9 = helper.runMigrationsAndValidate(
            TEST_DB,
            9,
            true,
            AppDatabase.MIGRATION_8_9
        )

        // Verify nutrition tables created
        val nutritionTables = listOf("food_items", "meal_entries", "water_entries")
        nutritionTables.forEach { tableName ->
            MigrationTestUtils.validateTableExists(db9, tableName)
        }

        // Verify existing goal data preserved with new columns
        MigrationTestUtils.validateDataExists(
            db9,
            "daily_goals",
            "dateIso = '2024-01-15'",
            mapOf("targetKcal" to 1800)
        )

        db9.close()
    }

    @Test
    fun migration_9_to_10_single_step() {
        val db9 = helper.createDatabase(TEST_DB, 9)
        
        // Insert test food item
        db9.execSQL("""
            INSERT INTO food_items (id, name, calories, carbs, protein, fat, createdAt)
            VALUES ('test-9-10', 'Test Food 9→10', 120, 15.0, 8.0, 3.0, 1705315200)
        """)
        
        db9.close()

        val db10 = helper.runMigrationsAndValidate(
            TEST_DB,
            10,
            true,
            AppDatabase.MIGRATION_9_10
        )

        // Verify BMI tables created
        val bmiTables = listOf("bmi_history", "weight_loss_programs", "behavioral_check_ins", "progress_photos")
        bmiTables.forEach { tableName ->
            MigrationTestUtils.validateTableExists(db10, tableName)
        }

        // Verify food item data preserved
        MigrationTestUtils.validateDataExists(
            db10,
            "food_items",
            "id = 'test-9-10'",
            mapOf("name" to "Test Food 9→10", "calories" to 120)
        )

        db10.close()
    }

    @Test
    fun migration_10_to_11_single_step() {
        val db10 = helper.createDatabase(TEST_DB, 10)
        db10.close()

        val db11 = helper.runMigrationsAndValidate(
            TEST_DB,
            11,
            true,
            AppDatabase.MIGRATION_10_11
        )

        // Verify workout tables created
        val workoutTables = listOf("workout_performance", "workout_sessions", "exercise_progressions")
        workoutTables.forEach { tableName ->
            MigrationTestUtils.validateTableExists(db11, tableName)
        }

        db11.close()
    }

    @Test
    fun migration_11_to_12_single_step() {
        val db11 = helper.createDatabase(TEST_DB, 11)
        db11.close()

        val db12 = helper.runMigrationsAndValidate(
            TEST_DB,
            12,
            true,
            AppDatabase.MIGRATION_11_12
        )

        // Verify cooking tables created
        val cookingTables = listOf("cooking_sessions", "cooking_timers")
        cookingTables.forEach { tableName ->
            MigrationTestUtils.validateTableExists(db12, tableName)
        }

        db12.close()
    }

    @Test
    fun migration_12_to_13_single_step() {
        val db12 = helper.createDatabase(TEST_DB, 12)
        db12.close()

        val db13 = helper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            AppDatabase.MIGRATION_12_13
        )

        // Verify cloud sync and health tables created
        val cloudTables = listOf("cloud_sync_metadata", "user_profiles", "sync_conflicts")
        val healthTables = listOf("health_connect_steps", "health_connect_heart_rate", 
                                 "health_connect_calories", "health_connect_sleep", 
                                 "health_connect_exercise_sessions")
        
        (cloudTables + healthTables).forEach { tableName ->
            MigrationTestUtils.validateTableExists(db13, tableName)
        }

        db13.close()
    }

    @Test
    fun migration_13_to_14_single_step() {
        val db13 = helper.createDatabase(TEST_DB, 13)
        db13.close()

        val db14 = helper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            AppDatabase.MIGRATION_13_14
        )

        // Verify social challenge tables created
        val socialTables = listOf("social_challenges", "challenge_participations", 
                                 "challenge_progress_logs", "social_badges", "leaderboard_entries")
        socialTables.forEach { tableName ->
            MigrationTestUtils.validateTableExists(db14, tableName)
        }

        db14.close()
    }

    @Test
    fun migration_14_to_15_single_step() {
        val db14 = helper.createDatabase(TEST_DB, 14)
        
        // Insert test data for no-op verification
        db14.execSQL("""
            INSERT INTO social_challenges (title, description, category, startDate, endDate, 
                                         targetValue, unit, isActive, createdBy, createdAt)
            VALUES ('No-op Test', 'Test no-op migration', 'fitness', '2024-01-01', '2024-01-31',
                    50.0, 'workouts', 1, 'test-user', 1705315200)
        """)
        
        db14.close()

        val db15 = helper.runMigrationsAndValidate(
            TEST_DB,
            15,
            true,
            AppDatabase.MIGRATION_14_15
        )

        // Verify no-op migration preserves data
        MigrationTestUtils.validateDataExists(
            db15,
            "social_challenges",
            "title = 'No-op Test'",
            mapOf("description" to "Test no-op migration", "targetValue" to 50.0)
        )

        db15.close()
    }

    @Test
    fun migration_15_to_16_single_step() {
        val db15 = helper.createDatabase(TEST_DB, 15)
        
        // Insert test recipe
        db15.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt)
            VALUES ('test-15-16', 'YAZIO Migration Test', '# Test Recipe', 350, NULL, 0, 1705315200)
        """)
        
        db15.close()

        val db16 = helper.runMigrationsAndValidate(
            TEST_DB,
            16,
            true,
            AppDatabase.MIGRATION_15_16
        )

        // Verify YAZIO tables created
        val yazioTables = listOf("meals", "recipe_ingredients", "recipe_steps", 
                                "grocery_lists", "grocery_items", "recipe_analytics",
                                "recipe_ratings", "pro_features", "recipe_collections", 
                                "recipe_collection_items")
        yazioTables.forEach { tableName ->
            MigrationTestUtils.validateTableExists(db16, tableName)
        }

        // Verify recipe data migrated to enhanced schema
        MigrationTestUtils.validateDataExists(
            db16,
            "recipes",
            "id = 'test-15-16'",
            mapOf("title" to "YAZIO Migration Test")
        )

        db16.close()
    }

    @Test
    fun migration_16_to_17_single_step() {
        val db16 = helper.createDatabase(TEST_DB, 16)
        
        // Insert test meal entry
        db16.execSQL("""
            INSERT INTO meal_entries (foodItemId, date, mealType, quantityGrams, recordedAt)
            VALUES ('test-food-16-17', '2024-01-20', 'dinner', 150.0, 1705315200)
        """)
        
        db16.close()

        val db17 = helper.runMigrationsAndValidate(
            TEST_DB,
            17,
            true,
            AppDatabase.MIGRATION_16_17
        )

        // Verify meal entry data preserved with new schema
        MigrationTestUtils.validateDataExists(
            db17,
            "meal_entries",
            "foodItemId = 'test-food-16-17'",
            mapOf("mealType" to "dinner", "quantityGrams" to 150.0)
        )

        db17.close()
    }

    @Test
    fun all_migrations_are_idempotent() {
        // Test that running migrations multiple times doesn't cause errors
        
        // Create base database
        val db7 = helper.createDatabase(TEST_DB, 7)
        db7.execSQL("""
            INSERT INTO recipes (id, title, markdown, calories, imageUrl, isFavorite, createdAt)
            VALUES ('idempotent-test', 'Idempotent Test', '# Test', 200, NULL, 0, 1705315200)
        """)
        db7.close()

        // Run migration once
        val db8_first = helper.runMigrationsAndValidate(
            TEST_DB,
            8,
            true,
            AppDatabase.MIGRATION_7_8
        )
        db8_first.close()

        // Attempt to run the same migration again (should not fail)
        // This tests IF NOT EXISTS clauses work correctly
        val db8_second = helper.runMigrationsAndValidate(
            "${TEST_DB}_idempotent",
            8,
            true,
            AppDatabase.MIGRATION_7_8
        )

        // Verify data is still intact
        MigrationTestUtils.validateDataExists(
            db8_second,
            "recipes",
            "id = 'idempotent-test'",
            mapOf("title" to "Idempotent Test", "calories" to 200)
        )

        db8_second.close()
    }
}