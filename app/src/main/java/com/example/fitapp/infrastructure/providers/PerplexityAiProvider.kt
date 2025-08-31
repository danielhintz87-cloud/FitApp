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
/**
 * Perplexity AI provider implementation
 */
class PerplexityAiProvider(
    private val context: Context,
    private val httpClient: OkHttpClient
) : AiProvider {
    
    override val providerType = com.example.fitapp.domain.entities.AiProvider.Perplexity
    
    override suspend fun isAvailable(): Boolean {
        // Perplexity is temporarily disabled by default
        // To re-enable, change this to: return ApiKeys.getPerplexityKey(context).isNotBlank()
        return false
    }
    
    override fun supportsVision(): Boolean = false
    
    override suspend fun generateText(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = ApiKeys.getPerplexityKey(context)
            if (apiKey.isBlank()) {
                throw IllegalStateException("Perplexity API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
            }
            
            val body = """
                {
                    "model": "llama-3.1-sonar-small-128k-online",
                    "messages": [
                        {"role": "user", "content": ${prompt.json()}}
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
                        400 -> "Perplexity 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        401 -> "Perplexity 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        402 -> "Perplexity 402: Guthaben aufgebraucht oder Zahlung erforderlich. Bitte prüfen Sie Ihr Perplexity-Konto."
                        403 -> "Perplexity 403: Zugriff verweigert. Möglicherweise ist Ihr API-Schlüssel nicht für diesen Service berechtigt."
                        429 -> "Perplexity 429: Zu viele Anfragen. Bitte warten Sie ein paar Sekunden und versuchen Sie es erneut."
                        in 500..599 -> "Perplexity ${response.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                        else -> "Perplexity ${response.code}: ${bodyStr.take(200)}"
                    }
                    throw IllegalStateException(errorMsg)
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
    }
    
    override suspend fun analyzeImage(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> {
        return Result.failure(
            IllegalStateException("Perplexity unterstützt keine Bildanalyse. Verwende Gemini für multimodale Aufgaben.")
        )
    }
    
    private fun String.json(): String = "\"${this.replace("\"", "\\\"").replace("\n", "\\n")}\""
}