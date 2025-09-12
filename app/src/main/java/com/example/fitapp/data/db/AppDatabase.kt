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
        // Nutrition & Hydration
        FoodItemEntity::class,
        MealEntryEntity::class,
        WaterEntryEntity::class,
        // BMI and Weight Loss tracking
        BMIHistoryEntity::class,
        WeightLossProgramEntity::class,
        BehavioralCheckInEntity::class,
        ProgressPhotoEntity::class,
        // Advanced Workout Execution Enhancement - Phase 1
        WorkoutPerformanceEntity::class,
        WorkoutSessionEntity::class,
        ExerciseProgressionEntity::class,
        // Cooking Features
        CookingSessionEntity::class,
        CookingTimerEntity::class,
        // Health Connect Integration
        HealthStepsEntity::class,
        HealthHeartRateEntity::class,
        HealthCalorieEntity::class,
        HealthSleepEntity::class,
        HealthExerciseSessionEntity::class,
        // Cloud Sync Entities
        CloudSyncEntity::class,
        UserProfileEntity::class,
        SyncConflictEntity::class,
        // Social Challenge Entities
        SocialChallengeEntity::class,
        ChallengeParticipationEntity::class,
        ChallengeProgressLogEntity::class,
        SocialBadgeEntity::class,
        LeaderboardEntryEntity::class,
        // YAZIO-style Recipe and Meal Management
        MealEntity::class,
        RecipeIngredientEntity::class,
        RecipeStepEntity::class,
        GroceryListEntity::class,
        GroceryItemEntity::class,
        RecipeAnalyticsEntity::class,
        RecipeRatingEntity::class,
        ProFeatureEntity::class,
        RecipeCollectionEntity::class,
        RecipeCollectionItemEntity::class,
    ],
    version = 17,
    exportSchema = true,
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

    abstract fun bmiHistoryDao(): BMIHistoryDao

    abstract fun weightLossProgramDao(): WeightLossProgramDao

    abstract fun behavioralCheckInDao(): BehavioralCheckInDao

    abstract fun progressPhotoDao(): ProgressPhotoDao

    // Advanced Workout Execution Enhancement - Phase 1 DAOs
    abstract fun workoutPerformanceDao(): WorkoutPerformanceDao

    abstract fun workoutSessionDao(): WorkoutSessionDao

    abstract fun exerciseProgressionDao(): ExerciseProgressionDao

    // Cooking Mode Enhancement - Phase 2 DAOs
    abstract fun cookingSessionDao(): CookingSessionDao

    abstract fun cookingTimerDao(): CookingTimerDao

    // Health Connect DAOs
    abstract fun healthStepsDao(): HealthStepsDao

    abstract fun healthHeartRateDao(): HealthHeartRateDao

    abstract fun healthCalorieDao(): HealthCalorieDao

    abstract fun healthSleepDao(): HealthSleepDao

    abstract fun healthExerciseSessionDao(): HealthExerciseSessionDao

    // Cloud Sync DAOs
    abstract fun cloudSyncDao(): CloudSyncDao

    abstract fun userProfileDao(): UserProfileDao

    abstract fun syncConflictDao(): SyncConflictDao

    // Social Challenge DAOs
    abstract fun socialChallengeDao(): SocialChallengeDao

    abstract fun challengeParticipationDao(): ChallengeParticipationDao

    abstract fun challengeProgressLogDao(): ChallengeProgressLogDao

    abstract fun socialBadgeDao(): SocialBadgeDao

    abstract fun leaderboardEntryDao(): LeaderboardEntryDao

    // YAZIO-style Recipe and Meal Management DAOs
    abstract fun mealDao(): MealDao

    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create personal achievements table
                    db.execSQL(
                        """
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
                        """.trimIndent(),
                    )

                    // Create personal streaks table
                    db.execSQL(
                        """
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
                        """.trimIndent(),
                    )

                    // Create personal records table
                    db.execSQL(
                        """
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
                        """.trimIndent(),
                    )

                    // Create progress milestones table
                    db.execSQL(
                        """
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
                        """.trimIndent(),
                    )
                }
            }

        val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Migrate personal_streaks table from lastActivityDate (TEXT) to lastActivityTimestamp (INTEGER)
                    db.execSQL(
                        """
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
                        """.trimIndent(),
                    )

                    // Convert existing data - parse ISO date strings to epoch seconds
                    db.execSQL(
                        """
                        INSERT INTO personal_streaks_new (id, name, description, category, currentStreak, longestStreak, lastActivityTimestamp, isActive, targetDays, createdAt)
                        SELECT id, name, description, category, currentStreak, longestStreak,
                               CASE
                                   WHEN lastActivityDate IS NOT NULL AND lastActivityDate != ''
                                   THEN strftime('%s', lastActivityDate || 'T00:00:00Z')
                                   ELSE NULL
                               END as lastActivityTimestamp,
                               isActive, targetDays, createdAt
                        FROM personal_streaks
                        """.trimIndent(),
                    )

                    // Drop old table and rename new table
                    db.execSQL("DROP TABLE personal_streaks")
                    db.execSQL("ALTER TABLE personal_streaks_new RENAME TO personal_streaks")
                }
            }

        val MIGRATION_7_8 =
            object : Migration(7, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create weight entries table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `weight_entries` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `weight` REAL NOT NULL,
                            `dateIso` TEXT NOT NULL,
                            `notes` TEXT,
                            `recordedAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Add indices for weight entries
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_weight_entries_dateIso` ON `weight_entries` (`dateIso`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_weight_entries_recordedAt` ON `weight_entries` (`recordedAt`)",
                    )

                    // Add missing recipe indices to match entity schema
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_createdAt` ON `recipes` (`createdAt`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_calories` ON `recipes` (`calories`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_title` ON `recipes` (`title`)")
                }
            }

        val MIGRATION_8_9 =
            object : Migration(8, 9) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Extend daily_goals table with macro targets
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `daily_goals_new` (
                            `dateIso` TEXT NOT NULL,
                            `targetKcal` INTEGER NOT NULL,
                            `targetCarbs` REAL,
                            `targetProtein` REAL,
                            `targetFat` REAL,
                            `targetWaterMl` INTEGER,
                            PRIMARY KEY(`dateIso`)
                        )
                        """.trimIndent(),
                    )

                    // Copy existing data
                    db.execSQL(
                        """
                        INSERT INTO daily_goals_new (dateIso, targetKcal)
                        SELECT dateIso, targetKcal FROM daily_goals
                        """.trimIndent(),
                    )

                    // Drop old table and rename new table
                    db.execSQL("DROP TABLE daily_goals")
                    db.execSQL("ALTER TABLE daily_goals_new RENAME TO daily_goals")

                    // Create food_items table
                    db.execSQL(
                        """
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
                        """.trimIndent(),
                    )

                    // Create meal_entries table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `meal_entries` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `foodItemId` TEXT NOT NULL,
                            `date` TEXT NOT NULL,
                            `mealType` TEXT NOT NULL,
                            `quantityGrams` REAL NOT NULL,
                            `notes` TEXT,
                            FOREIGN KEY(`foodItemId`) REFERENCES `food_items`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create water_entries table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `water_entries` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `date` TEXT NOT NULL,
                            `amountMl` INTEGER NOT NULL,
                            `timestamp` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Add indices for new tables
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_name` ON `food_items` (`name`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_barcode` ON `food_items` (`barcode`)")
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_meal_entries_foodItemId` ON `meal_entries` (`foodItemId`)",
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_date` ON `meal_entries` (`date`)")
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_meal_entries_mealType` ON `meal_entries` (`mealType`)",
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_water_entries_date` ON `water_entries` (`date`)")
                }
            }

        val MIGRATION_9_10 =
            object : Migration(9, 10) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Extend food_items table with OpenFoodFacts integration fields
                    db.execSQL(
                        """
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
                        """.trimIndent(),
                    )

                    // Copy existing data
                    db.execSQL(
                        """
                        INSERT INTO food_items_new (id, name, barcode, calories, carbs, protein, fat, createdAt)
                        SELECT id, name, barcode, calories, carbs, protein, fat, createdAt FROM food_items
                        """.trimIndent(),
                    )

                    // Drop old table and rename new table
                    db.execSQL("DROP TABLE food_items")
                    db.execSQL("ALTER TABLE food_items_new RENAME TO food_items")

                    // Recreate indices for food_items table
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_name` ON `food_items` (`name`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_barcode` ON `food_items` (`barcode`)")
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_food_items_categories` ON `food_items` (`categories`)",
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_brands` ON `food_items` (`brands`)")

                    // Create BMI history table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `bmi_history` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `date` TEXT NOT NULL,
                            `height` REAL NOT NULL,
                            `weight` REAL NOT NULL,
                            `bmi` REAL NOT NULL,
                            `category` TEXT NOT NULL,
                            `notes` TEXT,
                            `recordedAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Create weight loss programs table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `weight_loss_programs` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `startDate` TEXT NOT NULL,
                            `endDate` TEXT,
                            `startWeight` REAL NOT NULL,
                            `targetWeight` REAL NOT NULL,
                            `currentWeight` REAL NOT NULL,
                            `dailyCalorieTarget` INTEGER NOT NULL,
                            `weeklyWeightLossGoal` REAL NOT NULL,
                            `isActive` INTEGER NOT NULL,
                            `programType` TEXT NOT NULL,
                            `createdAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Create behavioral check-ins table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `behavioral_check_ins` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `moodScore` INTEGER NOT NULL,
                            `hungerLevel` INTEGER NOT NULL,
                            `stressLevel` INTEGER NOT NULL,
                            `sleepQuality` INTEGER,
                            `triggers` TEXT NOT NULL,
                            `copingStrategy` TEXT,
                            `mealContext` TEXT
                        )
                        """.trimIndent(),
                    )

                    // Create progress photos table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `progress_photos` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `filePath` TEXT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `weight` REAL NOT NULL,
                            `bmi` REAL NOT NULL,
                            `notes` TEXT
                        )
                        """.trimIndent(),
                    )

                    // Add indices for new tables
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_bmi_history_date` ON `bmi_history` (`date`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_bmi_history_bmi` ON `bmi_history` (`bmi`)")
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_bmi_history_recordedAt` ON `bmi_history` (`recordedAt`)",
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_weight_loss_programs_startDate` ON `weight_loss_programs` (`startDate`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_weight_loss_programs_isActive` ON `weight_loss_programs` (`isActive`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_weight_loss_programs_programType` ON `weight_loss_programs` (`programType`)",
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_behavioral_check_ins_timestamp` ON `behavioral_check_ins` (`timestamp`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_behavioral_check_ins_moodScore` ON `behavioral_check_ins` (`moodScore`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_behavioral_check_ins_stressLevel` ON `behavioral_check_ins` (`stressLevel`)",
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_progress_photos_timestamp` ON `progress_photos` (`timestamp`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_progress_photos_weight` ON `progress_photos` (`weight`)",
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_photos_bmi` ON `progress_photos` (`bmi`)")
                }
            }

        val MIGRATION_10_11 =
            object : Migration(10, 11) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create workout_performance table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `workout_performance` (
                            `id` TEXT NOT NULL,
                            `exerciseId` TEXT NOT NULL,
                            `sessionId` TEXT NOT NULL,
                            `planId` INTEGER NOT NULL,
                            `exerciseIndex` INTEGER NOT NULL,
                            `heartRateAvg` INTEGER,
                            `heartRateMax` INTEGER,
                            `heartRateZone` TEXT,
                            `reps` INTEGER NOT NULL,
                            `weight` REAL NOT NULL,
                            `volume` REAL NOT NULL,
                            `restTime` INTEGER NOT NULL,
                            `actualRestTime` INTEGER NOT NULL,
                            `formQuality` REAL NOT NULL DEFAULT 1.0,
                            `perceivedExertion` INTEGER,
                            `movementSpeed` REAL,
                            `rangeOfMotion` REAL,
                            `timestamp` INTEGER NOT NULL,
                            `duration` INTEGER NOT NULL,
                            `isPersonalRecord` INTEGER NOT NULL DEFAULT 0,
                            `notes` TEXT,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create workout_sessions table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `workout_sessions` (
                            `id` TEXT NOT NULL,
                            `planId` INTEGER NOT NULL,
                            `userId` TEXT NOT NULL,
                            `startTime` INTEGER NOT NULL,
                            `endTime` INTEGER,
                            `totalVolume` REAL NOT NULL DEFAULT 0.0,
                            `averageHeartRate` INTEGER,
                            `caloriesBurned` INTEGER,
                            `workoutEfficiencyScore` REAL NOT NULL DEFAULT 0.0,
                            `fatigueLevel` TEXT NOT NULL DEFAULT 'medium',
                            `personalRecordsAchieved` INTEGER NOT NULL DEFAULT 0,
                            `completionPercentage` REAL NOT NULL DEFAULT 0.0,
                            `sessionRating` INTEGER,
                            `sessionNotes` TEXT,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create exercise_progressions table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `exercise_progressions` (
                            `id` TEXT NOT NULL,
                            `exerciseId` TEXT NOT NULL,
                            `userId` TEXT NOT NULL,
                            `currentWeight` REAL NOT NULL,
                            `recommendedWeight` REAL NOT NULL,
                            `currentReps` INTEGER NOT NULL,
                            `recommendedReps` INTEGER NOT NULL,
                            `progressionReason` TEXT NOT NULL,
                            `performanceTrend` TEXT NOT NULL DEFAULT 'stable',
                            `plateauDetected` INTEGER NOT NULL DEFAULT 0,
                            `plateauWeeks` INTEGER NOT NULL DEFAULT 0,
                            `lastProgressDate` INTEGER NOT NULL,
                            `aiConfidence` REAL NOT NULL DEFAULT 0.5,
                            `nextReviewDate` INTEGER NOT NULL,
                            `adaptationNotes` TEXT,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Add indices for workout_performance table
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_performance_exerciseId` ON `workout_performance` (`exerciseId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_performance_sessionId` ON `workout_performance` (`sessionId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_performance_planId` ON `workout_performance` (`planId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_performance_timestamp` ON `workout_performance` (`timestamp`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_performance_exerciseIndex` ON `workout_performance` (`exerciseIndex`)",
                    )

                    // Add indices for workout_sessions table
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_sessions_planId` ON `workout_sessions` (`planId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_sessions_userId` ON `workout_sessions` (`userId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_sessions_startTime` ON `workout_sessions` (`startTime`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workout_sessions_workoutEfficiencyScore` ON `workout_sessions` (`workoutEfficiencyScore`)",
                    )

                    // Add indices for exercise_progressions table
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_exercise_progressions_exerciseId` ON `exercise_progressions` (`exerciseId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_exercise_progressions_userId` ON `exercise_progressions` (`userId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_exercise_progressions_performanceTrend` ON `exercise_progressions` (`performanceTrend`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_exercise_progressions_plateauDetected` ON `exercise_progressions` (`plateauDetected`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_exercise_progressions_lastProgressDate` ON `exercise_progressions` (`lastProgressDate`)",
                    )
                }
            }

        val MIGRATION_11_12 =
            object : Migration(11, 12) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create cooking_sessions table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `cooking_sessions` (
                            `id` TEXT NOT NULL,
                            `recipeId` TEXT NOT NULL,
                            `startTime` INTEGER NOT NULL,
                            `endTime` INTEGER,
                            `status` TEXT NOT NULL DEFAULT 'active',
                            `currentStep` INTEGER NOT NULL DEFAULT 0,
                            `totalSteps` INTEGER NOT NULL,
                            `estimatedDuration` INTEGER,
                            `actualDuration` INTEGER,
                            `notes` TEXT,
                            `createdAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create cooking_timers table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `cooking_timers` (
                            `id` TEXT NOT NULL,
                            `sessionId` TEXT NOT NULL,
                            `stepIndex` INTEGER NOT NULL,
                            `name` TEXT NOT NULL,
                            `durationSeconds` INTEGER NOT NULL,
                            `remainingSeconds` INTEGER NOT NULL,
                            `isActive` INTEGER NOT NULL DEFAULT 0,
                            `isPaused` INTEGER NOT NULL DEFAULT 0,
                            `startTime` INTEGER,
                            `completedAt` INTEGER,
                            `createdAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`),
                            FOREIGN KEY(`sessionId`) REFERENCES `cooking_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Add indices for cooking_sessions table
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cooking_sessions_recipeId` ON `cooking_sessions` (`recipeId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cooking_sessions_startTime` ON `cooking_sessions` (`startTime`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cooking_sessions_status` ON `cooking_sessions` (`status`)",
                    )

                    // Add indices for cooking_timers table
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cooking_timers_sessionId` ON `cooking_timers` (`sessionId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cooking_timers_stepIndex` ON `cooking_timers` (`stepIndex`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cooking_timers_isActive` ON `cooking_timers` (`isActive`)",
                    )
                }
            }

        val MIGRATION_12_13 =
            object : Migration(12, 13) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create Cloud Sync tables

                    // Create cloud_sync_metadata table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `cloud_sync_metadata` (
                            `id` TEXT NOT NULL,
                            `entityType` TEXT NOT NULL,
                            `entityId` TEXT NOT NULL,
                            `lastSyncTime` INTEGER NOT NULL,
                            `lastModifiedTime` INTEGER NOT NULL,
                            `syncStatus` TEXT NOT NULL,
                            `deviceId` TEXT NOT NULL,
                            `cloudVersion` TEXT,
                            `conflictData` TEXT,
                            `retryCount` INTEGER NOT NULL DEFAULT 0,
                            `errorMessage` TEXT,
                            `createdAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create user_profiles table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `user_profiles` (
                            `id` TEXT NOT NULL,
                            `userId` TEXT NOT NULL,
                            `email` TEXT NOT NULL,
                            `displayName` TEXT,
                            `deviceName` TEXT NOT NULL,
                            `deviceId` TEXT NOT NULL,
                            `lastSyncTime` INTEGER NOT NULL,
                            `syncPreferences` TEXT NOT NULL,
                            `encryptionKey` TEXT,
                            `isActive` INTEGER NOT NULL DEFAULT 1,
                            `createdAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create sync_conflicts table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `sync_conflicts` (
                            `id` TEXT NOT NULL,
                            `entityType` TEXT NOT NULL,
                            `entityId` TEXT NOT NULL,
                            `localData` TEXT NOT NULL,
                            `remoteData` TEXT NOT NULL,
                            `localTimestamp` INTEGER NOT NULL,
                            `remoteTimestamp` INTEGER NOT NULL,
                            `status` TEXT NOT NULL,
                            `resolution` TEXT,
                            `resolvedData` TEXT,
                            `resolvedBy` TEXT,
                            `createdAt` INTEGER NOT NULL,
                            `resolvedAt` INTEGER,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create Health Connect tables

                    // Health Connect Steps table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `health_connect_steps` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `date` TEXT NOT NULL,
                            `steps` INTEGER NOT NULL,
                            `source` TEXT NOT NULL,
                            `syncedAt` INTEGER NOT NULL,
                            `lastModified` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Health Connect Heart Rate table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `health_connect_heart_rate` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `date` TEXT NOT NULL,
                            `heartRate` INTEGER NOT NULL,
                            `source` TEXT NOT NULL,
                            `syncedAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Health Connect Calories table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `health_connect_calories` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `date` TEXT NOT NULL,
                            `calories` REAL NOT NULL,
                            `calorieType` TEXT NOT NULL,
                            `source` TEXT NOT NULL,
                            `syncedAt` INTEGER NOT NULL,
                            `lastModified` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Health Connect Sleep table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `health_connect_sleep` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `date` TEXT NOT NULL,
                            `startTime` INTEGER NOT NULL,
                            `endTime` INTEGER NOT NULL,
                            `durationMinutes` INTEGER NOT NULL,
                            `sleepStage` TEXT NOT NULL,
                            `source` TEXT NOT NULL,
                            `syncedAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Health Connect Exercise Sessions table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `health_connect_exercise_sessions` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `sessionId` TEXT NOT NULL,
                            `date` TEXT NOT NULL,
                            `startTime` INTEGER NOT NULL,
                            `endTime` INTEGER NOT NULL,
                            `durationMinutes` INTEGER NOT NULL,
                            `exerciseType` TEXT NOT NULL,
                            `title` TEXT NOT NULL,
                            `calories` REAL,
                            `avgHeartRate` INTEGER,
                            `maxHeartRate` INTEGER,
                            `distance` REAL,
                            `source` TEXT NOT NULL,
                            `syncedAt` INTEGER NOT NULL,
                            `lastModified` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Add indices for cloud_sync_metadata table
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_cloud_sync_metadata_entityType_entityId` ON `cloud_sync_metadata` (`entityType`, `entityId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cloud_sync_metadata_lastSyncTime` ON `cloud_sync_metadata` (`lastSyncTime`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cloud_sync_metadata_syncStatus` ON `cloud_sync_metadata` (`syncStatus`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_cloud_sync_metadata_deviceId` ON `cloud_sync_metadata` (`deviceId`)",
                    )

                    // Add indices for user_profiles table
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_user_profiles_userId` ON `user_profiles` (`userId`)",
                    )
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_user_profiles_email` ON `user_profiles` (`email`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_user_profiles_lastSyncTime` ON `user_profiles` (`lastSyncTime`)",
                    )

                    // Add indices for sync_conflicts table
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_sync_conflicts_entityType_entityId` ON `sync_conflicts` (`entityType`, `entityId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_sync_conflicts_createdAt` ON `sync_conflicts` (`createdAt`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_sync_conflicts_status` ON `sync_conflicts` (`status`)",
                    )

                    // Add indices for Health Connect tables
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_steps_date` ON `health_connect_steps` (`date`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_steps_source` ON `health_connect_steps` (`source`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_steps_syncedAt` ON `health_connect_steps` (`syncedAt`)",
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_heart_rate_date` ON `health_connect_heart_rate` (`date`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_heart_rate_timestamp` ON `health_connect_heart_rate` (`timestamp`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_heart_rate_source` ON `health_connect_heart_rate` (`source`)",
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_calories_date` ON `health_connect_calories` (`date`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_calories_calorieType` ON `health_connect_calories` (`calorieType`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_calories_source` ON `health_connect_calories` (`source`)",
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_sleep_date` ON `health_connect_sleep` (`date`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_sleep_source` ON `health_connect_sleep` (`source`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_sleep_sleepStage` ON `health_connect_sleep` (`sleepStage`)",
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_exercise_sessions_date` ON `health_connect_exercise_sessions` (`date`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_exercise_sessions_exerciseType` ON `health_connect_exercise_sessions` (`exerciseType`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_health_connect_exercise_sessions_source` ON `health_connect_exercise_sessions` (`source`)",
                    )
                }
            }

        val MIGRATION_13_14 =
            object : Migration(13, 14) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Add Social Challenge and Badge system tables

                    // Create social_challenges table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `social_challenges` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `description` TEXT NOT NULL,
                            `category` TEXT NOT NULL,
                            `challengeType` TEXT NOT NULL,
                            `targetMetric` TEXT NOT NULL,
                            `targetValue` REAL NOT NULL,
                            `unit` TEXT NOT NULL,
                            `duration` INTEGER NOT NULL,
                            `startDate` TEXT NOT NULL,
                            `endDate` TEXT NOT NULL,
                            `maxParticipants` INTEGER,
                            `currentParticipants` INTEGER NOT NULL DEFAULT 0,
                            `status` TEXT NOT NULL,
                            `creatorId` TEXT,
                            `reward` TEXT,
                            `difficulty` TEXT NOT NULL,
                            `imageUrl` TEXT,
                            `rules` TEXT,
                            `isOfficial` INTEGER NOT NULL DEFAULT 0,
                            `createdAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Create challenge_participations table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `challenge_participations` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `challengeId` INTEGER NOT NULL,
                            `userId` TEXT NOT NULL,
                            `userName` TEXT,
                            `status` TEXT NOT NULL,
                            `currentProgress` REAL NOT NULL DEFAULT 0.0,
                            `progressPercentage` REAL NOT NULL DEFAULT 0.0,
                            `lastActivityDate` TEXT,
                            `completedAt` INTEGER,
                            `joinedAt` INTEGER NOT NULL,
                            `rank` INTEGER,
                            `personalBest` REAL,
                            `notes` TEXT,
                            FOREIGN KEY(`challengeId`) REFERENCES `social_challenges`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create challenge_progress_logs table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `challenge_progress_logs` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `participationId` INTEGER NOT NULL,
                            `logDate` TEXT NOT NULL,
                            `value` REAL NOT NULL,
                            `description` TEXT,
                            `source` TEXT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            FOREIGN KEY(`participationId`) REFERENCES `challenge_participations`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create social_badges table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `social_badges` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `description` TEXT NOT NULL,
                            `category` TEXT NOT NULL,
                            `badgeType` TEXT NOT NULL,
                            `iconName` TEXT NOT NULL,
                            `rarity` TEXT NOT NULL,
                            `requirements` TEXT NOT NULL,
                            `challengeId` INTEGER,
                            `isUnlocked` INTEGER NOT NULL DEFAULT 0,
                            `unlockedAt` INTEGER,
                            `progress` REAL NOT NULL DEFAULT 0.0,
                            `createdAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Create leaderboard_entries table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `leaderboard_entries` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `challengeId` INTEGER NOT NULL,
                            `userId` TEXT NOT NULL,
                            `userName` TEXT,
                            `rank` INTEGER NOT NULL,
                            `score` REAL NOT NULL,
                            `completionTime` INTEGER,
                            `badge` TEXT,
                            `lastUpdated` INTEGER NOT NULL,
                            FOREIGN KEY(`challengeId`) REFERENCES `social_challenges`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Add indexes for social_challenges
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_challenges_status` ON `social_challenges` (`status`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_challenges_category` ON `social_challenges` (`category`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_challenges_startDate` ON `social_challenges` (`startDate`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_challenges_endDate` ON `social_challenges` (`endDate`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_challenges_createdAt` ON `social_challenges` (`createdAt`)",
                    )

                    // Add indexes for challenge_participations
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_challenge_participations_challengeId` ON `challenge_participations` (`challengeId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_challenge_participations_userId` ON `challenge_participations` (`userId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_challenge_participations_status` ON `challenge_participations` (`status`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_challenge_participations_joinedAt` ON `challenge_participations` (`joinedAt`)",
                    )

                    // Add indexes for challenge_progress_logs
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_challenge_progress_logs_participationId` ON `challenge_progress_logs` (`participationId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_challenge_progress_logs_logDate` ON `challenge_progress_logs` (`logDate`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_challenge_progress_logs_timestamp` ON `challenge_progress_logs` (`timestamp`)",
                    )

                    // Add indexes for social_badges
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_badges_category` ON `social_badges` (`category`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_badges_badgeType` ON `social_badges` (`badgeType`)",
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_social_badges_rarity` ON `social_badges` (`rarity`)")
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_badges_isUnlocked` ON `social_badges` (`isUnlocked`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_social_badges_unlockedAt` ON `social_badges` (`unlockedAt`)",
                    )

                    // Add indexes for leaderboard_entries
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_leaderboard_entries_challengeId` ON `leaderboard_entries` (`challengeId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_leaderboard_entries_userId` ON `leaderboard_entries` (`userId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_leaderboard_entries_rank` ON `leaderboard_entries` (`rank`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_leaderboard_score` ON `leaderboard_entries` (`score`)",
                    )

                    // Add new columns to personal_achievements table for enhanced badge system
                    addColumnIfNotExists(db, "personal_achievements", "badgeType", "TEXT")
                    addColumnIfNotExists(db, "personal_achievements", "rarity", "TEXT")
                    addColumnIfNotExists(db, "personal_achievements", "socialVisible", "INTEGER NOT NULL DEFAULT 1")
                    addColumnIfNotExists(db, "personal_achievements", "challengeId", "INTEGER")
                    addColumnIfNotExists(db, "personal_achievements", "shareMessage", "TEXT")
                    addColumnIfNotExists(db, "personal_achievements", "pointsValue", "INTEGER NOT NULL DEFAULT 0")
                }
            }

        val MIGRATION_14_15 =
            object : Migration(14, 15) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // No-op migration to maintain continuous migration chain
                }
            }

        val MIGRATION_15_16 =
            object : Migration(15, 16) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Enhance recipes table with YAZIO features
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recipes_new` (
                            `id` TEXT NOT NULL,
                            `title` TEXT NOT NULL,
                            `description` TEXT NOT NULL,
                            `markdown` TEXT NOT NULL,
                            `imageUrl` TEXT,
                            `prepTime` INTEGER,
                            `cookTime` INTEGER,
                            `servings` INTEGER,
                            `difficulty` TEXT,
                            `categories` TEXT,
                            `calories` INTEGER,
                            `protein` REAL,
                            `carbs` REAL,
                            `fat` REAL,
                            `fiber` REAL,
                            `sugar` REAL,
                            `sodium` REAL,
                            `createdAt` INTEGER NOT NULL,
                            `isOfficial` INTEGER NOT NULL DEFAULT 0,
                            `rating` REAL NOT NULL DEFAULT 0.0,
                            `ratingCount` INTEGER NOT NULL DEFAULT 0,
                            `isLocalOnly` INTEGER NOT NULL DEFAULT 1,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Copy existing data
                    db.execSQL(
                        """
                        INSERT INTO recipes_new (id, title, description, markdown, imageUrl, calories, createdAt)
                        SELECT id, title, '', markdown, imageUrl, calories, createdAt FROM recipes
                        """.trimIndent(),
                    )

                    // Drop old table and rename new table
                    db.execSQL("DROP TABLE recipes")
                    db.execSQL("ALTER TABLE recipes_new RENAME TO recipes")

                    // Create YAZIO-style meals table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `meals` (
                            `id` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `description` TEXT,
                            `mealType` TEXT NOT NULL,
                            `foods` TEXT NOT NULL,
                            `totalCalories` INTEGER NOT NULL,
                            `totalProtein` REAL NOT NULL,
                            `totalCarbs` REAL NOT NULL,
                            `totalFat` REAL NOT NULL,
                            `createdAt` INTEGER NOT NULL,
                            `lastUsedAt` INTEGER,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create recipe ingredients table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recipe_ingredients` (
                            `id` TEXT NOT NULL,
                            `recipeId` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `amount` REAL NOT NULL,
                            `unit` TEXT NOT NULL,
                            `ingredientOrder` INTEGER NOT NULL,
                            `isOptional` INTEGER NOT NULL DEFAULT 0,
                            `preparationNote` TEXT,
                            `category` TEXT,
                            PRIMARY KEY(`id`),
                            FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create recipe steps table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recipe_steps` (
                            `id` TEXT NOT NULL,
                            `recipeId` TEXT NOT NULL,
                            `stepOrder` INTEGER NOT NULL,
                            `instruction` TEXT NOT NULL,
                            `estimatedTimeMinutes` INTEGER,
                            `temperature` TEXT,
                            `timerName` TEXT,
                            `timerDurationSeconds` INTEGER,
                            `imageUrl` TEXT,
                            `tips` TEXT,
                            PRIMARY KEY(`id`),
                            FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create enhanced grocery lists table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `grocery_lists` (
                            `id` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `description` TEXT,
                            `isActive` INTEGER NOT NULL DEFAULT 1,
                            `isDefault` INTEGER NOT NULL DEFAULT 0,
                            `createdAt` INTEGER NOT NULL,
                            `lastModifiedAt` INTEGER NOT NULL,
                            `completedAt` INTEGER,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create grocery items table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `grocery_items` (
                            `id` TEXT NOT NULL,
                            `listId` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `quantity` REAL,
                            `unit` TEXT,
                            `category` TEXT NOT NULL,
                            `checked` INTEGER NOT NULL DEFAULT 0,
                            `fromRecipeId` TEXT,
                            `fromRecipeName` TEXT,
                            `estimatedPrice` REAL,
                            `notes` TEXT,
                            `addedAt` INTEGER NOT NULL,
                            `checkedAt` INTEGER,
                            PRIMARY KEY(`id`),
                            FOREIGN KEY(`listId`) REFERENCES `grocery_lists`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create recipe analytics table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recipe_analytics` (
                            `id` TEXT NOT NULL,
                            `recipeId` TEXT NOT NULL,
                            `eventType` TEXT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `sessionId` TEXT,
                            `metadata` TEXT,
                            PRIMARY KEY(`id`),
                            FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create recipe ratings table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recipe_ratings` (
                            `id` TEXT NOT NULL,
                            `recipeId` TEXT NOT NULL,
                            `rating` REAL NOT NULL,
                            `comment` TEXT,
                            `userId` TEXT,
                            `createdAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`),
                            FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create PRO feature access table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `pro_feature_access` (
                            `featureName` TEXT NOT NULL,
                            `isUnlocked` INTEGER NOT NULL DEFAULT 0,
                            `unlockedAt` INTEGER,
                            `lastChecked` INTEGER NOT NULL,
                            `usageCount` INTEGER NOT NULL DEFAULT 0,
                            `maxUsage` INTEGER,
                            PRIMARY KEY(`featureName`)
                        )
                        """.trimIndent(),
                    )

                    // Create recipe collections table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recipe_collections` (
                            `id` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `description` TEXT,
                            `imageUrl` TEXT,
                            `isOfficial` INTEGER NOT NULL DEFAULT 0,
                            `isPremium` INTEGER NOT NULL DEFAULT 0,
                            `sortOrder` INTEGER NOT NULL DEFAULT 0,
                            `createdAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    // Create recipe collection items table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recipe_collection_items` (
                            `id` TEXT NOT NULL,
                            `collectionId` TEXT NOT NULL,
                            `recipeId` TEXT NOT NULL,
                            `sortOrder` INTEGER NOT NULL DEFAULT 0,
                            `addedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`),
                            FOREIGN KEY(`collectionId`) REFERENCES `recipe_collections`(`id`) ON DELETE CASCADE,
                            FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Update shopping_list_categories with new fields
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `shopping_list_categories_new` (
                            `name` TEXT NOT NULL,
                            `order` INTEGER NOT NULL,
                            `iconName` TEXT,
                            `colorHex` TEXT,
                            PRIMARY KEY(`name`)
                        )
                        """.trimIndent(),
                    )

                    db.execSQL(
                        """
                        INSERT INTO shopping_list_categories_new (name, `order`)
                        SELECT name, `order` FROM shopping_list_categories
                        """.trimIndent(),
                    )

                    db.execSQL("DROP TABLE shopping_list_categories")
                    db.execSQL("ALTER TABLE shopping_list_categories_new RENAME TO shopping_list_categories")

                    // Add all the necessary indices
                    addYazioIndices(db)
                }

                private fun addYazioIndices(db: SupportSQLiteDatabase) {
                    val indices =
                        listOf(
                            // Recipe indices
                            "CREATE INDEX IF NOT EXISTS `index_recipes_difficulty` ON `recipes` (`difficulty`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipes_prepTime` ON `recipes` (`prepTime`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipes_cookTime` ON `recipes` (`cookTime`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipes_createdAt` ON `recipes` (`createdAt`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipes_calories` ON `recipes` (`calories`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipes_title` ON `recipes` (`title`)",
                            // Meal indices
                            "CREATE INDEX IF NOT EXISTS `index_meals_name` ON `meals` (`name`)",
                            "CREATE INDEX IF NOT EXISTS `index_meals_mealType` ON `meals` (`mealType`)",
                            "CREATE INDEX IF NOT EXISTS `index_meals_createdAt` ON `meals` (`createdAt`)",
                            // Recipe ingredients indices
                            "CREATE INDEX IF NOT EXISTS `index_recipe_ingredients_recipeId` ON `recipe_ingredients` (`recipeId`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_ingredients_ingredientOrder` ON `recipe_ingredients` (`ingredientOrder`)",
                            // Recipe steps indices
                            "CREATE INDEX IF NOT EXISTS `index_recipe_steps_recipeId` ON `recipe_steps` (`recipeId`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_steps_stepOrder` ON `recipe_steps` (`stepOrder`)",
                            // Grocery lists indices
                            "CREATE INDEX IF NOT EXISTS `index_grocery_lists_name` ON `grocery_lists` (`name`)",
                            "CREATE INDEX IF NOT EXISTS `index_grocery_lists_createdAt` ON `grocery_lists` (`createdAt`)",
                            "CREATE INDEX IF NOT EXISTS `index_grocery_lists_isActive` ON `grocery_lists` (`isActive`)",
                            // Grocery items indices
                            "CREATE INDEX IF NOT EXISTS `index_grocery_items_listId` ON `grocery_items` (`listId`)",
                            "CREATE INDEX IF NOT EXISTS `index_grocery_items_category` ON `grocery_items` (`category`)",
                            "CREATE INDEX IF NOT EXISTS `index_grocery_items_checked` ON `grocery_items` (`checked`)",
                            "CREATE INDEX IF NOT EXISTS `index_grocery_items_fromRecipeId` ON `grocery_items` (`fromRecipeId`)",
                            // Recipe analytics indices
                            "CREATE INDEX IF NOT EXISTS `index_recipe_analytics_recipeId` ON `recipe_analytics` (`recipeId`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_analytics_eventType` ON `recipe_analytics` (`eventType`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_analytics_timestamp` ON `recipe_analytics` (`timestamp`)",
                            // Recipe ratings indices
                            "CREATE INDEX IF NOT EXISTS `index_recipe_ratings_recipeId` ON `recipe_ratings` (`recipeId`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_ratings_rating` ON `recipe_ratings` (`rating`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_ratings_createdAt` ON `recipe_ratings` (`createdAt`)",
                            // PRO feature indices
                            "CREATE INDEX IF NOT EXISTS `index_pro_feature_access_featureName` ON `pro_feature_access` (`featureName`)",
                            "CREATE INDEX IF NOT EXISTS `index_pro_feature_access_isUnlocked` ON `pro_feature_access` (`isUnlocked`)",
                            "CREATE INDEX IF NOT EXISTS `index_pro_feature_access_lastChecked` ON `pro_feature_access` (`lastChecked`)",
                            // Recipe collections indices
                            "CREATE INDEX IF NOT EXISTS `index_recipe_collections_name` ON `recipe_collections` (`name`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_collections_isOfficial` ON `recipe_collections` (`isOfficial`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_collections_createdAt` ON `recipe_collections` (`createdAt`)",
                            // Recipe collection items indices
                            "CREATE INDEX IF NOT EXISTS `index_recipe_collection_items_collectionId` ON `recipe_collection_items` (`collectionId`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_collection_items_recipeId` ON `recipe_collection_items` (`recipeId`)",
                            "CREATE INDEX IF NOT EXISTS `index_recipe_collection_items_sortOrder` ON `recipe_collection_items` (`sortOrder`)",
                        )

                    indices.forEach { indexSql ->
                        try {
                            db.execSQL(indexSql)
                        } catch (e: Exception) {
                            android.util.Log.w("Migration", "Failed to create index: $indexSql", e)
                        }
                    }
                }
            }

        val MIGRATION_16_17 =
            object : Migration(16, 17) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Add recipe support to meal_entries for recipe-to-diary functionality

                    // Create new meal_entries table with recipe support
                    db.execSQL(
                        """
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
                """,
                    )

                    // Copy existing data (all existing entries are food items)
                    db.execSQL(
                        """
                    INSERT INTO meal_entries_new (id, foodItemId, recipeId, date, mealType, quantityGrams, servings, notes)
                    SELECT id, foodItemId, NULL, date, mealType, quantityGrams, NULL, notes
                    FROM meal_entries
                """,
                    )

                    // Drop old table and rename new one
                    db.execSQL("DROP TABLE meal_entries")
                    db.execSQL("ALTER TABLE meal_entries_new RENAME TO meal_entries")

                    // Recreate indices
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_meal_entries_foodItemId` ON `meal_entries` (`foodItemId`)",
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_date` ON `meal_entries` (`date`)")
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_meal_entries_mealType` ON `meal_entries` (`mealType`)",
                    )

                    android.util.Log.i("Migration", "Successfully migrated meal_entries to support recipes")
                }
            }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context)
            }

        private fun buildDatabase(context: Context): AppDatabase {
            return try {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp.db")
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17)
                    .apply {
                        // Only allow destructive migration in debug builds
                        if (com.example.fitapp.BuildConfig.DEBUG) {
                            fallbackToDestructiveMigration(true)
                        }
                    }
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // Enable WAL mode for better performance
                    .setQueryCallback({ sqlQuery, bindArgs ->
                        // Log slow queries for performance monitoring
                        android.util.Log.d("Database", "Query: $sqlQuery Args: $bindArgs")
                    }, java.util.concurrent.Executors.newSingleThreadExecutor())
                    .addCallback(
                        object : Callback() {
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
                        },
                    )
                    .build().also {
                        INSTANCE = it
                        android.util.Log.i("AppDatabase", "Database initialized successfully")
                    }
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Failed to create database", e)
                // Create a new database with fallback
                try {
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitapp_fallback.db")
                        .fallbackToDestructiveMigration(true)
                        .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
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
            val indicesToCreate =
                listOf(
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
                    "CREATE INDEX IF NOT EXISTS index_recipes_title ON recipes(title)",
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

            android.util.Log.i(
                "AppDatabase",
                "Performance indices creation completed: $successCount successful, $failureCount failed",
            )
        }

        /**
         * Safely adds a column to a table if it doesn't already exist.
         * This makes ALTER TABLE ADD COLUMN operations idempotent.
         */
        private fun addColumnIfNotExists(
            db: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnDefinition: String,
        ) {
            try {
                // Check if column already exists
                db.query("PRAGMA table_info($tableName)").use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        if (cursor.getString(nameIndex) == columnName) {
                            android.util.Log.d("AppDatabase", "Column $tableName.$columnName already exists, skipping")
                            return
                        }
                    }
                }

                // Column doesn't exist, add it
                db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDefinition")
                android.util.Log.d("AppDatabase", "Added column $tableName.$columnName")
            } catch (e: Exception) {
                android.util.Log.w("AppDatabase", "Failed to add column $tableName.$columnName", e)
                // Don't rethrow - this allows migrations to continue
            }
        }
    }
}
