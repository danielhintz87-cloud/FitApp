package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.fitapp.BuildConfig
import com.example.fitapp.data.db.AiLog
import com.example.fitapp.data.db.AiLogDao
import com.example.fitapp.data.prefs.ApiKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

enum class AiProvider { OpenAI, Gemini, DeepSeek, Claude }

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
            val r = when (provider) {
                AiProvider.OpenAI -> openAiVision(prompt, bitmap)
                AiProvider.Gemini -> geminiVision(prompt, bitmap)
                AiProvider.DeepSeek -> deepseekVision(prompt, bitmap)
                AiProvider.Claude -> claudeVision(prompt, bitmap)
            }
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
            val result = when (provider) {
                AiProvider.OpenAI -> openAiChat(prompt)
                AiProvider.Gemini -> geminiText(prompt)
                AiProvider.DeepSeek -> deepseekText(prompt)
                AiProvider.Claude -> claudeChat(prompt)
            }
            val took = System.currentTimeMillis() - started
            result.onSuccess {
                logDao.insert(AiLog.success("text", provider.name, prompt, it, took))
            }.onFailure {
                logDao.insert(AiLog.error("text", provider.name, prompt, it.message ?: "unknown", took))
            }
            result
        }

    // -------- OpenAI --------

    private suspend fun openAiChat(prompt: String): Result<String> = runCatching {
        val apiKey = ApiKeys.getOpenAiKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenAI API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val model = BuildConfig.OPENAI_MODEL.ifBlank { "gpt-4o-mini" }
        val body = """
            {"model":"$model","messages":[{"role":"user","content":${prompt.json()}}],"temperature":0.4}
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("${BuildConfig.OPENAI_BASE_URL}/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (resp.code == 401) {
                    error("OpenAI 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen.")
                } else {
                    error("OpenAI ${resp.code}")
                }
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

    private suspend fun openAiVision(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> = runCatching {
        val apiKey = ApiKeys.getOpenAiKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenAI API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val model = BuildConfig.OPENAI_MODEL.ifBlank { "gpt-4o-mini" }
        val dataUrl = "data:image/jpeg;base64," + bitmap.toJpegBytes().b64()
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

        val req = Request.Builder()
            .url("${BuildConfig.OPENAI_BASE_URL}/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(body).build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (resp.code == 401) {
                    error("OpenAI 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen.")
                } else {
                    error("OpenAI ${resp.code}")
                }
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

    // -------- Gemini --------

    private suspend fun geminiText(prompt: String): Result<String> = runCatching {
        val apiKey = ApiKeys.getGeminiKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val model = BuildConfig.GEMINI_MODEL.ifBlank { "gemini-1.5-pro" }
        val body = """{"contents":[{"parts":[{"text":${prompt.json()}}]}]}"""
            .toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("${BuildConfig.GEMINI_BASE_URL}/v1beta/models/$model:generateContent?key=$apiKey")
            .post(body).build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (resp.code == 401) {
                    error("Gemini 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen.")
                } else {
                    error("Gemini ${resp.code}")
                }
            }
            val txt = resp.body!!.string()
            // Manual JSON parsing for Gemini
            val textStart = txt.indexOf("\"text\":\"") + 8
            val textEnd = txt.indexOf("\"", textStart)
            val content = if (textStart > 7 && textEnd > textStart) {
                txt.substring(textStart, textEnd).replace("\\\"", "\"").replace("\\n", "\n")
            } else "Error parsing response"
            content
        }
    }

    private suspend fun geminiVision(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> = runCatching {
        val apiKey = ApiKeys.getGeminiKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val model = BuildConfig.GEMINI_MODEL.ifBlank { "gemini-1.5-pro" }
        val b64 = bitmap.toJpegBytes().b64()
        val body = """
        {"contents":[{"parts":[
          {"text":${prompt.json()}},
          {"inline_data":{"mime_type":"image/jpeg","data":"$b64"}}
        ]}]}
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("${BuildConfig.GEMINI_BASE_URL}/v1beta/models/$model:generateContent?key=$apiKey")
            .post(body).build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (resp.code == 401) {
                    error("Gemini 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen.")
                } else {
                    error("Gemini ${resp.code}")
                }
            }
            val txt = resp.body!!.string()
            // Manual JSON parsing for Gemini vision
            val textStart = txt.indexOf("\"text\":\"") + 8
            val textEnd = txt.indexOf("\"", textStart)
            val text = if (textStart > 7 && textEnd > textStart) {
                txt.substring(textStart, textEnd).replace("\\\"", "\"").replace("\\n", "\n")
            } else "Error parsing response"
            parseCalories(text)
        }
    }

    // -------- DeepSeek --------

    private suspend fun deepseekText(prompt: String): Result<String> = runCatching {
        val apiKey = ApiKeys.getDeepSeekKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("DeepSeek API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val model = BuildConfig.DEEPSEEK_MODEL.ifBlank { "deepseek-chat" }
        val body = """
            {"model":"$model","messages":[{"role":"user","content":${prompt.json()}}]}
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("${BuildConfig.DEEPSEEK_BASE_URL}/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(body).build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (resp.code == 401) {
                    error("DeepSeek 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen.")
                } else {
                    error("DeepSeek ${resp.code}")
                }
            }
            val txt = resp.body!!.string()
            // Manual JSON parsing for DeepSeek
            val contentStart = txt.indexOf("\"content\":\"") + 11
            val contentEnd = txt.indexOf("\"", contentStart)
            val content = if (contentStart > 10 && contentEnd > contentStart) {
                txt.substring(contentStart, contentEnd).replace("\\\"", "\"").replace("\\n", "\n")
            } else "Error parsing response"
            content
        }
    }

    private suspend fun deepseekVision(prompt: String, @Suppress("UNUSED_PARAMETER") bitmap: Bitmap): Result<CaloriesEstimate> {
        // Fallback: DeepSeek doesn't support vision. We provide a text-based estimation with note.
        val text = deepseekText("$prompt (Hinweis: Falls Vision nicht unterstützt, grobe Schätzung anhand Standardportion)").getOrThrow()
        return runCatching { parseCalories(text) }
    }

    // -------- Helpers --------

    private fun parseCalories(text: String): CaloriesEstimate {
        val kcalRegex = Regex("(\\d{2,4})\\s*(k?cal|Kilokalorien)", RegexOption.IGNORE_CASE)
        val confRegex = Regex("(confidence|sicherheit)[^0-9]{0,8}(\\d{1,3})", RegexOption.IGNORE_CASE)
        val kcal = kcalRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val conf = confRegex.find(text)?.groupValues?.get(2)?.toIntOrNull() ?: 60
        return CaloriesEstimate(kcal, conf.coerceIn(0, 100), text.take(600))
    }

    private fun String.json(): String = "\"${this.replace("\"", "\\\"").replace("\n", "\\n")}\""

    private fun Bitmap.toJpegBytes(quality: Int = 90): ByteArray =
        ByteArrayOutputStream().use { bos ->
            compress(Bitmap.CompressFormat.JPEG, quality, bos)
            bos.toByteArray()
        }

    private fun ByteArray.b64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    // -------- Claude Implementation --------

    private suspend fun claudeChat(prompt: String): Result<String> = runCatching {
        val apiKey = ApiKeys.getClaudeKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("Claude API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }

        val model = BuildConfig.CLAUDE_MODEL.ifBlank { "claude-3-5-sonnet-20241022" }
        val body = """
            {
                "model": "$model",
                "max_tokens": 4000,
                "messages": [
                    {
                        "role": "user",
                        "content": ${prompt.json()}
                    }
                ]
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("${BuildConfig.CLAUDE_BASE_URL}/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(body)
            .build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val errorBody = resp.body?.string() ?: "Kein Fehlertext verfügbar"
                if (resp.code == 401) {
                    error("Claude 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen.")
                } else {
                    error("Claude ${resp.code}: $errorBody")
                }
            }
            val txt = resp.body!!.string()
            val json = JSONObject(txt)
            json.getJSONArray("content").getJSONObject(0).getString("text")
        }
    }

    private suspend fun claudeVision(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> = runCatching {
        val apiKey = ApiKeys.getClaudeKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("Claude API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }

        val model = BuildConfig.CLAUDE_MODEL.ifBlank { "claude-3-5-sonnet-20241022" }
        val b64 = bitmap.toJpegBytes().b64()
        val body = """
            {
                "model": "$model",
                "max_tokens": 4000,
                "messages": [
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "text",
                                "text": ${prompt.json()}
                            },
                            {
                                "type": "image",
                                "source": {
                                    "type": "base64",
                                    "media_type": "image/jpeg",
                                    "data": "$b64"
                                }
                            }
                        ]
                    }
                ]
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("${BuildConfig.CLAUDE_BASE_URL}/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(body)
            .build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val errorBody = resp.body?.string() ?: "Kein Fehlertext verfügbar"
                if (resp.code == 401) {
                    error("Claude 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen.")
                } else {
                    error("Claude ${resp.code}: $errorBody")
                }
            }
            val txt = resp.body!!.string()
            val json = JSONObject(txt)
            val content = json.getJSONArray("content").getJSONObject(0).getString("text")
            parseCalories(content)
        }
    }

    // -------- Provider Selection & Fallback Logic --------

    companion object {
        /**
         * Selects optimal provider based on task type
         */
        fun selectOptimalProvider(taskType: TaskType, availableProviders: List<AiProvider>): AiProvider {
            if (availableProviders.isEmpty()) return AiProvider.OpenAI
            
            return when (taskType) {
                TaskType.TRAINING_PLAN -> {
                    // Claude > GPT-4o > Gemini > DeepSeek for complex reasoning
                    availableProviders.firstOrNull { it == AiProvider.Claude }
                        ?: availableProviders.firstOrNull { it == AiProvider.OpenAI }
                        ?: availableProviders.firstOrNull { it == AiProvider.Gemini }
                        ?: availableProviders.first()
                }
                TaskType.CALORIE_ESTIMATION -> {
                    // GPT-4o Vision > Claude > Gemini > DeepSeek for vision tasks
                    availableProviders.firstOrNull { it == AiProvider.OpenAI }
                        ?: availableProviders.firstOrNull { it == AiProvider.Claude }
                        ?: availableProviders.firstOrNull { it == AiProvider.Gemini }
                        ?: availableProviders.first()
                }
                TaskType.RECIPE_GENERATION -> {
                    // Claude > GPT-4o > Gemini > DeepSeek for structured output
                    availableProviders.firstOrNull { it == AiProvider.Claude }
                        ?: availableProviders.firstOrNull { it == AiProvider.OpenAI }
                        ?: availableProviders.firstOrNull { it == AiProvider.Gemini }
                        ?: availableProviders.first()
                }
            }
        }

        /**
         * Provides fallback chain for when primary provider fails
         */
        fun getFallbackChain(primary: AiProvider): List<AiProvider> {
            return when (primary) {
                AiProvider.OpenAI -> listOf(AiProvider.Claude, AiProvider.Gemini, AiProvider.DeepSeek)
                AiProvider.Claude -> listOf(AiProvider.OpenAI, AiProvider.Gemini, AiProvider.DeepSeek)
                AiProvider.Gemini -> listOf(AiProvider.OpenAI, AiProvider.Claude, AiProvider.DeepSeek)
                AiProvider.DeepSeek -> listOf(AiProvider.OpenAI, AiProvider.Claude, AiProvider.Gemini)
            }
        }
    }
}

enum class TaskType {
    TRAINING_PLAN,
    CALORIE_ESTIMATION,
    RECIPE_GENERATION
}