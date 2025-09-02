package com.example.fitapp.ui.nutrition.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitapp.ui.nutrition.CookingTimerManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerManagementCard(
    timerManager: CookingTimerManager,
    sessionId: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val activeTimers by timerManager.activeTimers.collectAsState(initial = emptyList())
    var showAddTimer by remember { mutableStateOf(false) }
    
    // Filter timers for this session
    val sessionTimers = activeTimers.filter { it.sessionId == sessionId }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Timer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = { showAddTimer = true }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Timer hinzufügen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (sessionTimers.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessionTimers) { timer ->
                        TimerCard(
                            timer = timer,
                            onStart = {
                                scope.launch {
                                    timerManager.startTimer(timer.id)
                                }
                            },
                            onPause = {
                                scope.launch {
                                    timerManager.pauseTimer(timer.id)
                                }
                            },
                            onStop = {
                                scope.launch {
                                    timerManager.stopTimer(timer.id)
                                }
                            },
                            formatTime = timerManager::formatTime
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Keine Timer aktiv. Tippen Sie auf + um einen Timer hinzuzufügen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Add Timer Dialog
    if (showAddTimer) {
        AddTimerDialog(
            onDismiss = { showAddTimer = false },
            onAddTimer = { name, duration ->
                scope.launch {
                    timerManager.createTimer(sessionId, name, duration)
                    showAddTimer = false
                }
            },
            suggestedTimers = timerManager.getSuggestedTimers()
        )
    }
}

@Composable
private fun TimerCard(
    timer: CookingTimerManager.CookingTimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                timer.isCompleted -> MaterialTheme.colorScheme.errorContainer
                timer.isActive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                timer.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                formatTime(timer.remainingTime),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    timer.isCompleted -> MaterialTheme.colorScheme.error
                    timer.isActive -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!timer.isCompleted) {
                    IconButton(
                        onClick = if (timer.isActive) onPause else onStart,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (timer.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (timer.isActive) "Pausieren" else "Starten",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stoppen",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTimerDialog(
    onDismiss: () -> Unit,
    onAddTimer: (String, Long) -> Unit,
    suggestedTimers: List<Pair<String, Long>>,
    modifier: Modifier = Modifier
) {
    var customName by remember { mutableStateOf("") }
    var customMinutes by remember { mutableStateOf("") }
    var showCustom by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timer hinzufügen") },
        text = {
            Column {
                if (!showCustom) {
                    Text("Vorschläge:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    
                    suggestedTimers.forEach { (name, seconds) ->
                        TextButton(
                            onClick = { onAddTimer(name, seconds) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("$name (${seconds / 60} Min)")
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showCustom = true }
                    ) {
                        Text("Benutzerdefinierten Timer erstellen")
                    }
                } else {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Timer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { customMinutes = it },
                        label = { Text("Minuten") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (showCustom) {
                Button(
                    onClick = {
                        val minutes = customMinutes.toIntOrNull()
                        if (customName.isNotBlank() && minutes != null && minutes > 0) {
                            onAddTimer(customName, minutes * 60L)
                        }
                    },
                    enabled = customName.isNotBlank() && customMinutes.toIntOrNull()?.let { it > 0 } == true
                ) {
                    Text("Hinzufügen")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}