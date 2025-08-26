package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.UiRecipe
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.RecipeEntity
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
            3 -> ShoppingListScreen(repo)
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeList(title: String, items: List<RecipeEntity>, onFavClick: (String, Boolean) -> Unit) {
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
                    }
                }
            }
        }
    }
}
