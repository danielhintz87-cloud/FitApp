package com.example.fitapp.infrastructure.providers

import android.content.Context
import android.graphics.Bitmap
import com.example.fitapp.data.prefs.ApiKeys
import com.example.fitapp.domain.entities.CaloriesEstimate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.example.fitapp.infrastructure.ai.*
/**
 * Perplexity AI provider implementation
 * 
 * Cost-Optimized Configuration for Fitness App:
 * - Model: Sonar Basic  
 * - Cost: $1 input / $1 output per million tokens + $5 per 1000 searches
 * - Ideal for: Current fitness trends, research, equipment reviews, health news
 * 
 * Primary use cases:
 * ✓ Latest fitness trends and exercise variations
 * ✓ Current nutrition research and supplement studies  
 * ✓ Equipment reviews and price comparisons
 * ✓ Health and wellness news updates
 * ✓ Scientific research citations and fact-checking
 * 
 * Usage Strategy: Reserve for dynamic, time-sensitive information
 * Uses intelligent model fallback for maximum compatibility and cost-efficiency
 */
class PerplexityAiProvider(
    private val context: Context,
    private val httpClient: OkHttpClient
) : AiProvider {
    private val logTag = "PerplexityProvider"
    
    // Cost-optimized model hierarchy for Fitness App (2025)
    // Primary: Sonar Basic ($1/$1 + $5/1000 searches) - Best price/performance
    // Focus on research, trends, and current information
    private val modelHierarchy = listOf(
        // Primary: Sonar Basic (cheapest, ideal for fitness research)
        "sonar",                            // Basic Sonar - $1 input/$1 output
        
        // Fallbacks for compatibility
        "sonar-small",                      // If sonar doesn't work
        "sonar-medium",                     // Medium tier
        
        // Legacy models as last resort
        "llama-3.1-sonar-small-128k-online",
        "mixtral-8x7b-instruct",
        "llama-2-70b"
    )
    
    override val providerType = com.example.fitapp.domain.entities.AiProvider.Perplexity
    
    override suspend fun isAvailable(): Boolean {
        val apiKey = ApiKeys.getPerplexityKey(context)
        if (apiKey.isBlank()) return false
        
        // Quick availability check - try simplest possible request
        return try {
            makeApiCall(apiKey, modelHierarchy.first(), "test")
            true
        } catch (e: Exception) {
            android.util.Log.w(logTag, "availability_check_failed message=${e.message}")
            // Return true if we have a key - let the actual usage handle model issues
            true
        }
    }
    
    override fun supportsVision(): Boolean = false
    
    override suspend fun generateText(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = ApiKeys.getPerplexityKey(context)
            if (apiKey.isBlank()) {
                throw IllegalStateException("Perplexity API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
            }
            
            // Try models in cost-optimized order
            var lastError: Exception? = null
            
            for (model in modelHierarchy) {
                try {
                    val response = makeApiCall(apiKey, model, prompt)
                    // Log successful model for future optimization
                    android.util.Log.d(logTag, "model_success model=$model")
                    return@runCatching response
                } catch (e: Exception) {
                    android.util.Log.w(logTag, "model_failed model=$model message=${e.message}")
                    lastError = e
                    
                    // Don't try other models for auth errors
                    if (e.message?.contains("401") == true || e.message?.contains("403") == true) {
                        throw e
                    }
                }
            }
            
            // All models failed
            throw lastError ?: IllegalStateException("Alle Perplexity-Modelle fehlgeschlagen. API möglicherweise nicht verfügbar.")
        }
    }
    
    private suspend fun makeApiCall(apiKey: String, model: String, prompt: String): String {
            val body = """
                {
                    "model": "$model",
                    "messages": [
                        {"role": "user", "content": "${prompt.json()}"}
                    ],
                    "temperature": 0.4,
                    "max_tokens": 4096
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.perplexity.ai/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("User-Agent", "fitapp/1.0")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string().orEmpty()
                    val errorMsg = when (response.code) {
                        400 -> {
                            if (bodyStr.contains("Invalid model")) {
                                throw IllegalArgumentException("Modell '$model' nicht verfügbar")
                            }
                            "Perplexity 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        }
                        401 -> "Perplexity 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        402 -> "Perplexity 402: Guthaben aufgebraucht oder Zahlung erforderlich. Bitte prüfen Sie Ihr Perplexity-Konto."
                        403 -> "Perplexity 403: Zugriff verweigert. Möglicherweise ist Ihr API-Schlüssel nicht für diesen Service berechtigt."
                        429 -> "Perplexity 429: Zu viele Anfragen. Bitte warten Sie ein paar Sekunden und versuchen Sie es erneut."
                        in 500..599 -> "Perplexity ${response.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                        else -> "Perplexity ${response.code}: ${bodyStr.take(200)}"
                    }
                    val aiErr = classifyHttpError(response.code, bodyStr)
                    android.util.Log.w(logTag, "http_error code=${response.code} model=$model aiErr=${aiErr.code} bodySnippet=${bodyStr.take(120)}")
                    throw ClassifiedAiException(errorMsg, aiErr, response.code)
                }
                
                val responseBody = response.body
                if (responseBody == null) {
                    throw IllegalStateException("Perplexity: Leere Antwort vom Server erhalten")
                }
                
                val txt = responseBody.string()
                if (txt.isBlank()) {
                    throw IllegalStateException("Perplexity: Leere Antwort vom Server erhalten")
                }
                
                // Parse OpenAI-compatible response from Perplexity
                val contentStart = txt.indexOf("\"content\":\"") + 11
                val contentEnd = txt.indexOf("\"", contentStart)
                val content = if (contentStart > 10 && contentEnd > contentStart) {
                    txt.substring(contentStart, contentEnd).replace("\\\"", "\"").replace("\\n", "\n")
                } else {
                    // Try to parse as JSON for better error handling
                    try {
                        val jsonObj = JSONObject(txt)
                        val choices = jsonObj.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val choice = choices.optJSONObject(0)
                            val message = choice?.optJSONObject("message")
                            message?.optString("content") ?: "Error parsing response"
                        } else {
                            "Error parsing response"
                        }
                    } catch (e: Exception) {
                        "Error parsing response: ${e.message}"
                    }
                }
                content
            }
    }
    
    override suspend fun analyzeImage(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> {
        return Result.failure(
            IllegalStateException("Perplexity unterstützt keine Bildanalyse. Verwende Gemini für multimodale Aufgaben.")
        )
    }
    
    private fun String.json(): String = "\"${this.replace("\"", "\\\"").replace("\n", "\\n")}\""
}