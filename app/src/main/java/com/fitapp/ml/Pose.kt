package com.fitapp.ml

import android.graphics.RectF

data class Keypoint(
    val name: String,
    val x: Float,      // Pixel-Koordinate im Originalbild
    val y: Float,      // Pixel-Koordinate im Originalbild
    val score: Float
)

data class Pose(
    val keypoints: List<Keypoint>,
    val boundingBox: RectF,
    val score: Float
)

// MoveNet Thunder spezifische Keypoint-Namen (17 COCO Keypoints)
object MoveNetSpec {
    val KEYPOINT_NAMES = listOf(
        "nose",
        "left_eye", "right_eye",
        "left_ear", "right_ear", 
        "left_shoulder", "right_shoulder",
        "left_elbow", "right_elbow",
        "left_wrist", "right_wrist",
        "left_hip", "right_hip",
        "left_knee", "right_knee", 
        "left_ankle", "right_ankle"
    )
    
    const val NUM_KEYPOINTS = 17
    const val INPUT_SIZE = 256
    const val OUTPUT_SIZE = NUM_KEYPOINTS * 3 // x, y, confidence für jeden Keypoint
}

// BlazePose spezifische Keypoint-Namen (33 Landmarks) 
object BlazePoseSpec {
    val KEYPOINT_NAMES = listOf(
        "nose",                    // 0
        "left_eye_inner",          // 1
        "left_eye",                // 2
        "left_eye_outer",          // 3
        "right_eye_inner",         // 4
        "right_eye",               // 5
        "right_eye_outer",         // 6
        "left_ear",                // 7
        "right_ear",               // 8
        "mouth_left",              // 9
        "mouth_right",             // 10
        "left_shoulder",           // 11
        "right_shoulder",          // 12
        "left_elbow",              // 13
        "right_elbow",             // 14
        "left_wrist",              // 15
        "right_wrist",             // 16
        "left_pinky",              // 17
        "right_pinky",             // 18
        "left_index",              // 19
        "right_index",             // 20
        "left_thumb",              // 21
        "right_thumb",             // 22
        "left_hip",                // 23
        "right_hip",               // 24
        "left_knee",               // 25
        "right_knee",              // 26
        "left_ankle",              // 27
        "right_ankle",             // 28
        "left_heel",               // 29
        "right_heel",              // 30
        "left_foot_index",         // 31
        "right_foot_index"         // 32
    )
    
    const val NUM_KEYPOINTS = 33
    const val INPUT_SIZE = 256
}