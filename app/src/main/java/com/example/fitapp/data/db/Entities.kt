package com.example.fitapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "recipes")
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

@Entity(tableName = "intake_entries")
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
    val targetKcal: Int
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

