package com.example.fitapp.ui.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitapp.ui.settings.CloudSyncSettingsScreen
import com.example.fitapp.ui.settings.CloudSyncSettingsViewModel
import kotlinx.coroutines.launch

/**
 * Demo screen showcasing cloud sync functionality
 * This is a demonstration screen for testing cloud sync features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncDemoScreen() {
    var showSyncSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    if (showSyncSettings) {
        CloudSyncSettingsScreen(
            onNavigateBack = { showSyncSettings = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cloud Sync Demo") },
                    actions = {
                        IconButton(
                            onClick = { showSyncSettings = true }
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = "Sync Settings")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WelcomeCard(onOpenSettings = { showSyncSettings = true })
                
                FeatureShowcaseCard()
                
                QuickActionsCard()
                
                StatusCard()
            }
        }
    }
}

@Composable
private fun WelcomeCard(onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "Cloud Sync für FitApp",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Text(
                "Synchronisieren Sie Ihre Fitnessdaten nahtlos zwischen allen Ihren Geräten. " +
                "Ihre Daten sind end-zu-end verschlüsselt und nur Sie haben Zugriff darauf.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync-Einstellungen öffnen")
            }
        }
    }
}

@Composable
private fun FeatureShowcaseCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Sync-Features",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            
            FeatureItem(
                icon = Icons.Default.EmojiEvents,
                title = "Erfolge & Fortschritt",
                description = "Persönliche Erfolge, Strähnen und Rekorde werden automatisch synchronisiert"
            )
            
            FeatureItem(
                icon = Icons.Default.FitnessCenter,
                title = "Workout-Daten",
                description = "Trainingseinheiten und Leistungsdaten bleiben auf allen Geräten aktuell"
            )
            
            FeatureItem(
                icon = Icons.Default.Restaurant,
                title = "Ernährungsdaten",
                description = "Mahlzeiten, Kalorien und Wasseraufnahme werden geräteübergreifend gespeichert"
            )
            
            FeatureItem(
                icon = Icons.Default.Security,
                title = "Datenschutz",
                description = "Ende-zu-Ende-Verschlüsselung schützt Ihre persönlichen Daten"
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Schnellaktionen",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Trigger manual sync */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync jetzt")
                }
                
                OutlinedButton(
                    onClick = { /* Show sync status */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Status")
                }
            }
        }
    }
}

@Composable
private fun StatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Demo-Status",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                "Dies ist eine Demonstration der Cloud-Sync-Funktionalität. " +
                "Die Sync-Einstellungen sind voll funktionsfähig und verwenden eine lokale Datenbank. " +
                "Der Cloud-Backend ist als Platzhalter implementiert.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Implementierung abgeschlossen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}