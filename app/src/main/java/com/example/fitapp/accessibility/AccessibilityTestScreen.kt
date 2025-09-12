package com.example.fitapp.accessibility

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Sample composable with accessibility violations for testing lint rules.
 * This should trigger accessibility lint errors when the setup is working.
 */
@Composable
fun AccessibilityTestScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // This should trigger ContentDescription lint error - icon without description
        IconButton(
            onClick = { },
            modifier = Modifier.size(24.dp), // This should trigger TouchTargetSizeCheck - too small
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null, // Missing content description - should trigger lint error
            )
        }

        // Example of proper accessibility implementation
        IconButton(
            onClick = { },
            modifier = Modifier.size(48.dp), // Proper touch target size
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new item", // Proper content description
            )
        }

        // Text without proper semantics - should trigger lint warnings
        Text(
            text = "Click here",
            modifier =
                Modifier.semantics {
                    // Missing contentDescription for clickable text
                },
        )
    }
}

/**
 * Composable that follows accessibility best practices
 */
@Composable
fun AccessibleScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = { /* Handle click */ },
            modifier = Modifier.size(48.dp), // Adequate touch target
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new fitness activity", // Descriptive content description
            )
        }

        Text(
            text = "Add Activity",
            modifier =
                Modifier.semantics {
                    contentDescription = "Add new fitness activity button"
                },
        )
    }
}
