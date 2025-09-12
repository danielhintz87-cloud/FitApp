package com.example.fitapp.di

import android.content.Context
import com.example.fitapp.network.healthconnect.HealthConnectManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for Health Connect dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object HealthConnectModule {
    @Provides
    @Singleton
    fun provideHealthConnectManager(
        @ApplicationContext context: Context,
    ): HealthConnectManager {
        return HealthConnectManager(context)
    }
}
