package com.example.fitapp.services

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.fitapp.util.StructuredLogger

/**
 * Voice Command System for hands-free workout navigation
 * Extends existing voice recognition functionality for workout execution
 */
class VoiceCommandManager(private val context: Context) {
    
    companion object {
        private const val TAG = "VoiceCommandManager"
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _lastCommand = MutableStateFlow<VoiceCommand?>(null)
    val lastCommand: StateFlow<VoiceCommand?> = _lastCommand.asStateFlow()
    
    private var commandCallback: ((VoiceCommand) -> Unit)? = null
    
    /**
     * Initialize voice recognition for workout commands
     */
    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            StructuredLogger.warning(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Speech recognition not available on this device"
            )
            return
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(voiceRecognitionListener)
        }
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.SYSTEM,
            TAG,
            "Voice command system initialized"
        )
    }
    
    /**
     * Start listening for voice commands
     */
    fun startListening(onCommand: (VoiceCommand) -> Unit) {
        commandCallback = onCommand
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Sage einen Befehl...")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.startListening(intent)
        _isListening.value = true
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.USER_ACTION,
            TAG,
            "Started voice command listening"
        )
    }
    
    /**
     * Stop listening for voice commands
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        commandCallback = null
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.USER_ACTION,
            TAG,
            "Stopped voice command listening"
        )
    }
    
    /**
     * Process voice input and extract commands
     */
    private fun processVoiceInput(spokenText: String): VoiceCommand? {
        val text = spokenText.lowercase().trim()
        
        return when {
            // Exercise control commands
            text.contains("fertig") || text.contains("komplett") || text.contains("beendet") -> 
                VoiceCommand.EXERCISE_COMPLETE
                
            text.contains("nächste") && text.contains("übung") -> 
                VoiceCommand.NEXT_EXERCISE
                
            text.contains("pause") || text.contains("pausiere") -> 
                VoiceCommand.PAUSE_WORKOUT
                
            text.contains("weiter") || text.contains("fortsetzen") -> 
                VoiceCommand.RESUME_WORKOUT
                
            text.contains("stop") || text.contains("beenden") -> 
                VoiceCommand.STOP_WORKOUT
                
            // Set logging commands
            text.contains("gewicht") -> extractWeightCommand(text)
            text.contains("wiederholung") || text.contains("reps") -> extractRepsCommand(text)
            
            // Emergency commands
            text.contains("hilfe") || text.contains("notfall") -> 
                VoiceCommand.EMERGENCY_HELP
                
            // Navigation commands
            text.contains("zurück") || text.contains("back") -> 
                VoiceCommand.GO_BACK
                
            text.contains("home") || text.contains("hauptmenü") -> 
                VoiceCommand.GO_HOME
                
            // Rest timer commands
            text.contains("timer") && text.contains("start") -> 
                VoiceCommand.START_REST_TIMER
                
            text.contains("timer") && text.contains("skip") -> 
                VoiceCommand.SKIP_REST
            
            else -> null
        }
    }
    
    /**
     * Extract weight value from voice command
     */
    private fun extractWeightCommand(text: String): VoiceCommand? {
        val weightRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:kg|kilo|kilogramm)?""")
        val match = weightRegex.find(text)
        
        return match?.let { 
            val weight = it.groupValues[1].toFloatOrNull()
            weight?.let { VoiceCommand.SET_WEIGHT(it) }
        }
    }
    
    /**
     * Extract repetition count from voice command
     */
    private fun extractRepsCommand(text: String): VoiceCommand? {
        val repsRegex = Regex("""(\d+)\s*(?:wiederholung|reps?|mal)""")
        val match = repsRegex.find(text)
        
        return match?.let {
            val reps = it.groupValues[1].toIntOrNull()
            reps?.let { VoiceCommand.SET_REPS(it) }
        }
    }
    
    /**
     * Voice recognition listener
     */
    private val voiceRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            StructuredLogger.debug(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Ready for speech input"
            )
        }
        
        override fun onBeginningOfSpeech() {
            StructuredLogger.debug(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Speech input started"
            )
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changed - could be used for UI feedback
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {
            // Audio buffer received
        }
        
        override fun onEndOfSpeech() {
            _isListening.value = false
            StructuredLogger.debug(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Speech input ended"
            )
        }
        
        override fun onError(error: Int) {
            _isListening.value = false
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech input recognized"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error: $error"
            }
            
            StructuredLogger.warning(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Voice recognition error: $errorMessage"
            )
        }
        
        override fun onResults(results: Bundle?) {
            _isListening.value = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            
            matches?.firstOrNull()?.let { spokenText ->
                StructuredLogger.info(
                    StructuredLogger.LogCategory.USER_ACTION,
                    TAG,
                    "Voice command recognized: $spokenText"
                )
                
                val command = processVoiceInput(spokenText)
                command?.let {
                    _lastCommand.value = it
                    commandCallback?.invoke(it)
                }
            }
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            // Handle partial results for real-time feedback
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { partialText ->
                StructuredLogger.debug(
                    StructuredLogger.LogCategory.USER_ACTION,
                    TAG,
                    "Partial voice input: $partialText"
                )
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {
            // Handle other speech events
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        commandCallback = null
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.SYSTEM,
            TAG,
            "Voice command system cleaned up"
        )
    }
}

/**
 * Voice commands for workout control
 */
sealed class VoiceCommand {
    // Exercise control
    object EXERCISE_COMPLETE : VoiceCommand()
    object NEXT_EXERCISE : VoiceCommand()
    object PAUSE_WORKOUT : VoiceCommand()
    object RESUME_WORKOUT : VoiceCommand()
    object STOP_WORKOUT : VoiceCommand()
    
    // Set logging
    data class SET_WEIGHT(val weight: Float) : VoiceCommand()
    data class SET_REPS(val reps: Int) : VoiceCommand()
    
    // Navigation
    object GO_BACK : VoiceCommand()
    object GO_HOME : VoiceCommand()
    
    // Rest timer
    object START_REST_TIMER : VoiceCommand()
    object SKIP_REST : VoiceCommand()
    
    // Emergency
    object EMERGENCY_HELP : VoiceCommand()
    
    fun getDisplayText(): String = when (this) {
        EXERCISE_COMPLETE -> "Übung beendet"
        NEXT_EXERCISE -> "Nächste Übung"
        PAUSE_WORKOUT -> "Training pausiert"
        RESUME_WORKOUT -> "Training fortgesetzt"
        STOP_WORKOUT -> "Training beendet"
        is SET_WEIGHT -> "Gewicht: ${weight}kg"
        is SET_REPS -> "Wiederholungen: $reps"
        GO_BACK -> "Zurück"
        GO_HOME -> "Hauptmenü"
        START_REST_TIMER -> "Timer gestartet"
        SKIP_REST -> "Pause übersprungen"
        EMERGENCY_HELP -> "Hilfe angefordert"
    }
}