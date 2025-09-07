package com.example.fitapp.services

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.*
import com.example.fitapp.util.StructuredLogger
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Audio player for rest timer coaching and countdown
 * Uses SoundPool for short sound effects and TTS for voice coaching
 */
class RestTimerAudioPlayer(private val context: Context) {
    
    companion object {
        private const val TAG = "RestTimerAudioPlayer"
        private const val MAX_STREAMS = 3
    }
    
    private var soundPool: SoundPool? = null
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    private val soundIds = mutableMapOf<String, Int>()
    
    /**
     * Initialize audio systems
     */
    suspend fun initialize(): Boolean = suspendCoroutine { continuation ->
        try {
            // Initialize SoundPool for sound effects
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
                
            soundPool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build()
            
            // Initialize Text-to-Speech
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.GERMAN
                    isInitialized = true
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYSTEM,
                        TAG,
                        "Audio systems initialized successfully"
                    )
                    continuation.resume(true)
                } else {
                    StructuredLogger.error(
                        StructuredLogger.LogCategory.SYSTEM,
                        TAG,
                        "TTS initialization failed with status: $status"
                    )
                    continuation.resume(false)
                }
            }
            
            // Load sound effects (if available in assets)
            loadSoundEffects()
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Failed to initialize audio systems",
                exception = e
            )
            continuation.resume(false)
        }
    }
    
    /**
     * Load sound effects from assets
     */
    private fun loadSoundEffects() {
        try {
            // Try to load sound files from assets if they exist
            // For now, we'll rely on TTS for all audio cues
            StructuredLogger.debug(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Sound effects loading completed (using TTS fallback)"
            )
        } catch (e: Exception) {
            StructuredLogger.debug(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "No sound assets found, using TTS only"
            )
        }
    }
    
    /**
     * Play audio cue based on type
     */
    fun playAudioCue(cueType: String) {
        if (!isInitialized) {
            StructuredLogger.debug(TAG, "Audio not initialized, skipping cue: $cueType")
            return
        }
        
        val message = when (cueType) {
            "rest_started" -> "Pause gestartet. Erhol dich gut!"
            "thirty_seconds_left" -> "Noch 30 Sekunden Pause"
            "ten_seconds_left" -> "Noch 10 Sekunden"
            "countdown_3" -> "3"
            "countdown_2" -> "2" 
            "countdown_1" -> "1"
            "rest_complete" -> "Pause beendet! Bereit für den nächsten Satz?"
            "rest_extended" -> "Pause verlängert"
            "rest_skipped" -> "Pause übersprungen"
            else -> null
        }
        
        message?.let { playTTSMessage(it) }
    }
    
    /**
     * Play coaching message
     */
    fun playCoachingMessage(message: String) {
        if (!isInitialized) return
        playTTSMessage(message)
    }
    
    /**
     * Play text-to-speech message
     */
    private fun playTTSMessage(message: String) {
        try {
            tts?.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "rest_timer_${System.currentTimeMillis()}"
            )
            
            StructuredLogger.debug(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Playing TTS: $message"
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.USER_ACTION,
                TAG,
                "Failed to play TTS message: $message",
                exception = e
            )
        }
    }
    
    /**
     * Set TTS speech rate (0.5 to 2.0)
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }
    
    /**
     * Set TTS pitch (0.5 to 2.0)
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }
    
    /**
     * Check if TTS is speaking
     */
    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }
    
    /**
     * Stop all audio playback
     */
    fun stopAudio() {
        try {
            tts?.stop()
            soundPool?.autoPause()
        } catch (e: Exception) {
            StructuredLogger.error(TAG, "Failed to stop audio", exception = e)
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        try {
            tts?.shutdown()
            tts = null
            
            soundPool?.release()
            soundPool = null
            
            isInitialized = false
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Audio resources released"
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Failed to release audio resources",
                exception = e
            )
        }
    }
}
