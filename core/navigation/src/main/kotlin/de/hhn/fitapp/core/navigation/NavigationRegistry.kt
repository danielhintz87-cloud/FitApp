package de.hhn.fitapp.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

/**
 * Schnittstelle für Navigationsanbieter, die NavigationDestinations registrieren können.
 * Implementiert von Modulen, die Navigationsrouten bereitstellen.
 */
interface NavigationProvider {
    /**
     * Registriert die vom Modul angebotenen Navigationsziele.
     */
    fun registerDestinations(registry: NavigationRegistry)
}

/**
 * Registry für alle verfügbaren NavigationDestinations in der Anwendung.
 * Erlaubt eine zentrale Verwaltung aller registrierten Routen und vereinfacht die Modularisierung.
 */
class NavigationRegistry {
    private val destinations = mutableMapOf<String, NavigationDestination>()
    private val navigationProviders = mutableListOf<NavigationProvider>()

    /**
     * Registriert ein Navigationsziel in der Registry.
     */
    fun registerDestination(destination: NavigationDestination) {
        destinations[destination.route] = destination
    }

    /**
     * Registriert einen NavigationProvider.
     */
    fun registerNavigationProvider(provider: NavigationProvider) {
        navigationProviders.add(provider)
        provider.registerDestinations(this)
    }

    /**
     * Anwenden aller registrierten Navigationsziele auf den NavGraphBuilder.
     */
    fun applyNavigation(navGraphBuilder: NavGraphBuilder, navController: NavController) {
        destinations.values.forEach { destination ->
            destination.addToNavGraph(navGraphBuilder, navController)
        }
    }

    /**
     * Gibt alle registrierten Routen zurück.
     */
    fun getRegisteredRoutes(): Set<String> = destinations.keys

    /**
     * Prüft, ob eine Route registriert ist.
     */
    fun isRouteRegistered(route: String): Boolean = destinations.containsKey(route)
}

/**
 * Abstrakte Basisklasse für Navigationsziele.
 */
abstract class NavigationDestination(val route: String) {
    /**
     * Fügt das Navigationsziel zum NavGraphBuilder hinzu.
     */
    abstract fun addToNavGraph(navGraphBuilder: NavGraphBuilder, navController: NavController)
}