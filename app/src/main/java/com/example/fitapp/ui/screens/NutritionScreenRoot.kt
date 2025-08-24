package com.example.fitapp.ui.screens

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun NutritionScreenRoot() {
    var showRecipes by remember { mutableStateOf(false) }
    if (showRecipes) {
        TextButton(onClick = { showRecipes = false }) { Text("← Tagebuch") }
        Text("Rezepte • Karten & Filter")
    } else {
        TextButton(onClick = { showRecipes = true }) { Text("Rezepte entdecken") }
        Text("Ernährung • Tagebuch")
    }
}
