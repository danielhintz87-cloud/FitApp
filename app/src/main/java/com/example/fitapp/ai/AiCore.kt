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

enum class AiProvider { OpenAI, Gemini, DeepSeek }

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

class AiCore(private val logDao: AiLogDao, private val context: Context) {

    private val http = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generatePlan(provider: AiProvider, req: PlanRequest): Result<String> =
        callText(provider,
            "Erzeuge einen **12-Wochen-Trainingsplan** in Markdown. " +
            "Ziel: ${req.goal}. Wochen: ${req.weeks}. Einheiten/Woche: ${req.sessionsPerWeek}. Minuten/Einheit: ${req.minutesPerSession}. " +
            "Geräte: ${req.equipment.joinToString()}. " +
            "Struktur: Für jede Woche Überschrift (H2), Tage mit Übungen (Sätze/Wdh/Tempo/RPE), Progression, optional Deload in Woche 4/8/12. " +
            "Kompakt, gut lesbar."
        )

    suspend fun generateRecipes(provider: AiProvider, req: RecipeRequest): Result<String> =
        callText(provider,
            "Erzeuge ${req.count} **Rezepte** als Markdown-Liste. Pro Rezept: Titel (H2), Zutaten mit Mengen, Schritte, Nährwerte (kcal, Protein, KH, Fett). " +
            "Bevorzugungen: ${req.preferences}. Diät: ${req.diet}. Ausgabe kompakt."
        )

    suspend fun estimateCaloriesFromPhoto(provider: AiProvider, bitmap: Bitmap, note: String = ""): Result<CaloriesEstimate> =
        withContext(Dispatchers.IO) {
            val prompt = "Schätze Kalorien des gezeigten Essens. Antworte knapp: 'kcal: <Zahl>', 'confidence: <0-100>' und 1 Satz."
            val started = System.currentTimeMillis()
            val r = when (provider) {
                AiProvider.OpenAI -> openAiVision(prompt, bitmap)
                AiProvider.Gemini -> geminiVision(prompt, bitmap)
                AiProvider.DeepSeek -> deepseekVision(prompt, bitmap)
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

    private suspend fun deepseekVision(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> {
        // Fallback: viele Modelle sind text-first. Wir liefern eine Textschätzung mit Hinweis.
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
}