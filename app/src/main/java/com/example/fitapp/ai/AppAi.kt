package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.fitapp.data.db.AppDatabase

object AppAi {
    private fun core(context: Context) = AiCore(AppDatabase.get(context).aiLogDao(), context)

    suspend fun plan(context: Context, provider: AiProvider, req: PlanRequest) =
        core(context).generatePlan(provider, req)

    suspend fun recipes(context: Context, provider: AiProvider, req: RecipeRequest) =
        core(context).generateRecipes(provider, req)

    suspend fun calories(context: Context, provider: AiProvider, bitmap: Bitmap, note: String = "") =
        core(context).estimateCaloriesFromPhoto(provider, bitmap, note)
}