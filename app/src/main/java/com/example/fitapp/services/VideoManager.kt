package com.example.fitapp.services

import android.content.Context
import android.net.Uri
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Video Management System for workout guidance
 * Handles local video library, caching, and adaptive streaming
 */
class VideoManager(private val context: Context) {
    companion object {
        private const val TAG = "VideoManager"
        private const val VIDEO_CACHE_DIR = "workout_videos"
        private const val MAX_CACHE_SIZE_MB = 500L // 500MB cache limit
    }

    private val cacheDir by lazy {
        File(context.cacheDir, VIDEO_CACHE_DIR).apply { mkdirs() }
    }

    /**
     * Preload workout videos for offline use
     */
    suspend fun preloadWorkoutVideos(exerciseIds: List<String>) =
        withContext(Dispatchers.IO) {
            exerciseIds.forEach { exerciseId ->
                try {
                    preloadExerciseVideo(exerciseId)
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYSTEM,
                        TAG,
                        "Preloaded video for exercise: $exerciseId",
                    )
                } catch (e: Exception) {
                    StructuredLogger.error(
                        StructuredLogger.LogCategory.SYSTEM,
                        TAG,
                        "Failed to preload video for exercise: $exerciseId",
                        exception = e,
                    )
                }
            }
        }

    /**
     * Get video URI for exercise with adaptive quality
     */
    suspend fun getExerciseVideo(
        exerciseId: String,
        quality: VideoQuality = VideoQuality.AUTO,
    ): VideoResource? =
        withContext(Dispatchers.IO) {
            try {
                val cachedFile = File(cacheDir, "${exerciseId}_${quality.name.lowercase()}.mp4")

                if (cachedFile.exists()) {
                    VideoResource(
                        uri = Uri.fromFile(cachedFile),
                        quality = quality,
                        isLocal = true,
                        fileSize = cachedFile.length(),
                    )
                } else {
                    // Return sample video resource for demo
                    getBuiltInExerciseVideo(exerciseId, quality)
                }
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYSTEM,
                    TAG,
                    "Failed to get video for exercise: $exerciseId",
                    exception = e,
                )
                null
            }
        }

    /**
     * Background video download with progress tracking
     */
    fun downloadVideoInBackground(exerciseId: String): Flow<DownloadProgress> =
        flow {
            try {
                emit(DownloadProgress(exerciseId, 0f, DownloadStatus.STARTING))

                // Simulate download progress
                for (progress in 10..100 step 10) {
                    kotlinx.coroutines.delay(200)
                    emit(DownloadProgress(exerciseId, progress / 100f, DownloadStatus.DOWNLOADING))
                }

                // Cache the "downloaded" video
                cacheExerciseVideo(exerciseId)
                emit(DownloadProgress(exerciseId, 1f, DownloadStatus.COMPLETED))
            } catch (e: Exception) {
                emit(DownloadProgress(exerciseId, 0f, DownloadStatus.ERROR))
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYSTEM,
                    TAG,
                    "Failed to download video for exercise: $exerciseId",
                    exception = e,
                )
            }
        }

    /**
     * Adaptive quality streaming based on network conditions
     */
    suspend fun adaptiveQualityStreaming(exerciseId: String): VideoResource? {
        // Check network conditions and device capabilities
        val networkType = getNetworkType()
        val quality =
            when (networkType) {
                NetworkType.WIFI -> VideoQuality.HIGH
                NetworkType.MOBILE_HIGH_SPEED -> VideoQuality.MEDIUM
                NetworkType.MOBILE_LOW_SPEED -> VideoQuality.LOW
                NetworkType.OFFLINE -> VideoQuality.LOW // Use cached low quality
            }

        return getExerciseVideo(exerciseId, quality)
    }

    /**
     * Manage cache size and cleanup old videos
     */
    suspend fun manageCacheSize() =
        withContext(Dispatchers.IO) {
            try {
                val totalSize =
                    cacheDir.walkTopDown()
                        .filter { it.isFile }
                        .map { it.length() }
                        .sum()

                if (totalSize > MAX_CACHE_SIZE_MB * 1024 * 1024) {
                    // Remove oldest files first
                    cacheDir.walkTopDown()
                        .filter { it.isFile }
                        .sortedBy { it.lastModified() }
                        .take((cacheDir.listFiles()?.size ?: 0) / 2)
                        .forEach { it.delete() }

                    StructuredLogger.info(
                        StructuredLogger.LogCategory.SYSTEM,
                        TAG,
                        "Cleaned up video cache, removed old files",
                    )
                }
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYSTEM,
                    TAG,
                    "Failed to manage cache size",
                    exception = e,
                )
            }
        }

    private suspend fun preloadExerciseVideo(exerciseId: String) {
        // Simulate preloading by creating a cached entry
        cacheExerciseVideo(exerciseId)
    }

    private suspend fun cacheExerciseVideo(exerciseId: String) {
        val cachedFile = File(cacheDir, "${exerciseId}_medium.mp4")
        if (!cachedFile.exists()) {
            // Create a placeholder cached file for demo
            cachedFile.writeText("# Cached video for $exerciseId")
        }
    }

    private fun getBuiltInExerciseVideo(
        exerciseId: String,
        quality: VideoQuality,
    ): VideoResource {
        // Return built-in video resources for common exercises
        return VideoResource(
            uri = Uri.parse("android.resource://${context.packageName}/raw/sample_exercise"),
            quality = quality,
            isLocal = true,
            fileSize = 1024 * 512, // 512KB sample
        )
    }

    private fun getNetworkType(): NetworkType {
        // Simplified network detection
        return NetworkType.WIFI // Default to WIFI for demo
    }
}

/**
 * Video quality options for adaptive streaming
 */
enum class VideoQuality {
    LOW, // 480p, smaller file size
    MEDIUM, // 720p, balanced quality/size
    HIGH, // 1080p, best quality
    AUTO, // Automatically select based on conditions
}

/**
 * Video resource information
 */
data class VideoResource(
    val uri: Uri,
    val quality: VideoQuality,
    val isLocal: Boolean,
    val fileSize: Long,
)

/**
 * Download progress tracking
 */
data class DownloadProgress(
    val exerciseId: String,
    val progress: Float, // 0.0 to 1.0
    val status: DownloadStatus,
)

enum class DownloadStatus {
    STARTING,
    DOWNLOADING,
    COMPLETED,
    ERROR,
}

enum class NetworkType {
    WIFI,
    MOBILE_HIGH_SPEED,
    MOBILE_LOW_SPEED,
    OFFLINE,
}
