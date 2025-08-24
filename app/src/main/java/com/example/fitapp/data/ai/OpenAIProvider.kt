package com.example.fitapp.data.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class OpenAIProvider(ctx: Context) : AiProvider {
    override val id = "openai"
    private val apiKey = KeyLoader.get(ctx, "OPENAI_API_KEY") ?: ""
    private val client = OkHttpClient()
    private val media = "application/json; charset=utf-8".toMediaType()

    override suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) return@withContext "Kein OPENAI_API_KEY gefunden."
        val body = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", "Du bist mein persönlicher Fitness- und Ernährungscoach. Antworte knapp, strukturiert, auf Deutsch."))
                put(JSONObject().put("role", "user").put("content", prompt))
            })
            put("temperature", 0.6)
            put("max_tokens", 600)
        }
        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(RequestBody.create(media, body.toString()))
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return@withContext "HTTP ${'$'}{res.code}"
            val txt = res.body?.string().orEmpty()
            val j = JSONObject(txt)
            j.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content")
        }
    }

    private fun String.toMediaType() = MediaType.parse(this)!!
}
