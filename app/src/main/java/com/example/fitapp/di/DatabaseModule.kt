package com.example.fitapp.di

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNutritionRepository(
        @ApplicationContext context: Context,
        database: AppDatabase
    ): NutritionRepository {
        return NutritionRepository(database, context)
    }
}