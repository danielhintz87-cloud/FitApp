package com.example.fitapp.di

import android.content.Context
import com.example.fitapp.data.datastore.UserPreferencesDataStore
import com.example.fitapp.data.datastore.UserPreferencesDataStoreImpl
import com.example.fitapp.data.prefs.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for DataStore-based preferences
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesDataStore(
        impl: UserPreferencesDataStoreImpl
    ): UserPreferencesDataStore

    companion object {
        @Provides
        @Singleton
        fun provideUserPreferencesRepository(
            @ApplicationContext context: Context
        ): UserPreferencesRepository = UserPreferencesRepository(context)
    }
}
