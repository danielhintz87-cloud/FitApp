package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.fitapp.data.AppRepository
import com.example.fitapp.data.Recipe
import com.example.fitapp.ui.components.InlineActions
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.design.Spacing

@Composable
fun NutritionScreen() {
    val recipes = remember { sampleRecipes() }

    recipes.forEach { recipe ->
        SectionCard(title = recipe.title, subtitle = "${recipe.minutes} min · ${recipe.calories} kcal") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Zutaten:")
                recipe.ingredients.forEach { (n, q) -> Text("• $n — $q") }
                InlineActions(
                    primaryLabel = "Zur Einkaufsliste",
                    onPrimary = { AppRepository.addShoppingItems(recipe.ingredients) },
                    secondaryLabel = "In Kalorien loggen",
                    onSecondary = { AppRepository.logFood(recipe.title, recipe.calories) }
                )
            }
        }
    }
}

private fun sampleRecipes(): List<Recipe> = listOf(
    Recipe(
        title = "Hähnchen-Bowl (Low Carb)",
        minutes = 20,
        calories = 520,
        tags = listOf("lowcarb", "protein"),
        ingredients = listOf(
            "Hähnchenbrust" to "300g",
            "Paprika" to "1 Stk",
            "Zucchini" to "1 Stk",
            "Joghurt 1,5%" to "100g",
            "Gewürze" to ""
        ),
        steps = listOf("Schneiden", "Anbraten", "Würzen", "Anrichten")
    ),
    Recipe(
        title = "Tofu-Gemüse-Pfanne",
        minutes = 18,
        calories = 480,
        tags = listOf("veggie", "protein"),
        ingredients = listOf(
            "Tofu" to "250g",
            "Brokkoli" to "300g",
            "Sojasauce" to "2 EL",
            "Sesam" to "1 EL"
        ),
        steps = listOf("Tofu anbraten", "Gemüse garen", "Sauce zugeben")
    )
)
