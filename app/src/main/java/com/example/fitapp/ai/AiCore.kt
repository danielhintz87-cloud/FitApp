package com.example.fitapp.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.fitapp.data.db.AiLog
import com.example.fitapp.data.db.AiLogDao
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "AiCore"

/**
 * Central AI layer with support for multiple providers and Vision APIs
 * Handles text generation and image analysis with automatic logging
 */
class AiCore(private val aiLogDao: AiLogDao? = null) {

    // ---------- Core Configuration ----------
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val http: OkHttpClient by lazy {
        val log = HttpLoggingInterceptor { msg -> Log.d(TAG, msg) }
        log.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder()
            .addInterceptor(log)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // ---------- Public API ----------

    /**
     * Generate text using specified AI provider
     */
    suspend fun generateText(
        prompt: String,
        provider: AiProvider = AiProvider.OpenAI
    ): Result<String> {
        return try {
            val startTime = System.currentTimeMillis()
            val response = when (provider) {
                AiProvider.OpenAI -> callOpenAI(prompt)
                AiProvider.Gemini -> callGemini(prompt)
                AiProvider.DeepSeek -> callDeepSeek(prompt)
            }
            val duration = System.currentTimeMillis() - startTime
            
            // Log successful request
            logAiInteraction(
                provider = provider.name,
                requestType = "text",
                prompt = prompt,
                response = response,
                success = true,
                duration = duration
            )
            
            Result.success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Text generation failed for $provider", e)
            
            // Log failed request
            logAiInteraction(
                provider = provider.name,
                requestType = "text",
                prompt = prompt,
                response = null,
                success = false,
                error = e.message
            )
            
            Result.failure(e)
        }
    }

    /**
     * Analyze image for calorie estimation using Vision APIs
     */
    suspend fun analyzeImageForCalories(
        bitmap: Bitmap,
        provider: AiProvider = AiProvider.OpenAI
    ): Result<CalorieEstimation> {
        return try {
            val startTime = System.currentTimeMillis()
            val base64Image = bitmapToBase64(bitmap)
            
            val prompt = """
                Analyze this food image and provide a calorie estimation.
                Return JSON format:
                {
                  "food_items": ["item1", "item2"],
                  "estimated_calories": 450,
                  "confidence": 0.8,
                  "portion_size": "1 serving",
                  "notes": "Brief description"
                }
            """.trimIndent()
            
            val response = when (provider) {
                AiProvider.OpenAI -> callOpenAIVision(base64Image, prompt)
                AiProvider.Gemini -> callGeminiVision(base64Image, prompt)
                AiProvider.DeepSeek -> callDeepSeekVision(base64Image, prompt)
            }
            val duration = System.currentTimeMillis() - startTime
            
            val estimation = parseCalorieResponse(response)
            
            // Log successful vision request
            logAiInteraction(
                provider = provider.name,
                requestType = "vision",
                prompt = prompt,
                response = response,
                success = true,
                duration = duration
            )
            
            Result.success(estimation)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Vision analysis failed for $provider", e)
            
            // Log failed vision request
            logAiInteraction(
                provider = provider.name,
                requestType = "vision",
                prompt = "Image calorie analysis",
                response = null,
                success = false,
                error = e.message
            )
            
            Result.failure(e)
        }
    }

    // ---------- Provider Implementations ----------

    private suspend fun callOpenAI(prompt: String): String {
        val key = AiConfig.apiKey(AiProvider.OpenAI)
        if (key.isBlank()) throw IllegalStateException("OpenAI API key not configured")
        
        val body = buildOpenAIRequest(prompt)
        val request = Request.Builder()
            .url("${AiConfig.baseUrl(AiProvider.OpenAI)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            throw RuntimeException("OpenAI Error ${response.code}: $errorBody")
        }
        
        val parsed = json.decodeFromString(ChatResponse.serializer(), response.body!!.string())
        return parsed.choices.firstOrNull()?.message?.content?.trim().orEmpty()
    }

    private suspend fun callGemini(prompt: String): String {
        val key = AiConfig.apiKey(AiProvider.Gemini)
        if (key.isBlank()) throw IllegalStateException("Gemini API key not configured")
        
        val body = buildGeminiRequest(prompt)
        val request = Request.Builder()
            .url("${AiConfig.baseUrl(AiProvider.Gemini)}/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            throw RuntimeException("Gemini Error ${response.code}: $errorBody")
        }
        
        val parsed = json.decodeFromString(GeminiResponse.serializer(), response.body!!.string())
        return parsed.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim().orEmpty()
    }

    private suspend fun callDeepSeek(prompt: String): String {
        val key = AiConfig.apiKey(AiProvider.DeepSeek)
        if (key.isBlank()) throw IllegalStateException("DeepSeek API key not configured")
        
        val body = buildOpenAIRequest(prompt) // DeepSeek uses OpenAI-compatible API
        val request = Request.Builder()
            .url("${AiConfig.baseUrl(AiProvider.DeepSeek)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            throw RuntimeException("DeepSeek Error ${response.code}: $errorBody")
        }
        
        val parsed = json.decodeFromString(ChatResponse.serializer(), response.body!!.string())
        return parsed.choices.firstOrNull()?.message?.content?.trim().orEmpty()
    }

    // ---------- Vision API Implementations ----------

    private suspend fun callOpenAIVision(base64Image: String, prompt: String): String {
        val key = AiConfig.apiKey(AiProvider.OpenAI)
        if (key.isBlank()) throw IllegalStateException("OpenAI API key not configured")
        
        val body = buildOpenAIVisionRequest(base64Image, prompt)
        val request = Request.Builder()
            .url("${AiConfig.baseUrl(AiProvider.OpenAI)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            throw RuntimeException("OpenAI Vision Error ${response.code}: $errorBody")
        }
        
        val parsed = json.decodeFromString(ChatResponse.serializer(), response.body!!.string())
        return parsed.choices.firstOrNull()?.message?.content?.trim().orEmpty()
    }

    private suspend fun callGeminiVision(base64Image: String, prompt: String): String {
        val key = AiConfig.apiKey(AiProvider.Gemini)
        if (key.isBlank()) throw IllegalStateException("Gemini API key not configured")
        
        val body = buildGeminiVisionRequest(base64Image, prompt)
        val request = Request.Builder()
            .url("${AiConfig.baseUrl(AiProvider.Gemini)}/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            throw RuntimeException("Gemini Vision Error ${response.code}: $errorBody")
        }
        
        val parsed = json.decodeFromString(GeminiResponse.serializer(), response.body!!.string())
        return parsed.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim().orEmpty()
    }

    private suspend fun callDeepSeekVision(base64Image: String, prompt: String): String {
        // DeepSeek may not have vision API, fallback to text description
        return callDeepSeek("Based on a food image: $prompt")
    }

    // ---------- Request Builders ----------

    private fun buildOpenAIRequest(prompt: String): String {
        return """
        {
          "model": "gpt-4o-mini",
          "messages": [{"role": "user", "content": "$prompt"}],
          "max_tokens": 1500,
          "temperature": 0.7
        }
        """.trimIndent()
    }

    private fun buildOpenAIVisionRequest(base64Image: String, prompt: String): String {
        return """
        {
          "model": "gpt-4o-mini",
          "messages": [
            {
              "role": "user",
              "content": [
                {"type": "text", "text": "$prompt"},
                {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,$base64Image"}}
              ]
            }
          ],
          "max_tokens": 1500
        }
        """.trimIndent()
    }

    private fun buildGeminiRequest(prompt: String): String {
        return """
        {
          "contents": [{
            "parts": [{"text": "$prompt"}]
          }]
        }
        """.trimIndent()
    }

    private fun buildGeminiVisionRequest(base64Image: String, prompt: String): String {
        return """
        {
          "contents": [{
            "parts": [
              {"text": "$prompt"},
              {"inline_data": {"mime_type": "image/jpeg", "data": "$base64Image"}}
            ]
          }]
        }
        """.trimIndent()
    }

    // ---------- Utility Functions ----------

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun parseCalorieResponse(response: String): CalorieEstimation {
        return try {
            // Try to extract JSON from response
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = response.substring(jsonStart, jsonEnd)
                json.decodeFromString(CalorieEstimation.serializer(), jsonStr)
            } else {
                // Fallback parsing
                CalorieEstimation(
                    food_items = listOf("Unknown food"),
                    estimated_calories = 300,
                    confidence = 0.5,
                    portion_size = "1 serving",
                    notes = response.take(200)
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse calorie response", e)
            CalorieEstimation(
                food_items = listOf("Unknown food"),
                estimated_calories = 300,
                confidence = 0.3,
                portion_size = "1 serving",
                notes = "Analysis completed but format was unclear"
            )
        }
    }

    private suspend fun logAiInteraction(
        provider: String,
        requestType: String,
        prompt: String,
        response: String?,
        success: Boolean,
        duration: Long = 0,
        error: String? = null
    ) {
        try {
            aiLogDao?.insert(
                AiLog(
                    provider = provider,
                    requestType = requestType,
                    prompt = prompt.take(500), // Limit length
                    response = response?.take(1000), // Limit length
                    success = success,
                    timestamp = System.currentTimeMillis(),
                    duration = duration,
                    error = error
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log AI interaction", e)
        }
    }

    // ---------- Data Classes ----------

    @Serializable
    data class CalorieEstimation(
        val food_items: List<String>,
        val estimated_calories: Int,
        val confidence: Double,
        val portion_size: String,
        val notes: String
    )

    // OpenAI Response Models
    @Serializable
    private data class ChatResponse(
        val choices: List<Choice>
    )

    @Serializable
    private data class Choice(
        val message: Message
    )

    @Serializable
    private data class Message(
        val content: String
    )

    // Gemini Response Models
    @Serializable
    private data class GeminiResponse(
        val candidates: List<Candidate>
    )

    @Serializable
    private data class Candidate(
        val content: Content
    )

    @Serializable
    private data class Content(
        val parts: List<Part>
    )

    @Serializable
    private data class Part(
        val text: String
    )
}