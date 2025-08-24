package com.example.fitapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.data.Recipe
import com.example.fitapp.data.RecipePrefs
import com.example.fitapp.ui.CalorieScreen
import com.example.fitapp.ui.coach.CoachLocalStore
import com.example.fitapp.ui.coach.SavedRecipe
import com.example.fitapp.ui.design.Spacing
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreenRoot() {
    var showRecipes by remember { mutableStateOf(false) }
    var vegetarian by remember { mutableStateOf(false) }
    var highProtein by remember { mutableStateOf(false) }
    var lowCarb by remember { mutableStateOf(false) }
    var targetCalories by remember { mutableStateOf("") }
    var recipeSuggestions by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        Text(
            text = if (showRecipes) "Rezepte" else "Ernährung",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
        )
        if (showRecipes) {
            TextButton(onClick = { showRecipes = false }, modifier = Modifier.padding(horizontal = Spacing.lg)) {
                Text("← Tagebuch")
            }
            Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
                Text("Filter", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = vegetarian,
                        onClick = { vegetarian = !vegetarian },
                        label = { Text("Vegetarisch") }
                    )
                    FilterChip(
                        selected = highProtein,
                        onClick = { highProtein = !highProtein },
                        label = { Text("High Protein") }
                    )
                    FilterChip(
                        selected = lowCarb,
                        onClick = { lowCarb = !lowCarb },
                        label = { Text("Low Carb") }
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetCalories,
                        onValueChange = { targetCalories = it },
                        label = { Text("Ziel kcal") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        val prefs = RecipePrefs(
                            vegetarian = vegetarian,
                            highProtein = highProtein,
                            lowCarb = lowCarb,
                            targetCalories = targetCalories.toIntOrNull()
                        )
                        scope.launch {
                            var results = com.example.fitapp.data.ai.Ai.repo.suggestRecipes(prefs, count = 3)
                            if (results.isEmpty()) {
                                results = com.example.fitapp.data.ai.MockAiRepository().suggestRecipes(prefs, count = 3)
                            }
                            recipeSuggestions = results
                        }
                    }) {
                        Text("Rezepte vorschlagen")
                    }
                }
            }
            Spacer(Modifier.height(Spacing.md))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = Spacing.lg)
            ) {
                items(recipeSuggestions, key = { it.id }) { recipe ->
                    OutlinedCard(modifier = Modifier.width(280.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                            Text("${'$'}{recipe.calories} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            var menuOpen by remember { mutableStateOf(false) }
                            Row {
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Aktionen")
                                }
                                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Zutaten zur Einkaufsliste") },
                                        onClick = {
                                            menuOpen = false
                                            if (recipe.ingredients.isNotEmpty()) {
                                                val items = recipe.ingredients.map { it.name to it.amount }
                                                AppRepository.addShoppingItems(items)
                                            } else if (recipe.markdown != null) {
                                                val lines = recipe.markdown.lines()
                                                val items = mutableListOf<Pair<String, String>>()
                                                for (l in lines) {
                                                    val t = l.trimStart()
                                                    if (t.startsWith("- ") || t.startsWith("• ")) {
                                                        val content = t.drop(2).trim()
                                                        val split = content.split("—", " - ", "–").map { it.trim() }
                                                        val name = split.firstOrNull().orEmpty()
                                                        val qty = split.getOrNull(1) ?: ""
                                                        if (name.isNotEmpty()) items += (name to qty)
                                                    }
                                                }
                                                if (items.isNotEmpty()) AppRepository.addShoppingItems(items)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Rezept speichern") },
                                        onClick = {
                                            menuOpen = false
                                            val saved = SavedRecipe(
                                                title = recipe.title,
                                                markdown = recipe.markdown ?: "(kein Detail)",
                                                tags = listOf("AI")
                                            )
                                            CoachLocalStore.addRecipe(saved)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            TextButton(onClick = { showRecipes = true }, modifier = Modifier.padding(horizontal = Spacing.lg)) {
                Text("Rezepte entdecken")
            }
            CalorieScreen()
        }
    }
}
