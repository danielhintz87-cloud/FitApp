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
    version = 6,
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
                        `lastActivityDate` TEXT,
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
        
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp.db")
                    .addMigrations(MIGRATION_5_6)
                    .build().also { INSTANCE = it }
            }
    }
}
