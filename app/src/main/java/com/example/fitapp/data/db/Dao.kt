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

    @Query("SELECT r.* FROM recipes r WHERE id = :id")
    suspend fun getRecipe(id: String): RecipeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(entity: RecipeFavoriteEntity)
    
    @Query("DELETE FROM recipe_favorites WHERE recipeId = :recipeId AND category = :category")
    suspend fun removeFromFavoriteCategory(recipeId: String, category: String)
    
    @Query("DELETE FROM recipe_favorites WHERE recipeId = :recipeId")
    suspend fun removeFromAllFavoriteCategories(recipeId: String)
    
    @Query("SELECT * FROM recipe_favorites")
    suspend fun getAllFavoriteCategories(): List<RecipeFavoriteEntity>
    
    @Query("SELECT recipeId FROM recipe_favorites WHERE category = :category")
    suspend fun getFavoriteRecipesByCategory(category: String): List<String>
    
    // Enhanced recipe search and filtering (YAZIO-style)
    @Query("""
        SELECT * FROM recipes 
        WHERE (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%' 
               OR description LIKE '%' || :searchQuery || '%')
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:maxPrepTime IS NULL OR prepTime <= :maxPrepTime)
        AND (:maxCookTime IS NULL OR cookTime <= :maxCookTime)
        AND (:maxCalories IS NULL OR calories <= :maxCalories)
        AND (:isOfficial IS NULL OR isOfficial = :isOfficial)
        ORDER BY 
            CASE :sortBy 
                WHEN 'name' THEN title
                WHEN 'difficulty' THEN difficulty
                WHEN 'prepTime' THEN CAST(prepTime AS TEXT)
                WHEN 'calories' THEN CAST(calories AS TEXT)
                ELSE CAST(createdAt AS TEXT)
            END
    """)
    suspend fun searchRecipesFiltered(
        searchQuery: String? = null,
        difficulty: String? = null,
        maxPrepTime: Int? = null,
        maxCookTime: Int? = null,
        maxCalories: Int? = null,
        isOfficial: Boolean? = null,
        sortBy: String = "createdAt"
    ): List<RecipeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(ingredient: RecipeIngredientEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeStep(step: RecipeStepEntity)
    
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY ingredientOrder")
    suspend fun getRecipeIngredients(recipeId: String): List<RecipeIngredientEntity>
    
    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId ORDER BY stepOrder")
    suspend fun getRecipeSteps(recipeId: String): List<RecipeStepEntity>
    
    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: String)
    
    @Query("DELETE FROM recipe_steps WHERE recipeId = :recipeId")
    suspend fun deleteRecipeSteps(recipeId: String)
}

// YAZIO-style Meal Management DAO
@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)
    
    @Update
    suspend fun updateMeal(meal: MealEntity)
    
    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: String)
    
    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMeal(id: String): MealEntity?
    
    @Query("SELECT * FROM meals WHERE mealType = :mealType ORDER BY lastUsedAt DESC, createdAt DESC")
    fun getMealsByType(mealType: String): Flow<List<MealEntity>>
    
    @Query("SELECT * FROM meals ORDER BY lastUsedAt DESC, createdAt DESC")
    fun getAllMeals(): Flow<List<MealEntity>>
    
    @Query("UPDATE meals SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long = System.currentTimeMillis() / 1000)
}

