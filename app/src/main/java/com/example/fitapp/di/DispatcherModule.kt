package com.example.fitapp.di

import com.example.fitapp.core.threading.DefaultDispatcherProvider
import com.example.fitapp.core.threading.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for IO dispatcher
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Qualifier for Main dispatcher
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

/**
 * Qualifier for Default dispatcher
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Hilt module for providing DispatcherProvider and qualified dispatchers
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {
    
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        defaultDispatcherProvider: DefaultDispatcherProvider
    ): DispatcherProvider
    
    companion object {
        @Provides
        @IoDispatcher
        fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
        
        @Provides
        @MainDispatcher
        fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
        
        @Provides
        @DefaultDispatcher
        fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    }
}