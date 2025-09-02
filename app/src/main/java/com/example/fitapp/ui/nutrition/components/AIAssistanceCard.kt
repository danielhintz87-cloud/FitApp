package com.example.fitapp.ui.nutrition.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.CookingAssistance
import com.example.fitapp.ai.TemperatureGuide
import com.example.fitapp.ai.Troubleshoot
import com.example.fitapp.ai.IngredientSubstitution

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistanceCard(
    cookingAssistance: CookingAssistance?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expandedSection by remember { mutableStateOf("tips") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "AI Koch-Assistent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                if (isLoading) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            if (cookingAssistance != null) {
                Spacer(Modifier.height(16.dp))
                
                // Section selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "tips" to "Tipps",
                        "temperature" to "Temperatur",
                        "troubleshooting" to "Probleme",
                        "substitutions" to "Ersatz"
                    ).forEach { (key, label) ->
                        FilterChip(
                            onClick = { expandedSection = key },
                            label = { Text(label) },
                            selected = expandedSection == key,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Content based on selected section
                when (expandedSection) {
                    "tips" -> TipsSection(cookingAssistance.tips)
                    "temperature" -> TemperatureSection(cookingAssistance.temperatureGuide)
                    "troubleshooting" -> TroubleshootingSection(cookingAssistance.troubleshooting)
                    "substitutions" -> SubstitutionsSection(cookingAssistance.substitutions)
                }
                
                // Timing advice (always shown if available)
                cookingAssistance.timingAdvice?.let { advice ->
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                advice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "AI-Tipps werden geladen...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TipsSection(tips: List<String>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        items(tips) { tip ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    tip,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TemperatureSection(temperatureGuide: TemperatureGuide?) {
    if (temperatureGuide != null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TemperatureInfoRow("Methode", temperatureGuide.method, Icons.Default.Whatshot)
            TemperatureInfoRow("Temperatur", temperatureGuide.temperature, Icons.Default.Thermostat)
            TemperatureInfoRow("Dauer", temperatureGuide.duration, Icons.Default.Timer)
            TemperatureInfoRow("Gargrad prüfen", temperatureGuide.doneness_check, Icons.Default.Visibility)
        }
    } else {
        Text(
            "Keine Temperaturinformationen verfügbar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TemperatureInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TroubleshootingSection(troubleshooting: List<Troubleshoot>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        items(troubleshooting) { issue ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            issue.problem,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        "Lösung: ${issue.solution}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Text(
                        "Vermeidung: ${issue.prevention}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SubstitutionsSection(substitutions: List<IngredientSubstitution>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        items(substitutions) { substitution ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${substitution.original} → ${substitution.substitute}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        "Verhältnis: ${substitution.ratio}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (substitution.notes.isNotBlank()) {
                        Text(
                            substitution.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}