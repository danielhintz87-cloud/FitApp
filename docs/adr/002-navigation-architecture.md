# ADR-002: Navigation Architecture Pattern

**Date**: 2024-01-11  
**Status**: Accepted  
**Decision Makers**: Development Team

## Context

FitApp needs a consistent navigation strategy that supports:
- Drawer-based main navigation (not bottom tabs)
- Deep linking for sharing and external integration
- Parameter passing between screens
- Testable navigation logic

## Decision

We adopt **Jetpack Compose Navigation** with centralized route management:

1. **Single NavHost** - All navigation managed through one NavHost in MainScaffold
2. **Drawer navigation** - Primary navigation via ModalNavigationDrawer, no bottom tabs
3. **Route constants** - Centralized route definitions with helper functions
4. **Parameter patterns** - Consistent argument naming and type safety

## Route Naming Conventions

```kotlin
// Primary routes: snake_case
"unified_dashboard", "nutrition", "enhanced_analytics"

// Sub-routes: section/feature_name pattern  
"nutrition/recipe_detail/{recipeId}"
"plan/training_execution/{planId}"

// Parameters: camelCase in braces
"{recipeId}", "{planId}", "{sessionId}"
```

## Consequences

### Positive
- ✅ Consistent navigation experience
- ✅ Type-safe parameter passing
- ✅ Deep linking support built-in
- ✅ Testable with navigation testing library

### Negative
- ⚠️ Single NavHost can become large
- ⚠️ Complex nested navigation requires careful planning
- ⚠️ Route string management needs discipline

## Implementation

```kotlin
object NavigationRoutes {
    const val DASHBOARD = "unified_dashboard"
    const val RECIPE_DETAIL = "nutrition/recipe_detail/{recipeId}"
    
    fun recipeDetail(recipeId: String) = "nutrition/recipe_detail/$recipeId"
}

// In NavHost
composable(
    route = NavigationRoutes.RECIPE_DETAIL,
    arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
) { backStackEntry ->
    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
    RecipeDetailScreen(navController, recipeId)
}
```

## Alternatives Considered

- **Bottom Navigation**: Rejected for this app's workflow (drawer better for many sections)
- **Multiple NavHosts**: Rejected due to complexity and state management issues
- **Manual navigation**: Rejected due to lack of type safety and testing support

## References

- [Compose Navigation Guide](https://developer.android.com/jetpack/compose/navigation)
- [Navigation Testing](https://developer.android.com/guide/navigation/navigation-testing)