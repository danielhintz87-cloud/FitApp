package com.example.fitapp.ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CoachScreen() {
    var provider by remember { mutableStateOf(AiProvider.OpenAI) }
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Coach", style = MaterialTheme.typography.titleLarge)
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, null) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("OpenAI") },
                            onClick = { provider = AiProvider.OpenAI; menu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Gemini") },
                            onClick = { provider = AiProvider.Gemini; menu = false }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("DeepSeek") },
                            onClick = { provider = AiProvider.DeepSeek; menu = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                SuggestionChip("Tagesplan (Abnehmen)") {
                    scope.launch { output = AppAi.generate12WeekPlan("Abnehmen", 4, "mittel", emptyList(), provider) }
                }
                SuggestionChip("20‑Min HIIT") {
                    scope.launch { output = AppAi.suggestAlternativeAndLog("HIIT heute 20 Min", "max 20 Min; keine Geräte", provider) }
                }
                SuggestionChip("Low‑Carb Rezepte") {
                    scope.launch { output = AppAi.generateRecipes("Low‑Carb, 500–700 kcal, 20–35 Min", 10, provider) }
                }
                SuggestionChip("Kardio‑Woche") {
                    scope.launch { output = AppAi.generate12WeekPlan("Ausdauer", 3, "leicht", listOf("Rudergerät"), provider) }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Frag den Coach …") },
                trailingIcon = {
                    Button(onClick = {
                        scope.launch {
                            output = AppAi.openAiFallback(input, provider)
                        }
                    }) { Text("Senden") }
                }
            )
            Spacer(Modifier.height(12.dp))
        }

        if (output.isNotBlank()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        text = output,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// kleine Hilfsfunktion: nutzt ausgewählten Provider
private suspend fun AppAi.openAiFallback(prompt: String, provider: AiProvider): String =
    when (provider) {
        AiProvider.OpenAI -> callInternal(prompt, AiProvider.OpenAI)
        AiProvider.Gemini -> callInternal(prompt, AiProvider.Gemini)
        AiProvider.DeepSeek -> callInternal(prompt, AiProvider.DeepSeek)
    }

private suspend fun AppAi.callInternal(prompt: String, p: AiProvider) =
    when (p) {
        AiProvider.OpenAI -> generate12WeekPlan(prompt, 4, "mittel", emptyList(), p)
        AiProvider.Gemini -> generate12WeekPlan(prompt, 4, "mittel", emptyList(), p)
        AiProvider.DeepSeek -> generate12WeekPlan(prompt, 4, "mittel", emptyList(), p)
    }

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) })
}
