package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.data.ai.Ai
import com.example.fitapp.logic.PlanGenerator
import com.example.fitapp.ui.design.Spacing
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val plan by AppRepository.plan.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg)
            .padding(bottom = 96.dp, top = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text("Heute", style = MaterialTheme.typography.titleLarge)
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.md)) {
                if (plan == null) {
                    Text("Noch kein Plan vorhanden.")
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "Wechsle zum Tab „Training“, um einen Grundplan zu erstellen.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val day = plan!!.week.firstOrNull()
                    if (day == null) {
                        Text("Plan leer.")
                    } else {
                        Text(day.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(Spacing.xs))
                        Text("Dauer: ${'$'}{day.durationMin} min")
                        Spacer(Modifier.height(Spacing.sm))
                        day.exercises.forEach {
                            val repsDisplay = it.reps?.toString() ?: "-"
                            val repsInfo = if (it.sets != null) " – ${it.sets}×$repsDisplay" else ""
                            Text("• ${it.name}$repsInfo")
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        Button(onClick = {
                            scope.launch {
                                val alternative = runCatching {
                                    Ai.repo.suggestAlternative(
                                        plan!!.goal,
                                        plan!!.devices.firstOrNull()?.name ?: "Körpergewicht",
                                        plan!!.timeBudgetMin
                                    )
                                }.getOrElse {
                                    PlanGenerator.alternativeForToday(
                                        goal = plan!!.goal,
                                        deviceHint = plan!!.devices.firstOrNull()?.name ?: "Körpergewicht",
                                        timeMin = plan!!.timeBudgetMin
                                    )
                                }
                                AppRepository.logExercise(alternative.title, alternative.durationMin, alternative.durationMin * 6)
                            }
                        }) {
                            Text("Alternative vorschlagen & loggen")
                        }
                    }
                }
            }
        }
    }
}
