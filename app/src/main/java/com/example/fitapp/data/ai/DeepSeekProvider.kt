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

class DeepSeekProvider(ctx: Context) : AiProvider {
    override val id = "deepseek"
    private val apiKey = KeyLoader.get(ctx, "DEEPSEEK_API_KEY") ?: ""
    private val client = OkHttpClient()
    private val media = "application/json; charset=utf-8".toMediaType()
    private val url = "https://api.deepseek.com/chat/completions"

    override suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) return@withContext "Kein DEEPSEEK_API_KEY gefunden."
        val body = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        }
        val req = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(RequestBody.create(media, body.toString()))
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return@withContext "HTTP ${'$'}{res.code}"
            val j = JSONObject(res.body?.string().orEmpty())
            j.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content")
        }
    }

    private fun String.toMediaType() = MediaType.parse(this)!!
}
