package com.example.fitapp.di

import android.content.Context
import com.example.fitapp.data.prefs.IUserPreferences
import com.example.fitapp.data.prefs.UserPreferencesDataStoreImpl
import dagger.Binds
import com.example.fitapp.data.prefs.UserPreferencesRepository
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

    companion object {
        @Provides
        @Singleton
        fun provideUserPreferencesRepository(
            @ApplicationContext context: Context
        ): UserPreferencesRepository = UserPreferencesRepository(context)
    }

    @Binds
    @Singleton
    abstract fun bindUserPreferences(
        impl: UserPreferencesDataStoreImpl
    ): IUserPreferences
}
