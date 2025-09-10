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
        Index(value = ["title"], name = "index_recipes_title"),
        Index(value = ["difficulty"], name = "index_recipes_difficulty"),
        Index(value = ["prepTime"], name = "index_recipes_prepTime"),
        Index(value = ["cookTime"], name = "index_recipes_cookTime")
    ]
)
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val markdown: String,
    val imageUrl: String?,
    val prepTime: Int?, // in minutes
    val cookTime: Int?, // in minutes
    val servings: Int?,
    val difficulty: String?, // "easy", "medium", "hard"
    val categories: String?, // JSON array as string
    val calories: Int?,
    val protein: Float?,
    val carbs: Float?,
    val fat: Float?,
    val fiber: Float?,
    val sugar: Float?,
    val sodium: Float?,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val isOfficial: Boolean = false, // true for YAZIO official recipes (PRO feature)
    val rating: Float = 0f, // average 5-star rating
    val ratingCount: Int = 0,
    val isLocalOnly: Boolean = true // for potential cloud sync
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
    val category: String = "general",
    val addedAt: Long = System.currentTimeMillis() / 1000,
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
    val targetCarbs: Float? = null,      // g per day
    val targetProtein: Float? = null,    // g per day  
    val targetFat: Float? = null,        // g per day
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

// YAZIO-style Meals for quick food combinations (no detailed cooking instructions)
@Entity(
    tableName = "meals",
    indices = [
        Index(value = ["name"]),
        Index(value = ["mealType"]),
        Index(value = ["createdAt"])
    ]
)
data class MealEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String?,
    val mealType: String, // "breakfast", "lunch", "dinner", "snack"
    val foods: String, // JSON array of food items with quantities
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val lastUsedAt: Long? = null
)

// Recipe ingredients with detailed portions and measurements
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["ingredientOrder"])
    ]
)
data class RecipeIngredientEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val recipeId: String,
    val name: String,
    val amount: Float,
    val unit: String, // "g", "ml", "cups", "tbsp", "pieces", etc.
    val ingredientOrder: Int,
    val isOptional: Boolean = false,
    val preparationNote: String? = null, // "diced", "chopped", "grated", etc.
    val category: String? = null // "protein", "vegetables", "spices", etc.
)

// Recipe cooking steps with detailed instructions
@Entity(
    tableName = "recipe_steps",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["stepOrder"])
    ]
)
data class RecipeStepEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val recipeId: String,
    val stepOrder: Int,
    val instruction: String,
    val estimatedTimeMinutes: Int? = null,
    val temperature: String? = null, // "180°C", "medium heat", etc.
    val timerName: String? = null, // for automatic timer creation
    val timerDurationSeconds: Int? = null,
    val imageUrl: String? = null,
    val tips: String? = null // additional tips for this step
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

// Enhanced Grocery Lists (YAZIO-style smart grocery management)
@Entity(
    tableName = "grocery_lists", 
    indices = [
        Index(value = ["name"]),
        Index(value = ["createdAt"]),
        Index(value = ["isActive"])
    ]
)
data class GroceryListEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val lastModifiedAt: Long = System.currentTimeMillis() / 1000,
    val completedAt: Long? = null
)

@Entity(
    tableName = "grocery_items",
    foreignKeys = [
        ForeignKey(
            entity = GroceryListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"]),
        Index(value = ["category"]),
        Index(value = ["checked"]),
        Index(value = ["fromRecipeId"])
    ]
)
data class GroceryItemEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val listId: String,
    val name: String,
    val quantity: Float? = null,
    val unit: String? = null,
    val category: String, // Store layout categories: "Obst & Gemüse", "Fleisch & Fisch", "Milchprodukte", etc.
    val checked: Boolean = false,
    val fromRecipeId: String? = null, // Track which recipe added this item
    val fromRecipeName: String? = null,
    val estimatedPrice: Float? = null, // for budget tracking
    val notes: String? = null,
    val addedAt: Long = System.currentTimeMillis() / 1000,
    val checkedAt: Long? = null
)

