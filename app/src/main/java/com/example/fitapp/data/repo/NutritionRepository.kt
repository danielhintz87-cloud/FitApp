package com.example.fitapp.data.repo

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import com.example.fitapp.ai.AiGateway
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.CalorieEstimate
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId

class NutritionRepository(private val db: AppDatabase) {
    fun favorites(): Flow<List<RecipeEntity>> = db.recipeDao().favoritesFlow()
    fun history(): Flow<List<RecipeEntity>> = db.recipeDao().historyFlow()

    suspend fun generateAndStore(prompt: String, provider: AiProvider): List<UiRecipe> {
        val list = AiGateway.generateRecipes(prompt, provider.toGateway())
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

    suspend fun analyzeFoodImage(ctx: Context, uri: Uri, provider: AiProvider): CalorieEstimate {
        return AiGateway.analyzeFoodImage(ctx, uri, provider.toGateway())
    }

    suspend fun analyzeFoodBitmap(bitmap: Bitmap, provider: AiProvider): CalorieEstimate {
        return AiGateway.analyzeFoodBitmap(bitmap, provider.toGateway())
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

    // Plan related methods
    suspend fun savePlan(title: String, content: String, goal: String, weeks: Int, sessionsPerWeek: Int, minutesPerSession: Int, equipment: List<String>): Long {
        val plan = PlanEntity(
            title = title,
            content = content,
            goal = goal,
            weeks = weeks,
            sessionsPerWeek = sessionsPerWeek,
            minutesPerSession = minutesPerSession,
            equipment = equipment.joinToString(",")
        )
        return db.planDao().insert(plan)
    }

    fun plansFlow() = db.planDao().plansFlow()
    suspend fun getLatestPlan() = db.planDao().getLatestPlan()
    suspend fun getPlan(id: Long) = db.planDao().getPlan(id)
    suspend fun deletePlan(id: Long) = db.planDao().delete(id)

    // Day adjustment logic
    suspend fun adjustDailyGoal(date: LocalDate) {
        val epochSec = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val consumedToday = totalForDay(epochSec)
        val currentGoal = goalFlow(date).firstOrNull()?.targetKcal ?: 2000
        
        // Simple adjustment logic: if consumed > 120% of goal, increase goal by 10% for next day
        // if consumed < 80% of goal, decrease goal by 5% for next day
        val nextDay = date.plusDays(1)
        val newTarget = when {
            consumedToday > (currentGoal * 1.2) -> (currentGoal * 1.1).toInt()
            consumedToday < (currentGoal * 0.8) -> (currentGoal * 0.95).toInt()
            else -> currentGoal
        }
        
        if (newTarget != currentGoal) {
            setDailyGoal(nextDay, newTarget)
        }
    }

    private fun AiProvider.toGateway(): AiGateway.Provider = when (this) {
        AiProvider.Gemini -> AiGateway.Provider.GEMINI
        else -> AiGateway.Provider.OPENAI
    }
}