// Smart Grocery Lists DAO (YAZIO-style)
@Dao 
interface GroceryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryList(list: GroceryListEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryItemEntity)
    
    @Update
    suspend fun updateGroceryList(list: GroceryListEntity)
    
    @Update
    suspend fun updateGroceryItem(item: GroceryItemEntity)
    
    @Query("DELETE FROM grocery_lists WHERE id = :id")
    suspend fun deleteGroceryList(id: String)
    
    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun deleteGroceryItem(id: String)
    
    @Query("SELECT * FROM grocery_lists WHERE isActive = 1 ORDER BY isDefault DESC, createdAt DESC")
    fun getActiveLists(): Flow<List<GroceryListEntity>>
    
    @Query("SELECT * FROM grocery_items WHERE listId = :listId ORDER BY category, name")
    fun getListItems(listId: String): Flow<List<GroceryItemEntity>>
    
    @Query("""
        SELECT * FROM grocery_items 
        WHERE listId = :listId AND category = :category 
        ORDER BY checked ASC, name ASC
    """)
    suspend fun getItemsByCategory(listId: String, category: String): List<GroceryItemEntity>
    
    @Query("SELECT DISTINCT category FROM grocery_items WHERE listId = :listId ORDER BY category")
    suspend fun getListCategories(listId: String): List<String>
    
    // Smart quantity merging for recipes
    @Query("""
        SELECT * FROM grocery_items 
        WHERE listId = :listId AND name = :itemName AND unit = :unit
        ORDER BY addedAt DESC
    """)
    suspend fun findSimilarItems(listId: String, itemName: String, unit: String): List<GroceryItemEntity>
    
    @Query("UPDATE grocery_items SET checked = :checked, checkedAt = :timestamp WHERE id = :id")
    suspend fun updateItemChecked(id: String, checked: Boolean, timestamp: Long = System.currentTimeMillis() / 1000)
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

    @Query("DELETE FROM intake_entries")
    suspend fun deleteAll()
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
    
    @Query("UPDATE shopping_items SET quantity = :quantity, unit = :unit WHERE id = :id")
    suspend fun updateQuantityAndUnit(id: Long, quantity: Double, unit: String)
    
    @Query("DELETE FROM shopping_items")
    suspend fun deleteAll()
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
    
    @Query("SELECT * FROM saved_recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): SavedRecipeEntity?

    @Query("SELECT * FROM saved_recipes WHERE id IN (:ids)")
    suspend fun getRecipesByIds(ids: List<String>): List<SavedRecipeEntity>

    @Query("UPDATE saved_recipes SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("UPDATE saved_recipes SET lastCookedAt = :timestamp WHERE id = :id")
    suspend fun markAsCooked(id: String, timestamp: Long = System.currentTimeMillis() / 1000)
    
    @Query("DELETE FROM saved_recipes")
    suspend fun deleteAll()
}

@Dao
interface ShoppingCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: ShoppingCategoryEntity)

    @Query("SELECT * FROM shopping_list_categories ORDER BY \"order\"")
    fun categoriesFlow(): Flow<List<ShoppingCategoryEntity>>

    @Query("SELECT * FROM shopping_list_categories ORDER BY \"order\"")
    suspend fun getCategories(): List<ShoppingCategoryEntity>
    
    @Query("DELETE FROM shopping_list_categories")
    suspend fun deleteAll()
}

@Dao
interface PlanDao {
    @Insert
    suspend fun insert(plan: PlanEntity): Long

