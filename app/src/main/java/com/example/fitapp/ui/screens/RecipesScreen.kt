package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.RecipeRequest
import kotlinx.coroutines.launch

@Composable
fun NutritionScreen(contentPadding: PaddingValues) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var preferences by remember { mutableStateOf("Vegetarisch, 500-700 kcal, 20-30 Min") }
    var diet by remember { mutableStateOf("Ausgewogen") }
    var count by remember { mutableStateOf("10") }
    var result by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var provider by remember { mutableStateOf(AiProvider.OPENAI) }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Rezepte", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = provider == AiProvider.OPENAI, onClick = { provider = AiProvider.OPENAI }, label = { Text("OpenAI") })
            FilterChip(selected = provider == AiProvider.GEMINI, onClick = { provider = AiProvider.GEMINI }, label = { Text("Gemini") })
            FilterChip(selected = provider == AiProvider.DEEPSEEK, onClick = { provider = AiProvider.DEEPSEEK }, label = { Text("DeepSeek") })
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = preferences,
            onValueChange = { preferences = it },
            label = { Text("Präferenzen") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = diet,
            onValueChange = { diet = it },
            label = { Text("Diät-Typ") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = count,
            onValueChange = { count = it },
            label = { Text("Anzahl Rezepte") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        Button(
            enabled = !busy,
            onClick = {
                scope.launch {
                    busy = true
                    result = try {
                        val req = RecipeRequest(
                            preferences = preferences,
                            diet = diet,
                            count = count.toIntOrNull() ?: 10
                        )
                        AppAi.recipes(ctx, provider, req).getOrThrow()
                    } catch (e: Exception) {
                        "Fehler: ${e.message}"
                    } finally {
                        busy = false
                    }
                }
            }
        ) {
            Text(if (busy) "Generiere..." else "Rezepte erstellen")
        }
        
        if (result.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Card {
                Text(result, Modifier.padding(16.dp))
            }
        }
    }
}
