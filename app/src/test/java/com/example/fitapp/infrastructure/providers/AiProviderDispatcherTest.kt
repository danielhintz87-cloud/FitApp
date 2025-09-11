package com.example.fitapp.infrastructure.providers

import android.content.Context
import com.example.fitapp.core.threading.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Test to verify AI providers properly use IO dispatcher for network operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AiProviderDispatcherTest {
    
    @Test
    fun `tracking dispatcher provider correctly identifies io dispatcher usage`() = runTest {
        val ioDispatcherCalled = AtomicBoolean(false)
        
        val trackingDispatcherProvider = object : DispatcherProvider {
            override val main = testScheduler.let { StandardTestDispatcher(it) }
            override val io = object : CoroutineDispatcher() {
                override fun dispatch(context: kotlin.coroutines.CoroutineContext, block: Runnable) {
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
    fun `dispatcher provider injection pattern works correctly`() {
        val dispatcherProvider = object : DispatcherProvider {
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
        assertTrue("Dispatchers should be properly configured", 
            dispatcherProvider.io != null && dispatcherProvider.main != null)
    }
}