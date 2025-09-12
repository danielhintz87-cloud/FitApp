package com.example.fitapp.di

import com.example.fitapp.infrastructure.providers.AiProvider
import com.example.fitapp.infrastructure.providers.GeminiAiProvider
import com.example.fitapp.infrastructure.providers.PerplexityAiProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import javax.inject.Singleton

/**
 * Hilt module for providing AI providers
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiProviderModule {
    @Binds
    @IntoMap
    @StringKey("Gemini")
    @Singleton
    abstract fun bindGeminiProvider(geminiProvider: GeminiAiProvider): AiProvider

    @Binds
    @IntoMap
    @StringKey("Perplexity")
    @Singleton
    abstract fun bindPerplexityProvider(perplexityProvider: PerplexityAiProvider): AiProvider
}
