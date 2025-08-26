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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.ShoppingCategoryEntity
import com.example.fitapp.data.db.ShoppingItemEntity
import kotlinx.coroutines.launch

@Composable
fun ShoppingListScreen() {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()
    
    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var sortBySupermarket by remember { mutableStateOf(false) }
    
    val items by if (sortBySupermarket) {
        db.shoppingDao().itemsFlow().collectAsState(initial = emptyList())
    } else {
        db.shoppingDao().itemsFlowByDate().collectAsState(initial = emptyList())
    }
    
    // Initialize default categories if needed
    LaunchedEffect(Unit) {
        initializeDefaultCategories(db)
    }
    
    val categorizedItems = if (sortBySupermarket) {
        items.groupBy { it.category ?: "Sonstiges" }
            .toSortedMap(compareBy { getSupermarketOrder(it) })
    } else {
        items.groupBy { if (it.checked) "Erledigt" else "Offen" }
    }

    Column(Modifier.fillMaxSize()) {
        // Header with actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Einkaufsliste",
                style = MaterialTheme.typography.titleLarge
            )
            Row {
                IconButton(onClick = { sortBySupermarket = !sortBySupermarket }) {
                    Icon(
                        if (sortBySupermarket) Icons.Filled.Store else Icons.Filled.List,
                        contentDescription = "Sortierung"
                    )
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            db.shoppingDao().deleteCheckedItems()
                        }
                    }
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Erledigte löschen")
                }
            }
        }
        
        // Sort mode indicator
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (sortBySupermarket) Icons.Filled.Store else Icons.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (sortBySupermarket) "Sortiert nach Supermarkt-Layout" else "Sortiert nach Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Shopping List Items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorizedItems.forEach { (category, categoryItems) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(categoryItems) { item ->
                    ShoppingItemCard(
                        item = item,
                        onCheckedChange = { checked ->
                            scope.launch {
                                db.shoppingDao().setChecked(item.id, checked)
                            }
                        },
                        onDelete = {
                            scope.launch {
                                db.shoppingDao().delete(item.id)
                            }
                        }
                    )
                }
                
                item { Spacer(Modifier.height(8.dp)) }
            }
            
            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Einkaufsliste ist leer",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "Fügen Sie Artikel hinzu oder importieren Sie sie aus Rezepten",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Artikel") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newItemQuantity,
                        onValueChange = { newItemQuantity = it },
                        label = { Text("Menge (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            scope.launch {
                                val category = categorizeItem(newItemName)
                                db.shoppingDao().insert(
                                    ShoppingItemEntity(
                                        name = newItemName,
                                        quantity = newItemQuantity.takeIf { it.isNotBlank() },
                                        category = category
                                    )
                                )
                                newItemName = ""
                                newItemQuantity = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItemEntity,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = onCheckedChange
            )
            
            Spacer(Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                    color = if (item.checked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                
                if (item.quantity != null) {
                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (item.fromRecipeId != null) {
                    Text(
                        text = "Aus Rezept",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Löschen",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private suspend fun initializeDefaultCategories(db: AppDatabase) {
    val existingCategories = db.shoppingCategoryDao().getCategories()
    if (existingCategories.isEmpty()) {
        val defaultCategories = listOf(
            ShoppingCategoryEntity("Obst & Gemüse", 1),
            ShoppingCategoryEntity("Fleisch & Fisch", 2),
            ShoppingCategoryEntity("Milchprodukte", 3),
            ShoppingCategoryEntity("Backwaren", 4),
            ShoppingCategoryEntity("Konserven", 5),
            ShoppingCategoryEntity("Tiefkühl", 6),
            ShoppingCategoryEntity("Getränke", 7),
            ShoppingCategoryEntity("Süßwaren", 8),
            ShoppingCategoryEntity("Haushalt", 9),
            ShoppingCategoryEntity("Sonstiges", 10)
        )
        
        defaultCategories.forEach {
            db.shoppingCategoryDao().insert(it)
        }
    }
}

private fun categorizeItem(itemName: String): String {
    val name = itemName.lowercase()
    return when {
        name.contains("apfel") || name.contains("banane") || name.contains("tomate") || 
        name.contains("zwiebel") || name.contains("karotte") || name.contains("salat") -> "Obst & Gemüse"
        
        name.contains("fleisch") || name.contains("hähnchen") || name.contains("fisch") || 
        name.contains("lachs") || name.contains("rind") || name.contains("schwein") -> "Fleisch & Fisch"
        
        name.contains("milch") || name.contains("joghurt") || name.contains("käse") || 
        name.contains("butter") || name.contains("quark") -> "Milchprodukte"
        
        name.contains("brot") || name.contains("brötchen") || name.contains("mehl") || 
        name.contains("zucker") -> "Backwaren"
        
        name.contains("dose") || name.contains("konserve") || name.contains("reis") || 
        name.contains("nudeln") || name.contains("pasta") -> "Konserven"
        
        name.contains("tiefkühl") || name.contains("eis") -> "Tiefkühl"
        
        name.contains("wasser") || name.contains("saft") || name.contains("cola") || 
        name.contains("bier") || name.contains("wein") -> "Getränke"
        
        name.contains("schokolade") || name.contains("keks") || name.contains("süß") -> "Süßwaren"
        
        name.contains("waschmittel") || name.contains("seife") || name.contains("papier") -> "Haushalt"
        
        else -> "Sonstiges"
    }
}

private fun getSupermarketOrder(category: String): Int {
    return when (category) {
        "Obst & Gemüse" -> 1
        "Fleisch & Fisch" -> 2
        "Milchprodukte" -> 3
        "Backwaren" -> 4
        "Konserven" -> 5
        "Tiefkühl" -> 6
        "Getränke" -> 7
        "Süßwaren" -> 8
        "Haushalt" -> 9
        "Sonstiges" -> 10
        "Offen" -> 1
        "Erledigt" -> 2
        else -> 999
    }
}