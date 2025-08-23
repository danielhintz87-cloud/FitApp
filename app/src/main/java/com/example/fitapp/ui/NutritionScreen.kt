package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.RecipePrefs
import com.example.fitapp.data.ai.Ai
import com.example.fitapp.ui.components.AppFilterChip
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.design.Spacing
import com.example.fitapp.ui.model.UiRecipe
import com.example.fitapp.ui.model.toUi
import kotlinx.coroutines.launch

@Composable
fun NutritionScreen() {
    var prefs by remember { mutableStateOf(RecipePrefs()) }
    var recipes by remember { mutableStateOf<List<UiRecipe>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SectionCard(title = "Rezepte generieren") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AppFilterChip("Vegetarisch", prefs.vegetarian) {
                        prefs = prefs.copy(vegetarian = !prefs.vegetarian)
                    }
                    AppFilterChip("High Protein", prefs.highProtein) {
                        prefs = prefs.copy(highProtein = !prefs.highProtein)
                    }
                    AppFilterChip("Low Carb", prefs.lowCarb) {
                        prefs = prefs.copy(lowCarb = !prefs.lowCarb)
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
                Button(
                    onClick = {
                        if (loading) return@Button
                        loading = true
                        scope.launch {
                            val list = runCatching {
                                Ai.repo.suggestRecipes(prefs, count = 3).map { it.toUi() }
                            }.getOrElse { emptyList() }
                            recipes = list
                            loading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Generiere…" else "Rezepte generieren") }
            }
        }

        SectionCard(
            title = "Vorschläge",
            subtitle = if (recipes.isEmpty()) "Noch keine Rezepte generiert" else "${recipes.size} Rezepte"
        ) {
            if (recipes.isEmpty()) {
                Text("Lege Präferenzen fest und tippe auf „Rezepte generieren“.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                recipes.forEach { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            Modifier.padding(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Text(r.title, style = MaterialTheme.typography.titleMedium)
                            Text("${r.calories} kcal · ${r.tagsLine}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Zutaten:", style = MaterialTheme.typography.titleSmall)
                            Text(r.ingredientsText)
                            Text("Zubereitung:", style = MaterialTheme.typography.titleSmall)
                            Text(r.stepsText)
                        }
                    }
                }
            }
        }
    }
}
