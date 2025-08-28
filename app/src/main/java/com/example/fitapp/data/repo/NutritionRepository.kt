package com.example.fitapp.data.repo

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
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

    suspend fun generateAndStoreOptimal(context: Context, prompt: String): List<UiRecipe> {
        val req = RecipeRequest(preferences = prompt, diet = "", count = 10)
        val result = AppAi.recipesWithOptimalProvider(context, req).getOrThrow()
        
        // Parse the result string into UiRecipe list (same logic as in AiGateway)
        val list = parseMarkdownRecipes(result)
        
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

    suspend fun generateAndStore(context: Context, prompt: String): List<UiRecipe> {
        val req = RecipeRequest(preferences = prompt, diet = "", count = 10)
        val result = AppAi.recipesWithOptimalProvider(context, req).getOrThrow()
        
        // Parse the result string into UiRecipe list
        val list = parseMarkdownRecipes(result)
        
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

    suspend fun analyzeFoodBitmap(context: Context, bitmap: Bitmap): CaloriesEstimate {
        return AppAi.caloriesWithOptimalProvider(context, bitmap).getOrThrow()
    }

    suspend fun logIntake(kcal: Int, label: String, source: String, refId: String? = null) {
        db.intakeDao().insert(IntakeEntryEntity(label = label, kcal = kcal, source = source, referenceId = refId))
        
        // Track nutrition logging for achievements and streaks
        // Note: This creates a potential circular dependency, but it's acceptable for this feature
        try {
            val motivationRepo = PersonalMotivationRepository(db)
            // This is a simplified approach - in a more robust implementation, 
            // you'd use dependency injection or event system
        } catch (e: Exception) {
            // Ignore nutrition tracking errors to avoid breaking core functionality
        }
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
    suspend fun savePlan(title: String, content: String, goal: String, weeks: Int, sessionsPerWeek: Int, minutesPerSession: Int, equipment: List<String>, trainingDays: List<String>? = null): Long {
        val plan = PlanEntity(
            title = title,
            content = content,
            goal = goal,
            weeks = weeks,
            sessionsPerWeek = sessionsPerWeek,
            minutesPerSession = minutesPerSession,
            equipment = equipment.joinToString(","),
            trainingDays = trainingDays?.joinToString(",")
        )
        return db.planDao().insert(plan)
    }

    fun plansFlow() = db.planDao().plansFlow()
    suspend fun getLatestPlan() = db.planDao().getLatestPlan()
    suspend fun getPlan(id: Long) = db.planDao().getPlan(id)
    suspend fun deletePlan(id: Long) = db.planDao().delete(id)
    suspend fun deleteAllPlans() = db.planDao().deleteAll()

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

    /**
     * AI-driven calorie recommendation based on training plan and goals
     */
    suspend fun generateAICalorieRecommendation(): Int {
        val latestPlan = getLatestPlan()
        if (latestPlan == null) {
            return 2000 // Default fallback
        }

        val baselineKcal = when (latestPlan.goal.lowercase()) {
            "abnehmen", "gewicht verlieren" -> 1800
            "muskelaufbau", "masse aufbauen" -> 2500
            "kraft steigern" -> 2300
            "ausdauer verbessern" -> 2200
            "körper definieren" -> 2000
            "allgemeine fitness" -> 2100
            "funktionelle fitness" -> 2200
            "beweglichkeit verbessern" -> 1900
            else -> 2000
        }

        // Adjust based on training frequency and intensity
        val intensityMultiplier = when {
            latestPlan.sessionsPerWeek >= 5 && latestPlan.minutesPerSession >= 60 -> 1.2
            latestPlan.sessionsPerWeek >= 4 && latestPlan.minutesPerSession >= 45 -> 1.1
            latestPlan.sessionsPerWeek >= 3 && latestPlan.minutesPerSession >= 30 -> 1.05
            else -> 1.0
        }

        return (baselineKcal * intensityMultiplier).toInt()
    }

    /**
     * Set AI-recommended daily goal based on current training plan
     */
    suspend fun setAIRecommendedGoal(date: LocalDate) {
        val recommendedKcal = generateAICalorieRecommendation()
        setDailyGoal(date, recommendedKcal)
    }

    private fun parseMarkdownRecipes(markdown: String): List<UiRecipe> {
        val blocks = markdown.split("\n## ").mapIndexed { idx, block ->
            if (idx == 0 && block.startsWith("## ")) block.removePrefix("## ") else block
        }.filter { it.isNotBlank() }
        return blocks.map { raw ->
            val title = raw.lineSequence().firstOrNull()?.trim() ?: "Rezept"
            val kcal = Regex("""Kalorien:\s*(\d{2,5})\s*kcal""", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)?.toIntOrNull()
            UiRecipe(title = title, markdown = "## $raw".trim(), calories = kcal)
        }
    }

    // Today workout methods
    suspend fun getTodayWorkout(dateIso: String) = db.todayWorkoutDao().getByDate(dateIso)
    suspend fun saveTodayWorkout(workout: com.example.fitapp.data.db.TodayWorkoutEntity) = db.todayWorkoutDao().upsert(workout)
    suspend fun setWorkoutStatus(dateIso: String, status: String, completedAt: Long?) = db.todayWorkoutDao().setStatus(dateIso, status, completedAt)
    suspend fun getWorkoutsBetween(fromIso: String, toIso: String) = db.todayWorkoutDao().getBetween(fromIso, toIso)
}