    @Query("SELECT * FROM training_plans ORDER BY createdAt DESC")
    fun plansFlow(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM training_plans WHERE id = :id")
    suspend fun getPlan(id: Long): PlanEntity?

    @Query("SELECT * FROM training_plans WHERE id = :id")
    suspend fun getById(id: Long): PlanEntity?

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

    @Query("DELETE FROM today_workouts")
    suspend fun deleteAll()
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
    
    @Query("UPDATE personal_achievements SET isCompleted = 0, completedAt = NULL, currentValue = 0.0")
    suspend fun resetAllAchievements()
    
    @Query("DELETE FROM personal_achievements")
    suspend fun deleteAll()
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
    
    @Query("UPDATE personal_streaks SET currentStreak = 0, lastActivityTimestamp = NULL")
    suspend fun resetAllStreaks()
    
    @Query("DELETE FROM personal_streaks")
    suspend fun deleteAll()
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

    @Query("SELECT * FROM personal_records WHERE exerciseName = :exerciseName AND recordType = :recordType ORDER BY achievedAt DESC LIMIT 1")
    suspend fun getRecord(exerciseName: String, recordType: String): PersonalRecordEntity?

    @Query("SELECT DISTINCT exerciseName FROM personal_records ORDER BY exerciseName")
    suspend fun getExerciseNames(): List<String>

    @Query("SELECT * FROM personal_records WHERE id = :id")
    suspend fun getRecord(id: Long): PersonalRecordEntity?
    
    @Query("DELETE FROM personal_records")
    suspend fun deleteAll()
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
    
    @Query("DELETE FROM progress_milestones")
    suspend fun deleteAll()
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

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAll()
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

    @Query("DELETE FROM food_items")
    suspend fun deleteAll()
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

    @Query("DELETE FROM meal_entries")
    suspend fun deleteAll()
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

    @Query("DELETE FROM water_entries")
    suspend fun deleteAll()
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

    @Query("DELETE FROM bmi_history")
    suspend fun deleteAll()
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

    @Query("DELETE FROM weight_loss_programs")
    suspend fun deleteAll()
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

    @Query("DELETE FROM behavioral_check_ins")
    suspend fun deleteAll()
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

// Advanced Workout Execution Enhancement - Phase 1 DAOs

@Dao
interface WorkoutPerformanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(performance: WorkoutPerformanceEntity)

    @Update
    suspend fun update(performance: WorkoutPerformanceEntity)

    @Query("DELETE FROM workout_performance WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM workout_performance WHERE id = :id")
    suspend fun getById(id: String): WorkoutPerformanceEntity?

    @Query("SELECT * FROM workout_performance WHERE sessionId = :sessionId ORDER BY exerciseIndex")
    suspend fun getBySessionId(sessionId: String): List<WorkoutPerformanceEntity>

    @Query("SELECT * FROM workout_performance WHERE sessionId = :sessionId ORDER BY exerciseIndex")
    fun getBySessionIdFlow(sessionId: String): Flow<List<WorkoutPerformanceEntity>>

    @Query("SELECT * FROM workout_performance WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    suspend fun getByExerciseId(exerciseId: String): List<WorkoutPerformanceEntity>

    @Query("SELECT * FROM workout_performance WHERE exerciseId = :exerciseId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentByExerciseId(exerciseId: String, limit: Int): List<WorkoutPerformanceEntity>

    @Query("SELECT * FROM workout_performance WHERE planId = :planId ORDER BY timestamp DESC")
    suspend fun getByPlanId(planId: Long): List<WorkoutPerformanceEntity>

    @Query("SELECT * FROM workout_performance WHERE isPersonalRecord = 1 ORDER BY timestamp DESC")
    suspend fun getPersonalRecords(): List<WorkoutPerformanceEntity>

    @Query("""
        SELECT * FROM workout_performance 
        WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY timestamp
    """)
    suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long): List<WorkoutPerformanceEntity>

    @Query("""
        SELECT AVG(volume) FROM workout_performance 
        WHERE exerciseId = :exerciseId AND timestamp >= :sinceTimestamp
    """)
    suspend fun getAverageVolumeForExercise(exerciseId: String, sinceTimestamp: Long): Float?

    @Query("""
        SELECT MAX(volume) FROM workout_performance 
        WHERE exerciseId = :exerciseId
    """)
    suspend fun getMaxVolumeForExercise(exerciseId: String): Float?

    @Query("DELETE FROM workout_performance")
    suspend fun deleteAll()
}

@Dao
interface WorkoutSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkoutSessionEntity)

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    suspend fun getAll(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllFlow(): Flow<List<WorkoutSessionEntity>>

    @Query("UPDATE workout_sessions SET pauseStartTime = :pauseTime WHERE id = :sessionId")
    suspend fun updatePauseTime(sessionId: String, pauseTime: Long)

    @Query("SELECT * FROM workout_sessions WHERE planId = :planId ORDER BY startTime DESC")
    suspend fun getByPlanId(planId: Long): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startTime DESC")
    suspend fun getByUserId(userId: String): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<WorkoutSessionEntity>

    @Query("""
        SELECT * FROM workout_sessions 
        WHERE startTime BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY startTime
    """)
    suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long): List<WorkoutSessionEntity>

    @Query("""
        SELECT AVG(workoutEfficiencyScore) FROM workout_sessions 
        WHERE userId = :userId AND startTime >= :sinceTimestamp
    """)
    suspend fun getAverageEfficiencyScore(userId: String, sinceTimestamp: Long): Float?

    @Query("""
        SELECT SUM(personalRecordsAchieved) FROM workout_sessions 
        WHERE userId = :userId AND startTime >= :sinceTimestamp
    """)
    suspend fun getTotalPersonalRecords(userId: String, sinceTimestamp: Long): Int?

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAll()
}

