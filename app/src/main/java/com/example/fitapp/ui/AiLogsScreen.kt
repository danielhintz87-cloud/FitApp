package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitapp.data.db.AiLog
import com.example.fitapp.data.db.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiLogsScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context) }
    val logs by database.aiLogDao().getAll().collectAsState(initial = emptyList())
    
    var filter by remember { mutableStateOf("all") }
    
    Column(Modifier.fillMaxSize()) {
        // Filter tabs
        ScrollableTabRow(
            selectedTabIndex = when(filter) {
                "success" -> 1
                "failed" -> 2
                else -> 0
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = filter == "all",
                onClick = { filter = "all" },
                text = { Text("Alle") }
            )
            Tab(
                selected = filter == "success",
                onClick = { filter = "success" },
                text = { Text("Erfolgreich") }
            )
            Tab(
                selected = filter == "failed",
                onClick = { filter = "failed" },
                text = { Text("Fehler") }
            )
        }
        
        val filteredLogs = when(filter) {
            "success" -> logs.filter { it.success }
            "failed" -> logs.filter { !it.success }
            else -> logs
        }
        
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Keine AI-Logs vorhanden",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "AI-Interaktionen werden automatisch protokolliert",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs) { log ->
                    AiLogCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun AiLogCard(log: AiLog) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (log.success) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.provider,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = log.requestType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (log.success) {
                    Text(
                        text = "${log.duration}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "❌",
                        fontSize = 16.sp
                    )
                }
            }
            
            // Timestamp
            Text(
                text = dateFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Prompt preview
            Text(
                text = "Prompt: ${log.prompt.take(100)}${if (log.prompt.length > 100) "..." else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Response or error
            if (log.success && !log.response.isNullOrBlank()) {
                Text(
                    text = "Response: ${log.response.take(150)}${if (log.response.length > 150) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (!log.success && !log.error.isNullOrBlank()) {
                Text(
                    text = "Error: ${log.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}