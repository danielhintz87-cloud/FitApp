package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.WeightEntity
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackingScreen(
    onBackPressed: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
    val weights by repo.allWeightsFlow().collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gewicht tracken") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add weight card with background image
            Card {
                Box {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(com.example.fitapp.R.drawable.generated_image_10),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        alpha = 0.3f
                    )
                    
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Neues Gewicht hinzufügen",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Gewicht (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("z.B. 70.5") }
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notizen (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val weightValue = weight.toDoubleOrNull()
                                        if (weightValue != null && weightValue > 0) {
                                            val today = LocalDate.now()
                                            val weightEntity = WeightEntity(
                                                weight = weightValue,
                                                dateIso = today.toString(),
                                                notes = notes.ifBlank { null }
                                            )
                                            
                                            // Check if entry for today already exists
                                            val existing = repo.getWeightByDate(today.toString())
                                            if (existing != null) {
                                                // Update existing entry
                                                repo.updateWeight(weightEntity.copy(id = existing.id))
                                                message = "Gewicht für heute aktualisiert!"
                                            } else {
                                                // Add new entry
                                                repo.saveWeight(weightEntity)
                                                message = "Gewicht erfolgreich hinzugefügt!"
                                            }
                                            
                                            // Track weight logging for streaks
                                            val streakManager = com.example.fitapp.services.PersonalStreakManager(
                                                ctx,
                                                com.example.fitapp.data.repo.PersonalMotivationRepository(AppDatabase.get(ctx))
                                            )
                                            streakManager.trackWeightLogging(today)
                                            
                                            weight = ""
                                            notes = ""
                                        } else {
                                            message = "Bitte gib ein gültiges Gewicht ein."
                                        }
                                    } catch (e: Exception) {
                                        message = "Fehler beim Speichern: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading && weight.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(if (isLoading) "Speichere..." else "Gewicht speichern")
                        }
                        
                        if (message.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (message.contains("Fehler")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Weight history
            if (weights.isNotEmpty()) {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Gewichtsverlauf",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        weights.take(10).forEach { weightEntry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "${weightEntry.weight} kg",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        weightEntry.dateIso,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    if (!weightEntry.notes.isNullOrBlank()) {
                                        Text(
                                            weightEntry.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repo.deleteWeight(weightEntry.id)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                                }
                            }
                            
                            if (weightEntry != weights.last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}