@Entity(tableName = "shopping_list_categories")
data class ShoppingCategoryEntity(
    @PrimaryKey val name: String,
    val order: Int, // for supermarket sorting
    val iconName: String? = null, // Material icon name
    val colorHex: String? = null // for visual categorization
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
    val category: String, // "fitness", "nutrition", "streak", "milestone", "social", "challenge"
    val iconName: String, // Material icon name for display
    val targetValue: Double? = null, // target for numeric achievements
    val currentValue: Double = 0.0, // current progress
    val unit: String? = null, // "kg", "reps", "days", "kcal", etc.
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    // NEW: Enhanced badge system like Freeletics
    val badgeType: String? = null, // "bronze", "silver", "gold", "platinum", "diamond"
    val rarity: String? = null, // "common", "rare", "epic", "legendary"
    val socialVisible: Boolean = true, // whether to show in social feeds
    val challengeId: Long? = null, // if earned from completing a challenge
    val shareMessage: String? = null, // custom message when sharing achievement
    val pointsValue: Int = 0 // points earned for completing this achievement
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
    val carbs: Float,     // g per 100g
    val protein: Float,   // g per 100g
    val fat: Float,       // g per 100g
    val createdAt: Long = System.currentTimeMillis() / 1000,
    // Extended fields for OpenFoodFacts integration
    val fiber: Float? = null,       // g per 100g
    val sugar: Float? = null,       // g per 100g
    val sodium: Float? = null,      // mg per 100g
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
    val quantityGrams: Float,   // Consumed amount in grams
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

// Advanced Workout Execution Enhancement - Phase 1 Entities

@Entity(
    tableName = "workout_performance",
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["sessionId"]),
        Index(value = ["planId"]),
        Index(value = ["timestamp"]),
        Index(value = ["exerciseIndex"])
    ]
)
data class WorkoutPerformanceEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val exerciseId: String,
    val sessionId: String,
    val planId: Long,
    val exerciseIndex: Int,
    
    // Performance Metrics
    val heartRateAvg: Int? = null,
    val heartRateMax: Int? = null,
    val heartRateZone: String? = null, // "zone1", "zone2", "zone3", "zone4"
    val reps: Int,
    val weight: Float,
    val volume: Float, // weight * reps
    val restTime: Long, // planned rest time in seconds
    val actualRestTime: Long, // actual rest time in seconds
    
    // Quality Metrics
    val formQuality: Float = 1.0f, // 0.0-1.0 from sensor analysis
    val perceivedExertion: Int? = null, // 1-10 RPE scale
    val movementSpeed: Float? = null, // reps per minute
    val rangeOfMotion: Float? = null, // percentage of full ROM
    
    // Session Context
    val timestamp: Long = System.currentTimeMillis() / 1000,
    val duration: Long, // exercise duration in seconds
    val isPersonalRecord: Boolean = false,
    val notes: String? = null
)

@Entity(
    tableName = "workout_sessions",
    indices = [
        Index(value = ["planId"]),
        Index(value = ["userId"]),
        Index(value = ["startTime"]),
        Index(value = ["workoutEfficiencyScore"])
    ]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val planId: Long,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    
    // Session Metrics
    val totalVolume: Float = 0f,
    val averageHeartRate: Int? = null,
    val caloriesBurned: Int? = null,
    val workoutEfficiencyScore: Float = 0f, // 0.0-1.0
    val fatigueLevel: String = "medium", // "low", "medium", "high"
    
    // Progress Tracking
    val personalRecordsAchieved: Int = 0,
    val completionPercentage: Float = 0f,
    val sessionRating: Int? = null, // 1-5 stars
    val sessionNotes: String? = null,
    
    // Pause/Resume Functionality
    val pauseStartTime: Long? = null,
    val totalPauseTime: Long = 0L,
    val actualDuration: Long? = null
)

