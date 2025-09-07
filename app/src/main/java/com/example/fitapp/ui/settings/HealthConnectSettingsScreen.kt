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
import androidx.navigation.NavController
import com.example.fitapp.network.healthconnect.HealthConnectManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import com.example.fitapp.data.prefs.UserPreferencesFactory
import com.example.fitapp.services.HealthConnectSyncWorker
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectSettingsScreen(
    navController: NavController,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val healthConnectManager = remember { HealthConnectManager(context) }
    
    var isAvailable by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
    var syncEnabled by remember { mutableStateOf(false) }
    var lastSyncTime by remember { mutableStateOf<String?>(null) }
    var syncStatus by remember { mutableStateOf("Nicht synchronisiert") }
    
    LaunchedEffect(Unit) {
        isAvailable = healthConnectManager.isAvailable()
        hasPermissions = healthConnectManager.hasPermissions()
        // Load last sync from preferences (DataStore fallback)
        lastSyncTime = loadLastSync(context)
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Connect") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                isAvailable = isAvailable,
                hasPermissions = hasPermissions,
                onRequestPermissions = {
                    scope.launch {
                        // Use the permission controller directly 
                        val permissionController = healthConnectManager.getPermissionController()
                        if (permissionController != null) {
                            // Request all required permissions
                            permissionLauncher.launch(
                                com.example.fitapp.network.healthconnect.HealthConnectManager.REQUIRED_PERMISSIONS
                                    .map { it.toString() }
                                    .toTypedArray()
                            )
                        }
                    }
                }
            )
            
            if (isAvailable && hasPermissions) {
                // Sync Settings
                HealthConnectSyncCard(
                    syncEnabled = syncEnabled,
                    lastSyncTime = lastSyncTime,
                    syncStatus = syncStatus,
                    onSyncEnabledChange = { enabled ->
                        syncEnabled = enabled
                        if (enabled) {
                            HealthConnectSyncWorker.schedulePeriodicSync(context)
                        } else {
                            HealthConnectSyncWorker.cancelSync(context)
                        }
                    },
                    onManualSync = {
                        scope.launch {
                            syncStatus = "Synchronisiere..."
                            try {
                                HealthConnectSyncWorker.triggerImmediateSync(context)
                                val syncTime = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                                lastSyncTime = syncTime
                                saveLastSync(context, syncTime)
                                syncStatus = "Erfolgreich synchronisiert"
                            } catch (e: Exception) {
                                syncStatus = "Synchronisation fehlgeschlagen"
                            }
                        }
                    }
                )
                
                // Data Sources
                DataSourcesCard()
                
                // Privacy Settings
                PrivacySettingsCard()
            }
            
            // Help & Info
            HelpInfoCard()
        }
    }
}

private suspend fun saveLastSync(context: Context, value: String) {
    // Kurzer einfacher SP Fallback bis eigener DataStore Key modelliert ist
    val prefs = context.getSharedPreferences("health_connect_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("last_sync", value).apply()
}

private fun loadLastSync(context: Context): String {
    val prefs = context.getSharedPreferences("health_connect_prefs", Context.MODE_PRIVATE)
    return prefs.getString("last_sync", "Noch nie") ?: "Noch nie"
}

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
                
                Button(onClick = onManualSync) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Jetzt synchronisieren")
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
                onClick = { /* TODO: Open privacy policy */ },
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
                onClick = { /* TODO: Open help */ },
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