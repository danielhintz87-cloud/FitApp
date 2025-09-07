package com.example.fitapp.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import com.example.fitapp.util.StructuredLogger
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Voice input manager for shopping list and other voice commands
 * Uses SpeechRecognizer API with Coroutine CallbackFlow
 */
class VoiceInputManager(private val context: Context) {
    
    companion object {
        private const val TAG = "VoiceInputManager"
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _lastResult = MutableStateFlow<VoiceInputResult?>(null)
    val lastResult: StateFlow<VoiceInputResult?> = _lastResult.asStateFlow()
    
    /**
     * Check if speech recognition is available
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    /**
     * Initialize voice input system
     */
    suspend fun initialize(): Boolean = suspendCoroutine { continuation ->
        try {
            if (!isAvailable()) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.SYSTEM,
                    TAG,
                    "Speech recognition not available on this device"
                )
                continuation.resume(false)
                return@suspendCoroutine
            }
            
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Voice input system initialized"
            )
            continuation.resume(true)
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Failed to initialize voice input",
                exception = e
            )
            continuation.resume(false)
        }
    }
    
    /**
     * Start listening for shopping list items
     */
    fun startListeningForShoppingItems(): Flow<VoiceInputResult> = callbackFlow {
        if (speechRecognizer == null) {
            close(IllegalStateException("Voice input not initialized"))
            return@callbackFlow
        }
        
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                StructuredLogger.debug(StructuredLogger.LogCategory.SYSTEM, TAG, "Ready for speech input")
            }
            
            override fun onBeginningOfSpeech() {
                StructuredLogger.debug(StructuredLogger.LogCategory.SYSTEM, TAG, "Speech input started")
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Audio level indicator (could be used for UI feedback)
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Not used for current implementation
            }
            
            override fun onEndOfSpeech() {
                _isListening.value = false
                StructuredLogger.debug(StructuredLogger.LogCategory.SYSTEM, TAG, "Speech input ended")
            }
            
            override fun onError(error: Int) {
                _isListening.value = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error: $error"
                }
                
                val result = VoiceInputResult.Error(errorMessage)
                _lastResult.value = result
                trySend(result)
                
                StructuredLogger.error(
                    StructuredLogger.LogCategory.USER_ACTION,
                    TAG,
                    "Speech recognition error: $errorMessage"
                )
            }
            
            override fun onResults(results: Bundle?) {
                _isListening.value = false
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidence = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    val bestMatch = matches[0]
                    val confidenceScore = confidence?.getOrNull(0) ?: 0.5f
                    
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.USER_ACTION,
                        TAG,
                        "Voice input recognized: '$bestMatch' (confidence: $confidenceScore)"
                    )
                    
                    val shoppingItems = parseShoppingListInput(bestMatch)
                    val result = VoiceInputResult.Success(bestMatch, shoppingItems, confidenceScore)
                    _lastResult.value = result
                    trySend(result)
                } else {
                    val result = VoiceInputResult.Error("No speech recognized")
                    _lastResult.value = result
                    trySend(result)
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val partial = matches[0]
                    val result = VoiceInputResult.Partial(partial)
                    trySend(result)
                    
                    StructuredLogger.debug(StructuredLogger.LogCategory.SYSTEM, TAG, "Partial result: '$partial'")
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Not used for current implementation
            }
        }
        
        speechRecognizer?.setRecognitionListener(recognitionListener)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Sagen Sie Ihre Einkaufsliste...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            val result = VoiceInputResult.Error("Failed to start listening: ${e.message}")
            _lastResult.value = result
            trySend(result)
        }
        
        awaitClose {
            speechRecognizer?.stopListening()
            _isListening.value = false
        }
    }
    
    /**
     * Parse voice input into shopping list items
     */
    private fun parseShoppingListInput(input: String): List<VoiceShoppingItem> {
        val items = mutableListOf<VoiceShoppingItem>()
        
        // Common separators in German speech
        val separators = listOf(" und ", " sowie ", ", ", " plus ", " außerdem ")
        var text = input.lowercase()
        
        // Split by separators
        for (separator in separators) {
            text = text.replace(separator, "|")
        }
        
        val parts = text.split("|").map { it.trim() }
        
        for (part in parts) {
            if (part.isBlank()) continue
            
            val item = parseIndividualItem(part)
            if (item != null) {
                items.add(item)
            }
        }
        
        // Fallback: if no items parsed, treat whole input as single item
        if (items.isEmpty() && input.isNotBlank()) {
            items.add(VoiceShoppingItem(
                name = input.trim(),
                quantity = "1",
                unit = "Stück",
                confidence = 0.7f
            ))
        }
        
        return items
    }
    
    /**
     * Parse individual shopping item with quantity and unit detection
     */
    private fun parseIndividualItem(text: String): VoiceShoppingItem? {
        if (text.isBlank()) return null
        
        // Common patterns for quantities and units in German
        val quantityPatterns = listOf(
            Regex("""(\d+(?:[.,]\d+)?)\s*(kg|kilogramm|kilo|gramm|g|liter|l|ml|stück|stk|packung|pack|dose|dosen|flasche|flaschen)\s*(.+)"""),
            Regex("""(\d+(?:[.,]\d+)?)\s*(.+)"""),
            Regex("""(ein|eine|einer|zwei|drei|vier|fünf|sechs|sieben|acht|neun|zehn)\s*(.+)""")
        )
        
        for (pattern in quantityPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val quantity = match.groupValues[1]
                val unit = if (match.groupValues.size > 3) match.groupValues[2] else "Stück"
                val name = if (match.groupValues.size > 3) match.groupValues[3] else match.groupValues[2]
                
                return VoiceShoppingItem(
                    name = name.trim(),
                    quantity = normalizeQuantity(quantity),
                    unit = normalizeUnit(unit),
                    confidence = 0.8f
                )
            }
        }
        
        // No quantity pattern matched, treat as simple item
        return VoiceShoppingItem(
            name = text.trim(),
            quantity = "1",
            unit = "Stück",
            confidence = 0.6f
        )
    }
    
    /**
     * Normalize spoken quantities to numbers
     */
    private fun normalizeQuantity(quantity: String): String {
        return when (quantity.lowercase()) {
            "ein", "eine", "einer" -> "1"
            "zwei" -> "2"
            "drei" -> "3"
            "vier" -> "4"
            "fünf" -> "5"
            "sechs" -> "6"
            "sieben" -> "7"
            "acht" -> "8"
            "neun" -> "9"
            "zehn" -> "10"
            else -> quantity.replace(",", ".")
        }
    }
    
    /**
     * Normalize spoken units
     */
    private fun normalizeUnit(unit: String): String {
        return when (unit.lowercase()) {
            "kg", "kilogramm", "kilo" -> "kg"
            "gramm", "g" -> "g"
            "liter", "l" -> "l"
            "ml" -> "ml"
            "stück", "stk" -> "Stück"
            "packung", "pack" -> "Packung"
            "dose", "dosen" -> "Dose"
            "flasche", "flaschen" -> "Flasche"
            else -> unit
        }
    }
    
    /**
     * Stop listening
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }
    
    /**
     * Release resources
     */
    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isListening.value = false
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Voice input resources released"
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Failed to release voice input resources",
                exception = e
            )
        }
    }
}

/**
 * Voice input result sealed class
 */
sealed class VoiceInputResult {
    data class Success(
        val originalText: String,
        val shoppingItems: List<VoiceShoppingItem>,
        val confidence: Float
    ) : VoiceInputResult()
    
    data class Partial(val partialText: String) : VoiceInputResult()
    data class Error(val message: String) : VoiceInputResult()
}

/**
 * Parsed shopping item from voice input
 */
data class VoiceShoppingItem(
    val name: String,
    val quantity: String,
    val unit: String,
    val confidence: Float
)
