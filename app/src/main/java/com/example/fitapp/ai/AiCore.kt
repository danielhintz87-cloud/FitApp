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

enum class AiProvider { Gemini, Perplexity }

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

    // -------- Intelligent Task Routing --------
    
    /**
     * Routes task to optimal AI provider based on task characteristics
     */
    private fun selectProviderForTask(task: TaskType, hasImage: Boolean = false): AiProvider {
        return when {
            // Multimodal tasks → Gemini
            hasImage -> AiProvider.Gemini
            // Structured fitness plans → Gemini  
            task == TaskType.TRAINING_PLAN -> AiProvider.Gemini
            // Quick Q&A and web search → Perplexity
            task == TaskType.SHOPPING_LIST_PARSING -> AiProvider.Perplexity
            task == TaskType.RECIPE_GENERATION -> AiProvider.Perplexity
            // Default to Gemini for complex tasks
            else -> AiProvider.Gemini
        }
    }

    suspend fun generatePlan(req: PlanRequest): Result<String> {
        val provider = selectProviderForTask(TaskType.TRAINING_PLAN)
        return callText(provider,
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
    }

    suspend fun generateRecipes(req: RecipeRequest): Result<String> {
        val provider = selectProviderForTask(TaskType.RECIPE_GENERATION)
        return callText(provider,
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
    }

    suspend fun parseShoppingList(spokenText: String): Result<String> {
        val provider = selectProviderForTask(TaskType.SHOPPING_LIST_PARSING)
        return callText(provider,
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
    }

    suspend fun estimateCaloriesFromPhoto(bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate> {
        val provider = selectProviderForTask(TaskType.CALORIE_ESTIMATION, hasImage = true)
        return withContext(Dispatchers.IO) {
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
            val r = when (provider) {
                AiProvider.Gemini -> geminiVision(prompt, bitmap)
                AiProvider.Perplexity -> Result.failure(IllegalStateException("Perplexity unterstützt keine Bildanalyse. Verwende Gemini für multimodale Aufgaben."))
            }
            val took = System.currentTimeMillis() - started
            r.onSuccess {
                logDao.insert(AiLog.success("vision_calories", provider.name, prompt + " $note", it.toString(), took))
            }.onFailure {
                logDao.insert(AiLog.error("vision_calories", provider.name, prompt + " $note", it.message ?: "unknown", took))
            }
            r
        }
    }

    suspend fun callText(provider: AiProvider, prompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            val started = System.currentTimeMillis()
            val result = when (provider) {
                AiProvider.Gemini -> geminiChat(prompt)
                AiProvider.Perplexity -> perplexityChat(prompt)
            }
            val took = System.currentTimeMillis() - started
            result.onSuccess {
                logDao.insert(AiLog.success("text", provider.name, prompt, it, took))
            }.onFailure {
                logDao.insert(AiLog.error("text", provider.name, prompt, it.message ?: "unknown", took))
            }
            result
        }

    // -------- Gemini API --------

    private suspend fun geminiChat(prompt: String): Result<String> = runCatching {
        withRetry {
            val apiKey = ApiKeys.getGeminiKey(context)
            if (apiKey.isBlank()) {
                throw IllegalStateException("Gemini API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
            }
            
            val body = """
                {
                    "contents": [{
                        "parts": [{"text": ${prompt.json()}}]
                    }],
                    "generationConfig": {
                        "temperature": 0.4,
                        "maxOutputTokens": 4096
                    }
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())

            val req = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$apiKey")
                .header("User-Agent", "fitapp/1.0")
                .post(body)
                .build()

            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val bodyStr = resp.body?.string().orEmpty()
                    val errorMsg = when (resp.code) {
                        400 -> "Gemini 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        401 -> "Gemini 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        403 -> "Gemini 403: Zugriff verweigert oder API-Limit erreicht. Prüfen Sie Ihr Google Cloud Konto."
                        429 -> "Gemini 429: Zu viele Anfragen. Bitte warten Sie ein paar Sekunden und versuchen Sie es erneut."
                        in 500..599 -> "Gemini ${resp.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                        else -> "Gemini ${resp.code}: ${bodyStr.take(200)}"
                    }
                    error(errorMsg)
                }
                
                val txt = resp.body!!.string()
                // Parse Gemini response JSON
                val jsonObj = JSONObject(txt)
                val candidates = jsonObj.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val content = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    content
                } else {
                    "Keine Antwort von Gemini erhalten"
                }
            }
        }
    }

    private suspend fun geminiVision(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> = runCatching {
        withRetry {
            val apiKey = ApiKeys.getGeminiKey(context)
            if (apiKey.isBlank()) {
                throw IllegalStateException("Gemini API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
            }
            
            val optimizedBitmap = optimizeBitmapForVision(bitmap)
            val imageData = optimizedBitmap.toJpegBytes(80).b64()
            
            val body = """
                {
                    "contents": [{
                        "parts": [
                            {"text": ${prompt.json()}},
                            {
                                "inline_data": {
                                    "mime_type": "image/jpeg",
                                    "data": "$imageData"
                                }
                            }
                        ]
                    }],
                    "generationConfig": {
                        "temperature": 0.3,
                        "maxOutputTokens": 2048
                    }
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())

            val req = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$apiKey")
                .header("User-Agent", "fitapp/1.0")
                .post(body)
                .build()

            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val bodyStr = resp.body?.string().orEmpty()
                    val errorMsg = when (resp.code) {
                        400 -> "Gemini 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        401 -> "Gemini 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        403 -> "Gemini 403: Zugriff verweigert oder API-Limit erreicht. Prüfen Sie Ihr Google Cloud Konto."
                        429 -> "Gemini 429: Zu viele Anfragen. Bitte warten Sie ein paar Sekunden und versuchen Sie es erneut."
                        in 500..599 -> "Gemini ${resp.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                        else -> "Gemini ${resp.code}: ${bodyStr.take(200)}"
                    }
                    error(errorMsg)
                }
                
                val txt = resp.body!!.string()
                // Parse Gemini response JSON
                val jsonObj = JSONObject(txt)
                val candidates = jsonObj.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val content = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    parseCalories(content)
                } else {
                    throw IllegalStateException("Keine Antwort von Gemini erhalten")
                }
            }
        }
    }

    // -------- Perplexity API --------

    private suspend fun perplexityChat(prompt: String): Result<String> = runCatching {
        withRetry {
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

            val req = Request.Builder()
                .url("https://api.perplexity.ai/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("User-Agent", "fitapp/1.0")
                .post(body)
                .build()

            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val bodyStr = resp.body?.string().orEmpty()
                    val errorMsg = when (resp.code) {
                        400 -> "Perplexity 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        401 -> "Perplexity 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        402 -> "Perplexity 402: Guthaben aufgebraucht oder Zahlung erforderlich. Bitte prüfen Sie Ihr Perplexity-Konto."
                        403 -> "Perplexity 403: Zugriff verweigert. Möglicherweise ist Ihr API-Schlüssel nicht für diesen Service berechtigt."
                        429 -> "Perplexity 429: Zu viele Anfragen. Bitte warten Sie ein paar Sekunden und versuchen Sie es erneut."
                        in 500..599 -> "Perplexity ${resp.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                        else -> "Perplexity ${resp.code}: ${bodyStr.take(200)}"
                    }
                    error(errorMsg)
                }
                
                val txt = resp.body!!.string()
                // Parse OpenAI-compatible response from Perplexity
                val contentStart = txt.indexOf("\"content\":\"") + 11
                val contentEnd = txt.indexOf("\"", contentStart)
                val content = if (contentStart > 10 && contentEnd > contentStart) {
                    txt.substring(contentStart, contentEnd).replace("\\\"", "\"").replace("\\n", "\n")
                } else "Error parsing response"
                content
            }
        }
    }

    // -------- Retry Logic --------

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

    private suspend fun <T> withRetry(request: suspend () -> T): T {
        var lastException: Exception? = null
        
        for (attempt in 0..3) {
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

                // Extract status code if available
                val lastStatusCode = when {
                    e.message?.contains(" 429") == true || e.message?.contains("429") == true -> 429
                    e.message?.contains(" 500") == true || e.message?.contains("500") == true -> 500
                    e.message?.contains(" 502") == true || e.message?.contains("502") == true -> 502
                    e.message?.contains(" 503") == true || e.message?.contains("503") == true -> 503
                    e.message?.contains(" 504") == true || e.message?.contains("504") == true -> 504
                    else -> null
                }

                if (attempt < 3 && isRetriableError(lastStatusCode, e)) {
                    val delayMs = calculateBackoffDelay(attempt)
                    delay(delayMs)
                } else {
                    throw e
                }
            }
        }
        throw lastException ?: IllegalStateException("Max retries exceeded")
    }

    // -------- Helpers --------

    /**
     * Optimize bitmap for Vision API to reduce token usage
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

    private fun Bitmap.toJpegBytes(quality: Int = 80): ByteArray =
        ByteArrayOutputStream().use { bos ->
            compress(Bitmap.CompressFormat.JPEG, quality, bos)
            bos.toByteArray()
        }

    private fun ByteArray.b64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    // -------- Usage Tracking --------
    
    companion object {
        // Global request throttling - max 2 concurrent API calls per provider
        private val requestSemaphore = Semaphore(4)
        
        /**
         * Get fallback provider for a given provider when it fails
         */
        fun getFallbackProvider(primary: AiProvider): AiProvider? {
            return when (primary) {
                AiProvider.Gemini -> AiProvider.Perplexity
                AiProvider.Perplexity -> AiProvider.Gemini
            }
        }
    }
}

enum class TaskType {
    TRAINING_PLAN,
    CALORIE_ESTIMATION,
    RECIPE_GENERATION,
    SHOPPING_LIST_PARSING
}