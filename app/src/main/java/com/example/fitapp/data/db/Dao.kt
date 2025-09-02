package com.example.fitapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Transaction
    suspend fun upsertAndAddToHistory(recipe: RecipeEntity) {
        upsertRecipe(recipe)
        insertHistory(RecipeHistoryEntity(recipeId = recipe.id))
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(fav: RecipeFavoriteEntity)

    @Query("DELETE FROM recipe_favorites WHERE recipeId = :recipeId")
    suspend fun removeFavorite(recipeId: String)

    @Query("""
        SELECT r.* FROM recipes r
        INNER JOIN recipe_favorites f ON r.id = f.recipeId
        ORDER BY f.savedAt DESC
    """)
    fun favoritesFlow(): Flow<List<RecipeEntity>>

    @Insert
    suspend fun insertHistory(history: RecipeHistoryEntity)

    @Query("""
        SELECT r.* FROM recipes r
        INNER JOIN recipe_history h ON r.id = h.recipeId
        ORDER BY h.createdAt DESC
    """)
    fun historyFlow(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipe(id: String): RecipeEntity?
}

@Dao
interface IntakeDao {
    @Insert
    suspend fun insert(entry: IntakeEntryEntity)

    @Query("""
        SELECT COALESCE(SUM(kcal),0) FROM intake_entries
        WHERE date(datetime(timestamp,'unixepoch')) = date(:epochSec,'unixepoch','localtime')
    """)
    suspend fun totalForDay(epochSec: Long): Int

    @Query("""
        SELECT * FROM intake_entries
        WHERE date(datetime(timestamp,'unixepoch')) = date(:epochSec,'unixepoch','localtime')
        ORDER BY timestamp DESC
    """)
    fun dayEntriesFlow(epochSec: Long): Flow<List<IntakeEntryEntity>>
}

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: DailyGoalEntity)

    @Query("SELECT * FROM daily_goals WHERE dateIso = :dateIso")
    fun goalFlow(dateIso: String): Flow<DailyGoalEntity?>
}

@Dao
interface ShoppingDao {
    @Insert
    suspend fun insert(item: ShoppingItemEntity)

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM shopping_items ORDER BY category, name")
    fun itemsFlow(): Flow<List<ShoppingItemEntity>>
    
    @Query("SELECT * FROM shopping_items ORDER BY checked, createdAt DESC")
    fun itemsFlowByDate(): Flow<List<ShoppingItemEntity>>

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)
    
    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun deleteCheckedItems()
}

@Dao
interface SavedRecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: SavedRecipeEntity)

    @Update
    suspend fun update(recipe: SavedRecipeEntity)

    @Query("DELETE FROM saved_recipes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM saved_recipes ORDER BY createdAt DESC")
    fun allRecipesFlow(): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun favoriteRecipesFlow(): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun recipesByTagFlow(tag: String): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchRecipesFlow(query: String): Flow<List<SavedRecipeEntity>>

    @Query("SELECT * FROM saved_recipes WHERE id = :id")
    suspend fun getRecipe(id: String): SavedRecipeEntity?

    @Query("UPDATE saved_recipes SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("UPDATE saved_recipes SET lastCookedAt = :timestamp WHERE id = :id")
    suspend fun markAsCooked(id: String, timestamp: Long = System.currentTimeMillis() / 1000)
}

@Dao
interface ShoppingCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: ShoppingCategoryEntity)

    @Query("SELECT * FROM shopping_list_categories ORDER BY \"order\"")
    fun categoriesFlow(): Flow<List<ShoppingCategoryEntity>>

    @Query("SELECT * FROM shopping_list_categories ORDER BY \"order\"")
    suspend fun getCategories(): List<ShoppingCategoryEntity>
}

@Dao
interface PlanDao {
    @Insert
    suspend fun insert(plan: PlanEntity): Long

    @Query("SELECT * FROM training_plans ORDER BY createdAt DESC")
    fun plansFlow(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM training_plans WHERE id = :id")
    suspend fun getPlan(id: Long): PlanEntity?

    @Query("SELECT * FROM training_plans ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestPlan(): PlanEntity?

    @Query("DELETE FROM training_plans WHERE id = :id")
    suspend fun delete(id: Long)
    
    @Query("DELETE FROM training_plans")
    suspend fun deleteAll()
}

@Dao
interface TodayWorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workout: TodayWorkoutEntity)

