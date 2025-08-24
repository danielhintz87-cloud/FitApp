package com.example.fitapp.ai

import com.example.fitapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Leichtgewichtiges AI-Gateway für mehrere Provider.
 * Aktuell: OpenAI produktiv; andere werden via API angebunden (sofern Key vorhanden).
 */
object AppAi {

    enum class Provider(val display: String) {
        OpenAI("OpenAI (GPT‑4/3.5)"),
        Gemini("Google Gemini"),
        Perplexity("Perplexity"),
        Copilot("Copilot"),
        DeepSeek("DeepSeek")
    }

    @Volatile
    var currentProvider: Provider = Provider.OpenAI

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun chatOnce(userText: String): String = withContext(Dispatchers.IO) {
        when (currentProvider) {
            Provider.OpenAI    -> openAiResponse(userText)
            Provider.Gemini    -> geminiResponse(userText)
            Provider.DeepSeek  -> deepSeekResponse(userText)
            Provider.Perplexity,
            Provider.Copilot   -> {
                delay(200)
                "ℹ️ ${currentProvider.display} ist vorbereitet. " +
                        "Hinterlege bei Bedarf den API‑Key & Endpoint – oder nutze vorerst OpenAI."
            }
        }
    }

    // ---------- OpenAI ----------
    private fun openAiApiKey(): String = BuildConfig.OPENAI_API_KEY ?: ""

    private suspend fun openAiResponse(prompt: String): String {
        val key = openAiApiKey()
        if (key.isBlank()) {
            delay(200)
            return "🔐 OpenAI‑Key nicht gesetzt. Lege in local.properties `OPENAI_API_KEY=...` ab."
        }
        // OpenAI Chat-Completions API Request
        val url = "https://api.openai.com/v1/chat/completions"
        val bodyJson = JSONObject().apply {
            put("model", "gpt-3.5-turbo")  // oder "gpt-4" falls verfügbar
            put("messages", JSONArray().apply {
                // System-Rolle: KI als Fitness-Coach instruieren
                put(JSONObject().put("role", "system").put("content",
                    "Du bist mein persönlicher Fitness- und Ernährungscoach. " +
                            "Antworte knapp, strukturiert, auf Deutsch."))
                // User-Eingabe
                put(JSONObject().put("role", "user").put("content", prompt))
            })
            put("temperature", 0.6)
            put("max_tokens", 600)
        }
        val req = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                return "❌ OpenAI: ${resp.code} – ${resp.message}"
            }
            val raw = resp.body?.string().orEmpty()
            return try {
                val root = JSONObject(raw)
                root.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            } catch (_: Throwable) {
                raw.take(2000)
            }
        }
    }

    // ---------- Gemini (Google PaLM API) ----------
    private fun geminiApiKey(): String = BuildConfig.GEMINI_API_KEY ?: ""

    private suspend fun geminiResponse(prompt: String): String {
        val key = geminiApiKey()
        if (key.isBlank()) {
            delay(200)
            return "🔐 Gemini‑Key nicht gesetzt. Lege in local.properties `GEMINI_API_KEY=...` ab."
        }
        // Google Generative Language API (Gemini) Request
        val url = "https://generativelanguage.googleapis.com/v1beta2/models/text-bison-001:generateText?key=$key"
        // Hinweis: 'text-bison-001' als Platzhalter-Modellname (Gemini noch Beta)
        val bodyJson = JSONObject().apply {
            put("prompt", JSONObject().put("text", prompt))
            put("temperature", 0.7)
            put("candidateCount", 1)
        }
        val req = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                return "❌ Gemini: ${resp.code} – ${resp.message}"
            }
            val raw = resp.body?.string().orEmpty()
            return try {
                val root = JSONObject(raw)
                // 'candidates'[0].'output' enthält den generierten Text (bei PaLM API)
                root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getString("output")
                    .trim()
            } catch (_: Throwable) {
                raw.take(2000)
            }
        }
    }

    // ---------- DeepSeek ----------
    private fun deepSeekApiKey(): String = BuildConfig.DEEPSEEK_API_KEY ?: ""

    private suspend fun deepSeekResponse(prompt: String): String {
        val key = deepSeekApiKey()
        if (key.isBlank()) {
            delay(200)
            return "🔐 DeepSeek‑Key nicht gesetzt. Lege in local.properties `DEEPSEEK_API_KEY=...` ab."
        }
        val url = "https://api.deepseek.com/chat/completions"
        val bodyJson = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", JSONArray().put(
                JSONObject().put("role", "user").put("content", prompt)
            ))
        }
        val req = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                return "❌ DeepSeek: ${resp.code} – ${resp.message}"
            }
            val raw = resp.body?.string().orEmpty()
            return try {
                val root = JSONObject(raw)
                root.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            } catch (_: Throwable) {
                raw.take(2000)
            }
        }
    }
}
