package com.example.fitapp.di

import com.example.fitapp.infrastructure.providers.api.GeminiApiService
import com.example.fitapp.infrastructure.providers.api.PerplexityApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for Gemini Retrofit instance
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiRetrofit

/**
 * Qualifier for Perplexity Retrofit instance
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PerplexityRetrofit

/**
 * Hilt module for AI provider API services
 */
@Module
@InstallIn(SingletonComponent::class)
object AiApiModule {
    @Provides
    @Singleton
    fun provideAiHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @GeminiRetrofit
    fun provideGeminiRetrofit(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @PerplexityRetrofit
    fun providePerplexityRetrofit(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.perplexity.ai/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(
        @GeminiRetrofit retrofit: Retrofit,
    ): GeminiApiService {
        return retrofit.create(GeminiApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePerplexityApiService(
        @PerplexityRetrofit retrofit: Retrofit,
    ): PerplexityApiService {
        return retrofit.create(PerplexityApiService::class.java)
    }
}
