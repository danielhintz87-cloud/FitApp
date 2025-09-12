package com.example.fitapp.data.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "fitapp_migration_11_to_12_test.db"

@RunWith(AndroidJUnit4::class)
class Migration11to12Test {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migration_11_to_12_creates_cooking_tables() {
        // 1) Create database with version 11
        helper.createDatabase(TEST_DB, 11).apply { close() }

        // 2) Run migration 11→12
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val db =
            Room.databaseBuilder(ctx, AppDatabase::class.java, TEST_DB)
                .addMigrations(AppDatabase.MIGRATION_11_12)
                .build()

        // 3) Validate that cooking_sessions table exists
        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='cooking_sessions'").use { c ->
                assert(c.moveToFirst()) { "cooking_sessions table should exist after migration" }
            }
        }

        // 4) Validate that cooking_timers table exists
        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='cooking_timers'").use { c ->
                assert(c.moveToFirst()) { "cooking_timers table should exist after migration" }
            }
        }

        // 5) Validate foreign key relationship exists
        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("PRAGMA foreign_key_list(cooking_timers)").use { c ->
                var foundFK = false
                val tableIdx = c.getColumnIndexOrThrow("table")
                val fromIdx = c.getColumnIndexOrThrow("from")
                while (c.moveToNext()) {
                    if (c.getString(tableIdx) == "cooking_sessions" && c.getString(fromIdx) == "sessionId") {
                        foundFK = true
                        break
                    }
                }
                assert(foundFK) { "cooking_timers should have foreign key to cooking_sessions" }
            }
        }

        // 6) Validate indices were created
        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("PRAGMA index_list('cooking_sessions')").use { c ->
                val names = mutableSetOf<String>()
                val nameIdx = c.getColumnIndexOrThrow("name")
                while (c.moveToNext()) names += c.getString(nameIdx)
                assert(names.contains("index_cooking_sessions_recipeId")) { "Missing index_cooking_sessions_recipeId" }
                assert(
                    names.contains("index_cooking_sessions_startTime"),
                ) { "Missing index_cooking_sessions_startTime" }
                assert(names.contains("index_cooking_sessions_status")) { "Missing index_cooking_sessions_status" }
            }
        }

        db.openHelper.writableDatabase.use { sqlDb ->
            sqlDb.query("PRAGMA index_list('cooking_timers')").use { c ->
                val names = mutableSetOf<String>()
                val nameIdx = c.getColumnIndexOrThrow("name")
                while (c.moveToNext()) names += c.getString(nameIdx)
                assert(names.contains("index_cooking_timers_sessionId")) { "Missing index_cooking_timers_sessionId" }
                assert(names.contains("index_cooking_timers_stepIndex")) { "Missing index_cooking_timers_stepIndex" }
                assert(names.contains("index_cooking_timers_isActive")) { "Missing index_cooking_timers_isActive" }
            }
        }

        db.close()
    }
}
