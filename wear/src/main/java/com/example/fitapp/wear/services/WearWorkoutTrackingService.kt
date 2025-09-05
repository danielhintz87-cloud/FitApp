package com.example.fitapp.wear.services

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Wear OS service for tracking workout data including heart rate
 */
class WearWorkoutTrackingService : Service(), SensorEventListener {
    
    companion object {
        private const val TAG = "WearWorkoutService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "workout_tracking"
    }
    
    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var dataSyncService: WearDataSyncService? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _heartRateFlow = MutableStateFlow(0)
    val heartRateFlow: StateFlow<Int> = _heartRateFlow.asStateFlow()
    
    private var lastHeartRateUpdate = 0L
    private val heartRateUpdateInterval = 5000L // 5 seconds
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WearWorkoutTrackingService created")
        
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        
        dataSyncService = WearDataSyncService().apply {
            initialize(this@WearWorkoutTrackingService)
        }
        
        startHeartRateMonitoring()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WearWorkoutTrackingService started")
        startForegroundService()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FitApp Workout Tracking")
            .setContentText("Tracking workout data on your watch")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun startHeartRateMonitoring() {
        heartRateSensor?.let { sensor ->
            val registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (registered) {
                Log.d(TAG, "Heart rate sensor registered successfully")
            } else {
                Log.w(TAG, "Failed to register heart rate sensor")
            }
        } ?: Log.w(TAG, "Heart rate sensor not available")
        
        // Monitor heart rate updates and sync with phone
        serviceScope.launch {
            heartRateFlow.collect { heartRate ->
                val currentTime = System.currentTimeMillis()
                if (heartRate > 0 && currentTime - lastHeartRateUpdate >= heartRateUpdateInterval) {
                    dataSyncService?.sendHeartRateUpdate(heartRate)
                    lastHeartRateUpdate = currentTime
                    Log.d(TAG, "Heart rate sent to phone: $heartRate BPM")
                }
            }
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()
            if (heartRate > 0) {
                _heartRateFlow.value = heartRate
                Log.d(TAG, "Heart rate detected: $heartRate BPM")
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WearWorkoutTrackingService destroyed")
        
        sensorManager.unregisterListener(this)
        dataSyncService?.cleanup()
        serviceScope.cancel()
    }
}