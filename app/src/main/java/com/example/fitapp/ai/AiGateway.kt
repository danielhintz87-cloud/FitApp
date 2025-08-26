package com.example.fitapp.ai

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.annotation.WorkerThread
import com.example.fitapp.BuildConfig
import com.example.fitapp.data.prefs.ApiKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID

// Simple recipe model for UI
data class UiRecipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val markdown: String,
    val calories: Int? = null,
    val imageUrl: String? = null
)

data class CalorieEstimate(
    val kcal: Int,
    val confidence: String,
    val details: String
)

object AiGateway {
    enum class Provider { OPENAI, GEMINI, DEEPSEEK }

    private val http = OkHttpClient()

    suspend fun generateRecipes(context: Context, prompt: String, provider: Provider = Provider.OPENAI): List<UiRecipe> =
        withContext(Dispatchers.IO) {
            val enhancedPrompt = "Erstelle 10 **nutritionsoptimierte Rezepte** als präzise Markdown-Liste für: $prompt\n\n" +
                "**Anforderungen pro Rezept:**\n" +
                "- Titel (## Format)\n" +
                "- Zutatenliste mit exakten Gramm-Angaben (nicht 'eine Tasse' sondern '150g')\n" +
                "- Schritt-für-Schritt Zubereitung (nummeriert)\n" +
                "- **Präzise Nährwerte:** Kalorien, Protein, Kohlenhydrate, Fett (jeweils in g), Ballaststoffe\n" +
                "- Portionsgröße und Anzahl Portionen\n" +
                "- Zubereitungszeit & Schwierigkeitsgrad\n\n" +
                "**Kalkulationsbasis:** Verwende USDA-Nährwertdatenbank-Standards für genaue Berechnungen."
            
            val text = when (provider) {
                Provider.OPENAI -> openAiChat(
                    context,
                    system = "Du bist ein erfahrener Koch und zertifizierter Ernährungscoach. Antworte IMMER als präzises Markdown mit exakten Nährwertangaben.",
                    user = enhancedPrompt
                )
                Provider.GEMINI -> geminiText(
                    context,
                    system = "Du bist ein erfahrener Koch und zertifizierter Ernährungscoach.",
                    user = enhancedPrompt
                )
                Provider.DEEPSEEK -> deepseekChat(
                    context,
                    system = "Du bist ein erfahrener Koch und zertifizierter Ernährungscoach.",
                    user = enhancedPrompt
                )
            }
            parseMarkdownRecipes(text)
        }

    suspend fun analyzeFoodImage(context: Context, imageUri: Uri, provider: Provider = Provider.OPENAI): CalorieEstimate =
        withContext(Dispatchers.IO) {
            val base64 = loadBitmapBase64(context.contentResolver, imageUri)
            analyzeFoodBase64(context, base64, provider)
        }

