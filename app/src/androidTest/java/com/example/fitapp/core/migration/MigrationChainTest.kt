package com.example.fitapp.core.migration

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
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
        }
        
        db.close()
    }
}