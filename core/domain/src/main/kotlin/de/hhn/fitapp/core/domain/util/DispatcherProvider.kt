package de.hhn.fitapp.core.domain.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Interface für die Bereitstellung von Coroutine Dispatchers.
 * Ermöglicht Testbarkeit durch Dependency Injection und Ersetzbarkeit der Dispatchers.
 */
interface DispatcherProvider {
    /**
     * Dispatcher für IO-gebundene Operationen.
     * Verwendet für Datenbankzugriffe, Netzwerkanfragen, Datei-IO, etc.
     */
    val io: CoroutineDispatcher
    
    /**
     * Dispatcher für CPU-intensive Berechnungen.
     * Verwendet für komplexe Algorithmen, Bildverarbeitung, etc.
     */
    val default: CoroutineDispatcher
    
    /**
     * Dispatcher für den Main-Thread.
     * Verwendet für UI-Updates und Interaktionen mit der UI.
     */
    val main: CoroutineDispatcher
    
    /**
     * Dispatcher für unconfined Coroutines.
     * Verwendet in speziellen Fällen, in denen der Thread nicht gewechselt werden soll.
     */
    val unconfined: CoroutineDispatcher
}

/**
 * Standard-Implementierung des DispatcherProvider-Interfaces.
 * Verwendet die Standard-Dispatchers aus der Coroutines-Bibliothek.
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * Singleton-Instanz für einfachen Zugriff.
 */
object AppDispatchers {
    val provider: DispatcherProvider = DefaultDispatcherProvider()
}