    @Query("SELECT * FROM today_workouts WHERE dateIso = :dateIso")
    suspend fun getByDate(dateIso: String): TodayWorkoutEntity?

    @Query("UPDATE today_workouts SET status = :status, completedAt = :completedAt WHERE dateIso = :dateIso")
    suspend fun setStatus(dateIso: String, status: String, completedAt: Long?)

    @Query("SELECT * FROM today_workouts WHERE dateIso BETWEEN :fromIso AND :toIso ORDER BY dateIso DESC")
    suspend fun getBetween(fromIso: String, toIso: String): List<TodayWorkoutEntity>
}

@Dao
interface PersonalAchievementDao {
    @Insert
    suspend fun insert(achievement: PersonalAchievementEntity): Long

    @Update
    suspend fun update(achievement: PersonalAchievementEntity)

    @Query("DELETE FROM personal_achievements WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM personal_achievements ORDER BY createdAt DESC")
    fun allAchievementsFlow(): Flow<List<PersonalAchievementEntity>>

    @Query("SELECT * FROM personal_achievements WHERE category = :category ORDER BY createdAt DESC")
    fun achievementsByCategoryFlow(category: String): Flow<List<PersonalAchievementEntity>>

    @Query("SELECT * FROM personal_achievements WHERE isCompleted = :completed ORDER BY createdAt DESC")
    fun achievementsByCompletionFlow(completed: Boolean): Flow<List<PersonalAchievementEntity>>

    @Query("UPDATE personal_achievements SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun markAsCompleted(id: Long, completed: Boolean, completedAt: Long?)

    @Query("UPDATE personal_achievements SET currentValue = :value WHERE id = :id")
    suspend fun updateProgress(id: Long, value: Double)

    @Query("SELECT * FROM personal_achievements WHERE id = :id")
    suspend fun getAchievement(id: Long): PersonalAchievementEntity?
}

@Dao
interface PersonalStreakDao {
    @Insert
    suspend fun insert(streak: PersonalStreakEntity): Long

    @Update
    suspend fun update(streak: PersonalStreakEntity)

    @Query("DELETE FROM personal_streaks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM personal_streaks WHERE isActive = 1 ORDER BY currentStreak DESC")
    fun activeStreaksFlow(): Flow<List<PersonalStreakEntity>>

    @Query("SELECT * FROM personal_streaks ORDER BY longestStreak DESC")
    fun allStreaksFlow(): Flow<List<PersonalStreakEntity>>

    @Query("SELECT * FROM personal_streaks WHERE category = :category ORDER BY currentStreak DESC")
    fun streaksByCategoryFlow(category: String): Flow<List<PersonalStreakEntity>>

    @Query("UPDATE personal_streaks SET currentStreak = :currentStreak, longestStreak = :longestStreak, lastActivityTimestamp = :lastActivityTimestamp WHERE id = :id")
    suspend fun updateStreak(id: Long, currentStreak: Int, longestStreak: Int, lastActivityTimestamp: Long?)

    @Query("UPDATE personal_streaks SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("SELECT * FROM personal_streaks WHERE id = :id")
    suspend fun getStreak(id: Long): PersonalStreakEntity?
}

@Dao
interface PersonalRecordDao {
    @Insert
    suspend fun insert(record: PersonalRecordEntity): Long

    @Update
    suspend fun update(record: PersonalRecordEntity)

    @Query("DELETE FROM personal_records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM personal_records ORDER BY achievedAt DESC")
    fun allRecordsFlow(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseName = :exerciseName ORDER BY achievedAt DESC")
    fun recordsByExerciseFlow(exerciseName: String): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE recordType = :recordType ORDER BY achievedAt DESC")
    fun recordsByTypeFlow(recordType: String): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseName = :exerciseName AND recordType = :recordType ORDER BY value DESC LIMIT 1")
    suspend fun getBestRecord(exerciseName: String, recordType: String): PersonalRecordEntity?

    @Query("SELECT DISTINCT exerciseName FROM personal_records ORDER BY exerciseName")
    suspend fun getExerciseNames(): List<String>

    @Query("SELECT * FROM personal_records WHERE id = :id")
    suspend fun getRecord(id: Long): PersonalRecordEntity?
}

@Dao
interface ProgressMilestoneDao {
    @Insert
    suspend fun insert(milestone: ProgressMilestoneEntity): Long

