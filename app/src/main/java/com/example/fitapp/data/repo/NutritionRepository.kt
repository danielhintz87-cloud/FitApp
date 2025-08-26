package com.example.fitapp.data.repo

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import com.example.fitapp.ai.AiGateway
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.CalorieEstimate
import com.example.fitapp.ai.CaloriesEstimate
import com.example.fitapp.ai.RecipeRequest
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId

class NutritionRepository(private val db: AppDatabase) {
    fun favorites(): Flow<List<RecipeEntity>> = db.recipeDao().favoritesFlow()
    fun history(): Flow<List<RecipeEntity>> = db.recipeDao().historyFlow()

    suspend fun generateAndStore(context: Context, prompt: String, provider: AiProvider): List<UiRecipe> {
        val list = AiGateway.generateRecipes(context, prompt, provider.toGateway())
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

    /**
     * Generate recipes using automatic provider selection
     */
    suspend fun generateAndStoreWithOptimalProvider(context: Context, prompt: String): List<UiRecipe> {
        val req = RecipeRequest(preferences = prompt, diet = "")
        val result = AppAi.recipesWithOptimalProvider(context, req)
        
        return if (result.isSuccess) {
            // Parse the result similar to AiGateway.parseMarkdownRecipes
            val content = result.getOrThrow()
            val recipes = parseRecipesFromMarkdown(content)
            
            recipes.forEach { r ->
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
            recipes
        } else {
            throw result.exceptionOrNull() ?: Exception("Failed to generate recipes")
        }
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

    suspend fun analyzeFoodBitmap(context: Context, bitmap: Bitmap, provider: AiProvider): CalorieEstimate {
        return AiGateway.analyzeFoodBitmap(context, bitmap, provider.toGateway())
    }

    /**
     * Analyze food image using automatic provider selection
     */
    suspend fun analyzeFoodImageWithOptimalProvider(ctx: Context, uri: Uri): CalorieEstimate {
        // Convert URI to bitmap first to use the optimized calorie estimation
        val bitmap = loadBitmapFromUri(ctx, uri)
        return analyzeFoodBitmapWithOptimalProvider(ctx, bitmap)
    }

    /**
     * Analyze food bitmap using automatic provider selection
     */
    suspend fun analyzeFoodBitmapWithOptimalProvider(context: Context, bitmap: Bitmap): CalorieEstimate {
        val result = AppAi.caloriesWithOptimalProvider(context, bitmap)
        
        return if (result.isSuccess) {
            // Convert CaloriesEstimate (from AiCore) to CalorieEstimate (from AiGateway)
            val estimate = result.getOrThrow()
            CalorieEstimate(
                kcal = estimate.kcal,
                confidence = when {
                    estimate.confidence >= 80 -> "hoch"
                    estimate.confidence >= 50 -> "mittel"
                    else -> "niedrig"
                },
                details = estimate.text
            )
        } else {
            throw result.exceptionOrNull() ?: Exception("Failed to analyze food image")
        }
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
        AiProvider.DeepSeek -> AiGateway.Provider.DEEPSEEK
        AiProvider.Claude -> AiGateway.Provider.CLAUDE
        AiProvider.Auto -> AiGateway.Provider.OPENAI // Default fallback for legacy compatibility
        else -> AiGateway.Provider.OPENAI
    }

    /**
     * Parse recipes from markdown content (similar to AiGateway.parseMarkdownRecipes)
     */
    private fun parseRecipesFromMarkdown(content: String): List<UiRecipe> {
        val blocks = content.split(Regex("(?=^## )", RegexOption.MULTILINE)).mapIndexed { idx, block ->
            if (idx == 0 && block.startsWith("## ")) block.removePrefix("## ") else block
        }.filter { it.isNotBlank() }
        
        return blocks.map { raw ->
            val title = raw.lineSequence().firstOrNull()?.trim() ?: "Rezept"
            val kcal = Regex("""Kalorien:\s*(\d{2,5})\s*kcal""", RegexOption.IGNORE_CASE)
                .find(raw)?.groupValues?.get(1)?.toIntOrNull()
            UiRecipe(title = title, markdown = "## $raw".trim(), calories = kcal)
        }
    }

    /**
     * Load bitmap from URI
     */
    private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
        // Simple implementation - in production you might want more robust error handling
        val inputStream = context.contentResolver.openInputStream(uri)
        return android.graphics.BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Unable to decode image from URI")
    }
}
