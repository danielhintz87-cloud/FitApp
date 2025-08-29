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
        ProgressMilestoneEntity::class
    ],
    version = 7,
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
        
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp.db")
                        .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                        .fallbackToDestructiveMigration() // Add fallback for migration issues
                        .build().also { INSTANCE = it }
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Failed to create database", e)
                    // Create a new database with fallback
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp_fallback.db")
                        .fallbackToDestructiveMigration()
                        .build().also { INSTANCE = it }
                }
            }
    }
}
