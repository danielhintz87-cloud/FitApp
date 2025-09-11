package com.example.fitapp.core.threading

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Interface for providing coroutine dispatchers
 * Allows for easy testing by injecting test dispatchers
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}