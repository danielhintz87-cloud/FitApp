package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.FoodItemEntity
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.components.BarcodeScannerView
import kotlinx.coroutines.launch

@Composable
fun BarcodeScannerScreen(
    contentPadding: PaddingValues,
    onBackPressed: () -> Unit,
    onFoodItemFound: (FoodItemEntity) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(context)) }
    
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var foundFoodItem by remember { mutableStateOf<FoodItemEntity?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var showManualEntry by remember { mutableStateOf(false) }
    
    when {
        scannedBarcode == null -> {
            // Show barcode scanner
            BarcodeScannerView(
                onBarcodeDetected = { barcode ->
                    scannedBarcode = barcode
                    isSearching = true
                    scope.launch {
                        try {
                            val foodItem = repo.getFoodItemByBarcode(barcode)
                            if (foodItem != null) {
                                foundFoodItem = foodItem
                                onFoodItemFound(foodItem)
                            } else {
                                // Barcode not found in database, show manual entry
                                showManualEntry = true
                            }
                        } catch (e: Exception) {
                            // Handle error
                            showManualEntry = true
                        } finally {
                            isSearching = false
                        }
                    }
                },
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
            singleLine = true
        )
        
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Kalorien pro 100g") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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