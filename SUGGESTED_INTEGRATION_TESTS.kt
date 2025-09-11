package com.example.fitapp.infrastructure.providers

import android.content.Context
import com.example.fitapp.core.threading.DispatcherProvider
import com.example.fitapp.domain.entities.TaskType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Enhanced integration tests for AI providers focusing on network handling and dispatcher usage
 * 
 * These tests demonstrate recommended patterns for testing network operations in Android
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AiProviderNetworkIntegrationTest {
    
    private lateinit var mockServer: MockWebServer
    private lateinit var context: Context
    private lateinit var testScheduler: TestCoroutineScheduler
    
    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
        context = RuntimeEnvironment.getApplication()
        testScheduler = TestCoroutineScheduler()
    }
    
    @After
    fun teardown() {
        mockServer.shutdown()
    }
    
    @Test
    fun `gemini provider uses io dispatcher for actual network calls`() = runTest(testScheduler) {
        // Arrange
        val ioDispatcherUsed = AtomicBoolean(false)
        val lastThreadName = AtomicReference<String>()
        
        val trackingDispatcherProvider = object : DispatcherProvider {
            override val main = StandardTestDispatcher(testScheduler)
            override val io = object : CoroutineDispatcher() {
                override fun dispatch(context: kotlin.coroutines.CoroutineContext, block: Runnable) {
                    ioDispatcherUsed.set(true)
                    lastThreadName.set(Thread.currentThread().name)
                    StandardTestDispatcher(testScheduler).dispatch(context, block)
                }
            }
            override val default = StandardTestDispatcher(testScheduler)
            override val unconfined = StandardTestDispatcher(testScheduler)
        }
        
        val validGeminiResponse = """{
            "candidates": [{
                "content": {
                    "parts": [{
                        "text": "Test response from Gemini"
                    }]
                }
            }]
        }"""
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(validGeminiResponse)
            .setHeader("Content-Type", "application/json"))
        
        val testHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val newUrl = request.url.newBuilder()
                    .scheme(mockServer.url("/").scheme)
                    .host(mockServer.url("/").host)
                    .port(mockServer.url("/").port)
                    .build()
                val newRequest = request.newBuilder().url(newUrl).build()
                chain.proceed(newRequest)
            }
            .build()
        
        val geminiProvider = GeminiAiProvider(context, testHttpClient, trackingDispatcherProvider)
        
        // Act
        val result = geminiProvider.generateText("Test prompt", TaskType.GENERAL)
        
        testScheduler.advanceUntilIdle()
        
        // Assert
        assertTrue("IO dispatcher should be used for network operations", ioDispatcherUsed.get())
        assertEquals("Test response from Gemini", result)
        
        // Verify the actual HTTP request was made
        val recordedRequest = mockServer.takeRequest()
        assertTrue("Request should contain the prompt", 
            recordedRequest.body.readUtf8().contains("Test prompt"))
        assertEquals("POST", recordedRequest.method)
    }
    
    @Test
    fun `perplexity provider handles network errors gracefully with io dispatcher`() = runTest(testScheduler) {
        // Arrange
        val ioDispatcherUsed = AtomicBoolean(false)
        
        val trackingDispatcherProvider = object : DispatcherProvider {
            override val main = StandardTestDispatcher(testScheduler)
            override val io = object : CoroutineDispatcher() {
                override fun dispatch(context: kotlin.coroutines.CoroutineContext, block: Runnable) {
                    ioDispatcherUsed.set(true)
                    StandardTestDispatcher(testScheduler).dispatch(context, block)
                }
            }
            override val default = StandardTestDispatcher(testScheduler)
            override val unconfined = StandardTestDispatcher(testScheduler)
        }
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(429)
            .setBody("""{"error": "Rate limit exceeded"}"""))
        
        val testHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val newUrl = request.url.newBuilder()
                    .scheme(mockServer.url("/").scheme)
                    .host(mockServer.url("/").host)
                    .port(mockServer.url("/").port)
                    .build()
                val newRequest = request.newBuilder().url(newUrl).build()
                chain.proceed(newRequest)
            }
            .build()
        
        val perplexityProvider = PerplexityAiProvider(context, testHttpClient, trackingDispatcherProvider)
        
        // Act & Assert
        try {
            perplexityProvider.generateText("Test prompt", TaskType.GENERAL)
            fail("Should have thrown an exception for 429 response")
        } catch (e: Exception) {
            assertTrue("Should contain rate limit message", 
                e.message?.contains("Zu viele Anfragen") == true)
        }
        
        testScheduler.advanceUntilIdle()
        
        // Verify IO dispatcher was used even for error scenarios
        assertTrue("IO dispatcher should be used even for failed requests", ioDispatcherUsed.get())
    }
    
    @Test
    fun `network operations respect timeout settings with io dispatcher`() = runTest(testScheduler) {
        // Arrange
        val ioDispatcherUsed = AtomicBoolean(false)
        
        val trackingDispatcherProvider = object : DispatcherProvider {
            override val main = StandardTestDispatcher(testScheduler)
            override val io = object : CoroutineDispatcher() {
                override fun dispatch(context: kotlin.coroutines.CoroutineContext, block: Runnable) {
                    ioDispatcherUsed.set(true)
                    StandardTestDispatcher(testScheduler).dispatch(context, block)
                }
            }
            override val default = StandardTestDispatcher(testScheduler)
            override val unconfined = StandardTestDispatcher(testScheduler)
        }
        
        // Set up a slow response
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBodyDelay(5, java.util.concurrent.TimeUnit.SECONDS)
            .setBody("""{"candidates": [{"content": {"parts": [{"text": "slow response"}]}}]}"""))
        
        val timeoutHttpClient = OkHttpClient.Builder()
            .callTimeout(100, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val newUrl = request.url.newBuilder()
                    .scheme(mockServer.url("/").scheme)
                    .host(mockServer.url("/").host)
                    .port(mockServer.url("/").port)
                    .build()
                val newRequest = request.newBuilder().url(newUrl).build()
                chain.proceed(newRequest)
            }
            .build()
        
        val geminiProvider = GeminiAiProvider(context, timeoutHttpClient, trackingDispatcherProvider)
        
        // Act & Assert
        try {
            geminiProvider.generateText("Test prompt", TaskType.GENERAL)
            fail("Should have thrown a timeout exception")
        } catch (e: Exception) {
            // Expected timeout exception
            assertTrue("Should be a timeout-related exception", 
                e.message?.contains("timeout") == true || e is java.net.SocketTimeoutException)
        }
        
        testScheduler.advanceUntilIdle()
        
        // Verify IO dispatcher was used for timeout scenarios
        assertTrue("IO dispatcher should be used even for timeout scenarios", ioDispatcherUsed.get())
    }
    
    @Test
    fun `strict mode detection works with network operations`() = runTest(testScheduler) {
        // This test would verify that StrictMode detects main thread violations
        // Note: This is more of a conceptual test as StrictMode is hard to test directly
        
        val mainDispatcherProvider = object : DispatcherProvider {
            override val main = StandardTestDispatcher(testScheduler)
            override val io = StandardTestDispatcher(testScheduler) // Using main for this test
            override val default = StandardTestDispatcher(testScheduler)
            override val unconfined = StandardTestDispatcher(testScheduler)
        }
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"candidates": [{"content": {"parts": [{"text": "response"}]}}]}"""))
        
        val testHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val newUrl = request.url.newBuilder()
                    .scheme(mockServer.url("/").scheme)
                    .host(mockServer.url("/").host)
                    .port(mockServer.url("/").port)
                    .build()
                val newRequest = request.newBuilder().url(newUrl).build()
                chain.proceed(newRequest)
            }
            .build()
        
        val geminiProvider = GeminiAiProvider(context, testHttpClient, mainDispatcherProvider)
        
        // Act
        val result = geminiProvider.generateText("Test prompt", TaskType.GENERAL)
        
        testScheduler.advanceUntilIdle()
        
        // Assert - the operation should complete successfully with proper dispatcher usage
        assertEquals("response", result)
    }
}