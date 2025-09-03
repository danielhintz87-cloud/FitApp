package com.example.fitapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.prefs.ApiKeys

/**
 * Reusable component that shows a warning banner and disables content
 * when API keys are not configured properly
 */
@Composable
fun AiKeyGate(
    modifier: Modifier = Modifier,
    onNavigateToApiKeys: () -> Unit,
    requireBothProviders: Boolean = true,
    content: @Composable (isEnabled: Boolean) -> Unit
) {
    val context = LocalContext.current
    val isEnabled = if (requireBothProviders) {
        ApiKeys.isPrimaryProviderAvailable(context)
    } else {
        ApiKeys.getGeminiKey(context).isNotBlank() || ApiKeys.getPerplexityKey(context).isNotBlank()
    }

    Column(modifier = modifier) {
        // Show warning banner when keys are not available
        if (!isEnabled) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "API-Schlüssel erforderlich",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (requireBothProviders) {
                            "Für KI-Funktionen werden sowohl Gemini- als auch Perplexity-API-Schlüssel benötigt."
                        } else {
                            "Für KI-Funktionen wird mindestens ein API-Schlüssel (Gemini oder Perplexity) benötigt."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToApiKeys,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                            contentColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(Icons.Filled.Key, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("API-Schlüssel konfigurieren")
                    }
                }
            }
        }

        // Content with enabled/disabled state
        content(isEnabled)
    }
}