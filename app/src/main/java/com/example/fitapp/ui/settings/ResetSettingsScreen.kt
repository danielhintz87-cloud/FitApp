package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.prefs.UserPreferences
import com.example.fitapp.services.ResetManager
import kotlinx.coroutines.launch

/**
 * Reset & Data Management Settings Screen
 * Provides comprehensive data reset functionality with safety confirmations
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetSettingsScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val userPrefs = remember { com.example.fitapp.data.prefs.UserPreferencesImpl(context) }
    val resetManager = remember { ResetManager(context, db, userPrefs) }
    val scope = rememberCoroutineScope()
    
    // State for reset operations
    val resetProgress by resetManager.resetProgress.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var selectedResetType by remember { mutableStateOf<ResetManager.ResetType?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var resetResult by remember { mutableStateOf<Map<String, Any>?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Daten & Reset",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ResetSectionHeader(
                    title = "Daten-Verwaltung",
                    subtitle = "Verwalte deine App-Daten und setze verschiedene Bereiche zurück"
                )
            }
            
            item {
                ResetOptionCard(
                    title = "Trainings-Daten",
                    description = "Lösche alle Trainingseinheiten, Leistungsdaten und Progressions-Verfolgung",
                    icon = Icons.Filled.FitnessCenter,
                    warningLevel = WarningLevel.MEDIUM,
                    onClick = {
                        selectedResetType = ResetManager.ResetType.WORKOUT_DATA
                        showResetDialog = true
                    }
                )
            }
            
            item {
                ResetOptionCard(
                    title = "Ernährungs-Daten",
                    description = "Lösche alle Mahlzeiten-Logs, Rezepte und Koch-Sessions",
                    icon = Icons.Filled.Restaurant,
                    warningLevel = WarningLevel.MEDIUM,
                    onClick = {
                        selectedResetType = ResetManager.ResetType.NUTRITION_DATA
                        showResetDialog = true
                    }
                )
            }
            
            item {
                ResetOptionCard(
                    title = "Benutzer-Profil",
                    description = "Lösche Gewichtsdaten, BMI-Verlauf und persönliche Einstellungen",
                    icon = Icons.Filled.Person,
                    warningLevel = WarningLevel.MEDIUM,
                    onClick = {
                        selectedResetType = ResetManager.ResetType.USER_PROFILE
                        showResetDialog = true
                    }
                )
            }
            
            item {
                ResetOptionCard(
                    title = "Erfolge & Streaks",
                    description = "Setze alle Erfolge, Streaks und persönliche Rekorde zurück",
                    icon = Icons.Filled.EmojiEvents,
                    warningLevel = WarningLevel.MEDIUM,
                    onClick = {
                        selectedResetType = ResetManager.ResetType.ACHIEVEMENTS
                        showResetDialog = true
                    }
                )
            }
            
            item {
                ResetOptionCard(
                    title = "Einkaufsliste",
                    description = "Lösche alle Einkaufsartikel und setze Kategorien zurück",
                    icon = Icons.Filled.ShoppingCart,
                    warningLevel = WarningLevel.LOW,
                    onClick = {
                        selectedResetType = ResetManager.ResetType.SHOPPING_LIST
                        showResetDialog = true
                    }
                )
            }
            
            item {
                Divider()
            }
            
            item {
                ResetSectionHeader(
                    title = "⚠️ Vollständiger Reset",
                    subtitle = "ACHTUNG: Diese Option löscht ALLE Daten unwiderruflich"
                )
            }
            
            item {
                ResetOptionCard(
                    title = "Kompletter App-Reset",
                    description = "Lösche ALLE Daten und setze die App in den Werkszustand zurück",
                    icon = Icons.Filled.RestartAlt,
                    warningLevel = WarningLevel.CRITICAL,
                    onClick = {
                        selectedResetType = ResetManager.ResetType.COMPLETE_RESET
                        showResetDialog = true
                    }
                )
            }
        }
    }

    // Reset Progress Dialog
    resetProgress?.let { progress ->
        ResetProgressDialog(
            progress = progress,
            onDismiss = {
                if (progress.isCompleted || progress.hasError) {
                    resetManager.clearResetProgress()
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog && selectedResetType != null) {
        ResetConfirmationDialog(
            resetType = selectedResetType!!,
            confirmationMessage = resetManager.getResetConfirmationMessage(selectedResetType!!),
            onConfirm = {
                scope.launch {
                    when (selectedResetType!!) {
                        ResetManager.ResetType.WORKOUT_DATA -> resetManager.resetWorkoutData()
                        ResetManager.ResetType.NUTRITION_DATA -> resetManager.resetNutritionData()
                        ResetManager.ResetType.USER_PROFILE -> resetManager.resetUserProfile()
                        ResetManager.ResetType.ACHIEVEMENTS -> resetManager.resetAchievements()
                        ResetManager.ResetType.SHOPPING_LIST -> resetManager.resetShoppingList()
                        ResetManager.ResetType.COMPLETE_RESET -> resetManager.performCompleteReset()
                        else -> { /* Handle other types */ }
                    }
                }
                showResetDialog = false
                selectedResetType = null
            },
            onDismiss = {
                showResetDialog = false
                selectedResetType = null
            }
        )
    }
}

@Composable
private fun ResetSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ResetOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    warningLevel: WarningLevel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (warningLevel) {
        WarningLevel.LOW -> MaterialTheme.colorScheme.surfaceVariant
        WarningLevel.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
        WarningLevel.CRITICAL -> MaterialTheme.colorScheme.errorContainer
    }
    
    val contentColor = when (warningLevel) {
        WarningLevel.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
        WarningLevel.MEDIUM -> MaterialTheme.colorScheme.onTertiaryContainer
        WarningLevel.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ResetConfirmationDialog(
    resetType: ResetManager.ResetType,
    confirmationMessage: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (resetType == ResetManager.ResetType.COMPLETE_RESET) {
                    Icons.Filled.Warning
                } else {
                    Icons.Filled.Delete
                },
                contentDescription = null,
                tint = if (resetType == ResetManager.ResetType.COMPLETE_RESET) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        },
        title = {
            Text(
                if (resetType == ResetManager.ResetType.COMPLETE_RESET) {
                    "⚠️ VOLLSTÄNDIGER RESET ⚠️"
                } else {
                    "Daten löschen?"
                }
            )
        },
        text = {
            Text(
                confirmationMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (resetType == ResetManager.ResetType.COMPLETE_RESET) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(
                    if (resetType == ResetManager.ResetType.COMPLETE_RESET) {
                        "ALLES LÖSCHEN"
                    } else {
                        "Löschen"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
private fun ResetProgressDialog(
    progress: ResetManager.ResetProgress,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (progress.isCompleted || progress.hasError) onDismiss else { {} },
        icon = {
            if (progress.isCompleted) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else if (progress.hasError) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                when {
                    progress.isCompleted -> "Abgeschlossen"
                    progress.hasError -> "Fehler"
                    else -> progress.operation
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (progress.hasError) {
                    Text(
                        progress.errorMessage ?: "Unbekannter Fehler",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        progress.currentStep,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (!progress.isCompleted) {
                        LinearProgressIndicator(
                            progress = { progress.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            "${(progress.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (progress.isCompleted || progress.hasError) {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        }
    )
}

private enum class WarningLevel {
    LOW, MEDIUM, CRITICAL
}