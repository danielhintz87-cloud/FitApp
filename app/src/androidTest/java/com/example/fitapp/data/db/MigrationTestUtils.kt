package com.example.fitapp.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Utility functions for migration testing that provide comprehensive schema validation.
 * These utilities help ensure database migrations maintain data integrity and schema consistency.
 */
object MigrationTestUtils {
    /**
     * Validates that a table exists with the expected column schema.
     *
     * @param db The database to validate
     * @param tableName Name of the table to check
     * @param expectedColumns Map of column names to their expected SQL types
     * @param requiredColumns Set of column names that must be NOT NULL (optional)
     * @param primaryKeys Set of column names that should be part of primary key (optional)
     */
    fun validateTableSchema(
        db: SupportSQLiteDatabase,
        tableName: String,
        expectedColumns: Map<String, String>,
        requiredColumns: Set<String> = emptySet(),
        primaryKeys: Set<String> = emptySet(),
    ) {
        db.query("PRAGMA table_info($tableName)").use { cursor ->
            val actualColumns = mutableMapOf<String, ColumnInfo>()
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            val typeIndex = cursor.getColumnIndexOrThrow("type")
            val notnullIndex = cursor.getColumnIndexOrThrow("notnull")
            val pkIndex = cursor.getColumnIndexOrThrow("pk")

            while (cursor.moveToNext()) {
                val columnName = cursor.getString(nameIndex)
                val columnType = cursor.getString(typeIndex)
                val notNull = cursor.getInt(notnullIndex) == 1
                val primaryKey = cursor.getInt(pkIndex) > 0

                actualColumns[columnName] = ColumnInfo(columnType, notNull, primaryKey)
            }

            // Validate all expected columns exist with correct types
            expectedColumns.forEach { (columnName, expectedType) ->
                assertTrue(
                    actualColumns.containsKey(columnName),
                    "Table $tableName should have column $columnName",
                )
                assertEquals(
                    expectedType,
                    actualColumns[columnName]?.type,
                    "Column $tableName.$columnName should have type $expectedType",
                )
            }

            // Validate NOT NULL constraints
            requiredColumns.forEach { columnName ->
                assertTrue(
                    actualColumns[columnName]?.notNull == true,
                    "Column $tableName.$columnName should be NOT NULL",
                )
            }

            // Validate primary key constraints
            primaryKeys.forEach { columnName ->
                assertTrue(
                    actualColumns[columnName]?.primaryKey == true,
                    "Column $tableName.$columnName should be part of primary key",
                )
            }
        }
    }

    /**
     * Validates that an index exists in the database.
     *
     * @param db The database to check
     * @param indexName Name of the index to verify
     * @param tableName Optional table name to validate index is on correct table
     */
    fun validateIndexExists(
        db: SupportSQLiteDatabase,
        indexName: String,
        tableName: String? = null,
    ) {
        val query =
            if (tableName != null) {
                "SELECT name, tbl_name FROM sqlite_master WHERE type='index' AND name='$indexName' AND tbl_name='$tableName'"
            } else {
                "SELECT name FROM sqlite_master WHERE type='index' AND name='$indexName'"
            }

        db.query(query).use { cursor ->
            assertTrue(
                cursor.moveToFirst(),
                "Index $indexName${if (tableName != null) " on table $tableName" else ""} should exist",
            )
        }
    }

