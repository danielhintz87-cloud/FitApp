# Placeholder Screen Template

This document provides a standardized template for creating new feature screens in FitApp to ensure consistent navigation integration and accessibility support.

## Template Code

```kotlin
package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourFeatureScreen(
    onBack: (() -> Unit)? = null,
    onNavigateToRelatedFeature: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Standard Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.your_feature_title), // Add to strings.xml
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = context.getString(R.string.back)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + contentPadding.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Feature content goes here
            
            // Example action button with proper accessibility
            Button(
                onClick = { onNavigateToRelatedFeature?.invoke() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.SomeIcon, // Choose appropriate icon
                    contentDescription = context.getString(R.string.icon_description),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(context.getString(R.string.action_text))
            }
        }
    }
}
```

## Integration Steps

### 1. Add String Resources

Add the following to `app/src/main/res/values/strings.xml`:

```xml
<!-- Your Feature Screen -->
<string name="your_feature_title">Your Feature Title</string>
<string name="your_feature_description">Description of your feature</string>
<string name="icon_your_feature">Your Feature Symbol</string>
<!-- Add other feature-specific strings -->
```

### 2. Add Navigation Route

In `MainScaffold.kt`, add the composable route:

```kotlin
composable("your_feature") {
    YourFeatureScreen(
        onBack = { nav.popBackStack() },
        onNavigateToRelatedFeature = { nav.navigate("related_feature") },
        contentPadding = padding
    )
}
```

### 3. Add Navigation Access Points

Choose appropriate access points for your feature:

**In Navigation Drawer (for major features):**
```kotlin
NavigationDrawerItem(
    label = { Text("🔧 ${ctx.getString(R.string.your_feature_title)}") }, 
    selected = currentRoute?.startsWith("your_feature") == true, 
    onClick = { scope.launch { drawerState.close() }; nav.navigate("your_feature") },
    icon = { Icon(Icons.Filled.YourIcon, contentDescription = ctx.getString(R.string.icon_your_feature)) },
    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
)
```

**In Top Bar Actions (for contextual features):**
```kotlin
currentRoute?.startsWith("related_screen") == true -> {
    IconButton(onClick = { nav.navigate("your_feature") }) {
        Icon(Icons.Filled.YourIcon, contentDescription = ctx.getString(R.string.your_feature_title))
    }
}
```

**In Overflow Menu (for settings/secondary features):**
```kotlin
DropdownMenuItem(
    text = { Text(ctx.getString(R.string.your_feature_title)) },
    onClick = {
        showOverflowMenu = false
        nav.navigate("your_feature")
    },
    leadingIcon = {
        Icon(Icons.Filled.YourIcon, contentDescription = null)
    }
)
```

### 4. Add Tests

Create test file `app/src/androidTest/java/com/example/fitapp/navigation/YourFeatureNavigationTest.kt`:

```kotlin
@Test
fun navHost_clickYourFeature_navigatesToYourFeature() {
    // Navigation test implementation
    composeTestRule.onNodeWithContentDescription("Menü").performClick()
    composeTestRule.onNodeWithText("🔧 Your Feature Title").performClick()
    composeTestRule.onNodeWithText("Your Feature Title").assertIsDisplayed()
}

@Test
fun yourFeature_backButtonWorks() {
    var backClicked = false
    
    composeTestRule.setContent {
        FitAppTheme {
            YourFeatureScreen(onBack = { backClicked = true })
        }
    }

    composeTestRule.onNodeWithContentDescription("Zurück").performClick()
    assert(backClicked) { "Back button should trigger navigation" }
}
```

### 5. Add Deeplink Support (Optional)

If your feature should support deeplinks, add to the list of supported fitapp:// URLs and handle in MainActivity or navigation setup.

## Accessibility Guidelines

- **Always** provide `contentDescription` for icons
- Use string resources for all user-visible text
- Ensure proper contrast and touch target sizes
- Test with TalkBack enabled
- Use semantic markup where appropriate

## Navigation Best Practices

- Keep navigation callbacks as lambda parameters for flexibility
- Use `onBack` for consistent back button behavior  
- Pass `contentPadding` to respect system UI padding
- Use existing icon sets and maintain visual consistency
- Group related features in logical navigation sections

## Testing Requirements

- Navigation functionality test
- Back button test
- Accessibility content description test
- String resource usage verification
- Visual regression test (if UI components)

This template ensures new features integrate seamlessly with the existing navigation structure while maintaining accessibility and testing standards.