package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.prefs.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSelectionScreen(
    @Suppress("UNUSED_PARAMETER") selectedEquipment: List<String>,
    onEquipmentChanged: (List<String>) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    
    // Load current equipment selection
    var currentSelection by remember { 
        mutableStateOf(UserPreferences.getSelectedEquipment(context).toSet()) 
    }
    
    val equipmentOptions = listOf(
        "Hanteln",
        "Langhantel",
        "Kurzhanteln", 
        "Klimmzugstange",
        "Dip-Station",
        "Trainingsbank",
        "Verstellbare Bank",
        "Kettlebells",
        "Widerstandsbänder",
        "TRX/Schlingen",
        "Medizinbälle",
        "Foam Roller",
        "Yoga-Matte",
        "Laufband",
        "Crosstrainer", 
        "Fahrradergometer",
        "Rudergerät",
        "Vollausstattung Fitnessstudio",
        "Heimstudio komplett",
        "Crossfit Equipment",
        "Functional Training Setup"
    )
    
    // Save selection when it changes
    LaunchedEffect(currentSelection) {
        val selectionList = currentSelection.toList()
        UserPreferences.saveSelectedEquipment(context, selectionList)
        onEquipmentChanged(selectionList)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geräteauswahl") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Wähle deine verfügbaren Trainingsgeräte:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "${currentSelection.size} Geräte ausgewählt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(equipmentOptions) { equipment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = currentSelection.contains(equipment),
                                onValueChange = { isSelected ->
                                    currentSelection = if (isSelected) {
                                        currentSelection + equipment
                                    } else {
                                        currentSelection - equipment
                                    }
                                },
                                role = Role.Checkbox
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentSelection.contains(equipment)) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = equipment,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Checkbox(
                                checked = currentSelection.contains(equipment),
                                onCheckedChange = null // handled by toggleable
                            )
                        }
                    }
                }
            }
        }
    }
}
