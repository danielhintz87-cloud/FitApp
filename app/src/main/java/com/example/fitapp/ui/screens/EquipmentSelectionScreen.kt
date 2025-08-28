package com.example.fitapp.ai

import android.content.Context
import com.example.fitapp.BuildConfig
import com.example.fitapp.data.db.AiLogDao
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.Semaphore
import kotlin.math.pow
import kotlin.random.Random

/**
 * Zentraler OpenAI-Zugriff mit Retry/Backoff.
 * (Gemäß deiner Entscheidung aktuell nur OpenAI aktiv.)
 */
class AiCore(
    private val context: Context,
    private val logDao: AiLogDao
) {

    private val http = OkHttpClient()
    private val requestSemaphore = Semaphore(2) // max. 2 gleichzeitige Requests

    /** Hilfsfunktion: sichere JSON-String-Quote */
    private fun String.json(): String = JSONObject.quote(this)

    /** true = Fehler ist mit Wartezeit erneut versuchbar */
    private fun isRetriableError(statusCode: Int?, exception: Exception?): Boolean {
        return when {
            statusCode == 429 -> true                // Rate limit / insufficient_quota
            statusCode in 500..599 -> true           // Serverfehler
            exception?.message?.contains("timeout", ignoreCase = true) == true -> true
            exception?.message?.contains("connection", ignoreCase = true) == true -> true
            else -> false
        }
    }

    /**
     * Führt einen OpenAI-Call mit bis zu 5 Versuchen aus.
     * Beachtet 'Retry-After' falls vorhanden, sonst Exponential Backoff mit Jitter.
     */
    private suspend fun <T> openAiWithRetry(request: suspend () -> T): T {
        var lastException: Exception? = null

        for (attempt in 0..4) { // 5 Versuche (0,1,2,3,4)
            try {
                requestSemaphore.acquire()
                try {
                    return request()
                } finally {
                    requestSemaphore.release()
                }
            } catch (e: Exception) {
                lastException = e

                // Heuristik: Statuscode aus Fehlermeldung extrahieren (Serverantwort)
                val lastStatusCode = when {
                    e.message?.contains(" 429") == true || e.message?.contains("429") == true -> 429
                    e.message?.contains(" 500") == true || e.message?.contains("500") == true -> 500
                    e.message?.contains(" 502") == true || e.message?.contains("502") == true -> 502
                    e.message?.contains(" 503") == true || e.message?.contains("503") == true -> 503
                    e.message?.contains(" 504") == true || e.message?.contains("504") == true -> 504
                    else -> null
                }

                if (attempt < 4 && isRetriableError(lastStatusCode, e)) {
                    // Try to read Retry-After aus der Exception-Message
                    val retryAfter = parseRetryAfter(
                        e.message?.substringAfter("Retry-After: ")?.substringBefore("\n")
                            ?: e.message?.substringAfter("retry_after=")?.substringBeforeAny(',', ' ', ')')
                    )
                    val delayMs = retryAfter ?: calculateBackoffDelay(attempt)
                    delay(delayMs)
                } else {
                    throw e
                }
            }
        }
        throw lastException ?: IllegalStateException("Max retries exceeded")
    }

    /**
     * Minimaler Chat-Call zu OpenAI (nutzt Key aus App-Einstellungen, sonst BuildConfig).
     * Gibt Result<String> zurück, damit der Aufrufer .getOrElse{} nutzen kann.
     */
    suspend fun openAiChat(prompt: String): Result<String> = runCatching {
        openAiWithRetry {
            val apiKey = ApiKeys.getOpenAiKey(context)
                .ifBlank { BuildConfig.OPENAI_API_KEY }
            if (apiKey.isBlank()) {
                throw IllegalStateException("OpenAI API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
            }

            val model = BuildConfig.OPENAI_MODEL.ifBlank { "gpt-4o-mini" }
            val body = """
                {"model":"$model","messages":[{"role":"user","content":${prompt.json()}}],"temperature":0.4}
            """.trimIndent().toRequestBody("application/json".toMediaType())

            val base = BuildConfig.OPENAI_BASE_URL.ifBlank { "https://api.openai.com" }
            val req = Request.Builder()
                .url("$base/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("User-Agent", "fitapp/1.0")
                .post(body)
                .build()

            http.newCall(req).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    // Retry-After direkt an die Exception anhängen, damit der Parser es findet
                    val retry = resp.header("Retry-After")?.let { " | Retry-After: $it" } ?: ""
                    val msg = try {
                        JSONObject(raw).optJSONObject("error")?.optString("message")
                    } catch (_: Exception) { null }
                    throw IllegalStateException("OpenAI HTTP ${resp.code} – ${msg ?: raw}$retry")
                }
                val json = JSONObject(raw)
                json.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content")
            }
        }
    }

    // -------- Retry/Backoff Hilfen --------

    /** Parsed "Retry-After" (Sekunden oder RFC1123-Datum), liefert Delay in Millisekunden. */
    private fun parseRetryAfter(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val seconds = value.trim().toLongOrNull()
        if (seconds != null) return seconds * 1000L
        // RFC1123 wäre aufwendig; wenn kein Sekundenwert: kleiner Fallback
        return 2_000L
    }

    private fun String.substringBeforeAny(vararg chars: Char): String {
        val idx = this.indexOfFirst { c -> chars.contains(c) }
        return if (idx >= 0) this.substring(0, idx) else this
    }

    /** Exponential Backoff: 1s, 2s, 4s, 8s (+ Jitter 0..250ms) */
    private fun calculateBackoffDelay(attempt: Int): Long {
        val base = 1000.0 * 2.0.pow(attempt.toDouble())
        val jitter = Random.nextInt(0, 250)
        return base.toLong() + jitter
    }
}
