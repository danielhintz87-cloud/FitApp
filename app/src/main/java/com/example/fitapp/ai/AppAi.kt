package com.example.fitapp.ai

import android.util.Log
import com.example.fitapp.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

private const val TAG = "AppAi"

enum class AiProvider { OpenAI, Gemini, DeepSeek }

object AppAi {

    // ---------- Public API ----------

    suspend fun generate12WeekPlan(
        goal: String,
        daysPerWeek: Int,
        intensity: String,
        equipment: List<String>,
        provider: AiProvider = AiProvider.OpenAI
    ): String {
        val prompt = """
            Erstelle einen durchgehenden 12‑Wochen-Trainingsplan (Kalender-Format, Woche 1–12).
            Parameter:
            - Ziel: $goal
            - Einheiten pro Woche: $daysPerWeek
            - Intensität: $intensity
            - Equipment: ${equipment.joinToString()}
            
            Format (Markdown, **ohne** Codeblock):
            ## Woche 1
            - Tag 1 (Dauer: 45): Übung A – Sätze×Wdh …
            …
            ## Woche 12
            …
        """.trimIndent()
        return callTextModel(prompt, provider)
    }

    suspend fun suggestAlternativeAndLog(
        todaysPlanMarkdown: String,
        constraints: String,
        provider: AiProvider = AiProvider.OpenAI
    ): String {
        val prompt = """
            Nutzer möchte eine Alternative zum heutigen Plan, die sich gut einfügt.
            Bedingungen: $constraints
            Ursprünglicher Plan (Markdown):
            $todaysPlanMarkdown

            Gib NUR einen kurzen Ersatz-Block (Markdown, ohne Codeblock) + kurze Begründung in 1 Satz.
        """.trimIndent()
        return callTextModel(prompt, provider)
    }

    suspend fun generateRecipes(
        preferences: String,
        count: Int = 10,
        provider: AiProvider = AiProvider.OpenAI
    ): String {
        val prompt = """
            Generiere $count abwechslungsreiche Rezepte passend zu:
            $preferences
            Ausgabe als Markdown-Liste:
            ### Titel – (Zeit min / kcal geschätzt)
            Zutaten:
            - …
            Schritte:
            1. …
        """.trimIndent()
        return callTextModel(prompt, provider)
    }

    // ---------- Core ----------

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

    private suspend fun callTextModel(prompt: String, provider: AiProvider): String {
        return when (provider) {
            AiProvider.OpenAI -> openAiChat(prompt)
            AiProvider.Gemini -> gemini(prompt)
            AiProvider.DeepSeek -> deepSeek(prompt)
        }
    }

    // ---------- OpenAI ----------
    @Serializable private data class ChatReq(
        val model: String = "gpt-4o-mini",
        val messages: List<ChatMsg>,
        val temperature: Double = 0.7
    )
    @Serializable private data class ChatMsg(val role: String, val content: String)
    @Serializable private data class ChatResp(val choices: List<Choice>) {
        @Serializable data class Choice(val message: ChatMsg)
    }

    private fun openAiChat(prompt: String): String {
        val key = BuildConfig.OPENAI_API_KEY
        require(key.isNotEmpty()) { "OPENAI_API_KEY fehlt" }

        val body = json.encodeToString(
            ChatReq.serializer(),
            ChatReq(messages = listOf(ChatMsg("user", prompt)))
        )
        val req = Request.Builder()
            .url("${'$'}{BuildConfig.OPENAI_BASE_URL}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val resp = http.newCall(req).execute()
        if (!resp.isSuccessful) {
            val code = resp.code
            val msg = resp.body?.string().orEmpty()
            Log.e(TAG, "OpenAI Fehler $code: $msg")
            return "❌ OpenAI: $code – ${'$'}{msg.take(250)}"
        }
        val parsed = json.decodeFromString(ChatResp.serializer(), resp.body!!.string())
        return parsed.choices.firstOrNull()?.message?.content?.trim().orEmpty()
    }

    // ---------- Gemini ----------
    @Serializable private data class GeminiReq(
        val contents: List<Map<String, List<Map<String, String>>>> // minimal
    )
    @Serializable private data class GeminiResp(
        val candidates: List<Map<String, Map<String, String>>>? = null
    )

    private fun gemini(prompt: String): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty()) return "ℹ️ Gemini: API‑Key noch nicht hinterlegt."
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$key"
        val body = """
            {"contents":[{"parts":[{"text":${'$'}{json.encodeToString(String.serializer(), prompt)}}]}]}
        """.trimIndent()
        val req = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        val resp = http.newCall(req).execute()
        if (!resp.isSuccessful) {
            val code = resp.code; val msg = resp.body?.string().orEmpty()
            Log.e(TAG, "Gemini Fehler $code: $msg")
            return "❌ Gemini: $code – ${'$'}{msg.take(250)}"
        }
        val parsed = json.decodeFromString(GeminiResp.serializer(), resp.body!!.string())
        val text = parsed.candidates?.firstOrNull()
            ?.get("content")?.get("parts")?.toString() ?: ""
        return if (text.isBlank()) "ℹ️ Gemini: keine Antwort" else text
    }

    // ---------- DeepSeek (optional) ----------
    private fun deepSeek(prompt: String): String {
        val key = BuildConfig.DEEPSEEK_API_KEY
        if (key.isEmpty()) return "ℹ️ DeepSeek: API‑Key noch nicht hinterlegt."
        val body = """
            {"model":"deepseek-chat","messages":[{"role":"user","content":${'$'}{json.encodeToString(String.serializer(), prompt)}}]}
        """.trimIndent()
        val req = Request.Builder()
            .url("${'$'}{BuildConfig.DEEPSEEK_BASE_URL}/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        val resp = http.newCall(req).execute()
        if (!resp.isSuccessful) {
            val code = resp.code; val msg = resp.body?.string().orEmpty()
            Log.e(TAG, "DeepSeek Fehler $code: $msg")
            return "❌ DeepSeek: $code – ${'$'}{msg.take(250)}"
        }
        val parsed = json.decodeFromString(ChatResp.serializer(), resp.body!!.string())
        return parsed.choices.firstOrNull()?.message?.content?.trim().orEmpty()
    }
}
