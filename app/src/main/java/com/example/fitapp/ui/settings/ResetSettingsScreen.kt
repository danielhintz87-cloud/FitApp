package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitapp.settings.ResetSettingsViewModel
import kotlinx.coroutines.launch

/**
 * Reset & Data Management Settings Screen
 * Provides comprehensive data reset functionality with safety confirmations
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: ResetSettingsViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }
    var selectedResetAction by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Daten & Reset",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "⚠️ Daten Reset Optionen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Diese Aktionen können nicht rückgängig gemacht werden. Bitte mit Vorsicht verwenden.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                ResetOptionCard(
                    title = "Training zurücksetzen",
                    description = "Löscht alle Trainings-Einstellungen",
                    icon = Icons.Default.FitnessCenter,
                    onClick = {
                        selectedResetAction = "workout"
                        showResetDialog = true
                    },
                    isLoading = isLoading,
                )
            }

            item {
                ResetOptionCard(
                    title = "Ernährung zurücksetzen",
                    description = "Löscht alle Ernährungs-Einstellungen",
                    icon = Icons.Default.Restaurant,
                    onClick = {
                        selectedResetAction = "nutrition"
                        showResetDialog = true
                    },
                    isLoading = isLoading,
                )
            }

            item {
                ResetOptionCard(
                    title = "Benutzer zurücksetzen",
                    description = "Löscht alle Benutzer-Einstellungen",
                    icon = Icons.Default.Person,
                    onClick = {
                        selectedResetAction = "user"
                        showResetDialog = true
                    },
                    isLoading = isLoading,
                )
            }

            item {
                ResetOptionCard(
                    title = "Erfolge zurücksetzen",
                    description = "Löscht alle Erfolgs-Einstellungen",
                    icon = Icons.Default.EmojiEvents,
                    onClick = {
                        selectedResetAction = "achievements"
                        showResetDialog = true
                    },
                    isLoading = isLoading,
                )
            }

            item {
                ResetOptionCard(
                    title = "Alles zurücksetzen",
                    description = "Löscht ALLE Einstellungen (nicht empfohlen)",
                    icon = Icons.Default.DeleteSweep,
                    onClick = {
                        selectedResetAction = "all"
                        showResetDialog = true
                    },
                    isLoading = isLoading,
                    isDestructive = true,
                )
            }
        }
    }

    // Confirmation Dialog
    if (showResetDialog && selectedResetAction != null) {
        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
                selectedResetAction = null
            },
            title = {
                Text("Reset bestätigen")
            },
            text = {
                Text(
                    "Sind Sie sicher, dass Sie diese Daten zurücksetzen möchten? Diese Aktion kann nicht rückgängig gemacht werden.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                when (selectedResetAction) {
                                    "workout" -> viewModel.clearWorkout()
                                    "nutrition" -> viewModel.clearNutrition()
                                    "user" -> viewModel.clearUser()
                                    "achievements" -> viewModel.clearAchievements()
                                    "all" -> viewModel.clearAll()
                                }
                            } finally {
                                isLoading = false
                                showResetDialog = false
                                selectedResetAction = null
                            }
                        }
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Text("Reset durchführen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        selectedResetAction = null
                    },
                ) {
                    Text("Abbrechen")
                }
            },
        )
    }
}

@Composable
fun ResetOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isDestructive: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isDestructive) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint =
                        if (isDestructive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    if (isDestructive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(if (isDestructive) "⚠️ Reset" else "Reset")
                }
            }
        }
    }
}
