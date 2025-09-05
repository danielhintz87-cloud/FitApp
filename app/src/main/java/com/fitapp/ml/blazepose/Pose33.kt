package com.fitapp.ml.blazepose

data class Landmark3D(
    val x: Float,     // normalisiert [0,1] relativ zur Input-Frame-Breite
    val y: Float,     // normalisiert [0,1] relativ zur Input-Frame-Höhe
    val z: Float,     // z (Meter-skaliert relativ zur Schulterbreite, kann variieren)
    val visibility: Float // 0..1
)

object BlazePoseSpec {
    // 33 Landmarks (MediaPipe)
    val NAMES = listOf(
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
    
    const val COUNT = 33
    
    // Verbindungen für Skelett-Rendering
    val CONNECTIONS = listOf(
        // Gesicht
        0 to 1, 1 to 2, 2 to 3,  // left eye
        0 to 4, 4 to 5, 5 to 6,  // right eye
        9 to 10,                  // mouth
        
        // Torso
        11 to 12,                 // shoulders
        11 to 23, 12 to 24,       // shoulder to hip
        23 to 24,                 // hips
        
        // Arms
        11 to 13, 13 to 15,       // left arm
        12 to 14, 14 to 16,       // right arm
        
        // Hands
        15 to 17, 15 to 19, 15 to 21,  // left hand
        16 to 18, 16 to 20, 16 to 22,  // right hand
        17 to 19, 19 to 21,            // left hand fingers
        18 to 20, 20 to 22,            // right hand fingers
        
        // Legs
        23 to 25, 25 to 27,       // left leg
        24 to 26, 26 to 28,       // right leg
        
        // Feet
        27 to 29, 27 to 31,       // left foot
        28 to 30, 28 to 32,       // right foot
        29 to 31, 30 to 32        // foot connections
    )
}

data class BlazePoseResult(
    val landmarks: List<Landmark3D>,
    val isDetected: Boolean,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun empty() = BlazePoseResult(
            landmarks = emptyList(),
            isDetected = false,
            confidence = 0f
        )
    }
}