@Dao
interface ExerciseProgressionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progression: ExerciseProgressionEntity)

    @Update
    suspend fun update(progression: ExerciseProgressionEntity)

    @Query("DELETE FROM exercise_progressions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM exercise_progressions WHERE id = :id")
    suspend fun getById(id: String): ExerciseProgressionEntity?

    @Query("SELECT * FROM exercise_progressions WHERE exerciseId = :exerciseId AND userId = :userId")
    suspend fun getByExerciseAndUser(exerciseId: String, userId: String): ExerciseProgressionEntity?

    @Query("SELECT * FROM exercise_progressions WHERE userId = :userId ORDER BY lastProgressDate DESC")
    suspend fun getByUserId(userId: String): List<ExerciseProgressionEntity>

    @Query("SELECT * FROM exercise_progressions WHERE userId = :userId ORDER BY lastProgressDate DESC")
    fun getByUserIdFlow(userId: String): Flow<List<ExerciseProgressionEntity>>

    @Query("SELECT * FROM exercise_progressions WHERE plateauDetected = 1 AND userId = :userId")
    suspend fun getPlateauedExercises(userId: String): List<ExerciseProgressionEntity>

    @Query("""
        SELECT * FROM exercise_progressions 
        WHERE performanceTrend = :trend AND userId = :userId 
        ORDER BY lastProgressDate DESC
    """)
    suspend fun getByPerformanceTrend(trend: String, userId: String): List<ExerciseProgressionEntity>

    @Query("""
        SELECT * FROM exercise_progressions 
        WHERE nextReviewDate <= :currentTimestamp AND userId = :userId
        ORDER BY nextReviewDate
    """)
    suspend fun getExercisesDueForReview(currentTimestamp: Long, userId: String): List<ExerciseProgressionEntity>

    @Query("""
        SELECT COUNT(*) FROM exercise_progressions 
        WHERE performanceTrend = 'improving' AND userId = :userId
    """)
    suspend fun getImprovingExercisesCount(userId: String): Int

    @Query("""
        SELECT AVG(aiConfidence) FROM exercise_progressions 
        WHERE userId = :userId AND lastProgressDate >= :sinceTimestamp
    """)
    suspend fun getAverageAIConfidence(userId: String, sinceTimestamp: Long): Float?

    @Query("DELETE FROM exercise_progressions")
    suspend fun deleteAll()
}

// Cooking Mode DAOs

@Dao
interface CookingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: CookingSessionEntity)

    @Update
    suspend fun update(session: CookingSessionEntity)

    @Query("DELETE FROM cooking_sessions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM cooking_sessions WHERE id = :id")
    suspend fun getById(id: String): CookingSessionEntity?

    @Query("SELECT * FROM cooking_sessions WHERE recipeId = :recipeId ORDER BY startTime DESC")
    suspend fun getByRecipeId(recipeId: String): List<CookingSessionEntity>

    @Query("SELECT * FROM cooking_sessions WHERE status = 'active' LIMIT 1")
    suspend fun getActiveSession(): CookingSessionEntity?

    @Query("SELECT * FROM cooking_sessions ORDER BY startTime DESC")
    suspend fun getAll(): List<CookingSessionEntity>

    @Query("SELECT * FROM cooking_sessions ORDER BY startTime DESC")
    fun getAllFlow(): Flow<List<CookingSessionEntity>>

    @Query("UPDATE cooking_sessions SET currentStep = :stepIndex WHERE id = :sessionId")
    suspend fun updateCurrentStep(sessionId: String, stepIndex: Int)

    @Query("UPDATE cooking_sessions SET status = :status WHERE id = :sessionId")
    suspend fun updateStatus(sessionId: String, status: String)

    @Query("""
        UPDATE cooking_sessions 
        SET endTime = :endTime, status = 'completed', actualDuration = :actualDuration 
        WHERE id = :sessionId
    """)
    suspend fun completeCookingSession(sessionId: String, endTime: Long, actualDuration: Long)

    @Query("""
        SELECT * FROM cooking_sessions 
        WHERE startTime BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY startTime
    """)
    suspend fun getByDateRange(startTimestamp: Long, endTimestamp: Long): List<CookingSessionEntity>

    @Query("SELECT COUNT(*) FROM cooking_sessions WHERE status = 'completed'")
    suspend fun getCompletedSessionsCount(): Int

    @Query("""
        SELECT AVG(actualDuration) FROM cooking_sessions 
        WHERE status = 'completed' AND actualDuration IS NOT NULL
    """)
    suspend fun getAverageCookingTime(): Float?
    
    @Query("DELETE FROM cooking_sessions")
    suspend fun deleteAll()
}

