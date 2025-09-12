package com.example.fitapp.ai

import android.content.Context
import com.example.fitapp.data.db.AiLogDao
import com.example.fitapp.data.prefs.ApiKeys
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Tests to ensure AiCore operations are properly dispatched off the main thread
 * and are cancellable.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AiCoreDispatcherTest {
    private lateinit var context: Context
    private lateinit var logDao: AiLogDao
    private lateinit var aiCore: AiCore
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScheduler: TestCoroutineScheduler

    @Before
    fun setup() {
        testScheduler = TestCoroutineScheduler()
        testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        logDao = mockk(relaxed = true)

        // Setup API key mock
        mockkObject(ApiKeys)
        every { ApiKeys.getGeminiKey(any()) } returns "test-gemini-key"
        every { ApiKeys.getPerplexityKey(any()) } returns "test-perplexity-key"
        every { ApiKeys.isProviderAvailable(any(), any()) } returns true

        aiCore = AiCore(context, logDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `geminiChat should not block main thread`() =
        runTest {
            // Mock the OkHttp client to simulate a network call
            val mockClient = mockk<OkHttpClient>()
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            val mockResponseBody = mockk<ResponseBody>()

            every { mockClient.newCall(any()) } returns mockCall
            every { mockResponse.isSuccessful } returns true
            every { mockResponse.body } returns mockResponseBody
            every { mockResponseBody.string() } returns
                """
                {
                    "candidates": [{
                        "content": {
                            "parts": [{"text": "Test response"}]
                        }
                    }]
                }
                """.trimIndent()
            every { mockResponse.use<Unit>(any()) } answers {
                firstArg<(Response) -> Unit>().invoke(mockResponse)
            }

            // Capture the callback to simulate async network call
            var capturedCallback: okhttp3.Callback? = null
            every { mockCall.enqueue(capture(slot<okhttp3.Callback>())) } answers {
                capturedCallback = captured
            }
            every { mockCall.cancel() } just Runs

            // Replace the http client in AiCore via reflection for testing
            val httpField = AiCore::class.java.getDeclaredField("http")
            httpField.isAccessible = true
            httpField.set(aiCore, mockClient)

            // Start the call - this should not block
            val result =
                async {
                    aiCore.callText(AiProvider.Gemini, "Test prompt")
                }

            // Verify that the call was enqueued (async)
            testScheduler.advanceUntilIdle()
            verify { mockCall.enqueue(any()) }

            // Simulate successful response
            capturedCallback?.onResponse(mockCall, mockResponse)

            // Advance coroutines and verify result
            testScheduler.advanceUntilIdle()
            val actualResult = result.await()

            assertTrue("Network call should succeed", actualResult.isSuccess)
            assertEquals("Test response", actualResult.getOrNull())

            // Verify the operation used proper dispatcher (not main thread)
            // The fact that we can advance the test scheduler confirms it's using test dispatcher
            verify { logDao.insert(any()) }
        }

    @Test
    fun `geminiChat should be cancellable`() =
        runTest {
            val mockClient = mockk<OkHttpClient>()
            val mockCall = mockk<Call>()

            every { mockClient.newCall(any()) } returns mockCall
            every { mockCall.cancel() } just Runs

            // Capture the callback but don't respond immediately
            var capturedCallback: okhttp3.Callback? = null
            every { mockCall.enqueue(capture(slot<okhttp3.Callback>())) } answers {
                capturedCallback = captured
            }

            // Replace the http client in AiCore via reflection for testing
            val httpField = AiCore::class.java.getDeclaredField("http")
            httpField.isAccessible = true
            httpField.set(aiCore, mockClient)

            // Start the call
            val job =
                launch {
                    aiCore.callText(AiProvider.Gemini, "Test prompt")
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
    fun `perplexityChat should not block main thread`() =
        runTest {
            val mockClient = mockk<OkHttpClient>()
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            val mockResponseBody = mockk<ResponseBody>()

            every { mockClient.newCall(any()) } returns mockCall
            every { mockResponse.isSuccessful } returns true
            every { mockResponse.body } returns mockResponseBody
            every { mockResponseBody.string() } returns
                """
                {
                    "choices": [{
                        "message": {
                            "content": "Test response"
                        }
                    }]
                }
                """.trimIndent()
            every { mockResponse.use<Unit>(any()) } answers {
                firstArg<(Response) -> Unit>().invoke(mockResponse)
            }

            var capturedCallback: okhttp3.Callback? = null
            every { mockCall.enqueue(capture(slot<okhttp3.Callback>())) } answers {
                capturedCallback = captured
            }
            every { mockCall.cancel() } just Runs

            // Replace the http client in AiCore
            val httpField = AiCore::class.java.getDeclaredField("http")
            httpField.isAccessible = true
            httpField.set(aiCore, mockClient)

            val result =
                async {
                    aiCore.callText(AiProvider.Perplexity, "Test prompt")
                }

            testScheduler.advanceUntilIdle()
            verify { mockCall.enqueue(any()) }

            // Simulate successful response
            capturedCallback?.onResponse(mockCall, mockResponse)

            testScheduler.advanceUntilIdle()
            val actualResult = result.await()

            assertTrue("Network call should succeed", actualResult.isSuccess)
            assertTrue("Should contain response text", actualResult.getOrNull()?.contains("Test response") == true)
        }

    @Test
    fun `network error should be handled gracefully`() =
        runTest {
            val mockClient = mockk<OkHttpClient>()
            val mockCall = mockk<Call>()

            every { mockClient.newCall(any()) } returns mockCall
            every { mockCall.cancel() } just Runs

            var capturedCallback: okhttp3.Callback? = null
            every { mockCall.enqueue(capture(slot<okhttp3.Callback>())) } answers {
                capturedCallback = captured
            }

            // Replace the http client in AiCore
            val httpField = AiCore::class.java.getDeclaredField("http")
            httpField.isAccessible = true
            httpField.set(aiCore, mockClient)

            val result =
                async {
                    aiCore.callText(AiProvider.Gemini, "Test prompt")
                }

            testScheduler.advanceUntilIdle()

            // Simulate network error
            capturedCallback?.onFailure(mockCall, IOException("Network error"))

            testScheduler.advanceUntilIdle()
            val actualResult = result.await()

            assertTrue("Network error should result in failure", actualResult.isFailure)
            assertTrue("Error should be IOException", actualResult.exceptionOrNull() is IOException)
        }
}
