@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitapp.ui.nutrition

import android.content.Context
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.RecipeEntity
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.ui.components.AiKeyGate
import kotlinx.coroutines.launch
import com.example.fitapp.ui.util.applyContentPadding

@Composable
fun NutritionScreen(
    onNavigateToApiKeys: (() -> Unit)? = null,
    onNavigateToCookingMode: ((String) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()

    var tab by remember { mutableIntStateOf(0) }
    var prompt by remember { mutableStateOf("10 Rezepte für Abnehmen, High-Protein, 500-700 kcal pro Portion") }
    var generating by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<UiRecipe>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val favorites by repo.favorites().collectAsState(initial = emptyList())
    val history by repo.history().collectAsState(initial = emptyList())
    val tabs = listOf("Generieren", "Favoriten", "Historie", "Einkaufsliste")

    Column(modifier.fillMaxSize().applyContentPadding(contentPadding)) {
        ScrollableTabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
            }
        }
        when (tab) {
            0 -> GenerateTab(prompt, { prompt = it }, generating, results, error, onNavigateToApiKeys, onGenerate = {
                generating = true
                scope.launch {
                    try {
                        results = repo.generateAndStoreOptimal(ctx, prompt)
                        error = null
                    } catch (e: Exception) {
                        results = emptyList()
                        error = "Fehler bei der Rezeptgenerierung:\n\n${e.message}\n\nProvider Status:\n${com.example.fitapp.ai.AppAi.getProviderStatus(ctx)}"
                    } finally { generating = false }
                }
            }, onFav = { id, fav -> scope.launch { repo.setFavorite(id, fav) } }, onToShopping = { id -> scope.launch { repo.addRecipeToShoppingList(id) } }, onLog = { r -> scope.launch {
                repo.logIntake(r.calories ?: 0, "Rezept: ${'$'}{r.title}", "RECIPE", r.id)
                repo.adjustDailyGoal(java.time.LocalDate.now())
            } })
            1 -> RecipeList("Favoriten", favorites, onFavClick = { id, fav -> scope.launch { repo.setFavorite(id, fav) } }, contentPadding = contentPadding)
            2 -> RecipeList("Historie", history, onFavClick = { id, fav -> scope.launch { repo.setFavorite(id, fav) } }, contentPadding = contentPadding)
            3 -> SimpleShoppingListTab(repo)
        }
    }
}

