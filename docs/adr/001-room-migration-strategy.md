# ADR-001: Room Database Migration Strategy

**Date**: 2024-01-11  
**Status**: Accepted  
**Decision Makers**: Development Team

## Context

FitApp requires a robust database schema evolution strategy to support:
- Complex fitness and nutrition data models (70+ entities)
- Continuous feature development and schema changes
- Data preservation across app updates
- Testing and validation of database migrations

## Decision

We adopt **additive-only Room migrations** with mandatory schema export:

1. **No destructive migrations** - Never use `fallbackToDestructiveMigration()` in production
2. **Continuous migration chain** - Every version 5→6→7...→17 must be covered
3. **Schema export enabled** - `exportSchema = true` with `schemaLocation = "$projectDir/schemas"`
4. **Migration testing required** - Every schema change must include migration tests

## Consequences

### Positive
- ✅ Zero data loss during app updates
- ✅ Testable migration paths with exported schemas
- ✅ Clear audit trail of database evolution
- ✅ Confident deployment of schema changes

### Negative
- ⚠️ Larger APK size due to schema files
- ⚠️ More complex migration logic for restructuring
- ⚠️ Additional development time for migration tests

## Implementation

```kotlin
@Database(
    entities = [/* all entities */],
    version = 17,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Additive changes only
            }
        }
    }
}
```

## Alternatives Considered

- **Destructive migration**: Rejected due to data loss risk
- **Manual schema management**: Rejected due to complexity and error-proneness
- **NoSQL approach**: Rejected due to existing Room investment and complex queries needed

## References

- [Room Migration Documentation](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Schema Export Guide](https://developer.android.com/training/data-storage/room/migrating-db-versions#export-schema)