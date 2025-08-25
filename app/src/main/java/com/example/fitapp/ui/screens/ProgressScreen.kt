package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Fortschritt",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Kommende Features",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "• Gewichtsverlauf tracking\n• Körpermaße tracking\n• Trainingsfortschritt\n• Statistiken und Diagramme",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun NutritionScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Ernährung",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Ernährungsoptionen",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Für detaillierte Ernährungsplanung besuchen Sie:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Rezepte Tab für AI-generierte Rezepte\n• Food Scan für Kalorien-Analyse\n• Coach für personalisierte Beratung",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}