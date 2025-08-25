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

    @Query("SELECT * FROM shopping_items ORDER BY createdAt DESC")
    fun itemsFlow(): Flow<List<ShoppingItemEntity>>

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)
}

