package com.example.fitapp.ui.nutrition

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.RecipeEntity
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.launch

@Composable
fun NutritionScreen() {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(0) }
    var prompt by remember { mutableStateOf("10 Rezepte für Abnehmen, High-Protein, 500-700 kcal pro Portion") }
    var generating by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<UiRecipe>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val favorites by repo.favorites().collectAsState(initial = emptyList())
    val history by repo.history().collectAsState(initial = emptyList())
    val tabs = listOf("Generieren", "Favoriten", "Historie", "Einkaufsliste")

    Column(Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
            }
        }
        when (tab) {
            0 -> GenerateTab(prompt, { prompt = it }, generating, results, error, onGenerate = {
                generating = true
                scope.launch {
                    try {
                        results = repo.generateAndStoreOptimal(ctx, prompt)
                        error = null
                    } catch (e: Exception) {
                        results = emptyList()
                        error = e.message
                    } finally { generating = false }
                }
            }, onFav = { id, fav -> scope.launch { repo.setFavorite(id, fav) } }, onToShopping = { id -> scope.launch { repo.addRecipeToShoppingList(id) } }, onLog = { r -> scope.launch {
                repo.logIntake(r.calories ?: 0, "Rezept: ${'$'}{r.title}", "RECIPE", r.id)
                repo.adjustDailyGoal(java.time.LocalDate.now())
            } })
            1 -> RecipeList("Favoriten", favorites) { id, fav -> scope.launch { repo.setFavorite(id, fav) } }
            2 -> RecipeList("Historie", history) { id, fav -> scope.launch { repo.setFavorite(id, fav) } }
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
    onGenerate: () -> Unit,
    onFav: (String, Boolean) -> Unit,
    onToShopping: (String) -> Unit,
    onLog: (UiRecipe) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            label = { Text("Worauf hast du Lust? (Prompt)") }
        )
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onGenerate, enabled = !generating) {
                Text(if (generating) "Generiere…" else "Rezepte generieren")
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp,16.dp,16.dp,96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(results) { r ->
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(r.title, style = MaterialTheme.typography.titleMedium)
                        r.calories?.let { Text("~$it kcal", style = MaterialTheme.typography.labelMedium) }
                        Text(r.markdown, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var fav by remember(r.id) { mutableStateOf(false) }
                            OutlinedButton(onClick = { onToShopping(r.id) }) { Text("Zutaten → Einkaufsliste") }
                            Button(onClick = { onLog(r) }) { Text("In Tagesbilanz buchen") }
                            FilledTonalButton(onClick = { fav = !fav; onFav(r.id, fav) }) { Text(if (fav) "Favorit ✓" else "Als Favorit speichern") }
                            OutlinedButton(
                                onClick = { 
                                    scope.launch {
                                        saveToSavedRecipes(ctx, r)
                                    }
                                }
                            ) { 
                                Text("Dauerhaft speichern") 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeList(title: String, items: List<RecipeEntity>, onFavClick: (String, Boolean) -> Unit) {
    val ctx = LocalContext.current
    val repo = remember { NutritionRepository(AppDatabase.get(ctx)) }
    val scope = rememberCoroutineScope()
    
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp,16.dp,16.dp,96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

suspend fun saveToSavedRecipes(context: Context, recipe: UiRecipe) {
    val db = AppDatabase.get(context)
    
    // Parse recipe details for better categorization
    val tags = mutableListOf<String>()
    val markdown = recipe.markdown.lowercase()
    
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
    return difficultyRegex.find(markdown)?.value?.lowercase()
}

private fun extractServings(markdown: String): Int? {
    val servingsRegex = Regex("""(\d+)\s*(portion|portionen|serving|servings)""", RegexOption.IGNORE_CASE)
    return servingsRegex.find(markdown)?.groupValues?.get(1)?.toIntOrNull()
}

@Composable
private fun SimpleShoppingListTab(repo: NutritionRepository) {
    val items by repo.shoppingItems().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
