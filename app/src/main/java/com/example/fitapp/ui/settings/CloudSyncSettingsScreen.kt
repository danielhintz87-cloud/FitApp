package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncSettingsScreen(
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: CloudSyncSettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud-Synchronisation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.triggerManualSync()
                            }
                        },
                        enabled = uiState.isSignedIn && !uiState.isSyncing,
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync jetzt")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // User Account Section
            UserAccountSection(
                uiState = uiState,
                onSignIn = { email ->
                    scope.launch {
                        viewModel.signIn(email)
                    }
                },
                onSignOut = {
                    scope.launch {
                        viewModel.signOut()
                    }
                },
            )

            if (uiState.isSignedIn) {
                // Sync Status Section
                SyncStatusSection(uiState = uiState)

                // Sync Preferences Section
                SyncPreferencesSection(
                    preferences = uiState.syncPreferences,
                    onPreferenceChanged = { key, value ->
                        scope.launch {
                            viewModel.updateSyncPreference(key, value)
                        }
                    },
                )

                // Conflicts Section
                if (uiState.pendingConflicts > 0) {
                    ConflictsSection(
                        conflictCount = uiState.pendingConflicts,
                        onResolveConflicts = {
                            scope.launch {
                                viewModel.navigateToConflicts()
                            }
                        },
                    )
                }

                // Device Management Section
                DeviceManagementSection(uiState = uiState)

                // Privacy & Security Section
                PrivacySecuritySection(uiState = uiState)
            }
        }
    }
}

@Composable
private fun UserAccountSection(
    uiState: CloudSyncUiState,
    onSignIn: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Benutzerkonto",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                )
            }

            if (uiState.isSignedIn) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Angemeldet als:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        uiState.userEmail ?: "Unbekannt",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        "Gerät: ${uiState.deviceName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abmelden")
                    }
                }
            } else {
                var email by remember { mutableStateOf("") }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Melden Sie sich an, um Ihre Daten geräteübergreifend zu synchronisieren.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-Mail") },
                        placeholder = { Text("ihre@email.de") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Button(
                        onClick = { onSignIn(email) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank() && !uiState.isSigningIn,
                    ) {
                        if (uiState.isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Anmelden")
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusSection(uiState: CloudSyncUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Sync,
                    contentDescription = null,
                    tint =
                        if (uiState.isSyncing) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Text(
                    "Sync-Status",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Letzte Synchronisation:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        uiState.lastSyncTime ?: "Nie",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                if (uiState.isSyncing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            "Synchronisiert...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Synchronisiert",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncPreferencesSection(
    preferences: Map<String, Boolean>,
    onPreferenceChanged: (String, Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Sync-Einstellungen",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                "Wählen Sie, welche Daten synchronisiert werden sollen:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SyncPreferenceItem(
                title = "Erfolge & Fortschritt",
                description = "Persönliche Erfolge, Strähnen und Rekorde",
                icon = Icons.Default.EmojiEvents,
                checked = preferences["sync_achievements"] ?: true,
                onCheckedChange = { onPreferenceChanged("sync_achievements", it) },
            )

            SyncPreferenceItem(
                title = "Workouts",
                description = "Trainingseinheiten und Leistungsdaten",
                icon = Icons.Default.FitnessCenter,
                checked = preferences["sync_workouts"] ?: true,
                onCheckedChange = { onPreferenceChanged("sync_workouts", it) },
            )

            SyncPreferenceItem(
                title = "Ernährung",
                description = "Mahlzeiten, Kalorien und Wasseraufnahme",
                icon = Icons.Default.Restaurant,
                checked = preferences["sync_nutrition"] ?: true,
                onCheckedChange = { onPreferenceChanged("sync_nutrition", it) },
            )

            SyncPreferenceItem(
                title = "Gewicht & BMI",
                description = "Gewichtsverlauf und BMI-Daten",
                icon = Icons.Default.MonitorHeart, // Using heart monitor as weight alternative
                checked = preferences["sync_weight"] ?: true,
                onCheckedChange = { onPreferenceChanged("sync_weight", it) },
            )

            SyncPreferenceItem(
                title = "App-Einstellungen",
                description = "Benachrichtigungen und Präferenzen",
                icon = Icons.Default.Tune,
                checked = preferences["sync_settings"] ?: true,
                onCheckedChange = { onPreferenceChanged("sync_settings", it) },
            )
        }
    }
}

@Composable
private fun SyncPreferenceItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ConflictsSection(
    conflictCount: Int,
    onResolveConflicts: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    "Konflikte",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            Text(
                "$conflictCount Konflikte benötigen Ihre Aufmerksamkeit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            Button(
                onClick = onResolveConflicts,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Icon(Icons.Default.Build, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Konflikte lösen")
            }
        }
    }
}

@Composable
private fun DeviceManagementSection(uiState: CloudSyncUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Devices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Geräte-Verwaltung",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Dieses Gerät:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        uiState.deviceName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                OutlinedButton(
                    onClick = { /* Navigate to device management */ },
                ) {
                    Text("Alle Geräte")
                }
            }
        }
    }
}

@Composable
private fun PrivacySecuritySection(uiState: CloudSyncUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Datenschutz & Sicherheit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecurityFeatureItem(
                    title = "Ende-zu-Ende-Verschlüsselung",
                    description = "Ihre Daten sind verschlüsselt und nur Sie können sie lesen",
                    icon = Icons.Default.Lock,
                    enabled = true,
                )

                SecurityFeatureItem(
                    title = "Lokale Datenspeicherung",
                    description = "Daten bleiben lokal verfügbar auch ohne Internet",
                    icon = Icons.Default.Storage,
                    enabled = true,
                )

                SecurityFeatureItem(
                    title = "DSGVO-konform",
                    description = "Vollständig konform mit europäischen Datenschutzbestimmungen",
                    icon = Icons.Default.VerifiedUser,
                    enabled = true,
                )
            }
        }
    }
}

@Composable
private fun SecurityFeatureItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint =
                if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            if (enabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (enabled) "Aktiviert" else "Deaktiviert",
            tint =
                if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}
