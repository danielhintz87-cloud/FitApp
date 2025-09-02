package com.example.fitapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.services.CookingModeManager

/**
 * Enhanced Cooking Mode UI Components
 * Following the design specifications from the problem statement
 */

@Composable
fun CookingProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Schritt $currentStep von $totalSteps",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${((currentStep.toFloat() / totalSteps) * 100).toInt()}% abgeschlossen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StepInstructionCard(
    cookingStep: CookingModeManager.CookingStep,
    modifier: Modifier = Modifier,
    onShowImage: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step Number and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${cookingStep.stepNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                // Image button if available
                if (cookingStep.image != null) {
                    IconButton(onClick = onShowImage) {
                        Icon(
                            Icons.Filled.Image,
                            contentDescription = "Bild anzeigen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Instruction Text
            Text(
                text = cookingStep.instruction,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
            )
            
            // Step-specific Information Row
            if (cookingStep.temperature != null || cookingStep.estimatedTime != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Temperature Indicator
                    cookingStep.temperature?.let { temp ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Thermostat,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    temp,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    
                    // Estimated Time
                    cookingStep.estimatedTime?.let { time ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${time / 60} Min",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Step Ingredients Chips
            if (cookingStep.ingredients.isNotEmpty()) {
                Column {
                    Text(
                        "Zutaten für diesen Schritt:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    // Ingredients flow layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cookingStep.ingredients.take(3).forEach { ingredient ->
                            StepIngredientsChip(ingredient = ingredient)
                        }
                        
                        if (cookingStep.ingredients.size > 3) {
                            AssistChip(
                                onClick = { /* Show all ingredients */ },
                                label = { Text("+${cookingStep.ingredients.size - 3} weitere") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIngredientsChip(
    ingredient: CookingModeManager.Ingredient,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { /* Could show ingredient details */ },
        label = {
            Text(
                "${ingredient.quantity} ${ingredient.unit} ${ingredient.name}",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = if (ingredient.isOptional) {
            {
                Icon(
                    Icons.Filled.HelpOutline,
                    contentDescription = "Optional",
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (ingredient.isOptional) {
                MaterialTheme.colorScheme.outlineVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    )
}

@Composable
fun StepTimerCard(
    timer: CookingModeManager.StepTimer?,
    stepDuration: Int?,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Timer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            if (timer != null && timer.isActive) {
                // Active Timer Display
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 
                            if (timer.totalDuration > 0) {
                                (timer.totalDuration - timer.remainingTime).toFloat() / timer.totalDuration.toFloat()
                            } else 0f
                        },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            formatTime(timer.remainingTime),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "verbleibend",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                // Timer Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onPauseTimer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (timer.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (timer.isPaused) "Fortsetzen" else "Pausieren")
                    }
                    
                    OutlinedButton(
                        onClick = onResetTimer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
                
            } else if (timer?.isCompleted == true) {
                // Completed Timer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Timer abgelaufen! 🔔",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                
            } else if (stepDuration != null) {
                // Timer Available but Not Started
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        formatTime(stepDuration),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onStartTimer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Timer starten")
                    }
                }
            }
        }
    }
}

@Composable
fun CookingTipsCard(
    tips: List<String>,
    modifier: Modifier = Modifier
) {
    if (tips.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Koch-Tipps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                tips.forEach { tip ->
                    TipChip(tip = tip)
                }
            }
        }
    }
}

@Composable
private fun TipChip(
    tip: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            tip,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun CookingNavigationBar(
    canGoBack: Boolean,
    canGoNext: Boolean,
    isLastStep: Boolean,
    onPrevious: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Previous Step Button
        OutlinedButton(
            onClick = onPrevious,
            enabled = canGoBack,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Zurück")
        }
        
        // Pause Button
        OutlinedButton(
            onClick = onPause,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Filled.Pause, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Pause")
        }
        
        // Next/Finish Button
        Button(
            onClick = if (isLastStep) onFinish else onNext,
            enabled = canGoNext,
            modifier = Modifier.weight(1f)
        ) {
            if (isLastStep) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Fertig")
            } else {
                Text("Weiter")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun RecipeHeaderCard(
    recipeTitle: String,
    servings: Int,
    difficulty: String?,
    estimatedTime: Int?,
    onServingsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                recipeTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Servings Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Portionen:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = { onServingsChange((servings - 1).coerceAtLeast(1)) }
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Weniger")
                    }
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            "$servings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onServingsChange(servings + 1) }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Mehr")
                    }
                }
                
                Spacer(Modifier.weight(1f))
                
                // Difficulty and Time Info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    difficulty?.let {
                        Text(
                            "Schwierigkeit: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    estimatedTime?.let {
                        Text(
                            "Gesamt: ${it / 60} Min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

// Helper function to format time
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) {
        String.format("%d:%02d", minutes, secs)
    } else {
        String.format("0:%02d", secs)
    }
}