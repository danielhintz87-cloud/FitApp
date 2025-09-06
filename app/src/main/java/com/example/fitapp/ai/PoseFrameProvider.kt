package com.example.fitapp.ai

import android.graphics.Bitmap

/**
 * Provides current camera frame as Bitmap for pose analysis.
 * Implementationen können intern YUV→RGB konvertieren.
 */
interface PoseFrameProvider {
    fun currentFrame(): Bitmap?
}

object DefaultNoCameraFrameProvider : PoseFrameProvider {
    override fun currentFrame(): Bitmap? = null
}