package com.example.fitapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.getPersonalizedRecommendations
import com.example.fitapp.domain.entities.*
import kotlinx.coroutines.launch

data class AIPersonalTrainerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dailySummary: String = "Lade deine personalisierte Zusammenfassung...",
    val recommendations: List<AIRecommendation> = emptyList(),
    val workoutPlan: WorkoutPlan? = null,
    val mealPlan: PersonalizedMealPlan? = null,
    val progressAnalysis: ProgressAnalysis? = null,
    val motivation: MotivationalMessage? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPersonalTrainerScreen(
    onBack: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf(AIPersonalTrainerUiState(isLoading = true)) }
    
    // Load AI recommendations on screen start
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Create sample user context for demonstration
                val sampleUserContext = UserContext(
                    profile = UserProfile(
                        age = 30,
                        gender = "male",
                        height = 175f,
                        currentWeight = 80f,
                        targetWeight = 75f,
                        activityLevel = "moderately_active",
                        fitnessGoals = listOf("Abnehmen", "Muskeln aufbauen")
                    ),
                    fitnessLevel = FitnessLevel(
                        strength = "intermediate",
                        cardio = "beginner",
                        flexibility = "beginner",
                        experience = "intermediate"
                    ),
                    recentProgress = listOf(
                        WeightEntry("2024-01-01", 82f),
                        WeightEntry("2024-01-08", 81f),
                        WeightEntry("2024-01-15", 80f)
                    ),
                    currentGoals = listOf("Abnehmen", "Fitness verbessern"),
                    availableEquipment = listOf("Körpergewicht", "Hanteln")
                )
                
                val result = AppAi.getPersonalizedRecommendations(context, sampleUserContext)
                
                result.onSuccess { response ->
                    uiState = uiState.copy(
                        isLoading = false,
                        dailySummary = response.motivation?.message ?: "Willkommen zu deinem AI Personal Trainer!",
                        recommendations = response.recommendations,
                        workoutPlan = response.workoutPlan,
                        mealPlan = response.mealPlan,
                        progressAnalysis = response.progressAnalysis,
                        motivation = response.motivation
                    )
                }.onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Personal Trainer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lade personalisierte Empfehlungen...")
                }
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Fehler beim Laden",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = uiState.error ?: "Unbekannter Fehler",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Retry loading
                            uiState = uiState.copy(isLoading = true, error = null)
                        }
                    ) {
                        Text("Erneut versuchen")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + contentPadding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Daily AI Summary Card
                item {
                    DailyAISummaryCard(
                        summary = uiState.dailySummary,
                        motivation = uiState.motivation
                    )
                }
                
                // Smart Recommendations
                items(uiState.recommendations) { recommendation ->
                    SmartRecommendationCard(
                        recommendation = recommendation,
                        onAccept = { /* Handle accept */ },
                        onDismiss = { /* Handle dismiss */ }
                    )
                }
                
                // Workout Plan Card
                uiState.workoutPlan?.let { workoutPlan ->
                    item {
                        WorkoutPlanCard(workoutPlan = workoutPlan)
                    }
                }
                
                // Meal Plan Card
                uiState.mealPlan?.let { mealPlan ->
                    item {
                        MealPlanCard(mealPlan = mealPlan)
                    }
                }
                
                // Progress Analysis Card
                uiState.progressAnalysis?.let { analysis ->
                    item {
                        ProgressAnalysisCard(analysis = analysis)
                    }
                }
                
                // Quick Actions
                item {
                    QuickActionsCard(
                        onGenerateWorkout = { /* Handle generate workout */ },
                        onGetNutritionAdvice = { /* Handle nutrition advice */ },
                        onAnalyzeProgress = { /* Handle analyze progress */ }
                    )
                }
            }
        }
    }
}

@Composable
fun DailyAISummaryCard(
    summary: String,
    motivation: MotivationalMessage?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tägliche AI-Zusammenfassung",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            motivation?.let {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SmartRecommendationCard(
    recommendation: AIRecommendation,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (recommendation.type) {
                        "workout" -> Icons.Default.FitnessCenter
                        "nutrition" -> Icons.Default.Restaurant
                        else -> Icons.Default.Lightbulb
                    },
                    contentDescription = null,
                    tint = when (recommendation.priority) {
                        "high" -> MaterialTheme.colorScheme.error
                        "medium" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = "AI Empfehlung • ${recommendation.priority}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Später")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onAccept) {
                    Text("Ausprobieren")
                }
            }
        }
    }
}

@Composable
fun WorkoutPlanCard(workoutPlan: WorkoutPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = workoutPlan.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = workoutPlan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱️ ${workoutPlan.estimatedDuration} Min",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "📊 ${workoutPlan.difficulty}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "🏋️ ${workoutPlan.exercises.size} Übungen",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { /* Navigate to workout details */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Workout starten")
            }
        }
    }
}

@Composable
fun MealPlanCard(mealPlan: PersonalizedMealPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mealPlan.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🔥 ${mealPlan.dailyCalories} kcal",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "🥩 ${mealPlan.macroTargets.protein}g Protein",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            mealPlan.meals.take(2).forEach { meal ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { /* Navigate to meal plan details */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vollständigen Plan anzeigen")
            }
        }
    }
}

@Composable
fun ProgressAnalysisCard(analysis: ProgressAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fortschritts-Analyse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Trend: ${analysis.weightTrend}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { analysis.adherenceScore },
                modifier = Modifier.fillMaxWidth(),
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Einhaltung: ${(analysis.adherenceScore * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (analysis.insights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Insights:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                analysis.insights.take(2).forEach { insight ->
                    Text(
                        text = "• $insight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onGenerateWorkout: () -> Unit,
    onGetNutritionAdvice: () -> Unit,
    onAnalyzeProgress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Schnellaktionen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onGenerateWorkout,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Workout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Workout",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onGetNutritionAdvice,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Ernährung",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Ernährung",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onAnalyzeProgress,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analyse",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Analyse",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}