package com.example.fitapp.ui.nutrition

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.services.CookingModeManager
import com.example.fitapp.services.ShoppingListManager
import com.example.fitapp.services.SimilarRecipesEngine
import kotlinx.coroutines.launch

/**
 * Professional Cooking Experience Screen
 * Full-screen cooking mode with AI assistant, timers, and step navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalCookingScreen(
    recipe: SavedRecipeEntity,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context) }
    val cookingManager = remember { CookingModeManager(database) }
    val shoppingManager = remember { ShoppingListManager(database) }
    val similarRecipesEngine = remember { SimilarRecipesEngine(context, database) }
    val scope = rememberCoroutineScope()
    
    // State management
    var currentMode by remember { mutableStateOf(CookingScreenMode.PREPARATION) }
    var showIngredientDialog by remember { mutableStateOf(false) }
    var showAIAssistant by remember { mutableStateOf(false) }
    var showSimilarRecipes by remember { mutableStateOf(false) }
    var keepScreenOn by remember { mutableStateOf(false) }
    
    // Cooking state
    val cookingFlow by cookingManager.cookingFlow.collectAsStateWithLifecycle()
    val currentStep by cookingManager.currentStep.collectAsStateWithLifecycle()
    val isInCookingMode by cookingManager.isInCookingMode.collectAsStateWithLifecycle()
    val stepTimers by cookingManager.stepTimers.collectAsStateWithLifecycle()
    
    // Screen wake lock (keep display active)
    LaunchedEffect(keepScreenOn) {
        // TODO: Implement screen wake lock when keepScreenOn is true
        // This would require Android system service integration
    }
    
    // Auto-start cooking mode when entering this screen
    LaunchedEffect(recipe.id) {
        if (!isInCookingMode) {
            cookingManager.startCookingMode(recipe)
        }
    }
    
    // Handle back press
    LaunchedEffect(Unit) {
        // TODO: Handle back press with confirmation if cooking is active
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with cooking controls
            CookingTopBar(
                recipe = recipe,
                currentMode = currentMode,
                onModeChange = { currentMode = it },
                onBackPressed = onBackPressed,
                onAIAssistant = { showAIAssistant = true },
                onSimilarRecipes = { showSimilarRecipes = true },
                keepScreenOn = keepScreenOn,
                onKeepScreenOnChange = { keepScreenOn = it }
            )
            
            // Main content based on current mode
            when (currentMode) {
                CookingScreenMode.PREPARATION -> {
                    PreparationModeContent(
                        recipe = recipe,
                        shoppingManager = shoppingManager,
                        onStartCooking = { currentMode = CookingScreenMode.COOKING },
                        onShowIngredients = { showIngredientDialog = true }
                    )
                }
                CookingScreenMode.COOKING -> {
                    CookingModeContent(
                        cookingFlow = cookingFlow,
                        currentStep = currentStep,
                        stepTimers = stepTimers,
                        cookingManager = cookingManager,
                        onFinishCooking = {
                            scope.launch {
                                cookingManager.finishCooking()
                                currentMode = CookingScreenMode.COMPLETED
                            }
                        }
                    )
                }
                CookingScreenMode.COMPLETED -> {
                    CompletionModeContent(
                        recipe = recipe,
                        onBackToRecipes = onBackPressed,
                        onCookAgain = { 
                            scope.launch {
                                cookingManager.startCookingMode(recipe)
                                currentMode = CookingScreenMode.COOKING
                            }
                        }
                    )
                }
            }
        }
        
        // Floating Action Button for quick actions
        if (currentMode == CookingScreenMode.COOKING) {
            FloatingActionButton(
                onClick = { showAIAssistant = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Filled.Assistant,
                    contentDescription = "AI Assistent"
                )
            }
        }
    }
    
    // Dialogs and overlays
    if (showIngredientDialog) {
        IngredientsToShoppingListDialog(
            recipe = recipe,
            shoppingManager = shoppingManager,
            onDismiss = { showIngredientDialog = false }
        )
    }
    
    if (showAIAssistant) {
        AIAssistantDialog(
            recipe = recipe,
            currentStep = currentStep,
            onDismiss = { showAIAssistant = false }
        )
    }
    
    if (showSimilarRecipes) {
        SimilarRecipesDialog(
            recipe = recipe,
            similarRecipesEngine = similarRecipesEngine,
            onDismiss = { showSimilarRecipes = false },
            onRecipeSelected = { /* Navigate to new recipe */ }
        )
    }
}

