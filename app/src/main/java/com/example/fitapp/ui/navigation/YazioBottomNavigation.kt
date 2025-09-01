package com.example.fitapp.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * YAZIO-style bottom navigation bar with smooth animations
 * 
 * Features:
 * - Clean 4-tab design
 * - Smooth color and scale animations
 * - Proper accessibility support
 * - Material Design 3 styling
 */
@Composable
fun YazioBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = NavigationRoutes.getParentDestination(currentRoute) 
        ?: BottomNavDestination.fromRoute(currentRoute)
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        BottomNavDestination.values().forEach { destination ->
            val selected = currentDestination == destination
            
            NavigationBarItem(
                icon = {
                    AnimatedIcon(
                        icon = destination.icon,
                        selected = selected,
                        contentDescription = destination.label
                    )
                },
                label = {
                    AnimatedLabel(
                        text = destination.label,
                        selected = selected
                    )
                },
                selected = selected,
                onClick = {
                    navigateToDestination(navController, destination)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun AnimatedIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    contentDescription: String
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "icon_scale"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = Modifier.scale(scale)
    )
}

@Composable
private fun AnimatedLabel(
    text: String,
    selected: Boolean
) {
    val fontWeight by animateFloatAsState(
        targetValue = if (selected) FontWeight.SemiBold.weight.toFloat() else FontWeight.Normal.weight.toFloat(),
        animationSpec = tween(durationMillis = 200),
        label = "font_weight"
    )
    
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight(fontWeight.toInt()),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Navigate to a bottom navigation destination
 * Handles proper back stack management for smooth navigation
 */
private fun navigateToDestination(
    navController: NavController,
    destination: BottomNavDestination
) {
    val targetRoute = when (destination) {
        BottomNavDestination.DIARY -> NavigationRoutes.FOOD_DIARY
        BottomNavDestination.FASTING -> NavigationRoutes.FASTING_TIMER
        BottomNavDestination.RECIPES -> NavigationRoutes.RECIPE_BROWSE
        BottomNavDestination.PROFILE -> NavigationRoutes.PROFILE_OVERVIEW
    }
    
    navController.navigate(targetRoute) {
        // Pop up to the start destination to avoid building up a large stack
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

/**
 * Compact version of bottom navigation for landscape mode or small screens
 */
@Composable
fun CompactBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = NavigationRoutes.getParentDestination(currentRoute) 
        ?: BottomNavDestination.fromRoute(currentRoute)
    
    NavigationBar(
        modifier = modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        BottomNavDestination.values().forEach { destination ->
            val selected = currentDestination == destination
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = null, // No labels in compact mode
                selected = selected,
                onClick = {
                    navigateToDestination(navController, destination)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}