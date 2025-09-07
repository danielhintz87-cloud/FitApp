package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.prefs.ApiKeys

@Composable
fun ApiKeysScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    
    var geminiKey by remember { mutableStateOf(ApiKeys.getGeminiKey(context)) }
    var perplexityKey by remember { mutableStateOf(ApiKeys.getPerplexityKey(context)) }
    
    var showGeminiPassword by remember { mutableStateOf(false) }
    var showPerplexityPassword by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var showDebugInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Provider API-Schlüssel",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = "Geben Sie Ihre API-Schlüssel für Gemini und Perplexity ein, um AI-Features zu nutzen. Die Schlüssel werden sicher auf dem Gerät gespeichert.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Status-Info Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Provider Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { showDebugInfo = !showDebugInfo }) {
                        Text(if (showDebugInfo) "Weniger" else "Details")
                    }
                }
                
                if (showDebugInfo) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ApiKeys.getConfigurationStatus(context),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    Text(
                        text = if (ApiKeys.isPrimaryProviderAvailable(context)) 
                            "✅ Beide Provider konfiguriert und verfügbar" 
                        else "⚠️ Konfiguration prüfen - Details anzeigen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gemini Key
        OutlinedTextField(
            value = geminiKey,
            onValueChange = { geminiKey = it; saved = false },
            label = { Text("Gemini API Key") },
            placeholder = { Text("AIza...") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showGeminiPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = { Text("Für multimodale Aufgaben (Bilder, strukturierte Pläne)") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { showGeminiPassword = !showGeminiPassword },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (showGeminiPassword) "Ausblenden" else "Anzeigen")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Perplexity Key
        OutlinedTextField(
            value = perplexityKey,
            onValueChange = { perplexityKey = it; saved = false },
            label = { Text("Perplexity API Key") },
            placeholder = { Text("pplx-...") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPerplexityPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = { Text("Für schnelle Q&A und Web-basierte Suche") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { showPerplexityPassword = !showPerplexityPassword },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (showPerplexityPassword) "Ausblenden" else "Anzeigen")
            }

            Button(
                onClick = {
                    ApiKeys.saveGeminiKey(context, geminiKey.trim())
                    ApiKeys.savePerplexityKey(context, perplexityKey.trim())
                    saved = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Beide Speichern")
            }
        }

        if (saved) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "✓ API-Schlüssel erfolgreich gespeichert",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Intelligente Task-Verteilung",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• **Gemini**: Multimodale Aufgaben (Bildanalyse), strukturierte Trainingspläne\n" +
                          "• **Perplexity**: Schnelle Q&A, Web-basierte Suche, Shopping-Listen\n" +
                          "• Die App wählt automatisch den optimalen Provider für jede Aufgabe\n" +
                          "• Fallback-Mechanismen bei Problemen mit einem Provider",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hinweise zur Sicherheit",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Die Schlüssel werden nur lokal auf Ihrem Gerät gespeichert\n" +
                          "• Für Produktionsnutzung wird verschlüsselte Speicherung empfohlen\n" +
                          "• Teilen Sie Ihre API-Schlüssel niemals mit anderen\n" +
                          "• Gemini und Perplexity als optimierte AI-Provider",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "API-Schlüssel erhalten",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "**Gemini:**\n" +
                          "• Besuchen Sie aistudio.google.com\n" +
                          "• Melden Sie sich an oder erstellen Sie ein Konto\n" +
                          "• Erstellen Sie einen neuen API-Schlüssel\n\n" +
                          "**Perplexity:**\n" +
                          "• Besuchen Sie www.perplexity.ai\n" +
                          "• Gehen Sie zu Settings → API\n" +
                          "• Erstellen Sie einen neuen API-Schlüssel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}