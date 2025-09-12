# Room Database Migrations Guide

This guide covers how to handle Room database migrations, schema exports, and testing in the FitApp project.

## Overview

FitApp uses Room with **exportSchema = true** to maintain a complete migration chain without destructive migrations. The database is currently at version **17** and includes comprehensive entities for fitness, nutrition, and health tracking.

## Schema Export Configuration

### Build Configuration

Schema export is enabled in `app/build.gradle.kts`:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}
```

### Schema Files Location

- **Directory**: `app/schemas/`
- **Files**: `com.example.fitapp.data.db.AppDatabase/<version>.json`
- **Purpose**: JSON schema files for each database version to enable migration testing

## Database Setup

### Current Configuration

```kotlin
@Database(
    entities = [/* 70+ entities */],
    version = 17,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase()
```

Key principles:
- ✅ **exportSchema = true** - Always enabled
- ✅ **Additive migrations only** - No destructive changes
- ✅ **Continuous migration chain** - Every version 5→6→7...→17 covered
- ❌ **fallbackToDestructiveMigration()** - Only in DEBUG builds

## Writing a Migration

### Migration Template

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create new tables
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `new_table` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `field` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL
            )
        """.trimIndent())
        
        // 2. Add new columns to existing tables
        db.execSQL("ALTER TABLE existing_table ADD COLUMN new_field TEXT")
        
        // 3. Create indices for performance
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_table_field` ON `new_table` (`field`)")
        
        // 4. Migrate existing data (if needed)
        db.execSQL("""
            INSERT INTO new_table (field, created_at) 
            SELECT converted_field, timestamp FROM old_table
        """.trimIndent())
    }
}
```

### Migration Checklist

- [ ] **Additive only**: No DROP TABLE, DROP COLUMN, or destructive changes
- [ ] **SQL syntax**: Use proper SQLite syntax with IF NOT EXISTS
- [ ] **Data preservation**: Migrate existing data when restructuring
- [ ] **Indices**: Add performance indices for new tables/columns
- [ ] **Testing**: Write migration test (see below)
- [ ] **Registration**: Add to `addMigrations()` in buildDatabase()

### Real Example: Migration 16→17

```kotlin
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add recipe support to meal_entries for recipe-to-diary functionality
        
        // Create new table with additional fields
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `meal_entries_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `foodItemId` TEXT,
                `recipeId` TEXT,
                `date` TEXT NOT NULL,
                `mealType` TEXT NOT NULL,
                `quantityGrams` REAL NOT NULL,
                `servings` REAL,
                `notes` TEXT
            )
        """)
        
        // Copy existing data 
        db.execSQL("""
            INSERT INTO meal_entries_new (id, foodItemId, recipeId, date, mealType, quantityGrams, servings, notes)
            SELECT id, foodItemId, NULL, date, mealType, quantityGrams, NULL, notes
            FROM meal_entries
        """)
        
        // Replace old table
        db.execSQL("DROP TABLE meal_entries")
        db.execSQL("ALTER TABLE meal_entries_new RENAME TO meal_entries")
        
        // Recreate indices
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_foodItemId` ON `meal_entries` (`foodItemId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_date` ON `meal_entries` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_mealType` ON `meal_entries` (`mealType`)")
    }
}
```

## Testing Migrations

### Migration Test Template

Create migration tests in `app/src/androidTest/java/`:

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTestX_Y {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate_X_to_Y() {
        // Create database with version X
        var db = helper.createDatabase(TEST_DB, X).apply {
            // Insert test data for version X
            execSQL("INSERT INTO old_table (field) VALUES ('test')")
            close()
        }

        // Re-open database with version Y and provide migration
        db = helper.runMigrationsAndValidate(TEST_DB, Y, true, MIGRATION_X_Y)

        // Verify migration success
        val cursor = db.query("SELECT * FROM new_table")
        assertThat(cursor.count, `is`(1))
        cursor.close()
    }
}
```

### Running Migration Tests

```bash
# Run all migration tests
./gradlew connectedAndroidTest --tests="*.MigrationTest*"

# Run specific migration test
./gradlew connectedAndroidTest --tests="*.MigrationTest16_17"
```

## Common Migration Patterns

### Adding a New Table

```kotlin
db.execSQL("""
    CREATE TABLE IF NOT EXISTS `new_feature_table` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `user_id` TEXT NOT NULL,
        `data` TEXT NOT NULL,
        `created_at` INTEGER NOT NULL
    )
""".trimIndent())

// Always add indices
db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_feature_table_user_id` ON `new_feature_table` (`user_id`)")
```

### Adding Columns to Existing Table

```kotlin
// Add nullable columns
db.execSQL("ALTER TABLE recipes ADD COLUMN rating REAL")
db.execSQL("ALTER TABLE recipes ADD COLUMN is_premium INTEGER NOT NULL DEFAULT 0")

// Add index if the column will be queried
db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_rating` ON `recipes` (`rating`)")
```

### Restructuring Table (Copy Pattern)

```kotlin
// 1. Create new table with desired structure
db.execSQL("CREATE TABLE recipes_new (...)")

// 2. Copy data with transformation
db.execSQL("""
    INSERT INTO recipes_new (id, title, new_field)
    SELECT id, title, COALESCE(old_field, default_value) 
    FROM recipes
""")

// 3. Replace old table
db.execSQL("DROP TABLE recipes")
db.execSQL("ALTER TABLE recipes_new RENAME TO recipes")

// 4. Recreate indices
db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_title` ON `recipes` (`title`)")
```

## Best Practices

### DO ✅

- Always use `CREATE TABLE IF NOT EXISTS`
- Write migration tests for every schema change
- Preserve existing data during restructuring
- Add appropriate indices for new fields
- Use `trimIndent()` for multiline SQL strings
- Log migration success/failure
- Keep migrations small and focused

### DON'T ❌

- Use `fallbackToDestructiveMigration()` in production
- Drop tables or columns (data loss)
- Skip version numbers in migration chain
- Forget to update entity classes to match schema
- Ignore SQL syntax errors (test migrations!)
- Create migrations without indices

### Performance Tips

- Create indices for columns used in WHERE, ORDER BY, GROUP BY
- Use `COALESCE()` for handling nulls during data migration
- Batch large data migrations to avoid timeouts
- Use transactions implicitly (Room handles this)

## Troubleshooting

### Schema Mismatch Error

```
Expected schema hash: abc123
Found schema hash: def456
```

**Solution**: Update entity annotations to match actual migration, then rebuild.

### Migration Test Failure

```
IllegalStateException: Migration didn't properly handle: table_name(expected_column)
```

**Solution**: Ensure migration SQL exactly matches entity structure and indices.

### Slow Migration Performance

- Add indices during migration, not after
- Use efficient SQL patterns (avoid N+1 queries)
- Consider batching large data transformations

## Current Migration Chain

```
Version 5 → 6: Personal achievements, streaks, records, milestones
Version 6 → 7: Streak timestamp migration (TEXT → INTEGER)
Version 7 → 8: Weight tracking, recipe indices
Version 8 → 9: Daily goals macros, food items, meal/water entries
Version 9 → 10: OpenFoodFacts integration, BMI, weight loss programs
Version 10 → 11: Workout performance, sessions, exercise progressions
Version 11 → 12: Cooking sessions and timers
Version 12 → 13: Cloud sync, Health Connect integration
Version 13 → 14: Social challenges and badges system
Version 14 → 15: No-op migration (placeholder)
Version 15 → 16: YAZIO-style recipes and meal management
Version 16 → 17: Recipe support in meal entries
```

---

**Next Steps**: When adding new features, follow this guide to ensure proper database evolution without data loss.