enum class CookingScreenMode {
    PREPARATION,
    COOKING,
    COMPLETED
}

/**
 * Top bar with cooking controls and mode switcher
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CookingTopBar(
    recipe: SavedRecipeEntity,
    currentMode: CookingScreenMode,
    onModeChange: (CookingScreenMode) -> Unit,
    onBackPressed: () -> Unit,
    onAIAssistant: () -> Unit,
    onSimilarRecipes: () -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = when (currentMode) {
                        CookingScreenMode.PREPARATION -> "Vorbereitung"
                        CookingScreenMode.COOKING -> "Kochmodus"
                        CookingScreenMode.COMPLETED -> "Abgeschlossen"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
            }
        },
        actions = {
            // Keep screen on toggle
            IconButton(
                onClick = { onKeepScreenOnChange(!keepScreenOn) }
            ) {
                Icon(
                    if (keepScreenOn) Icons.Filled.ScreenLockPortrait else Icons.Filled.ScreenLockRotation,
                    contentDescription = if (keepScreenOn) "Display-Sperre deaktivieren" else "Display aktiv halten",
                    tint = if (keepScreenOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // AI Assistant
            IconButton(onClick = onAIAssistant) {
                Icon(Icons.Filled.Assistant, contentDescription = "AI Assistent")
            }
            
            // Similar recipes
            IconButton(onClick = onSimilarRecipes) {
                Icon(Icons.Filled.Recommend, contentDescription = "Ähnliche Rezepte")
            }
            
            // More options
            IconButton(onClick = { /* Show more options */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Mehr Optionen")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = when (currentMode) {
                CookingScreenMode.PREPARATION -> MaterialTheme.colorScheme.surface
                CookingScreenMode.COOKING -> MaterialTheme.colorScheme.primaryContainer
                CookingScreenMode.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    )
}

/**
 * Preparation mode content - ingredient checklist and shopping list integration
 */
