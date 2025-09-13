package com.example.fitapp.data.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class Migration18To19Test {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun `migrate18To19 should create sync_operations table`() {
        // Given: Database at version 18
        val db = helper.createDatabase(TEST_DB, 18)
        db.close()

        // When: Migrate to version 19
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 19, true, AppDatabase.MIGRATION_18_19)

        // Then: Verify sync_operations table exists with correct schema
        val cursor = migratedDb.query("PRAGMA table_info(sync_operations)")
        val columns = mutableSetOf<String>()
        
        cursor.use {
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(nameIndex))
            }
        }

        // Verify all required columns exist
        assert(columns.contains("id"))
        assert(columns.contains("operationType"))
        assert(columns.contains("operationData"))
        assert(columns.contains("timestamp"))
        assert(columns.contains("retryCount"))
        assert(columns.contains("maxRetries"))
        assert(columns.contains("status"))
        assert(columns.contains("priority"))
        assert(columns.contains("errorMessage"))
        assert(columns.contains("lastAttemptAt"))
        assert(columns.contains("nextRetryAt"))
        assert(columns.contains("createdAt"))
        assert(columns.contains("completedAt"))

        migratedDb.close()
    }

    @Test
    @Throws(IOException::class)
    fun `migrate18To19 should create proper indices for sync_operations`() {
        // Given: Database at version 18
        val db = helper.createDatabase(TEST_DB, 18)
        db.close()

        // When: Migrate to version 19
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 19, true, AppDatabase.MIGRATION_18_19)

        // Then: Verify indices exist
        val cursor = migratedDb.query("PRAGMA index_list(sync_operations)")
        val indices = mutableSetOf<String>()
        
        cursor.use {
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                indices.add(cursor.getString(nameIndex))
            }
        }

        // Verify required indices exist
        assert(indices.any { it.contains("status_priority_timestamp") })
        assert(indices.any { it.contains("operationType") })
        assert(indices.any { it.contains("retryCount") })
        assert(indices.any { it.contains("createdAt") })

        migratedDb.close()
    }

    @Test
    @Throws(IOException::class)
    fun `sync_operations table should accept valid operation data`() {
        // Given: Database migrated to version 19
        var db = helper.createDatabase(TEST_DB, 18)
        db.close()
        db = helper.runMigrationsAndValidate(TEST_DB, 19, true, AppDatabase.MIGRATION_18_19)

        // When: Insert test sync operation
        val currentTime = System.currentTimeMillis() / 1000
        db.execSQL("""
            INSERT INTO sync_operations 
            (id, operationType, operationData, timestamp, retryCount, maxRetries, status, priority, createdAt)
            VALUES 
            ('test-op-1', 'NUTRITION_ENTRY', 'mealId=123,calories=500', $currentTime, 0, 3, 'pending', 0, $currentTime)
        """)

        // Then: Verify operation was inserted successfully
        val cursor = db.query("SELECT * FROM sync_operations WHERE id = 'test-op-1'")
        cursor.use {
            assert(cursor.moveToFirst())
            assert(cursor.getString(cursor.getColumnIndexOrThrow("operationType")) == "NUTRITION_ENTRY")
            assert(cursor.getString(cursor.getColumnIndexOrThrow("status")) == "pending")
            assert(cursor.getInt(cursor.getColumnIndexOrThrow("retryCount")) == 0)
        }

        db.close()
    }
}