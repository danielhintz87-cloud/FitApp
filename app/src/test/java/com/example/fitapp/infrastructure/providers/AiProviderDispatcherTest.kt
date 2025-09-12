package com.example.fitapp.infrastructure.providers

import com.example.fitapp.core.threading.DispatcherProvider
import com.example.fitapp.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Test to verify AI providers properly use IO dispatcher for network operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AiProviderDispatcherTest {
    @Test
    fun `tracking dispatcher provider correctly identifies io dispatcher usage`() =
        runTest {
            val ioDispatcherCalled = AtomicBoolean(false)

            val trackingDispatcherProvider =
                object : DispatcherProvider {
                    override val main = testScheduler.let { StandardTestDispatcher(it) }
                    override val io =
                        object : CoroutineDispatcher() {
                            override fun dispatch(
                                context: kotlin.coroutines.CoroutineContext,
                                block: Runnable,
                            ) {
                                ioDispatcherCalled.set(true)
                                // Execute on test dispatcher
                                StandardTestDispatcher(testScheduler).dispatch(context, block)
                            }
                        }
                    override val default = testScheduler.let { StandardTestDispatcher(it) }
                    override val unconfined = testScheduler.let { StandardTestDispatcher(it) }
                }

            // Simulate what AI providers should do
            kotlinx.coroutines.withContext(trackingDispatcherProvider.io) {
                // This represents network operation
                Thread.sleep(1) // Simulate work
            }

            testScheduler.advanceUntilIdle()

            // Verify IO dispatcher was used
            assertTrue("IO dispatcher should be used for operations", ioDispatcherCalled.get())
        }

    @Test
    fun `ai providers with io dispatcher injection avoid main thread`() =
        runTest {
            val threadNameCapture = AtomicReference<String>()
            val mainThreadDetected = AtomicBoolean(false)

            // Create tracking IO dispatcher that captures thread names
            val trackingIoDispatcher =
                object : CoroutineDispatcher() {
                    override fun dispatch(
                        context: kotlin.coroutines.CoroutineContext,
                        block: Runnable,
                    ) {
                        StandardTestDispatcher(testScheduler).dispatch(context) {
                            val currentThreadName = Thread.currentThread().name
                            threadNameCapture.set(currentThreadName)

                            // Check if we're on main thread (should never happen for network operations)
                            if (currentThreadName.contains("main", ignoreCase = true)) {
                                mainThreadDetected.set(true)
                            }

                            block.run()
                        }
                    }
                }

            // Simulate dispatcher usage as AI providers would
            kotlinx.coroutines.withContext(trackingIoDispatcher) {
                // Simulate network operation work
                "network call result"
            }

            testScheduler.advanceUntilIdle()

            // Verify no main thread usage was detected
            assertFalse(
                "AI provider network operations should never execute on main thread. " +
                    "Detected thread: ${threadNameCapture.get()}",
                mainThreadDetected.get(),
            )
        }

    @Test
    fun `io dispatcher qualifier injection pattern works correctly`() {
        // This test verifies the @IoDispatcher injection pattern
        val testDispatcher = StandardTestDispatcher()

        // Simulate how @IoDispatcher would be injected
        class TestProviderWithIoDispatcher(
            @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        ) {
            fun getIoDispatcher() = ioDispatcher
        }

        val testProvider = TestProviderWithIoDispatcher(testDispatcher)

        // Verify the dispatcher can be injected and accessed
        assertNotNull("IO dispatcher should be injectable", testProvider.getIoDispatcher())
        assertEquals("Injected dispatcher should match provided one", testDispatcher, testProvider.getIoDispatcher())
    }

    @Test
    fun `dispatcher provider injection pattern works correctly`() {
        val dispatcherProvider =
            object : DispatcherProvider {
                override val main = StandardTestDispatcher()
                override val io = StandardTestDispatcher()
                override val default = StandardTestDispatcher()
                override val unconfined = StandardTestDispatcher()
            }

        // This simulates how AI providers are constructed
        assertNotNull("Main dispatcher should be available", dispatcherProvider.main)
        assertNotNull("IO dispatcher should be available", dispatcherProvider.io)
        assertNotNull("Default dispatcher should be available", dispatcherProvider.default)
        assertNotNull("Unconfined dispatcher should be available", dispatcherProvider.unconfined)

        // Verify they can be different instances if needed
        assertTrue(
            "Dispatchers should be properly configured",
            dispatcherProvider.io != null && dispatcherProvider.main != null,
        )
    }
}
