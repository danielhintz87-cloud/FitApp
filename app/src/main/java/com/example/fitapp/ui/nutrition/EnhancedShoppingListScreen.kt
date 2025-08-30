package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.launch
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedShoppingListScreen(
    padding: PaddingValues = PaddingValues(0.dp),
    onBackPressed: (() -> Unit)? = null
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()
    
    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var sortBySupermarket by remember { mutableStateOf(false) }
    var isListeningForVoice by remember { mutableStateOf(false) }
    var voiceInput by remember { mutableStateOf("") }
    
    // Voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningForVoice = false
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { spokenText ->
            voiceInput = spokenText
            // Process voice input with AI to extract shopping items
            scope.launch {
                try {
                    // Use the new shopping list parsing AI method
                    val aiResponse = AppAi.parseShoppingListWithOptimalProvider(ctx, spokenText).getOrNull()
                    
                    if (aiResponse != null && aiResponse.isNotBlank()) {
                        // Parse the AI response format: "Item1|Menge1\nItem2|Menge2\n..."
                        val lines = aiResponse.split("\n").filter { it.isNotBlank() }
                        lines.forEach { line ->
                            val parts = line.split("|")
                            if (parts.isNotEmpty()) {
                                val itemName = parts[0].trim()
                                val quantity = if (parts.size > 1 && parts[1].isNotBlank()) parts[1].trim() else null
                                
                                if (itemName.isNotBlank()) {
                                    val category = categorizeItem(itemName)
                                    db.shoppingDao().insert(
                                        ShoppingItemEntity(
                                            name = itemName,
                                            quantity = quantity,
                                            unit = null,
                                            category = category
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // If AI parsing fails, fall back to simple parsing
                        val items = spokenText.split(",", "und", "&").map { it.trim() }
                        items.forEach { item ->
                            if (item.isNotBlank()) {
                                val category = categorizeItem(item)
                                db.shoppingDao().insert(
                                    ShoppingItemEntity(
                                        name = item,
                                        quantity = null,
                                        unit = null,
                                        category = category
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback: just add the spoken text as a single item
                    if (spokenText.isNotBlank()) {
                        val category = categorizeItem(spokenText)
                        db.shoppingDao().insert(
                            ShoppingItemEntity(
                                name = spokenText,
                                quantity = null,
                                unit = null,
                                category = category
                            )
                        )
                    }
                }
            }
        }
    }
    
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
        // Top App Bar for better navigation
        if (onBackPressed != null) {
            TopAppBar(
                title = { Text("Einkaufsliste") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { sortBySupermarket = !sortBySupermarket }) {
                        Icon(
                            if (sortBySupermarket) Icons.Filled.Store else Icons.AutoMirrored.Filled.List,
                            contentDescription = "Sortierung"
                        )
                    }
                    IconButton(
                        onClick = {
                            isListeningForVoice = true
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Sagen Sie Ihre Einkaufsliste")
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                            }
                            voiceLauncher.launch(intent)
                        }
                    ) {
                        Icon(
                            if (isListeningForVoice) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Spracheingabe"
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
                    }
                }
            )
        }
        
        Column(Modifier.fillMaxSize().padding(if (onBackPressed != null) PaddingValues(0.dp) else padding)) {
            // Header with actions (only shown when no TopAppBar)
            if (onBackPressed == null) {
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
                                if (sortBySupermarket) Icons.Filled.Store else Icons.AutoMirrored.Filled.List,
                                contentDescription = "Sortierung"
                            )
                        }
                        IconButton(
                            onClick = {
                                isListeningForVoice = true
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Sagen Sie Ihre Einkaufsliste...")
                                }
                                voiceLauncher.launch(intent)
                            }
                        ) {
                            Icon(
                                if (isListeningForVoice) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = "Spracheingabe"
                            )
                        }
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
                        }
                    }
                }
            }
        
        // Voice input indicator
        if (isListeningForVoice) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "🎤 Spracherkennung aktiv - Sagen Sie Ihre Einkaufsliste...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        
        // Voice input result
        if (voiceInput.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🎤 Spracherkennung:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        OutlinedButton(
                            onClick = { voiceInput = "" },
                            modifier = Modifier.size(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("×", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Text(
                        "\"$voiceInput\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Items wurden automatisch hinzugefügt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
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
                    if (sortBySupermarket) Icons.Filled.Store else Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (sortBySupermarket) "Sortiert nach Supermarkt-Layout" else "Sortiert nach Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${items.size} Items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
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
                                        unit = null,
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