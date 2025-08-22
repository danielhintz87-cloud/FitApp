package com.example.fitapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable fun DailyWorkoutScreen()   = PlaceholderScreen("Daily Workout")
@Composable fun CalorieScreen()        = PlaceholderScreen("Kalorien")
@Composable fun NutritionScreen()      = PlaceholderScreen("Ernährung")
@Composable fun ShoppingListScreen()   = PlaceholderScreen("Einkauf")
@Composable fun ProgressScreen()       = PlaceholderScreen("Fortschritt")

@Composable
private fun PlaceholderScreen(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