@Entity(
    tableName = "exercise_progressions",
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["userId"]),
        Index(value = ["performanceTrend"]),
        Index(value = ["plateauDetected"]),
        Index(value = ["lastProgressDate"])
    ]
)
data class ExerciseProgressionEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val exerciseId: String,
    val userId: String,
    
    // Progression Recommendations
    val currentWeight: Float,
    val recommendedWeight: Float,
    val currentReps: Int,
    val recommendedReps: Int,
    val progressionReason: String, // "strength_gain", "plateau_break", "deload"
    
    // Performance Analysis
    val performanceTrend: String = "stable", // "improving", "plateauing", "declining", "stable"
    val plateauDetected: Boolean = false,
    val plateauWeeks: Int = 0,
    val lastProgressDate: Long,
    
    // AI Insights
    val aiConfidence: Float = 0.5f, // 0.0-1.0
    val nextReviewDate: Long,
    val adaptationNotes: String? = null
)

// Cooking Feature Entities

@Entity(
    tableName = "cooking_sessions",
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["startTime"]),
        Index(value = ["status"])
    ]
)
data class CookingSessionEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val recipeId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String = "active", // "active", "paused", "completed", "cancelled"
    val currentStep: Int = 0,
    val totalSteps: Int,
    val estimatedDuration: Long? = null, // in seconds
    val actualDuration: Long? = null, // in seconds
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "cooking_timers",
    foreignKeys = [
        ForeignKey(
            entity = CookingSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["stepIndex"]),
        Index(value = ["isActive"])
    ]
)
data class CookingTimerEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val sessionId: String,
    val stepIndex: Int,
    val name: String,
    val durationSeconds: Long,
    val remainingSeconds: Long,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val startTime: Long? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

// Health Connect Integration Entities

@Entity(
    tableName = "health_connect_steps",
    indices = [
        Index(value = ["date"]),
        Index(value = ["source"]),
        Index(value = ["syncedAt"])
    ]
)
data class HealthStepsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // ISO date (yyyy-MM-dd)
    val steps: Int,
    val source: String, // "health_connect", "manual", "google_fit", etc.
    val syncedAt: Long = System.currentTimeMillis() / 1000,
    val lastModified: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "health_connect_heart_rate",
    indices = [
        Index(value = ["date"]),
        Index(value = ["timestamp"]),
        Index(value = ["source"])
    ]
)
data class HealthHeartRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, // Epoch timestamp in seconds
    val date: String, // ISO date (yyyy-MM-dd)
    val heartRate: Int, // BPM
    val source: String,
    val syncedAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "health_connect_calories",
    indices = [
        Index(value = ["date"]),
        Index(value = ["calorieType"]),
        Index(value = ["source"])
    ]
)
data class HealthCalorieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // ISO date (yyyy-MM-dd)
    val calories: Double, // kcal
    val calorieType: String, // "active", "total", "basal"
    val source: String,
    val syncedAt: Long = System.currentTimeMillis() / 1000,
    val lastModified: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "health_connect_sleep",
    indices = [
        Index(value = ["date"]),
        Index(value = ["source"]),
        Index(value = ["sleepStage"])
    ]
)
data class HealthSleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // ISO date (yyyy-MM-dd)
    val startTime: Long, // Epoch timestamp in seconds
    val endTime: Long, // Epoch timestamp in seconds
    val durationMinutes: Int,
    val sleepStage: String, // "light", "deep", "rem", "awake", "unknown"
    val source: String,
    val syncedAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "health_connect_exercise_sessions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["exerciseType"]),
        Index(value = ["source"])
    ]
)
data class HealthExerciseSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String, // Unique identifier from Health Connect
    val date: String, // ISO date (yyyy-MM-dd)
    val startTime: Long, // Epoch timestamp in seconds
    val endTime: Long, // Epoch timestamp in seconds
    val durationMinutes: Int,
    val exerciseType: String, // "running", "cycling", "weightlifting", etc.
    val title: String,
    val calories: Double? = null,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val distance: Double? = null, // in meters
    val source: String,
    val syncedAt: Long = System.currentTimeMillis() / 1000,
    val lastModified: Long = System.currentTimeMillis() / 1000
)