@Composable
private fun GenerateTab(
    prompt: String,
    onPromptChange: (String) -> Unit,
    generating: Boolean,
    results: List<UiRecipe>,
    error: String?,
    onNavigateToApiKeys: (() -> Unit)? = null,
    onGenerate: () -> Unit,
    onFav: (String, Boolean) -> Unit,
    onToShopping: (String) -> Unit,
    onLog: (UiRecipe) -> Unit,
    onNavigateToCookingMode: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    AiKeyGate(
        modifier = Modifier.fillMaxSize(),
    onNavigateToApiKeys = { onNavigateToApiKeys?.invoke() },
        requireBothProviders = true
    ) { isEnabled ->
        Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Worauf hast du Lust? (Prompt)") },
                enabled = isEnabled
            )
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGenerate, 
                    enabled = isEnabled && !generating
                ) {
                    Text(if (generating) "Generiere…" else "Rezepte generieren")
                }
            }
            
            // Only show error if it's not an API key related error and keys are available
            if (isEnabled) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
            }
            
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp,16.dp,16.dp,96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { r ->
                    IndividualRecipeCard(
                        recipe = r,
                        onFavoriteClick = { fav -> onFav(r.id, fav) },
                        onAddToShopping = { onToShopping(r.id) },
                        onLogCalories = { onLog(r) },
                        onSaveRecipe = { 
                            scope.launch {
                                saveToSavedRecipes(ctx, r)
                            }
                        },
                        onPrepareRecipe = {
                            scope.launch {
                                // Save recipe first, then navigate to cooking mode
                                saveToSavedRecipes(ctx, r)
                                onNavigateToCookingMode?.invoke(r.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeList(
    title: String, 
    items: List<RecipeEntity>, 
    onFavClick: (String, Boolean) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier.fillMaxSize(), 
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp + contentPadding.calculateBottomPadding()
        ), 
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(title, style = MaterialTheme.typography.titleLarge) }
        items(items) { r ->
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(r.title, style = MaterialTheme.typography.titleMedium)
                    r.calories?.let { Text("~$it kcal", style = MaterialTheme.typography.labelMedium) }
                    Text(r.markdown, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { onFavClick(r.id, true) }) { Text("Favorit ✓") }
                        OutlinedButton(onClick = { onFavClick(r.id, false) }) { Text("Favorit entfernen") }
                        Button(
                            onClick = { 
                                scope.launch {
                                    repo.logIntake(
                                        kcal = r.calories ?: 0,
                                        label = "Gekocht: ${r.title}",
                                        source = "RECIPE",
                                        refId = r.id
                                    )
                                }
                            }
                        ) { 
                            Text("Gekocht - zu Tagesbilanz hinzufügen") 
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndividualRecipeCard(
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
                    Text("Rezept zubereiten")
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

suspend fun saveToSavedRecipes(context: Context, recipe: UiRecipe) {
    val db = AppDatabase.get(context)
    
    // Parse recipe details for better categorization
    val tags = mutableListOf<String>()
    val markdown = recipe.markdown.lowercase(java.util.Locale.ROOT)
    
    // Detect dietary tags
    if (markdown.contains("vegetarisch") || markdown.contains("vegetarian")) tags.add("vegetarian")
    if (markdown.contains("vegan")) tags.add("vegan")
    if (markdown.contains("protein") || markdown.contains("eiweiß")) tags.add("high-protein")
    if (markdown.contains("low-carb") || markdown.contains("kohlenhydratarm")) tags.add("low-carb")
    if (markdown.contains("low-fat") || markdown.contains("fettarm")) tags.add("low-fat")
    if (markdown.contains("glutenfrei") || markdown.contains("gluten-free")) tags.add("gluten-free")
    
    // Parse ingredients from markdown
    val ingredients = extractIngredients(recipe.markdown)
    
    // Parse prep time and difficulty
    val prepTime = extractPrepTime(recipe.markdown)
    val difficulty = extractDifficulty(recipe.markdown)
    val servings = extractServings(recipe.markdown)
    
    val savedRecipe = SavedRecipeEntity(
        id = recipe.id,
        title = recipe.title,
        markdown = recipe.markdown,
        calories = recipe.calories,
        imageUrl = recipe.imageUrl,
        ingredients = ingredients.joinToString(","),
        tags = tags.joinToString(","),
        prepTime = prepTime,
        difficulty = difficulty,
        servings = servings
    )
    
    db.savedRecipeDao().insert(savedRecipe)
}

private fun extractIngredients(markdown: String): List<String> {
    val ingredients = mutableListOf<String>()
    val lines = markdown.lines()
    var inIngredients = false
    
    for (line in lines) {
        when {
            line.contains("Zutaten", ignoreCase = true) || line.contains("Ingredients", ignoreCase = true) -> {
                inIngredients = true
            }
            line.startsWith("##") && inIngredients -> {
                break
            }
            inIngredients && (line.startsWith("- ") || line.startsWith("* ")) -> {
                ingredients.add(line.substring(2).trim())
            }
        }
    }
    return ingredients
}

private fun extractPrepTime(markdown: String): Int? {
    val timeRegex = Regex("""(\d+)\s*(min|minute|minuten)""", RegexOption.IGNORE_CASE)
    return timeRegex.find(markdown)?.groupValues?.get(1)?.toIntOrNull()
}

private fun extractDifficulty(markdown: String): String? {
    val difficultyRegex = Regex("""(einfach|leicht|mittel|schwer|schwierig|easy|medium|hard)""", RegexOption.IGNORE_CASE)
    return difficultyRegex.find(markdown)?.value?.lowercase(java.util.Locale.ROOT)
}

private fun extractServings(markdown: String): Int? {
    val servingsRegex = Regex("""(\d+)\s*(portion|portionen|serving|servings)""", RegexOption.IGNORE_CASE)
    return servingsRegex.find(markdown)?.groupValues?.get(1)?.toIntOrNull()
}

@Composable
private fun SimpleShoppingListTab(
    repo: NutritionRepository,
    modifier: Modifier = Modifier
) {
    val items by repo.shoppingItems().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { 
            Text("Einkaufsliste", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
        }
        
        items(items) { item ->
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = { checked ->
                            scope.launch {
                                repo.setItemChecked(item.id, checked)
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (item.checked) TextDecoration.LineThrough else null
                        )
                        item.quantity?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                repo.deleteItem(item.id)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                    }
                }
            }
        }
        
        if (items.isEmpty()) {
            item {
                Text(
                    "Keine Artikel in der Einkaufsliste",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}
