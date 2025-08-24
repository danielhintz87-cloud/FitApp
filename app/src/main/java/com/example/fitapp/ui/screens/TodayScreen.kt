package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.launch

data class DayWorkout(
    val title: String,
    val durationMin: Int,
    val items: List<String>
)

@Composable
fun TodayScreen() {
    // Dummy „heute“ – normalerweise aus gespeichertem Plan lesen
    var day by remember {
        mutableStateOf(
            DayWorkout("Full Body Fatburn #1", 45, listOf("Kurzhantel Squats – 3×12","Liegestütze – 3×10","Plank – 60s"))
        )
    }
    var alt by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Heute", style = MaterialTheme.typography.titleLarge) }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(day.title, style = MaterialTheme.typography.titleMedium)
                    Text("Dauer: ${'$'}{day.durationMin} min")
                    day.items.forEach { Text("• $it") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            val markdown = """
                                ${'$'}{day.title}
                                Dauer: ${'$'}{day.durationMin}
                                ${'$'}{day.items.joinToString("\n")}
                            """.trimIndent()
                            alt = AppAi.suggestAlternativeAndLog(
                                todaysPlanMarkdown = markdown,
                                constraints = "ähnliche Muskelgruppen; Dauer ±10 Min"
                            )
                        }
                    }) { Text("Alternative vorschlagen & loggen") }
                }
            }
        }
        if (alt.isNotBlank()) {
            item { Text("Alternative:", style = MaterialTheme.typography.titleMedium) }
            item { Card { Text(alt, Modifier.padding(16.dp)) } }
        }
    }
}
