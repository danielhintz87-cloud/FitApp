package com.example.fitapp.data.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class GeminiProvider(ctx: Context) : AiProvider {
    override val id = "gemini"
    private val apiKey = KeyLoader.get(ctx, "GEMINI_API_KEY") ?: ""
    private val client = OkHttpClient()
    private val media = "application/json; charset=utf-8".toMediaType()

    override suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) return@withContext "Kein GEMINI_API_KEY gefunden."
        val body = JSONObject().apply {
            put("contents", listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))))
        }
        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            .post(RequestBody.create(media, body.toString()))
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return@withContext "HTTP ${'$'}{res.code}"
            val txt = res.body?.string().orEmpty()
            val j = JSONObject(txt)
            val candidates = j.optJSONArray("candidates") ?: return@withContext "Keine Antwort."
            val first = candidates.getJSONObject(0)
            val parts = first.getJSONObject("content").getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        }
    }

}
