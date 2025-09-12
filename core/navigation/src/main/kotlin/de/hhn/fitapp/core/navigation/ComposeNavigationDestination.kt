package de.hhn.fitapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

/**
 * Spezialisierung von NavigationDestination für Compose-Screens.
 * Vereinfacht die Registrierung von Compose-Screens als Navigationsziele.
 */
class ComposeNavigationDestination(
    route: String,
    private val arguments: List<NavArgument> = emptyList(),
    private val deepLinks: List<DeepLink> = emptyList(),
    private val content: @Composable (NavHostController, NavBackStackEntry) -> Unit
) : NavigationDestination(route) {

    override fun addToNavGraph(navGraphBuilder: NavGraphBuilder, navController: NavController) {
        val navArgs = arguments.map { it.toNavArgument() }
        val navDeepLinks = deepLinks.map { it.toNavDeepLink() }

        navGraphBuilder.composable(
            route = route,
            arguments = navArgs,
            deepLinks = navDeepLinks
        ) { backStackEntry ->
            content(navController as NavHostController, backStackEntry)
        }
    }

    /**
     * Builder-Klasse für ComposeNavigationDestination.
     */
    class Builder(private val route: String) {
        private val arguments = mutableListOf<NavArgument>()
        private val deepLinks = mutableListOf<DeepLink>()
        private var content: (@Composable (NavHostController, NavBackStackEntry) -> Unit)? = null

        fun addArgument(name: String, type: NavArgumentType, nullable: Boolean = false): Builder {
            arguments.add(NavArgument(name, type, nullable))
            return this
        }

        fun addDeepLink(uriPattern: String): Builder {
            deepLinks.add(DeepLink(uriPattern))
            return this
        }

        fun setContent(content: @Composable (NavHostController, NavBackStackEntry) -> Unit): Builder {
            this.content = content
            return this
        }

        fun build(): ComposeNavigationDestination {
            requireNotNull(content) { "Content must be set for ComposeNavigationDestination" }
            return ComposeNavigationDestination(route, arguments, deepLinks, content!!)
        }
    }
}

/**
 * Hilfsmethode zum Erstellen eines ComposeNavigationDestination.Builder.
 */
fun composeDestination(route: String): ComposeNavigationDestination.Builder {
    return ComposeNavigationDestination.Builder(route)
}

/**
 * Hilfsklasse für die Darstellung von Navigationsargumenten.
 */
data class NavArgument(
    val name: String,
    val type: NavArgumentType,
    val nullable: Boolean = false
) {
    fun toNavArgument(): androidx.navigation.NamedNavArgument {
        return androidx.navigation.navArgument(name) {
            type = this@NavArgument.type.toNavType()
            nullable = this@NavArgument.nullable
        }
    }
}

/**
 * Hilfsklasse für die Darstellung von Deep Links.
 */
data class DeepLink(val uriPattern: String) {
    fun toNavDeepLink(): androidx.navigation.NavDeepLink {
        return androidx.navigation.navDeepLink {
            uriPattern = this@DeepLink.uriPattern
        }
    }
}

/**
 * Enum für die Typen von Navigationsargumenten.
 */
enum class NavArgumentType {
    STRING, INT, BOOLEAN, LONG, FLOAT;

    fun toNavType(): androidx.navigation.NavType<*> {
        return when (this) {
            STRING -> androidx.navigation.NavType.StringType
            INT -> androidx.navigation.NavType.IntType
            BOOLEAN -> androidx.navigation.NavType.BoolType
            LONG -> androidx.navigation.NavType.LongType
            FLOAT -> androidx.navigation.NavType.FloatType
        }
    }
}