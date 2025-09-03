package com.example.fitapp.ui.nutrition

import android.app.Activity
import android.view.WindowManager
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.services.CookingModeManager
import com.example.fitapp.services.ShoppingListManager
import com.example.fitapp.ui.components.*
import kotlinx.coroutines.launch

/**
 * Enhanced Cooking Mode Screen
 * Implements the comprehensive cooking experience system as specified
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCookingModeScreen(
    recipe: SavedRecipeEntity,
    onBackPressed: () -> Unit,
    onFinishCooking: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()
    
    // Cooking mode state
    val cookingManager = remember { CookingModeManager(db) }
    val shoppingManager = remember { ShoppingListManager(db) }
    
    val cookingFlow by cookingManager.cookingFlow.collectAsState()
    val currentStep by cookingManager.currentStep.collectAsState()
    val isInCookingMode by cookingManager.isInCookingMode.collectAsState()
    val stepTimers by cookingManager.stepTimers.collectAsState()
    
    // UI state
    var showOverview by remember { mutableStateOf(true) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var servings by remember { mutableIntStateOf(recipe.servings ?: 1) }
    var keepScreenOn by remember { mutableStateOf(true) }
    
    // Keep screen on during cooking
    DisposableEffect(isInCookingMode && keepScreenOn) {
        val activity = context as? Activity
        if (isInCookingMode && keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Main UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isInCookingMode) "Kochmodus" else recipe.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    if (isInCookingMode) {
                        IconButton(onClick = { keepScreenOn = !keepScreenOn }) {
                            Icon(
                                if (keepScreenOn) Icons.Filled.ScreenLockPortrait else Icons.Filled.ScreenLockRotation,
                                contentDescription = "Display an/aus"
                            )
                        }
                        IconButton(onClick = { showPauseDialog = true }) {
                            Icon(Icons.Filled.Pause, contentDescription = "Pausieren")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Show recipe overview before starting cooking mode
                showOverview && !isInCookingMode -> {
                    RecipeDetailScreen(
                        recipe = recipe,
                        servings = servings,
                        onServingsChange = { servings = it },
                        onStartCooking = {
                            scope.launch {
                                cookingManager.startCookingMode(recipe)
                                showOverview = false
                            }
                        },
                        onAddToShoppingList = { ingredients ->
                            scope.launch {
                                shoppingManager.addAllRecipeIngredients(
                                    recipeTitle = recipe.title ?: "Kochmodus",
                                    ingredients = ingredients,
                                    servings = servings
                                )
                            }
                        }
                    )
                }
                
                // Show cooking mode execution
                isInCookingMode && currentStep != null && cookingFlow != null -> {
                    CookingExecutionScreen(
                        cookingStep = currentStep!!,
                        cookingFlow = cookingFlow!!,
                        stepTimer = stepTimers[cookingFlow!!.currentStepIndex],
                        servings = servings,
                        onNextStep = {
                            scope.launch {
                                val nextStep = cookingManager.navigateToNextStep()
                                if (nextStep == null) {
                                    // Cooking completed
                                    cookingManager.finishCooking()
                                    onFinishCooking()
                                }
                            }
                        },
                        onPreviousStep = {
                            scope.launch {
                                cookingManager.navigateToPreviousStep()
                            }
                        },
                        onPauseCooking = {
                            showPauseDialog = true
                        },
                        onFinishCooking = {
                            showFinishDialog = true
                        },
                        onStartTimer = { duration ->
                            scope.launch {
                                cookingManager.startStepTimer(
                                    stepIndex = cookingFlow!!.currentStepIndex,
                                    duration = duration,
                                    name = "Schritt ${currentStep!!.stepNumber}"
                                )
                            }
                        },
                        onToggleTimer = {
                            scope.launch {
                                cookingManager.toggleStepTimer(cookingFlow!!.currentStepIndex)
                            }
                        },
                        onResetTimer = {
                            // Implementation for reset timer
                        },
                        onShowImage = {
                            showImageDialog = true
                        }
                    )
                }
                
                // Loading state
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Dialogs
    if (showPauseDialog) {
        PauseCookingDialog(
            onResume = { 
                scope.launch {
                    cookingManager.resumeCooking()
                    showPauseDialog = false
                }
            },
            onEndCooking = {
                scope.launch {
                    cookingManager.finishCooking()
                    onFinishCooking()
                    showPauseDialog = false
                }
            },
            onDismiss = { showPauseDialog = false }
        )
    }

    if (showFinishDialog) {
        FinishCookingDialog(
            onConfirm = {
                scope.launch {
                    cookingManager.finishCooking()
                    onFinishCooking()
                    showFinishDialog = false
                }
            },
            onDismiss = { showFinishDialog = false }
        )
    }

    if (showImageDialog && currentStep != null) {
        StepImageDialog(
            stepNumber = currentStep!!.stepNumber,
            imagePath = currentStep!!.image,
            onDismiss = { showImageDialog = false }
        )
    }
}

@Composable
private fun RecipeDetailScreen(
    recipe: SavedRecipeEntity,
    servings: Int,
    onServingsChange: (Int) -> Unit,
    onStartCooking: () -> Unit,
    onAddToShoppingList: (List<CookingModeManager.Ingredient>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RecipeHeaderCard(
                recipeTitle = recipe.title,
                servings = servings,
                difficulty = recipe.difficulty,
                estimatedTime = recipe.prepTime,
                onServingsChange = onServingsChange
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Bereit zum Kochen?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Starte den Kochmodus für eine geführte Schritt-für-Schritt Anleitung",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onStartCooking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Kochmodus starten")
                    }
                }
            }
        }

        // Recipe ingredients preview (simplified)
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Zutaten",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    // Parse and display ingredients from markdown
                    val ingredients = parseIngredientsFromMarkdown(recipe.markdown, servings)
                    ingredients.take(5).forEach { ingredient ->
                        Text(
                            "• ${ingredient.quantity} ${ingredient.unit} ${ingredient.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (ingredients.size > 5) {
                        Text(
                            "... und ${ingredients.size - 5} weitere",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onAddToShoppingList(ingredients) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Zur Einkaufsliste hinzufügen")
                    }
                }
            }
        }

        // Recipe instructions preview
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Anleitung (Vorschau)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    val steps = parseStepsPreview(recipe.markdown)
                    steps.take(3).forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "${index + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(24.dp)
                            )
                            Text(
                                step.take(80) + if (step.length > 80) "..." else "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (index < steps.size - 1) Spacer(Modifier.height(4.dp))
                    }
                    
                    if (steps.size > 3) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "... und ${steps.size - 3} weitere Schritte",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CookingExecutionScreen(
    cookingStep: CookingModeManager.CookingStep,
    cookingFlow: CookingModeManager.CookingFlow,
    stepTimer: CookingModeManager.StepTimer?,
    servings: Int,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onPauseCooking: () -> Unit,
    onFinishCooking: () -> Unit,
    onStartTimer: (Int) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onShowImage: () -> Unit
) {
    val isLastStep = cookingFlow.currentStepIndex >= cookingFlow.steps.size - 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CookingProgressBar(
                currentStep = cookingStep.stepNumber,
                totalSteps = cookingFlow.steps.size
            )
        }

        item {
            StepInstructionCard(
                cookingStep = cookingStep,
                onShowImage = onShowImage
            )
        }

        // Timer Section (if step has duration)
        if (cookingStep.duration != null) {
            item {
                StepTimerCard(
                    timer = stepTimer,
                    stepDuration = cookingStep.duration,
                    onStartTimer = { onStartTimer(cookingStep.duration!!) },
                    onPauseTimer = onToggleTimer,
                    onResetTimer = onResetTimer
                )
            }
        }

        // Cooking Tips
        if (cookingStep.tips.isNotEmpty()) {
            item {
                CookingTipsCard(tips = cookingStep.tips)
            }
        }

        item {
            CookingNavigationBar(
                canGoBack = cookingFlow.currentStepIndex > 0,
                canGoNext = true,
                isLastStep = isLastStep,
                onPrevious = onPreviousStep,
                onPause = onPauseCooking,
                onNext = onNextStep,
                onFinish = onFinishCooking
            )
        }
    }
}

// Dialog Components
@Composable
private fun PauseCookingDialog(
    onResume: () -> Unit,
    onEndCooking: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Pause, contentDescription = null)
        },
        title = {
            Text("Kochen pausiert")
        },
        text = {
            Text("Möchtest du das Kochen fortsetzen oder beenden?")
        },
        confirmButton = {
            Button(onClick = onResume) {
                Text("Fortsetzen")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onEndCooking) {
                Text("Beenden")
            }
        }
    )
}

@Composable
private fun FinishCookingDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Check, contentDescription = null)
        },
        title = {
            Text("Kochen beenden?")
        },
        text = {
            Text("Möchtest du das Kochen jetzt beenden? Dein Fortschritt wird gespeichert.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Beenden")
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
private fun StepImageDialog(
    stepNumber: Int,
    imagePath: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Bild - Schritt $stepNumber",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                
                // Placeholder for image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Schritt-Bild")
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Schließen")
                }
            }
        }
    }
}

// Helper functions
private fun parseIngredientsFromMarkdown(
    markdown: String, 
    servings: Int
): List<CookingModeManager.Ingredient> {
    val ingredients = mutableListOf<CookingModeManager.Ingredient>()
    val lines = markdown.split("\n")
    
    lines.forEach { line ->
        val trimmedLine = line.trim()
        if (trimmedLine.startsWith("-") || trimmedLine.startsWith("*")) {
            val ingredientText = trimmedLine.substring(1).trim()
            
            // Simple parsing for demonstration
            val parts = ingredientText.split(" ", limit = 3)
            if (parts.size >= 3) {
                try {
                    val quantity = (parts[0].toFloat() * servings / (1)).toString() // Adjust for servings
                    val unit = parts[1]
                    val name = parts.drop(2).joinToString(" ")
                    
                    ingredients.add(
                        CookingModeManager.Ingredient(
                            name = name,
                            quantity = quantity,
                            unit = unit,
                            isOptional = ingredientText.contains("optional", ignoreCase = true)
                        )
                    )
                } catch (e: NumberFormatException) {
                    // Fallback for non-numeric quantities
                    ingredients.add(
                        CookingModeManager.Ingredient(
                            name = ingredientText,
                            quantity = "1",
                            unit = "Stück"
                        )
                    )
                }
            }
        }
    }
    
    return ingredients
}

private fun parseStepsPreview(markdown: String): List<String> {
    val steps = mutableListOf<String>()
    val lines = markdown.split("\n")
    
    lines.forEach { line ->
        val trimmedLine = line.trim()
        if (trimmedLine.matches(Regex("^\\d+\\s*\\..*")) || 
            trimmedLine.matches(Regex("^#{1,3}\\s*[Ss]chritt\\s+\\d+.*"))) {
            
            val stepText = trimmedLine
                .replace(Regex("^\\d+\\.\\s*"), "")
                .replace(Regex("^#{1,3}\\s*[Ss]chritt\\s+\\d+:?\\s*"), "")
            
            if (stepText.isNotEmpty()) {
                steps.add(stepText)
            }
        }
    }
    
    return steps
}