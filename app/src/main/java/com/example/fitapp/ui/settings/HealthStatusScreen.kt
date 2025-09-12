package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitapp.core.health.HealthStatus
import com.example.fitapp.core.health.HealthSummary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthStatusScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: HealthStatusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "API Health Status",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Monitor the health of external service providers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Overall Summary
        uiState.summary?.let { summary ->
            HealthSummaryCard(
                summary = summary,
                onRefresh = viewModel::triggerHealthCheck
            )
        }
        
        // Individual Provider Status
        if (uiState.providerStatuses.isNotEmpty()) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Provider Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    uiState.providerStatuses.forEach { status ->
                        ProviderStatusItem(
                            status = status,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Actions
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = viewModel::triggerHealthCheck,
                        enabled = !uiState.isRefreshing,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Refresh")
                    }
                    
                    OutlinedButton(
                        onClick = viewModel::clearHealthData,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Clear")
                    }
                }
            }
        }
        
        // Error Display
        uiState.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthSummaryCard(
    summary: HealthSummary,
    onRefresh: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusIndicator(status = summary.overallStatus)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = summary.overallStatus.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusStat(
                    label = "Healthy",
                    count = summary.healthyCount,
                    color = Color(0xFF4CAF50)
                )
                StatusStat(
                    label = "Degraded", 
                    count = summary.degradedCount,
                    color = Color(0xFFFF9800)
                )
                StatusStat(
                    label = "Down",
                    count = summary.downCount,
                    color = Color(0xFFF44336)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Last updated
            if (summary.lastUpdated > 0) {
                val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                Text(
                    text = "Last updated: ${formatter.format(Date(summary.lastUpdated))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusStat(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProviderStatusItem(
    status: HealthStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusIndicator(status = status.status)
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.provider,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                status.responseTimeMs?.let { responseTime ->
                    Text(
                        text = "${responseTime}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (status.lastChecked > 0) {
                    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(
                        text = formatter.format(Date(status.lastChecked)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            status.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: HealthStatus.Status) {
    val (icon, color) = when (status) {
        HealthStatus.Status.OK -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        HealthStatus.Status.DEGRADED -> Icons.Default.Warning to Color(0xFFFF9800)
        HealthStatus.Status.DOWN -> Icons.Default.Error to Color(0xFFF44336)
    }
    
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}