@Composable
private fun PreparationModeContent(
    recipe: SavedRecipeEntity,
    shoppingManager: ShoppingListManager,
    onStartCooking: () -> Unit,
    onShowIngredients: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recipe overview card
        RecipeOverviewCard(recipe = recipe)
        
        // Preparation checklist
        PreparationChecklistCard(
            recipe = recipe,
            onAddToShoppingList = onShowIngredients
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Start cooking button
        Button(
            onClick = onStartCooking,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.Restaurant, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "Kochmodus starten",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Active cooking mode content with step navigation and timers
 */
@Composable
private fun CookingModeContent(
    cookingFlow: CookingModeManager.CookingFlow?,
    currentStep: CookingModeManager.CookingStep?,
    stepTimers: Map<Int, CookingModeManager.StepTimer>,
    cookingManager: CookingModeManager,
    onFinishCooking: () -> Unit
) {
    if (cookingFlow == null || currentStep == null) {
        // Loading state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        CookingProgressIndicator(
            currentStep = cookingFlow.currentStepIndex + 1,
            totalSteps = cookingFlow.steps.size
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current step content
        CurrentStepCard(
            step = currentStep,
            stepTimer = stepTimers[cookingFlow.currentStepIndex],
            cookingManager = cookingManager,
            cookingFlow = cookingFlow
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation controls
        CookingNavigationControls(
            canGoBack = cookingFlow.currentStepIndex > 0,
            canGoNext = cookingFlow.currentStepIndex < cookingFlow.steps.size - 1,
            isLastStep = cookingFlow.currentStepIndex == cookingFlow.steps.size - 1,
            onPrevious = { cookingManager.navigateToPreviousStep() },
            onNext = { cookingManager.navigateToNextStep() },
            onMarkComplete = { cookingManager.markStepComplete() },
            onFinish = onFinishCooking
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Step overview
        StepOverviewCard(
            steps = cookingFlow.steps,
            currentStepIndex = cookingFlow.currentStepIndex
        )
    }
}

/**
 * Completion mode content with feedback and options
 */
@Composable
private fun CompletionModeContent(
    recipe: SavedRecipeEntity,
    onBackToRecipes: () -> Unit,
    onCookAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success animation/icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Perfekt gekocht! 🎉",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Ihr ${recipe.title} ist fertig!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Rating and feedback section
        CookingFeedbackCard(recipe = recipe)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackToRecipes,
                modifier = Modifier.weight(1f)
            ) {
                Text("Zu Rezepten")
            }
            
            Button(
                onClick = onCookAgain,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Nochmal kochen")
            }
        }
    }
}

/**
 * Recipe overview card for preparation mode
 */
@Composable
private fun RecipeOverviewCard(recipe: SavedRecipeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rezept-Übersicht",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                recipe.prepTime?.let { prepTime ->
                    QuickStat(
                        icon = Icons.Filled.AccessTime,
                        label = "Zeit",
                        value = "$prepTime min"
                    )
                }
                
                recipe.servings?.let { servings ->
                    QuickStat(
                        icon = Icons.Filled.Group,
                        label = "Portionen",
                        value = servings.toString()
                    )
                }
                
                recipe.difficulty?.let { difficulty ->
                    QuickStat(
                        icon = Icons.Filled.TrendingUp,
                        label = "Schwierigkeit",
                        value = difficulty
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Preparation checklist with ingredients
 */
@Composable
private fun PreparationChecklistCard(
    recipe: SavedRecipeEntity,
    onAddToShoppingList: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zutaten vorbereiten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onAddToShoppingList) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Zur Einkaufsliste")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // TODO: Parse and display ingredients with checkboxes
            Text(
                text = "Alle Zutaten bereit legen und nach Bedarf vorbereiten.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Cooking progress indicator
 */
@Composable
private fun CookingProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Schritt $currentStep von $totalSteps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${(currentStep.toFloat() / totalSteps * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = currentStep.toFloat() / totalSteps,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Current step card with timer and instructions
 */
@Composable
private fun CurrentStepCard(
    step: CookingModeManager.CookingStep,
    stepTimer: CookingModeManager.StepTimer?,
    cookingManager: CookingModeManager,
    cookingFlow: CookingModeManager.CookingFlow
) {
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Step instruction
            Text(
                text = step.instruction,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
            )
            
            // Timer section
            step.duration?.let { duration ->
                Spacer(modifier = Modifier.height(16.dp))
                
                if (stepTimer != null) {
                    ActiveTimerDisplay(
                        timer = stepTimer,
                        onToggle = { 
                            scope.launch {
                                cookingManager.toggleStepTimer(cookingFlow.currentStepIndex) 
                            }
                        }
                    )
                } else {
                    TimerStartButton(
                        duration = duration,
                        onStart = { 
                            scope.launch {
                                cookingManager.startStepTimer(
                                    cookingFlow.currentStepIndex,
                                    duration,
                                    "Schritt ${step.stepNumber}"
                                )
                            }
                        }
                    )
                }
            }
            
            // Tips if available
            if (step.tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                step.tips.forEach { tip ->
                    TipItem(tip = tip)
                }
            }
        }
    }
}

@Composable
private fun ActiveTimerDisplay(
    timer: CookingModeManager.StepTimer,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = timer.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = formatTime(timer.remainingTime),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (timer.remainingTime < 60) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onToggle) {
                Icon(
                    if (timer.isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (timer.isActive) "Pausieren" else "Fortsetzen"
                )
            }
        }
    }
}

@Composable
private fun TimerStartButton(
    duration: Int,
    onStart: () -> Unit
) {
    OutlinedButton(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.Timer, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Timer starten (${formatTime(duration)})")
    }
}

@Composable
private fun TipItem(tip: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Filled.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = tip,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Navigation controls for cooking mode
 */
@Composable
private fun CookingNavigationControls(
    canGoBack: Boolean,
    canGoNext: Boolean,
    isLastStep: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMarkComplete: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Previous button
        OutlinedButton(
            onClick = onPrevious,
            enabled = canGoBack,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.NavigateBefore, contentDescription = null)
            Text("Zurück")
        }
        
        // Mark complete button
        OutlinedButton(
            onClick = onMarkComplete,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Text("Erledigt")
        }
        
        // Next/Finish button
        Button(
            onClick = if (isLastStep) onFinish else onNext,
            enabled = canGoNext || isLastStep,
            modifier = Modifier.weight(1f),
            colors = if (isLastStep) ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ) else ButtonDefaults.buttonColors()
        ) {
            if (isLastStep) {
                Icon(Icons.Filled.RestaurantMenu, contentDescription = null)
                Text("Fertig!")
            } else {
                Text("Weiter")
                Icon(Icons.Filled.NavigateNext, contentDescription = null)
            }
        }
    }
}

/**
 * Step overview card showing all steps
 */
@Composable
private fun StepOverviewCard(
    steps: List<CookingModeManager.CookingStep>,
    currentStepIndex: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Alle Schritte",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(steps.mapIndexed { index, step -> index to step }) { (index, step) ->
                    StepOverviewItem(
                        step = step,
                        stepNumber = index + 1,
                        isActive = index == currentStepIndex,
                        isCompleted = step.isCompleted
                    )
                }
            }
        }
    }
}

