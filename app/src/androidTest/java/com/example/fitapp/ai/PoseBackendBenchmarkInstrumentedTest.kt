package com.example.fitapp.ai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PoseBackendBenchmarkInstrumentedTest {
    @Test
    fun benchmarkLightningTfliteAndOnnx() =
        runBlocking {
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            // Lightning TFLite
            AdvancedMLModels.forceOnnxBackend(false)
            val tflite =
                PoseBackendBenchmark.run(
                    ctx,
                    variants = listOf(AdvancedMLModels.PoseModelType.MOVENET_LIGHTNING),
                    runs = 5,
                    warmup = 1,
                    useOnnx = false,
                )
            // Lightning ONNX
            AdvancedMLModels.forceOnnxBackend(true)
            val onnx =
                PoseBackendBenchmark.run(
                    ctx,
                    variants = listOf(AdvancedMLModels.PoseModelType.MOVENET_LIGHTNING),
                    runs = 5,
                    warmup = 1,
                    useOnnx = true,
                )
            val combined = tflite + onnx
            val csv = PoseBackendBenchmark.toCsv(combined)
            val outDir = File(ctx.filesDir, "benchmarks/ml").apply { mkdirs() }
            val outFile = File(outDir, "pose_backend_latency_ci.csv")
            outFile.writeText(csv)
            assertTrue(outFile.exists())
        }
}
