package com.example.fitapp.ai

import android.util.Log

/**
 * Parser utilities for MoveNet style model outputs.
 * Expected tensor shape: [1,1,17,3] with (y,x,score).
 */
internal fun parseMoveNetOutput(
    output: Array<Array<Array<FloatArray>>>,
    confidenceThreshold: Float
): List<Keypoint> = try {
    val names = listOf(
        "nose", "left_eye", "right_eye", "left_ear", "right_ear",
        "left_shoulder", "right_shoulder", "left_elbow", "right_elbow",
        "left_wrist", "right_wrist", "left_hip", "right_hip",
        "left_knee", "right_knee", "left_ankle", "right_ankle"
    )
    output[0][0].mapIndexed { idx, arr ->
        val y = arr[0].coerceIn(0f, 1f)
        val x = arr[1].coerceIn(0f, 1f)
        val score = arr[2]
        Keypoint(name = names[idx], x = x, y = y, confidence = score)
    }.filter { it.confidence >= confidenceThreshold }
} catch (e: Exception) {
    Log.w("MoveNetOutputParser", "Parsing failed", e)
    emptyList()
}
