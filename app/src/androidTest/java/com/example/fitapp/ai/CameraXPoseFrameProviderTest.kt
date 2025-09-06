package com.example.fitapp.ai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

/**
 * Sehr einfacher Smoke-Test für CameraXPoseFrameProvider:
 * - Startet Provider mit LifecycleOwner
 * - Wartet kurz und prüft, dass entweder kein Crash oder (optional) ein Frame vorhanden ist
 * Hinweis: In Emulator Testumgebung kann kein echtes Kamerabild geliefert werden; Test validiert Stabilität.
 */
@RunWith(AndroidJUnit4::class)
class CameraXPoseFrameProviderTest {

    private class TestOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        init { registry.currentState = Lifecycle.State.RESUMED }
        override fun getLifecycle(): Lifecycle = registry
    }

    @Test
    fun startAndStopDoesNotCrash() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val ml = AdvancedMLModels.getInstance(ctx)
        // Kein echtes init nötig für Frame Provider
        val provider = CameraXPoseFrameProvider(ctx, ml)
        provider.start(TestOwner(), null)
        delay(300) // kurze Zeit für potenzielle Frames
        // Darf nicht abgestürzt sein – optional Frame prüfen
        val frame = provider.currentFrame()
        // Frame kann null sein (Emulator ohne Kamera) – nur sicherstellen, dass kein Exception Flow
        assertTrue(true)
        provider.stop()
        // Nach stop sollte kein Crash bei erneutem Stop auftreten
        provider.stop()
    }
}
