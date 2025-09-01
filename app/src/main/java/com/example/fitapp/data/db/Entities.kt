package com.example.fitapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "recipes",
    indices = [
        Index(value = ["createdAt"], name = "index_recipes_createdAt"),
        Index(value = ["calories"], name = "index_recipes_calories"), 
        Index(value = ["title"], name = "index_recipes_title")
    ]
)
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val markdown: String,
    val calories: Int?,
    val imageUrl: String?,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "recipe_favorites",
    primaryKeys = ["recipeId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class RecipeFavoriteEntity(
    val recipeId: String,
    val savedAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "recipe_history",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class RecipeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: String,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "intake_entries",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["kcal"]),
        Index(value = ["timestamp", "kcal"])
    ]
)
data class IntakeEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis() / 1000,
    val label: String,
    val kcal: Int,
    val source: String,
    val referenceId: String?
)

@Entity(tableName = "daily_goals", primaryKeys = ["dateIso"])
data class DailyGoalEntity(
    val dateIso: String,
    val targetKcal: Int,
    val targetCarbs: Double? = null,      // g per day
    val targetProtein: Double? = null,    // g per day  
    val targetFat: Double? = null,        // g per day
    val targetWaterMl: Int? = null       // ml per day
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: String?,
    val unit: String?,
    val checked: Boolean = false,
    val category: String? = null, // "Obst & Gemüse", "Fleisch & Fisch", etc.
    val fromRecipeId: String? = null, // reference to recipe if added from recipe
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(tableName = "training_plans")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val goal: String,
    val weeks: Int,
    val sessionsPerWeek: Int,
    val minutesPerSession: Int,
    val equipment: String, // JSON array as string
    val trainingDays: String? = null, // comma-separated DayOfWeek names, e.g. "MONDAY,WEDNESDAY,THURSDAY"
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val markdown: String,
    val calories: Int?,
    val imageUrl: String?,
    val ingredients: String, // JSON array as string
    val tags: String, // comma-separated tags like "vegetarian,high-protein,low-carb"
    val prepTime: Int?, // minutes
    val difficulty: String?, // "easy", "medium", "hard"
    val servings: Int?,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val lastCookedAt: Long? = null
)

@Entity(tableName = "shopping_list_categories")
data class ShoppingCategoryEntity(
    @PrimaryKey val name: String,
    val order: Int // for supermarket sorting
)

@Entity(
    tableName = "today_workouts",
    indices = [
        Index(value = ["dateIso"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class TodayWorkoutEntity(
    @PrimaryKey val dateIso: String, // e.g., "2025-08-28"
    val content: String, // string with pipe-lines format
    val status: String, // "pending", "completed", "skipped"
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val completedAt: Long? = null,
    val planId: Long? = null // nullable FK-like field; no constraint needed
)

@Entity(
    tableName = "personal_achievements",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isCompleted"]),
        Index(value = ["createdAt"])
    ]
)
data class PersonalAchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "fitness", "nutrition", "streak", "milestone"
    val iconName: String, // Material icon name for display
    val targetValue: Double? = null, // target for numeric achievements
    val currentValue: Double = 0.0, // current progress
    val unit: String? = null, // "kg", "reps", "days", "kcal", etc.
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(tableName = "personal_streaks")
data class PersonalStreakEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val category: String, // "workout", "nutrition", "habit"
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityTimestamp: Long? = null, // Epoch timestamp in seconds
    val isActive: Boolean = true,
    val targetDays: Int? = null, // target streak length
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val recordType: String, // "weight", "reps", "time", "distance"
    val value: Double,
    val unit: String, // "kg", "lbs", "reps", "seconds", "meters", etc.
    val notes: String? = null,
    val achievedAt: Long = System.currentTimeMillis() / 1000,
    val previousRecord: Double? = null, // previous best for comparison
    val improvement: Double? = null // calculated improvement percentage
)

@Entity(tableName = "progress_milestones")
data class ProgressMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "weight_loss", "muscle_gain", "endurance", "strength"
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String, // "kg", "kcal", "minutes", "reps", etc.
    val targetDate: String? = null, // ISO date string for deadline
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val progress: Double = 0.0, // percentage (0.0 to 100.0)
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "weight_entries",
    indices = [
        Index(value = ["dateIso"]),
        Index(value = ["recordedAt"])
    ]
)
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weight: Double, // weight in kg
    val dateIso: String, // e.g., "2025-01-15"
    val notes: String? = null,
    val recordedAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "food_items", 
    indices = [
        Index(value = ["name"]), 
        Index(value = ["barcode"])
    ]
)
data class FoodItemEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val barcode: String? = null,
    val calories: Int,    // kcal per 100g
    val carbs: Double,     // g per 100g
    val protein: Double,   // g per 100g
    val fat: Double,       // g per 100g
    val createdAt: Long = System.currentTimeMillis() / 1000,
    // Extended fields for OpenFoodFacts integration
    val fiber: Double? = null,       // g per 100g
    val sugar: Double? = null,       // g per 100g
    val sodium: Double? = null,      // mg per 100g
    val brands: String? = null,
    val categories: String? = null,
    val imageUrl: String? = null,
    val servingSize: String? = null,
    val ingredients: String? = null
)

@Entity(
    tableName = "meal_entries",
    foreignKeys = [ForeignKey(
        entity = FoodItemEntity::class,
        parentColumns = ["id"],
        childColumns = ["foodItemId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["foodItemId"]),
        Index(value = ["date"]),
        Index(value = ["mealType"])
    ]
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodItemId: String,
    val date: String,           // ISO-Date (yyyy-MM-dd)
    val mealType: String,       // breakfast, lunch, dinner, snack
    val quantityGrams: Double,   // Consumed amount in grams
    val notes: String? = null
)

@Entity(
    tableName = "water_entries",
    indices = [Index(value = ["date"])]
)
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,  // ISO-Date (yyyy-MM-dd)
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "bmi_history",
    indices = [
        Index(value = ["date"]),
        Index(value = ["bmi"]),
        Index(value = ["recordedAt"])
    ]
)
data class BMIHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // ISO date (yyyy-MM-dd)
    val height: Float, // cm
    val weight: Float, // kg
    val bmi: Float,
    val category: String, // UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE
    val notes: String? = null,
    val recordedAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "weight_loss_programs",
    indices = [
        Index(value = ["startDate"]),
        Index(value = ["isActive"]),
        Index(value = ["programType"])
    ]
)
data class WeightLossProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: String, // ISO date
    val endDate: String?, // ISO date
    val startWeight: Float, // kg
    val targetWeight: Float, // kg
    val currentWeight: Float, // kg
    val dailyCalorieTarget: Int,
    val weeklyWeightLossGoal: Float, // kg per week
    val isActive: Boolean = true,
    val programType: String, // "standard", "intensive", "maintenance"
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "behavioral_check_ins",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["moodScore"]),
        Index(value = ["stressLevel"])
    ]
)
data class BehavioralCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val moodScore: Int, // 1-10 scale
    val hungerLevel: Int, // 1-10 scale
    val stressLevel: Int, // 1-10 scale
    val sleepQuality: Int?, // 1-10 scale
    val triggers: String, // JSON encoded list of EmotionalTrigger
    val copingStrategy: String?,
    val mealContext: String? // "before_meal", "after_meal", "snack"
)

@Entity(
    tableName = "progress_photos",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["weight"]),
        Index(value = ["bmi"])
    ]
)
data class ProgressPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val timestamp: Long,
    val weight: Float,
    val bmi: Float,
    val notes: String? = null
)

