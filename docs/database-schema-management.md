# Room Database Schema Management

This document describes the schema export workflow and migration practices for the FitApp Room database.

## Schema Export Configuration

Room schema export is enabled in `app/build.gradle.kts`:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}
```

And in the database class:

```kotlin
@Database(
    entities = [...],
    version = 17,
    exportSchema = true  // This enables schema export
)
abstract class AppDatabase : RoomDatabase() { ... }
```

## Schema Files Location

Schema JSON files are automatically generated in:
```
app/schemas/com.example.fitapp.data.db.AppDatabase/
├── 8.json    # Schema version 8
├── 9.json    # Schema version 9
├── ...
└── 17.json   # Current latest version
```

**Important**: These schema files are committed to version control and should never be manually edited.

## Migration Development Workflow

### 1. Making Entity Changes

When you modify Room entities:

1. **Update Entity Classes**: Modify your `@Entity` classes in `Entities.kt`
2. **Increment Database Version**: Update the `version` in `@Database` annotation
3. **Clean Build**: Run `./gradlew clean` to ensure schemas are regenerated
4. **Build Project**: Run `./gradlew assembleDebug` to generate new schema

### 2. Creating Migrations

After schema changes, create a new migration:

```kotlin
val MIGRATION_N_N+1 = object : Migration(N, N+1) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Use idempotent DDL
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `new_table` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `column1` TEXT NOT NULL
            )
        """)
        
        // For adding columns, use the utility function
        addColumnIfNotExists(db, "existing_table", "new_column", "TEXT")
        
        // Create indices idempotently  
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_name` ON `table` (`column`)")
    }
}
```

### 3. Migration Best Practices

#### Always Use Idempotent DDL
- `CREATE TABLE IF NOT EXISTS` for new tables
- `CREATE INDEX IF NOT EXISTS` for new indices
- Use `addColumnIfNotExists()` utility for adding columns
- Avoid `DROP` statements when possible

#### Data Preservation
- Use `CREATE TABLE ... AS SELECT` patterns for table restructuring
- Always copy existing data when changing table structure
- Test data migration with real data scenarios

#### Migration Examples

**Adding a New Table:**
```kotlin
db.execSQL("""
    CREATE TABLE IF NOT EXISTS `new_feature` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `name` TEXT NOT NULL,
        `created_at` INTEGER NOT NULL
    )
""")
```

**Adding Columns:**
```kotlin
addColumnIfNotExists(db, "users", "email", "TEXT")
addColumnIfNotExists(db, "users", "verified", "INTEGER NOT NULL DEFAULT 0")
```

**Restructuring Tables:**
```kotlin
// Create new table with updated schema
db.execSQL("""
    CREATE TABLE IF NOT EXISTS `table_new` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `name` TEXT NOT NULL,
        `new_field` TEXT
    )
""")

// Copy data from old table
db.execSQL("""
    INSERT INTO table_new (id, name, new_field)
    SELECT id, name, NULL FROM table_old
""")

// Replace old table
db.execSQL("DROP TABLE table_old")
db.execSQL("ALTER TABLE table_new RENAME TO table_old")
```

### 4. Testing Migrations

#### Single Migration Tests
Test each migration individually:

```kotlin
@Test
fun migration_N_to_N+1_works() {
    val dbN = helper.createDatabase(TEST_DB, N)
    // Insert test data
    dbN.close()
    
    val dbN1 = helper.runMigrationsAndValidate(
        TEST_DB, N+1, true, AppDatabase.MIGRATION_N_N+1
    )
    // Validate schema and data
    dbN1.close()
}
```

#### Full Migration Chain Tests
Test complete migration paths:

```kotlin
@Test
fun full_migration_from_earliest_to_latest() {
    val dbEarliest = helper.createDatabase(TEST_DB, 5)
    // Insert comprehensive test data
    dbEarliest.close()
    
    val dbLatest = helper.runMigrationsAndValidate(
        TEST_DB, 17, true,
        AppDatabase.MIGRATION_5_6,
        AppDatabase.MIGRATION_6_7,
        // ... all migrations
        AppDatabase.MIGRATION_16_17
    )
    // Validate final state
    dbLatest.close()
}
```

## Schema Validation

### Automated Validation
The exported schema files enable Room to automatically validate that:
- Entity definitions match the database schema
- Migrations correctly transform schemas
- No schema drift occurs between versions

### Manual Validation
Use `ComprehensiveMigrationTest` to validate:
- Table structure matches entity definitions
- Required indices exist
- Data integrity through migrations
- Migration determinism (no flakiness)

## Troubleshooting

### Schema Mismatch Errors
If you see "Migration didn't properly handle" errors:

1. **Check Entity Changes**: Ensure all entity modifications are reflected in migrations
2. **Validate Schema Files**: Compare generated schema JSON with your entities
3. **Test Migration**: Run migration tests to identify specific issues
4. **Check Indices**: Ensure all `@Index` annotations have corresponding CREATE INDEX statements

### Migration Failures
If migrations fail:

1. **Check DDL Syntax**: Validate SQL statements in migrations
2. **Test with Real Data**: Use realistic test data that might expose edge cases
3. **Check Dependencies**: Ensure foreign key relationships are maintained
4. **Validate Idempotency**: Test running migrations multiple times

### Performance Issues
If migrations are slow:

1. **Batch Operations**: Group related DDL statements
2. **Disable Foreign Keys**: Temporarily disable during large data moves
3. **Use Transactions**: Wrap migration logic in transactions
4. **Index Timing**: Create indices after data insertion for large tables

## Continuous Integration

Ensure CI pipelines:
- Run migration tests on every PR
- Validate schema files are committed
- Test against multiple Android API levels
- Run full migration chains, not just single steps

## Schema History Management

### Version Control
- Always commit schema JSON files
- Never manually edit schema files
- Tag releases with corresponding schema versions
- Document breaking changes in migration comments

### Rollback Strategy
- Maintain downward migration capability where possible
- Test rollback scenarios in development
- Document data loss implications of rollbacks
- Consider feature flags for reversible changes

## Production Considerations

### Deployment
- Test migrations on production-like datasets
- Monitor migration performance in production
- Have rollback plans for failed migrations
- Consider staged rollouts for major schema changes

### Monitoring
- Log migration execution times
- Monitor database size changes
- Track migration success/failure rates
- Alert on unexpected schema drift