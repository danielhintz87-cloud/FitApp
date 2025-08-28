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