@Dao
interface CookingTimerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: CookingTimerEntity)

    @Update
    suspend fun update(timer: CookingTimerEntity)

    @Query("DELETE FROM cooking_timers WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM cooking_timers WHERE id = :id")
    suspend fun getTimerById(id: String): CookingTimerEntity?

    @Query("SELECT * FROM cooking_timers WHERE sessionId = :sessionId ORDER BY stepIndex")
    suspend fun getBySessionId(sessionId: String): List<CookingTimerEntity>

    @Query("SELECT * FROM cooking_timers WHERE sessionId = :sessionId ORDER BY stepIndex")
    fun getBySessionIdFlow(sessionId: String): Flow<List<CookingTimerEntity>>

    @Query("SELECT * FROM cooking_timers WHERE stepIndex = :stepIndex AND sessionId = :sessionId")
    suspend fun getByStepIndex(sessionId: String, stepIndex: Int): List<CookingTimerEntity>

    @Query("SELECT * FROM cooking_timers WHERE isActive = 1")
    suspend fun getActiveTimers(): List<CookingTimerEntity>

    @Query("SELECT * FROM cooking_timers WHERE isActive = 1")
    fun getActiveTimersFlow(): Flow<List<CookingTimerEntity>>

    @Query("UPDATE cooking_timers SET isPaused = :isPaused WHERE stepIndex = :stepIndex")
    suspend fun updatePauseState(stepIndex: Int, isPaused: Boolean)

    @Query("UPDATE cooking_timers SET remainingSeconds = :remainingSeconds WHERE id = :timerId")
    suspend fun updateRemainingTime(timerId: String, remainingSeconds: Long)

    @Query("""
        UPDATE cooking_timers 
        SET isActive = 0, completedAt = :completedAt 
        WHERE id = :timerId
    """)
    suspend fun completeTimer(timerId: String, completedAt: Long)

    @Query("DELETE FROM cooking_timers WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)
    
    @Query("DELETE FROM cooking_timers")
    suspend fun deleteAll()
}

// Health Connect DAOs

@Dao
interface HealthStepsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(steps: HealthStepsEntity)

    @Update
    suspend fun update(steps: HealthStepsEntity)

    @Query("DELETE FROM health_connect_steps WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM health_connect_steps WHERE date = :date ORDER BY syncedAt DESC")
    suspend fun getByDate(date: String): List<HealthStepsEntity>

    @Query("SELECT * FROM health_connect_steps WHERE date = :date AND source = :source")
    suspend fun getByDateAndSource(date: String, source: String): HealthStepsEntity?

    @Query("SELECT SUM(steps) FROM health_connect_steps WHERE date = :date")
    suspend fun getTotalStepsForDate(date: String): Int?

    @Query("""
        SELECT * FROM health_connect_steps 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    suspend fun getByDateRange(startDate: String, endDate: String): List<HealthStepsEntity>

    @Query("SELECT * FROM health_connect_steps ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<HealthStepsEntity>

    @Query("DELETE FROM health_connect_steps WHERE syncedAt < :beforeTimestamp")
    suspend fun deleteOldEntries(beforeTimestamp: Long)

    @Query("DELETE FROM health_connect_steps")
    suspend fun deleteAll()
}

@Dao
interface HealthHeartRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(heartRate: HealthHeartRateEntity)

    @Update
    suspend fun update(heartRate: HealthHeartRateEntity)

    @Query("DELETE FROM health_connect_heart_rate WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM health_connect_heart_rate WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getByDate(date: String): List<HealthHeartRateEntity>

    @Query("SELECT AVG(heartRate) FROM health_connect_heart_rate WHERE date = :date")
    suspend fun getAverageHeartRateForDate(date: String): Float?

    @Query("SELECT MAX(heartRate) FROM health_connect_heart_rate WHERE date = :date")
    suspend fun getMaxHeartRateForDate(date: String): Int?

    @Query("""
        SELECT * FROM health_connect_heart_rate 
        WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY timestamp
    """)
    suspend fun getByTimeRange(startTimestamp: Long, endTimestamp: Long): List<HealthHeartRateEntity>

    @Query("DELETE FROM health_connect_heart_rate WHERE syncedAt < :beforeTimestamp")
    suspend fun deleteOldEntries(beforeTimestamp: Long)

    @Query("DELETE FROM health_connect_heart_rate")
    suspend fun deleteAll()
}

@Dao
interface HealthCalorieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calories: HealthCalorieEntity)

    @Update
    suspend fun update(calories: HealthCalorieEntity)

    @Query("DELETE FROM health_connect_calories WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM health_connect_calories WHERE date = :date ORDER BY syncedAt DESC")
    suspend fun getByDate(date: String): List<HealthCalorieEntity>

    @Query("""
        SELECT * FROM health_connect_calories 
        WHERE date = :date AND calorieType = :type AND source = :source
    """)
    suspend fun getByDateTypeAndSource(date: String, type: String, source: String): HealthCalorieEntity?

    @Query("SELECT SUM(calories) FROM health_connect_calories WHERE date = :date AND calorieType = :type")
    suspend fun getTotalCaloriesForDateAndType(date: String, type: String): Double?

    @Query("""
        SELECT * FROM health_connect_calories 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    suspend fun getByDateRange(startDate: String, endDate: String): List<HealthCalorieEntity>

    @Query("DELETE FROM health_connect_calories WHERE syncedAt < :beforeTimestamp")
    suspend fun deleteOldEntries(beforeTimestamp: Long)

    @Query("DELETE FROM health_connect_calories")
    suspend fun deleteAll()
}

