package com.example.fitapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Enhanced Training Hub Screen
 * Central hub for all training-related features with better organization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTrainingHubScreen(
    navController: NavController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section
        item {
            TrainingHeroCard(
                onStartQuickWorkout = { navController.navigate("todaytraining") },
                onOpenPlanner = { navController.navigate("plan") }
            )
        }
        
        // AI Training Section
        item {
            TrainingSectionCard(
                title = "🤖 KI-gestützte Trainingspläne",
                description = "Personalisierte Workouts durch künstliche Intelligenz",
                actions = listOf(
                    TrainingAction("KI Personal Trainer", Icons.Filled.Psychology) { 
                        navController.navigate("ai_personal_trainer") 
                    },
                    TrainingAction("Adaptive Pläne", Icons.Filled.AutoAwesome) { 
                        navController.navigate("plan") 
                    }
                )
            )
        }
        
        // Workout Types Section
        item {
            TrainingSectionCard(
                title = "🏋️ Workout-Typen",
                description = "Verschiedene Trainingsformen für deine Ziele",
                actions = listOf(
                    TrainingAction("HIIT Training", Icons.Filled.Timer) { 
                        navController.navigate("hiit_builder") 
                    },
                    TrainingAction("Krafttraining", Icons.Filled.FitnessCenter) { 
                        navController.navigate("plan") 
                    },
                    TrainingAction("Cardio", Icons.AutoMirrored.Filled.DirectionsRun) { 
                        navController.navigate("daily_workout/Ausdauer/30") 
                    }
                )
            )
        }
        
        // Progress & Tracking Section
        item {
            TrainingSectionCard(
                title = "📊 Fortschritt & Tracking",
                description = "Verfolge deinen Trainingsfortschritt",
                actions = listOf(
                    TrainingAction("Fortschritts-Analytics", Icons.Filled.Insights) { 
                        navController.navigate("enhanced_analytics") 
                    },
                    TrainingAction("Gewichtsverfolgung", Icons.AutoMirrored.Filled.TrendingUp) { 
                        navController.navigate("weight_tracking") 
                    },
                    TrainingAction("BMI Rechner", Icons.Filled.Calculate) { 
                        navController.navigate("bmi_calculator") 
                    }
                )
            )
        }
        
        // Health & Wellness Section
        item {
            TrainingSectionCard(
                title = "🌱 Gesundheit & Wellness",
                description = "Ganzheitlicher Ansatz für dein Wohlbefinden",
                actions = listOf(
                    TrainingAction("Abnehmprogramm", Icons.Filled.Flag) { 
                        navController.navigate("weight_loss_program") 
                    },
                    TrainingAction("Social Challenges", Icons.Filled.EmojiEvents) { 
                        navController.navigate("social_challenges") 
                    }
                )
            )
        }
        
        // Quick Stats
        item {
            QuickStatsRow(navController = navController)
        }
    }
}

@Composable
private fun TrainingHeroCard(
    onStartQuickWorkout: () -> Unit,
    onOpenPlanner: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯 Training & Fitness Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Dein Weg zu einem stärkeren, gesünderen Ich",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStartQuickWorkout,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Schnell-Training")
                }
                
                OutlinedButton(
                    onClick = onOpenPlanner,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Timeline, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Planer")
                }
            }
        }
    }
}

@Composable
private fun TrainingSectionCard(
    title: String,
    description: String,
    actions: List<TrainingAction>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(actions) { action ->
                    OutlinedButton(
                        onClick = action.onClick,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Icon(
                            action.icon, 
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(action.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Schnellzugriff",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    icon = Icons.Filled.Today,
                    label = "Heute",
                    value = "0/1",
                    onClick = { navController.navigate("today") }
                )
                QuickStatItem(
                    icon = Icons.Filled.LocalFireDepartment,
                    label = "Kalorien",
                    value = "0",
                    onClick = { navController.navigate("enhanced_analytics") }
                )
                QuickStatItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Streak",
                    value = "0",
                    onClick = { navController.navigate("weight_tracking") }
                )
                QuickStatItem(
                    icon = Icons.Filled.Flag,
                    label = "Ziele",
                    value = "3",
                    onClick = { navController.navigate("plan") }
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

data class TrainingAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
