package com.example.fitapp.data.repo

import android.content.Context
import android.net.Uri
import com.example.fitapp.ai.AiGateway
import com.example.fitapp.ai.CalorieEstimate
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class NutritionRepository(private val db: AppDatabase) {
    fun favorites(): Flow<List<RecipeEntity>> = db.recipeDao().favoritesFlow()
    fun history(): Flow<List<RecipeEntity>> = db.recipeDao().historyFlow()

    suspend fun generateAndStore(prompt: String, provider: AiGateway.Provider): List<UiRecipe> {
        val list = AiGateway.generateRecipes(prompt, provider)
        list.forEach { r ->
            db.recipeDao().upsertAndAddToHistory(
                RecipeEntity(
                    id = r.id,
                    title = r.title,
                    markdown = r.markdown,
                    calories = r.calories,
                    imageUrl = r.imageUrl
                )
            )
        }
        return list
    }

    suspend fun setFavorite(recipeId: String, fav: Boolean) {
        if (fav) db.recipeDao().addFavorite(RecipeFavoriteEntity(recipeId))
        else db.recipeDao().removeFavorite(recipeId)
    }

    suspend fun addRecipeToShoppingList(recipeId: String) {
        val recipe = db.recipeDao().getRecipe(recipeId) ?: return
        val block = Regex("""(?s)Zutaten.*?(?:\n\n|$)""").find(recipe.markdown)?.value ?: return
        val items = Regex("""^\s*[-*]\s+(.+)$""", RegexOption.MULTILINE).findAll(block)
        items.forEach { m ->
            db.shoppingDao().insert(ShoppingItemEntity(name = m.groupValues[1], quantity = null, unit = null))
        }
    }

    suspend fun analyzeFoodImage(ctx: Context, uri: Uri, provider: AiGateway.Provider): CalorieEstimate {
        return AiGateway.analyzeFoodImage(ctx, uri, provider)
    }

    suspend fun logIntake(kcal: Int, label: String, source: String, refId: String? = null) {
        db.intakeDao().insert(IntakeEntryEntity(label = label, kcal = kcal, source = source, referenceId = refId))
    }

    fun dayEntriesFlow(epochSec: Long) = db.intakeDao().dayEntriesFlow(epochSec)

    fun goalFlow(date: LocalDate) = db.goalDao().goalFlow(date.toString())

    suspend fun setDailyGoal(date: LocalDate, targetKcal: Int) {
        db.goalDao().upsert(DailyGoalEntity(dateIso = date.toString(), targetKcal = targetKcal))
    }

    suspend fun totalForDay(epochSec: Long) = db.intakeDao().totalForDay(epochSec)

    fun shoppingItems() = db.shoppingDao().itemsFlow()
    suspend fun setItemChecked(id: Long, checked: Boolean) = db.shoppingDao().setChecked(id, checked)
    suspend fun deleteItem(id: Long) = db.shoppingDao().delete(id)
}
