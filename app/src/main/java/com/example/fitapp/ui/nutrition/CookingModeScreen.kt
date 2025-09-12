package com.example.fitapp.ui.nutrition

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.MealEntryEntity
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.services.CookingModeManager
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    recipe: SavedRecipeEntity,
    onBackPressed: () -> Unit,
    onFinishCooking: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val repo = remember { NutritionRepository(db) }
    val cookingManager = remember { CookingModeManager(db) }
    val scope = rememberCoroutineScope()

    // Enhanced cooking mode with timer support
    var currentStep by remember { mutableIntStateOf(0) }
    var showIngredients by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var keepScreenOn by remember { mutableStateOf(true) }

    // Timer states
    val stepTimers by cookingManager.stepTimers.collectAsState()
    val currentTimer = stepTimers[currentStep]

    // Parse recipe steps from markdown
    val steps =
        remember(recipe.markdown) {
            parseRecipeSteps(recipe.markdown)
        }

    // Track ingredient completion state
    val ingredients = remember(recipe.markdown) { parseIngredients(recipe.markdown) }
    var checkedIngredients by remember { mutableStateOf(setOf<Int>()) }

    // Keep screen on in cooking mode
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ingredients progress indicator
                    if (ingredients.isNotEmpty()) {
                        Text(
                            "${checkedIngredients.size}/${ingredients.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                if (checkedIngredients.size == ingredients.size) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(onClick = { showIngredients = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = "Zutaten",
                            tint =
                                if (checkedIngredients.size == ingredients.size && ingredients.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                    IconButton(onClick = { keepScreenOn = !keepScreenOn }) {
                        Icon(
                            if (keepScreenOn) Icons.Filled.ScreenLockPortrait else Icons.Filled.ScreenLockRotation,
                            contentDescription = "Display an/aus",
                        )
                    }
                }
            },
        )

        // Progress Indicator
        Column(Modifier.padding(16.dp)) {
            Text(
                "Schritt ${currentStep + 1} von ${steps.size}",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (steps.isNotEmpty()) (currentStep + 1).toFloat() / steps.size.toFloat() else 0f },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Current Step Content
        if (steps.isNotEmpty()) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                            .padding(bottom = contentPadding.calculateBottomPadding()),
                ) {
                    Text(
                        "Schritt ${currentStep + 1}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        steps[currentStep],
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                    )

                    // Timer Section for Current Step
                    Spacer(Modifier.height(24.dp))
                    TimerSection(
                        currentStep = currentStep,
                        currentTimer = currentTimer,
                        stepText = steps[currentStep],
                        onStartTimer = { duration ->
                            scope.launch {
                                cookingManager.startStepTimer(currentStep, duration, "Schritt ${currentStep + 1} Timer")
                            }
                        },
                        onToggleTimer = {
                            scope.launch {
                                cookingManager.toggleStepTimer(currentStep)
                            }
                        },
                    )
                }
            }
        }

        // Navigation Buttons
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { currentStep = maxOf(0, currentStep - 1) },
                enabled = currentStep > 0,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Zurück")
            }

            if (currentStep < steps.size - 1) {
                Button(
                    onClick = { currentStep = minOf(steps.size - 1, currentStep + 1) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Weiter")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                Button(
                    onClick = { showFinishDialog = true },
                    modifier = Modifier.weight(1f),
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
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f),
            ) {
                Column(Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text("Zutaten") },
                        navigationIcon = {
                            IconButton(onClick = { showIngredients = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Schließen")
                            }
                        },
                        actions = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val ingredients = parseIngredients(recipe.markdown)
                                        ingredients.forEach { ingredient ->
                                            val category = categorizeIngredient(ingredient)
                                            db.shoppingDao().insert(
                                                com.example.fitapp.data.db.ShoppingItemEntity(
                                                    name = ingredient,
                                                    quantity = null,
                                                    unit = null,
                                                    category = category,
                                                    fromRecipeId = recipe.id,
                                                ),
                                            )
                                        }
                                        showIngredients = false
                                    }
                                },
                            ) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Alle zur Liste")
                            }
                        },
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp + contentPadding.calculateBottomPadding(),
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(ingredients.size) { index ->
                            val isChecked = checkedIngredients.contains(index)
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
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            checkedIngredients =
                                                if (checked) {
                                                    checkedIngredients + index
                                                } else {
                                                    checkedIngredients - index
                                                }
                                        },
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        ingredients[index],
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                        color = if (isChecked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                    )
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val category = categorizeIngredient(ingredients[index])
                                                db.shoppingDao().insert(
                                                    com.example.fitapp.data.db.ShoppingItemEntity(
                                                        name = ingredients[index],
                                                        quantity = null,
                                                        unit = null,
                                                        category = category,
                                                        fromRecipeId = recipe.id,
                                                    ),
                                                )
                                            }
                                        },
                                    ) {
                                        Icon(
                                            Icons.Filled.Add,
                                            contentDescription = "Zur Einkaufsliste hinzufügen",
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Finish Cooking Dialog
    if (showFinishDialog) {
        RecipeToDiaryDialog(
            recipe = recipe,
            onDismiss = { showFinishDialog = false },
            onConfirm = { servings, weightInGrams ->
                scope.launch {
                    // Mark recipe as cooked
                    db.savedRecipeDao().markAsCooked(recipe.id)

                    // Add recipe to diary with enhanced support
                    val mealEntry =
                        MealEntryEntity(
                            foodItemId = null,
                            recipeId = recipe.id,
                            date = LocalDate.now().toString(),
                            mealType = "dinner", // Default, could be made selectable
                            quantityGrams = weightInGrams ?: 0f,
                            servings = servings,
                            notes = "Gekocht am ${LocalDate.now()}",
                        )

                    db.mealEntryDao().insert(mealEntry)

                    // Also log in intake for compatibility
                    recipe.calories?.let { kcal ->
                        val adjustedCalories =
                            if (servings != null) {
                                (kcal * servings).toInt()
                            } else if (weightInGrams != null) {
                                // Assume recipe calories are per 100g if weight is specified
                                ((kcal * weightInGrams) / 100f).toInt()
                            } else {
                                kcal
                            }

                        repo.logIntake(adjustedCalories, "Gekocht: ${recipe.title}", "RECIPE", recipe.id)
                        repo.adjustDailyGoal(LocalDate.now())
                    }

                    showFinishDialog = false
                    onFinishCooking()
                }
            },
            onSkip = {
                scope.launch {
                    // Just mark as cooked without adding to nutrition log
                    db.savedRecipeDao().markAsCooked(recipe.id)
                    showFinishDialog = false
                    onFinishCooking()
                }
            },
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

private fun categorizeIngredient(ingredient: String): String {
    val lowercaseIngredient = ingredient.lowercase(java.util.Locale.ROOT)

    return when {
        // Fruits & Vegetables
        lowercaseIngredient.contains("apfel") || lowercaseIngredient.contains("banane") ||
            lowercaseIngredient.contains("orange") || lowercaseIngredient.contains("tomate") ||
            lowercaseIngredient.contains("zwiebel") || lowercaseIngredient.contains("kartoffel") ||
            lowercaseIngredient.contains("möhre") || lowercaseIngredient.contains("salat") ||
            lowercaseIngredient.contains("paprika") || lowercaseIngredient.contains("gurke") -> "Obst & Gemüse"

        // Meat & Fish
        lowercaseIngredient.contains("fleisch") || lowercaseIngredient.contains("huhn") ||
            lowercaseIngredient.contains("rind") || lowercaseIngredient.contains("schwein") ||
            lowercaseIngredient.contains("fisch") || lowercaseIngredient.contains("lachs") ||
            lowercaseIngredient.contains("thunfisch") || lowercaseIngredient.contains("wurst") -> "Fleisch & Fisch"

        // Dairy
        lowercaseIngredient.contains("milch") || lowercaseIngredient.contains("käse") ||
            lowercaseIngredient.contains("joghurt") || lowercaseIngredient.contains("butter") ||
            lowercaseIngredient.contains("sahne") || lowercaseIngredient.contains("quark") -> "Milchprodukte"

        // Pantry items
        lowercaseIngredient.contains("mehl") || lowercaseIngredient.contains("zucker") ||
            lowercaseIngredient.contains("salz") || lowercaseIngredient.contains("pfeffer") ||
            lowercaseIngredient.contains("öl") || lowercaseIngredient.contains("essig") ||
            lowercaseIngredient.contains("reis") || lowercaseIngredient.contains("nudeln") ||
            lowercaseIngredient.contains("pasta") -> "Grundnahrungsmittel"

        // Bread & Bakery
        lowercaseIngredient.contains("brot") || lowercaseIngredient.contains("brötchen") ||
            lowercaseIngredient.contains("semmel") -> "Bäckerei"

        else -> "Sonstiges"
    }
}

@Composable
private fun TimerSection(
    currentStep: Int,
    currentTimer: CookingModeManager.StepTimer?,
    stepText: String,
    onStartTimer: (Int) -> Unit,
    onToggleTimer: () -> Unit,
) {
    // Extract timer duration from step text
    val timerMatch =
        remember(stepText) {
            Regex("(\\d+)\\s*(min|minuten|sek|sekunden)", RegexOption.IGNORE_CASE).find(stepText)
        }

    val suggestedDuration =
        timerMatch?.let { match ->
            val value = match.groupValues[1].toIntOrNull() ?: 0
            val unit = match.groupValues[2].lowercase()
            when {
                unit.startsWith("min") -> value * 60 // minutes to seconds
                unit.startsWith("sek") -> value // seconds
                else -> value * 60 // default to minutes
            }
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (currentTimer?.isActive == true) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = "Timer",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Schritt-Timer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                if (currentTimer?.isActive == true) {
                    // Show running indicator
                    Text(
                        "AKTIV",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Timer display
            currentTimer?.let { timer ->
                Text(
                    formatTime(timer.remainingTime),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (timer.remainingTime <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.height(12.dp))

                // Timer controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onToggleTimer,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            if (timer.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (timer.isPaused) "Fortsetzen" else "Pausieren",
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (timer.isPaused) "Fortsetzen" else "Pausieren")
                    }
                }
            } ?: run {
                // No active timer - show start options
                suggestedDuration?.let { duration ->
                    Text(
                        "Empfohlene Zeit: ${formatTime(duration)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { onStartTimer(duration) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Timer starten (${formatTime(duration)})")
                    }
                } ?: run {
                    // Manual timer input
                    Text(
                        "Manueller Timer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Quick timer buttons
                        OutlinedButton(onClick = { onStartTimer(60) }) { // 1 minute
                            Text("1 Min")
                        }
                        OutlinedButton(onClick = { onStartTimer(300) }) { // 5 minutes
                            Text("5 Min")
                        }
                        OutlinedButton(onClick = { onStartTimer(600) }) { // 10 minutes
                            Text("10 Min")
                        }
                        OutlinedButton(onClick = { onStartTimer(1200) }) { // 20 minutes
                            Text("20 Min")
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "%d:%02d".format(minutes, remainingSeconds)
    } else {
        "%ds".format(remainingSeconds)
    }
}

@Composable
fun CookingModeFromId(
    recipeId: String,
    onBackPressed: () -> Unit,
    onFinishCooking: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    var recipe by remember { mutableStateOf<SavedRecipeEntity?>(null) }

    LaunchedEffect(recipeId) {
        recipe = db.savedRecipeDao().getRecipe(recipeId)
    }

    recipe?.let { savedRecipe ->
        CookingModeScreen(
            recipe = savedRecipe,
            onBackPressed = onBackPressed,
            onFinishCooking = onFinishCooking,
            contentPadding = contentPadding,
        )
    } ?: run {
        // Loading or recipe not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Rezept wird geladen...")
            }
        }
    }
}

@Composable
private fun RecipeToDiaryDialog(
    recipe: SavedRecipeEntity,
    onDismiss: () -> Unit,
    onConfirm: (servings: Float?, weightInGrams: Float?) -> Unit,
    onSkip: () -> Unit,
) {
    var selectedMode by remember { mutableStateOf("servings") } // "servings" or "weight"
    var servingsText by remember { mutableStateOf(recipe.servings?.toString() ?: "1") }
    var weightText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zum Tagebuch hinzufügen") },
        text = {
            Column {
                Text("Wie möchten Sie das Rezept zu Ihrem Ernährungstagebuch hinzufügen?")

                Spacer(Modifier.height(16.dp))

                // Mode selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        onClick = { selectedMode = "servings" },
                        label = { Text("Portionen") },
                        selected = selectedMode == "servings",
                        modifier = Modifier.weight(1f),
                    )
                    FilterChip(
                        onClick = { selectedMode = "weight" },
                        label = { Text("Gewicht") },
                        selected = selectedMode == "weight",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Input based on selected mode
                when (selectedMode) {
                    "servings" -> {
                        OutlinedTextField(
                            value = servingsText,
                            onValueChange = { servingsText = it },
                            label = { Text("Anzahl Portionen") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        recipe.servings?.let { defaultServings ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Standard: $defaultServings Portionen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    "weight" -> {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Gewicht in Gramm") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Geben Sie das tatsächliche Gewicht der verzehrten Menge ein",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Nutrition preview
                recipe.calories?.let { calories ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Geschätzte Nährwerte:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                            )

                            val multiplier =
                                when (selectedMode) {
                                    "servings" -> servingsText.toFloatOrNull() ?: 1f
                                    "weight" -> (weightText.toFloatOrNull() ?: 100f) / 100f
                                    else -> 1f
                                }

                            Text("Kalorien: ${(calories * multiplier).toInt()} kcal")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedMode) {
                        "servings" -> {
                            val servings = servingsText.toFloatOrNull()
                            if (servings != null && servings > 0) {
                                onConfirm(servings, null)
                            }
                        }
                        "weight" -> {
                            val weight = weightText.toFloatOrNull()
                            if (weight != null && weight > 0) {
                                onConfirm(null, weight)
                            }
                        }
                    }
                },
                enabled =
                    when (selectedMode) {
                        "servings" -> servingsText.toFloatOrNull()?.let { it > 0 } ?: false
                        "weight" -> weightText.toFloatOrNull()?.let { it > 0 } ?: false
                        else -> false
                    },
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSkip) {
                    Text("Überspringen")
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }
            }
        },
    )
}
