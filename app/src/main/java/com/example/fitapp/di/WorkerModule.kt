package com.example.fitapp.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.fitapp.services.WaterReminderWorker
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Custom WorkerFactory for Hilt dependency injection in Workers
 */
@Singleton
class HiltWorkerFactory @Inject constructor(
    private val waterReminderWorkerFactory: WaterReminderWorker.Factory
) : WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            WaterReminderWorker::class.java.name -> {
                waterReminderWorkerFactory.create(appContext, workerParameters)
            }
            else -> null
        }
    }
}