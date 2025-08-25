package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.launch

@Composable
fun PlanScreen() {
    // This is essentially the same as PlanBuilderScreen
    PlanBuilderScreen()
}

@Composable
fun NutritionScreen() {
    var query by remember { mutableStateOf("Gesunde Mahlzeiten, ausgewogen, 400-600 kcal") }
    var result by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { 
            Text("Ernährung & Rezepte", style = MaterialTheme.typography.titleLarge) 
        }
        
        item {
            Text(
                "Lassen Sie AI personalisierte Rezepte für Ihre Ernährungsziele erstellen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        item {
            OutlinedTextField(
                value = query, 
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Präferenzen, Diät, Kalorienziel...") }
            )
        }
        
        item {
            Button(onClick = {
                scope.launch { 
                    result = AppAi.generateRecipes(query, 10) 
                }
            }) { 
                Text("🍽️ 10 Rezeptvorschläge generieren") 
            }
        }
        
        if (result.isNotBlank()) {
            item { 
                Card { 
                    Text(
                        result, 
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                } 
            }
        }
    }
}