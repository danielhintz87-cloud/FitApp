package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp                    // Sorting toggle
                    IconButton(
                        onClick = { showSortingDialog = true }
                    ) {
                        Icon(
                            Icons.Filled.Sort,
                            contentDescription = "Sortierung ändern"
                        )
                    }s.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.ShoppingCategoryEntity
import com.example.fitapp.services.ShoppingListManager
import com.example.fitapp.services.CookingModeManager
import com.example.fitapp.services.VoiceInputManager
import com.example.fitapp.services.VoiceInputResult
import com.example.fitapp.services.ShoppingListSorter
import com.example.fitapp.ui.components.*
import kotlinx.coroutines.launch

/**
 * New Enhanced Shopping List Screen
 * Implements the comprehensive shopping list management system as specified
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartShoppingListScreen(
    padding: PaddingValues = PaddingValues(0.dp),
    onBackPressed: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()
    
    // Shopping list manager
    val shoppingManager = remember { ShoppingListManager(db) }
    
    // Voice input manager
    val voiceInputManager = remember { VoiceInputManager(context) }
    
    // State
    val shoppingItems by shoppingManager.shoppingItems.collectAsState()
    val categorizedItems by shoppingManager.categorizedItems.collectAsState()
    val stats = shoppingManager.getShoppingStats()
    val isListeningForVoice by voiceInputManager.isListening.collectAsState()
    val voiceInputResult by voiceInputManager.lastResult.collectAsState()
    val currentSortingMode by shoppingManager.sortingMode.collectAsState()
    
    var sortBySupermarket by remember { mutableStateOf(true) }
    var showSortingDialog by remember { mutableStateOf(false) }
    var showAddFromRecipeDialog by remember { mutableStateOf(false) }
    var showEditQuantityDialog by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    
    // Load initial data
    LaunchedEffect(Unit) {
        // Initialize categories if needed
        initializeDefaultCategories(db)
        shoppingManager.loadShoppingList()
        voiceInputManager.initialize()
    }
    
    // Handle voice input results
    LaunchedEffect(voiceInputResult) {
        when (val result = voiceInputResult) {
            is VoiceInputResult.Success -> {
                // Add all recognized items to shopping list
                result.shoppingItems.forEach { voiceItem ->
                    val ingredient = CookingModeManager.Ingredient(
                        name = voiceItem.name,
                        quantity = voiceItem.quantity,
                        unit = voiceItem.unit
                    )
                    shoppingManager.addIngredient(ingredient, "Voice Input")
                }
            }
            is VoiceInputResult.Error -> {
                // Error handling could be expanded here
            }
            else -> { /* Handle partial results or null */ }
        }
    }
    
    // Release voice resources on dispose
    DisposableEffect(Unit) {
        onDispose {
            voiceInputManager.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Smart Einkaufsliste",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onBackPressed != null) {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                },
                actions = {
                    // Sort toggle
                    IconButton(
                        onClick = { sortBySupermarket = !sortBySupermarket }
                    ) {
                        Icon(
                            if (sortBySupermarket) Icons.Filled.Store else Icons.AutoMirrored.Filled.List,
                            contentDescription = if (sortBySupermarket) "Nach Liste sortieren" else "Nach Supermarkt sortieren"
                        )
                    }
                    
                    // Add from recipe
                    IconButton(
                        onClick = { showAddFromRecipeDialog = true }
                    ) {
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = "Aus Rezept hinzufügen"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    scope.launch {
                        // Quick add functionality - could open voice input
                        // For now, we'll show the add dialog via the section
                    }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Shopping List Header with Stats
            ShoppingListHeader(
                stats = stats,
                onClearCompleted = {
                    scope.launch {
                        shoppingManager.clearCompletedItems()
                    }
                },
                modifier = Modifier.padding(16.dp)
            )
            
            // Add Item Section
            AddItemSection(
                onAddItem = { name, quantity, unit ->
                    scope.launch {
                        val ingredient = CookingModeManager.Ingredient(
                            name = name,
                            quantity = quantity,
                            unit = unit
                        )
                        shoppingManager.addIngredient(ingredient, "Manual")
                    }
                },
                onVoiceInput = {
                    // Start voice recognition for shopping list
                    scope.launch {
                        if (!isListeningForVoice) {
                            voiceInputManager.startListeningForShoppingItems().collect { result ->
                                // Results are handled in LaunchedEffect above
                            }
                        } else {
                            voiceInputManager.stopListening()
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Shopping List Content
            if (shoppingItems.isEmpty()) {
                EmptyShoppingListView(
                    onAddFirst = { showAddFromRecipeDialog = true }
                )
            } else {
                val displayItems = shoppingManager.groupIngredientsByCategory()
                
                ShoppingListGroupedView(
                    categorizedItems = displayItems,
                    onTogglePurchased = { itemId ->
                        scope.launch {
                            val item = shoppingItems.find { it.id == itemId }
                            if (item != null) {
                                shoppingManager.markIngredientAsPurchased(itemId, !item.isPurchased)
                            }
                        }
                    },
                    onRemove = { itemId ->
                        scope.launch {
                            shoppingManager.removeIngredient(itemId)
                        }
                    },
                    onEditQuantity = { itemId ->
                        selectedItemId = itemId
                        showEditQuantityDialog = true
                    }
                )
            }
        }
    }

    // Dialogs
    if (showAddFromRecipeDialog) {
        AddFromRecipeDialog(
            onAddRecipe = { recipeTitle, ingredients, servings ->
                scope.launch {
                    shoppingManager.addAllRecipeIngredients(recipeTitle, ingredients, servings)
                    showAddFromRecipeDialog = false
                }
            },
            onDismiss = { showAddFromRecipeDialog = false }
        )
    }

    if (showEditQuantityDialog && selectedItemId != null) {
        val selectedItem = shoppingItems.find { it.id == selectedItemId }
        if (selectedItem != null) {
            EditQuantityDialog(
                item = selectedItem,
                onUpdate = { newQuantity, newUnit ->
                    scope.launch {
                        shoppingManager.updateItemQuantity(selectedItemId!!, newQuantity, newUnit)
                        showEditQuantityDialog = false
                        selectedItemId = null
                    }
                },
                onDismiss = {
                    showEditQuantityDialog = false
                    selectedItemId = null
                }
            )
        }
        
        // Sorting Dialog
        if (showSortingDialog) {
            SortingModeDialog(
                currentMode = currentSortingMode,
                onModeSelected = { newMode ->
                    shoppingManager.changeSortingMode(newMode)
                    showSortingDialog = false
                },
                onDismiss = { showSortingDialog = false }
            )
        }
    }
}

@Composable
private fun EmptyShoppingListView(
    onAddFirst: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            
            Text(
                "Einkaufsliste ist leer",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outline
            )
            
            Text(
                "Füge Artikel hinzu oder importiere sie aus einem Rezept",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            
            Button(onClick = onAddFirst) {
                Icon(Icons.Filled.Restaurant, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Aus Rezept hinzufügen")
            }
        }
    }
}

@Composable
private fun AddFromRecipeDialog(
    onAddRecipe: (String, List<CookingModeManager.Ingredient>, Int) -> Unit,
    onDismiss: () -> Unit
) {
    // This is a simplified dialog - in a real implementation, 
    // you would load saved recipes from the database
    var servings by remember { mutableIntStateOf(4) }
    var selectedRecipe by remember { mutableStateOf<String?>(null) }
    
    val sampleRecipes = listOf(
        "Spaghetti Bolognese" to listOf(
            CookingModeManager.Ingredient("Spaghetti", "500", "g"),
            CookingModeManager.Ingredient("Hackfleisch", "400", "g"),
            CookingModeManager.Ingredient("Zwiebeln", "2", "Stück"),
            CookingModeManager.Ingredient("Tomaten", "400", "g"),
            CookingModeManager.Ingredient("Parmesan", "100", "g")
        ),
        "Gemüsecurry" to listOf(
            CookingModeManager.Ingredient("Brokkoli", "300", "g"),
            CookingModeManager.Ingredient("Paprika", "2", "Stück"),
            CookingModeManager.Ingredient("Kokosmilch", "400", "ml"),
            CookingModeManager.Ingredient("Currypaste", "2", "EL"),
            CookingModeManager.Ingredient("Reis", "200", "g")
        )
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aus Rezept hinzufügen") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Wähle ein Rezept:")
                
                sampleRecipes.forEach { (recipeName, ingredients) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedRecipe = recipeName },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRecipe == recipeName) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                recipeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${ingredients.size} Zutaten",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                
                if (selectedRecipe != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Portionen:")
                        
                        IconButton(
                            onClick = { servings = (servings - 1).coerceAtLeast(1) }
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Weniger")
                        }
                        
                        Text(
                            "$servings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = { servings = servings + 1 }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Mehr")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedRecipe?.let { recipeName ->
                        val recipe = sampleRecipes.find { it.first == recipeName }
                        recipe?.let { (title, ingredients) ->
                            onAddRecipe(title, ingredients, servings)
                        }
                    }
                },
                enabled = selectedRecipe != null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditQuantityDialog(
    item: ShoppingListManager.ShoppingListItem,
    onUpdate: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var unit by remember { mutableStateOf(item.unit) }
    
    val commonUnits = listOf("Stück", "g", "kg", "ml", "l", "EL", "TL", "Packung", "Dose", "Bund")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menge bearbeiten") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
                            modifier = Modifier.menuAnchor(MenuAnchorType.Primary)
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
                onClick = { onUpdate(quantity, unit) },
                enabled = quantity.isNotBlank()
            ) {
                Text("Aktualisieren")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

// Helper function to initialize default categories
private suspend fun initializeDefaultCategories(db: AppDatabase) {
    val categories = listOf(
        "Obst & Gemüse" to 1,
        "Fleisch & Fisch" to 2,
        "Milchprodukte" to 3,
        "Getreide & Backwaren" to 4,
        "Tiefkühl" to 5,
        "Getränke" to 6,
        "Gewürze & Kräuter" to 7,
        "Sonstiges" to 8
    )
    
    categories.forEach { (name, order) ->
        try {
            db.shoppingCategoryDao().insert(
                ShoppingCategoryEntity(name = name, order = order)
            )
        } catch (e: Exception) {
            // Category might already exist
        }
    }
}

/**
 * Sorting Mode Selection Dialog
 */
@Composable
private fun SortingModeDialog(
    currentMode: ShoppingListSorter.SortingMode,
    onModeSelected: (ShoppingListSorter.SortingMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sortierung wählen") },
        text = {
            Column {
                ShoppingListSorter.SortingMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = { onModeSelected(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = getSortingModeTitle(mode),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = getSortingModeDescription(mode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

/**
 * Get human-readable title for sorting mode
 */
private fun getSortingModeTitle(mode: ShoppingListSorter.SortingMode): String {
    return when (mode) {
        ShoppingListSorter.SortingMode.SUPERMARKET_LAYOUT -> "🏪 Supermarkt-Layout"
        ShoppingListSorter.SortingMode.ALPHABETICAL -> "🔤 Alphabetisch"
        ShoppingListSorter.SortingMode.CATEGORY_ALPHABETICAL -> "📂 Kategorien A-Z"
        ShoppingListSorter.SortingMode.PRIORITY -> "⭐ Nach Priorität"
        ShoppingListSorter.SortingMode.RECENTLY_ADDED -> "🕒 Zuletzt hinzugefügt"
        ShoppingListSorter.SortingMode.STATUS -> "✅ Nach Status"
    }
}

/**
 * Get description for sorting mode
 */
private fun getSortingModeDescription(mode: ShoppingListSorter.SortingMode): String {
    return when (mode) {
        ShoppingListSorter.SortingMode.SUPERMARKET_LAYOUT -> "Wie im deutschen Supermarkt angeordnet"
        ShoppingListSorter.SortingMode.ALPHABETICAL -> "Alle Artikel von A bis Z"
        ShoppingListSorter.SortingMode.CATEGORY_ALPHABETICAL -> "Kategorien alphabetisch sortiert"
        ShoppingListSorter.SortingMode.PRIORITY -> "Dringende Artikel zuerst"
        ShoppingListSorter.SortingMode.RECENTLY_ADDED -> "Neueste Artikel zuerst"
        ShoppingListSorter.SortingMode.STATUS -> "Gekauft und nicht gekauft getrennt"
    }
}