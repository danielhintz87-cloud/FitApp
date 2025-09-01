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
        WeightEntity::class,
        FoodItemEntity::class,
        MealEntryEntity::class,
        WaterEntryEntity::class
    ],
    version = 10,
    exportSchema = true
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
    abstract fun foodItemDao(): FoodItemDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun waterEntryDao(): WaterEntryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create personal achievements table
                db.execSQL("""
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
                db.execSQL("""
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
                db.execSQL("""
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
                db.execSQL("""
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
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migrate personal_streaks table from lastActivityDate (TEXT) to lastActivityTimestamp (INTEGER)
                db.execSQL("""
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
                db.execSQL("""
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
                db.execSQL("DROP TABLE personal_streaks")
                db.execSQL("ALTER TABLE personal_streaks_new RENAME TO personal_streaks")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create weight entries table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `weight_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `weight` REAL NOT NULL,
                        `dateIso` TEXT NOT NULL,
                        `notes` TEXT,
                        `recordedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Add indices for weight entries
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_dateIso` ON `weight_entries` (`dateIso`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_recordedAt` ON `weight_entries` (`recordedAt`)")
                
                // Add missing recipe indices to match entity schema
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_createdAt` ON `recipes` (`createdAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_calories` ON `recipes` (`calories`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_title` ON `recipes` (`title`)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Extend daily_goals table with macro targets
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `daily_goals_new` (
                        `dateIso` TEXT NOT NULL,
                        `targetKcal` INTEGER NOT NULL,
                        `targetCarbs` REAL,
                        `targetProtein` REAL,
                        `targetFat` REAL,
                        `targetWaterMl` INTEGER,
                        PRIMARY KEY(`dateIso`)
                    )
                """.trimIndent())
                
                // Copy existing data
                db.execSQL("""
                    INSERT INTO daily_goals_new (dateIso, targetKcal)
                    SELECT dateIso, targetKcal FROM daily_goals
                """.trimIndent())
                
                // Drop old table and rename new table
                db.execSQL("DROP TABLE daily_goals")
                db.execSQL("ALTER TABLE daily_goals_new RENAME TO daily_goals")
                
                // Create food_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `food_items` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `barcode` TEXT,
                        `calories` INTEGER NOT NULL,
                        `carbs` REAL NOT NULL,
                        `protein` REAL NOT NULL,
                        `fat` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                
                // Create meal_entries table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `meal_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `foodItemId` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `mealType` TEXT NOT NULL,
                        `quantityGrams` REAL NOT NULL,
                        `notes` TEXT,
                        FOREIGN KEY(`foodItemId`) REFERENCES `food_items`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Create water_entries table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `water_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `amountMl` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Add indices for new tables
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_name` ON `food_items` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_barcode` ON `food_items` (`barcode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_foodItemId` ON `meal_entries` (`foodItemId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_date` ON `meal_entries` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_mealType` ON `meal_entries` (`mealType`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_water_entries_date` ON `water_entries` (`date`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Extend food_items table with OpenFoodFacts integration fields
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `food_items_new` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `barcode` TEXT,
                        `calories` INTEGER NOT NULL,
                        `carbs` REAL NOT NULL,
                        `protein` REAL NOT NULL,
                        `fat` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `fiber` REAL,
                        `sugar` REAL,
                        `sodium` REAL,
                        `brands` TEXT,
                        `categories` TEXT,
                        `imageUrl` TEXT,
                        `servingSize` TEXT,
                        `ingredients` TEXT,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                
                // Copy existing data
                db.execSQL("""
                    INSERT INTO food_items_new (id, name, barcode, calories, carbs, protein, fat, createdAt)
                    SELECT id, name, barcode, calories, carbs, protein, fat, createdAt FROM food_items
                """.trimIndent())
                
                // Drop old table and rename new table
                db.execSQL("DROP TABLE food_items")
                db.execSQL("ALTER TABLE food_items_new RENAME TO food_items")
                
                // Recreate indices for food_items table
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_name` ON `food_items` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_barcode` ON `food_items` (`barcode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_categories` ON `food_items` (`categories`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_brands` ON `food_items` (`brands`)")
            }
        }
        
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context)
            }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return try {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp.db")
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .apply {
                        // Only allow destructive migration in debug builds
                        if (com.example.fitapp.BuildConfig.DEBUG) {
                            fallbackToDestructiveMigration()
                        }
                    }
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
                            // Configure database performance settings safely
                            try {
                                // Use query method instead of execSQL for PRAGMA statements
                                db.query("PRAGMA wal_autocheckpoint=1000").close()
                                db.query("PRAGMA synchronous=NORMAL").close()
                                db.query("PRAGMA cache_size=10000").close() 
                                db.query("PRAGMA temp_store=MEMORY").close()
                                android.util.Log.d("AppDatabase", "Database performance settings configured")
                            } catch (e: Exception) {
                                android.util.Log.w("AppDatabase", "Failed to configure some database settings", e)
                                // Continue without these optimizations if they fail
                            }
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
            val indicesToCreate = listOf(
                "CREATE INDEX IF NOT EXISTS idx_intake_entries_timestamp ON intake_entries(timestamp)",
                "CREATE INDEX IF NOT EXISTS idx_shopping_items_category ON shopping_items(category)",
                "CREATE INDEX IF NOT EXISTS idx_shopping_items_checked ON shopping_items(checked)",
                "CREATE INDEX IF NOT EXISTS idx_personal_achievements_category ON personal_achievements(category)",
                "CREATE INDEX IF NOT EXISTS idx_personal_achievements_completed ON personal_achievements(isCompleted)",
                "CREATE INDEX IF NOT EXISTS idx_personal_streaks_category ON personal_streaks(category)",
                "CREATE INDEX IF NOT EXISTS idx_personal_streaks_active ON personal_streaks(isActive)",
                "CREATE INDEX IF NOT EXISTS idx_personal_records_exercise ON personal_records(exerciseName)",
                "CREATE INDEX IF NOT EXISTS idx_progress_milestones_category ON progress_milestones(category)",
                "CREATE INDEX IF NOT EXISTS idx_progress_milestones_completed ON progress_milestones(isCompleted)",
                "CREATE INDEX IF NOT EXISTS idx_recipe_favorites_recipe ON recipe_favorites(recipeId)",
                "CREATE INDEX IF NOT EXISTS idx_recipe_history_recipe ON recipe_history(recipeId)",
                "CREATE INDEX IF NOT EXISTS idx_ai_logs_ts ON ai_logs(ts)",
                "CREATE INDEX IF NOT EXISTS index_recipes_createdAt ON recipes(createdAt)",
                "CREATE INDEX IF NOT EXISTS index_recipes_calories ON recipes(calories)",
                "CREATE INDEX IF NOT EXISTS index_recipes_title ON recipes(title)"
            )
            
            var successCount = 0
            var failureCount = 0
            
            for (indexSql in indicesToCreate) {
                try {
                    db.execSQL(indexSql)
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    android.util.Log.w("AppDatabase", "Failed to create index: $indexSql", e)
                }
            }
            
            android.util.Log.i("AppDatabase", "Performance indices creation completed: $successCount successful, $failureCount failed")
        }
    }
}
