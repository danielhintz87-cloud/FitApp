package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.network.healthconnect.HealthConnectManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.services.HealthConnectSyncWorker
import com.example.fitapp.util.UrlOpener
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectSettingsScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: HealthConnectSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val healthConnectManager = remember { HealthConnectManager(context) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Permission launcher using PermissionController contract
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        scope.launch {
            viewModel.onPermissionsResult(granted)
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadHealthConnectStatus()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Connect") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Connect Status
            HealthConnectStatusCard(
                isAvailable = uiState.isAvailable,
                hasPermissions = uiState.hasPermissions,
                onRequestPermissions = {
                    if (uiState.isAvailable) {
                        viewModel.requestPermissions()
                        val permissionController = healthConnectManager.getPermissionController()
                        if (permissionController != null) {
                            permissionLauncher.launch(
                                HealthConnectManager.REQUIRED_PERMISSIONS
                            )
                        }
                    }
                }
            )
            
            if (uiState.isAvailable && uiState.hasPermissions) {
                // Sync Settings
                HealthConnectSyncCard(
                    syncEnabled = uiState.syncEnabled,
                    lastSyncTime = uiState.lastSyncTime,
                    syncStatus = uiState.syncStatus,
                    isSyncing = uiState.isSyncing,
                    onSyncEnabledChange = { enabled ->
                        viewModel.toggleSyncEnabled(enabled)
                        if (enabled) {
                            HealthConnectSyncWorker.schedulePeriodicSync(context)
                        } else {
                            HealthConnectSyncWorker.cancelSync(context)
                        }
                    },
                    onManualSync = {
                        viewModel.triggerSync()
                    }
                )
                
                // Data Sources
                DataSourcesCard()
                
                // Privacy Settings
                PrivacySettingsCard()
            }
            
            // Help & Info
            HelpInfoCard()
            
            // Error handling
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    // Show error message
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearError()
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

// Helper functions removed - now using DataStore via ViewModel

@Composable
private fun HealthConnectStatusCard(
    isAvailable: Boolean,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Health Connect Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            StatusRow(
                label = "Health Connect verfügbar",
                status = isAvailable,
                statusText = if (isAvailable) "Verfügbar" else "Nicht verfügbar"
            )
            
            StatusRow(
                label = "Berechtigungen erteilt",
                status = hasPermissions,
                statusText = if (hasPermissions) "Erteilt" else "Nicht erteilt"
            )
            
            if (isAvailable && !hasPermissions) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Security, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Berechtigungen anfordern")
                }
            }
        }
    }
}

@Composable
private fun HealthConnectSyncCard(
    syncEnabled: Boolean,
    lastSyncTime: String?,
    syncStatus: String,
    isSyncing: Boolean = false,
    onSyncEnabledChange: (Boolean) -> Unit,
    onManualSync: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Synchronisation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Automatische Synchronisation",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Daten alle 15 Minuten synchronisieren",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = syncEnabled,
                    onCheckedChange = onSyncEnabledChange
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Letzte Synchronisation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = lastSyncTime ?: "Noch nie",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = syncStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onManualSync,
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (isSyncing) "Synchronisiere..." else "Jetzt synchronisieren")
                }
            }
        }
    }
}

@Composable
private fun DataSourcesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Source,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Datenquellen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            DataSourceRow("Schritte", true, "Google Fit, Samsung Health")
            DataSourceRow("Herzfrequenz", false, "Nicht verfügbar")
            DataSourceRow("Gewicht", true, "Manuell eingegeben")
            DataSourceRow("Workouts", true, "FitApp, Google Fit")
            DataSourceRow("Kalorien", true, "Berechnet")
        }
    }
}

@Composable
private fun PrivacySettingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Datenschutz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "Deine Gesundheitsdaten werden nur lokal verarbeitet und mit deiner ausdrücklichen Zustimmung mit Health Connect geteilt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { 
                    val context = LocalContext.current
                    UrlOpener.openPrivacyPolicy(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Policy, null)
                Spacer(Modifier.width(8.dp))
                Text("Datenschutzerklärung anzeigen")
            }
        }
    }
}

@Composable
private fun HelpInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Help,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Hilfe & Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "Health Connect ermöglicht es, Gesundheitsdaten zwischen verschiedenen Apps zu teilen. " +
                        "Deine FitApp kann so Schritte, Herzfrequenz und andere Daten automatisch importieren.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { 
                    // Open Health Connect help documentation
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://developer.android.com/health-and-fitness/guides/health-connect"))
                    val context = LocalContext.current
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, null)
                Spacer(Modifier.width(8.dp))
                Text("Weitere Informationen")
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    status: Boolean,
    statusText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (status) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (status) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (status) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DataSourceRow(
    dataType: String,
    isEnabled: Boolean,
    source: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = dataType,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = source,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
    }
}