    @Update
    suspend fun update(milestone: ProgressMilestoneEntity)

    @Query("DELETE FROM progress_milestones WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM progress_milestones ORDER BY createdAt DESC")
    fun allMilestonesFlow(): Flow<List<ProgressMilestoneEntity>>

    @Query("SELECT * FROM progress_milestones WHERE category = :category ORDER BY createdAt DESC")
    fun milestonesByCategoryFlow(category: String): Flow<List<ProgressMilestoneEntity>>

    @Query("SELECT * FROM progress_milestones WHERE isCompleted = :completed ORDER BY createdAt DESC")
    fun milestonesByCompletionFlow(completed: Boolean): Flow<List<ProgressMilestoneEntity>>

    @Query("UPDATE progress_milestones SET currentValue = :value, progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: Long, value: Double, progress: Double)

    @Query("UPDATE progress_milestones SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun markAsCompleted(id: Long, completed: Boolean, completedAt: Long?)

    @Query("SELECT * FROM progress_milestones WHERE id = :id")
    suspend fun getMilestone(id: Long): ProgressMilestoneEntity?
}

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(weight: WeightEntity): Long

    @Update
    suspend fun update(weight: WeightEntity)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM weight_entries ORDER BY dateIso DESC")
    fun allWeightsFlow(): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weight_entries WHERE dateIso = :dateIso")
    suspend fun getByDate(dateIso: String): WeightEntity?

    @Query("SELECT * FROM weight_entries ORDER BY dateIso DESC LIMIT 1")
    suspend fun getLatest(): WeightEntity?

    @Query("SELECT * FROM weight_entries WHERE dateIso BETWEEN :fromIso AND :toIso ORDER BY dateIso DESC")
    suspend fun getBetween(fromIso: String, toIso: String): List<WeightEntity>

    @Query("SELECT COUNT(*) FROM weight_entries WHERE dateIso = :dateIso")
    suspend fun hasEntryForDate(dateIso: String): Int
}

@Dao
interface FoodItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItemEntity)

    @Update
    suspend fun update(foodItem: FoodItemEntity)

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getById(id: String): FoodItemEntity?

