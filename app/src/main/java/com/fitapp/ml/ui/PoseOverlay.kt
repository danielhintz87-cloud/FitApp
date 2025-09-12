package com.fitapp.ml.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.fitapp.ml.Pose
import com.fitapp.ml.blazepose.BlazePoseResult
import com.fitapp.ml.blazepose.BlazePoseSpec
import com.fitapp.ml.blazepose.Landmark3D

/**
 * Compose Canvas für MoveNet Pose Overlay
 */
@Composable
fun MoveNetPoseOverlay(
    pose: Pose?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        pose?.let { drawMoveNetPose(it, imageWidth, imageHeight) }
    }
}

/**
 * Compose Canvas für BlazePose Overlay
 */
@Composable
fun BlazePoseOverlay(
    blazePoseResult: BlazePoseResult,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (blazePoseResult.isDetected) {
            drawBlazePose(blazePoseResult.landmarks, imageWidth, imageHeight)
        }
    }
}

/**
 * Zeichnet MoveNet Pose (17 Keypoints)
 */
private fun DrawScope.drawMoveNetPose(
    pose: Pose,
    imageWidth: Int,
    imageHeight: Int,
) {
    val scaleX = size.width / imageWidth
    val scaleY = size.height / imageHeight

    // Zeichne Keypoints
    pose.keypoints.forEach { keypoint ->
        val x = keypoint.x * scaleX
        val y = keypoint.y * scaleY

        // Keypoint Kreis
        drawCircle(
            color = Color.Red,
            radius = 8.dp.toPx(),
            center = Offset(x, y),
        )

        // Confidence Indikator
        val alpha = keypoint.score
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 4.dp.toPx(),
            center = Offset(x, y),
        )
    }

    // Zeichne Skelett-Verbindungen
    drawMoveNetSkeleton(pose, scaleX, scaleY)
}

/**
 * Zeichnet BlazePose (33 Landmarks)
 */
private fun DrawScope.drawBlazePose(
    landmarks: List<Landmark3D>,
    imageWidth: Int,
    imageHeight: Int,
) {
    val scaleX = size.width / imageWidth
    val scaleY = size.height / imageHeight

    // Zeichne Landmarks
    landmarks.forEachIndexed { index, landmark ->
        val x = landmark.x * size.width
        val y = landmark.y * size.height

        // Landmark Kreis
        val color =
            when {
                landmark.visibility > 0.8f -> Color.Green
                landmark.visibility > 0.5f -> Color.Yellow
                else -> Color.Red
            }

        drawCircle(
            color = color,
            radius = 6.dp.toPx(),
            center = Offset(x, y),
        )

        // Visibility Indikator
        drawCircle(
            color = Color.White.copy(alpha = landmark.visibility),
            radius = 3.dp.toPx(),
            center = Offset(x, y),
        )
    }

    // Zeichne Skelett-Verbindungen
    drawBlazePoseSkeleton(landmarks)
}

/**
 * Zeichnet MoveNet Skelett-Verbindungen
 */
private fun DrawScope.drawMoveNetSkeleton(
    pose: Pose,
    scaleX: Float,
    scaleY: Float,
) {
    val keypointMap = pose.keypoints.associateBy { it.name }

    // Definiere Verbindungen
    val connections =
        listOf(
            // Kopf
            "left_ear" to "left_eye",
            "left_eye" to "nose",
            "nose" to "right_eye",
            "right_eye" to "right_ear",
            // Torso
            "left_shoulder" to "right_shoulder",
            "left_shoulder" to "left_hip",
            "right_shoulder" to "right_hip",
            "left_hip" to "right_hip",
            // Arme
            "left_shoulder" to "left_elbow",
            "left_elbow" to "left_wrist",
            "right_shoulder" to "right_elbow",
            "right_elbow" to "right_wrist",
            // Beine
            "left_hip" to "left_knee",
            "left_knee" to "left_ankle",
            "right_hip" to "right_knee",
            "right_knee" to "right_ankle",
        )

    connections.forEach { (start, end) ->
        val startPoint = keypointMap[start]
        val endPoint = keypointMap[end]

        if (startPoint != null && endPoint != null &&
            startPoint.score > 0.3f && endPoint.score > 0.3f
        ) {
            drawLine(
                color = Color.Blue,
                start = Offset(startPoint.x * scaleX, startPoint.y * scaleY),
                end = Offset(endPoint.x * scaleX, endPoint.y * scaleY),
                strokeWidth = 3.dp.toPx(),
            )
        }
    }
}

/**
 * Zeichnet BlazePose Skelett-Verbindungen
 */
private fun DrawScope.drawBlazePoseSkeleton(landmarks: List<Landmark3D>) {
    if (landmarks.size < BlazePoseSpec.COUNT) return

    BlazePoseSpec.CONNECTIONS.forEach { (startIdx, endIdx) ->
        if (startIdx < landmarks.size && endIdx < landmarks.size) {
            val start = landmarks[startIdx]
            val end = landmarks[endIdx]

            if (start.visibility > 0.3f && end.visibility > 0.3f) {
                drawLine(
                    color = Color.Cyan,
                    start = Offset(start.x * size.width, start.y * size.height),
                    end = Offset(end.x * size.width, end.y * size.height),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        }
    }
}
