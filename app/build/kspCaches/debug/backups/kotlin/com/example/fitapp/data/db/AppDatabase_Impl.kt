package com.example.fitapp.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _aiLogDao: Lazy<AiLogDao> = lazy {
    AiLogDao_Impl(this)
  }

  private val _recipeDao: Lazy<RecipeDao> = lazy {
    RecipeDao_Impl(this)
  }

  private val _intakeDao: Lazy<IntakeDao> = lazy {
    IntakeDao_Impl(this)
  }

  private val _goalDao: Lazy<GoalDao> = lazy {
    GoalDao_Impl(this)
  }

  private val _shoppingDao: Lazy<ShoppingDao> = lazy {
    ShoppingDao_Impl(this)
  }

  private val _planDao: Lazy<PlanDao> = lazy {
    PlanDao_Impl(this)
  }

  private val _savedRecipeDao: Lazy<SavedRecipeDao> = lazy {
    SavedRecipeDao_Impl(this)
  }

  private val _shoppingCategoryDao: Lazy<ShoppingCategoryDao> = lazy {
    ShoppingCategoryDao_Impl(this)
  }

  private val _todayWorkoutDao: Lazy<TodayWorkoutDao> = lazy {
    TodayWorkoutDao_Impl(this)
  }

  private val _personalAchievementDao: Lazy<PersonalAchievementDao> = lazy {
    PersonalAchievementDao_Impl(this)
  }

  private val _personalStreakDao: Lazy<PersonalStreakDao> = lazy {
    PersonalStreakDao_Impl(this)
  }

  private val _personalRecordDao: Lazy<PersonalRecordDao> = lazy {
    PersonalRecordDao_Impl(this)
  }

  private val _progressMilestoneDao: Lazy<ProgressMilestoneDao> = lazy {
    ProgressMilestoneDao_Impl(this)
  }

  private val _weightDao: Lazy<WeightDao> = lazy {
    WeightDao_Impl(this)
  }

  private val _foodItemDao: Lazy<FoodItemDao> = lazy {
    FoodItemDao_Impl(this)
  }

  private val _mealEntryDao: Lazy<MealEntryDao> = lazy {
    MealEntryDao_Impl(this)
  }

  private val _waterEntryDao: Lazy<WaterEntryDao> = lazy {
    WaterEntryDao_Impl(this)
  }

  private val _bMIHistoryDao: Lazy<BMIHistoryDao> = lazy {
    BMIHistoryDao_Impl(this)
  }

  private val _weightLossProgramDao: Lazy<WeightLossProgramDao> = lazy {
    WeightLossProgramDao_Impl(this)
  }

  private val _behavioralCheckInDao: Lazy<BehavioralCheckInDao> = lazy {
    BehavioralCheckInDao_Impl(this)
  }

  private val _progressPhotoDao: Lazy<ProgressPhotoDao> = lazy {
    ProgressPhotoDao_Impl(this)
  }

  private val _workoutPerformanceDao: Lazy<WorkoutPerformanceDao> = lazy {
    WorkoutPerformanceDao_Impl(this)
  }

  private val _workoutSessionDao: Lazy<WorkoutSessionDao> = lazy {
    WorkoutSessionDao_Impl(this)
  }

  private val _exerciseProgressionDao: Lazy<ExerciseProgressionDao> = lazy {
    ExerciseProgressionDao_Impl(this)
  }

  private val _cookingSessionDao: Lazy<CookingSessionDao> = lazy {
    CookingSessionDao_Impl(this)
  }

  private val _cookingTimerDao: Lazy<CookingTimerDao> = lazy {
    CookingTimerDao_Impl(this)
  }

  private val _healthStepsDao: Lazy<HealthStepsDao> = lazy {
    HealthStepsDao_Impl(this)
  }

  private val _healthHeartRateDao: Lazy<HealthHeartRateDao> = lazy {
    HealthHeartRateDao_Impl(this)
  }

  private val _healthCalorieDao: Lazy<HealthCalorieDao> = lazy {
    HealthCalorieDao_Impl(this)
  }

  private val _healthSleepDao: Lazy<HealthSleepDao> = lazy {
    HealthSleepDao_Impl(this)
  }

  private val _healthExerciseSessionDao: Lazy<HealthExerciseSessionDao> = lazy {
    HealthExerciseSessionDao_Impl(this)
  }

  private val _cloudSyncDao: Lazy<CloudSyncDao> = lazy {
    CloudSyncDao_Impl(this)
  }

  private val _userProfileDao: Lazy<UserProfileDao> = lazy {
    UserProfileDao_Impl(this)
  }

  private val _syncConflictDao: Lazy<SyncConflictDao> = lazy {
    SyncConflictDao_Impl(this)
  }

  private val _socialChallengeDao: Lazy<SocialChallengeDao> = lazy {
    SocialChallengeDao_Impl(this)
  }

  private val _challengeParticipationDao: Lazy<ChallengeParticipationDao> = lazy {
    ChallengeParticipationDao_Impl(this)
  }

  private val _challengeProgressLogDao: Lazy<ChallengeProgressLogDao> = lazy {
    ChallengeProgressLogDao_Impl(this)
  }

  private val _socialBadgeDao: Lazy<SocialBadgeDao> = lazy {
    SocialBadgeDao_Impl(this)
  }

  private val _leaderboardEntryDao: Lazy<LeaderboardEntryDao> = lazy {
    LeaderboardEntryDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(14,
        "d5a8043b8bdd9ed129a49a6e0ad9463b", "1ecfbf08cf73660c7b154cc606e736cf") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `ai_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ts` INTEGER NOT NULL, `type` TEXT NOT NULL, `provider` TEXT NOT NULL, `prompt` TEXT NOT NULL, `result` TEXT NOT NULL, `success` INTEGER NOT NULL, `tookMs` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `recipes` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `markdown` TEXT NOT NULL, `calories` INTEGER, `imageUrl` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_createdAt` ON `recipes` (`createdAt`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_calories` ON `recipes` (`calories`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_title` ON `recipes` (`title`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `recipe_favorites` (`recipeId` TEXT NOT NULL, `savedAt` INTEGER NOT NULL, PRIMARY KEY(`recipeId`), FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_recipe_favorites_recipeId` ON `recipe_favorites` (`recipeId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `recipe_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recipeId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_recipe_history_recipeId` ON `recipe_history` (`recipeId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `intake_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `label` TEXT NOT NULL, `kcal` INTEGER NOT NULL, `source` TEXT NOT NULL, `referenceId` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_intake_entries_timestamp` ON `intake_entries` (`timestamp`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_intake_entries_kcal` ON `intake_entries` (`kcal`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_intake_entries_timestamp_kcal` ON `intake_entries` (`timestamp`, `kcal`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `daily_goals` (`dateIso` TEXT NOT NULL, `targetKcal` INTEGER NOT NULL, `targetCarbs` REAL, `targetProtein` REAL, `targetFat` REAL, `targetWaterMl` INTEGER, PRIMARY KEY(`dateIso`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `shopping_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `quantity` TEXT, `unit` TEXT, `checked` INTEGER NOT NULL, `category` TEXT, `fromRecipeId` TEXT, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `training_plans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `goal` TEXT NOT NULL, `weeks` INTEGER NOT NULL, `sessionsPerWeek` INTEGER NOT NULL, `minutesPerSession` INTEGER NOT NULL, `equipment` TEXT NOT NULL, `trainingDays` TEXT, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `saved_recipes` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `markdown` TEXT NOT NULL, `calories` INTEGER, `imageUrl` TEXT, `ingredients` TEXT NOT NULL, `tags` TEXT NOT NULL, `prepTime` INTEGER, `difficulty` TEXT, `servings` INTEGER, `isFavorite` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `lastCookedAt` INTEGER, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `shopping_list_categories` (`name` TEXT NOT NULL, `order` INTEGER NOT NULL, PRIMARY KEY(`name`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `today_workouts` (`dateIso` TEXT NOT NULL, `content` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `completedAt` INTEGER, `planId` INTEGER, PRIMARY KEY(`dateIso`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_today_workouts_dateIso` ON `today_workouts` (`dateIso`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_today_workouts_status` ON `today_workouts` (`status`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_today_workouts_createdAt` ON `today_workouts` (`createdAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `personal_achievements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `iconName` TEXT NOT NULL, `targetValue` REAL, `currentValue` REAL NOT NULL, `unit` TEXT, `isCompleted` INTEGER NOT NULL, `completedAt` INTEGER, `createdAt` INTEGER NOT NULL, `badgeType` TEXT, `rarity` TEXT, `socialVisible` INTEGER NOT NULL, `challengeId` INTEGER, `shareMessage` TEXT, `pointsValue` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_achievements_category` ON `personal_achievements` (`category`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_achievements_isCompleted` ON `personal_achievements` (`isCompleted`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_achievements_createdAt` ON `personal_achievements` (`createdAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `personal_streaks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `currentStreak` INTEGER NOT NULL, `longestStreak` INTEGER NOT NULL, `lastActivityTimestamp` INTEGER, `isActive` INTEGER NOT NULL, `targetDays` INTEGER, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `personal_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `exerciseName` TEXT NOT NULL, `recordType` TEXT NOT NULL, `value` REAL NOT NULL, `unit` TEXT NOT NULL, `notes` TEXT, `achievedAt` INTEGER NOT NULL, `previousRecord` REAL, `improvement` REAL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `progress_milestones` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `targetValue` REAL NOT NULL, `currentValue` REAL NOT NULL, `unit` TEXT NOT NULL, `targetDate` TEXT, `isCompleted` INTEGER NOT NULL, `completedAt` INTEGER, `progress` REAL NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `weight_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `weight` REAL NOT NULL, `dateIso` TEXT NOT NULL, `notes` TEXT, `recordedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_dateIso` ON `weight_entries` (`dateIso`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_recordedAt` ON `weight_entries` (`recordedAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `food_items` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `barcode` TEXT, `calories` INTEGER NOT NULL, `carbs` REAL NOT NULL, `protein` REAL NOT NULL, `fat` REAL NOT NULL, `createdAt` INTEGER NOT NULL, `fiber` REAL, `sugar` REAL, `sodium` REAL, `brands` TEXT, `categories` TEXT, `imageUrl` TEXT, `servingSize` TEXT, `ingredients` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_name` ON `food_items` (`name`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_food_items_barcode` ON `food_items` (`barcode`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `meal_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `foodItemId` TEXT NOT NULL, `date` TEXT NOT NULL, `mealType` TEXT NOT NULL, `quantityGrams` REAL NOT NULL, `notes` TEXT, FOREIGN KEY(`foodItemId`) REFERENCES `food_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_foodItemId` ON `meal_entries` (`foodItemId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_date` ON `meal_entries` (`date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_mealType` ON `meal_entries` (`mealType`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `water_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `amountMl` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_water_entries_date` ON `water_entries` (`date`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `bmi_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `height` REAL NOT NULL, `weight` REAL NOT NULL, `bmi` REAL NOT NULL, `category` TEXT NOT NULL, `notes` TEXT, `recordedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_bmi_history_date` ON `bmi_history` (`date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_bmi_history_bmi` ON `bmi_history` (`bmi`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_bmi_history_recordedAt` ON `bmi_history` (`recordedAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `weight_loss_programs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT, `startWeight` REAL NOT NULL, `targetWeight` REAL NOT NULL, `currentWeight` REAL NOT NULL, `dailyCalorieTarget` INTEGER NOT NULL, `weeklyWeightLossGoal` REAL NOT NULL, `isActive` INTEGER NOT NULL, `programType` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_loss_programs_startDate` ON `weight_loss_programs` (`startDate`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_loss_programs_isActive` ON `weight_loss_programs` (`isActive`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_loss_programs_programType` ON `weight_loss_programs` (`programType`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `behavioral_check_ins` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `moodScore` INTEGER NOT NULL, `hungerLevel` INTEGER NOT NULL, `stressLevel` INTEGER NOT NULL, `sleepQuality` INTEGER, `triggers` TEXT NOT NULL, `copingStrategy` TEXT, `mealContext` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_behavioral_check_ins_timestamp` ON `behavioral_check_ins` (`timestamp`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_behavioral_check_ins_moodScore` ON `behavioral_check_ins` (`moodScore`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_behavioral_check_ins_stressLevel` ON `behavioral_check_ins` (`stressLevel`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `progress_photos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `filePath` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `weight` REAL NOT NULL, `bmi` REAL NOT NULL, `notes` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_photos_timestamp` ON `progress_photos` (`timestamp`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_photos_weight` ON `progress_photos` (`weight`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_photos_bmi` ON `progress_photos` (`bmi`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workout_performance` (`id` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `planId` INTEGER NOT NULL, `exerciseIndex` INTEGER NOT NULL, `heartRateAvg` INTEGER, `heartRateMax` INTEGER, `heartRateZone` TEXT, `reps` INTEGER NOT NULL, `weight` REAL NOT NULL, `volume` REAL NOT NULL, `restTime` INTEGER NOT NULL, `actualRestTime` INTEGER NOT NULL, `formQuality` REAL NOT NULL, `perceivedExertion` INTEGER, `movementSpeed` REAL, `rangeOfMotion` REAL, `timestamp` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `isPersonalRecord` INTEGER NOT NULL, `notes` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_performance_exerciseId` ON `workout_performance` (`exerciseId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_performance_sessionId` ON `workout_performance` (`sessionId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_performance_planId` ON `workout_performance` (`planId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_performance_timestamp` ON `workout_performance` (`timestamp`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_performance_exerciseIndex` ON `workout_performance` (`exerciseIndex`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` TEXT NOT NULL, `planId` INTEGER NOT NULL, `userId` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `totalVolume` REAL NOT NULL, `averageHeartRate` INTEGER, `caloriesBurned` INTEGER, `workoutEfficiencyScore` REAL NOT NULL, `fatigueLevel` TEXT NOT NULL, `personalRecordsAchieved` INTEGER NOT NULL, `completionPercentage` REAL NOT NULL, `sessionRating` INTEGER, `sessionNotes` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_planId` ON `workout_sessions` (`planId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_userId` ON `workout_sessions` (`userId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_startTime` ON `workout_sessions` (`startTime`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_workoutEfficiencyScore` ON `workout_sessions` (`workoutEfficiencyScore`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercise_progressions` (`id` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `userId` TEXT NOT NULL, `currentWeight` REAL NOT NULL, `recommendedWeight` REAL NOT NULL, `currentReps` INTEGER NOT NULL, `recommendedReps` INTEGER NOT NULL, `progressionReason` TEXT NOT NULL, `performanceTrend` TEXT NOT NULL, `plateauDetected` INTEGER NOT NULL, `plateauWeeks` INTEGER NOT NULL, `lastProgressDate` INTEGER NOT NULL, `aiConfidence` REAL NOT NULL, `nextReviewDate` INTEGER NOT NULL, `adaptationNotes` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_progressions_exerciseId` ON `exercise_progressions` (`exerciseId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_progressions_userId` ON `exercise_progressions` (`userId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_progressions_performanceTrend` ON `exercise_progressions` (`performanceTrend`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_progressions_plateauDetected` ON `exercise_progressions` (`plateauDetected`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_progressions_lastProgressDate` ON `exercise_progressions` (`lastProgressDate`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cooking_sessions` (`id` TEXT NOT NULL, `recipeId` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `status` TEXT NOT NULL, `currentStep` INTEGER NOT NULL, `totalSteps` INTEGER NOT NULL, `estimatedDuration` INTEGER, `actualDuration` INTEGER, `notes` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cooking_sessions_recipeId` ON `cooking_sessions` (`recipeId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cooking_sessions_startTime` ON `cooking_sessions` (`startTime`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cooking_sessions_status` ON `cooking_sessions` (`status`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cooking_timers` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `stepIndex` INTEGER NOT NULL, `name` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL, `remainingSeconds` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `isPaused` INTEGER NOT NULL, `startTime` INTEGER, `completedAt` INTEGER, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`sessionId`) REFERENCES `cooking_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cooking_timers_sessionId` ON `cooking_timers` (`sessionId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cooking_timers_stepIndex` ON `cooking_timers` (`stepIndex`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cooking_timers_isActive` ON `cooking_timers` (`isActive`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `health_connect_steps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `steps` INTEGER NOT NULL, `source` TEXT NOT NULL, `syncedAt` INTEGER NOT NULL, `lastModified` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_steps_date` ON `health_connect_steps` (`date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_steps_source` ON `health_connect_steps` (`source`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_steps_syncedAt` ON `health_connect_steps` (`syncedAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `health_connect_heart_rate` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `date` TEXT NOT NULL, `heartRate` INTEGER NOT NULL, `source` TEXT NOT NULL, `syncedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_heart_rate_date` ON `health_connect_heart_rate` (`date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_heart_rate_timestamp` ON `health_connect_heart_rate` (`timestamp`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_heart_rate_source` ON `health_connect_heart_rate` (`source`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `health_connect_calories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `calories` REAL NOT NULL, `calorieType` TEXT NOT NULL, `source` TEXT NOT NULL, `syncedAt` INTEGER NOT NULL, `lastModified` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_calories_date` ON `health_connect_calories` (`date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_calories_calorieType` ON `health_connect_calories` (`calorieType`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_calories_source` ON `health_connect_calories` (`source`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `health_connect_sleep` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `sleepStage` TEXT NOT NULL, `source` TEXT NOT NULL, `syncedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_sleep_date` ON `health_connect_sleep` (`date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_sleep_source` ON `health_connect_sleep` (`source`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_sleep_sleepStage` ON `health_connect_sleep` (`sleepStage`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `health_connect_exercise_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` TEXT NOT NULL, `date` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `exerciseType` TEXT NOT NULL, `title` TEXT NOT NULL, `calories` REAL, `avgHeartRate` INTEGER, `maxHeartRate` INTEGER, `distance` REAL, `source` TEXT NOT NULL, `syncedAt` INTEGER NOT NULL, `lastModified` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_exercise_sessions_date` ON `health_connect_exercise_sessions` (`date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_exercise_sessions_exerciseType` ON `health_connect_exercise_sessions` (`exerciseType`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_health_connect_exercise_sessions_source` ON `health_connect_exercise_sessions` (`source`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cloud_sync_metadata` (`id` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `lastSyncTime` INTEGER NOT NULL, `lastModifiedTime` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `cloudVersion` TEXT, `conflictData` TEXT, `retryCount` INTEGER NOT NULL, `errorMessage` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_cloud_sync_metadata_entityType_entityId` ON `cloud_sync_metadata` (`entityType`, `entityId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cloud_sync_metadata_lastSyncTime` ON `cloud_sync_metadata` (`lastSyncTime`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cloud_sync_metadata_syncStatus` ON `cloud_sync_metadata` (`syncStatus`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cloud_sync_metadata_deviceId` ON `cloud_sync_metadata` (`deviceId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_profiles` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `email` TEXT NOT NULL, `displayName` TEXT, `deviceName` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `lastSyncTime` INTEGER NOT NULL, `syncPreferences` TEXT NOT NULL, `encryptionKey` TEXT, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_profiles_userId` ON `user_profiles` (`userId`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_profiles_email` ON `user_profiles` (`email`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_user_profiles_lastSyncTime` ON `user_profiles` (`lastSyncTime`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sync_conflicts` (`id` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `localData` TEXT NOT NULL, `remoteData` TEXT NOT NULL, `localTimestamp` INTEGER NOT NULL, `remoteTimestamp` INTEGER NOT NULL, `status` TEXT NOT NULL, `resolution` TEXT, `resolvedData` TEXT, `resolvedBy` TEXT, `createdAt` INTEGER NOT NULL, `resolvedAt` INTEGER, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_conflicts_entityType_entityId` ON `sync_conflicts` (`entityType`, `entityId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_conflicts_createdAt` ON `sync_conflicts` (`createdAt`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_conflicts_status` ON `sync_conflicts` (`status`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `social_challenges` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `challengeType` TEXT NOT NULL, `targetMetric` TEXT NOT NULL, `targetValue` REAL NOT NULL, `unit` TEXT NOT NULL, `duration` INTEGER NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `maxParticipants` INTEGER, `currentParticipants` INTEGER NOT NULL, `status` TEXT NOT NULL, `creatorId` TEXT, `reward` TEXT, `difficulty` TEXT NOT NULL, `imageUrl` TEXT, `rules` TEXT, `isOfficial` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_challenges_status` ON `social_challenges` (`status`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_challenges_category` ON `social_challenges` (`category`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_challenges_startDate` ON `social_challenges` (`startDate`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_challenges_endDate` ON `social_challenges` (`endDate`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_challenges_createdAt` ON `social_challenges` (`createdAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `challenge_participations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `challengeId` INTEGER NOT NULL, `userId` TEXT NOT NULL, `userName` TEXT, `status` TEXT NOT NULL, `currentProgress` REAL NOT NULL, `progressPercentage` REAL NOT NULL, `lastActivityDate` TEXT, `completedAt` INTEGER, `joinedAt` INTEGER NOT NULL, `rank` INTEGER, `personalBest` REAL, `notes` TEXT, FOREIGN KEY(`challengeId`) REFERENCES `social_challenges`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_participations_challengeId` ON `challenge_participations` (`challengeId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_participations_userId` ON `challenge_participations` (`userId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_participations_status` ON `challenge_participations` (`status`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_participations_joinedAt` ON `challenge_participations` (`joinedAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `challenge_progress_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `participationId` INTEGER NOT NULL, `logDate` TEXT NOT NULL, `value` REAL NOT NULL, `description` TEXT, `source` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`participationId`) REFERENCES `challenge_participations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_progress_logs_participationId` ON `challenge_progress_logs` (`participationId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_progress_logs_logDate` ON `challenge_progress_logs` (`logDate`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_progress_logs_timestamp` ON `challenge_progress_logs` (`timestamp`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `social_badges` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `badgeType` TEXT NOT NULL, `iconName` TEXT NOT NULL, `rarity` TEXT NOT NULL, `requirements` TEXT NOT NULL, `challengeId` INTEGER, `isUnlocked` INTEGER NOT NULL, `unlockedAt` INTEGER, `progress` REAL NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_badges_category` ON `social_badges` (`category`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_badges_badgeType` ON `social_badges` (`badgeType`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_badges_rarity` ON `social_badges` (`rarity`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_badges_isUnlocked` ON `social_badges` (`isUnlocked`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_social_badges_unlockedAt` ON `social_badges` (`unlockedAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `leaderboard_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `challengeId` INTEGER NOT NULL, `userId` TEXT NOT NULL, `userName` TEXT, `rank` INTEGER NOT NULL, `score` REAL NOT NULL, `completionTime` INTEGER, `badge` TEXT, `lastUpdated` INTEGER NOT NULL, FOREIGN KEY(`challengeId`) REFERENCES `social_challenges`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_leaderboard_entries_challengeId` ON `leaderboard_entries` (`challengeId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_leaderboard_entries_userId` ON `leaderboard_entries` (`userId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_leaderboard_entries_rank` ON `leaderboard_entries` (`rank`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_leaderboard_score` ON `leaderboard_entries` (`score`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd5a8043b8bdd9ed129a49a6e0ad9463b')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `ai_logs`")
        connection.execSQL("DROP TABLE IF EXISTS `recipes`")
        connection.execSQL("DROP TABLE IF EXISTS `recipe_favorites`")
        connection.execSQL("DROP TABLE IF EXISTS `recipe_history`")
        connection.execSQL("DROP TABLE IF EXISTS `intake_entries`")
        connection.execSQL("DROP TABLE IF EXISTS `daily_goals`")
        connection.execSQL("DROP TABLE IF EXISTS `shopping_items`")
        connection.execSQL("DROP TABLE IF EXISTS `training_plans`")
        connection.execSQL("DROP TABLE IF EXISTS `saved_recipes`")
        connection.execSQL("DROP TABLE IF EXISTS `shopping_list_categories`")
        connection.execSQL("DROP TABLE IF EXISTS `today_workouts`")
        connection.execSQL("DROP TABLE IF EXISTS `personal_achievements`")
        connection.execSQL("DROP TABLE IF EXISTS `personal_streaks`")
        connection.execSQL("DROP TABLE IF EXISTS `personal_records`")
        connection.execSQL("DROP TABLE IF EXISTS `progress_milestones`")
        connection.execSQL("DROP TABLE IF EXISTS `weight_entries`")
        connection.execSQL("DROP TABLE IF EXISTS `food_items`")
        connection.execSQL("DROP TABLE IF EXISTS `meal_entries`")
        connection.execSQL("DROP TABLE IF EXISTS `water_entries`")
        connection.execSQL("DROP TABLE IF EXISTS `bmi_history`")
        connection.execSQL("DROP TABLE IF EXISTS `weight_loss_programs`")
        connection.execSQL("DROP TABLE IF EXISTS `behavioral_check_ins`")
        connection.execSQL("DROP TABLE IF EXISTS `progress_photos`")
        connection.execSQL("DROP TABLE IF EXISTS `workout_performance`")
        connection.execSQL("DROP TABLE IF EXISTS `workout_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `exercise_progressions`")
        connection.execSQL("DROP TABLE IF EXISTS `cooking_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `cooking_timers`")
        connection.execSQL("DROP TABLE IF EXISTS `health_connect_steps`")
        connection.execSQL("DROP TABLE IF EXISTS `health_connect_heart_rate`")
        connection.execSQL("DROP TABLE IF EXISTS `health_connect_calories`")
        connection.execSQL("DROP TABLE IF EXISTS `health_connect_sleep`")
        connection.execSQL("DROP TABLE IF EXISTS `health_connect_exercise_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `cloud_sync_metadata`")
        connection.execSQL("DROP TABLE IF EXISTS `user_profiles`")
        connection.execSQL("DROP TABLE IF EXISTS `sync_conflicts`")
        connection.execSQL("DROP TABLE IF EXISTS `social_challenges`")
        connection.execSQL("DROP TABLE IF EXISTS `challenge_participations`")
        connection.execSQL("DROP TABLE IF EXISTS `challenge_progress_logs`")
        connection.execSQL("DROP TABLE IF EXISTS `social_badges`")
        connection.execSQL("DROP TABLE IF EXISTS `leaderboard_entries`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsAiLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAiLogs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAiLogs.put("ts", TableInfo.Column("ts", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAiLogs.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAiLogs.put("provider", TableInfo.Column("provider", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAiLogs.put("prompt", TableInfo.Column("prompt", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAiLogs.put("result", TableInfo.Column("result", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAiLogs.put("success", TableInfo.Column("success", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAiLogs.put("tookMs", TableInfo.Column("tookMs", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAiLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAiLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoAiLogs: TableInfo = TableInfo("ai_logs", _columnsAiLogs, _foreignKeysAiLogs,
            _indicesAiLogs)
        val _existingAiLogs: TableInfo = read(connection, "ai_logs")
        if (!_infoAiLogs.equals(_existingAiLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |ai_logs(com.example.fitapp.data.db.AiLog).
              | Expected:
              |""".trimMargin() + _infoAiLogs + """
              |
              | Found:
              |""".trimMargin() + _existingAiLogs)
        }
        val _columnsRecipes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRecipes.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecipes.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecipes.put("markdown", TableInfo.Column("markdown", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecipes.put("calories", TableInfo.Column("calories", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecipes.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecipes.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRecipes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRecipes: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesRecipes.add(TableInfo.Index("index_recipes_createdAt", false, listOf("createdAt"),
            listOf("ASC")))
        _indicesRecipes.add(TableInfo.Index("index_recipes_calories", false, listOf("calories"),
            listOf("ASC")))
        _indicesRecipes.add(TableInfo.Index("index_recipes_title", false, listOf("title"),
            listOf("ASC")))
        val _infoRecipes: TableInfo = TableInfo("recipes", _columnsRecipes, _foreignKeysRecipes,
            _indicesRecipes)
        val _existingRecipes: TableInfo = read(connection, "recipes")
        if (!_infoRecipes.equals(_existingRecipes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |recipes(com.example.fitapp.data.db.RecipeEntity).
              | Expected:
              |""".trimMargin() + _infoRecipes + """
              |
              | Found:
              |""".trimMargin() + _existingRecipes)
        }
        val _columnsRecipeFavorites: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRecipeFavorites.put("recipeId", TableInfo.Column("recipeId", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecipeFavorites.put("savedAt", TableInfo.Column("savedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRecipeFavorites: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysRecipeFavorites.add(TableInfo.ForeignKey("recipes", "CASCADE", "NO ACTION",
            listOf("recipeId"), listOf("id")))
        val _indicesRecipeFavorites: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesRecipeFavorites.add(TableInfo.Index("index_recipe_favorites_recipeId", false,
            listOf("recipeId"), listOf("ASC")))
        val _infoRecipeFavorites: TableInfo = TableInfo("recipe_favorites", _columnsRecipeFavorites,
            _foreignKeysRecipeFavorites, _indicesRecipeFavorites)
        val _existingRecipeFavorites: TableInfo = read(connection, "recipe_favorites")
        if (!_infoRecipeFavorites.equals(_existingRecipeFavorites)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |recipe_favorites(com.example.fitapp.data.db.RecipeFavoriteEntity).
              | Expected:
              |""".trimMargin() + _infoRecipeFavorites + """
              |
              | Found:
              |""".trimMargin() + _existingRecipeFavorites)
        }
        val _columnsRecipeHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRecipeHistory.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecipeHistory.put("recipeId", TableInfo.Column("recipeId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRecipeHistory.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRecipeHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysRecipeHistory.add(TableInfo.ForeignKey("recipes", "CASCADE", "NO ACTION",
            listOf("recipeId"), listOf("id")))
        val _indicesRecipeHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesRecipeHistory.add(TableInfo.Index("index_recipe_history_recipeId", false,
            listOf("recipeId"), listOf("ASC")))
        val _infoRecipeHistory: TableInfo = TableInfo("recipe_history", _columnsRecipeHistory,
            _foreignKeysRecipeHistory, _indicesRecipeHistory)
        val _existingRecipeHistory: TableInfo = read(connection, "recipe_history")
        if (!_infoRecipeHistory.equals(_existingRecipeHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |recipe_history(com.example.fitapp.data.db.RecipeHistoryEntity).
              | Expected:
              |""".trimMargin() + _infoRecipeHistory + """
              |
              | Found:
              |""".trimMargin() + _existingRecipeHistory)
        }
        val _columnsIntakeEntries: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsIntakeEntries.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsIntakeEntries.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsIntakeEntries.put("label", TableInfo.Column("label", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsIntakeEntries.put("kcal", TableInfo.Column("kcal", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsIntakeEntries.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsIntakeEntries.put("referenceId", TableInfo.Column("referenceId", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysIntakeEntries: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesIntakeEntries: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesIntakeEntries.add(TableInfo.Index("index_intake_entries_timestamp", false,
            listOf("timestamp"), listOf("ASC")))
        _indicesIntakeEntries.add(TableInfo.Index("index_intake_entries_kcal", false,
            listOf("kcal"), listOf("ASC")))
        _indicesIntakeEntries.add(TableInfo.Index("index_intake_entries_timestamp_kcal", false,
            listOf("timestamp", "kcal"), listOf("ASC", "ASC")))
        val _infoIntakeEntries: TableInfo = TableInfo("intake_entries", _columnsIntakeEntries,
            _foreignKeysIntakeEntries, _indicesIntakeEntries)
        val _existingIntakeEntries: TableInfo = read(connection, "intake_entries")
        if (!_infoIntakeEntries.equals(_existingIntakeEntries)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |intake_entries(com.example.fitapp.data.db.IntakeEntryEntity).
              | Expected:
              |""".trimMargin() + _infoIntakeEntries + """
              |
              | Found:
              |""".trimMargin() + _existingIntakeEntries)
        }
        val _columnsDailyGoals: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDailyGoals.put("dateIso", TableInfo.Column("dateIso", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyGoals.put("targetKcal", TableInfo.Column("targetKcal", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyGoals.put("targetCarbs", TableInfo.Column("targetCarbs", "REAL", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyGoals.put("targetProtein", TableInfo.Column("targetProtein", "REAL", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyGoals.put("targetFat", TableInfo.Column("targetFat", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyGoals.put("targetWaterMl", TableInfo.Column("targetWaterMl", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDailyGoals: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDailyGoals: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDailyGoals: TableInfo = TableInfo("daily_goals", _columnsDailyGoals,
            _foreignKeysDailyGoals, _indicesDailyGoals)
        val _existingDailyGoals: TableInfo = read(connection, "daily_goals")
        if (!_infoDailyGoals.equals(_existingDailyGoals)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |daily_goals(com.example.fitapp.data.db.DailyGoalEntity).
              | Expected:
              |""".trimMargin() + _infoDailyGoals + """
              |
              | Found:
              |""".trimMargin() + _existingDailyGoals)
        }
        val _columnsShoppingItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsShoppingItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("quantity", TableInfo.Column("quantity", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("unit", TableInfo.Column("unit", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("checked", TableInfo.Column("checked", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("category", TableInfo.Column("category", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("fromRecipeId", TableInfo.Column("fromRecipeId", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingItems.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysShoppingItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesShoppingItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoShoppingItems: TableInfo = TableInfo("shopping_items", _columnsShoppingItems,
            _foreignKeysShoppingItems, _indicesShoppingItems)
        val _existingShoppingItems: TableInfo = read(connection, "shopping_items")
        if (!_infoShoppingItems.equals(_existingShoppingItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |shopping_items(com.example.fitapp.data.db.ShoppingItemEntity).
              | Expected:
              |""".trimMargin() + _infoShoppingItems + """
              |
              | Found:
              |""".trimMargin() + _existingShoppingItems)
        }
        val _columnsTrainingPlans: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTrainingPlans.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("goal", TableInfo.Column("goal", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("weeks", TableInfo.Column("weeks", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("sessionsPerWeek", TableInfo.Column("sessionsPerWeek", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("minutesPerSession", TableInfo.Column("minutesPerSession",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("equipment", TableInfo.Column("equipment", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("trainingDays", TableInfo.Column("trainingDays", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlans.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTrainingPlans: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTrainingPlans: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTrainingPlans: TableInfo = TableInfo("training_plans", _columnsTrainingPlans,
            _foreignKeysTrainingPlans, _indicesTrainingPlans)
        val _existingTrainingPlans: TableInfo = read(connection, "training_plans")
        if (!_infoTrainingPlans.equals(_existingTrainingPlans)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |training_plans(com.example.fitapp.data.db.PlanEntity).
              | Expected:
              |""".trimMargin() + _infoTrainingPlans + """
              |
              | Found:
              |""".trimMargin() + _existingTrainingPlans)
        }
        val _columnsSavedRecipes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSavedRecipes.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("markdown", TableInfo.Column("markdown", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("calories", TableInfo.Column("calories", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("ingredients", TableInfo.Column("ingredients", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("prepTime", TableInfo.Column("prepTime", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("difficulty", TableInfo.Column("difficulty", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("servings", TableInfo.Column("servings", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("isFavorite", TableInfo.Column("isFavorite", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedRecipes.put("lastCookedAt", TableInfo.Column("lastCookedAt", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSavedRecipes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSavedRecipes: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSavedRecipes: TableInfo = TableInfo("saved_recipes", _columnsSavedRecipes,
            _foreignKeysSavedRecipes, _indicesSavedRecipes)
        val _existingSavedRecipes: TableInfo = read(connection, "saved_recipes")
        if (!_infoSavedRecipes.equals(_existingSavedRecipes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |saved_recipes(com.example.fitapp.data.db.SavedRecipeEntity).
              | Expected:
              |""".trimMargin() + _infoSavedRecipes + """
              |
              | Found:
              |""".trimMargin() + _existingSavedRecipes)
        }
        val _columnsShoppingListCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsShoppingListCategories.put("name", TableInfo.Column("name", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsShoppingListCategories.put("order", TableInfo.Column("order", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysShoppingListCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesShoppingListCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoShoppingListCategories: TableInfo = TableInfo("shopping_list_categories",
            _columnsShoppingListCategories, _foreignKeysShoppingListCategories,
            _indicesShoppingListCategories)
        val _existingShoppingListCategories: TableInfo = read(connection,
            "shopping_list_categories")
        if (!_infoShoppingListCategories.equals(_existingShoppingListCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |shopping_list_categories(com.example.fitapp.data.db.ShoppingCategoryEntity).
              | Expected:
              |""".trimMargin() + _infoShoppingListCategories + """
              |
              | Found:
              |""".trimMargin() + _existingShoppingListCategories)
        }
        val _columnsTodayWorkouts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTodayWorkouts.put("dateIso", TableInfo.Column("dateIso", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTodayWorkouts.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTodayWorkouts.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTodayWorkouts.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTodayWorkouts.put("completedAt", TableInfo.Column("completedAt", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTodayWorkouts.put("planId", TableInfo.Column("planId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTodayWorkouts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTodayWorkouts: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTodayWorkouts.add(TableInfo.Index("index_today_workouts_dateIso", false,
            listOf("dateIso"), listOf("ASC")))
        _indicesTodayWorkouts.add(TableInfo.Index("index_today_workouts_status", false,
            listOf("status"), listOf("ASC")))
        _indicesTodayWorkouts.add(TableInfo.Index("index_today_workouts_createdAt", false,
            listOf("createdAt"), listOf("ASC")))
        val _infoTodayWorkouts: TableInfo = TableInfo("today_workouts", _columnsTodayWorkouts,
            _foreignKeysTodayWorkouts, _indicesTodayWorkouts)
        val _existingTodayWorkouts: TableInfo = read(connection, "today_workouts")
        if (!_infoTodayWorkouts.equals(_existingTodayWorkouts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |today_workouts(com.example.fitapp.data.db.TodayWorkoutEntity).
              | Expected:
              |""".trimMargin() + _infoTodayWorkouts + """
              |
              | Found:
              |""".trimMargin() + _existingTodayWorkouts)
        }
        val _columnsPersonalAchievements: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPersonalAchievements.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("description", TableInfo.Column("description", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("category", TableInfo.Column("category", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("iconName", TableInfo.Column("iconName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("targetValue", TableInfo.Column("targetValue", "REAL",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("currentValue", TableInfo.Column("currentValue", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("unit", TableInfo.Column("unit", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("completedAt", TableInfo.Column("completedAt", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("badgeType", TableInfo.Column("badgeType", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("rarity", TableInfo.Column("rarity", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("socialVisible", TableInfo.Column("socialVisible",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("challengeId", TableInfo.Column("challengeId", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("shareMessage", TableInfo.Column("shareMessage", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalAchievements.put("pointsValue", TableInfo.Column("pointsValue", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPersonalAchievements: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPersonalAchievements: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPersonalAchievements.add(TableInfo.Index("index_personal_achievements_category",
            false, listOf("category"), listOf("ASC")))
        _indicesPersonalAchievements.add(TableInfo.Index("index_personal_achievements_isCompleted",
            false, listOf("isCompleted"), listOf("ASC")))
        _indicesPersonalAchievements.add(TableInfo.Index("index_personal_achievements_createdAt",
            false, listOf("createdAt"), listOf("ASC")))
        val _infoPersonalAchievements: TableInfo = TableInfo("personal_achievements",
            _columnsPersonalAchievements, _foreignKeysPersonalAchievements,
            _indicesPersonalAchievements)
        val _existingPersonalAchievements: TableInfo = read(connection, "personal_achievements")
        if (!_infoPersonalAchievements.equals(_existingPersonalAchievements)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |personal_achievements(com.example.fitapp.data.db.PersonalAchievementEntity).
              | Expected:
              |""".trimMargin() + _infoPersonalAchievements + """
              |
              | Found:
              |""".trimMargin() + _existingPersonalAchievements)
        }
        val _columnsPersonalStreaks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPersonalStreaks.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("description", TableInfo.Column("description", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("currentStreak", TableInfo.Column("currentStreak", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("longestStreak", TableInfo.Column("longestStreak", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("lastActivityTimestamp",
            TableInfo.Column("lastActivityTimestamp", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("targetDays", TableInfo.Column("targetDays", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalStreaks.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPersonalStreaks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPersonalStreaks: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPersonalStreaks: TableInfo = TableInfo("personal_streaks", _columnsPersonalStreaks,
            _foreignKeysPersonalStreaks, _indicesPersonalStreaks)
        val _existingPersonalStreaks: TableInfo = read(connection, "personal_streaks")
        if (!_infoPersonalStreaks.equals(_existingPersonalStreaks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |personal_streaks(com.example.fitapp.data.db.PersonalStreakEntity).
              | Expected:
              |""".trimMargin() + _infoPersonalStreaks + """
              |
              | Found:
              |""".trimMargin() + _existingPersonalStreaks)
        }
        val _columnsPersonalRecords: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPersonalRecords.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalRecords.put("exerciseName", TableInfo.Column("exerciseName", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalRecords.put("recordType", TableInfo.Column("recordType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalRecords.put("value", TableInfo.Column("value", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalRecords.put("unit", TableInfo.Column("unit", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalRecords.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalRecords.put("achievedAt", TableInfo.Column("achievedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalRecords.put("previousRecord", TableInfo.Column("previousRecord", "REAL",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPersonalRecords.put("improvement", TableInfo.Column("improvement", "REAL", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPersonalRecords: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPersonalRecords: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPersonalRecords: TableInfo = TableInfo("personal_records", _columnsPersonalRecords,
            _foreignKeysPersonalRecords, _indicesPersonalRecords)
        val _existingPersonalRecords: TableInfo = read(connection, "personal_records")
        if (!_infoPersonalRecords.equals(_existingPersonalRecords)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |personal_records(com.example.fitapp.data.db.PersonalRecordEntity).
              | Expected:
              |""".trimMargin() + _infoPersonalRecords + """
              |
              | Found:
              |""".trimMargin() + _existingPersonalRecords)
        }
        val _columnsProgressMilestones: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProgressMilestones.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("description", TableInfo.Column("description", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("category", TableInfo.Column("category", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("targetValue", TableInfo.Column("targetValue", "REAL", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("currentValue", TableInfo.Column("currentValue", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("unit", TableInfo.Column("unit", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("targetDate", TableInfo.Column("targetDate", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("completedAt", TableInfo.Column("completedAt", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("progress", TableInfo.Column("progress", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressMilestones.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProgressMilestones: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesProgressMilestones: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoProgressMilestones: TableInfo = TableInfo("progress_milestones",
            _columnsProgressMilestones, _foreignKeysProgressMilestones, _indicesProgressMilestones)
        val _existingProgressMilestones: TableInfo = read(connection, "progress_milestones")
        if (!_infoProgressMilestones.equals(_existingProgressMilestones)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |progress_milestones(com.example.fitapp.data.db.ProgressMilestoneEntity).
              | Expected:
              |""".trimMargin() + _infoProgressMilestones + """
              |
              | Found:
              |""".trimMargin() + _existingProgressMilestones)
        }
        val _columnsWeightEntries: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWeightEntries.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightEntries.put("weight", TableInfo.Column("weight", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightEntries.put("dateIso", TableInfo.Column("dateIso", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightEntries.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightEntries.put("recordedAt", TableInfo.Column("recordedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWeightEntries: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWeightEntries: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWeightEntries.add(TableInfo.Index("index_weight_entries_dateIso", false,
            listOf("dateIso"), listOf("ASC")))
        _indicesWeightEntries.add(TableInfo.Index("index_weight_entries_recordedAt", false,
            listOf("recordedAt"), listOf("ASC")))
        val _infoWeightEntries: TableInfo = TableInfo("weight_entries", _columnsWeightEntries,
            _foreignKeysWeightEntries, _indicesWeightEntries)
        val _existingWeightEntries: TableInfo = read(connection, "weight_entries")
        if (!_infoWeightEntries.equals(_existingWeightEntries)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |weight_entries(com.example.fitapp.data.db.WeightEntity).
              | Expected:
              |""".trimMargin() + _infoWeightEntries + """
              |
              | Found:
              |""".trimMargin() + _existingWeightEntries)
        }
        val _columnsFoodItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFoodItems.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("barcode", TableInfo.Column("barcode", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("calories", TableInfo.Column("calories", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("carbs", TableInfo.Column("carbs", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("protein", TableInfo.Column("protein", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("fat", TableInfo.Column("fat", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("fiber", TableInfo.Column("fiber", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("sugar", TableInfo.Column("sugar", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("sodium", TableInfo.Column("sodium", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("brands", TableInfo.Column("brands", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("categories", TableInfo.Column("categories", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("servingSize", TableInfo.Column("servingSize", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItems.put("ingredients", TableInfo.Column("ingredients", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFoodItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFoodItems: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesFoodItems.add(TableInfo.Index("index_food_items_name", false, listOf("name"),
            listOf("ASC")))
        _indicesFoodItems.add(TableInfo.Index("index_food_items_barcode", false, listOf("barcode"),
            listOf("ASC")))
        val _infoFoodItems: TableInfo = TableInfo("food_items", _columnsFoodItems,
            _foreignKeysFoodItems, _indicesFoodItems)
        val _existingFoodItems: TableInfo = read(connection, "food_items")
        if (!_infoFoodItems.equals(_existingFoodItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |food_items(com.example.fitapp.data.db.FoodItemEntity).
              | Expected:
              |""".trimMargin() + _infoFoodItems + """
              |
              | Found:
              |""".trimMargin() + _existingFoodItems)
        }
        val _columnsMealEntries: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMealEntries.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMealEntries.put("foodItemId", TableInfo.Column("foodItemId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMealEntries.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMealEntries.put("mealType", TableInfo.Column("mealType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMealEntries.put("quantityGrams", TableInfo.Column("quantityGrams", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMealEntries.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMealEntries: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysMealEntries.add(TableInfo.ForeignKey("food_items", "CASCADE", "NO ACTION",
            listOf("foodItemId"), listOf("id")))
        val _indicesMealEntries: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesMealEntries.add(TableInfo.Index("index_meal_entries_foodItemId", false,
            listOf("foodItemId"), listOf("ASC")))
        _indicesMealEntries.add(TableInfo.Index("index_meal_entries_date", false, listOf("date"),
            listOf("ASC")))
        _indicesMealEntries.add(TableInfo.Index("index_meal_entries_mealType", false,
            listOf("mealType"), listOf("ASC")))
        val _infoMealEntries: TableInfo = TableInfo("meal_entries", _columnsMealEntries,
            _foreignKeysMealEntries, _indicesMealEntries)
        val _existingMealEntries: TableInfo = read(connection, "meal_entries")
        if (!_infoMealEntries.equals(_existingMealEntries)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |meal_entries(com.example.fitapp.data.db.MealEntryEntity).
              | Expected:
              |""".trimMargin() + _infoMealEntries + """
              |
              | Found:
              |""".trimMargin() + _existingMealEntries)
        }
        val _columnsWaterEntries: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWaterEntries.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWaterEntries.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWaterEntries.put("amountMl", TableInfo.Column("amountMl", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWaterEntries.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWaterEntries: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWaterEntries: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWaterEntries.add(TableInfo.Index("index_water_entries_date", false, listOf("date"),
            listOf("ASC")))
        val _infoWaterEntries: TableInfo = TableInfo("water_entries", _columnsWaterEntries,
            _foreignKeysWaterEntries, _indicesWaterEntries)
        val _existingWaterEntries: TableInfo = read(connection, "water_entries")
        if (!_infoWaterEntries.equals(_existingWaterEntries)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |water_entries(com.example.fitapp.data.db.WaterEntryEntity).
              | Expected:
              |""".trimMargin() + _infoWaterEntries + """
              |
              | Found:
              |""".trimMargin() + _existingWaterEntries)
        }
        val _columnsBmiHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBmiHistory.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBmiHistory.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBmiHistory.put("height", TableInfo.Column("height", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBmiHistory.put("weight", TableInfo.Column("weight", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBmiHistory.put("bmi", TableInfo.Column("bmi", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBmiHistory.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBmiHistory.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBmiHistory.put("recordedAt", TableInfo.Column("recordedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBmiHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBmiHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesBmiHistory.add(TableInfo.Index("index_bmi_history_date", false, listOf("date"),
            listOf("ASC")))
        _indicesBmiHistory.add(TableInfo.Index("index_bmi_history_bmi", false, listOf("bmi"),
            listOf("ASC")))
        _indicesBmiHistory.add(TableInfo.Index("index_bmi_history_recordedAt", false,
            listOf("recordedAt"), listOf("ASC")))
        val _infoBmiHistory: TableInfo = TableInfo("bmi_history", _columnsBmiHistory,
            _foreignKeysBmiHistory, _indicesBmiHistory)
        val _existingBmiHistory: TableInfo = read(connection, "bmi_history")
        if (!_infoBmiHistory.equals(_existingBmiHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |bmi_history(com.example.fitapp.data.db.BMIHistoryEntity).
              | Expected:
              |""".trimMargin() + _infoBmiHistory + """
              |
              | Found:
              |""".trimMargin() + _existingBmiHistory)
        }
        val _columnsWeightLossPrograms: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWeightLossPrograms.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("startDate", TableInfo.Column("startDate", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("endDate", TableInfo.Column("endDate", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("startWeight", TableInfo.Column("startWeight", "REAL", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("targetWeight", TableInfo.Column("targetWeight", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("currentWeight", TableInfo.Column("currentWeight", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("dailyCalorieTarget", TableInfo.Column("dailyCalorieTarget",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("weeklyWeightLossGoal",
            TableInfo.Column("weeklyWeightLossGoal", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("programType", TableInfo.Column("programType", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeightLossPrograms.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWeightLossPrograms: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWeightLossPrograms: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWeightLossPrograms.add(TableInfo.Index("index_weight_loss_programs_startDate",
            false, listOf("startDate"), listOf("ASC")))
        _indicesWeightLossPrograms.add(TableInfo.Index("index_weight_loss_programs_isActive", false,
            listOf("isActive"), listOf("ASC")))
        _indicesWeightLossPrograms.add(TableInfo.Index("index_weight_loss_programs_programType",
            false, listOf("programType"), listOf("ASC")))
        val _infoWeightLossPrograms: TableInfo = TableInfo("weight_loss_programs",
            _columnsWeightLossPrograms, _foreignKeysWeightLossPrograms, _indicesWeightLossPrograms)
        val _existingWeightLossPrograms: TableInfo = read(connection, "weight_loss_programs")
        if (!_infoWeightLossPrograms.equals(_existingWeightLossPrograms)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |weight_loss_programs(com.example.fitapp.data.db.WeightLossProgramEntity).
              | Expected:
              |""".trimMargin() + _infoWeightLossPrograms + """
              |
              | Found:
              |""".trimMargin() + _existingWeightLossPrograms)
        }
        val _columnsBehavioralCheckIns: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBehavioralCheckIns.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBehavioralCheckIns.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBehavioralCheckIns.put("moodScore", TableInfo.Column("moodScore", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBehavioralCheckIns.put("hungerLevel", TableInfo.Column("hungerLevel", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBehavioralCheckIns.put("stressLevel", TableInfo.Column("stressLevel", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBehavioralCheckIns.put("sleepQuality", TableInfo.Column("sleepQuality", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBehavioralCheckIns.put("triggers", TableInfo.Column("triggers", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBehavioralCheckIns.put("copingStrategy", TableInfo.Column("copingStrategy", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBehavioralCheckIns.put("mealContext", TableInfo.Column("mealContext", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBehavioralCheckIns: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBehavioralCheckIns: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesBehavioralCheckIns.add(TableInfo.Index("index_behavioral_check_ins_timestamp",
            false, listOf("timestamp"), listOf("ASC")))
        _indicesBehavioralCheckIns.add(TableInfo.Index("index_behavioral_check_ins_moodScore",
            false, listOf("moodScore"), listOf("ASC")))
        _indicesBehavioralCheckIns.add(TableInfo.Index("index_behavioral_check_ins_stressLevel",
            false, listOf("stressLevel"), listOf("ASC")))
        val _infoBehavioralCheckIns: TableInfo = TableInfo("behavioral_check_ins",
            _columnsBehavioralCheckIns, _foreignKeysBehavioralCheckIns, _indicesBehavioralCheckIns)
        val _existingBehavioralCheckIns: TableInfo = read(connection, "behavioral_check_ins")
        if (!_infoBehavioralCheckIns.equals(_existingBehavioralCheckIns)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |behavioral_check_ins(com.example.fitapp.data.db.BehavioralCheckInEntity).
              | Expected:
              |""".trimMargin() + _infoBehavioralCheckIns + """
              |
              | Found:
              |""".trimMargin() + _existingBehavioralCheckIns)
        }
        val _columnsProgressPhotos: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProgressPhotos.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhotos.put("filePath", TableInfo.Column("filePath", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhotos.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhotos.put("weight", TableInfo.Column("weight", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhotos.put("bmi", TableInfo.Column("bmi", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhotos.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProgressPhotos: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesProgressPhotos: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesProgressPhotos.add(TableInfo.Index("index_progress_photos_timestamp", false,
            listOf("timestamp"), listOf("ASC")))
        _indicesProgressPhotos.add(TableInfo.Index("index_progress_photos_weight", false,
            listOf("weight"), listOf("ASC")))
        _indicesProgressPhotos.add(TableInfo.Index("index_progress_photos_bmi", false,
            listOf("bmi"), listOf("ASC")))
        val _infoProgressPhotos: TableInfo = TableInfo("progress_photos", _columnsProgressPhotos,
            _foreignKeysProgressPhotos, _indicesProgressPhotos)
        val _existingProgressPhotos: TableInfo = read(connection, "progress_photos")
        if (!_infoProgressPhotos.equals(_existingProgressPhotos)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |progress_photos(com.example.fitapp.data.db.ProgressPhotoEntity).
              | Expected:
              |""".trimMargin() + _infoProgressPhotos + """
              |
              | Found:
              |""".trimMargin() + _existingProgressPhotos)
        }
        val _columnsWorkoutPerformance: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkoutPerformance.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("exerciseId", TableInfo.Column("exerciseId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("planId", TableInfo.Column("planId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("exerciseIndex", TableInfo.Column("exerciseIndex", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("heartRateAvg", TableInfo.Column("heartRateAvg", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("heartRateMax", TableInfo.Column("heartRateMax", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("heartRateZone", TableInfo.Column("heartRateZone", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("reps", TableInfo.Column("reps", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("weight", TableInfo.Column("weight", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("volume", TableInfo.Column("volume", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("restTime", TableInfo.Column("restTime", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("actualRestTime", TableInfo.Column("actualRestTime",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("formQuality", TableInfo.Column("formQuality", "REAL", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("perceivedExertion", TableInfo.Column("perceivedExertion",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("movementSpeed", TableInfo.Column("movementSpeed", "REAL",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("rangeOfMotion", TableInfo.Column("rangeOfMotion", "REAL",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("duration", TableInfo.Column("duration", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("isPersonalRecord", TableInfo.Column("isPersonalRecord",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutPerformance.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkoutPerformance: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWorkoutPerformance: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWorkoutPerformance.add(TableInfo.Index("index_workout_performance_exerciseId",
            false, listOf("exerciseId"), listOf("ASC")))
        _indicesWorkoutPerformance.add(TableInfo.Index("index_workout_performance_sessionId", false,
            listOf("sessionId"), listOf("ASC")))
        _indicesWorkoutPerformance.add(TableInfo.Index("index_workout_performance_planId", false,
            listOf("planId"), listOf("ASC")))
        _indicesWorkoutPerformance.add(TableInfo.Index("index_workout_performance_timestamp", false,
            listOf("timestamp"), listOf("ASC")))
        _indicesWorkoutPerformance.add(TableInfo.Index("index_workout_performance_exerciseIndex",
            false, listOf("exerciseIndex"), listOf("ASC")))
        val _infoWorkoutPerformance: TableInfo = TableInfo("workout_performance",
            _columnsWorkoutPerformance, _foreignKeysWorkoutPerformance, _indicesWorkoutPerformance)
        val _existingWorkoutPerformance: TableInfo = read(connection, "workout_performance")
        if (!_infoWorkoutPerformance.equals(_existingWorkoutPerformance)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workout_performance(com.example.fitapp.data.db.WorkoutPerformanceEntity).
              | Expected:
              |""".trimMargin() + _infoWorkoutPerformance + """
              |
              | Found:
              |""".trimMargin() + _existingWorkoutPerformance)
        }
        val _columnsWorkoutSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkoutSessions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("planId", TableInfo.Column("planId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("startTime", TableInfo.Column("startTime", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("endTime", TableInfo.Column("endTime", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("totalVolume", TableInfo.Column("totalVolume", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("averageHeartRate", TableInfo.Column("averageHeartRate",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("caloriesBurned", TableInfo.Column("caloriesBurned", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("workoutEfficiencyScore",
            TableInfo.Column("workoutEfficiencyScore", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("fatigueLevel", TableInfo.Column("fatigueLevel", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("personalRecordsAchieved",
            TableInfo.Column("personalRecordsAchieved", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("completionPercentage", TableInfo.Column("completionPercentage",
            "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("sessionRating", TableInfo.Column("sessionRating", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("sessionNotes", TableInfo.Column("sessionNotes", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkoutSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWorkoutSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWorkoutSessions.add(TableInfo.Index("index_workout_sessions_planId", false,
            listOf("planId"), listOf("ASC")))
        _indicesWorkoutSessions.add(TableInfo.Index("index_workout_sessions_userId", false,
            listOf("userId"), listOf("ASC")))
        _indicesWorkoutSessions.add(TableInfo.Index("index_workout_sessions_startTime", false,
            listOf("startTime"), listOf("ASC")))
        _indicesWorkoutSessions.add(TableInfo.Index("index_workout_sessions_workoutEfficiencyScore",
            false, listOf("workoutEfficiencyScore"), listOf("ASC")))
        val _infoWorkoutSessions: TableInfo = TableInfo("workout_sessions", _columnsWorkoutSessions,
            _foreignKeysWorkoutSessions, _indicesWorkoutSessions)
        val _existingWorkoutSessions: TableInfo = read(connection, "workout_sessions")
        if (!_infoWorkoutSessions.equals(_existingWorkoutSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workout_sessions(com.example.fitapp.data.db.WorkoutSessionEntity).
              | Expected:
              |""".trimMargin() + _infoWorkoutSessions + """
              |
              | Found:
              |""".trimMargin() + _existingWorkoutSessions)
        }
        val _columnsExerciseProgressions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExerciseProgressions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("exerciseId", TableInfo.Column("exerciseId", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("currentWeight", TableInfo.Column("currentWeight", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("recommendedWeight", TableInfo.Column("recommendedWeight",
            "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("currentReps", TableInfo.Column("currentReps", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("recommendedReps", TableInfo.Column("recommendedReps",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("progressionReason", TableInfo.Column("progressionReason",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("performanceTrend", TableInfo.Column("performanceTrend",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("plateauDetected", TableInfo.Column("plateauDetected",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("plateauWeeks", TableInfo.Column("plateauWeeks", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("lastProgressDate", TableInfo.Column("lastProgressDate",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("aiConfidence", TableInfo.Column("aiConfidence", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("nextReviewDate", TableInfo.Column("nextReviewDate",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseProgressions.put("adaptationNotes", TableInfo.Column("adaptationNotes",
            "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExerciseProgressions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExerciseProgressions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesExerciseProgressions.add(TableInfo.Index("index_exercise_progressions_exerciseId",
            false, listOf("exerciseId"), listOf("ASC")))
        _indicesExerciseProgressions.add(TableInfo.Index("index_exercise_progressions_userId",
            false, listOf("userId"), listOf("ASC")))
        _indicesExerciseProgressions.add(TableInfo.Index("index_exercise_progressions_performanceTrend",
            false, listOf("performanceTrend"), listOf("ASC")))
        _indicesExerciseProgressions.add(TableInfo.Index("index_exercise_progressions_plateauDetected",
            false, listOf("plateauDetected"), listOf("ASC")))
        _indicesExerciseProgressions.add(TableInfo.Index("index_exercise_progressions_lastProgressDate",
            false, listOf("lastProgressDate"), listOf("ASC")))
        val _infoExerciseProgressions: TableInfo = TableInfo("exercise_progressions",
            _columnsExerciseProgressions, _foreignKeysExerciseProgressions,
            _indicesExerciseProgressions)
        val _existingExerciseProgressions: TableInfo = read(connection, "exercise_progressions")
        if (!_infoExerciseProgressions.equals(_existingExerciseProgressions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercise_progressions(com.example.fitapp.data.db.ExerciseProgressionEntity).
              | Expected:
              |""".trimMargin() + _infoExerciseProgressions + """
              |
              | Found:
              |""".trimMargin() + _existingExerciseProgressions)
        }
        val _columnsCookingSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCookingSessions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("recipeId", TableInfo.Column("recipeId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("startTime", TableInfo.Column("startTime", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("endTime", TableInfo.Column("endTime", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("currentStep", TableInfo.Column("currentStep", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("totalSteps", TableInfo.Column("totalSteps", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("estimatedDuration", TableInfo.Column("estimatedDuration",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("actualDuration", TableInfo.Column("actualDuration", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingSessions.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCookingSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCookingSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCookingSessions.add(TableInfo.Index("index_cooking_sessions_recipeId", false,
            listOf("recipeId"), listOf("ASC")))
        _indicesCookingSessions.add(TableInfo.Index("index_cooking_sessions_startTime", false,
            listOf("startTime"), listOf("ASC")))
        _indicesCookingSessions.add(TableInfo.Index("index_cooking_sessions_status", false,
            listOf("status"), listOf("ASC")))
        val _infoCookingSessions: TableInfo = TableInfo("cooking_sessions", _columnsCookingSessions,
            _foreignKeysCookingSessions, _indicesCookingSessions)
        val _existingCookingSessions: TableInfo = read(connection, "cooking_sessions")
        if (!_infoCookingSessions.equals(_existingCookingSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |cooking_sessions(com.example.fitapp.data.db.CookingSessionEntity).
              | Expected:
              |""".trimMargin() + _infoCookingSessions + """
              |
              | Found:
              |""".trimMargin() + _existingCookingSessions)
        }
        val _columnsCookingTimers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCookingTimers.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("stepIndex", TableInfo.Column("stepIndex", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("durationSeconds", TableInfo.Column("durationSeconds", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("remainingSeconds", TableInfo.Column("remainingSeconds",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("isPaused", TableInfo.Column("isPaused", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("startTime", TableInfo.Column("startTime", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("completedAt", TableInfo.Column("completedAt", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCookingTimers.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCookingTimers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysCookingTimers.add(TableInfo.ForeignKey("cooking_sessions", "CASCADE",
            "NO ACTION", listOf("sessionId"), listOf("id")))
        val _indicesCookingTimers: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCookingTimers.add(TableInfo.Index("index_cooking_timers_sessionId", false,
            listOf("sessionId"), listOf("ASC")))
        _indicesCookingTimers.add(TableInfo.Index("index_cooking_timers_stepIndex", false,
            listOf("stepIndex"), listOf("ASC")))
        _indicesCookingTimers.add(TableInfo.Index("index_cooking_timers_isActive", false,
            listOf("isActive"), listOf("ASC")))
        val _infoCookingTimers: TableInfo = TableInfo("cooking_timers", _columnsCookingTimers,
            _foreignKeysCookingTimers, _indicesCookingTimers)
        val _existingCookingTimers: TableInfo = read(connection, "cooking_timers")
        if (!_infoCookingTimers.equals(_existingCookingTimers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |cooking_timers(com.example.fitapp.data.db.CookingTimerEntity).
              | Expected:
              |""".trimMargin() + _infoCookingTimers + """
              |
              | Found:
              |""".trimMargin() + _existingCookingTimers)
        }
        val _columnsHealthConnectSteps: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHealthConnectSteps.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSteps.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSteps.put("steps", TableInfo.Column("steps", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSteps.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSteps.put("syncedAt", TableInfo.Column("syncedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSteps.put("lastModified", TableInfo.Column("lastModified", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHealthConnectSteps: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesHealthConnectSteps: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesHealthConnectSteps.add(TableInfo.Index("index_health_connect_steps_date", false,
            listOf("date"), listOf("ASC")))
        _indicesHealthConnectSteps.add(TableInfo.Index("index_health_connect_steps_source", false,
            listOf("source"), listOf("ASC")))
        _indicesHealthConnectSteps.add(TableInfo.Index("index_health_connect_steps_syncedAt", false,
            listOf("syncedAt"), listOf("ASC")))
        val _infoHealthConnectSteps: TableInfo = TableInfo("health_connect_steps",
            _columnsHealthConnectSteps, _foreignKeysHealthConnectSteps, _indicesHealthConnectSteps)
        val _existingHealthConnectSteps: TableInfo = read(connection, "health_connect_steps")
        if (!_infoHealthConnectSteps.equals(_existingHealthConnectSteps)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |health_connect_steps(com.example.fitapp.data.db.HealthStepsEntity).
              | Expected:
              |""".trimMargin() + _infoHealthConnectSteps + """
              |
              | Found:
              |""".trimMargin() + _existingHealthConnectSteps)
        }
        val _columnsHealthConnectHeartRate: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHealthConnectHeartRate.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectHeartRate.put("timestamp", TableInfo.Column("timestamp", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectHeartRate.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectHeartRate.put("heartRate", TableInfo.Column("heartRate", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectHeartRate.put("source", TableInfo.Column("source", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectHeartRate.put("syncedAt", TableInfo.Column("syncedAt", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHealthConnectHeartRate: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesHealthConnectHeartRate: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesHealthConnectHeartRate.add(TableInfo.Index("index_health_connect_heart_rate_date",
            false, listOf("date"), listOf("ASC")))
        _indicesHealthConnectHeartRate.add(TableInfo.Index("index_health_connect_heart_rate_timestamp",
            false, listOf("timestamp"), listOf("ASC")))
        _indicesHealthConnectHeartRate.add(TableInfo.Index("index_health_connect_heart_rate_source",
            false, listOf("source"), listOf("ASC")))
        val _infoHealthConnectHeartRate: TableInfo = TableInfo("health_connect_heart_rate",
            _columnsHealthConnectHeartRate, _foreignKeysHealthConnectHeartRate,
            _indicesHealthConnectHeartRate)
        val _existingHealthConnectHeartRate: TableInfo = read(connection,
            "health_connect_heart_rate")
        if (!_infoHealthConnectHeartRate.equals(_existingHealthConnectHeartRate)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |health_connect_heart_rate(com.example.fitapp.data.db.HealthHeartRateEntity).
              | Expected:
              |""".trimMargin() + _infoHealthConnectHeartRate + """
              |
              | Found:
              |""".trimMargin() + _existingHealthConnectHeartRate)
        }
        val _columnsHealthConnectCalories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHealthConnectCalories.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectCalories.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectCalories.put("calories", TableInfo.Column("calories", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectCalories.put("calorieType", TableInfo.Column("calorieType", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectCalories.put("source", TableInfo.Column("source", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectCalories.put("syncedAt", TableInfo.Column("syncedAt", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectCalories.put("lastModified", TableInfo.Column("lastModified",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHealthConnectCalories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesHealthConnectCalories: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesHealthConnectCalories.add(TableInfo.Index("index_health_connect_calories_date",
            false, listOf("date"), listOf("ASC")))
        _indicesHealthConnectCalories.add(TableInfo.Index("index_health_connect_calories_calorieType",
            false, listOf("calorieType"), listOf("ASC")))
        _indicesHealthConnectCalories.add(TableInfo.Index("index_health_connect_calories_source",
            false, listOf("source"), listOf("ASC")))
        val _infoHealthConnectCalories: TableInfo = TableInfo("health_connect_calories",
            _columnsHealthConnectCalories, _foreignKeysHealthConnectCalories,
            _indicesHealthConnectCalories)
        val _existingHealthConnectCalories: TableInfo = read(connection, "health_connect_calories")
        if (!_infoHealthConnectCalories.equals(_existingHealthConnectCalories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |health_connect_calories(com.example.fitapp.data.db.HealthCalorieEntity).
              | Expected:
              |""".trimMargin() + _infoHealthConnectCalories + """
              |
              | Found:
              |""".trimMargin() + _existingHealthConnectCalories)
        }
        val _columnsHealthConnectSleep: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHealthConnectSleep.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSleep.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSleep.put("startTime", TableInfo.Column("startTime", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSleep.put("endTime", TableInfo.Column("endTime", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSleep.put("durationMinutes", TableInfo.Column("durationMinutes",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSleep.put("sleepStage", TableInfo.Column("sleepStage", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSleep.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectSleep.put("syncedAt", TableInfo.Column("syncedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHealthConnectSleep: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesHealthConnectSleep: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesHealthConnectSleep.add(TableInfo.Index("index_health_connect_sleep_date", false,
            listOf("date"), listOf("ASC")))
        _indicesHealthConnectSleep.add(TableInfo.Index("index_health_connect_sleep_source", false,
            listOf("source"), listOf("ASC")))
        _indicesHealthConnectSleep.add(TableInfo.Index("index_health_connect_sleep_sleepStage",
            false, listOf("sleepStage"), listOf("ASC")))
        val _infoHealthConnectSleep: TableInfo = TableInfo("health_connect_sleep",
            _columnsHealthConnectSleep, _foreignKeysHealthConnectSleep, _indicesHealthConnectSleep)
        val _existingHealthConnectSleep: TableInfo = read(connection, "health_connect_sleep")
        if (!_infoHealthConnectSleep.equals(_existingHealthConnectSleep)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |health_connect_sleep(com.example.fitapp.data.db.HealthSleepEntity).
              | Expected:
              |""".trimMargin() + _infoHealthConnectSleep + """
              |
              | Found:
              |""".trimMargin() + _existingHealthConnectSleep)
        }
        val _columnsHealthConnectExerciseSessions: MutableMap<String, TableInfo.Column> =
            mutableMapOf()
        _columnsHealthConnectExerciseSessions.put("id", TableInfo.Column("id", "INTEGER", true, 1,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("sessionId", TableInfo.Column("sessionId", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("date", TableInfo.Column("date", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("startTime", TableInfo.Column("startTime",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("endTime", TableInfo.Column("endTime", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("durationMinutes",
            TableInfo.Column("durationMinutes", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("exerciseType", TableInfo.Column("exerciseType",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("title", TableInfo.Column("title", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("calories", TableInfo.Column("calories", "REAL",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("avgHeartRate", TableInfo.Column("avgHeartRate",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("maxHeartRate", TableInfo.Column("maxHeartRate",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("distance", TableInfo.Column("distance", "REAL",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("source", TableInfo.Column("source", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("syncedAt", TableInfo.Column("syncedAt",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHealthConnectExerciseSessions.put("lastModified", TableInfo.Column("lastModified",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHealthConnectExerciseSessions: MutableSet<TableInfo.ForeignKey> =
            mutableSetOf()
        val _indicesHealthConnectExerciseSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesHealthConnectExerciseSessions.add(TableInfo.Index("index_health_connect_exercise_sessions_date",
            false, listOf("date"), listOf("ASC")))
        _indicesHealthConnectExerciseSessions.add(TableInfo.Index("index_health_connect_exercise_sessions_exerciseType",
            false, listOf("exerciseType"), listOf("ASC")))
        _indicesHealthConnectExerciseSessions.add(TableInfo.Index("index_health_connect_exercise_sessions_source",
            false, listOf("source"), listOf("ASC")))
        val _infoHealthConnectExerciseSessions: TableInfo =
            TableInfo("health_connect_exercise_sessions", _columnsHealthConnectExerciseSessions,
            _foreignKeysHealthConnectExerciseSessions, _indicesHealthConnectExerciseSessions)
        val _existingHealthConnectExerciseSessions: TableInfo = read(connection,
            "health_connect_exercise_sessions")
        if (!_infoHealthConnectExerciseSessions.equals(_existingHealthConnectExerciseSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |health_connect_exercise_sessions(com.example.fitapp.data.db.HealthExerciseSessionEntity).
              | Expected:
              |""".trimMargin() + _infoHealthConnectExerciseSessions + """
              |
              | Found:
              |""".trimMargin() + _existingHealthConnectExerciseSessions)
        }
        val _columnsCloudSyncMetadata: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCloudSyncMetadata.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("entityType", TableInfo.Column("entityType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("entityId", TableInfo.Column("entityId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("lastSyncTime", TableInfo.Column("lastSyncTime", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("lastModifiedTime", TableInfo.Column("lastModifiedTime",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("syncStatus", TableInfo.Column("syncStatus", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("cloudVersion", TableInfo.Column("cloudVersion", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("conflictData", TableInfo.Column("conflictData", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("retryCount", TableInfo.Column("retryCount", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("errorMessage", TableInfo.Column("errorMessage", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCloudSyncMetadata.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCloudSyncMetadata: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCloudSyncMetadata: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCloudSyncMetadata.add(TableInfo.Index("index_cloud_sync_metadata_entityType_entityId",
            true, listOf("entityType", "entityId"), listOf("ASC", "ASC")))
        _indicesCloudSyncMetadata.add(TableInfo.Index("index_cloud_sync_metadata_lastSyncTime",
            false, listOf("lastSyncTime"), listOf("ASC")))
        _indicesCloudSyncMetadata.add(TableInfo.Index("index_cloud_sync_metadata_syncStatus", false,
            listOf("syncStatus"), listOf("ASC")))
        _indicesCloudSyncMetadata.add(TableInfo.Index("index_cloud_sync_metadata_deviceId", false,
            listOf("deviceId"), listOf("ASC")))
        val _infoCloudSyncMetadata: TableInfo = TableInfo("cloud_sync_metadata",
            _columnsCloudSyncMetadata, _foreignKeysCloudSyncMetadata, _indicesCloudSyncMetadata)
        val _existingCloudSyncMetadata: TableInfo = read(connection, "cloud_sync_metadata")
        if (!_infoCloudSyncMetadata.equals(_existingCloudSyncMetadata)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |cloud_sync_metadata(com.example.fitapp.data.db.CloudSyncEntity).
              | Expected:
              |""".trimMargin() + _infoCloudSyncMetadata + """
              |
              | Found:
              |""".trimMargin() + _existingCloudSyncMetadata)
        }
        val _columnsUserProfiles: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUserProfiles.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("email", TableInfo.Column("email", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("displayName", TableInfo.Column("displayName", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("deviceName", TableInfo.Column("deviceName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("lastSyncTime", TableInfo.Column("lastSyncTime", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("syncPreferences", TableInfo.Column("syncPreferences", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("encryptionKey", TableInfo.Column("encryptionKey", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfiles.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserProfiles: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUserProfiles: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesUserProfiles.add(TableInfo.Index("index_user_profiles_userId", true,
            listOf("userId"), listOf("ASC")))
        _indicesUserProfiles.add(TableInfo.Index("index_user_profiles_email", true, listOf("email"),
            listOf("ASC")))
        _indicesUserProfiles.add(TableInfo.Index("index_user_profiles_lastSyncTime", false,
            listOf("lastSyncTime"), listOf("ASC")))
        val _infoUserProfiles: TableInfo = TableInfo("user_profiles", _columnsUserProfiles,
            _foreignKeysUserProfiles, _indicesUserProfiles)
        val _existingUserProfiles: TableInfo = read(connection, "user_profiles")
        if (!_infoUserProfiles.equals(_existingUserProfiles)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |user_profiles(com.example.fitapp.data.db.UserProfileEntity).
              | Expected:
              |""".trimMargin() + _infoUserProfiles + """
              |
              | Found:
              |""".trimMargin() + _existingUserProfiles)
        }
        val _columnsSyncConflicts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSyncConflicts.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("entityType", TableInfo.Column("entityType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("entityId", TableInfo.Column("entityId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("localData", TableInfo.Column("localData", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("remoteData", TableInfo.Column("remoteData", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("localTimestamp", TableInfo.Column("localTimestamp", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("remoteTimestamp", TableInfo.Column("remoteTimestamp", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("resolution", TableInfo.Column("resolution", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("resolvedData", TableInfo.Column("resolvedData", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("resolvedBy", TableInfo.Column("resolvedBy", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncConflicts.put("resolvedAt", TableInfo.Column("resolvedAt", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSyncConflicts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSyncConflicts: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSyncConflicts.add(TableInfo.Index("index_sync_conflicts_entityType_entityId", false,
            listOf("entityType", "entityId"), listOf("ASC", "ASC")))
        _indicesSyncConflicts.add(TableInfo.Index("index_sync_conflicts_createdAt", false,
            listOf("createdAt"), listOf("ASC")))
        _indicesSyncConflicts.add(TableInfo.Index("index_sync_conflicts_status", false,
            listOf("status"), listOf("ASC")))
        val _infoSyncConflicts: TableInfo = TableInfo("sync_conflicts", _columnsSyncConflicts,
            _foreignKeysSyncConflicts, _indicesSyncConflicts)
        val _existingSyncConflicts: TableInfo = read(connection, "sync_conflicts")
        if (!_infoSyncConflicts.equals(_existingSyncConflicts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sync_conflicts(com.example.fitapp.data.db.SyncConflictEntity).
              | Expected:
              |""".trimMargin() + _infoSyncConflicts + """
              |
              | Found:
              |""".trimMargin() + _existingSyncConflicts)
        }
        val _columnsSocialChallenges: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSocialChallenges.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("description", TableInfo.Column("description", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("challengeType", TableInfo.Column("challengeType", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("targetMetric", TableInfo.Column("targetMetric", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("targetValue", TableInfo.Column("targetValue", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("unit", TableInfo.Column("unit", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("duration", TableInfo.Column("duration", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("startDate", TableInfo.Column("startDate", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("endDate", TableInfo.Column("endDate", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("maxParticipants", TableInfo.Column("maxParticipants",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("currentParticipants", TableInfo.Column("currentParticipants",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("creatorId", TableInfo.Column("creatorId", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("reward", TableInfo.Column("reward", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("difficulty", TableInfo.Column("difficulty", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("rules", TableInfo.Column("rules", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("isOfficial", TableInfo.Column("isOfficial", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialChallenges.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSocialChallenges: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSocialChallenges: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSocialChallenges.add(TableInfo.Index("index_social_challenges_status", false,
            listOf("status"), listOf("ASC")))
        _indicesSocialChallenges.add(TableInfo.Index("index_social_challenges_category", false,
            listOf("category"), listOf("ASC")))
        _indicesSocialChallenges.add(TableInfo.Index("index_social_challenges_startDate", false,
            listOf("startDate"), listOf("ASC")))
        _indicesSocialChallenges.add(TableInfo.Index("index_social_challenges_endDate", false,
            listOf("endDate"), listOf("ASC")))
        _indicesSocialChallenges.add(TableInfo.Index("index_social_challenges_createdAt", false,
            listOf("createdAt"), listOf("ASC")))
        val _infoSocialChallenges: TableInfo = TableInfo("social_challenges",
            _columnsSocialChallenges, _foreignKeysSocialChallenges, _indicesSocialChallenges)
        val _existingSocialChallenges: TableInfo = read(connection, "social_challenges")
        if (!_infoSocialChallenges.equals(_existingSocialChallenges)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |social_challenges(com.example.fitapp.data.db.SocialChallengeEntity).
              | Expected:
              |""".trimMargin() + _infoSocialChallenges + """
              |
              | Found:
              |""".trimMargin() + _existingSocialChallenges)
        }
        val _columnsChallengeParticipations: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChallengeParticipations.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("challengeId", TableInfo.Column("challengeId",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("userId", TableInfo.Column("userId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("userName", TableInfo.Column("userName", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("status", TableInfo.Column("status", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("currentProgress", TableInfo.Column("currentProgress",
            "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("progressPercentage",
            TableInfo.Column("progressPercentage", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("lastActivityDate", TableInfo.Column("lastActivityDate",
            "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("completedAt", TableInfo.Column("completedAt",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("joinedAt", TableInfo.Column("joinedAt", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("rank", TableInfo.Column("rank", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("personalBest", TableInfo.Column("personalBest", "REAL",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeParticipations.put("notes", TableInfo.Column("notes", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChallengeParticipations: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysChallengeParticipations.add(TableInfo.ForeignKey("social_challenges", "CASCADE",
            "NO ACTION", listOf("challengeId"), listOf("id")))
        val _indicesChallengeParticipations: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesChallengeParticipations.add(TableInfo.Index("index_challenge_participations_challengeId",
            false, listOf("challengeId"), listOf("ASC")))
        _indicesChallengeParticipations.add(TableInfo.Index("index_challenge_participations_userId",
            false, listOf("userId"), listOf("ASC")))
        _indicesChallengeParticipations.add(TableInfo.Index("index_challenge_participations_status",
            false, listOf("status"), listOf("ASC")))
        _indicesChallengeParticipations.add(TableInfo.Index("index_challenge_participations_joinedAt",
            false, listOf("joinedAt"), listOf("ASC")))
        val _infoChallengeParticipations: TableInfo = TableInfo("challenge_participations",
            _columnsChallengeParticipations, _foreignKeysChallengeParticipations,
            _indicesChallengeParticipations)
        val _existingChallengeParticipations: TableInfo = read(connection,
            "challenge_participations")
        if (!_infoChallengeParticipations.equals(_existingChallengeParticipations)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |challenge_participations(com.example.fitapp.data.db.ChallengeParticipationEntity).
              | Expected:
              |""".trimMargin() + _infoChallengeParticipations + """
              |
              | Found:
              |""".trimMargin() + _existingChallengeParticipations)
        }
        val _columnsChallengeProgressLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChallengeProgressLogs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeProgressLogs.put("participationId", TableInfo.Column("participationId",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeProgressLogs.put("logDate", TableInfo.Column("logDate", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeProgressLogs.put("value", TableInfo.Column("value", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeProgressLogs.put("description", TableInfo.Column("description", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeProgressLogs.put("source", TableInfo.Column("source", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChallengeProgressLogs.put("timestamp", TableInfo.Column("timestamp", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChallengeProgressLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysChallengeProgressLogs.add(TableInfo.ForeignKey("challenge_participations",
            "CASCADE", "NO ACTION", listOf("participationId"), listOf("id")))
        val _indicesChallengeProgressLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesChallengeProgressLogs.add(TableInfo.Index("index_challenge_progress_logs_participationId",
            false, listOf("participationId"), listOf("ASC")))
        _indicesChallengeProgressLogs.add(TableInfo.Index("index_challenge_progress_logs_logDate",
            false, listOf("logDate"), listOf("ASC")))
        _indicesChallengeProgressLogs.add(TableInfo.Index("index_challenge_progress_logs_timestamp",
            false, listOf("timestamp"), listOf("ASC")))
        val _infoChallengeProgressLogs: TableInfo = TableInfo("challenge_progress_logs",
            _columnsChallengeProgressLogs, _foreignKeysChallengeProgressLogs,
            _indicesChallengeProgressLogs)
        val _existingChallengeProgressLogs: TableInfo = read(connection, "challenge_progress_logs")
        if (!_infoChallengeProgressLogs.equals(_existingChallengeProgressLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |challenge_progress_logs(com.example.fitapp.data.db.ChallengeProgressLogEntity).
              | Expected:
              |""".trimMargin() + _infoChallengeProgressLogs + """
              |
              | Found:
              |""".trimMargin() + _existingChallengeProgressLogs)
        }
        val _columnsSocialBadges: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSocialBadges.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("description", TableInfo.Column("description", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("badgeType", TableInfo.Column("badgeType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("iconName", TableInfo.Column("iconName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("rarity", TableInfo.Column("rarity", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("requirements", TableInfo.Column("requirements", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("challengeId", TableInfo.Column("challengeId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("isUnlocked", TableInfo.Column("isUnlocked", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("unlockedAt", TableInfo.Column("unlockedAt", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("progress", TableInfo.Column("progress", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSocialBadges.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSocialBadges: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSocialBadges: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSocialBadges.add(TableInfo.Index("index_social_badges_category", false,
            listOf("category"), listOf("ASC")))
        _indicesSocialBadges.add(TableInfo.Index("index_social_badges_badgeType", false,
            listOf("badgeType"), listOf("ASC")))
        _indicesSocialBadges.add(TableInfo.Index("index_social_badges_rarity", false,
            listOf("rarity"), listOf("ASC")))
        _indicesSocialBadges.add(TableInfo.Index("index_social_badges_isUnlocked", false,
            listOf("isUnlocked"), listOf("ASC")))
        _indicesSocialBadges.add(TableInfo.Index("index_social_badges_unlockedAt", false,
            listOf("unlockedAt"), listOf("ASC")))
        val _infoSocialBadges: TableInfo = TableInfo("social_badges", _columnsSocialBadges,
            _foreignKeysSocialBadges, _indicesSocialBadges)
        val _existingSocialBadges: TableInfo = read(connection, "social_badges")
        if (!_infoSocialBadges.equals(_existingSocialBadges)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |social_badges(com.example.fitapp.data.db.SocialBadgeEntity).
              | Expected:
              |""".trimMargin() + _infoSocialBadges + """
              |
              | Found:
              |""".trimMargin() + _existingSocialBadges)
        }
        val _columnsLeaderboardEntries: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLeaderboardEntries.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLeaderboardEntries.put("challengeId", TableInfo.Column("challengeId", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLeaderboardEntries.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLeaderboardEntries.put("userName", TableInfo.Column("userName", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLeaderboardEntries.put("rank", TableInfo.Column("rank", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLeaderboardEntries.put("score", TableInfo.Column("score", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLeaderboardEntries.put("completionTime", TableInfo.Column("completionTime",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLeaderboardEntries.put("badge", TableInfo.Column("badge", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLeaderboardEntries.put("lastUpdated", TableInfo.Column("lastUpdated", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLeaderboardEntries: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysLeaderboardEntries.add(TableInfo.ForeignKey("social_challenges", "CASCADE",
            "NO ACTION", listOf("challengeId"), listOf("id")))
        val _indicesLeaderboardEntries: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesLeaderboardEntries.add(TableInfo.Index("index_leaderboard_entries_challengeId",
            false, listOf("challengeId"), listOf("ASC")))
        _indicesLeaderboardEntries.add(TableInfo.Index("index_leaderboard_entries_userId", false,
            listOf("userId"), listOf("ASC")))
        _indicesLeaderboardEntries.add(TableInfo.Index("index_leaderboard_entries_rank", false,
            listOf("rank"), listOf("ASC")))
        _indicesLeaderboardEntries.add(TableInfo.Index("index_leaderboard_score", false,
            listOf("score"), listOf("ASC")))
        val _infoLeaderboardEntries: TableInfo = TableInfo("leaderboard_entries",
            _columnsLeaderboardEntries, _foreignKeysLeaderboardEntries, _indicesLeaderboardEntries)
        val _existingLeaderboardEntries: TableInfo = read(connection, "leaderboard_entries")
        if (!_infoLeaderboardEntries.equals(_existingLeaderboardEntries)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |leaderboard_entries(com.example.fitapp.data.db.LeaderboardEntryEntity).
              | Expected:
              |""".trimMargin() + _infoLeaderboardEntries + """
              |
              | Found:
              |""".trimMargin() + _existingLeaderboardEntries)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "ai_logs", "recipes",
        "recipe_favorites", "recipe_history", "intake_entries", "daily_goals", "shopping_items",
        "training_plans", "saved_recipes", "shopping_list_categories", "today_workouts",
        "personal_achievements", "personal_streaks", "personal_records", "progress_milestones",
        "weight_entries", "food_items", "meal_entries", "water_entries", "bmi_history",
        "weight_loss_programs", "behavioral_check_ins", "progress_photos", "workout_performance",
        "workout_sessions", "exercise_progressions", "cooking_sessions", "cooking_timers",
        "health_connect_steps", "health_connect_heart_rate", "health_connect_calories",
        "health_connect_sleep", "health_connect_exercise_sessions", "cloud_sync_metadata",
        "user_profiles", "sync_conflicts", "social_challenges", "challenge_participations",
        "challenge_progress_logs", "social_badges", "leaderboard_entries")
  }

  public override fun clearAllTables() {
    super.performClear(true, "ai_logs", "recipes", "recipe_favorites", "recipe_history",
        "intake_entries", "daily_goals", "shopping_items", "training_plans", "saved_recipes",
        "shopping_list_categories", "today_workouts", "personal_achievements", "personal_streaks",
        "personal_records", "progress_milestones", "weight_entries", "food_items", "meal_entries",
        "water_entries", "bmi_history", "weight_loss_programs", "behavioral_check_ins",
        "progress_photos", "workout_performance", "workout_sessions", "exercise_progressions",
        "cooking_sessions", "cooking_timers", "health_connect_steps", "health_connect_heart_rate",
        "health_connect_calories", "health_connect_sleep", "health_connect_exercise_sessions",
        "cloud_sync_metadata", "user_profiles", "sync_conflicts", "social_challenges",
        "challenge_participations", "challenge_progress_logs", "social_badges",
        "leaderboard_entries")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AiLogDao::class, AiLogDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RecipeDao::class, RecipeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(IntakeDao::class, IntakeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(GoalDao::class, GoalDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ShoppingDao::class, ShoppingDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PlanDao::class, PlanDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SavedRecipeDao::class, SavedRecipeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ShoppingCategoryDao::class,
        ShoppingCategoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TodayWorkoutDao::class, TodayWorkoutDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PersonalAchievementDao::class,
        PersonalAchievementDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PersonalStreakDao::class, PersonalStreakDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PersonalRecordDao::class, PersonalRecordDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ProgressMilestoneDao::class,
        ProgressMilestoneDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WeightDao::class, WeightDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(FoodItemDao::class, FoodItemDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MealEntryDao::class, MealEntryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WaterEntryDao::class, WaterEntryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BMIHistoryDao::class, BMIHistoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WeightLossProgramDao::class,
        WeightLossProgramDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BehavioralCheckInDao::class,
        BehavioralCheckInDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ProgressPhotoDao::class, ProgressPhotoDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WorkoutPerformanceDao::class,
        WorkoutPerformanceDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WorkoutSessionDao::class, WorkoutSessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ExerciseProgressionDao::class,
        ExerciseProgressionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CookingSessionDao::class, CookingSessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CookingTimerDao::class, CookingTimerDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HealthStepsDao::class, HealthStepsDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HealthHeartRateDao::class,
        HealthHeartRateDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HealthCalorieDao::class, HealthCalorieDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HealthSleepDao::class, HealthSleepDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HealthExerciseSessionDao::class,
        HealthExerciseSessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CloudSyncDao::class, CloudSyncDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(UserProfileDao::class, UserProfileDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SyncConflictDao::class, SyncConflictDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SocialChallengeDao::class,
        SocialChallengeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ChallengeParticipationDao::class,
        ChallengeParticipationDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ChallengeProgressLogDao::class,
        ChallengeProgressLogDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SocialBadgeDao::class, SocialBadgeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(LeaderboardEntryDao::class,
        LeaderboardEntryDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun aiLogDao(): AiLogDao = _aiLogDao.value

  public override fun recipeDao(): RecipeDao = _recipeDao.value

  public override fun intakeDao(): IntakeDao = _intakeDao.value

  public override fun goalDao(): GoalDao = _goalDao.value

  public override fun shoppingDao(): ShoppingDao = _shoppingDao.value

  public override fun planDao(): PlanDao = _planDao.value

  public override fun savedRecipeDao(): SavedRecipeDao = _savedRecipeDao.value

  public override fun shoppingCategoryDao(): ShoppingCategoryDao = _shoppingCategoryDao.value

  public override fun todayWorkoutDao(): TodayWorkoutDao = _todayWorkoutDao.value

  public override fun personalAchievementDao(): PersonalAchievementDao =
      _personalAchievementDao.value

  public override fun personalStreakDao(): PersonalStreakDao = _personalStreakDao.value

  public override fun personalRecordDao(): PersonalRecordDao = _personalRecordDao.value

  public override fun progressMilestoneDao(): ProgressMilestoneDao = _progressMilestoneDao.value

  public override fun weightDao(): WeightDao = _weightDao.value

  public override fun foodItemDao(): FoodItemDao = _foodItemDao.value

  public override fun mealEntryDao(): MealEntryDao = _mealEntryDao.value

  public override fun waterEntryDao(): WaterEntryDao = _waterEntryDao.value

  public override fun bmiHistoryDao(): BMIHistoryDao = _bMIHistoryDao.value

  public override fun weightLossProgramDao(): WeightLossProgramDao = _weightLossProgramDao.value

  public override fun behavioralCheckInDao(): BehavioralCheckInDao = _behavioralCheckInDao.value

  public override fun progressPhotoDao(): ProgressPhotoDao = _progressPhotoDao.value

  public override fun workoutPerformanceDao(): WorkoutPerformanceDao = _workoutPerformanceDao.value

  public override fun workoutSessionDao(): WorkoutSessionDao = _workoutSessionDao.value

  public override fun exerciseProgressionDao(): ExerciseProgressionDao =
      _exerciseProgressionDao.value

  public override fun cookingSessionDao(): CookingSessionDao = _cookingSessionDao.value

  public override fun cookingTimerDao(): CookingTimerDao = _cookingTimerDao.value

  public override fun healthStepsDao(): HealthStepsDao = _healthStepsDao.value

  public override fun healthHeartRateDao(): HealthHeartRateDao = _healthHeartRateDao.value

  public override fun healthCalorieDao(): HealthCalorieDao = _healthCalorieDao.value

  public override fun healthSleepDao(): HealthSleepDao = _healthSleepDao.value

  public override fun healthExerciseSessionDao(): HealthExerciseSessionDao =
      _healthExerciseSessionDao.value

  public override fun cloudSyncDao(): CloudSyncDao = _cloudSyncDao.value

  public override fun userProfileDao(): UserProfileDao = _userProfileDao.value

  public override fun syncConflictDao(): SyncConflictDao = _syncConflictDao.value

  public override fun socialChallengeDao(): SocialChallengeDao = _socialChallengeDao.value

  public override fun challengeParticipationDao(): ChallengeParticipationDao =
      _challengeParticipationDao.value

  public override fun challengeProgressLogDao(): ChallengeProgressLogDao =
      _challengeProgressLogDao.value

  public override fun socialBadgeDao(): SocialBadgeDao = _socialBadgeDao.value

  public override fun leaderboardEntryDao(): LeaderboardEntryDao = _leaderboardEntryDao.value
}
