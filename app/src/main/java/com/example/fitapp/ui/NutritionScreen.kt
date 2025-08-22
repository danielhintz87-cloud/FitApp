package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.components.FilterChip as AppFilterChip  // <- explizit DEINE Chip-Variante

private data class RecipeUi(
    val title: String,
    val minutes: Int,
    val kcal: Int,
    val ingredients: List<Pair<String, String>>,
    val markdown: String,
    var favorite: Boolean = false
)

@Composable
fun NutritionScreen() {
    var lowCarb by remember { mutableStateOf(true) }
    var highProtein by remember { mutableStateOf(true) }
    var vegetarian by remember { mutableStateOf(false) }
    var vegan by remember { mutableStateOf(false) }

    var recipes by remember { mutableStateOf<List<RecipeUi>>(emptyList()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(title = "Filter") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppFilterChip(text = "Low-Carb", selected = lowCarb, onClick = { lowCarb = !lowCarb })
                    AppFilterChip(text = "High-Protein", selected = highProtein, onClick = { highProtein = !highProtein })
                    AppFilterChip(text = "Vegetarisch", selected = vegetarian, onClick = { vegetarian = !vegetarian })
                    AppFilterChip(text = "Vegan", selected = vegan, onClick = { vegan = !vegan })
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { recipes = sampleRecipes(lowCarb, highProtein, vegetarian, vegan) }) {
                    Text("Rezepte generieren")
                }
            }
        }

        items(recipes, key = { it.title }) { r ->
            SectionCard(
                title = r.title,
                subtitle = "${r.minutes} min • ${r.kcal} kcal"
            ) {
                // Favorit-Toggle (kein trailing-Slot nötig)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { r.favorite = !r.favorite }) {
                        Text(if (r.favorite) "★ Favorit" else "☆ Favorit")
                    }
                }
                Spacer(Modifier.height(8.dp))

                Text("Zutaten:")
                Spacer(Modifier.height(6.dp))
                r.ingredients.forEach { (name, qty) -> Text("• $name — $qty") }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { AppRepository.logFood(r.title, r.kcal) }) {
                        Text("In Kalorien loggen")
                    }
                    Button(onClick = { AppRepository.addShoppingItems(r.ingredients) }) {
                        Text("Zur Einkaufsliste")
                    }
                }
            }
        }
    }
}

/** Einfacher lokaler Generator – später AiCoach.suggestRecipes(...) */
private fun sampleRecipes(lowCarb: Boolean, highProtein: Boolean, veg: Boolean, vegan: Boolean): List<RecipeUi> {
    val list = mutableListOf<RecipeUi>()
    if (vegan || veg) {
        list += RecipeUi(
            "Tofu-Brokkoli-Bowl",
            minutes = 20, kcal = 520,
            ingredients = listOf("Tofu" to "250 g", "Brokkoli" to "300 g", "Sojasauce" to "2 EL", "Sesam" to "1 EL"),
            markdown = "## Schritte\n1. Tofu anbraten …\n2. Brokkoli dämpfen …"
        )
    }
    if (!vegan && !veg && highProtein) {
        list += RecipeUi(
            "Hähnchen-Bowl (Low-Carb)",
            minutes = 20, kcal = 540,
            ingredients = listOf("Hähnchenbrust" to "250 g", "Paprika" to "1", "Zucchini" to "1", "Öl" to "1 EL"),
            markdown = "## Schritte\n1. Hähnchen würzen …\n2. Gemüse anbraten …"
        )
    }
    list += RecipeUi(
        "Skyr-Beeren-Snack",
        minutes = 5, kcal = 220,
        ingredients = listOf("Skyr" to "250 g", "Beeren" to "150 g", "Nüsse" to "20 g"),
        markdown = "## Schritte\n1. Alles mischen …"
    )
    return list
}
