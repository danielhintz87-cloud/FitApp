package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.fitapp.BuildConfig
import com.example.fitapp.data.db.AiLog
import com.example.fitapp.data.db.AiLogDao
import com.example.fitapp.data.prefs.ApiKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Semaphore
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random

enum class AiProvider { OpenAI }

data class PlanRequest(
    val goal: String,
    val weeks: Int = 12,
    val sessionsPerWeek: Int,
    val minutesPerSession: Int,
    val equipment: List<String> = emptyList()
)

data class RecipeRequest(
    val preferences: String,
    val diet: String,
    val count: Int = 10
)

data class CaloriesEstimate(val kcal: Int, val confidence: Int, val text: String)

// Simple recipe model for UI
data class UiRecipe(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val markdown: String,
    val calories: Int? = null,
    val imageUrl: String? = null
)

class AiCore(private val context: Context, private val logDao: AiLogDao) {

    private val http = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generatePlan(provider: AiProvider, req: PlanRequest): Result<String> =
        callText(provider,
            "Erstelle einen wissenschaftlich fundierten **${req.weeks}-Wochen-Trainingsplan** in Markdown. " +
            "Ziel: ${req.goal}. Trainingsfrequenz: ${req.sessionsPerWeek} Einheiten/Woche, ${req.minutesPerSession} Min/Einheit. " +
            "Verfügbare Geräte: ${req.equipment.joinToString()}. " +
            "\n\n**Medizinische Anforderungen:**\n" +
            "- Progressive Überlastung mit 5-10% Steigerung alle 2 Wochen\n" +
            "- Deload-Wochen alle 4 Wochen (50-60% Intensität)\n" +
            "- RPE-Skala (6-20) für Intensitätskontrolle\n" +
            "- Mindestens 48h Pause zwischen gleichen Muskelgruppen\n" +
            "- Aufwärm- und Cool-Down-Protokolle\n" +
            "\n**Struktur:** Jede Woche mit H2-Überschrift, dann Trainingstage mit:\n" +
            "- Aufwärmung (5-10 Min)\n" +
            "- Hauptübungen: Übung | Sätze x Wiederholungen | Tempo (1-2-1-0) | RPE | Pausenzeit\n" +
            "- Cool-Down & Mobility (5-10 Min)\n" +
            "- Progressionshinweise für nächste Woche\n" +
            "- Anpassungen bei Beschwerden oder Stagnation"
        )

    suspend fun generateRecipes(provider: AiProvider, req: RecipeRequest): Result<String> =
        callText(provider,
            "Erstelle ${req.count} **nutritionsoptimierte Rezepte** als präzise Markdown-Liste. " +
            "Präferenzen: ${req.preferences}. Diätform: ${req.diet}. " +
            "\n\n**Anforderungen pro Rezept:**\n" +
            "- Titel (## Format)\n" +
            "- Zutatenliste mit exakten Gramm-Angaben (nicht 'eine Tasse' sondern '150g')\n" +
            "- Schritt-für-Schritt Zubereitung (nummeriert)\n" +
            "- **Präzise Nährwerte:** Kalorien, Protein, Kohlenhydrate, Fett (jeweils in g), Ballaststoffe\n" +
            "- Portionsgröße und Anzahl Portionen\n" +
            "- Zubereitungszeit & Schwierigkeitsgrad\n" +
            "- Mikronährstoff-Highlights (Vitamin C, Eisen, etc.)\n" +
            "\n**Kalkulationsbasis:** Verwende USDA-Nährwertdatenbank-Standards für genaue Berechnungen. " +
            "Achte auf realistische Portionsgrößen und präzise Makronährstoff-Verteilung."
        )

