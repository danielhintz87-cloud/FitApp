package com.example.fitapp.core.health

import android.content.Context
import com.example.fitapp.core.threading.DispatcherProvider
import com.example.fitapp.data.prefs.ApiKeys
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Tests to ensure GeminiHealthChecker operations are properly dispatched off the main thread
 * and are cancellable.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GeminiHealthCheckerDispatcherTest {
    private lateinit var context: Context
    private lateinit var httpClient: OkHttpClient
    private lateinit var dispatchers: DispatcherProvider
    private lateinit var healthChecker: GeminiHealthChecker
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScheduler: TestCoroutineScheduler

    @Before
    fun setup() {
        testScheduler = TestCoroutineScheduler()
        testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        httpClient = mockk(relaxed = true)
        dispatchers =
            mockk {
                every { io } returns testDispatcher
            }

        // Setup API key mock
        mockkObject(ApiKeys)
        every { ApiKeys.getGeminiKey(any()) } returns "test-gemini-key"

        healthChecker = GeminiHealthChecker(context, httpClient, dispatchers)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `checkHealth should not block main thread`() =
        runTest {
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()

            every { httpClient.newCall(any()) } returns mockCall
            every { mockResponse.isSuccessful } returns true
            every { mockResponse.use<Unit>(any()) } answers {
                firstArg<(Response) -> Unit>().invoke(mockResponse)
            }

            var capturedCallback: okhttp3.Callback? = null
            every { mockCall.enqueue(capture(slot<okhttp3.Callback>())) } answers {
                capturedCallback = captured
            }
            every { mockCall.cancel() } just Runs

            // Start the health check - this should not block
            val result =
                async {
                    healthChecker.checkHealth()
                }

            // Advance test scheduler to process the coroutine
            testScheduler.advanceUntilIdle()

            // Verify that the call was enqueued (async)
            verify { mockCall.enqueue(any()) }

            // Simulate successful response
            capturedCallback?.onResponse(mockCall, mockResponse)

            // Advance coroutines and verify result
            testScheduler.advanceUntilIdle()
            val actualResult = result.await()

            assertTrue("Health check should succeed", actualResult.isHealthy)
            assertEquals("Gemini AI", actualResult.provider)
            assertTrue("Response time should be recorded", actualResult.responseTimeMs >= 0)
        }

    @Test
    fun `checkHealth should be cancellable`() =
        runTest {
            val mockCall = mockk<Call>()

            every { httpClient.newCall(any()) } returns mockCall
            every { mockCall.cancel() } just Runs

            // Capture the callback but don't respond immediately
            var capturedCallback: okhttp3.Callback? = null
            every { mockCall.enqueue(capture(slot<okhttp3.Callback>())) } answers {
                capturedCallback = captured
            }

            // Start the health check
            val job =
                launch {
                    healthChecker.checkHealth()
                }

            testScheduler.advanceUntilIdle()

            // Cancel the job
            job.cancel()
            testScheduler.advanceUntilIdle()

            // Verify that the OkHttp call was cancelled
            verify { mockCall.cancel() }
            assertTrue("Job should be cancelled", job.isCancelled)
        }

    @Test
    fun `checkHealth should handle network errors gracefully`() =
        runTest {
            val mockCall = mockk<Call>()

            every { httpClient.newCall(any()) } returns mockCall
            every { mockCall.cancel() } just Runs

            var capturedCallback: okhttp3.Callback? = null
            every { mockCall.enqueue(capture(slot<okhttp3.Callback>())) } answers {
                capturedCallback = captured
            }

            val result =
                async {
                    healthChecker.checkHealth()
                }

            testScheduler.advanceUntilIdle()

            // Simulate network error
            capturedCallback?.onFailure(mockCall, IOException("Network error"))

            testScheduler.advanceUntilIdle()
            val actualResult = result.await()

            assertFalse("Health check should fail on network error", actualResult.isHealthy)
            assertEquals("Gemini AI", actualResult.provider)
            assertNotNull("Error message should be provided", actualResult.errorMessage)
        }

    @Test
    fun `checkHealth should fail when API key is missing`() =
        runTest {
            // Override API key to be blank
            every { ApiKeys.getGeminiKey(any()) } returns ""

            val result = healthChecker.checkHealth()

            assertFalse("Health check should fail when API key is missing", result.isHealthy)
            assertEquals("Gemini AI", result.provider)
            assertEquals("API key not configured", result.errorMessage)
            assertTrue("Response time should be recorded", result.responseTimeMs >= 0)
        }

    @Test
    fun `checkHealth should timeout appropriately`() =
        runTest {
            val mockCall = mockk<Call>()

            every { httpClient.newCall(any()) } returns mockCall
            every { mockCall.cancel() } just Runs

            // Don't respond to the callback to simulate timeout
            every { mockCall.enqueue(any()) } just Runs

            val result = healthChecker.checkHealth()

            assertFalse("Health check should fail on timeout", result.isHealthy)
            assertEquals("Gemini AI", result.provider)
            assertEquals("Health check failed or timed out", result.errorMessage)
        }

    @Test
    fun `healthStatusFlow should use IO dispatcher`() =
        runTest {
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()

            every { httpClient.newCall(any()) } returns mockCall
            every { mockResponse.isSuccessful } returns true
            every { mockResponse.use<Unit>(any()) } answers {
                firstArg<(Response) -> Unit>().invoke(mockResponse)
            }

            var capturedCallback: okhttp3.Callback? = null
            every { mockCall.enqueue(capture(slot<okhttp3.Callback>())) } answers {
                capturedCallback = captured
            }

            // Collect from the flow
            val results = mutableListOf<HealthStatus>()
            val job =
                launch {
                    healthChecker.healthStatusFlow().collect { results.add(it) }
                }

            testScheduler.advanceUntilIdle()

            // Simulate response
            capturedCallback?.onResponse(mockCall, mockResponse)

            testScheduler.advanceUntilIdle()
            job.cancel()

            assertEquals("Should emit one health status", 1, results.size)
            assertTrue("Health status should be healthy", results[0].isHealthy)

            // Verify IO dispatcher was used
            verify { dispatchers.io }
        }
}
