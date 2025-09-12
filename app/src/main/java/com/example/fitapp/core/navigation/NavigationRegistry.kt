package com.example.fitapp.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

/**
 * Zentrale Registry für die modulare Navigation.
 * Sammelt NavigationProvider aus verschiedenen Modulen und ermöglicht eine
 * zentrale Registrierung aller Routen.
 */
class NavigationRegistry {
    private val providers = mutableListOf<NavigationProvider>()

    /**
     * Registriert einen NavigationProvider in der Registry.
     *
     * @param provider Der zu registrierende NavigationProvider
     */
    fun registerProvider(provider: NavigationProvider) {
        providers.add(provider)
    }

    /**
     * Registriert alle NavigationProvider in einem NavGraphBuilder.
     *
     * @param builder Der NavGraphBuilder, in dem die Routen registriert werden sollen
     * @param navController Der NavController für die Navigation
     */
    fun registerAllNavigations(builder: NavGraphBuilder, navController: NavController) {
        providers.forEach { provider ->
            provider.registerNavigation(builder, navController)
        }
    }

    /**
     * Gibt alle Hauptrouten aller registrierten Provider zurück.
     *
     * @return Eine Liste aller Hauptrouten
     */
    fun getAllMainRoutes(): List<RouteDefinition> {
        return providers.flatMap { it.getMainRoutes() }
    }

    /**
     * Gibt alle Hauptrouten einer bestimmten Kategorie zurück.
     *
     * @param category Die Kategorie, nach der gefiltert werden soll
     * @return Eine Liste aller Hauptrouten der angegebenen Kategorie
     */
    fun getMainRoutesByCategory(category: String): List<RouteDefinition> {
        return getAllMainRoutes().filter { it.category == category }
    }

    companion object {
        // Singleton-Instanz
        @Volatile
        private var INSTANCE: NavigationRegistry? = null

        fun getInstance(): NavigationRegistry {
            return INSTANCE ?: synchronized(this) {
                val instance = NavigationRegistry()
                INSTANCE = instance
                instance
            }
        }
    }
}