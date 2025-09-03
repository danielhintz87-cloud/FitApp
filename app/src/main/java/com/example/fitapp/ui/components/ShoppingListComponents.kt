package com.example.fitapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitapp.services.ShoppingListManager

/**
 * Enhanced Shopping List UI Components
 * Following the design specifications from the problem statement
 */

@Composable
fun ShoppingListHeader(
    stats: ShoppingListManager.ShoppingStats,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Einkaufsübersicht",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShoppingStatItem(
                    label = "Artikel",
                    value = "${stats.purchasedItems}/${stats.totalItems}",
                    icon = Icons.Filled.ShoppingCart
                )
                
                ShoppingStatItem(
                    label = "Fortschritt",
                    value = "${stats.completionPercentage}%",
                    icon = Icons.Filled.CheckCircle
                )
                
                if (stats.estimatedTotal > 0) {
                    ShoppingStatItem(
                        label = "Geschätzt",
                        value = "${String.format("%.2f", stats.estimatedTotal)}€",
                        icon = Icons.Filled.Euro
                    )
                }
                
                if (stats.urgentItems > 0) {
                    ShoppingStatItem(
                        label = "Dringend",
                        value = "${stats.urgentItems}",
                        icon = Icons.Filled.PriorityHigh,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { stats.completionPercentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline
            )
            
            // Clear Completed Button
            if (stats.purchasedItems > 0) {
                OutlinedButton(
                    onClick = onClearCompleted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.ClearAll, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Erledigte löschen (${stats.purchasedItems})")
                }
            }
        }
    }
}

@Composable
private fun ShoppingStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun CategoryHeader(
    category: String,
    itemCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    getCategoryIcon(category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    "$completedCount/$itemCount",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ShoppingListItemRow(
    item: ShoppingListManager.ShoppingListItem,
    onTogglePurchased: (String) -> Unit,
    onRemove: (String) -> Unit,
    onEditQuantity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPurchased) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = item.isPurchased,
                onCheckedChange = { onTogglePurchased(item.id) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(Modifier.width(12.dp))
            
            // Item Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (item.isPurchased) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (item.isPurchased) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Priority indicator
                    when (item.priority) {
                        ShoppingListManager.Priority.URGENT -> {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Filled.PriorityHigh,
                                contentDescription = "Dringend",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        ShoppingListManager.Priority.HIGH -> {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Filled.KeyboardArrowUp,
                                contentDescription = "Hoch",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        else -> { /* Normal or Low priority - no indicator */ }
                    }
                }
                
                // Quantity and Source
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isPurchased) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (item.addedFrom != "Manual") {
                        Spacer(Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                item.addedFrom,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Notes if available
                item.notes?.let { notes ->
                    Text(
                        notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Estimated cost if available
                item.estimatedCost?.let { cost ->
                    Text(
                        "≈ ${String.format("%.2f", cost)}€",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            // Action Buttons
            Row {
                // Edit quantity button
                IconButton(
                    onClick = { onEditQuantity(item.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Menge bearbeiten",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Remove button
                IconButton(
                    onClick = { onRemove(item.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddItemSection(
    onAddItem: (String, String, String) -> Unit,
    onVoiceInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add manually button
            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Hinzufügen")
            }
            
            // Voice input button
            OutlinedButton(
                onClick = onVoiceInput,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sprechen")
            }
        }
    }
    
    if (showAddDialog) {
        AddItemDialog(
            onAdd = { name, quantity, unit ->
                onAddItem(name, quantity, unit)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    onAdd: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("Stück") }
    
    val commonUnits = listOf("Stück", "g", "kg", "ml", "l", "EL", "TL", "Packung", "Dose", "Bund")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Artikel hinzufügen") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Artikel") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Menge") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Einheit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            commonUnits.forEach { unitOption ->
                                DropdownMenuItem(
                                    text = { Text(unitOption) },
                                    onClick = {
                                        unit = unitOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(itemName, quantity, unit) },
                enabled = itemName.isNotBlank()
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun ShoppingListGroupedView(
    categorizedItems: Map<String, List<ShoppingListManager.ShoppingListItem>>,
    onTogglePurchased: (String) -> Unit,
    onRemove: (String) -> Unit,
    onEditQuantity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        categorizedItems.forEach { (category, items) ->
            item {
                CategoryHeader(
                    category = category,
                    itemCount = items.size,
                    completedCount = items.count { it.isPurchased }
                )
            }
            
            items(items, key = { it.id }) { item ->
                ShoppingListItemRow(
                    item = item,
                    onTogglePurchased = onTogglePurchased,
                    onRemove = onRemove,
                    onEditQuantity = onEditQuantity
                )
            }
        }
    }
}

// Helper function to get category icons
private fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Obst & Gemüse" -> Icons.Filled.Agriculture
        "Fleisch & Fisch" -> Icons.Filled.SetMeal
        "Milchprodukte" -> Icons.Filled.LocalDrink
        "Getreide & Backwaren" -> Icons.Filled.Bakery
        "Tiefkühl" -> Icons.Filled.AcUnit
        "Getränke" -> Icons.Filled.LocalBar
        "Gewürze & Kräuter" -> Icons.Filled.Spa
        else -> Icons.Filled.ShoppingBasket
    }
}