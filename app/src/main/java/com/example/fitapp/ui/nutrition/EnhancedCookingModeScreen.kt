package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.CookingAssistance
import com.example.fitapp.ai.CookingAssistanceRequest
import com.example.fitapp.ai.generateCookingTips
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.CookingSessionEntity
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.nutrition.components.AIAssistanceCard
import com.example.fitapp.ui.nutrition.components.ProgressDocumentationCard
import com.example.fitapp.ui.nutrition.components.TimerManagementCard
import com.example.fitapp.ui.nutrition.components.VoiceControlCard
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import android.app.Activity
import android.view.WindowManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCookingModeScreen(
    recipe: SavedRecipeEntity,
    onBackPressed: () -> Unit,
    onFinishCooking: () -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val repo = remember { NutritionRepository(db) }
    val scope = rememberCoroutineScope()
    
    // Enhanced state management
    var currentStep by remember { mutableIntStateOf(0) }
    var showIngredients by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var keepScreenOn by remember { mutableStateOf(true) }
    var showEnhancedFeatures by remember { mutableStateOf(true) }
    
    // Cooking session management
    var cookingSession by remember { mutableStateOf<CookingSessionEntity?>(null) }
    var sessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    
    // Timer management
    val timerManager = remember { CookingTimerManager(ctx) }
    
    // AI assistance
    var cookingAssistance by remember { mutableStateOf<CookingAssistance?>(null) }
    var isLoadingAI by remember { mutableStateOf(false) }
    var isListeningForVoice by remember { mutableStateOf(false) }
    
    // Parse recipe steps from markdown
    val steps = remember(recipe.markdown) {
        parseRecipeSteps(recipe.markdown)
    }
    
    // Create cooking session on start
    LaunchedEffect(recipe.id) {
        val session = CookingSessionEntity(
            id = sessionId,
            recipeId = recipe.id,
            startTime = System.currentTimeMillis() / 1000,
            endTime = null,
            notes = null,
            photos = "",
            rating = null,
            difficulty = null,
            modifications = null
        )
        db.cookingSessionDao().insert(session)
        cookingSession = session
    }
    
    // Load AI cooking assistance when step changes
    LaunchedEffect(currentStep) {
        if (currentStep < steps.size) {
            isLoadingAI = true
            try {
                val ingredients = parseIngredients(recipe.markdown)
                val request = CookingAssistanceRequest(
                    recipeName = recipe.title,
                    currentStep = currentStep + 1,
                    totalSteps = steps.size,
                    ingredients = ingredients,
                    userSkillLevel = "intermediate", // Could be made configurable
                    cookingMethod = detectCookingMethod(steps.getOrNull(currentStep) ?: ""),
                    estimatedCookingTime = null
                )
                
                val result = AppAi.generateCookingTips(ctx, request)
                if (result.isSuccess) {
                    cookingAssistance = result.getOrNull()
                }
            } catch (e: Exception) {
                // Fallback to basic assistance
                android.util.Log.w("CookingMode", "AI assistance failed", e)
            } finally {
                isLoadingAI = false
            }
        }
    }
    
    // Keep screen on functionality
    DisposableEffect(keepScreenOn) {
        val activity = ctx as? Activity
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { 
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    Column(Modifier.fillMaxSize()) {
        // Enhanced Top App Bar
        TopAppBar(
            title = { 
                Column {
                    Text("Enhanced Kochmodus", style = MaterialTheme.typography.titleMedium)
                    Text(recipe.title, style = MaterialTheme.typography.bodyMedium)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { showIngredients = true }) {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Zutaten")
                }
                IconButton(onClick = { showEnhancedFeatures = !showEnhancedFeatures }) {
                    Icon(
                        if (showEnhancedFeatures) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Erweiterte Features"
                    )
                }
                IconButton(onClick = { keepScreenOn = !keepScreenOn }) {
                    Icon(
                        if (keepScreenOn) Icons.Filled.ScreenLockPortrait else Icons.Filled.ScreenLockRotation,
                        contentDescription = "Display an/aus"
                    )
                }
            }
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Indicator
            Card {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Schritt ${currentStep + 1} von ${steps.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${((currentStep + 1).toFloat() / steps.size.toFloat() * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (currentStep + 1).toFloat() / steps.size.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Enhanced Features Section
            if (showEnhancedFeatures) {
                // Timer Management
                TimerManagementCard(
                    timerManager = timerManager,
                    sessionId = sessionId
                )
                
                // AI Assistance
                AIAssistanceCard(
                    cookingAssistance = cookingAssistance,
                    isLoading = isLoadingAI
                )
                
                // Progress Documentation
                ProgressDocumentationCard(
                    cookingSession = cookingSession,
                    currentStep = currentStep + 1,
                    totalSteps = steps.size,
                    onTakePhoto = {
                        // TODO: Implement camera functionality
                        // For now, just show a placeholder
                    },
                    onAddNote = { note ->
                        scope.launch {
                            cookingSession?.let { session ->
                                val updatedSession = session.copy(notes = note)
                                db.cookingSessionDao().update(updatedSession)
                                cookingSession = updatedSession
                            }
                        }
                    }
                )
                
                // Voice Control
                VoiceControlCard(
                    onVoiceCommand = { command ->
                        when {
                            command.contains("nächster", ignoreCase = true) -> {
                                if (currentStep < steps.size - 1) {
                                    currentStep++
                                }
                            }
                            command.contains("timer", ignoreCase = true) -> {
                                // Extract timer duration and create timer
                                val minutes = command.filter { it.isDigit() }.toIntOrNull() ?: 5
                                scope.launch {
                                    timerManager.createTimer(sessionId, "Sprach-Timer", minutes * 60L)
                                }
                            }
                            command.contains("notiz", ignoreCase = true) -> {
                                // TODO: Open voice note recording
                            }
                        }
                    },
                    isListening = isListeningForVoice
                )
            }
            
            // Current Step Content
            if (steps.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { currentStep = maxOf(0, currentStep - 1) },
                    enabled = currentStep > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
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
    }
    
    // Ingredients Dialog (same as original)
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
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        val ingredients = parseIngredients(recipe.markdown)
                        ingredients.forEach { ingredient ->
                            Text(
                                "• $ingredient",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Finish Cooking Dialog (enhanced)
    if (showFinishDialog) {
        var rating by remember { mutableIntStateOf(0) }
        var finalNotes by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Kochen abgeschlossen!") },
            text = { 
                Column {
                    Text("Wie war das Kocherlebnis?")
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Rating stars
                    Row {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 }
                            ) {
                                Icon(
                                    if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "${index + 1} Sterne",
                                    tint = if (index < rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = finalNotes,
                        onValueChange = { finalNotes = it },
                        label = { Text("Abschließende Notizen") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    recipe.calories?.let {
                        Text("Möchten Sie das Rezept ($it kcal) zum Ernährungstagebuch hinzufügen?", 
                             style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            // Finish cooking session
                            cookingSession?.let { session ->
                                db.cookingSessionDao().finishSession(
                                    session.id,
                                    System.currentTimeMillis() / 1000,
                                    if (rating > 0) rating else null,
                                    finalNotes.takeIf { it.isNotBlank() }
                                )
                            }
                            
                            // Mark recipe as cooked
                            db.savedRecipeDao().markAsCooked(recipe.id)
                            
                            // Add to nutrition log
                            recipe.calories?.let { kcal ->
                                repo.logIntake(kcal, "Gekocht: ${recipe.title}", "RECIPE", recipe.id)
                                repo.adjustDailyGoal(LocalDate.now())
                            }
                            
                            // Clear session timers
                            timerManager.clearSessionTimers(sessionId)
                            
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
                            // Finish session without nutrition logging
                            cookingSession?.let { session ->
                                db.cookingSessionDao().finishSession(
                                    session.id,
                                    System.currentTimeMillis() / 1000,
                                    if (rating > 0) rating else null,
                                    finalNotes.takeIf { it.isNotBlank() }
                                )
                            }
                            
                            db.savedRecipeDao().markAsCooked(recipe.id)
                            timerManager.clearSessionTimers(sessionId)
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

// Helper functions (moved from original CookingModeScreen.kt)
private fun detectCookingMethod(stepText: String): String? {
    return when {
        stepText.contains("braten", ignoreCase = true) -> "frying"
        stepText.contains("kochen", ignoreCase = true) -> "boiling"
        stepText.contains("backen", ignoreCase = true) -> "baking"
        stepText.contains("grillen", ignoreCase = true) -> "grilling"
        stepText.contains("dämpfen", ignoreCase = true) -> "steaming"
        else -> null
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
            line.contains("Instructions", ignoreCase = true) -> {
                inInstructions = true
            }
            line.startsWith("##") && inInstructions -> {
                break // Next section
            }
            inInstructions && (line.startsWith("- ") || line.startsWith("* ")) -> {
                steps.add(line.substring(2).trim())
            }
            inInstructions && line.matches(Regex("^\\d+\\..*")) -> {
                // Numbered step
                steps.add(line.substringAfter('.').trim())
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