package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.fitapp.data.AppRepository
import com.example.fitapp.ui.components.InputField
import com.example.fitapp.ui.components.InlineActions
import com.example.fitapp.ui.components.NumberField
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.design.Spacing
import java.time.LocalDate

@Composable
fun CalorieScreen() {
    val settings by AppRepository.calorieSettings.collectAsState()
    val foods by AppRepository.foodLogs.collectAsState()
    val workouts by AppRepository.exerciseLogs.collectAsState()

    var foodTitle by remember { mutableStateOf("") }
    var foodKcal by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf(settings.dailyBudget.toString()) }

    val today = LocalDate.now()
    val todayFood = foods.filter { it.date == today }
    val todayOut = workouts.filter { it.date == today }.sumOf { it.caloriesOut }
    val todayIn = todayFood.sumOf { it.caloriesIn }
    val rest = (budget.toIntOrNull() ?: settings.dailyBudget) - todayIn + todayOut

    SectionCard(title = "Tagesbudget") {
        NumberField(label = "Kalorienbudget", value = budget, onValueChange = { budget = it })
        Spacer(Modifier.height(Spacing.sm))
        Text("Heute gegessen: $todayIn kcal  |  Sport: +$todayOut kcal  →  Rest: $rest kcal",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    SectionCard(title = "Food-Log hinzufügen") {
        InputField(label = "Gericht", value = foodTitle, onValueChange = { foodTitle = it })
        Spacer(Modifier.height(Spacing.sm))
        NumberField(label = "Kalorien", value = foodKcal, onValueChange = { foodKcal = it })
        InlineActions(
            primaryLabel = "Hinzufügen",
            onPrimary = {
                val kcal = foodKcal.toIntOrNull() ?: 0
                if (kcal > 0 && foodTitle.isNotBlank()) AppRepository.logFood(foodTitle, kcal)
                foodTitle = ""; foodKcal = ""
            },
            secondaryLabel = "Budget speichern",
            onSecondary = {
                val b = budget.toIntOrNull() ?: settings.dailyBudget
                AppRepository.setCalorieSettings(settings.copy(dailyBudget = b))
            }
        )
    }

    SectionCard(title = "Heute gegessen") {
        if (todayFood.isEmpty()) {
            Text("Noch keine Einträge.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                todayFood.forEach { Text("• ${it.title}  –  ${it.caloriesIn} kcal") }
            }
        }
    }
}
