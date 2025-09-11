package com.example.fitapp.ui.nutrition

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.FoodItemEntity
import com.example.fitapp.data.nutrition.FoodItem
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.components.BarcodeScannerView
import com.example.fitapp.ui.nutrition.barcode.FoodDatabaseLookup
import kotlinx.coroutines.launch

@ExperimentalGetImage
@Composable
fun BarcodeScannerScreen(
    contentPadding: PaddingValues,
    onBackPressed: () -> Unit,
    onFoodItemFound: (FoodItemEntity) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(context), context) }
    val foodLookup = remember { FoodDatabaseLookup() }
    
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var foundFoodItem by remember { mutableStateOf<FoodItem?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var showManualEntry by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchAttempts by remember { mutableStateOf(0) }
    
    // Enhanced barcode scanning with multiple attempts
    fun handleBarcodeScanned(barcode: String) {
        if (scannedBarcode == barcode && searchAttempts >= 3) {
            return // Avoid infinite retry loops
        }
        
        scannedBarcode = barcode
        isSearching = true
        errorMessage = null
        searchAttempts++
        
        scope.launch {
            try {
                // First try local database
                var foodItem = repo.getFoodItemByBarcode(barcode)?.let { entity ->
                    FoodItem(
                        id = entity.id.toLongOrNull() ?: 0L,
                        name = entity.name,
                        brand = entity.brands ?: "",
                        barcode = entity.barcode ?: "",
                        caloriesPer100g = entity.calories.toFloat(),
                        proteinPer100g = entity.protein,
                        carbsPer100g = entity.carbs,
                        fatPer100g = entity.fat,
                        category = entity.categories ?: "Unbekannt",
                        source = "Local Database"
                    )
                }
                
                // If not found locally, search online
                if (foodItem == null) {
                    foodItem = foodLookup.lookupByBarcode(barcode)
                }
                
                if (foodItem != null) {
                    foundFoodItem = foodItem
                    
                    // Convert to entity and save to local database if from online source
                    if (foodItem.source != "Local Database") {
                        val entity = FoodItemEntity(
                            name = foodItem.name,
                            calories = foodItem.caloriesPer100g.toInt(),
                            carbs = foodItem.carbsPer100g,
                            protein = foodItem.proteinPer100g,
                            fat = foodItem.fatPer100g
                        )
                        repo.addFoodItem(entity)
                        onFoodItemFound(entity)
                    } else {
                        // Use existing entity
                        val entity = repo.getFoodItemByBarcode(barcode)!!
                        onFoodItemFound(entity)
                    }
                } else {
                    // Product not found, show manual entry option
                    showManualEntry = true
                    errorMessage = "Produkt nicht gefunden. Möchten Sie es manuell hinzufügen?"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Suchen: ${e.message}"
                if (searchAttempts < 3) {
                    // Allow retry
                    showManualEntry = false
                } else {
                    showManualEntry = true
                }
            } finally {
                isSearching = false
            }
        }
    }

    when {
        scannedBarcode == null -> {
            // Show enhanced barcode scanner
            EnhancedBarcodeScannerView(
                onBarcodeDetected = ::handleBarcodeScanned,
                onClose = onBackPressed,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        isSearching -> {
            // Show loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Suche Produkt...")
                        Text(
                            "Barcode: $scannedBarcode",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        showManualEntry -> {
            // Show manual entry for unknown barcode
            BarcodeNotFoundScreen(
                barcode = scannedBarcode!!,
                onBackPressed = onBackPressed,
                onFoodItemCreated = { foodItem ->
                    scope.launch {
                        repo.addFoodItem(foodItem)
                        onFoodItemFound(foodItem)
                    }
                },
                contentPadding = contentPadding
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarcodeNotFoundScreen(
    barcode: String,
    onBackPressed: () -> Unit,
    onFoodItemCreated: (FoodItemEntity) -> Unit,
    contentPadding: PaddingValues
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Produkt nicht gefunden",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Barcode: $barcode",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Bitte geben Sie die Produktinformationen manuell ein:",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Produktname") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Kalorien pro 100g") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text("Kohlenhydrate (g)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Protein (g)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fett (g)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBackPressed,
                modifier = Modifier.weight(1f)
            ) {
                Text("Abbrechen")
            }
            
            Button(
                onClick = {
                    if (name.isNotBlank() && calories.isNotBlank() && 
                        carbs.isNotBlank() && protein.isNotBlank() && fat.isNotBlank()) {
                        val foodItem = FoodItemEntity(
                            name = name.trim(),
                            barcode = barcode,
                            calories = calories.toIntOrNull() ?: 0,
                            carbs = carbs.toFloatOrNull() ?: 0f,
                            protein = protein.toFloatOrNull() ?: 0f,
                            fat = fat.toFloatOrNull() ?: 0f
                        )
                        onFoodItemCreated(foodItem)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && calories.isNotBlank() && 
                         carbs.isNotBlank() && protein.isNotBlank() && fat.isNotBlank()
            ) {
                Text("Speichern")
            }
        }
    }
}

/**
 * Enhanced Barcode Scanner View with ML Kit integration
 */
@Composable
private fun EnhancedBarcodeScannerView(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // For now, use the existing BarcodeScannerView
    // TODO: Implement CameraX + ML Kit integration here
    BarcodeScannerView(
        onBarcodeDetected = onBarcodeDetected,
        onClose = onClose,
        modifier = modifier
    )
}