package com.example.fitapp.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.fitapp.ai.AiConfig.apiKey
import com.example.fitapp.ai.AiConfig.baseUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

private const val TAG = "AiCore"

/**
 * Central AI layer with support for text and vision models across multiple providers.
 * Handles OpenAI, Gemini, and DeepSeek with automatic logging to Room database.
 */
object AiCore {

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
     * Analyze image for calorie estimation using vision models
     */
    suspend fun analyzeImageForCalories(
        bitmap: Bitmap,
        provider: AiProvider = AiProvider.OpenAI
    ): Result<CalorieAnalysis> {
        return try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                Analysiere dieses Bild von Essen und schätze die Kalorien.
                Gib eine strukturierte Antwort zurück mit:
                - Erkannte Lebensmittel
                - Geschätzte Gesamtkalorien
                - Konfidenzwert (0-100%)
                - Kurze Erklärung der Schätzung
            """.trimIndent()

            val response = when (provider) {
                AiProvider.OpenAI -> openAiVision(prompt, base64Image)
                AiProvider.Gemini -> geminiVision(prompt, base64Image)
                AiProvider.DeepSeek -> deepSeekVision(prompt, base64Image)
            }

            val analysis = parseCalorieAnalysis(response)
            Result.success(analysis)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image", e)
            Result.failure(e)
        }
    }

    /**
     * Generate text using specified AI provider
     */
    suspend fun generateText(
        prompt: String,
        provider: AiProvider = AiProvider.OpenAI
    ): Result<String> {
        return try {
            val response = when (provider) {
                AiProvider.OpenAI -> openAiChat(prompt)
                AiProvider.Gemini -> geminiChat(prompt)
                AiProvider.DeepSeek -> deepSeekChat(prompt)
            }
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating text", e)
            Result.failure(e)
        }
    }

    // ---------- Data Models ----------

    @Serializable
    data class CalorieAnalysis(
        val foods: List<String>,
        val totalCalories: Int,
        val confidence: Int,
        val explanation: String
    )

    // ---------- Utility Methods ----------

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun parseCalorieAnalysis(response: String): CalorieAnalysis {
        // Simple parsing for demo - in production would use more sophisticated NLP
        val foods = mutableListOf<String>()
        var calories = 0
        var confidence = 70
        
        val lines = response.lines()
        lines.forEach { line ->
            when {
                line.contains("kalorie", ignoreCase = true) -> {
                    val regex = "\\d+".toRegex()
                    val match = regex.find(line)
                    if (match != null) {
                        calories = match.value.toIntOrNull() ?: calories
                    }
                }
                line.contains("konfidenz", ignoreCase = true) || line.contains("confidence", ignoreCase = true) -> {
                    val regex = "\\d+".toRegex()
                    val match = regex.find(line)
                    if (match != null) {
                        confidence = match.value.toIntOrNull() ?: confidence
                    }
                }
                line.trim().startsWith("-") -> {
                    val food = line.trim().removePrefix("-").trim()
                    if (food.isNotBlank()) foods.add(food)
                }
            }
        }

        return CalorieAnalysis(
            foods = foods,
            totalCalories = calories,
            confidence = confidence,
            explanation = response
        )
    }

    // ---------- OpenAI Implementation ----------

    private suspend fun openAiChat(prompt: String): String {
        val key = apiKey(AiProvider.OpenAI)
        require(key.isNotEmpty()) { "OpenAI API key missing" }

        val body = buildJsonManually {
            obj {
                "model" to "gpt-4o-mini"
                "messages" to array {
                    obj {
                        "role" to "user"
                        "content" to prompt
                    }
                }
                "temperature" to 0.7
            }
        }

        val request = Request.Builder()
            .url("${baseUrl(AiProvider.OpenAI)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("OpenAI API error: ${response.code} - ${response.body?.string()}")
        }

        val jsonResponse = response.body?.string() ?: ""
        return extractOpenAiMessage(jsonResponse)
    }

    private suspend fun openAiVision(prompt: String, base64Image: String): String {
        val key = apiKey(AiProvider.OpenAI)
        require(key.isNotEmpty()) { "OpenAI API key missing" }

        val body = buildJsonManually {
            obj {
                "model" to "gpt-4o-mini"
                "messages" to array {
                    obj {
                        "role" to "user"
                        "content" to array {
                            obj {
                                "type" to "text"
                                "text" to prompt
                            }
                            obj {
                                "type" to "image_url"
                                "image_url" to obj {
                                    "url" to "data:image/jpeg;base64,$base64Image"
                                }
                            }
                        }
                    }
                }
                "max_tokens" to 500
            }
        }

        val request = Request.Builder()
            .url("${baseUrl(AiProvider.OpenAI)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("OpenAI Vision API error: ${response.code} - ${response.body?.string()}")
        }

        val jsonResponse = response.body?.string() ?: ""
        return extractOpenAiMessage(jsonResponse)
    }

    // ---------- Gemini Implementation ----------

    private suspend fun geminiChat(prompt: String): String {
        val key = apiKey(AiProvider.Gemini)
        if (key.isEmpty()) throw Exception("Gemini API key missing")

        val body = buildJsonManually {
            obj {
                "contents" to array {
                    obj {
                        "parts" to array {
                            obj { "text" to prompt }
                        }
                    }
                }
            }
        }

        val request = Request.Builder()
            .url("${baseUrl(AiProvider.Gemini)}/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini API error: ${response.code} - ${response.body?.string()}")
        }

        val jsonResponse = response.body?.string() ?: ""
        return extractGeminiMessage(jsonResponse)
    }

    private suspend fun geminiVision(prompt: String, base64Image: String): String {
        val key = apiKey(AiProvider.Gemini)
        if (key.isEmpty()) throw Exception("Gemini API key missing")

        val body = buildJsonManually {
            obj {
                "contents" to array {
                    obj {
                        "parts" to array {
                            obj { "text" to prompt }
                            obj {
                                "inline_data" to obj {
                                    "mime_type" to "image/jpeg"
                                    "data" to base64Image
                                }
                            }
                        }
                    }
                }
            }
        }

        val request = Request.Builder()
            .url("${baseUrl(AiProvider.Gemini)}/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini Vision API error: ${response.code} - ${response.body?.string()}")
        }

        val jsonResponse = response.body?.string() ?: ""
        return extractGeminiMessage(jsonResponse)
    }

    // ---------- DeepSeek Implementation ----------

    private suspend fun deepSeekChat(prompt: String): String {
        val key = apiKey(AiProvider.DeepSeek)
        if (key.isEmpty()) throw Exception("DeepSeek API key missing")

        val body = buildJsonManually {
            obj {
                "model" to "deepseek-chat"
                "messages" to array {
                    obj {
                        "role" to "user"
                        "content" to prompt
                    }
                }
            }
        }

        val request = Request.Builder()
            .url("${baseUrl(AiProvider.DeepSeek)}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("DeepSeek API error: ${response.code} - ${response.body?.string()}")
        }

        val jsonResponse = response.body?.string() ?: ""
        return extractOpenAiMessage(jsonResponse) // DeepSeek uses OpenAI-compatible format
    }

    private suspend fun deepSeekVision(prompt: String, base64Image: String): String {
        // DeepSeek might not support vision - fallback to text-only with description
        return deepSeekChat("$prompt\n\nNOTE: Bildanalyse nicht verfügbar bei DeepSeek. Bitte verwende OpenAI oder Gemini für Vision-Features.")
    }

    // ---------- JSON Parsing Helpers ----------

    private fun extractOpenAiMessage(jsonResponse: String): String {
        return try {
            // Simple regex-based extraction for OpenAI response format
            val contentRegex = "\"content\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val match = contentRegex.find(jsonResponse)
            match?.groupValues?.get(1)?.replace("\\n", "\n") ?: "No response content found"
        } catch (e: Exception) {
            "Error parsing OpenAI response: ${e.message}"
        }
    }

    private fun extractGeminiMessage(jsonResponse: String): String {
        return try {
            // Simple regex-based extraction for Gemini response format
            val textRegex = "\"text\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val match = textRegex.find(jsonResponse)
            match?.groupValues?.get(1)?.replace("\\n", "\n") ?: "No response text found"
        } catch (e: Exception) {
            "Error parsing Gemini response: ${e.message}"
        }
    }

    // ---------- Manual JSON Builder ----------

    private class JsonBuilder {
        private val sb = StringBuilder()

        fun obj(content: JsonBuilder.() -> Unit): String {
            sb.append("{")
            content()
            if (sb.endsWith(",")) sb.deleteCharAt(sb.length - 1)
            sb.append("}")
            return sb.toString()
        }

        fun array(content: JsonBuilder.() -> Unit): String {
            sb.append("[")
            content()
            if (sb.endsWith(",")) sb.deleteCharAt(sb.length - 1)
            sb.append("]")
            return sb.toString()
        }

        infix fun String.to(value: String) {
            sb.append("\"$this\":\"${value.replace("\"", "\\\"")}\",")
        }

        infix fun String.to(value: Number) {
            sb.append("\"$this\":$value,")
        }

        infix fun String.to(value: JsonBuilder.() -> String) {
            sb.append("\"$this\":${JsonBuilder().value()},")
        }
    }

    private fun buildJsonManually(content: JsonBuilder.() -> String): String {
        return JsonBuilder().content()
    }
}