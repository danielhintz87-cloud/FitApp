package com.example.fitapp.ui.nutrition

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.fitapp.ai.AppAi
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.ShoppingCategoryEntity
import com.example.fitapp.data.db.ShoppingItemEntity
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.services.ShoppingListManager
import com.example.fitapp.services.ShoppingListSorter
import com.example.fitapp.ui.components.AutoCompleteTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedShoppingListScreen(
    padding: PaddingValues = PaddingValues(0.dp),
    onBackPressed: (() -> Unit)? = null,
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val preferencesRepository = remember { UserPreferencesRepository(ctx) }
    val shoppingManager = remember { ShoppingListManager(db, preferencesRepository) }
    val scope = rememberCoroutineScope()

    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var isListeningForVoice by remember { mutableStateOf(false) }
    var voiceInput by remember { mutableStateOf("") }
    var showSortingDialog by remember { mutableStateOf(false) }

    // AutoComplete state
    var autoCompleteInput by remember { mutableStateOf("") }
    val autoCompleteSuggestions by shoppingManager.autoCompleteSuggestions.collectAsState()

    // Use ShoppingListManager's reactive state
    val currentSortingMode by shoppingManager.sortingMode.collectAsState()
    val categorizedItems by shoppingManager.categorizedItems.collectAsState()

    // Debounced search for autocomplete
    LaunchedEffect(autoCompleteInput) {
        if (autoCompleteInput.length >= 2) {
            delay(300) // Debounce search
            scope.launch {
                shoppingManager.getAutoCompleteSuggestions(autoCompleteInput)
            }
        } else {
            shoppingManager.clearAutoCompleteSuggestions()
        }
    }

    // Voice recognition launcher
    val voiceLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
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
                                                category = category,
                                            ),
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
                                            category = category,
                                        ),
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
                                    category = category,
                                ),
                            )
                        }
                    }
                }
            }
        }

    val items by shoppingManager.shoppingItems.collectAsState()

    // Show smart suggestions when list is empty
    var showSmartSuggestions by remember { mutableStateOf(true) }

    LaunchedEffect(items.size) {
        showSmartSuggestions = items.isEmpty()
    }

    // Initialize default categories if needed
    LaunchedEffect(Unit) {
        initializeDefaultCategories(db)
    }

    // Remove the manual categorization - use manager's categorizedItems instead

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
                    IconButton(onClick = { showSortingDialog = true }) {
                        Icon(
                            Icons.Filled.Sort,
                            contentDescription = "Sortierung",
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                shoppingManager.clearAllItems()
                            }
                        },
                    ) {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = "Alle löschen",
                        )
                    }
                    IconButton(
                        onClick = {
                            isListeningForVoice = true
                            val intent =
                                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                                    )
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Sagen Sie Ihre Einkaufsliste")
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                                }
                            voiceLauncher.launch(intent)
                        },
                    ) {
                        Icon(
                            if (isListeningForVoice) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Spracheingabe",
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
                    }
                },
            )
        }

        Column(Modifier.fillMaxSize().padding(if (onBackPressed != null) PaddingValues(0.dp) else padding)) {
            // Header with actions (only shown when no TopAppBar)
            if (onBackPressed == null) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Einkaufsliste",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Row {
                        IconButton(onClick = { showSortingDialog = true }) {
                            Icon(
                                Icons.Filled.Sort,
                                contentDescription = "Sortierung",
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    shoppingManager.clearAllItems()
                                }
                            },
                        ) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                contentDescription = "Alle löschen",
                            )
                        }
                        IconButton(
                            onClick = {
                                isListeningForVoice = true
                                val intent =
                                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                                        )
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Sagen Sie Ihre Einkaufsliste...")
                                    }
                                voiceLauncher.launch(intent)
                            },
                        ) {
                            Icon(
                                if (isListeningForVoice) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = "Spracheingabe",
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "🎤 Spracherkennung aktiv - Sagen Sie Ihre Einkaufsliste...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Quick Add Section with AutoComplete
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Artikel hinzufügen",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    AutoCompleteTextField(
                        value = autoCompleteInput,
                        onValueChange = { autoCompleteInput = it },
                        suggestions = autoCompleteSuggestions,
                        onSuggestionSelected = { suggestion ->
                            scope.launch {
                                shoppingManager.addItemFromSuggestion(suggestion)
                                autoCompleteInput = ""
                            }
                        },
                        label = "Produkt eingeben",
                        placeholder = "z.B. Milch, Äpfel, Brot...",
                        leadingIcon = {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                        },
                        trailingIcon = {
                            if (autoCompleteInput.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            if (autoCompleteInput.isNotBlank()) {
                                                // Add as manual item
                                                val category = categorizeItem(autoCompleteInput)
                                                db.shoppingDao().insert(
                                                    ShoppingItemEntity(
                                                        name = autoCompleteInput,
                                                        quantity = null,
                                                        unit = null,
                                                        category = category,
                                                    ),
                                                )
                                                // Record usage for learning
                                                shoppingManager.recordItemUsage(autoCompleteInput)
                                                autoCompleteInput = ""
                                            }
                                        }
                                    },
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
                                }
                            }
                        },
                    )

                    if (autoCompleteSuggestions.isNotEmpty()) {
                        Text(
                            "💡 ${autoCompleteSuggestions.size} intelligente Vorschläge verfügbar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            // Smart Suggestions (when list is empty)
            if (showSmartSuggestions && items.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "💡 Beliebte Artikel schnell hinzufügen",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )

                        val commonItems = listOf("Milch", "Brot", "Eier", "Butter", "Äpfel", "Bananen")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                        ) {
                            items(commonItems) { itemName ->
                                SuggestionChip(
                                    onClick = {
                                        scope.launch {
                                            val category = categorizeItem(itemName)
                                            db.shoppingDao().insert(
                                                ShoppingItemEntity(
                                                    name = itemName,
                                                    quantity = null,
                                                    unit = null,
                                                    category = category,
                                                ),
                                            )
                                            shoppingManager.recordItemUsage(itemName)
                                        }
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            getItemIcon(itemName)?.let { icon ->
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                                Spacer(Modifier.width(4.dp))
                                            }
                                            Text(
                                                itemName,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (voiceInput.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "🎤 Spracherkennung:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            OutlinedButton(
                                onClick = { voiceInput = "" },
                                modifier = Modifier.size(32.dp),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text("×", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Text(
                            "\"$voiceInput\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "Items wurden automatisch hinzugefügt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Sort mode indicator
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        getSortModeIcon(currentSortingMode),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        getSortModeDescription(currentSortingMode),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${items.size} Items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Shopping List Items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categorizedItems.forEach { (category, categoryItems) ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    items(categoryItems) { item ->
                        ShoppingItemCard(
                            item = item,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    shoppingManager.markIngredientAsPurchased(item.id, checked)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    shoppingManager.removeIngredient(item.id)
                                }
                            },
                        )
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (items.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Einkaufsliste ist leer",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                                Text(
                                    "Fügen Sie Artikel hinzu oder importieren Sie sie aus Rezepten",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                                Spacer(Modifier.height(16.dp))
                                // Add a prominent button for adding items when list is empty
                                Button(
                                    onClick = { showAddDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Ersten Artikel hinzufügen")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sorting Mode Dialog
        if (showSortingDialog) {
            AlertDialog(
                onDismissRequest = { showSortingDialog = false },
                title = { Text("Sortierung wählen") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShoppingListSorter.SortingMode.values().forEach { mode ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch {
                                                shoppingManager.changeSortingMode(mode)
                                                showSortingDialog = false
                                            }
                                        }
                                        .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = currentSortingMode == mode,
                                    onClick = {
                                        scope.launch {
                                            shoppingManager.changeSortingMode(mode)
                                            showSortingDialog = false
                                        }
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    getSortModeIcon(mode),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        getSortModeTitle(mode),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        getSortModeDescription(mode),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSortingDialog = false }) {
                        Text("OK")
                    }
                },
            )
        }

        // Add Item Dialog with AutoComplete
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    newItemName = ""
                    newItemQuantity = ""
                },
                title = { Text("Artikel hinzufügen") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AutoCompleteTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            suggestions = autoCompleteSuggestions,
                            onSuggestionSelected = { suggestion ->
                                newItemName = suggestion.text
                                // Auto-dismiss suggestions after selection
                                scope.launch {
                                    delay(100)
                                    shoppingManager.clearAutoCompleteSuggestions()
                                }
                            },
                            label = "Artikel",
                            placeholder = "Produktname eingeben...",
                        )

                        OutlinedTextField(
                            value = newItemQuantity,
                            onValueChange = { newItemQuantity = it },
                            label = { Text("Menge (optional)") },
                            placeholder = { Text("z.B. 2 kg, 500g, 1 Liter") },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (newItemName.length >= 2) {
                            LaunchedEffect(newItemName) {
                                delay(300)
                                shoppingManager.getAutoCompleteSuggestions(newItemName)
                            }
                        }
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
                                            category = category,
                                        ),
                                    )
                                    // Record usage for learning
                                    shoppingManager.recordItemUsage(newItemName)
                                    newItemName = ""
                                    newItemQuantity = ""
                                    showAddDialog = false
                                }
                            }
                        },
                    ) {
                        Text("Hinzufügen")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showAddDialog = false
                            newItemName = ""
                            newItemQuantity = ""
                        },
                    ) {
                        Text("Abbrechen")
                    }
                },
            )
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingListManager.ShoppingListItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = item.isPurchased,
                onCheckedChange = onCheckedChange,
            )

            Spacer(Modifier.width(8.dp))

            // Add food icon if available
            getItemIcon(item.name)?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                    color = if (item.isPurchased) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                )

                if (item.quantity > 0 && item.unit.isNotEmpty()) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (item.addedFrom != "Manual") {
                    Text(
                        text = "Aus: ${item.addedFrom}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // Show priority if not normal
                if (item.priority != ShoppingListManager.Priority.NORMAL) {
                    Text(
                        text =
                            when (item.priority) {
                                ShoppingListManager.Priority.URGENT -> "🔴 Dringend"
                                ShoppingListManager.Priority.HIGH -> "🟡 Wichtig"
                                ShoppingListManager.Priority.LOW -> "🔵 Niedrig"
                                else -> ""
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            when (item.priority) {
                                ShoppingListManager.Priority.URGENT -> MaterialTheme.colorScheme.error
                                ShoppingListManager.Priority.HIGH -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            },
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Löschen",
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

private suspend fun initializeDefaultCategories(db: AppDatabase) {
    val existingCategories = db.shoppingCategoryDao().getCategories()
    if (existingCategories.isEmpty()) {
        val defaultCategories =
            listOf(
                ShoppingCategoryEntity("Obst & Gemüse", 1),
                ShoppingCategoryEntity("Fleisch & Fisch", 2),
                ShoppingCategoryEntity("Milchprodukte", 3),
                ShoppingCategoryEntity("Backwaren", 4),
                ShoppingCategoryEntity("Konserven", 5),
                ShoppingCategoryEntity("Tiefkühl", 6),
                ShoppingCategoryEntity("Getränke", 7),
                ShoppingCategoryEntity("Süßwaren", 8),
                ShoppingCategoryEntity("Haushalt", 9),
                ShoppingCategoryEntity("Sonstiges", 10),
            )

        defaultCategories.forEach {
            db.shoppingCategoryDao().insert(it)
        }
    }
}

private fun categorizeItem(itemName: String): String {
    val name = itemName.lowercase(java.util.Locale.ROOT)
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

private fun getSortModeIcon(mode: ShoppingListSorter.SortingMode): androidx.compose.ui.graphics.vector.ImageVector {
    return when (mode) {
        ShoppingListSorter.SortingMode.SUPERMARKET_LAYOUT -> Icons.Filled.Store
        ShoppingListSorter.SortingMode.ALPHABETICAL -> Icons.AutoMirrored.Filled.List
        ShoppingListSorter.SortingMode.CATEGORY_ALPHABETICAL -> Icons.Filled.Category
        ShoppingListSorter.SortingMode.PRIORITY -> Icons.Filled.PriorityHigh
        ShoppingListSorter.SortingMode.RECENTLY_ADDED -> Icons.Filled.Schedule
        ShoppingListSorter.SortingMode.STATUS -> Icons.Filled.CheckCircle
    }
}

private fun getSortModeTitle(mode: ShoppingListSorter.SortingMode): String {
    return when (mode) {
        ShoppingListSorter.SortingMode.SUPERMARKET_LAYOUT -> "Supermarkt-Layout"
        ShoppingListSorter.SortingMode.ALPHABETICAL -> "Alphabetisch"
        ShoppingListSorter.SortingMode.CATEGORY_ALPHABETICAL -> "Kategorien A-Z"
        ShoppingListSorter.SortingMode.PRIORITY -> "Nach Priorität"
        ShoppingListSorter.SortingMode.RECENTLY_ADDED -> "Zuletzt hinzugefügt"
        ShoppingListSorter.SortingMode.STATUS -> "Nach Status"
    }
}

private fun getSortModeDescription(mode: ShoppingListSorter.SortingMode): String {
    return when (mode) {
        ShoppingListSorter.SortingMode.SUPERMARKET_LAYOUT -> "Optimiert für deutschen Supermarkt-Rundgang"
        ShoppingListSorter.SortingMode.ALPHABETICAL -> "Alle Artikel von A-Z sortiert"
        ShoppingListSorter.SortingMode.CATEGORY_ALPHABETICAL -> "Kategorien alphabetisch, dann Artikel A-Z"
        ShoppingListSorter.SortingMode.PRIORITY -> "Dringende Artikel zuerst"
        ShoppingListSorter.SortingMode.RECENTLY_ADDED -> "Neueste Artikel zuerst"
        ShoppingListSorter.SortingMode.STATUS -> "Offene und erledigte Artikel getrennt"
    }
}

private fun getItemIcon(itemName: String): androidx.compose.ui.graphics.vector.ImageVector? {
    val name = itemName.lowercase()
    return when {
        // Fruits & Vegetables
        name.contains("apfel") || name.contains("äpfel") -> Icons.Filled.Eco
        name.contains("banane") -> Icons.Filled.Eco
        name.contains("tomate") -> Icons.Filled.Eco
        name.contains("kartoffel") -> Icons.Filled.Eco
        name.contains("zwiebel") -> Icons.Filled.Eco
        name.contains("salat") -> Icons.Filled.Eco
        name.contains("möhre") || name.contains("karotte") -> Icons.Filled.Eco

        // Dairy
        name.contains("milch") -> Icons.Filled.LocalDrink
        name.contains("käse") -> Icons.Filled.Cake
        name.contains("butter") -> Icons.Filled.Cake
        name.contains("joghurt") -> Icons.Filled.LocalDrink

        // Meat & Fish
        name.contains("fleisch") || name.contains("hähnchen") || name.contains("rind") -> Icons.Filled.Restaurant
        name.contains("fisch") || name.contains("lachs") -> Icons.Filled.Restaurant
        name.contains("wurst") || name.contains("schinken") -> Icons.Filled.Restaurant

        // Bread & Bakery
        name.contains("brot") || name.contains("brötchen") -> Icons.Filled.Cake
        name.contains("toast") -> Icons.Filled.Cake

        // Beverages
        name.contains("wasser") -> Icons.Filled.LocalDrink
        name.contains("saft") || name.contains("cola") -> Icons.Filled.LocalDrink
        name.contains("kaffee") -> Icons.Filled.LocalCafe
        name.contains("tee") -> Icons.Filled.LocalCafe
        name.contains("bier") || name.contains("wein") -> Icons.Filled.LocalBar

        // Eggs
        name.contains("ei") || name.contains("eier") -> Icons.Filled.Circle

        // Sweets & Snacks
        name.contains("schokolade") -> Icons.Filled.Cake
        name.contains("süß") || name.contains("keks") -> Icons.Filled.Cake

        // Household
        name.contains("spül") || name.contains("wasch") -> Icons.Filled.CleaningServices
        name.contains("papier") -> Icons.Filled.Assignment

        // Default fallback
        else -> null
    }
}
