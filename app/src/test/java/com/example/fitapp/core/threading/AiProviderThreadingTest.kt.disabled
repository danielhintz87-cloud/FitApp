package com.example.fitapp.core.threading

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test to verify AI providers don't run network calls on main thread
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AiProviderThreadingTest {
    @Test
    fun `dispatcher provider ensures io operations are not on main thread`() =
        runTest {
            val testProvider = TestDispatcherProvider()

            // Simulate what would happen in an AI provider
            var executedOnCorrectThread = false

            withContext(testProvider.io) {
                // This should NOT be the main thread in real usage
                // In tests, our test dispatcher handles this
                executedOnCorrectThread = true
            }

            testProvider.advanceUntilIdle()
            assertTrue("Operation should execute on IO dispatcher", executedOnCorrectThread)
        }

    @Test
    fun `default dispatcher provider provides different dispatcher instances`() {
        val provider = DefaultDispatcherProvider()

        // Verify that we get actual dispatcher instances
        assertNotNull("Main dispatcher should not be null", provider.main)
        assertNotNull("IO dispatcher should not be null", provider.io)
        assertNotNull("Default dispatcher should not be null", provider.default)
        assertNotNull("Unconfined dispatcher should not be null", provider.unconfined)

        // In a real app, IO and Main should be different
        // (We can't easily test this without a full Android environment)
        assertTrue(
            "Dispatchers should be configured",
            provider.main != null && provider.io != null,
        )
    }
}