    /**
     * Validates that a table exists.
     *
     * @param db The database to check
     * @param tableName Name of the table to verify
     */
    fun validateTableExists(
        db: SupportSQLiteDatabase,
        tableName: String,
    ) {
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'").use { cursor ->
            assertTrue(cursor.moveToFirst(), "Table $tableName should exist")
        }
    }

    /**
     * Validates that a foreign key relationship exists.
     *
     * @param db The database to check
     * @param tableName Name of the table with the foreign key
     * @param columnName Column that should be a foreign key
     * @param referencedTable Table that should be referenced
     * @param referencedColumn Column in referenced table (optional, defaults to 'id')
     */
    fun validateForeignKey(
        db: SupportSQLiteDatabase,
        tableName: String,
        columnName: String,
        referencedTable: String,
        referencedColumn: String = "id",
    ) {
        db.query("PRAGMA foreign_key_list($tableName)").use { cursor ->
            var found = false
            val fromIndex = cursor.getColumnIndexOrThrow("from")
            val tableIndex = cursor.getColumnIndexOrThrow("table")
            val toIndex = cursor.getColumnIndexOrThrow("to")

            while (cursor.moveToNext()) {
                val from = cursor.getString(fromIndex)
                val table = cursor.getString(tableIndex)
                val to = cursor.getString(toIndex)

                if (from == columnName && table == referencedTable && to == referencedColumn) {
                    found = true
                    break
                }
            }

            assertTrue(
                found,
                "Foreign key $tableName.$columnName -> $referencedTable.$referencedColumn should exist",
            )
        }
    }

    /**
     * Validates that a table has expected row count.
     * Useful for verifying data preservation during migrations.
     *
     * @param db The database to check
     * @param tableName Name of the table
     * @param expectedCount Expected number of rows
     */
    fun validateRowCount(
        db: SupportSQLiteDatabase,
        tableName: String,
        expectedCount: Int,
    ) {
        db.query("SELECT COUNT(*) FROM $tableName").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(
                expectedCount,
                cursor.getInt(0),
                "Table $tableName should have $expectedCount rows",
            )
        }
    }

    /**
     * Validates that specific data exists in a table after migration.
     *
     * @param db The database to check
     * @param tableName Name of the table
     * @param whereClause SQL WHERE clause to find the data
     * @param expectedValues Map of column names to expected values
     */
    fun validateDataExists(
        db: SupportSQLiteDatabase,
        tableName: String,
        whereClause: String,
        expectedValues: Map<String, Any>,
    ) {
        val columns = expectedValues.keys.joinToString(", ")
        db.query("SELECT $columns FROM $tableName WHERE $whereClause").use { cursor ->
            assertTrue(
                cursor.moveToFirst(),
                "Expected data should exist in $tableName WHERE $whereClause",
            )

            expectedValues.forEach { (columnName, expectedValue) ->
                val columnIndex = cursor.getColumnIndex(columnName)
                assertTrue(columnIndex >= 0, "Column $columnName should exist in result")

                val actualValue =
                    when (expectedValue) {
                        is String -> cursor.getString(columnIndex)
                        is Int -> cursor.getInt(columnIndex)
                        is Long -> cursor.getLong(columnIndex)
                        is Double -> cursor.getDouble(columnIndex)
                        is Float -> cursor.getFloat(columnIndex)
                        else -> throw IllegalArgumentException("Unsupported value type: ${expectedValue::class}")
                    }

                assertEquals(
                    expectedValue,
                    actualValue,
                    "Column $tableName.$columnName should have value $expectedValue",
                )
            }
        }
    }

    /**
     * Validates database version matches expected version.
     *
     * @param db The database to check
     * @param expectedVersion Expected database version
     */
    fun validateDatabaseVersion(
        db: SupportSQLiteDatabase,
        expectedVersion: Int,
    ) {
        assertEquals(
            expectedVersion,
            db.version,
            "Database version should be $expectedVersion",
        )
    }

    /**
     * Gets all table names from the database.
     * Useful for debugging and comprehensive validation.
     *
     * @param db The database to query
     * @return Set of all table names (excluding sqlite_* system tables)
     */
    fun getAllTableNames(db: SupportSQLiteDatabase): Set<String> {
        val tables = mutableSetOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(nameIndex))
            }
        }
        return tables
    }

    /**
     * Gets all index names from the database.
     *
     * @param db The database to query
     * @param tableName Optional table name to filter indices for specific table
     * @return Set of all index names
     */
    fun getAllIndexNames(
        db: SupportSQLiteDatabase,
        tableName: String? = null,
    ): Set<String> {
        val indices = mutableSetOf<String>()
        val query =
            if (tableName != null) {
                "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='$tableName'"
            } else {
                "SELECT name FROM sqlite_master WHERE type='index'"
            }

        db.query(query).use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                val indexName = cursor.getString(nameIndex)
                // Filter out auto-generated indices
                if (!indexName.startsWith("sqlite_autoindex_")) {
                    indices.add(indexName)
                }
            }
        }
        return indices
    }

    /**
     * Data class to hold column information.
     */
    private data class ColumnInfo(
        val type: String,
        val notNull: Boolean,
        val primaryKey: Boolean,
    )
}
