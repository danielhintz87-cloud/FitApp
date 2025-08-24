package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanBuilderScreen() {
    var goal by remember { mutableStateOf("Abnehmen") }
    var days by remember { mutableStateOf(4f) }
    var intensity by remember { mutableStateOf("mittel") }
    val equipmentOptions = listOf("Kurzhantel", "Kettlebell", "Bänder", "Klimmzugstange", "Rudergerät", "Matte")
    val selected = remember { mutableStateListOf<String>() }
    var plan by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Trainingsplan erstellen", style = MaterialTheme.typography.titleLarge) }
        item {
            SegmentedButtons(
                items = listOf("Abnehmen","Muskelaufbau","Ausdauer"),
                selected = goal,
                onSelect = { goal = it }
            )
        }
        item {
            Text("Einheiten/Woche: ${'$'}{days.toInt()}"); Slider(days, { days = it }, steps = 5, valueRange = 1f..7f)
        }
        item {
            SegmentedButtons(
                items = listOf("leicht","mittel","intensiv"),
                selected = intensity,
                onSelect = { intensity = it }
            )
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                equipmentOptions.forEach { e ->
                    FilterChip(
                        selected = selected.contains(e),
                        onClick = { if (e in selected) selected.remove(e) else selected.add(e) },
                        label = { Text(e) }
                    )
                }
            }
        }
        item {
            Button(onClick = {
                scope.launch {
                    plan = AppAi.generate12WeekPlan(goal, days.toInt(), intensity, selected.toList())
                }
            }) { Text("12‑Wochen‑Plan generieren") }
        }
        if (plan.isNotBlank()) {
            item { Text("Vorschau", style = MaterialTheme.typography.titleMedium) }
            item {
                Card { Text(plan, modifier = Modifier.padding(16.dp)) }
            }
        }
    }
}

@Composable
private fun SegmentedButtons(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    SingleChoiceSegmentedButtonRow {
        items.forEachIndexed { idx, label ->
            SegmentedButton(
                selected = selected == label,
                onClick = { onSelect(label) },
                shape = SegmentedButtonDefaults.itemShape(idx, items.size),
                label = { Text(label) }
            )
        }
    }
}
