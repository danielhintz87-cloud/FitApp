@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitapp.ui.nutrition

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.components.AiKeyGate
import kotlinx.coroutines.launch
import com.example.fitapp.ui.util.applyContentPadding

/**
 * Dedicated AI-powered recipe generation screen with user-friendly form interface
 */
@Composable
fun RecipeGenerationScreen(
    onBackPressed: () -> Unit,
    onNavigateToApiKeys: (() -> Unit)? = null,
    onNavigateToCookingMode: ((String) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()

    // Form state
    var whatToCook by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("") }
    var selectedDietaryForm by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var servings by remember { mutableIntStateOf(2) }
    var cookingTime by remember { mutableIntStateOf(30) }

    // Generation state
    var generating by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<UiRecipe>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier.fillMaxSize().applyContentPadding(contentPadding)) {
        // Top App Bar
        TopAppBar(
            title = { Text("KI Rezept Generator") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            }
        )

        AiKeyGate(
            modifier = Modifier.fillMaxSize(),
            onNavigateToApiKeys = { onNavigateToApiKeys?.invoke() },
            requireBothProviders = true
        ) { isEnabled ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Section
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
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "🍽️ Personalisierte Rezepte",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Lass die KI dein perfektes Rezept basierend auf deinen Zielen und Vorlieben erstellen",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // What to Cook Section
                item {
                    RecipeFormSection(
                        title = "Was möchtest du kochen?",
                        icon = Icons.Filled.Restaurant
                    ) {
                        OutlinedTextField(
                            value = whatToCook,
                            onValueChange = { whatToCook = it },
                            label = { Text("Beschreibe dein Wunschgericht...") },
                            placeholder = { Text("z.B. Nudeln mit Gemüse, Protein-Smoothie, Suppe...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            enabled = isEnabled
                        )
                    }
                }

                // Goals Section
                item {
                    RecipeFormSection(
                        title = "Dein Ziel",
                        icon = Icons.Filled.Flag
                    ) {
                        val goals = listOf(
                            "Muskelaufbau",
                            "Abnehmen",
                            "Ausdauer",
                            "Gesund leben",
                            "Energie steigern",
                            "Keine speziellen Ziele"
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(goals) { goal ->
                                FilterChip(
                                    onClick = { selectedGoal = if (selectedGoal == goal) "" else goal },
                                    label = { Text(goal) },
                                    selected = selectedGoal == goal,
                                    enabled = isEnabled
                                )
                            }
                        }
                    }
                }

                // Dietary Form Section
                item {
                    RecipeFormSection(
                        title = "Ernährungsform",
                        icon = Icons.Filled.Eco
                    ) {
                        val dietaryForms = listOf(
                            "Vegetarisch",
                            "Vegan",
                            "Proteinreich",
                            "Low Carb",
                            "Low Fat",
                            "Ketogen",
                            "Mediterran",
                            "Keine Einschränkungen"
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(dietaryForms) { form ->
                                FilterChip(
                                    onClick = { selectedDietaryForm = if (selectedDietaryForm == form) "" else form },
                                    label = { Text(form) },
                                    selected = selectedDietaryForm == form,
                                    enabled = isEnabled
                                )
                            }
                        }
                    }
                }

                // Servings and Time Section
                item {
                    RecipeFormSection(
                        title = "Portionen & Zeit",
                        icon = Icons.Filled.Schedule
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Servings
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Portionen", style = MaterialTheme.typography.labelMedium)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (servings > 1) servings-- },
                                        enabled = isEnabled
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Weniger")
                                    }
                                    Text(
                                        "$servings",
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.width(48.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { if (servings < 10) servings++ },
                                        enabled = isEnabled
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Mehr")
                                    }
                                }
                            }

                            // Cooking Time
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Zubereitungszeit", style = MaterialTheme.typography.labelMedium)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (cookingTime > 15) cookingTime -= 15 },
                                        enabled = isEnabled
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Weniger")
                                    }
                                    Text(
                                        "${cookingTime} min",
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.width(80.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { if (cookingTime < 120) cookingTime += 15 },
                                        enabled = isEnabled
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Mehr")
                                    }
                                }
                            }
                        }
                    }
                }

                // Allergies Section (Optional)
                item {
                    RecipeFormSection(
                        title = "Allergien & No-Gos (optional)",
                        icon = Icons.Filled.Warning
                    ) {
                        OutlinedTextField(
                            value = allergies,
                            onValueChange = { allergies = it },
                            label = { Text("Allergien oder Zutaten die du vermeiden möchtest") },
                            placeholder = { Text("z.B. Nüsse, Milchprodukte, Gluten...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            enabled = isEnabled
                        )
                    }
                }

                // Generate Button
                item {
                    Button(
                        onClick = {
                            generating = true
                            scope.launch {
                                try {
                                    val prompt = buildRecipePrompt(
                                        whatToCook, selectedGoal, selectedDietaryForm,
                                        allergies, servings, cookingTime
                                    )
                                    results = repo.generateAndStoreOptimal(ctx, prompt)
                                    error = null
                                } catch (e: Exception) {
                                    results = emptyList()
                                    error = "Fehler bei der Rezeptgenerierung:\n\n${e.message}\n\nProvider Status:\n${com.example.fitapp.ai.AppAi.getProviderStatus(ctx)}"
                                } finally { 
                                    generating = false
                                }
                            }
                        },
                        enabled = isEnabled && !generating && (whatToCook.isNotBlank() || selectedGoal.isNotBlank()),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        if (generating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Generiere Rezepte...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Rezepte generieren")
                        }
                    }
                }

                // Error Display
                if (isEnabled && error != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // Results
                items(results) { recipe ->
                    GeneratedRecipeCard(
                        recipe = recipe,
                        onFavoriteClick = { fav -> 
                            scope.launch { repo.setFavorite(recipe.id, fav) }
                        },
                        onAddToShopping = { 
                            scope.launch { repo.addRecipeToShoppingList(recipe.id) }
                        },
                        onLogCalories = { 
                            scope.launch {
                                repo.logIntake(
                                    recipe.calories ?: 0, 
                                    "Rezept: ${recipe.title}", 
                                    "RECIPE", 
                                    recipe.id
                                )
                                repo.adjustDailyGoal(java.time.LocalDate.now())
                            }
                        },
                        onSaveRecipe = { 
                            scope.launch {
                                saveToSavedRecipes(ctx, recipe)
                            }
                        },
                        onPrepareRecipe = {
                            scope.launch {
                                saveToSavedRecipes(ctx, recipe)
                                onNavigateToCookingMode?.invoke(recipe.id)
                            }
                        }
                    )
                }

                // Add bottom padding for scroll space
                item {
                    Spacer(Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
private fun RecipeFormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            content()
        }
    }
}

private fun buildRecipePrompt(
    whatToCook: String,
    goal: String,
    dietaryForm: String,
    allergies: String,
    servings: Int,
    cookingTime: Int
): String {
    val prompt = StringBuilder()
    
    prompt.append("Erstelle mir ")
    
    if (whatToCook.isNotBlank()) {
        prompt.append("Rezepte für: $whatToCook")
    } else {
        prompt.append("passende Rezepte")
    }
    
    prompt.append(" für $servings ${if (servings == 1) "Portion" else "Portionen"}")
    
    if (goal.isNotBlank() && goal != "Keine speziellen Ziele") {
        prompt.append(", Ziel: $goal")
    }
    
    if (dietaryForm.isNotBlank() && dietaryForm != "Keine Einschränkungen") {
        prompt.append(", Ernährungsform: $dietaryForm")
    }
    
    prompt.append(", Zubereitungszeit: maximal $cookingTime Minuten")
    
    if (allergies.isNotBlank()) {
        prompt.append(", zu vermeiden: $allergies")
    }
    
    prompt.append(". Bitte erstelle 3-5 detaillierte Rezepte mit Zutatenliste, Nährwerten und Schritt-für-Schritt Anleitung.")
    
    return prompt.toString()
}

@Composable
private fun GeneratedRecipeCard(
    recipe: UiRecipe,
    onFavoriteClick: (Boolean) -> Unit,
    onAddToShopping: () -> Unit,
    onLogCalories: () -> Unit,
    onSaveRecipe: () -> Unit,
    onPrepareRecipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFavorite by remember(recipe.id) { mutableStateOf(false) }
    var showFullRecipe by remember(recipe.id) { mutableStateOf(false) }
    
    ElevatedCard(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            // Header with title and calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    recipe.calories?.let { 
                        Text(
                            "~$it kcal",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Favorite button
                IconButton(
                    onClick = { 
                        isFavorite = !isFavorite
                        onFavoriteClick(isFavorite) 
                    }
                ) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Aus Favoriten entfernen" else "Zu Favoriten hinzufügen",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Recipe preview/summary
            val recipePreview = extractRecipePreview(recipe.markdown)
            Text(
                recipePreview,
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (showFullRecipe) Int.MAX_VALUE else 3
            )
            
            // Show full recipe toggle
            if (!showFullRecipe) {
                TextButton(
                    onClick = { showFullRecipe = true }
                ) {
                    Text("Vollständiges Rezept anzeigen")
                    Icon(Icons.Filled.ExpandMore, contentDescription = null)
                }
            } else {
                // Full recipe content
                Spacer(Modifier.height(8.dp))
                Text(
                    recipe.markdown,
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(
                    onClick = { showFullRecipe = false }
                ) {
                    Text("Weniger anzeigen")
                    Icon(Icons.Filled.ExpandLess, contentDescription = null)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary action - Prepare Recipe
                Button(
                    onClick = onPrepareRecipe,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Zubereiten")
                }
            }
            
            // Secondary actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToShopping,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Zutaten", style = MaterialTheme.typography.bodySmall)
                }
                
                OutlinedButton(
                    onClick = onLogCalories,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Loggen", style = MaterialTheme.typography.bodySmall)
                }
                
                OutlinedButton(
                    onClick = onSaveRecipe,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Speichern", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun extractRecipePreview(markdown: String): String {
    // Extract ingredients and basic info for preview
    val lines = markdown.lines()
    val preview = StringBuilder()
    
    for (line in lines) {
        when {
            line.trim().startsWith("**Zutaten") || line.trim().startsWith("Zutaten") -> {
                preview.append("🥘 ").append(line.trim()).append("\n")
            }
            line.trim().startsWith("**Zubereitungszeit") || line.trim().startsWith("Zubereitungszeit") -> {
                preview.append("⏱️ ").append(line.trim()).append("\n")
            }
            line.trim().startsWith("**Schwierigkeit") || line.trim().startsWith("Schwierigkeit") -> {
                preview.append("⭐ ").append(line.trim()).append("\n")
            }
            line.trim().startsWith("- ") && preview.length < 200 -> {
                preview.append(line.trim()).append("\n")
            }
        }
        if (preview.length > 250) break
    }
    
    return if (preview.isEmpty()) {
        "Leckeres Rezept mit detaillierter Anleitung..."
    } else {
        preview.toString().trim()
    }
}