package com.example.fitapp.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

/**
 * Interface für Navigation Provider, die von Feature-Modulen implementiert wird.
 * Jedes Feature-Modul kann seine eigenen Navigationsrouten definieren und registrieren.
 */
interface NavigationProvider {
    /**
     * Registriert alle Navigationsrouten dieses Providers im NavGraphBuilder.
     *
     * @param builder Der NavGraphBuilder, in dem die Routen registriert werden
     * @param navController Der NavController für die Navigation zwischen Screens
     */
    fun registerNavigation(builder: NavGraphBuilder, navController: NavController)

    /**
     * Gibt die Liste der Hauptrouten zurück, die dieser Provider definiert.
     * Diese werden für die Navigationsleiste und andere Navigationskomponenten verwendet.
     *
     * @return Eine Liste von RouteDefinition-Objekten
     */
    fun getMainRoutes(): List<RouteDefinition>
}

/**
 * Datenklasse zur Definition einer Route mit ihren Eigenschaften.
 *
 * @param route Die Route-ID, die für die Navigation verwendet wird
 * @param title Der anzuzeigende Titel der Route
 * @param icon Optional: Eine Icon-Ressource für die Route (z.B. für Navigation Drawer)
 * @param contentDescription Optional: Beschreibung für Barrierefreiheit
 * @param category Optional: Kategorie der Route für Gruppierungen
 */
data class RouteDefinition(
    val route: String,
    val title: String,
    val icon: Any? = null,
    val contentDescription: String? = null,
    val category: String? = null
)