@Dao
interface HealthSleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleep: HealthSleepEntity)

    @Update
    suspend fun update(sleep: HealthSleepEntity)

    @Query("DELETE FROM health_connect_sleep WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM health_connect_sleep WHERE date = :date ORDER BY startTime")
    suspend fun getByDate(date: String): List<HealthSleepEntity>

    @Query("SELECT SUM(durationMinutes) FROM health_connect_sleep WHERE date = :date")
    suspend fun getTotalSleepForDate(date: String): Int?

    @Query("""
        SELECT SUM(durationMinutes) FROM health_connect_sleep 
        WHERE date = :date AND sleepStage = :stage
    """)
    suspend fun getSleepDurationByStage(date: String, stage: String): Int?

    @Query("""
        SELECT * FROM health_connect_sleep 
        WHERE startTime BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY startTime
    """)
    suspend fun getByTimeRange(startTimestamp: Long, endTimestamp: Long): List<HealthSleepEntity>

    @Query("DELETE FROM health_connect_sleep WHERE syncedAt < :beforeTimestamp")
    suspend fun deleteOldEntries(beforeTimestamp: Long)

    @Query("DELETE FROM health_connect_sleep")
    suspend fun deleteAll()
}

@Dao
interface HealthExerciseSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: HealthExerciseSessionEntity)

    @Update
    suspend fun update(session: HealthExerciseSessionEntity)

    @Query("DELETE FROM health_connect_exercise_sessions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM health_connect_exercise_sessions WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): HealthExerciseSessionEntity?

    @Query("SELECT * FROM health_connect_exercise_sessions WHERE date = :date ORDER BY startTime DESC")
    suspend fun getByDate(date: String): List<HealthExerciseSessionEntity>

    @Query("""
        SELECT * FROM health_connect_exercise_sessions 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY startTime DESC
    """)
    suspend fun getByDateRange(startDate: String, endDate: String): List<HealthExerciseSessionEntity>

    @Query("SELECT * FROM health_connect_exercise_sessions WHERE exerciseType = :type ORDER BY startTime DESC")
    suspend fun getByExerciseType(type: String): List<HealthExerciseSessionEntity>

    @Query("SELECT SUM(durationMinutes) FROM health_connect_exercise_sessions WHERE date = :date")
    suspend fun getTotalExerciseTimeForDate(date: String): Int?

    @Query("SELECT SUM(calories) FROM health_connect_exercise_sessions WHERE date = :date")
    suspend fun getTotalCaloriesBurnedForDate(date: String): Double?

    @Query("DELETE FROM health_connect_exercise_sessions WHERE syncedAt < :beforeTimestamp")
    suspend fun deleteOldEntries(beforeTimestamp: Long)

    @Query("DELETE FROM health_connect_exercise_sessions")
    suspend fun deleteAll()
}

