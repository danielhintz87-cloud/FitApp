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

@RunWith(AndroidJUnit4::class)
class Migration7to8Test {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migration_7_to_8_adds_recipe_indices() {
        // 1) Create database with version 7
        helper.createDatabase(TEST_DB, 7).apply { close() }

        // 2) Run migration 7→8
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db =
            Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
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
    fun migration_7_to_8_creates_weight_entries_table() {
        // 1) Create database with version 7
        helper.createDatabase(TEST_DB, 7).apply { close() }

        // 2) Run migration 7→8
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db =
            Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
                .addMigrations(AppDatabase.MIGRATION_7_8)
                .build()

        // 3) Validate that weight_entries table exists
        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='weight_entries'").use { c ->
                assert(c.moveToFirst()) { "weight_entries table should exist after migration" }
            }
        }

        db.close()
    }
}
