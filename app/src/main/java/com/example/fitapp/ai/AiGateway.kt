package com.example.fitapp.ai

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import com.example.fitapp.BuildConfig
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID

// Simple data classes representing AI outputs

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
    enum class Provider { OPENAI, GEMINI }

    private val http = OkHttpClient()

    suspend fun generateRecipes(prompt: String, provider: Provider = Provider.OPENAI): List<UiRecipe> {
        val text = when (provider) {
            Provider.OPENAI -> openAiChat(
                "Du bist ein erfahrener Koch. Gib 10 Rezepte im Markdown-Format aus. Jeder Abschnitt beginnt mit '## Titel'.",
                prompt
            )
            Provider.GEMINI -> geminiText("Du bist ein erfahrener Koch.", "Erzeuge 10 Rezepte als Markdown: $prompt")
        }
        return parseMarkdownRecipes(text)
    }

    suspend fun analyzeFoodImage(context: Context, imageUri: Uri, provider: Provider = Provider.OPENAI): CalorieEstimate {
        val base64 = loadBitmapBase64(context.contentResolver, imageUri)
        val raw = when (provider) {
            Provider.OPENAI -> {
                val content = JSONArray().apply {
                    put(JSONObject(mapOf("type" to "text", "text" to "Schätze Kalorien des Essens")))
                    put(JSONObject(mapOf("type" to "image_url", "image_url" to JSONObject(mapOf("url" to "data:image/jpeg;base64,$base64")))))
                }
                openAiChatRaw(listOf(JSONObject(mapOf("role" to "user", "content" to content))))
            }
            Provider.GEMINI -> geminiVision(base64, "Schätze Kalorien des Essens")
        }
        val text = extractContentText(raw)
        val kcal = Regex("""(\d{2,5})\s*kcal""", RegexOption.IGNORE_CASE).find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val conf = when {
            "hoch" in text.lowercase() -> "hoch"
            "niedrig" in text.lowercase() -> "niedrig"
            else -> "mittel"
        }
        return CalorieEstimate(kcal, conf, text)
    }

    // ---- OpenAI ----

    private fun openAiKey(): String = BuildConfig.OPENAI_API_KEY
    private fun openAiModel(): String = "gpt-4o-mini"

    private fun openAiChat(system: String, user: String): String {
        val messages = JSONArray()
            .put(JSONObject(mapOf("role" to "system", "content" to system)))
            .put(JSONObject(mapOf("role" to "user", "content" to user)))
        val body = JSONObject(mapOf("model" to openAiModel(), "messages" to messages, "temperature" to 0.7)).toString()
        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
            .post(RequestBody.create(MediaType.parse("application/json"), body))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("OpenAI HTTP ${resp.code()}")
            val json = JSONObject(resp.body()!!.string())
            return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
        }
    }

    private fun openAiChatRaw(messages: List<JSONObject>): JSONObject {
        val body = JSONObject(mapOf("model" to openAiModel(), "messages" to JSONArray(messages))).toString()
        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
            .post(RequestBody.create(MediaType.parse("application/json"), body))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("OpenAI HTTP ${resp.code()}")
            return JSONObject(resp.body()!!.string())
        }
    }

    // ---- Gemini ----

    private fun geminiText(system: String, user: String): String {
        val model = "gemini-1.5-flash-latest"
        val body = JSONObject(
            mapOf(
                "contents" to JSONArray().put(
                    JSONObject(
                        mapOf("parts" to JSONArray().put(JSONObject(mapOf("text" to "$system\n\n$user"))))
                    )
                )
            )
        ).toString()
        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
            .post(RequestBody.create(MediaType.parse("application/json"), body))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Gemini HTTP ${resp.code()}")
            val json = JSONObject(resp.body()!!.string())
            return json.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
        }
    }

    private fun geminiVision(base64: String, prompt: String): JSONObject {
        val model = "gemini-1.5-flash-latest"
        val body = JSONObject(
            mapOf(
                "contents" to JSONArray().put(
                    JSONObject(mapOf(
                        "parts" to JSONArray()
                            .put(JSONObject(mapOf("text" to prompt)))
                            .put(JSONObject(mapOf("inline_data" to JSONObject(mapOf("mime_type" to "image/jpeg", "data" to base64)))))
                    ))
                )
            )
        ).toString()
        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
            .post(RequestBody.create(MediaType.parse("application/json"), body))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Gemini HTTP ${resp.code()}")
            return JSONObject(resp.body()!!.string())
        }
    }

    // ---- Helpers ----

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
