package com.example.fitapp.core.threading

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Test for DispatcherProvider implementations
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DispatcherProviderTest {
    @Test
    fun `default dispatcher provider provides standard dispatchers`() {
        val provider = DefaultDispatcherProvider()

        assertNotNull("Main dispatcher should not be null", provider.main)
        assertNotNull("IO dispatcher should not be null", provider.io)
        assertNotNull("Default dispatcher should not be null", provider.default)
        assertNotNull("Unconfined dispatcher should not be null", provider.unconfined)
    }

    @Test
    fun `test dispatcher provider can be mocked for testing`() =
        runTest {
            val testDispatcher = StandardTestDispatcher(testScheduler)

            // Create a test implementation
            val testProvider =
                object : DispatcherProvider {
                    override val main = testDispatcher
                    override val io = testDispatcher
                    override val default = testDispatcher
                    override val unconfined = testDispatcher
                }

            assertEquals("All dispatchers should be the test dispatcher", testDispatcher, testProvider.main)
            assertEquals("All dispatchers should be the test dispatcher", testDispatcher, testProvider.io)
            assertEquals("All dispatchers should be the test dispatcher", testDispatcher, testProvider.default)
            assertEquals("All dispatchers should be the test dispatcher", testDispatcher, testProvider.unconfined)
        }
}