// Cloud Sync DAOs for Multi-Device Support
@Dao
interface CloudSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncMetadata(metadata: CloudSyncEntity)
    
    @Query("SELECT * FROM cloud_sync_metadata WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun getSyncMetadata(entityType: String, entityId: String): CloudSyncEntity?
    
    @Query("SELECT * FROM cloud_sync_metadata WHERE syncStatus = :status")
    suspend fun getByStatus(status: String): List<CloudSyncEntity>
    
    @Query("SELECT * FROM cloud_sync_metadata WHERE syncStatus = 'pending' OR syncStatus = 'error'")
    suspend fun getPendingSync(): List<CloudSyncEntity>
    
    @Query("SELECT * FROM cloud_sync_metadata WHERE deviceId = :deviceId")
    suspend fun getByDeviceId(deviceId: String): List<CloudSyncEntity>
    
    @Query("UPDATE cloud_sync_metadata SET syncStatus = :status, lastSyncTime = :timestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, timestamp: Long)
    
    @Query("UPDATE cloud_sync_metadata SET retryCount = retryCount + 1, errorMessage = :error WHERE id = :id")
    suspend fun incrementRetryCount(id: String, error: String?)
    
    @Query("DELETE FROM cloud_sync_metadata WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteSyncMetadata(entityType: String, entityId: String)
    
    @Query("DELETE FROM cloud_sync_metadata WHERE lastSyncTime < :cutoffTime")
    suspend fun cleanupOldMetadata(cutoffTime: Long)
}

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(profile: UserProfileEntity)
    
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?
    
    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveUserProfile(): UserProfileEntity?
    
    @Query("SELECT * FROM user_profiles WHERE deviceId = :deviceId")
    suspend fun getByDeviceId(deviceId: String): UserProfileEntity?
    
    @Query("UPDATE user_profiles SET lastSyncTime = :timestamp WHERE userId = :userId")
    suspend fun updateLastSyncTime(userId: String, timestamp: Long)
    
    @Query("UPDATE user_profiles SET syncPreferences = :preferences WHERE userId = :userId")
    suspend fun updateSyncPreferences(userId: String, preferences: String)
    
    @Query("UPDATE user_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()
    
    @Query("UPDATE user_profiles SET isActive = 1 WHERE userId = :userId")
    suspend fun activateProfile(userId: String)
    
    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String)
}

@Dao
interface SyncConflictDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: SyncConflictEntity)
    
    @Query("SELECT * FROM sync_conflicts WHERE status = 'pending'")
    suspend fun getPendingConflicts(): List<SyncConflictEntity>
    
    @Query("SELECT * FROM sync_conflicts WHERE entityType = :entityType AND entityId = :entityId AND status = 'pending'")
    suspend fun getConflictForEntity(entityType: String, entityId: String): SyncConflictEntity?
    
    @Query("UPDATE sync_conflicts SET status = :status, resolution = :resolution, resolvedData = :resolvedData, resolvedBy = :resolvedBy, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun resolveConflict(id: String, status: String, resolution: String, resolvedData: String?, resolvedBy: String, resolvedAt: Long)
    
    @Query("DELETE FROM sync_conflicts WHERE id = :id")
    suspend fun deleteConflict(id: String)
    
    @Query("DELETE FROM sync_conflicts WHERE createdAt < :cutoffTime AND status != 'pending'")
    suspend fun cleanupResolvedConflicts(cutoffTime: Long)
    
    @Query("SELECT COUNT(*) FROM sync_conflicts WHERE status = 'pending'")
    fun getPendingConflictCount(): Flow<Int>
}

// Social Challenge DAOs for Freeletics-style gamification

@Dao
interface SocialChallengeDao {
    @Insert
    suspend fun insert(challenge: SocialChallengeEntity): Long

    @Update
    suspend fun update(challenge: SocialChallengeEntity)

    @Query("DELETE FROM social_challenges WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM social_challenges ORDER BY startDate DESC")
    fun allChallengesFlow(): Flow<List<SocialChallengeEntity>>

    @Query("SELECT * FROM social_challenges WHERE status = :status ORDER BY startDate DESC")
    fun challengesByStatusFlow(status: String): Flow<List<SocialChallengeEntity>>

    @Query("SELECT * FROM social_challenges WHERE category = :category ORDER BY startDate DESC")
    fun challengesByCategoryFlow(category: String): Flow<List<SocialChallengeEntity>>

    @Query("SELECT * FROM social_challenges WHERE status = 'active' ORDER BY startDate ASC")
    fun activeChallengesFlow(): Flow<List<SocialChallengeEntity>>

    @Query("SELECT * FROM social_challenges WHERE isOfficial = 1 ORDER BY startDate DESC")
    fun officialChallengesFlow(): Flow<List<SocialChallengeEntity>>

    @Query("SELECT * FROM social_challenges WHERE id = :id")
    suspend fun getChallenge(id: Long): SocialChallengeEntity?

    @Query("UPDATE social_challenges SET currentParticipants = :count WHERE id = :id")
    suspend fun updateParticipantCount(id: Long, count: Int)

