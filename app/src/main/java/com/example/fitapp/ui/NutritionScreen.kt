package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

import com.example.fitapp.data.*
import com.example.fitapp.data.ai.Ai
import com.example.fitapp.ui.components.*
import com.example.fitapp.ui.design.Spacing

// WICHTIG: unseren FilterChip klar vom M3-FilterChip unterscheiden
import com.example.fitapp.ui.components.FilterChip as AppFilterChip

@Composable
fun NutritionScreen() {
    var lowCarb by remember { mutableStateOf(false) }
    var highProtein by remember { mutableStateOf(false) }
    var vegetarian by remember { mutableStateOf(false) }
    var vegan by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }

    val scope = rememberCoroutineScope()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            SectionCard(title = "Filter") {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AppFilterChip("Low-Carb", lowCarb) { lowCarb = !lowCarb }
                    AppFilterChip("High-Protein", highProtein) { highProtein = !highProtein }
                    AppFilterChip("Vegetarisch", vegetarian) { vegetarian = !vegetarian }
                    AppFilterChip("Vegan", vegan) { vegan = !vegan }
                }

                Spacer(Modifier.height(Spacing.md))
                PrimaryButton(
                    label = if (isLoading) "Generiere…" else "Rezepte generieren",
                    onClick = {
                        if (isLoading) return@PrimaryButton
                        isLoading = true
                        val prefs = RecipePrefs(
                            lowCarb = lowCarb,
                            highProtein = highProtein,
                            vegetarian = vegetarian,
                            vegan = vegan
                        )
                        scope.launch {
                            try {
                                recipes = Ai.coach.suggestRecipes(prefs, count = 5)
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }

        items(recipes, key = { it.id }) { r ->
            SectionCard(
                title = r.title,
                subtitle = "${r.timeMin} min · ${r.calories} kcal"
            ) {
                if (!r.markdown.isNullOrBlank()) {
                    MarkdownText(r.markdown!!)
                    Spacer(Modifier.height(Spacing.sm))
                } else {
                    // Fallback kompakt
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Zutaten:", style = MaterialTheme.typography.titleSmall)
                        r.ingredients.forEach { ing -> Text("• ${ing.name} — ${ing.amount}") }
                    }
                    Spacer(Modifier.height(Spacing.sm))
                }

                InlineActions(
                    primaryLabel = "In Kalorien loggen",
                    onPrimary = { AppRepository.logFood(r.title, r.calories) },
                    secondaryLabel = "Zur Einkaufsliste",
                    onSecondary = {
                        val pairs = r.ingredients.map { it.name to it.amount }
                        AppRepository.addShoppingItems(pairs)
                    }
                )
            }
        }

        // Abstand unterhalb der Bottom Bar
        item { Spacer(Modifier.height(96.dp)) }
    }
}
