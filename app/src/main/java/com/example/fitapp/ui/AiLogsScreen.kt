package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AiLog
import com.example.fitapp.data.db.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiLogsScreen() {
    val context = LocalContext.current
    val database = AppDatabase.get(context)
    val logs by database.aiLogDao().getAll().collectAsState(initial = emptyList())
    
    var selectedFilter by remember { mutableStateOf("all") }
    val filters = listOf(
        "all" to "Alle",
        "text" to "Text",
        "vision" to "Vision",
        "plan" to "Pläne",
        "recipe" to "Rezepte"
    )
    
    val filteredLogs = if (selectedFilter == "all") {
        logs
    } else {
        logs.filter { it.requestType == selectedFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "AI Logs",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keine AI-Logs gefunden",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredLogs) { log ->
                AiLogCard(log = log)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiLogCard(log: AiLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (log.success) {
            CardDefaults.cardColors()
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (log.success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = if (log.success) "Erfolg" else "Fehler",
                        tint = if (log.success) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Text(
                        text = "${log.provider} • ${log.requestType}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = formatTimestamp(log.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Confidence score for vision requests
            if (log.requestType == "vision" && log.confidenceScore != null && log.success) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Konfidenz:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = log.confidenceScore,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(log.confidenceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Prompt (truncated)
            if (log.prompt.isNotBlank()) {
                Text(
                    text = "Anfrage: ${log.prompt.take(100)}${if (log.prompt.length > 100) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Response or error
            if (log.success && log.response.isNotBlank()) {
                Text(
                    text = "Antwort: ${log.response.take(200)}${if (log.response.length > 200) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (!log.success && !log.errorMessage.isNullOrBlank()) {
                Text(
                    text = "Fehler: ${log.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}