package de.hhn.fitapp.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import de.hhn.fitapp.core.navigation.NavigationRegistry
import de.hhn.fitapp.core.navigation.NavigationRoutes

/**
 * Modulare NavHost-Implementierung für die FitApp-Anwendung.
 * Nutzt die NavigationRegistry, um Navigationsziele aus verschiedenen Modulen zu integrieren.
 */
@Composable
fun ModularNavHost(
    navController: NavHostController,
    navigationRegistry: NavigationRegistry,
    startDestination: String = NavigationRoutes.HOME
) {
    NavHost(navController = navController, startDestination = startDestination) {
        // Wende alle registrierten Navigationsziele auf den NavGraph an
        navigationRegistry.applyNavigation(this, navController)
    }
}