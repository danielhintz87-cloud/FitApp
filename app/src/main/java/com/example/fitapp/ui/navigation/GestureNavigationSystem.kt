package com.example.fitapp.ui.navigation

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Enhanced Gesture Navigation System for FitApp
 * Provides intuitive gesture controls with haptic feedback
 */

/**
 * Gesture-enabled container that detects swipes, long presses, and custom gestures
 */
@Composable
fun GestureNavigationContainer(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
    onCustomGesture: ((GestureType) -> Unit)? = null,
    enableHapticFeedback: Boolean = true,
    swipeThreshold: Float = 100f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hapticManager = remember { HapticFeedbackManager(context) }
    val gestureRecognizer = remember { CustomGestureRecognizer() }
    
    var isLongPressing by remember { mutableStateOf(false) }
    var gestureStartPosition by remember { mutableStateOf(Offset.Zero) }
    val swipeThresholdPx = with(density) { swipeThreshold.dp.toPx() }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        gestureStartPosition = offset
                        gestureRecognizer.startGesture(offset)
                        if (enableHapticFeedback) {
                            hapticManager.lightTap()
                        }
                    },
                    onDrag = { change, _ ->
                        gestureRecognizer.addPoint(change.position)
                    },
                    onDragEnd = {
                        val gesture = gestureRecognizer.endGesture()
                        gesture?.let { recognizedGesture ->
                            onCustomGesture?.invoke(recognizedGesture)
                            if (enableHapticFeedback) {
                                hapticManager.gestureCompleted(recognizedGesture)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val currentPosition = gestureRecognizer.lastPosition
                        val deltaX = currentPosition.x - gestureStartPosition.x
                        
                        when {
                            deltaX > swipeThresholdPx -> {
                                onSwipeRight?.invoke()
                                if (enableHapticFeedback) hapticManager.swipeSuccess()
                            }
                            deltaX < -swipeThresholdPx -> {
                                onSwipeLeft?.invoke()
                                if (enableHapticFeedback) hapticManager.swipeSuccess()
                            }
                        }
                    }
                ) { _, _ -> }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        val currentPosition = gestureRecognizer.lastPosition
                        val deltaY = currentPosition.y - gestureStartPosition.y
                        
                        when {
                            deltaY > swipeThresholdPx -> {
                                onSwipeDown?.invoke()
                                if (enableHapticFeedback) hapticManager.swipeSuccess()
                            }
                            deltaY < -swipeThresholdPx -> {
                                onSwipeUp?.invoke()
                                if (enableHapticFeedback) hapticManager.swipeSuccess()
                            }
                        }
                    }
                ) { _, _ -> }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { _ ->
                        isLongPressing = true
                        onLongPress?.invoke()
                        if (enableHapticFeedback) {
                            hapticManager.longPressActivated()
                        }
                    },
                    onDoubleTap = { _ ->
                        onDoubleTap?.invoke()
                        if (enableHapticFeedback) {
                            hapticManager.doubleTapSuccess()
                        }
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Haptic Feedback Manager for different interaction types
 */
class HapticFeedbackManager(private val context: Context) {
    
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    /**
     * Light tap feedback for gesture start
     */
    fun lightTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }
    
    /**
     * Success feedback for completed swipes
     */
    fun swipeSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, 80))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(25)
        }
    }
    
    /**
     * Long press activation feedback
     */
    fun longPressActivated() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 50, 100, 50)
            val amplitudes = intArrayOf(0, 120, 0, 120)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            val pattern = longArrayOf(0, 50, 100, 50)
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Double tap success feedback
     */
    fun doubleTapSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 30, 50, 30)
            val amplitudes = intArrayOf(0, 100, 0, 100)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            val pattern = longArrayOf(0, 30, 50, 30)
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Custom gesture completion feedback
     */
    fun gestureCompleted(gesture: GestureType) {
        val vibrationPattern = when (gesture) {
            GestureType.CIRCLE -> HapticPattern.CIRCLE
            GestureType.STAR -> HapticPattern.STAR
            GestureType.TRIANGLE -> HapticPattern.TRIANGLE
            GestureType.WAVE -> HapticPattern.WAVE
            GestureType.CHECK -> HapticPattern.CHECK
            GestureType.X_MARK -> HapticPattern.X_MARK
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern.pattern, vibrationPattern.amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationPattern.pattern, -1)
        }
    }
    
    /**
     * Error/warning feedback
     */
    fun errorFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 100, 50, 100, 50, 100)
            val amplitudes = intArrayOf(0, 150, 0, 150, 0, 150)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            val pattern = longArrayOf(0, 100, 50, 100, 50, 100)
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Success confirmation feedback
     */
    fun successConfirmation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, 120))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
}

/**
 * Custom gesture recognizer for detecting complex patterns
 */
class CustomGestureRecognizer {
    private val gesturePoints = mutableListOf<Offset>()
    private var startTime = 0L
    var lastPosition = Offset.Zero
        private set
    