    @Query("UPDATE social_challenges SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

@Dao
interface ChallengeParticipationDao {
    @Insert
    suspend fun insert(participation: ChallengeParticipationEntity): Long

    @Update
    suspend fun update(participation: ChallengeParticipationEntity)

    @Query("DELETE FROM challenge_participations WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM challenge_participations WHERE challengeId = :challengeId ORDER BY currentProgress DESC")
    fun participationsByChallengeFlow(challengeId: Long): Flow<List<ChallengeParticipationEntity>>

    @Query("SELECT * FROM challenge_participations WHERE userId = :userId ORDER BY joinedAt DESC")
    fun participationsByUserFlow(userId: String): Flow<List<ChallengeParticipationEntity>>

    @Query("SELECT * FROM challenge_participations WHERE challengeId = :challengeId AND userId = :userId")
    suspend fun getUserParticipation(challengeId: Long, userId: String): ChallengeParticipationEntity?

    @Query("UPDATE challenge_participations SET currentProgress = :progress, progressPercentage = :percentage, lastActivityDate = :date WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Double, percentage: Double, date: String)

    @Query("UPDATE challenge_participations SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?)

    @Query("UPDATE challenge_participations SET rank = :rank WHERE id = :id")
    suspend fun updateRank(id: Long, rank: Int)

    @Query("SELECT COUNT(*) FROM challenge_participations WHERE challengeId = :challengeId")
    suspend fun getParticipantCount(challengeId: Long): Int

    @Query("SELECT * FROM challenge_participations WHERE challengeId = :challengeId ORDER BY currentProgress DESC LIMIT :limit")
    suspend fun getTopParticipants(challengeId: Long, limit: Int): List<ChallengeParticipationEntity>
}

@Dao
interface ChallengeProgressLogDao {
    @Insert
    suspend fun insert(log: ChallengeProgressLogEntity): Long

    @Update
    suspend fun update(log: ChallengeProgressLogEntity)

    @Query("DELETE FROM challenge_progress_logs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM challenge_progress_logs WHERE participationId = :participationId ORDER BY timestamp DESC")
    fun logsByParticipationFlow(participationId: Long): Flow<List<ChallengeProgressLogEntity>>

    @Query("SELECT * FROM challenge_progress_logs WHERE participationId = :participationId AND logDate = :date")
    suspend fun getLogForDate(participationId: Long, date: String): ChallengeProgressLogEntity?

    @Query("SELECT SUM(value) FROM challenge_progress_logs WHERE participationId = :participationId")
    suspend fun getTotalProgress(participationId: Long): Double?

    @Query("SELECT SUM(value) FROM challenge_progress_logs WHERE participationId = :participationId AND logDate BETWEEN :startDate AND :endDate")
    suspend fun getProgressBetweenDates(participationId: Long, startDate: String, endDate: String): Double?
}

@Dao
interface SocialBadgeDao {
    @Insert
    suspend fun insert(badge: SocialBadgeEntity): Long

    @Update
    suspend fun update(badge: SocialBadgeEntity)

    @Query("DELETE FROM social_badges WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM social_badges ORDER BY unlockedAt DESC")
    fun allBadgesFlow(): Flow<List<SocialBadgeEntity>>

    @Query("SELECT * FROM social_badges WHERE isUnlocked = :unlocked ORDER BY unlockedAt DESC")
    fun badgesByUnlockStatusFlow(unlocked: Boolean): Flow<List<SocialBadgeEntity>>

    @Query("SELECT * FROM social_badges WHERE category = :category ORDER BY unlockedAt DESC")
    fun badgesByCategoryFlow(category: String): Flow<List<SocialBadgeEntity>>

    @Query("SELECT * FROM social_badges WHERE badgeType = :type ORDER BY unlockedAt DESC")
    fun badgesByTypeFlow(type: String): Flow<List<SocialBadgeEntity>>

    @Query("SELECT * FROM social_badges WHERE id = :id")
    suspend fun getBadge(id: Long): SocialBadgeEntity?

    @Query("UPDATE social_badges SET isUnlocked = :unlocked, unlockedAt = :unlockedAt WHERE id = :id")
    suspend fun updateUnlockStatus(id: Long, unlocked: Boolean, unlockedAt: Long?)

    @Query("UPDATE social_badges SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Double)

    @Query("SELECT COUNT(*) FROM social_badges WHERE isUnlocked = 1")
    suspend fun getUnlockedBadgeCount(): Int
}

@Dao
interface LeaderboardEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LeaderboardEntryEntity): Long

    @Update
    suspend fun update(entry: LeaderboardEntryEntity)

    @Query("DELETE FROM leaderboard_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM leaderboard_entries WHERE challengeId = :challengeId ORDER BY rank ASC")
    fun leaderboardByChallengeFlow(challengeId: Long): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT * FROM leaderboard_entries WHERE challengeId = :challengeId ORDER BY score DESC LIMIT :limit")
    suspend fun getTopEntries(challengeId: Long, limit: Int): List<LeaderboardEntryEntity>

    @Query("SELECT * FROM leaderboard_entries WHERE challengeId = :challengeId AND userId = :userId")
    suspend fun getUserEntry(challengeId: Long, userId: String): LeaderboardEntryEntity?

    @Query("UPDATE leaderboard_entries SET rank = :rank, score = :score, lastUpdated = :updated WHERE challengeId = :challengeId AND userId = :userId")
    suspend fun updateEntry(challengeId: Long, userId: String, rank: Int, score: Double, updated: Long)

    @Query("DELETE FROM leaderboard_entries WHERE challengeId = :challengeId")
    suspend fun clearLeaderboard(challengeId: Long)
}

