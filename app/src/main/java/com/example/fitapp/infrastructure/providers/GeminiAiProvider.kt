package com.example.fitapp.infrastructure.providers

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.fitapp.data.prefs.ApiKeys
import com.example.fitapp.domain.entities.CaloriesEstimate
import com.example.fitapp.util.ApiCallWrapper
import com.example.fitapp.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
/**
 * Gemini AI provider implementation
 */
class GeminiAiProvider(
    private val context: Context,
    private val httpClient: OkHttpClient
) : AiProvider {
    
    override val providerType = com.example.fitapp.domain.entities.AiProvider.Gemini
    
    override suspend fun isAvailable(): Boolean {
        return ApiKeys.getGeminiKey(context).isNotBlank()
    }
    
    override fun supportsVision(): Boolean = true
    
    override suspend fun generateText(prompt: String): Result<String> = 
        safeApiCall(context, "Gemini Text Generation") {
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

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$apiKey")
                .header("User-Agent", "fitapp/1.0")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            response.use {
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string().orEmpty()
                    val errorMsg = when (response.code) {
                        400 -> "Gemini 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        401 -> "Gemini 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        403 -> "Gemini 403: Zugriff verweigert oder API-Limit erreicht. Prüfen Sie Ihr Google Cloud Konto."
                        429 -> "Gemini 429: Zu viele Anfragen. Bitte warten Sie ein paar Sekunden und versuchen Sie es erneut."
                        in 500..599 -> "Gemini ${response.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                        else -> "Gemini ${response.code}: ${bodyStr.take(200)}"
                    }
                    throw IllegalStateException(errorMsg)
                }
                
                val responseBodyResult = ApiCallWrapper.safeReadResponseBody(response)
                if (responseBodyResult.isFailure) {
                    throw responseBodyResult.exceptionOrNull() ?: IllegalStateException("Failed to read response")
                }
                
                val txt = responseBodyResult.getOrThrow()
                val jsonObj = JSONObject(txt)
                val candidates = jsonObj.optJSONArray("candidates")
                    ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - candidates fehlt")
                
                if (candidates.length() > 0) {
                    val candidate = candidates.optJSONObject(0)
                        ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - candidate fehlt")
                    
                    val content = candidate.optJSONObject("content")
                        ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - content fehlt")
                    
                    val parts = content.optJSONArray("parts")
                        ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - parts fehlt")
                    
                    val part = parts.optJSONObject(0)
                        ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - part fehlt")
                    
                    val text = part.optString("text")
                    if (text.isNullOrBlank()) {
                        throw IllegalStateException("Gemini: Kein Text in der Antwort gefunden")
                    }
                    text
                } else {
                    throw IllegalStateException("Keine Antwort von Gemini erhalten")
                }
            }
        }
    
    override suspend fun analyzeImage(prompt: String, bitmap: Bitmap): Result<CaloriesEstimate> = withContext(Dispatchers.IO) {
        runCatching {
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

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$apiKey")
                .header("User-Agent", "fitapp/1.0")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string().orEmpty()
                    val errorMsg = when (response.code) {
                        400 -> "Gemini 400: API-Schlüssel ungültig oder Anfrage fehlerhaft. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        401 -> "Gemini 401: API-Schlüssel ungültig oder fehlt. Bitte unter Einstellungen → API-Schlüssel prüfen."
                        403 -> "Gemini 403: Zugriff verweigert oder API-Limit erreicht. Prüfen Sie Ihr Google Cloud Konto."
                        429 -> "Gemini 429: Zu viele Anfragen. Bitte warten Sie ein paar Sekunden und versuchen Sie es erneut."
                        in 500..599 -> "Gemini ${response.code}: Server-Fehler. Bitte versuchen Sie es später erneut."
                        else -> "Gemini ${response.code}: ${bodyStr.take(200)}"
                    }
                    throw IllegalStateException(errorMsg)
                }
                
                val responseBody = response.body
                if (responseBody == null) {
                    throw IllegalStateException("Gemini: Leere Antwort vom Server erhalten")
                }
                
                val txt = responseBody.string()
                if (txt.isBlank()) {
                    throw IllegalStateException("Gemini: Leere Antwort vom Server erhalten")
                }
                
                val jsonObj = JSONObject(txt)
                val candidates = jsonObj.optJSONArray("candidates")
                    ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - candidates fehlt")
                
                if (candidates.length() > 0) {
                    val candidate = candidates.optJSONObject(0)
                        ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - candidate fehlt")
                    
                    val content = candidate.optJSONObject("content")
                        ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - content fehlt")
                    
                    val parts = content.optJSONArray("parts")
                        ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - parts fehlt")
                    
                    val part = parts.optJSONObject(0)
                        ?: throw IllegalStateException("Gemini: Ungültige Antwortstruktur - part fehlt")
                    
                    val text = part.optString("text")
                    if (text.isNullOrBlank()) {
                        throw IllegalStateException("Gemini: Kein Text in der Antwort gefunden")
                    }
                    val content = text
                    parseCalories(content)
                } else {
                    throw IllegalStateException("Keine Antwort von Gemini erhalten")
                }
            }
        }
    }
    
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
}