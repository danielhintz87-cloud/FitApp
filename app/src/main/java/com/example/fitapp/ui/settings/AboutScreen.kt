package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.BuildConfig

@Composable
fun AboutScreen(
    onBack: () -> Unit, 
    contentPadding: PaddingValues = PaddingValues(0.dp),
    versionName: String = BuildConfig.VERSION_NAME
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Über FitApp") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "FitApp",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Version $versionName",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Eine vollständige Fitness-Plattform mit KI-Coach, Ernährungsanalyse und Gesundheitsintegration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Features Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Features",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val features = listOf(
                            "🏋️ Intelligenter Workout-Planer" to "Adaptive Trainingspläne mit KI-Unterstützung",
                            "🍎 Barcode-Scanner" to "Automatische Nährwert-Erkennung mit ML Kit",
                            "🎤 Sprachsteuerung" to "Voice Input für Einkaufslisten und Commands",
                            "📱 Health Connect" to "Synchronisation mit Android Health-Ökosystem",
                            "⏱️ Smart Rest Timer" to "Audio-Coaching zwischen Trainingssätzen",
                            "🔬 Pose Detection" to "MediaPipe-basierte Bewegungsanalyse"
                        )
                        
                        features.forEach { (title, description) ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(0.6f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Tech Stack Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Technologie",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val techItems = listOf(
                            "Kotlin" to "Native Android Development",
                            "Jetpack Compose" to "Moderne UI mit deklarativem Design",
                            "Room Database" to "Lokale Datenpersistierung",
                            "Hilt" to "Dependency Injection Framework",
                            "Proto DataStore" to "Type-safe Preferences Management",
                            "ML Kit" to "On-Device Machine Learning",
                            "MediaPipe" to "Computer Vision für Pose Detection",
                            "Health Connect" to "Android Gesundheitsdaten-API"
                        )
                        
                        techItems.forEach { (tech, description) ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "• $tech",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(0.3f)
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(0.7f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Legal & Open Source
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Open Source & Lizenzen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Diese App ist Open Source und unter der MIT-Lizenz verfügbar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "© 2025 FitApp Contributors",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Build Type: ${BuildConfig.BUILD_TYPE}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