// Cloud Sync Entities for Multi-Device Support
@Entity(
    tableName = "cloud_sync_metadata",
    indices = [
        Index(value = ["entityType", "entityId"], unique = true),
        Index(value = ["lastSyncTime"]),
        Index(value = ["syncStatus"]),
        Index(value = ["deviceId"])
    ]
)
data class CloudSyncEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val entityType: String,  // e.g., "PersonalAchievement", "WeightEntry", "WorkoutSession"
    val entityId: String,    // The ID of the actual entity being synced
    val lastSyncTime: Long,  // Timestamp of last successful sync
    val lastModifiedTime: Long, // Timestamp when entity was last modified locally
    val syncStatus: String,  // "synced", "pending", "conflict", "error"
    val deviceId: String,    // Unique identifier for this device
    val cloudVersion: String?, // Version/etag from cloud storage
    val conflictData: String? = null, // JSON data for conflict resolution
    val retryCount: Int = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "user_profiles",
    indices = [
        Index(value = ["userId"], unique = true),
        Index(value = ["email"], unique = true),
        Index(value = ["lastSyncTime"])
    ]
)
data class UserProfileEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,      // Cloud user ID (Firebase/Auth0/etc)
    val email: String,       // User email for identification
    val displayName: String? = null,
    val deviceName: String,  // Name of this device
    val deviceId: String,    // Unique device identifier
    val lastSyncTime: Long,  // Last time this user synced
    val syncPreferences: String, // JSON with sync settings (what to sync)
    val encryptionKey: String? = null, // For end-to-end encryption
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "sync_conflicts",
    indices = [
        Index(value = ["entityType", "entityId"]),
        Index(value = ["createdAt"]),
        Index(value = ["status"])
    ]
)
data class SyncConflictEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val localData: String,   // JSON of local version
    val remoteData: String,  // JSON of remote version
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val status: String,      // "pending", "resolved", "auto_resolved"
    val resolution: String?, // "local_wins", "remote_wins", "merged", "manual"
    val resolvedData: String? = null, // JSON of resolved version
    val resolvedBy: String? = null,   // "auto", "user", "ai"
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val resolvedAt: Long? = null
)

// Social Challenge Entities for Freeletics-style gamification

// Recipe Analytics (YAZIO-style usage tracking)
@Entity(
    tableName = "recipe_analytics",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["eventType"]),
        Index(value = ["timestamp"])
    ]
)
data class RecipeAnalyticsEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val recipeId: String,
    val eventType: String, // "view", "cooking_started", "cooking_completed", "added_to_favorites", "added_to_grocery_list"
    val timestamp: Long = System.currentTimeMillis() / 1000,
    val sessionId: String? = null, // for cooking sessions
    val metadata: String? = null // JSON for additional data like completion percentage
)

@Entity(
    tableName = "recipe_ratings",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["rating"]),
        Index(value = ["createdAt"])
    ]
)
data class RecipeRatingEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val recipeId: String,
    val rating: Float, // 1.0 to 5.0
    val comment: String? = null,
    val userId: String? = null, // if user accounts are implemented
    val createdAt: Long = System.currentTimeMillis() / 1000
)

// PRO Feature Access Control
@Entity(
    tableName = "pro_feature_access",
    indices = [
        Index(value = ["featureName"]),
        Index(value = ["isUnlocked"]),
        Index(value = ["lastChecked"])
    ]
)
data class ProFeatureEntity(
    @PrimaryKey val featureName: String, // "access_official_recipe_database", "advanced_recipe_filters", etc.
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val lastChecked: Long = System.currentTimeMillis() / 1000,
    val usageCount: Int = 0,
    val maxUsage: Int? = null // for trial features
)