    suspend fun parseShoppingList(provider: AiProvider, spokenText: String): Result<String> =
        callText(provider,
            "Analysiere folgenden gesprochenen Text und extrahiere einzelne Einkaufsliste-Items: '$spokenText'\n\n" +
            "**Aufgabe:** Zerlege den Text in einzelne Lebensmittel mit optional genannten Mengen.\n\n" +
            "**Ausgabeformat:** Eine Zeile pro Item im Format: 'Produktname|Menge'\n" +
            "**Beispiele:**\n" +
            "- 'Äpfel|2kg'\n" +
            "- 'Milch|1L'\n" +
            "- 'Brot|1 Stück'\n" +
            "- 'Bananen|' (wenn keine Menge genannt)\n\n" +
            "**Regeln:**\n" +
            "- Erkenne Trennwörter wie 'und', 'sowie', ',', '&'\n" +
            "- Normalisiere Produktnamen (z.B. 'Tomaten' statt 'Tomate')\n" +
            "- Wenn keine Menge explizit genannt, lasse Mengenfeld leer\n" +
            "- Ignoriere Füllwörter wie 'ich brauche', 'kaufen', etc.\n" +
            "- Ein Item pro Zeile, keine zusätzlichen Erklärungen"
        )

    suspend fun estimateCaloriesFromPhoto(provider: AiProvider, bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate> =
        withContext(Dispatchers.IO) {
            val prompt = "Analysiere das Bild und schätze präzise die Kalorien des gezeigten Essens.\n\n" +
                "**Analyseschritte:**\n" +
                "1. Identifiziere alle sichtbaren Lebensmittel/Getränke\n" +
                "2. Schätze Portionsgrößen anhand von Referenzobjekten (Teller ≈ 25cm, Gabel ≈ 20cm, Hand ≈ 18cm)\n" +
                "3. Berücksictige Zubereitungsart (frittiert +30%, gedämpft -20%)\n" +
                "4. Kalkuliere Gesamtkalorien mit USDA-Nährwertstandards\n\n" +
                "**Antwortformat:**\n" +
                "kcal: <Zahl>\n" +
                "confidence: <0-100>\n" +
                "Begründung: [Lebensmittel] ca. [Gramm]g = [kcal]kcal, [weitere Komponenten]\n" +
                "Unsicherheitsfaktoren: [versteckte Fette, Portionsgröße, etc.]"
            val started = System.currentTimeMillis()
            val r = openAiVision(prompt, bitmap)
            val took = System.currentTimeMillis() - started
            r.onSuccess {
                logDao.insert(AiLog.success("vision_calories", provider.name, prompt + " $note", it.toString(), took))
            }.onFailure {
                logDao.insert(AiLog.error("vision_calories", provider.name, prompt + " $note", it.message ?: "unknown", took))
            }
            r
        }

    suspend fun callText(provider: AiProvider, prompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            val started = System.currentTimeMillis()
            val result = openAiChat(prompt)
            val took = System.currentTimeMillis() - started
            result.onSuccess {
                logDao.insert(AiLog.success("text", provider.name, prompt, it, took))
            }.onFailure {
                logDao.insert(AiLog.error("text", provider.name, prompt, it.message ?: "unknown", took))
            }
            result
        }

    // -------- OpenAI --------

    /**
     * Calculate exponential backoff delay with jitter for retry attempts
     */
    private fun calculateBackoffDelay(attempt: Int): Long {
        val baseDelays = listOf(800L, 1500L, 2500L, 4000L, 6000L)
        val baseDelay = baseDelays.getOrElse(attempt) { 8000L }
        val jitter = Random.nextLong(0, 400)
        return baseDelay + jitter
    }

    /**
     * Parse retry-after header value from OpenAI response - improved version
     */
    private fun parseRetryAfter(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val seconds = value.trim().toLongOrNull()
        if (seconds != null) return seconds * 1000L
        // RFC1123 date parsing would be complex; fallback for non-numeric values
        return 2_000L
    }

    /**
     * Check if an exception or HTTP status code indicates a retriable error
     */
    private fun isRetriableError(statusCode: Int?, exception: Exception?): Boolean {
        return when {
            statusCode == 429 -> true  // Rate limit
            statusCode in 500..599 -> true  // Server errors
            exception?.message?.contains("timeout", ignoreCase = true) == true -> true
            exception?.message?.contains("connection", ignoreCase = true) == true -> true
            else -> false
        }
    }

    private suspend fun <T> openAiWithRetry(request: suspend () -> T): T {
        var lastException: Exception? = null
        
        for (attempt in 0..4) { // Increased to 4 attempts
            try {
                // Acquire semaphore to limit concurrent requests
                requestSemaphore.acquire()
                try {
                    return request()
                } finally {
                    requestSemaphore.release()
                }
            } catch (e: Exception) {
                lastException = e

                // Extract status code if available - improved pattern matching
                val lastStatusCode = when {
                    e.message?.contains(" 429") == true || e.message?.contains("429") == true -> 429
                    e.message?.contains(" 500") == true || e.message?.contains("500") == true -> 500
                    e.message?.contains(" 502") == true || e.message?.contains("502") == true -> 502
                    e.message?.contains(" 503") == true || e.message?.contains("503") == true -> 503
                    e.message?.contains(" 504") == true || e.message?.contains("504") == true -> 504
                    else -> null
                }

                if (attempt < 4 && isRetriableError(lastStatusCode, e)) {
                    // Improved retry-after parsing - check multiple formats
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

    private suspend fun openAiChat(prompt: String): Result<String> = runCatching {
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
            if (!resp.isSuccessful) {
                val bodyStr = resp.body?.string().orEmpty()
                val retryAfter = resp.header("Retry-After")
                val errorMsg = when (resp.code) {
                    400 -> "OpenAI 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                    401 -> "OpenAI 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                    402 -> "OpenAI 402: Guthaben aufgebraucht oder Zahlung erforderlich. Bitte prüfen Sie Ihr OpenAI-Konto."
                    403 -> "OpenAI 403: Zugriff verweigert. Möglicherweise ist Ihr API-Schlüssel nicht für diesen Service berechtigt."
                    429 -> {
                        val waitTime = retryAfter?.let { "${it}s" } ?: "ein paar Sekunden"
                        "OpenAI 429: Zu viele Anfragen. Bitte warten Sie $waitTime und versuchen Sie es erneut."
                    }
                    in 500..599 -> "OpenAI ${resp.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                    else -> "OpenAI ${resp.code}: ${bodyStr.take(200)}"
                }
                // Include retry-after in error message for retry logic
                val fullError = if (retryAfter != null) "$errorMsg Retry-After: $retryAfter" else errorMsg
                error(fullError)
            }
            val txt = resp.body!!.string()
            // Manual JSON parsing for simplicity
            val contentStart = txt.indexOf("\"content\":\"") + 11
            val contentEnd = txt.indexOf("\"", contentStart)
            val content = if (contentStart > 10 && contentEnd > contentStart) {
                txt.substring(contentStart, contentEnd).replace("\\\"", "\"").replace("\\n", "\n")
            } else "Error parsing response"
            content
        }
        }
    }

    private suspend fun openAiVision(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> = runCatching {
        openAiWithRetry {
        val apiKey = ApiKeys.getOpenAiKey(context)
            .ifBlank { BuildConfig.OPENAI_API_KEY }
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenAI API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val model = BuildConfig.OPENAI_MODEL.ifBlank { "gpt-4o-mini" }
        val optimizedBitmap = optimizeBitmapForVision(bitmap)
        val dataUrl = "data:image/jpeg;base64," + optimizedBitmap.toJpegBytes(80).b64()
        val body = """
        {
          "model":"$model",
          "messages":[
            {"role":"user","content":[
              {"type":"text","text":${prompt.json()}},
              {"type":"image_url","image_url":{"url":${dataUrl.json()}}}
            ]}
          ]
        }""".trimIndent().toRequestBody("application/json".toMediaType())

        val base = BuildConfig.OPENAI_BASE_URL.ifBlank { "https://api.openai.com" }
        val req = Request.Builder()
            .url("$base/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("User-Agent", "fitapp/1.0")
            .post(body).build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val bodyStr = resp.body?.string().orEmpty()
                val retryAfter = resp.header("Retry-After")
                val errorMsg = when (resp.code) {
                    400 -> "OpenAI 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                    401 -> "OpenAI 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                    402 -> "OpenAI 402: Guthaben aufgebraucht oder Zahlung erforderlich. Bitte prüfen Sie Ihr OpenAI-Konto."
                    403 -> "OpenAI 403: Zugriff verweigert. Möglicherweise ist Ihr API-Schlüssel nicht für diesen Service berechtigt."
                    429 -> {
                        val waitTime = retryAfter?.let { "${it}s" } ?: "ein paar Sekunden"
                        "OpenAI 429: Zu viele Anfragen. Bitte warten Sie $waitTime und versuchen Sie es erneut."
                    }
                    in 500..599 -> "OpenAI ${resp.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                    else -> "OpenAI ${resp.code}: ${bodyStr.take(200)}"
                }
                // Include retry-after in error message for retry logic
                val fullError = if (retryAfter != null) "$errorMsg Retry-After: $retryAfter" else errorMsg
                error(fullError)
            }
            val txt = resp.body!!.string()
            // Manual JSON parsing for vision response
            val contentStart = txt.indexOf("\"content\":\"") + 11
            val contentEnd = txt.indexOf("\"", contentStart)
            val text = if (contentStart > 10 && contentEnd > contentStart) {
                txt.substring(contentStart, contentEnd).replace("\\\"", "\"").replace("\\n", "\n")
            } else "Error parsing response"
            parseCalories(text)
        }
        }
    }

    // -------- Helpers --------

    /**
     * Optimize bitmap for OpenAI Vision API to reduce token usage
     */
    private fun optimizeBitmapForVision(bitmap: Bitmap): Bitmap {
        val maxDimension = 1024
        val width = bitmap.width
        val height = bitmap.height
        val scaleFactor = (maxDimension.toFloat() / maxOf(width, height)).coerceAtMost(1f)
        
        return if (scaleFactor < 1f) {
            val newWidth = (width * scaleFactor).toInt()
            val newHeight = (height * scaleFactor).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
    }

    private fun parseCalories(text: String): CaloriesEstimate {
        val kcalRegex = Regex("(\\d{2,4})\\s*(k?cal|Kilokalorien)", RegexOption.IGNORE_CASE)
        val confRegex = Regex("(confidence|sicherheit)[^0-9]{0,8}(\\d{1,3})", RegexOption.IGNORE_CASE)
        val kcal = kcalRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val conf = confRegex.find(text)?.groupValues?.get(2)?.toIntOrNull() ?: 60
        return CaloriesEstimate(kcal, conf.coerceIn(0, 100), text.take(600))
    }

    private fun String.json(): String = "\"${this.replace("\"", "\\\"").replace("\n", "\\n")}\""

    private fun String.substringBeforeAny(vararg chars: Char): String {
        val idx = this.indexOfFirst { c -> chars.contains(c) }
        return if (idx >= 0) this.substring(0, idx) else this
    }

    private fun Bitmap.toJpegBytes(quality: Int = 80): ByteArray =
        ByteArrayOutputStream().use { bos ->
            compress(Bitmap.CompressFormat.JPEG, quality, bos)
            bos.toByteArray()
        }

    private fun ByteArray.b64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    // -------- Provider Selection & Fallback Logic --------

    companion object {
        // Global request throttling - max 2 concurrent OpenAI API calls
        private val requestSemaphore = Semaphore(2)
        
        /**
         * Returns OpenAI as the only provider
         */
        fun selectOptimalProvider(): AiProvider {
            return AiProvider.OpenAI
        }

        /**
         * No fallback chain needed since only OpenAI is available
         */
        fun getFallbackChain(): List<AiProvider> {
            return emptyList()
        }
    }
}

enum class TaskType {
    TRAINING_PLAN,
    CALORIE_ESTIMATION,
    RECIPE_GENERATION,
    SHOPPING_LIST_PARSING
}