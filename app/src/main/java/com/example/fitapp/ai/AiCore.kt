package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.fitapp.ai.AiConfig.apiKey
import com.example.fitapp.ai.AiConfig.baseUrl
import com.example.fitapp.data.db.AiLog
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

private const val TAG = "AiCore"

/**
 * Central AI layer with support for all providers and Vision API
 */
class AiCore(private val context: Context) {
    
    private val database = AppDatabase.get(context)
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
     * Generate text using AI provider
     */
    suspend fun generateText(
        prompt: String,
        requestType: String = "text",
        provider: AiProvider = AiProvider.OpenAI
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = when (provider) {
                AiProvider.OpenAI -> callOpenAI(prompt)
                AiProvider.Gemini -> callGemini(prompt)
                AiProvider.DeepSeek -> callDeepSeek(prompt)
            }
            
            logAiInteraction(
                provider = provider.name,
                requestType = requestType,
                prompt = prompt,
                response = response,
                success = true
            )
            
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "AI generation failed", e)
            
            logAiInteraction(
                provider = provider.name,
                requestType = requestType,
                prompt = prompt,
                response = "",
                success = false,
                errorMessage = e.message
            )
            
            Result.failure(e)
        }
    }

    /**
     * Analyze image for calorie estimation using Vision API
     */
    suspend fun analyzeImageForCalories(
        bitmap: Bitmap,
        provider: AiProvider = AiProvider.OpenAI
    ): Result<CalorieEstimation> = withContext(Dispatchers.IO) {
        try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                Analyze this food image and provide a calorie estimation.
                Return ONLY a JSON object with this exact format:
                {
                  "food_items": ["item1", "item2"],
                  "estimated_calories": 450,
                  "confidence": 0.75,
                  "description": "Brief description of the food"
                }
            """.trimIndent()
            
            val response = when (provider) {
                AiProvider.OpenAI -> callOpenAIVision(prompt, base64Image)
                AiProvider.Gemini -> callGeminiVision(prompt, base64Image)
                AiProvider.DeepSeek -> callOpenAIVision(prompt, base64Image) // DeepSeek uses OpenAI format
            }
            
            val estimation = parseCalorieResponse(response)
            
            logAiInteraction(
                provider = provider.name,
                requestType = "vision",
                prompt = "Image calorie analysis",
                response = response,
                success = true,
                confidenceScore = estimation.confidence
            )
            
            Result.success(estimation)
        } catch (e: Exception) {
            Log.e(TAG, "Vision analysis failed", e)
            
            logAiInteraction(
                provider = provider.name,
                requestType = "vision", 
                prompt = "Image calorie analysis",
                response = "",
                success = false,
                errorMessage = e.message
            )
            
            Result.failure(e)
        }
    }

    // ---------- Private Implementation ----------

    private suspend fun callOpenAI(prompt: String): String {
        val key = apiKey(AiProvider.OpenAI)
        require(key.isNotEmpty()) { "OPENAI_API_KEY fehlt" }

        val body = buildOpenAITextRequest(prompt)
        val request = Request.Builder()
            .url("${baseUrl(AiProvider.OpenAI)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("OpenAI error ${response.code}: ${response.body?.string()}")
        }
        
        return parseOpenAIResponse(response.body!!.string())
    }

    private suspend fun callOpenAIVision(prompt: String, base64Image: String): String {
        val key = apiKey(AiProvider.OpenAI)
        require(key.isNotEmpty()) { "OPENAI_API_KEY fehlt" }

        val body = buildOpenAIVisionRequest(prompt, base64Image)
        val request = Request.Builder()
            .url("${baseUrl(AiProvider.OpenAI)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("OpenAI Vision error ${response.code}: ${response.body?.string()}")
        }
        
        return parseOpenAIResponse(response.body!!.string())
    }

    private suspend fun callGemini(prompt: String): String {
        val key = apiKey(AiProvider.Gemini)
        if (key.isEmpty()) throw Exception("GEMINI_API_KEY fehlt")
        
        val url = "${baseUrl(AiProvider.Gemini)}/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$key"
        val body = buildGeminiTextRequest(prompt)
        
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
            
        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini error ${response.code}: ${response.body?.string()}")
        }
        
        return parseGeminiResponse(response.body!!.string())
    }

    private suspend fun callGeminiVision(prompt: String, base64Image: String): String {
        val key = apiKey(AiProvider.Gemini)
        if (key.isEmpty()) throw Exception("GEMINI_API_KEY fehlt")
        
        val url = "${baseUrl(AiProvider.Gemini)}/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$key"
        val body = buildGeminiVisionRequest(prompt, base64Image)
        
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
            
        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini Vision error ${response.code}: ${response.body?.string()}")
        }
        
        return parseGeminiResponse(response.body!!.string())
    }

    private suspend fun callDeepSeek(prompt: String): String {
        val key = apiKey(AiProvider.DeepSeek)
        if (key.isEmpty()) throw Exception("DEEPSEEK_API_KEY fehlt")

        val body = buildOpenAITextRequest(prompt) // DeepSeek uses OpenAI format
        val request = Request.Builder()
            .url("${baseUrl(AiProvider.DeepSeek)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("DeepSeek error ${response.code}: ${response.body?.string()}")
        }
        
        return parseOpenAIResponse(response.body!!.string())
    }

    // ---------- JSON Builders (Manual) ----------

    private fun buildOpenAITextRequest(prompt: String): String {
        return """
        {
          "model": "gpt-4o-mini",
          "messages": [
            {"role": "user", "content": ${json.encodeToString(kotlinx.serialization.serializer(), prompt)}}
          ],
          "temperature": 0.7
        }
        """.trimIndent()
    }

    private fun buildOpenAIVisionRequest(prompt: String, base64Image: String): String {
        return """
        {
          "model": "gpt-4o-mini",
          "messages": [
            {
              "role": "user",
              "content": [
                {"type": "text", "text": ${json.encodeToString(kotlinx.serialization.serializer(), prompt)}},
                {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,$base64Image"}}
              ]
            }
          ],
          "temperature": 0.7
        }
        """.trimIndent()
    }

    private fun buildGeminiTextRequest(prompt: String): String {
        return """
        {
          "contents": [
            {"parts": [{"text": ${json.encodeToString(kotlinx.serialization.serializer(), prompt)}}]}
          ]
        }
        """.trimIndent()
    }

    private fun buildGeminiVisionRequest(prompt: String, base64Image: String): String {
        return """
        {
          "contents": [
            {
              "parts": [
                {"text": ${json.encodeToString(kotlinx.serialization.serializer(), prompt)}},
                {"inline_data": {"mime_type": "image/jpeg", "data": "$base64Image"}}
              ]
            }
          ]
        }
        """.trimIndent()
    }

    // ---------- Response Parsers ----------

    private fun parseOpenAIResponse(responseBody: String): String {
        val jsonObject = json.parseToJsonElement(responseBody).jsonObject
        return jsonObject["choices"]?.jsonArray?.get(0)?.jsonObject
            ?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
            ?: throw Exception("Invalid OpenAI response format")
    }

    private fun parseGeminiResponse(responseBody: String): String {
        val jsonObject = json.parseToJsonElement(responseBody).jsonObject
        return jsonObject["candidates"]?.jsonArray?.get(0)?.jsonObject
            ?.get("content")?.jsonObject?.get("parts")?.jsonArray?.get(0)?.jsonObject
            ?.get("text")?.jsonPrimitive?.content
            ?: throw Exception("Invalid Gemini response format")
    }

    private fun parseCalorieResponse(response: String): CalorieEstimation {
        // Extract JSON from response (handle cases where AI adds extra text)
        val jsonStart = response.indexOf("{")
        val jsonEnd = response.lastIndexOf("}") + 1
        val jsonString = if (jsonStart >= 0 && jsonEnd > jsonStart) {
            response.substring(jsonStart, jsonEnd)
        } else {
            throw Exception("No valid JSON found in response")
        }
        
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        return CalorieEstimation(
            foodItems = jsonObject["food_items"]?.jsonArray?.map { 
                it.jsonPrimitive.content 
            } ?: emptyList(),
            estimatedCalories = jsonObject["estimated_calories"]?.jsonPrimitive?.int ?: 0,
            confidence = jsonObject["confidence"]?.jsonPrimitive?.float ?: 0.5f,
            description = jsonObject["description"]?.jsonPrimitive?.content ?: ""
        )
    }

    // ---------- Utility Functions ----------

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private suspend fun logAiInteraction(
        provider: String,
        requestType: String,
        prompt: String,
        response: String,
        success: Boolean,
        errorMessage: String? = null,
        confidenceScore: Float? = null
    ) {
        try {
            val log = AiLog(
                provider = provider,
                requestType = requestType,
                prompt = prompt.take(500), // Limit prompt length
                response = response.take(1000), // Limit response length
                success = success,
                errorMessage = errorMessage,
                confidenceScore = confidenceScore
            )
            database.aiLogDao().insert(log)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log AI interaction", e)
        }
    }
}

/**
 * Data class for calorie estimation results
 */
data class CalorieEstimation(
    val foodItems: List<String>,
    val estimatedCalories: Int,
    val confidence: Float,
    val description: String
)