    suspend fun analyzeFoodBitmap(context: Context, bitmap: Bitmap, provider: Provider = Provider.OPENAI): CalorieEstimate =
        withContext(Dispatchers.IO) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            analyzeFoodBase64(context, base64, provider)
        }

    private suspend fun analyzeFoodBase64(context: Context, base64: String, provider: Provider): CalorieEstimate {
        val enhancedPrompt = "Analysiere das Bild und schätze präzise die Kalorien des gezeigten Essens.\n\n" +
            "**Analyseschritte:**\n" +
            "1. Identifiziere alle sichtbaren Lebensmittel/Getränke\n" +
            "2. Schätze Portionsgrößen anhand von Referenzobjekten (Teller ≈ 25cm, Gabel ≈ 20cm, Hand ≈ 18cm)\n" +
            "3. Berücksichtige Zubereitungsart (frittiert +30%, gedämpft -20%)\n" +
            "4. Kalkuliere Gesamtkalorien mit USDA-Nährwertstandards\n\n" +
            "**Antwortformat:**\n" +
            "Schätzung: <Zahl> kcal\n" +
            "Begründung: [Lebensmittel] ca. [Gramm]g = [kcal]kcal, [weitere Komponenten]\n" +
            "Unsicherheitsfaktoren: [versteckte Fette, Portionsgröße, etc.]"
        
        val text = when (provider) {
            Provider.OPENAI -> {
                val content = JSONArray().apply {
                    put(
                        JSONObject(
                            mapOf(
                                "type" to "text",
                                "text" to enhancedPrompt
                            )
                        )
                    )
                    put(
                        JSONObject(
                            mapOf(
                                "type" to "image_url",
                                "image_url" to JSONObject(mapOf("url" to "data:image/jpeg;base64,$base64"))
                            )
                        )
                    )
                }
                val raw = openAiChatRaw(context, listOf(JSONObject(mapOf("role" to "user", "content" to content))))
                extractContentText(raw)
            }
            Provider.GEMINI -> {
                val raw = geminiVision(context, base64, enhancedPrompt)
                extractContentText(raw)
            }
            Provider.DEEPSEEK -> deepseekChat(
                context,
                system = "Du bist ein erfahrener Ernährungscoach.",
                user = "Schätze grob die Kalorien des Essens auf einem Foto. Hinweis: DeepSeek unterstützt eventuell keine Bildanalyse, schätze daher basierend auf allgemeinem Wissen.",
            )
        }
        val kcal = Regex("""(\d{2,5})\s*kcal""", RegexOption.IGNORE_CASE)
            .find(text)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull() ?: 0
        val confidence = when {
            "hoch" in text.lowercase() || "sicher" in text.lowercase() -> "hoch"
            "mittel" in text.lowercase() -> "mittel"
            "niedrig" in text.lowercase() || "unsicher" in text.lowercase() -> "niedrig"
            else -> "mittel"
        }
        return CalorieEstimate(kcal, confidence, text)
    }

    // --- OpenAI ---

    private fun openAiKey(context: Context) = ApiKeys.getOpenAiKey(context)
    private fun openAiModel() = BuildConfig.OPENAI_MODEL.ifBlank { "gpt-4o-mini" }

    @WorkerThread
    private fun openAiChat(context: Context, system: String, user: String): String {
        val apiKey = openAiKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenAI API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val messages = JSONArray()
            .put(JSONObject(mapOf("role" to "system", "content" to system)))
            .put(JSONObject(mapOf("role" to "user", "content" to user)))
        val body = JSONObject(mapOf("model" to openAiModel(), "messages" to messages, "temperature" to 0.7)).toString()
        val req = Request.Builder()
            .url("${BuildConfig.OPENAI_BASE_URL}/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val msg = resp.body?.string()
                if (resp.code == 401) throw IllegalStateException("OpenAI 401: API-Schlüssel ungültig oder fehlt")
                else throw IllegalStateException("OpenAI HTTP ${resp.code}: $msg")
            }
            val json = JSONObject(resp.body!!.string())
            return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
        }
    }

    @WorkerThread
    private fun openAiChatRaw(context: Context, messages: List<JSONObject>): JSONObject {
        val apiKey = openAiKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenAI API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val body = JSONObject(mapOf("model" to openAiModel(), "messages" to JSONArray(messages), "temperature" to 0.2)).toString()
        val req = Request.Builder()
            .url("${BuildConfig.OPENAI_BASE_URL}/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val msg = resp.body?.string()
                if (resp.code == 401) throw IllegalStateException("OpenAI 401: API-Schlüssel ungültig oder fehlt")
                else throw IllegalStateException("OpenAI HTTP ${resp.code}: $msg")
            }
            return JSONObject(resp.body!!.string())
        }
    }

    // --- Gemini ---

    private fun geminiKey(context: Context) = ApiKeys.getGeminiKey(context)

    @WorkerThread
    private fun geminiText(context: Context, system: String, user: String): String {
        val apiKey = geminiKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val model = BuildConfig.GEMINI_MODEL.ifBlank { "gemini-1.5-pro" }
        val body = JSONObject(mapOf("contents" to JSONArray().put(JSONObject(mapOf("parts" to JSONArray().put(JSONObject(mapOf("text" to "$system\n\n$user")))))))).toString()
        val req = Request.Builder()
            .url("${BuildConfig.GEMINI_BASE_URL}/v1beta/models/$model:generateContent?key=$apiKey")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (resp.code == 401) throw IllegalStateException("Gemini 401: API-Schlüssel ungültig oder fehlt")
                else throw IllegalStateException("Gemini HTTP ${resp.code}: ${resp.body?.string()}")
            }
            val json = JSONObject(resp.body!!.string())
            return json.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
        }
    }

    @WorkerThread
    private fun geminiVision(context: Context, base64: String, prompt: String): JSONObject {
        val apiKey = geminiKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val model = BuildConfig.GEMINI_MODEL.ifBlank { "gemini-1.5-pro" }
        val body = JSONObject(mapOf(
            "contents" to JSONArray().put(
                JSONObject(mapOf(
                    "parts" to JSONArray()
                        .put(JSONObject(mapOf("text" to prompt)))
                        .put(JSONObject(mapOf(
                            "inline_data" to JSONObject(mapOf(
                                "mime_type" to "image/jpeg",
                                "data" to base64
                            ))
                        )))
                ))
            )
        )).toString()
        
        val req = Request.Builder()
            .url("${BuildConfig.GEMINI_BASE_URL}/v1beta/models/$model:generateContent?key=$apiKey")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (resp.code == 401) throw IllegalStateException("Gemini 401: API-Schlüssel ungültig oder fehlt")
                else throw IllegalStateException("Gemini HTTP ${resp.code}: ${resp.body?.string()}")
            }
            return JSONObject(resp.body!!.string())
        }
    }

    // --- DeepSeek ---

    private fun deepSeekKey(context: Context) = ApiKeys.getDeepSeekKey(context)
    private fun deepSeekModel() = BuildConfig.DEEPSEEK_MODEL.ifBlank { "deepseek-chat" }

    private fun getNormalizedDeepSeekBaseUrl(): String {
        val base = BuildConfig.DEEPSEEK_BASE_URL.trim()
        return if (base.endsWith("/")) base.dropLast(1) else base
    }

    @WorkerThread
    private fun deepseekChat(context: Context, system: String, user: String): String {
        val apiKey = deepSeekKey(context)
        if (apiKey.isBlank()) {
            throw IllegalStateException("DeepSeek API-Schlüssel nicht konfiguriert. Bitte unter Einstellungen → API-Schlüssel eingeben.")
        }
        
        val messages = JSONArray()
            .put(JSONObject(mapOf("role" to "system", "content" to system)))
            .put(JSONObject(mapOf("role" to "user", "content" to user)))
        val body = JSONObject(mapOf("model" to deepSeekModel(), "messages" to messages)).toString()
        val req = Request.Builder()
            .url("${getNormalizedDeepSeekBaseUrl()}/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (resp.code == 401) throw IllegalStateException("DeepSeek 401: API-Schlüssel ungültig oder fehlt")
                else throw IllegalStateException("DeepSeek HTTP ${resp.code}: ${resp.body?.string()}")
            }
            val json = JSONObject(resp.body!!.string())
            return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
        }
    }

    // --- helpers ---

    private fun extractContentText(json: JSONObject): String {
        return when {
            json.has("choices") -> json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            json.has("candidates") -> json.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            else -> ""
        }
    }

    private fun loadBitmapBase64(cr: ContentResolver, uri: Uri): String {
        val bmp: Bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(cr, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(cr, uri)
        }
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun parseMarkdownRecipes(markdown: String): List<UiRecipe> {
        val blocks = markdown.split("\n## ").mapIndexed { idx, block ->
            if (idx == 0 && block.startsWith("## ")) block.removePrefix("## ") else block
        }.filter { it.isNotBlank() }
        return blocks.map { raw ->
            val title = raw.lineSequence().firstOrNull()?.trim() ?: "Rezept"
            val kcal = Regex("""Kalorien:\s*(\d{2,5})\s*kcal""", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)?.toIntOrNull()
            UiRecipe(title = title, markdown = "## $raw".trim(), calories = kcal)
        }
    }

}