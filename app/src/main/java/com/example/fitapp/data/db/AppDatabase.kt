package com.example.fitapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AiLog::class,
        RecipeEntity::class,
        RecipeFavoriteEntity::class,
        RecipeHistoryEntity::class,
        IntakeEntryEntity::class,
        DailyGoalEntity::class,
        ShoppingItemEntity::class,
        PlanEntity::class,
        SavedRecipeEntity::class,
        ShoppingCategoryEntity::class,
        TodayWorkoutEntity::class,
        PersonalAchievementEntity::class,
        PersonalStreakEntity::class,
        PersonalRecordEntity::class,
        ProgressMilestoneEntity::class,
        WeightEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aiLogDao(): AiLogDao
    abstract fun recipeDao(): RecipeDao
    abstract fun intakeDao(): IntakeDao
    abstract fun goalDao(): GoalDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun planDao(): PlanDao
    abstract fun savedRecipeDao(): SavedRecipeDao
    abstract fun shoppingCategoryDao(): ShoppingCategoryDao
    abstract fun todayWorkoutDao(): TodayWorkoutDao
    abstract fun personalAchievementDao(): PersonalAchievementDao
    abstract fun personalStreakDao(): PersonalStreakDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun progressMilestoneDao(): ProgressMilestoneDao
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create personal achievements table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `personal_achievements` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `targetValue` REAL,
                        `currentValue` REAL NOT NULL,
                        `unit` TEXT,
                        `isCompleted` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create personal streaks table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `personal_streaks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `currentStreak` INTEGER NOT NULL,
                        `longestStreak` INTEGER NOT NULL,
                        `lastActivityTimestamp` INTEGER,
                        `isActive` INTEGER NOT NULL,
                        `targetDays` INTEGER,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create personal records table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `personal_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `exerciseName` TEXT NOT NULL,
                        `recordType` TEXT NOT NULL,
                        `value` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `notes` TEXT,
                        `achievedAt` INTEGER NOT NULL,
                        `previousRecord` REAL,
                        `improvement` REAL
                    )
                """.trimIndent())
                
                // Create progress milestones table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `progress_milestones` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `targetValue` REAL NOT NULL,
                        `currentValue` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `targetDate` TEXT,
                        `isCompleted` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        `progress` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migrate personal_streaks table from lastActivityDate (TEXT) to lastActivityTimestamp (INTEGER)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `personal_streaks_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `currentStreak` INTEGER NOT NULL,
                        `longestStreak` INTEGER NOT NULL,
                        `lastActivityTimestamp` INTEGER,
                        `isActive` INTEGER NOT NULL,
                        `targetDays` INTEGER,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Convert existing data - parse ISO date strings to epoch seconds
                database.execSQL("""
                    INSERT INTO personal_streaks_new (id, name, description, category, currentStreak, longestStreak, lastActivityTimestamp, isActive, targetDays, createdAt)
                    SELECT id, name, description, category, currentStreak, longestStreak, 
                           CASE 
                               WHEN lastActivityDate IS NOT NULL AND lastActivityDate != '' 
                               THEN strftime('%s', lastActivityDate || 'T00:00:00Z')
                               ELSE NULL 
                           END as lastActivityTimestamp,
                           isActive, targetDays, createdAt
                    FROM personal_streaks
                """.trimIndent())
                
                // Drop old table and rename new table
                database.execSQL("DROP TABLE personal_streaks")
                database.execSQL("ALTER TABLE personal_streaks_new RENAME TO personal_streaks")
            }
        }
        
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create weight entries table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `weight_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `weight` REAL NOT NULL,
                        `dateIso` TEXT NOT NULL,
                        `notes` TEXT,
                        `recordedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Add indices for weight entries
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_dateIso` ON `weight_entries` (`dateIso`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_recordedAt` ON `weight_entries` (`recordedAt`)")
            }
        }
        
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context)
            }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return try {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp.db")
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration() // Add fallback for migration issues
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Enable WAL mode for better performance
                    .setQueryCallback({ sqlQuery, bindArgs ->
                        // Log slow queries for performance monitoring
                        android.util.Log.d("Database", "Query: $sqlQuery Args: $bindArgs")
                    }, java.util.concurrent.Executors.newSingleThreadExecutor())
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Add indices for better query performance
                            addPerformanceIndices(db)
                        }
                        
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Ensure WAL mode is enabled and configure for performance
                            db.execSQL("PRAGMA wal_autocheckpoint=1000")
                            db.execSQL("PRAGMA synchronous=NORMAL")
                            db.execSQL("PRAGMA cache_size=10000")
                            db.execSQL("PRAGMA temp_store=MEMORY")
                        }
                    })
                    .build().also { 
                        INSTANCE = it 
                        android.util.Log.i("AppDatabase", "Database initialized successfully")
                    }
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Failed to create database", e)
                // Create a new database with fallback
                try {
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp_fallback.db")
                        .fallbackToDestructiveMigration()
                        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                        .build().also { 
                            INSTANCE = it 
                            android.util.Log.w("AppDatabase", "Using fallback database")
                        }
                } catch (fallbackError: Exception) {
                    android.util.Log.e("AppDatabase", "Even fallback database failed", fallbackError)
                    throw IllegalStateException("Unable to create database", fallbackError)
                }
            }
        }
        
        private fun addPerformanceIndices(db: SupportSQLiteDatabase) {
            try {
                // Add indices for commonly queried columns
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_intake_entries_timestamp ON intake_entries(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_shopping_items_category ON shopping_items(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_shopping_items_checked ON shopping_items(checked)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_personal_achievements_category ON personal_achievements(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_personal_achievements_completed ON personal_achievements(isCompleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_personal_streaks_category ON personal_streaks(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_personal_streaks_active ON personal_streaks(isActive)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_personal_records_exercise ON personal_records(exerciseName)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_progress_milestones_category ON progress_milestones(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_progress_milestones_completed ON progress_milestones(isCompleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_recipe_favorites_recipe ON recipe_favorites(recipeId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_recipe_history_recipe ON recipe_history(recipeId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_ai_logs_timestamp ON ai_logs(timestamp)")
                android.util.Log.i("AppDatabase", "Performance indices added successfully")
            } catch (e: Exception) {
                android.util.Log.w("AppDatabase", "Failed to add some indices", e)
            }
        }
    }
}
