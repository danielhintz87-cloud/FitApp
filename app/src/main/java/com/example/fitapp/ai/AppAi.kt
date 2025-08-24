package com.example.fitapp.ai

import com.example.fitapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Leichtgewichtiges AI-Gateway für mehrere Provider.
 * Aktuell: OpenAI produktiv; andere liefern eine freundliche Info bis Keys/Endpunkte hinterlegt sind.
 */
object AppAi {

    enum class Provider(val display: String) {
        OpenAI("OpenAI (GPT‑5)"),
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
            Provider.OpenAI -> openAiResponse(userText)
            Provider.Gemini,
            Provider.Perplexity,
            Provider.Copilot,
            Provider.DeepSeek -> {
                delay(200)
                "ℹ️ ${'$'}{currentProvider.display} ist vorbereitet. Hinterlege bei Bedarf den API‑Key & Endpoint – oder nutze vorerst OpenAI."
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

        val url = "https://api.openai.com/v1/responses"
        val bodyJson = JSONObject()
            .put("model", "gpt-5")
            .put("input", prompt)
            .put("temperature", 0.3)

        val req = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${'$'}key")
            .header("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return "❌ OpenAI: ${'$'}{resp.code} – ${'$'}{resp.message}"
            val raw = resp.body?.string().orEmpty()
            return parseOutputText(raw)
        }
    }

    private fun parseOutputText(raw: String): String {
        return try {
            val root = JSONObject(raw)
            if (root.has("output_text")) {
                val s = root.optString("output_text").trim()
                if (s.isNotEmpty()) return s
            }
            val arr = root.optJSONArray("responses")
            if (arr != null && arr.length() > 0) {
                val s = arr.getJSONObject(0).optString("output_text").trim()
                if (s.isNotEmpty()) return s
            }
            raw.take(2000)
        } catch (_: Throwable) {
            raw.take(2000)
        }
    }
}

