package com.example.fitapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AiLog::class,
        RecipeEntity::class,
        RecipeFavoriteEntity::class,
        RecipeHistoryEntity::class,
        IntakeEntryEntity::class,
        DailyGoalEntity::class,
        ShoppingItemEntity::class,
        PlanEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aiLogDao(): AiLogDao
    abstract fun recipeDao(): RecipeDao
    abstract fun intakeDao(): IntakeDao
    abstract fun goalDao(): GoalDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun planDao(): PlanDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp.db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
