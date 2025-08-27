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
    
    var openAiKey by remember { mutableStateOf(ApiKeys.getStoredOpenAiKey(context)) }
    
    var showPassword by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "OpenAI API-Schlüssel",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = "Geben Sie Ihren OpenAI API-Schlüssel ein, um AI-Features zu nutzen. Der Schlüssel wird sicher auf dem Gerät gespeichert.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // OpenAI Key
        OutlinedTextField(
            value = openAiKey,
            onValueChange = { openAiKey = it; saved = false },
            label = { Text("OpenAI API Key") },
            placeholder = { Text("sk-...") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = { Text("Für ChatGPT-basierte Features (gpt-4o-mini)") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { showPassword = !showPassword },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (showPassword) "Ausblenden" else "Anzeigen")
            }

            Button(
                onClick = {
                    ApiKeys.saveOpenAiKey(context, openAiKey.trim())
                    saved = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Speichern")
            }
        }

        if (saved) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "✓ OpenAI API-Schlüssel erfolgreich gespeichert",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hinweise zur Sicherheit",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Der Schlüssel wird nur lokal auf Ihrem Gerät gespeichert\n" +
                          "• Für Produktionsnutzung wird verschlüsselte Speicherung empfohlen\n" +
                          "• Teilen Sie Ihren API-Schlüssel niemals mit anderen\n" +
                          "• Nur OpenAI wird als AI-Provider unterstützt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "OpenAI API-Schlüssel erhalten",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Besuchen Sie platform.openai.com\n" +
                          "• Melden Sie sich an oder erstellen Sie ein Konto\n" +
                          "• Gehen Sie zu 'API Keys' in Ihrem Dashboard\n" +
                          "• Erstellen Sie einen neuen API-Schlüssel\n" +
                          "• Kopieren Sie den Schlüssel und fügen Sie ihn hier ein",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}