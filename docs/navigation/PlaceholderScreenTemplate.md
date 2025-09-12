# Placeholder Screen Template

This document provides a template for creating placeholder screens to ensure consistent navigation routes and prevent runtime navigation errors.

## Basic Template

```kotlin
package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.R

/**
 * Placeholder screen for [FEATURE_NAME]
 * TODO: Implement actual functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun [FeatureName]Screen(
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_[feature_name])) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(contentPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.[FeatureIcon],
                contentDescription = stringResource(R.string.cd_[feature]_icon),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.title_[feature_name]),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedButton(
                onClick = onBack
            ) {
                Text(stringResource(R.string.action_back))
            }
        }
    }
}
```

## Required String Resources

Add these to `strings.xml`:

```xml
<!-- Feature specific strings -->
<string name="title_[feature_name]">[Feature Display Name]</string>
<string name="cd_[feature]_icon">[Feature] icon</string>
```

## Navigation Integration

### 1. Add to NavHost in MainScaffold.kt

```kotlin
composable("[feature_route]",
    deepLinks = listOf(navDeepLink { uriPattern = "fitapp://[feature_route]" })
) {
    [FeatureName]Screen(
        onBack = { nav.popBackStack() }
    )
}
```

### 2. Add Navigation Call

```kotlin
// In calling screen
onClick = { nav.navigate("[feature_route]") }
```

### 3. Add Deep Link Support

Add to AndroidManifest.xml if not already present:

```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="fitapp" />
</intent-filter>
```

## Testing Requirements

### 1. Add to NavigationTest.kt

```kotlin
@Test
fun navigation_[featureName]Accessible() {
    composeTestRule.setContent {
        MainScaffold()
    }

    // Test navigation path to feature
    // Add specific test steps based on how feature is accessed
    
    composeTestRule.waitForIdle()
}
```

### 2. Add Route Validation

Add the route to the `validDestinations` list in `navigation_verifyAllNavigationCallsHaveDestinations()` test.

## Accessibility Checklist

- [ ] All interactive elements have contentDescription
- [ ] Screen title uses string resource
- [ ] Icon has meaningful contentDescription
- [ ] Text contrast meets WCAG guidelines
- [ ] Focus order is logical for screen readers
- [ ] All text uses string resources for i18n

## Deep Link Testing

Test the deep link manually:

```bash
adb shell am start \
  -W -a android.intent.action.VIEW \
  -d "fitapp://[feature_route]" \
  com.example.fitapp
```

## Integration Points

### Drawer Navigation
Add to drawer menu if appropriate:

```kotlin
// In drawer composable
NavigationDrawerItem(
    label = { Text(stringResource(R.string.title_[feature_name])) },
    selected = false,
    onClick = { 
        nav.navigate("[feature_route]")
        scope.launch { drawerState.close() }
    },
    icon = { 
        Icon(
            Icons.Filled.[FeatureIcon], 
            contentDescription = stringResource(R.string.cd_[feature]_icon)
        ) 
    }
)
```

### Quick Actions
Add to QuickActionsScreen if appropriate:

```kotlin
QuickAction(
    title = stringResource(R.string.title_[feature_name]),
    icon = Icons.Filled.[FeatureIcon],
    route = "[feature_route]",
    category = QuickActionCategory.[CATEGORY]
)
```

## Migration Notes

When converting a placeholder to a full implementation:

1. Keep the same route and deep link
2. Maintain the same navigation signature
3. Update tests to verify actual functionality
4. Remove "Coming Soon" placeholder content
5. Update documentation

## Example Usage

See existing screens like `FeedbackScreen.kt` and `QuickActionsScreen.kt` for real implementations following this template.