    @Query("SELECT * FROM food_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): FoodItemEntity?

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT :limit")
    suspend fun searchByName(query: String, limit: Int = 20): List<FoodItemEntity>

    @Query("SELECT * FROM food_items ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<FoodItemEntity>

    @Query("SELECT * FROM food_items ORDER BY name")
    fun allFoodItemsFlow(): Flow<List<FoodItemEntity>>
}

@Dao
interface MealEntryDao {
    @Insert
    suspend fun insert(mealEntry: MealEntryEntity): Long

    @Update
    suspend fun update(mealEntry: MealEntryEntity)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY id")
    suspend fun getByDate(date: String): List<MealEntryEntity>

    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY id")
    fun getByDateFlow(date: String): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE date = :date AND mealType = :mealType ORDER BY id")
    suspend fun getByDateAndMealType(date: String, mealType: String): List<MealEntryEntity>

    @Query("SELECT * FROM meal_entries WHERE date = :date AND mealType = :mealType ORDER BY id")
    fun getByDateAndMealTypeFlow(date: String, mealType: String): Flow<List<MealEntryEntity>>

    @Query("""
        SELECT SUM(
            CASE mealType
                WHEN 'breakfast' THEN (quantityGrams / 100.0) * (SELECT calories FROM food_items WHERE id = foodItemId)
                WHEN 'lunch' THEN (quantityGrams / 100.0) * (SELECT calories FROM food_items WHERE id = foodItemId)
                WHEN 'dinner' THEN (quantityGrams / 100.0) * (SELECT calories FROM food_items WHERE id = foodItemId)
                WHEN 'snack' THEN (quantityGrams / 100.0) * (SELECT calories FROM food_items WHERE id = foodItemId)
                ELSE 0
            END
        ) FROM meal_entries WHERE date = :date
    """)
    suspend fun getTotalCaloriesForDate(date: String): Float?

    @Query("""
        SELECT SUM((quantityGrams / 100.0) * (SELECT carbs FROM food_items WHERE id = foodItemId))
        FROM meal_entries WHERE date = :date
    """)
    suspend fun getTotalCarbsForDate(date: String): Float?

    @Query("""
        SELECT SUM((quantityGrams / 100.0) * (SELECT protein FROM food_items WHERE id = foodItemId))
        FROM meal_entries WHERE date = :date
    """)
    suspend fun getTotalProteinForDate(date: String): Float?

    @Query("""
        SELECT SUM((quantityGrams / 100.0) * (SELECT fat FROM food_items WHERE id = foodItemId))
        FROM meal_entries WHERE date = :date
    """)
    suspend fun getTotalFatForDate(date: String): Float?
}

@Dao
interface WaterEntryDao {
    @Insert
    suspend fun insert(waterEntry: WaterEntryEntity): Long

    @Update
    suspend fun update(waterEntry: WaterEntryEntity)

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp")
    suspend fun getByDate(date: String): List<WaterEntryEntity>

    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp")
    fun getByDateFlow(date: String): Flow<List<WaterEntryEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE date = :date")
    suspend fun getTotalWaterForDate(date: String): Int

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE date = :date")
    fun getTotalWaterForDateFlow(date: String): Flow<Int>

    @Query("DELETE FROM water_entries WHERE date = :date")
    suspend fun clearForDate(date: String)
}

@Dao
interface BMIHistoryDao {
    @Insert
    suspend fun insert(bmiHistory: BMIHistoryEntity): Long

    @Update
    suspend fun update(bmiHistory: BMIHistoryEntity)

    @Query("DELETE FROM bmi_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM bmi_history WHERE date = :date")
    suspend fun getByDate(date: String): BMIHistoryEntity?

    @Query("SELECT * FROM bmi_history ORDER BY date DESC")
    suspend fun getAll(): List<BMIHistoryEntity>

    @Query("SELECT * FROM bmi_history ORDER BY date DESC")
    fun getAllFlow(): Flow<List<BMIHistoryEntity>>

    @Query("SELECT * FROM bmi_history ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<BMIHistoryEntity>

    @Query("SELECT * FROM bmi_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    suspend fun getByDateRange(startDate: String, endDate: String): List<BMIHistoryEntity>
}

@Dao
interface WeightLossProgramDao {
    @Insert
    suspend fun insert(program: WeightLossProgramEntity): Long

    @Update
    suspend fun update(program: WeightLossProgramEntity)

    @Query("DELETE FROM weight_loss_programs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM weight_loss_programs WHERE id = :id")
    suspend fun getById(id: Long): WeightLossProgramEntity?

    @Query("SELECT * FROM weight_loss_programs WHERE isActive = 1 ORDER BY startDate DESC LIMIT 1")
    suspend fun getActiveProgram(): WeightLossProgramEntity?

    @Query("SELECT * FROM weight_loss_programs WHERE isActive = 1 ORDER BY startDate DESC LIMIT 1")
    fun getActiveProgramFlow(): Flow<WeightLossProgramEntity?>

    @Query("SELECT * FROM weight_loss_programs ORDER BY startDate DESC")
    suspend fun getAll(): List<WeightLossProgramEntity>

    @Query("SELECT * FROM weight_loss_programs ORDER BY startDate DESC")
    fun getAllFlow(): Flow<List<WeightLossProgramEntity>>

    @Query("UPDATE weight_loss_programs SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAllPrograms()
}

@Dao
interface BehavioralCheckInDao {
    @Insert
    suspend fun insert(checkIn: BehavioralCheckInEntity): Long

    @Update
    suspend fun update(checkIn: BehavioralCheckInEntity)

    @Query("DELETE FROM behavioral_check_ins WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM behavioral_check_ins WHERE id = :id")
    suspend fun getById(id: Long): BehavioralCheckInEntity?

    @Query("SELECT * FROM behavioral_check_ins ORDER BY timestamp DESC")
    suspend fun getAll(): List<BehavioralCheckInEntity>

    @Query("SELECT * FROM behavioral_check_ins ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<BehavioralCheckInEntity>>

    @Query("SELECT * FROM behavioral_check_ins ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<BehavioralCheckInEntity>

    @Query("""
        SELECT * FROM behavioral_check_ins 
        WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY timestamp
    """)
    suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long): List<BehavioralCheckInEntity>
}

@Dao
interface ProgressPhotoDao {
    @Insert
    suspend fun insert(photo: ProgressPhotoEntity): Long

    @Update
    suspend fun update(photo: ProgressPhotoEntity)

    @Query("DELETE FROM progress_photos WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM progress_photos WHERE id = :id")
    suspend fun getById(id: Long): ProgressPhotoEntity?

    @Query("SELECT * FROM progress_photos ORDER BY timestamp DESC")
    suspend fun getAll(): List<ProgressPhotoEntity>

    @Query("SELECT * FROM progress_photos ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT * FROM progress_photos ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ProgressPhotoEntity>

    @Query("""
        SELECT * FROM progress_photos 
        WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY timestamp
    """)
    suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long): List<ProgressPhotoEntity>
}

@Dao
interface CookingSessionDao {
    @Insert
    suspend fun insert(session: CookingSessionEntity): Long

    @Update
    suspend fun update(session: CookingSessionEntity)

    @Query("DELETE FROM cooking_sessions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM cooking_sessions WHERE id = :id")
    suspend fun getById(id: String): CookingSessionEntity?

    @Query("SELECT * FROM cooking_sessions WHERE recipeId = :recipeId ORDER BY startTime DESC")
    suspend fun getByRecipeId(recipeId: String): List<CookingSessionEntity>

    @Query("SELECT * FROM cooking_sessions WHERE recipeId = :recipeId ORDER BY startTime DESC")
    fun getByRecipeIdFlow(recipeId: String): Flow<List<CookingSessionEntity>>

    @Query("SELECT * FROM cooking_sessions ORDER BY startTime DESC")
    suspend fun getAll(): List<CookingSessionEntity>

    @Query("SELECT * FROM cooking_sessions ORDER BY startTime DESC")
    fun getAllFlow(): Flow<List<CookingSessionEntity>>

    @Query("SELECT * FROM cooking_sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<CookingSessionEntity>

    @Query("SELECT * FROM cooking_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): CookingSessionEntity?

    @Query("SELECT * FROM cooking_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<CookingSessionEntity?>

    @Query("UPDATE cooking_sessions SET endTime = :endTime, rating = :rating, notes = :notes WHERE id = :id")
    suspend fun finishSession(id: String, endTime: Long, rating: Int?, notes: String?)
}

@Dao
interface CookingTimerDao {
    @Insert
    suspend fun insert(timer: CookingTimerEntity): Long

    @Update
    suspend fun update(timer: CookingTimerEntity)

    @Query("DELETE FROM cooking_timers WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM cooking_timers WHERE id = :id")
    suspend fun getById(id: String): CookingTimerEntity?

    @Query("SELECT * FROM cooking_timers WHERE sessionId = :sessionId ORDER BY createdAt")
    suspend fun getBySessionId(sessionId: String): List<CookingTimerEntity>

    @Query("SELECT * FROM cooking_timers WHERE sessionId = :sessionId ORDER BY createdAt")
    fun getBySessionIdFlow(sessionId: String): Flow<List<CookingTimerEntity>>

    @Query("SELECT * FROM cooking_timers WHERE isActive = 1 ORDER BY createdAt")
    suspend fun getActiveTimers(): List<CookingTimerEntity>

    @Query("SELECT * FROM cooking_timers WHERE isActive = 1 ORDER BY createdAt")
    fun getActiveTimersFlow(): Flow<List<CookingTimerEntity>>

    @Query("UPDATE cooking_timers SET remainingTime = :remainingTime WHERE id = :id")
    suspend fun updateRemainingTime(id: String, remainingTime: Long)

    @Query("UPDATE cooking_timers SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean)

    @Query("UPDATE cooking_timers SET isActive = 0 WHERE id = :id")
    suspend fun stopTimer(id: String)

    @Query("DELETE FROM cooking_timers WHERE sessionId = :sessionId")
    suspend fun clearSessionTimers(sessionId: String)
}