@Composable
private fun StepOverviewItem(
    step: CookingModeManager.CookingStep,
    stepNumber: Int,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.outline
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer 
                           else Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Step text
        Text(
            text = step.instruction.take(60) + if (step.instruction.length > 60) "..." else "",
            style = MaterialTheme.typography.bodySmall,
            color = when {
                isActive -> MaterialTheme.colorScheme.primary
                isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.outline
            }
        )
    }
}

/**
 * Cooking feedback card for completion
 */
@Composable
private fun CookingFeedbackCard(recipe: SavedRecipeEntity) {
    var rating by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Wie war's?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Star rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { rating = index + 1 },
                        tint = if (index < rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Notizen zum Kochen (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}

// Dialog components would go here (IngredientsToShoppingListDialog, AIAssistantDialog, SimilarRecipesDialog)
// These are simplified for brevity

@Composable
private fun IngredientsToShoppingListDialog(
    recipe: SavedRecipeEntity,
    shoppingManager: ShoppingListManager,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Zutaten zur Einkaufsliste hinzufügen",
                    style = MaterialTheme.typography.titleMedium
                )
                // TODO: Implementation
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Schließen")
                }
            }
        }
    }
}

@Composable
private fun AIAssistantDialog(
    recipe: SavedRecipeEntity,
    currentStep: CookingModeManager.CookingStep?,
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🤖 AI Koch-Assistent",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                currentStep?.let { step ->
                    Text(
                        text = "Aktueller Schritt: ${step.instruction}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "💡 Tipp: ${step.tips.firstOrNull() ?: "Folgen Sie den Anweisungen Schritt für Schritt."}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verstanden")
                }
            }
        }
    }
}

@Composable
private fun SimilarRecipesDialog(
    recipe: SavedRecipeEntity,
    similarRecipesEngine: SimilarRecipesEngine,
    onDismiss: () -> Unit,
    onRecipeSelected: (SavedRecipeEntity) -> Unit
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Ähnliche Rezepte",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Nicht zufrieden? Hier sind ähnliche Alternativen:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
