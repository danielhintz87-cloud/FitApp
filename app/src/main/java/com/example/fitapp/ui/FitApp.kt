package com.example.fitapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.fitapp.ui.coach.CoachScreen

private enum class NavItem(val label: String) {
    Today("Heute"),
    Training("Training"),
    Coach("Coach"),
    Nutrition("Ernährung"),
    Shopping("Einkauf"),
    Progress("Fortschritt")
}

@Composable
fun FitApp() {
    val items = listOf(
        NavItem.Today, NavItem.Training, NavItem.Coach, NavItem.Nutrition, NavItem.Shopping, NavItem.Progress
    )
    var selected by rememberSaveable { mutableStateOf(NavItem.Today) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = selected == item,
                        onClick = { selected = item },
                        icon = {},
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (selected) {
                NavItem.Today -> TodayScreen()
                NavItem.Training -> TrainingSetupScreen()
                NavItem.Coach -> CoachScreen()
                NavItem.Nutrition -> NutritionScreen()
                NavItem.Shopping -> ShoppingListScreen()
                NavItem.Progress -> ProgressScreen()
            }
        }
    }
}

