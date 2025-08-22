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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.RecipePrefs
import com.example.fitapp.data.ai.AiSuggestRecipes
import com.example.fitapp.ui.components.AppFilterChip
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.design.Spacing
import com.example.fitapp.ui.model.UiRecipe
import com.example.fitapp.ui.model.toUi
import kotlinx.coroutines.launch

@Composable
fun NutritionScreen() {
    var vegetarian by remember { mutableStateOf(false) }
    var highProtein by remember { mutableStateOf(false) }
    var lowCarb by remember { mutableStateOf(false) }
    var recipes by remember { mutableStateOf<List<UiRecipe>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SectionCard(title = "Rezepte generieren", subtitle = "Mit deinen Präferenzen") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AppFilterChip("Vegetarisch", vegetarian, { vegetarian = !vegetarian })
                    AppFilterChip("High Protein", highProtein, { highProtein = !highProtein })
                    AppFilterChip("Low Carb", lowCarb, { lowCarb = !lowCarb })
                }
                Spacer(Modifier.height(Spacing.sm))
                Button(onClick = {
                    scope.launch {
                        val prefs = RecipePrefs(
                            vegetarian = vegetarian,
                            highProtein = highProtein,
                            lowCarb = lowCarb
                        )
                        val list = AiSuggestRecipes(prefs, count = 3).map { it.toUi() }
                        recipes = list
                    }
                }) { Text("Vorschläge holen") }
            }
        }

        recipes.forEach { r ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(r.title, style = MaterialTheme.typography.titleMedium)
                    Text("${r.calories} kcal · ${r.tagsLine}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!r.markdown.isNullOrBlank()) {
                        Spacer(Modifier.height(Spacing.xs))
                        Text(r.markdown!!, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text("Zutaten", style = MaterialTheme.typography.titleSmall)
                    Text(r.ingredientsText)
                    Spacer(Modifier.height(Spacing.sm))
                    Text("Zubereitung", style = MaterialTheme.typography.titleSmall)
                    Text(r.stepsText)
                }
            }
        }
    }
}
