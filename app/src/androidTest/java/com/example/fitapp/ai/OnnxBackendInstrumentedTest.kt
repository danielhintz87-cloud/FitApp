package com.example.fitapp.ai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke-Test für optionales ONNX Backend.
 * Läuft nur sinnvoll wenn USE_ONNX_MOVENET=true gesetzt ist.
 */
@RunWith(AndroidJUnit4::class)
class OnnxBackendInstrumentedTest {
    @Test
    fun onnxOrTfliteFallbackLoads() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val ml = AdvancedMLModels.getInstance(ctx)
        val ok = ml.initialize(AdvancedMLModels.PoseModelType.MOVENET_LIGHTNING)
        assertTrue("Initialisierung (ONNX oder Fallback TFLite) muss erfolgreich sein", ok)
    }
}
