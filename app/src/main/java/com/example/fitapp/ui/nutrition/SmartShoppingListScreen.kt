package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ShoppingItem(
    val id: String,
    val name: String,
    val category: String,
    val quantity: String = "1",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM
)

enum class Priority(val label: String, val color: Color) {
    HIGH("Hoch", Color(0xFFE57373)),
    MEDIUM("Mittel", Color(0xFFFFB74D)),
    LOW("Niedrig", Color(0xFF81C784))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartShoppingListScreen() {
    var shoppingItems by remember { 
        mutableStateOf(
            listOf(
                ShoppingItem("1", "Bananen", "Obst & Gemüse", "6 Stück", false, Priority.HIGH),
                ShoppingItem("2", "Vollkornbrot", "Backwaren", "1 Laib", false, Priority.MEDIUM),
                ShoppingItem("3", "Hähnchenbrust", "Fleisch & Fisch", "500g", false, Priority.HIGH),
                ShoppingItem("4", "Griechischer Joghurt", "Milchprodukte", "2 Becher", true, Priority.MEDIUM),
                ShoppingItem("5", "Quinoa", "Getreide & Hülsenfrüchte", "500g", false, Priority.LOW),
                ShoppingItem("6", "Spinat", "Obst & Gemüse", "1 Bund", false, Priority.MEDIUM)
            )
        )
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Sonstiges") }
    var newItemQuantity by remember { mutableStateOf("1") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }

    val categories = listOf(
        "Obst & Gemüse", "Milchprodukte", "Fleisch & Fisch", 
        "Backwaren", "Getreide & Hülsenfrüchte", "Sonstiges"
    )

    // Stats calculations
    val totalItems = shoppingItems.size
    val completedItems = shoppingItems.count { it.isCompleted }
    val completionPercentage = if (totalItems > 0) (completedItems * 100) / totalItems else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Smart Shopping List",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Hinzufügen")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$totalItems",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gesamt",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$completedItems",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Erledigt",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$completionPercentage%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fortschritt",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shopping List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group items by category
            val groupedItems = shoppingItems.groupBy { it.category }
            
            groupedItems.forEach { (category, items) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(items) { item ->
                    ShoppingListItem(
                        item = item,
                        onToggleComplete = { 
                            shoppingItems = shoppingItems.map { 
                                if (it.id == item.id) it.copy(isCompleted = !it.isCompleted) 
                                else it 
                            }
                        },
                        onDelete = {
                            shoppingItems = shoppingItems.filter { it.id != item.id }
                        }
                    )
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Artikel hinzufügen") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Artikelname") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newItemQuantity,
                        onValueChange = { newItemQuantity = it },
                        label = { Text("Menge") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Category Dropdown would go here
                    Text("Kategorie: $newItemCategory")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            val newItem = ShoppingItem(
                                id = (shoppingItems.size + 1).toString(),
                                name = newItemName,
                                category = newItemCategory,
                                quantity = newItemQuantity,
                                priority = selectedPriority
                            )
                            shoppingItems = shoppingItems + newItem
                            newItemName = ""
                            newItemQuantity = "1"
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = item.priority.color.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = item.priority.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = item.priority.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Löschen",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