    fun startGesture(position: Offset) {
        gesturePoints.clear()
        gesturePoints.add(position)
        startTime = System.currentTimeMillis()
        lastPosition = position
    }
    
    fun addPoint(position: Offset) {
        gesturePoints.add(position)
        lastPosition = position
    }
    
    fun endGesture(): GestureType? {
        if (gesturePoints.size < 3) return null
        
        val duration = System.currentTimeMillis() - startTime
        if (duration > 3000) return null // Too long for a gesture
        
        return recognizePattern()
    }
    
    private fun recognizePattern(): GestureType? {
        return when {
            isCircularGesture() -> GestureType.CIRCLE
            isStarGesture() -> GestureType.STAR
            isTriangleGesture() -> GestureType.TRIANGLE
            isWaveGesture() -> GestureType.WAVE
            isCheckGesture() -> GestureType.CHECK
            isXMarkGesture() -> GestureType.X_MARK
            else -> null
        }
    }
    
    private fun isCircularGesture(): Boolean {
        if (gesturePoints.size < 10) return false
        
        val center = calculateCentroid()
        val distances = gesturePoints.map { point ->
            sqrt((point.x - center.x).pow(2) + (point.y - center.y).pow(2))
        }
        
        val avgDistance = distances.average()
        val variance = distances.map { (it - avgDistance).pow(2) }.average()
        val standardDeviation = sqrt(variance)
        
        // Check if most points are roughly equidistant from center
        val tolerance = avgDistance * 0.3
        val pointsInRange = distances.count { abs(it - avgDistance) <= tolerance }
        
        return pointsInRange >= gesturePoints.size * 0.7 && avgDistance > 50
    }
    
    private fun isStarGesture(): Boolean {
        // Simplified star detection - look for 5 direction changes
        val directions = calculateDirectionChanges()
        return directions.size >= 8 && directions.size <= 12
    }
    
    private fun isTriangleGesture(): Boolean {
        // Look for 3 main direction changes forming roughly equal angles
        val corners = findCorners()
        return corners.size == 3 && isClosedShape()
    }
    
    private fun isWaveGesture(): Boolean {
        // Look for alternating up-down pattern
        val yValues = gesturePoints.map { it.y }
        var peaks = 0
        var valleys = 0
        
        for (i in 1 until yValues.size - 1) {
            if (yValues[i] > yValues[i-1] && yValues[i] > yValues[i+1]) peaks++
            if (yValues[i] < yValues[i-1] && yValues[i] < yValues[i+1]) valleys++
        }
        
        return peaks >= 2 && valleys >= 2
    }
    
    private fun isCheckGesture(): Boolean {
        // Look for L-shaped movement (down-right then up-right)
        if (gesturePoints.size < 6) return false
        
        val midPoint = gesturePoints.size / 2
        val firstHalf = gesturePoints.subList(0, midPoint)
        val secondHalf = gesturePoints.subList(midPoint, gesturePoints.size)
        
        val firstDirection = calculateGeneralDirection(firstHalf)
        val secondDirection = calculateGeneralDirection(secondHalf)
        
        return firstDirection.y > 0 && firstDirection.x > 0 && // Down-right
               secondDirection.y < 0 && secondDirection.x > 0   // Up-right
    }
    
    private fun isXMarkGesture(): Boolean {
        // Look for two intersecting diagonal lines
        val totalDistance = calculateTotalDistance()
        val directDistance = gesturePoints.first().let { start ->
            gesturePoints.last().let { end ->
                sqrt((end.x - start.x).pow(2) + (end.y - start.y).pow(2))
            }
        }
        
        // X pattern has high total distance relative to direct distance
        return totalDistance / directDistance > 2.5
    }
    
    private fun calculateCentroid(): Offset {
        val sumX = gesturePoints.sumOf { it.x.toDouble() }.toFloat()
        val sumY = gesturePoints.sumOf { it.y.toDouble() }.toFloat()
        return Offset(sumX / gesturePoints.size, sumY / gesturePoints.size)
    }
    
    private fun calculateDirectionChanges(): List<Float> {
        val directions = mutableListOf<Float>()
        
        for (i in 1 until gesturePoints.size) {
            val prev = gesturePoints[i-1]
            val curr = gesturePoints[i]
            val angle = atan2(curr.y - prev.y, curr.x - prev.x)
            directions.add(angle)
        }
        
        return directions
    }
    
    private fun findCorners(): List<Offset> {
        val corners = mutableListOf<Offset>()
        val threshold = 0.8 // Angle threshold for corner detection
        
        for (i in 1 until gesturePoints.size - 1) {
            val prev = gesturePoints[i-1]
            val curr = gesturePoints[i]
            val next = gesturePoints[i+1]
            
            val angle1 = atan2(curr.y - prev.y, curr.x - prev.x)
            val angle2 = atan2(next.y - curr.y, next.x - curr.x)
            val angleDiff = abs(angle2 - angle1)
            
            if (angleDiff > threshold && angleDiff < PI - threshold) {
                corners.add(curr)
            }
        }
        
        return corners
    }
    
