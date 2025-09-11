package com.example.fitapp.di

import com.example.fitapp.core.threading.DefaultDispatcherProvider
import com.example.fitapp.core.threading.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing DispatcherProvider
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {
    
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        defaultDispatcherProvider: DefaultDispatcherProvider
    ): DispatcherProvider
}