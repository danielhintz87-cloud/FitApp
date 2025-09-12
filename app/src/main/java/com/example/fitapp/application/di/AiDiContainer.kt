package com.example.fitapp.application.di

import android.content.Context
import com.example.fitapp.application.usecases.*
import com.example.fitapp.core.threading.DefaultDispatcherProvider
import com.example.fitapp.core.threading.DispatcherProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.domain.repositories.AiProviderRepository
import com.example.fitapp.domain.usecases.*
import com.example.fitapp.infrastructure.logging.AiLogger
import com.example.fitapp.infrastructure.providers.AiProvider
import com.example.fitapp.infrastructure.providers.GeminiAiProvider
import com.example.fitapp.infrastructure.providers.PerplexityAiProvider
import com.example.fitapp.infrastructure.repositories.AiProviderRepositoryImpl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Simple dependency injection container
 * Provides instances for the Clean Architecture components
 */
class AiDiContainer private constructor(context: Context) {
    private val appContext = context.applicationContext

    // Infrastructure
    private val dispatchers: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val aiLogDao by lazy {
        AppDatabase.get(appContext).aiLogDao()
    }

    private val aiLogger by lazy {
        AiLogger(appContext, aiLogDao)
    }

    // Providers - Note: These are fallback instances for non-Hilt contexts
    // In Hilt-managed contexts, use the injected providers from AiProviderModule
    private val geminiProvider: AiProvider by lazy {
        // Create Retrofit services manually for non-Hilt usage
        val geminiRetrofit =
            retrofit2.Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .client(httpClient)
                .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create())
                .build()
        val geminiApiService =
            geminiRetrofit.create(
                com.example.fitapp.infrastructure.providers.api.GeminiApiService::class.java,
            )

        GeminiAiProvider(appContext, geminiApiService, dispatchers.io)
    }

    private val perplexityProvider: AiProvider by lazy {
        // Create Retrofit services manually for non-Hilt usage
        val perplexityRetrofit =
            retrofit2.Retrofit.Builder()
                .baseUrl("https://api.perplexity.ai/")
                .client(httpClient)
                .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create())
                .build()
        val perplexityApiService =
            perplexityRetrofit.create(
                com.example.fitapp.infrastructure.providers.api.PerplexityApiService::class.java,
            )

        PerplexityAiProvider()
    }

    private val providers: Map<com.example.fitapp.domain.entities.AiProvider, AiProvider> by lazy {
        mapOf(
            com.example.fitapp.domain.entities.AiProvider.Gemini to geminiProvider,
            com.example.fitapp.domain.entities.AiProvider.Perplexity to perplexityProvider,
        )
    }

    // Repository
    val aiProviderRepository: AiProviderRepository by lazy {
        AiProviderRepositoryImpl(providers, aiLogger)
    }

    // Use Cases
    val generateTrainingPlanUseCase: GenerateTrainingPlanUseCase by lazy {
        GenerateTrainingPlanUseCaseImpl(aiProviderRepository)
    }

    val generateRecipesUseCase: GenerateRecipesUseCase by lazy {
        GenerateRecipesUseCaseImpl(aiProviderRepository)
    }

    val estimateCaloriesUseCase: EstimateCaloriesUseCase by lazy {
        EstimateCaloriesUseCaseImpl(aiProviderRepository)
    }

    val parseShoppingListUseCase: ParseShoppingListUseCase by lazy {
        ParseShoppingListUseCaseImpl(aiProviderRepository)
    }

    val estimateCaloriesForManualEntryUseCase: EstimateCaloriesForManualEntryUseCase by lazy {
        EstimateCaloriesForManualEntryUseCaseImpl(aiProviderRepository)
    }

    val generateDailyWorkoutStepsUseCase: GenerateDailyWorkoutStepsUseCase by lazy {
        GenerateDailyWorkoutStepsUseCaseImpl(aiProviderRepository)
    }

    val generatePersonalizedWorkoutUseCase: GeneratePersonalizedWorkoutUseCase by lazy {
        GeneratePersonalizedWorkoutUseCaseImpl(aiProviderRepository)
    }

    val generateNutritionAdviceUseCase: GenerateNutritionAdviceUseCase by lazy {
        GenerateNutritionAdviceUseCaseImpl(aiProviderRepository)
    }

    val analyzeProgressUseCase: AnalyzeProgressUseCase by lazy {
        AnalyzeProgressUseCaseImpl(aiProviderRepository)
    }

    val generateMotivationUseCase: GenerateMotivationUseCase by lazy {
        GenerateMotivationUseCaseImpl(aiProviderRepository)
    }

    val getPersonalizedRecommendationsUseCase: GetPersonalizedRecommendationsUseCase by lazy {
        GetPersonalizedRecommendationsUseCaseImpl(
            generatePersonalizedWorkoutUseCase,
            generateNutritionAdviceUseCase,
            analyzeProgressUseCase,
            generateMotivationUseCase,
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: AiDiContainer? = null

        fun getInstance(context: Context): AiDiContainer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiDiContainer(context).also { INSTANCE = it }
            }
        }

        fun reset() {
            INSTANCE = null
        }
    }
}
