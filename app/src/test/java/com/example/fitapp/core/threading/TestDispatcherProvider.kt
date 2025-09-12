package com.example.fitapp.core.threading

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * Test implementation of DispatcherProvider for unit tests
 */
class TestDispatcherProvider(
    private val testScheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
) : DispatcherProvider {
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
    override val unconfined: CoroutineDispatcher = testDispatcher

    fun advanceUntilIdle() {
        testScheduler.advanceUntilIdle()
    }
}
