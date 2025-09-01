package com.example.fitapp.ui.nutrition

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.FoodItemEntity
import com.example.fitapp.data.db.MealEntryEntity
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.nutrition.BarcodeScannerScreen
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    contentPadding: PaddingValues,
    onBackPressed: () -> Unit,
    onFoodAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(context)) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FoodItemEntity>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showAddCustomFood by remember { mutableStateOf(false) }
    var recentFoods by remember { mutableStateOf<List<FoodItemEntity>>(emptyList()) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var selectedFoodItem by remember { mutableStateOf<FoodItemEntity?>(null) }
    
    // Permission launcher for camera
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // TODO: Launch barcode scanner
        }
    }
    
    // Load recent foods on start
    LaunchedEffect(Unit) {
        scope.launch {
            recentFoods = repo.getRecentFoodItems(20)
        }
    }
    
    // Search for foods when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            scope.launch {
                try {
                    searchResults = repo.searchFoodItems(searchQuery, 20)
                } finally {
                    isSearching = false
                }
            }
        } else {
            searchResults = emptyList()
        }
    }
    
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Lebensmittel hinzufügen",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showAddCustomFood = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Food")
            }
        }
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Lebensmittel suchen") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            )
        )
        
        // Barcode scanner button
        Button(
            onClick = {
                val permission = Manifest.permission.CAMERA
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, permission) -> {
                        showBarcodeScanner = true
                    }
                    else -> {
                        permissionLauncher.launch(permission)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Barcode scannen")
        }
        
        // Search results or recent foods
        if (searchQuery.isNotBlank()) {
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SearchResultsList(
                    searchResults = searchResults,
                    onFoodSelected = { foodItem ->
                        selectedFoodItem = foodItem
                    }
                )
            }
        } else {
            RecentFoodsList(
                recentFoods = recentFoods,
                onFoodSelected = { foodItem ->
                    selectedFoodItem = foodItem
                }
            )
        }
    }
    
    // Barcode scanner
    if (showBarcodeScanner) {
        BarcodeScannerScreen(
            contentPadding = contentPadding,
            onBackPressed = { showBarcodeScanner = false },
            onFoodItemFound = { foodItem ->
                selectedFoodItem = foodItem
                showBarcodeScanner = false
            }
        )
    }
    
    // Add custom food dialog
    if (showAddCustomFood) {
        AddCustomFoodDialog(
            onDismiss = { showAddCustomFood = false },
            onFoodAdded = { foodItem ->
                scope.launch {
                    repo.addFoodItem(foodItem)
                    recentFoods = repo.getRecentFoodItems(20)
                    showAddCustomFood = false
                }
            }
        )
    }
    
    // Meal selection dialog
    selectedFoodItem?.let { foodItem ->
        AddMealDialog(
            foodItem = foodItem,
            onDismiss = { selectedFoodItem = null },
            onMealAdded = { mealEntry ->
                scope.launch {
                    repo.addMealEntry(mealEntry)
                    selectedFoodItem = null
                    onFoodAdded()
                }
            }
        )
    }
}

@Composable
private fun SearchResultsList(
    searchResults: List<FoodItemEntity>,
    onFoodSelected: (FoodItemEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Suchergebnisse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (searchResults.isEmpty()) {
            item {
                Text(
                    "Keine Ergebnisse gefunden",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(searchResults) { foodItem ->
                FoodItemCard(
                    foodItem = foodItem,
                    onClick = { onFoodSelected(foodItem) }
                )
            }
        }
    }
}

@Composable
private fun RecentFoodsList(
    recentFoods: List<FoodItemEntity>,
    onFoodSelected: (FoodItemEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Zuletzt verwendet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (recentFoods.isEmpty()) {
            item {
                Text(
                    "Keine kürzlich verwendeten Lebensmittel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recentFoods) { foodItem ->
                FoodItemCard(
                    foodItem = foodItem,
                    onClick = { onFoodSelected(foodItem) }
                )
            }
        }
    }
}

@Composable
private fun FoodItemCard(
    foodItem: FoodItemEntity,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                foodItem.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NutritionInfo("${foodItem.calories} kcal", "pro 100g")
                NutritionInfo("${foodItem.carbs}g", "Kohlenhydrate")
                NutritionInfo("${foodItem.protein}g", "Protein")
                NutritionInfo("${foodItem.fat}g", "Fett")
            }
        }
    }
}

@Composable
private fun NutritionInfo(
    value: String,
    label: String
) {
    Column {
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomFoodDialog(
    onDismiss: () -> Unit,
    onFoodAdded: (FoodItemEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eigenes Lebensmittel hinzufügen") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Kalorien (pro 100g)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Kohlenhydrate (g pro 100g)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g pro 100g)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fett (g pro 100g)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && calories.isNotBlank() && 
                        carbs.isNotBlank() && protein.isNotBlank() && fat.isNotBlank()) {
                        val foodItem = FoodItemEntity(
                            name = name.trim(),
                            calories = calories.toIntOrNull() ?: 0,
                            carbs = carbs.toFloatOrNull() ?: 0f,
                            protein = protein.toFloatOrNull() ?: 0f,
                            fat = fat.toFloatOrNull() ?: 0f,
                            barcode = barcode.takeIf { it.isNotBlank() }
                        )
                        onFoodAdded(foodItem)
                    }
                }
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealDialog(
    foodItem: FoodItemEntity,
    onDismiss: () -> Unit,
    onMealAdded: (MealEntryEntity) -> Unit
) {
    var selectedMealType by remember { mutableStateOf("breakfast") }
    var quantity by remember { mutableStateOf("100") }
    var notes by remember { mutableStateOf("") }
    
    val mealTypes = listOf(
        "breakfast" to "Frühstück",
        "lunch" to "Mittagessen", 
        "dinner" to "Abendessen",
        "snack" to "Snack"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${foodItem.name} hinzufügen") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Meal type selection
                Text(
                    "Mahlzeit",
                    style = MaterialTheme.typography.labelLarge
                )
                
                Column {
                    mealTypes.forEach { (type, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMealType == type,
                                onClick = { selectedMealType = type }
                            )
                            Text(
                                label,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                // Quantity
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Menge (g)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                // Nutrition preview
                val quantityFloat = quantity.toFloatOrNull() ?: 100f
                val previewCalories = ((quantityFloat / 100f) * foodItem.calories).toInt()
                val previewCarbs = (quantityFloat / 100f) * foodItem.carbs
                val previewProtein = (quantityFloat / 100f) * foodItem.protein
                val previewFat = (quantityFloat / 100f) * foodItem.fat
                
                Card {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Nährwerte für ${quantityFloat.toInt()}g:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text("${previewCalories} kcal")
                        Text("${previewCarbs.toString().take(4)}g Kohlenhydrate")
                        Text("${previewProtein.toString().take(4)}g Protein")
                        Text("${previewFat.toString().take(4)}g Fett")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantityFloat = quantity.toFloatOrNull() ?: 100f
                    val mealEntry = MealEntryEntity(
                        foodItemId = foodItem.id,
                        date = LocalDate.now().toString(),
                        mealType = selectedMealType,
                        quantityGrams = quantityFloat,
                        notes = notes.takeIf { it.isNotBlank() }
                    )
                    onMealAdded(mealEntry)
                }
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}