    private fun isClosedShape(): Boolean {
        val start = gesturePoints.first()
        val end = gesturePoints.last()
        val distance = sqrt((end.x - start.x).pow(2) + (end.y - start.y).pow(2))
        return distance < 100 // Close enough to be considered closed
    }
    
    private fun calculateGeneralDirection(points: List<Offset>): Offset {
        if (points.size < 2) return Offset.Zero
        
        val start = points.first()
        val end = points.last()
        return Offset(end.x - start.x, end.y - start.y)
    }
    
    private fun calculateTotalDistance(): Float {
        var totalDistance = 0f
        
        for (i in 1 until gesturePoints.size) {
            val prev = gesturePoints[i-1]
            val curr = gesturePoints[i]
            totalDistance += sqrt((curr.x - prev.x).pow(2) + (curr.y - prev.y).pow(2))
        }
        
        return totalDistance
    }
}

/**
 * Types of gestures that can be recognized
 */
enum class GestureType {
    CIRCLE,     // Navigate back/cancel
    STAR,       // Favorite/bookmark
    TRIANGLE,   // Warning/attention
    WAVE,       // Skip/next
    CHECK,      // Complete/confirm
    X_MARK      // Delete/cancel
}

/**
 * Haptic patterns for different gestures
 */
enum class HapticPattern(
    val pattern: LongArray,
    val amplitudes: IntArray
) {
    CIRCLE(
        longArrayOf(0, 40, 20, 40, 20, 40, 20, 40),
        intArrayOf(0, 80, 0, 80, 0, 80, 0, 80)
    ),
    STAR(
        longArrayOf(0, 20, 10, 20, 10, 20, 10, 20, 10, 20),
        intArrayOf(0, 100, 0, 100, 0, 100, 0, 100, 0, 100)
    ),
    TRIANGLE(
        longArrayOf(0, 60, 30, 60, 30, 60),
        intArrayOf(0, 90, 0, 90, 0, 90)
    ),
    WAVE(
        longArrayOf(0, 30, 15, 30, 15, 30, 15, 30),
        intArrayOf(0, 70, 0, 100, 0, 70, 0, 100)
    ),
    CHECK(
        longArrayOf(0, 40, 20, 80),
        intArrayOf(0, 80, 0, 120)
    ),
    X_MARK(
        longArrayOf(0, 100, 50, 100),
        intArrayOf(0, 150, 0, 150)
    )
}

/**
 * Pre-defined gesture actions for common use cases
 */
object GestureActions {
    
    /**
     * Workout-specific gesture mappings
     */
    val workoutGestures = mapOf(
        GestureType.CHECK to "complete_exercise",
        GestureType.X_MARK to "skip_exercise", 
        GestureType.CIRCLE to "go_back",
        GestureType.STAR to "favorite_exercise",
        GestureType.WAVE to "next_exercise",
        GestureType.TRIANGLE to "emergency_stop"
    )
    
    /**
     * Navigation gesture mappings
     */
    val navigationGestures = mapOf(
        GestureType.CIRCLE to "back",
        GestureType.WAVE to "forward",
        GestureType.STAR to "bookmark",
        GestureType.CHECK to "confirm",
        GestureType.X_MARK to "cancel",
        GestureType.TRIANGLE to "menu"
    )
    
    /**
     * Quick action gesture mappings
     */
    val quickActionGestures = mapOf(
        GestureType.CHECK to "log_weight",
        GestureType.STAR to "start_workout",
        GestureType.WAVE to "add_water",
        GestureType.CIRCLE to "food_scan",
        GestureType.TRIANGLE to "view_progress",
        GestureType.X_MARK to "dismiss"
    )
}

/**
 * Composable for gesture training/customization
 */
@Composable
fun GestureTrainingScreen(
    onGestureRecorded: (GestureType, List<Offset>) -> Unit,
    onTrainingComplete: () -> Unit
) {
    var currentGesture by remember { mutableStateOf<GestureType?>(null) }
    var trainingProgress by remember { mutableStateOf(0) }
    val gestureTypes = GestureType.values()
    val hapticManager = HapticFeedbackManager(LocalContext.current)
    
    LaunchedEffect(trainingProgress) {
        if (trainingProgress >= gestureTypes.size) {
            onTrainingComplete()
        } else {
            currentGesture = gestureTypes[trainingProgress]
        }
    }
    
    currentGesture?.let { gesture ->
        GestureNavigationContainer(
            onCustomGesture = { recognizedGesture ->
                if (recognizedGesture == gesture) {
                    hapticManager.successConfirmation()
                    trainingProgress++
                } else {
                    hapticManager.errorFeedback()
                }
            },
            enableHapticFeedback = true
        ) {
            // Training UI content would go here
        }
    }
}