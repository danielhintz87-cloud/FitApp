package com.example.fitapp.di

import android.content.Context
import com.example.fitapp.services.RestTimerAudioPlayer
import com.example.fitapp.services.SmartRestTimer
import com.example.fitapp.services.VoiceInputManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for audio and timer services
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    @Provides
    @Singleton
    fun provideRestTimerAudioPlayer(
        @ApplicationContext context: Context,
    ): RestTimerAudioPlayer {
        return RestTimerAudioPlayer(context)
    }

    @Provides
    @Singleton
    fun provideSmartRestTimer(
        @ApplicationContext context: Context,
    ): SmartRestTimer {
        return SmartRestTimer(context)
    }

    @Provides
    @Singleton
    fun provideVoiceInputManager(
        @ApplicationContext context: Context,
    ): VoiceInputManager {
        return VoiceInputManager(context)
    }
}
