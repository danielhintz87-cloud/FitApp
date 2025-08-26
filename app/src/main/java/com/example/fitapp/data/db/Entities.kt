package com.example.fitapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val markdown: String,
    val calories: Int?,
    val imageUrl: String?,
    val createdAt: Long = Instant.now().epochSecond
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
    val savedAt: Long = Instant.now().epochSecond
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
    val createdAt: Long = Instant.now().epochSecond
)

@Entity(tableName = "intake_entries")
data class IntakeEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = Instant.now().epochSecond,
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
    val createdAt: Long = Instant.now().epochSecond
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
    val createdAt: Long = Instant.now().epochSecond
)

