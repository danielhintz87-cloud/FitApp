package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alle") }
    var expandedCategory by remember { mutableStateOf(false) }
    
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
        mutableStateListOf<String>().apply { addAll(selectedEquipment) }
    }
    
    Column(Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Geräte auswählen") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                Button(
                    onClick = {
                        onEquipmentChanged(mutableSelectedEquipment.toList())
                        onBackPressed()
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Fertig (${mutableSelectedEquipment.size})")
                }
            }
        )
        
        Column(Modifier.padding(16.dp)) {
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
            
            Spacer(Modifier.height(16.dp))
            
            // Selected Count
            Text(
                "${mutableSelectedEquipment.size} Geräte ausgewählt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Equipment List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
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