// Recipe Collections/Categories (YAZIO-style organization)
@Entity(
    tableName = "recipe_collections",
    indices = [
        Index(value = ["name"]),
        Index(value = ["isOfficial"]),
        Index(value = ["createdAt"])
    ]
)
data class RecipeCollectionEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val isOfficial: Boolean = false, // true for YAZIO curated collections
    val isPremium: Boolean = false, // requires PRO subscription
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "recipe_collection_items",
    foreignKeys = [
        ForeignKey(
            entity = RecipeCollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["collectionId"]),
        Index(value = ["recipeId"]),
        Index(value = ["sortOrder"])
    ]
)
data class RecipeCollectionItemEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val collectionId: String,
    val recipeId: String,
    val sortOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "social_challenges",
    indices = [
        Index(value = ["status"]),
        Index(value = ["category"]),
        Index(value = ["startDate"]),
        Index(value = ["endDate"]),
        Index(value = ["createdAt"])
    ]
)
data class SocialChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "fitness", "nutrition", "weight_loss", "endurance", "strength"
    val challengeType: String, // "public", "private", "featured"
    val targetMetric: String, // "workouts", "calories", "weight_loss", "steps", "water_intake"
    val targetValue: Double,
    val unit: String, // "trainings", "kcal", "kg", "steps", "liters"
    val duration: Int, // days
    val startDate: String, // ISO date string
    val endDate: String, // ISO date string
    val maxParticipants: Int? = null, // null for unlimited
    val currentParticipants: Int = 0,
    val status: String, // "upcoming", "active", "completed", "cancelled"
    val creatorId: String? = null, // for user-created challenges
    val reward: String? = null, // badge or reward description
    val difficulty: String, // "beginner", "intermediate", "advanced", "expert"
    val imageUrl: String? = null,
    val rules: String? = null, // Additional rules or description
    val isOfficial: Boolean = false, // true for app-created challenges
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "challenge_participations",
    foreignKeys = [
        ForeignKey(
            entity = SocialChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["challengeId"]),
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["joinedAt"])
    ]
)
data class ChallengeParticipationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: Long,
    val userId: String, // can be device ID or user ID if we have user accounts
    val userName: String? = null, // display name for leaderboards
    val status: String, // "active", "completed", "quit", "failed"
    val currentProgress: Double = 0.0,
    val progressPercentage: Double = 0.0, // calculated percentage
    val lastActivityDate: String? = null, // ISO date string
    val completedAt: Long? = null,
    val joinedAt: Long = System.currentTimeMillis() / 1000,
    val rank: Int? = null, // current ranking in challenge
    val personalBest: Double? = null, // best single day/session performance
    val notes: String? = null // user notes or motivation
)

@Entity(
    tableName = "challenge_progress_logs",
    foreignKeys = [
        ForeignKey(
            entity = ChallengeParticipationEntity::class,
            parentColumns = ["id"],
            childColumns = ["participationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["participationId"]),
        Index(value = ["logDate"]),
        Index(value = ["timestamp"])
    ]
)
data class ChallengeProgressLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val participationId: Long,
    val logDate: String, // ISO date string
    val value: Double, // progress value for this day/session
    val description: String? = null, // what was accomplished
    val source: String, // "workout", "nutrition", "manual", "automatic"
    val timestamp: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "social_badges",
    indices = [
        Index(value = ["category"]),
        Index(value = ["badgeType"]),
        Index(value = ["rarity"]),
        Index(value = ["isUnlocked"]),
        Index(value = ["unlockedAt"])
    ]
)
data class SocialBadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "achievement", "streak", "challenge", "social", "milestone"
    val badgeType: String, // "bronze", "silver", "gold", "platinum", "diamond", "special"
    val iconName: String, // Material icon name
    val rarity: String, // "common", "rare", "epic", "legendary"
    val requirements: String, // JSON or text description of unlock requirements
    val challengeId: Long? = null, // associated with specific challenge
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Double = 0.0, // progress towards unlocking (0.0 to 100.0)
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Entity(
    tableName = "leaderboard_entries",
    foreignKeys = [
        ForeignKey(
            entity = SocialChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["challengeId"]),
        Index(value = ["userId"]),
        Index(value = ["rank"]),
        Index(value = ["score"], name = "index_leaderboard_score")
    ]
)
data class LeaderboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: Long,
    val userId: String,
    val userName: String? = null,
    val rank: Int,
    val score: Double, // final score/progress
    val completionTime: Long? = null, // when they completed the challenge
    val badge: String? = null, // badge earned for this ranking
    val lastUpdated: Long = System.currentTimeMillis() / 1000
)
