package com.example.fitapp.di

import com.example.fitapp.core.health.ApiHealthRegistry
import com.example.fitapp.core.health.GeminiHealthChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for health checker dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object HealthCheckerModule {
    @Provides
    @Singleton
    fun provideApiHealthRegistry(geminiHealthChecker: GeminiHealthChecker): ApiHealthRegistry {
        return ApiHealthRegistry(geminiHealthChecker)
    }
}
