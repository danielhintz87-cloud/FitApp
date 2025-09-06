package com.example.fitapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Simple benchmark for comparing inference latency across backends and model variants.
 * Produces average ms over N runs after warmup.
 */
object PoseBackendBenchmark {
    data class Result(val variant: String, val backend: String, val avgMs: Double, val runs: Int)

    suspend fun run(
        context: Context,
        variants: List<AdvancedMLModels.PoseModelType> = listOf(
            AdvancedMLModels.PoseModelType.MOVENET_LIGHTNING,
            AdvancedMLModels.PoseModelType.MOVENET_THUNDER
        ),
        runs: Int = 50,
        warmup: Int = 5,
        useOnnx: Boolean = false
    ): List<Result> = withContext(Dispatchers.Default) {
        val results = mutableListOf<Result>()
        val testBitmap = makeTestBitmap(512,512)
        for (variant in variants) {
            // Toggle ONNX via env var simulation (process-wide flag read at init)
            val ml = AdvancedMLModels.getInstance(context)
            // NOTE: For switching backend between runs in same process an app restart might be needed;
            // here we proceed sequentially (best effort) but real measurement should isolate processes.
            ml.initialize(variant)
            // Warmup
            repeat(warmup) { ml.analyzePoseFromFrame(testBitmap) }
            val times = mutableListOf<Long>()
            repeat(runs) {
                val start = System.nanoTime()
                ml.analyzePoseFromFrame(testBitmap)
                times += (System.nanoTime() - start) / 1_000_000
            }
            val avg = times.average()
            val backend = if (useOnnx && variant == AdvancedMLModels.PoseModelType.MOVENET_LIGHTNING) "onnx_or_fallback" else "tflite"
            results += Result(variant.name.lowercase(), backend, avg, runs)
            ml.cleanup()
        }
        results
    }

    private fun makeTestBitmap(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.BLACK)
        val p = Paint().apply { color = Color.WHITE; strokeWidth = 4f }
        for (i in 0 until 50) {
            c.drawLine((i*10 % w).toFloat(), 0f, (i*7 % w).toFloat(), h.toFloat(), p)
        }
        return bmp
    }
}
