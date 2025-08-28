package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.prefs.UserPreferences

data class EquipmentItem(
    val name: String,
    val category: String,
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSelectionScreen(
    selectedEquipment: List<String>,
    onEquipmentChanged: (List<String>) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alle") }
    var expandedCategory by remember { mutableStateOf(false) }
    
    // Load equipment from persistent storage initially
    val persistentEquipment = remember { UserPreferences.getSelectedEquipment(context) }
    val initialEquipment = if (selectedEquipment.isEmpty()) persistentEquipment else selectedEquipment
    
    val categories = listOf(
        "Alle", "Krafttraining", "Cardio", "Funktionell", "Körpergewicht", "Zubehör"
    )
    
    val allEquipment = listOf(
        // Krafttraining
        EquipmentItem("Langhantel", "Krafttraining", "Für schwere Grundübungen"),
        EquipmentItem("Kurzhanteln", "Krafttraining", "Für einseitige und Isolationsübungen"),
        EquipmentItem("Hantelscheiben", "Krafttraining", "Verschiedene Gewichte"),
        EquipmentItem("Klimmzugstange", "Krafttraining", "Für Zugübungen des Oberkörpers"),
        EquipmentItem("Dipstange", "Krafttraining", "Für Druckübungen des Oberkörpers"),
        EquipmentItem("Hantelbank", "Krafttraining", "Verstellbare Trainingsbank"),
        EquipmentItem("Power Rack", "Krafttraining", "Sicherheit für schwere Übungen"),
        EquipmentItem("Kabelzugturm", "Krafttraining", "Vielseitige Zugübungen"),
        
        // Cardio
        EquipmentItem("Laufband", "Cardio", "Für Indoor-Lauftraining"),
        EquipmentItem("Crosstrainer", "Cardio", "Ganzkörper-Cardiotraining"),
        EquipmentItem("Fahrradergometer", "Cardio", "Stationäres Fahrradtraining"),
        EquipmentItem("Rudergerät", "Cardio", "Ganzkörper-Ausdauertraining"),
        EquipmentItem("Stepper", "Cardio", "Für Beintraining und Ausdauer"),
        
        // Funktionell
        EquipmentItem("Kettlebells", "Funktionell", "Für dynamische Ganzkörperübungen"),
        EquipmentItem("TRX Suspension Trainer", "Funktionell", "Körpergewichtstraining mit Gurten"),
        EquipmentItem("Medizinball", "Funktionell", "Für explosive Bewegungen"),
        EquipmentItem("Battle Ropes", "Funktionell", "Hochintensives Kardio-Training"),
        EquipmentItem("Bosu Ball", "Funktionell", "Für Balance und Stabilität"),
        EquipmentItem("Agility Ladder", "Funktionell", "Für Schnelligkeitstraining"),
        EquipmentItem("Plyobox", "Funktionell", "Für Sprungkrafttraining"),
        
        // Körpergewicht
        EquipmentItem("Yoga Matte", "Körpergewicht", "Für Bodenübungen"),
        EquipmentItem("Parallettes", "Körpergewicht", "Für erweiterte Körpergewichtsübungen"),
        EquipmentItem("Ab Wheel", "Körpergewicht", "Für Bauchmuskeltraining"),
        
        // Zubehör
        EquipmentItem("Widerstandsbänder", "Zubehör", "Verschiedene Widerstände"),
        EquipmentItem("Gymnastikmatte", "Zubehör", "Für Stretching und Yoga"),
        EquipmentItem("Foam Roller", "Zubehör", "Für Regeneration und Massage"),
        EquipmentItem("Gewichtsgürtel", "Zubehör", "Für schwere Grundübungen"),
        EquipmentItem("Handschuhe", "Zubehör", "Für besseren Grip"),
        EquipmentItem("Springdeil", "Zubehör", "Für Cardio-Training")
    )
    
    val filteredEquipment = remember(selectedCategory, searchQuery) {
        allEquipment.filter { equipment ->
            val matchesCategory = selectedCategory == "Alle" || equipment.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() || 
                              equipment.name.contains(searchQuery, ignoreCase = true) ||
                              equipment.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }
    
    val mutableSelectedEquipment = remember { 
        mutableStateListOf<String>().apply { addAll(initialEquipment) }
    }
    
    val saveAndExit = {
        val newEquipment = mutableSelectedEquipment.toList()
        // Save to persistent storage
        UserPreferences.saveSelectedEquipment(context, newEquipment)
        // Also notify the callback
        onEquipmentChanged(newEquipment)
        onBackPressed()
    }

    BackHandler { saveAndExit() }

    Column(Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Geräte auswählen") },
            navigationIcon = {
                IconButton(onClick = saveAndExit) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                Button(
                    onClick = saveAndExit,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Fertig (${mutableSelectedEquipment.size})")
                }
            }
        )
        
        // Search and Filter Section
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Suche") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Category Filter
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Kategorie") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expandedCategory = false
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Selected Count
            Text(
                "${mutableSelectedEquipment.size} Geräte ausgewählt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Equipment List - Now properly fills remaining space and allows full scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f), // This ensures LazyColumn takes remaining space
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                bottom = 16.dp + 48.dp // Extra bottom padding to ensure scrolling to last item
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredEquipment, key = { it.name }) { equipment ->
                EquipmentSelectionCard(
                    equipment = equipment,
                    isSelected = equipment.name in mutableSelectedEquipment,
                    onSelectionChange = { selected ->
                        if (selected) {
                            mutableSelectedEquipment.add(equipment.name)
                        } else {
                            mutableSelectedEquipment.remove(equipment.name)
                        }
                    }
                )
            }
        }

        Button(
            onClick = saveAndExit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Auswahl speichern")
        }
    }
}

@Composable
private fun EquipmentSelectionCard(
    equipment: EquipmentItem,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    ElevatedCard(
        onClick = { onSelectionChange(!isSelected) },
        colors = if (isSelected) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.elevatedCardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = equipment.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = equipment.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (equipment.description.isNotBlank()) {
                    Text(
                        text = equipment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Ausgewählt",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}