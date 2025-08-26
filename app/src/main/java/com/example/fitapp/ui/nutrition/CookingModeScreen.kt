package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun CookingModeScreen(
    recipe: SavedRecipeEntity,
    onBackPressed: () -> Unit,
    onFinishCooking: () -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val repo = remember { NutritionRepository(db) }
    val scope = rememberCoroutineScope()
    
    var currentStep by remember { mutableStateOf(0) }
    var showIngredients by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var keepScreenOn by remember { mutableStateOf(true) }
    
    // Parse recipe steps from markdown
    val steps = remember(recipe.markdown) {
        parseRecipeSteps(recipe.markdown)
    }
    
    // Keep screen on in cooking mode
    DisposableEffect(keepScreenOn) {
        // This would require activity reference to set FLAG_KEEP_SCREEN_ON
        // For now, just a placeholder
        onDispose { }
    }

    Column(Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { 
                Column {
                    Text("Kochmodus", style = MaterialTheme.typography.titleMedium)
                    Text(recipe.title, style = MaterialTheme.typography.bodyMedium)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { showIngredients = true }) {
                    Icon(Icons.Filled.ListAlt, contentDescription = "Zutaten")
                }
                IconButton(onClick = { keepScreenOn = !keepScreenOn }) {
                    Icon(
                        if (keepScreenOn) Icons.Filled.ScreenLockPortrait else Icons.Filled.ScreenLockRotation,
                        contentDescription = "Display an/aus"
                    )
                }
            }
        )
        
        // Progress Indicator
        Column(Modifier.padding(16.dp)) {
            Text(
                "Schritt ${currentStep + 1} von ${steps.size}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (currentStep + 1).toFloat() / steps.size.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Current Step Content
        if (steps.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        "Schritt ${currentStep + 1}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        steps[currentStep],
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                    )
                }
            }
        }
        
        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { currentStep = maxOf(0, currentStep - 1) },
                enabled = currentStep > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Zurück")
            }
            
            if (currentStep < steps.size - 1) {
                Button(
                    onClick = { currentStep = minOf(steps.size - 1, currentStep + 1) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Weiter")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                Button(
                    onClick = { showFinishDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Fertig!")
                }
            }
        }
    }
    
    // Ingredients Dialog
    if (showIngredients) {
        Dialog(
            onDismissRequest = { showIngredients = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
            ) {
                Column(Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text("Zutaten") },
                        navigationIcon = {
                            IconButton(onClick = { showIngredients = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Schließen")
                            }
                        }
                    )
                    
                    val ingredients = parseIngredients(recipe.markdown)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ingredients.size) { index ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    ingredients[index],
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Finish Cooking Dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Kochen abgeschlossen") },
            text = { 
                Column {
                    Text("Möchten Sie das Rezept zu Ihrem Ernährungstagebuch hinzufügen?")
                    Spacer(Modifier.height(8.dp))
                    recipe.calories?.let {
                        Text("Kalorien: $it kcal", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            // Mark recipe as cooked
                            db.savedRecipeDao().markAsCooked(recipe.id)
                            
                            // Add to nutrition log
                            recipe.calories?.let { kcal ->
                                repo.logIntake(kcal, "Gekocht: ${recipe.title}", "RECIPE", recipe.id)
                                repo.adjustDailyGoal(LocalDate.now())
                            }
                            
                            showFinishDialog = false
                            onFinishCooking()
                        }
                    }
                ) {
                    Text("Ja, hinzufügen")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            // Just mark as cooked without adding to nutrition log
                            db.savedRecipeDao().markAsCooked(recipe.id)
                            showFinishDialog = false
                            onFinishCooking()
                        }
                    }
                ) {
                    Text("Nur als gekocht markieren")
                }
            }
        )
    }
}

private fun parseRecipeSteps(markdown: String): List<String> {
    val steps = mutableListOf<String>()
    val lines = markdown.lines()
    var inInstructions = false
    
    for (line in lines) {
        when {
            line.contains("Zubereitung", ignoreCase = true) || 
            line.contains("Anleitung", ignoreCase = true) ||
            line.contains("Schritte", ignoreCase = true) -> {
                inInstructions = true
            }
            line.startsWith("##") && inInstructions -> {
                break // Next section
            }
            inInstructions && line.matches(Regex("^\\d+\\.\\s+.+")) -> {
                // Numbered step
                steps.add(line.substringAfter(". ").trim())
            }
            inInstructions && line.trim().isNotBlank() && !line.startsWith("#") -> {
                // Add non-empty lines as potential steps
                val trimmed = line.trim()
                if (trimmed.length > 10) { // Filter out very short lines
                    steps.add(trimmed)
                }
            }
        }
    }
    
    if (steps.isEmpty()) {
        // Fallback: extract any numbered lines
        lines.forEach { line ->
            if (line.matches(Regex("^\\d+\\.\\s+.+"))) {
                steps.add(line.substringAfter(". ").trim())
            }
        }
    }
    
    if (steps.isEmpty()) {
        steps.add("Folgen Sie den Anweisungen im Rezept.")
    }
    
    return steps
}

private fun parseIngredients(markdown: String): List<String> {
    val ingredients = mutableListOf<String>()
    val lines = markdown.lines()
    var inIngredients = false
    
    for (line in lines) {
        when {
            line.contains("Zutaten", ignoreCase = true) || 
            line.contains("Ingredients", ignoreCase = true) -> {
                inIngredients = true
            }
            line.startsWith("##") && inIngredients -> {
                break // Next section
            }
            inIngredients && (line.startsWith("- ") || line.startsWith("* ")) -> {
                ingredients.add(line.substring(2).trim())
            }
            inIngredients && line.matches(Regex("^\\d+.*")) -> {
                // Ingredient with quantity
                ingredients.add(line.trim())
            }
        }
    }
    
    if (ingredients.isEmpty()) {
        ingredients.add("Siehe Rezept für Zutaten")
    }
    
    return ingredients
}