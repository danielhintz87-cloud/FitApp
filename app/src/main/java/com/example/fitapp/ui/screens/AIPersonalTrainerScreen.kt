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
import com.example.fitapp.R
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ai.getPersonalizedRecommendations
import com.example.fitapp.data.prefs.ApiKeys
import com.example.fitapp.domain.entities.*
import kotlinx.coroutines.launch

data class AIPersonalTrainerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isApiKeyError: Boolean = false,
    val dailySummary: String = "Lade deine personalisierte Zusammenfassung...",
    val recommendations: List<AIRecommendation> = emptyList(),
    val workoutPlan: WorkoutPlan? = null,
    val mealPlan: PersonalizedMealPlan? = null,
    val progressAnalysis: ProgressAnalysis? = null,
    val motivation: MotivationalMessage? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPersonalTrainerScreen(
    onBack: (() -> Unit)? = null,
    onNavigateToApiKeys: (() -> Unit)? = null,
    onNavigateToWorkout: (() -> Unit)? = null,
    onNavigateToNutrition: (() -> Unit)? = null,
    onNavigateToProgress: (() -> Unit)? = null,
    onNavigateToHiitBuilder: (() -> Unit)? = null,
    onNavigateToAnalytics: (() -> Unit)? = null,
    onNavigateToRecipeGeneration: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf(AIPersonalTrainerUiState(isLoading = true)) }

    // Load AI recommendations on screen start
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Create sample user context for demonstration
                val sampleUserContext =
                    UserContext(
                        profile =
                            UserProfile(
                                age = 30,
                                gender = "male",
                                height = 175f,
                                currentWeight = 80f,
                                targetWeight = 75f,
                                activityLevel = "moderately_active",
                                fitnessGoals = listOf("Abnehmen", "Muskeln aufbauen"),
                            ),
                        fitnessLevel =
                            FitnessLevel(
                                strength = "intermediate",
                                cardio = "beginner",
                                flexibility = "beginner",
                                experience = "intermediate",
                            ),
                        recentProgress =
                            listOf(
                                WeightEntry("2024-01-01", 82f),
                                WeightEntry("2024-01-08", 81f),
                                WeightEntry("2024-01-15", 80f),
                            ),
                        currentGoals = listOf("Abnehmen", "Fitness verbessern"),
                        availableEquipment = listOf("Körpergewicht", "Hanteln"),
                    )

                val result = AppAi.getPersonalizedRecommendations(context, sampleUserContext)

                result.onSuccess { response ->
                    uiState =
                        uiState.copy(
                            isLoading = false,
                            dailySummary = response.motivation?.message ?: "Willkommen zu deinem AI Personal Trainer!",
                            recommendations = response.recommendations,
                            workoutPlan = response.workoutPlan,
                            mealPlan = response.mealPlan,
                            progressAnalysis = response.progressAnalysis,
                            motivation = response.motivation,
                        )
                }.onFailure { error ->
                    val isApiKeyError =
                        !ApiKeys.isPrimaryProviderAvailable(context) ||
                            error.message?.contains("API-Schlüssel", ignoreCase = true) == true ||
                            error.message?.contains("Key", ignoreCase = true) == true

                    uiState =
                        uiState.copy(
                            isLoading = false,
                            error = error.message,
                            isApiKeyError = isApiKeyError,
                        )
                }
            } catch (e: Exception) {
                val isApiKeyError =
                    !ApiKeys.isPrimaryProviderAvailable(context) ||
                        e.message?.contains("API-Schlüssel", ignoreCase = true) == true ||
                        e.message?.contains("Key", ignoreCase = true) == true

                uiState =
                    uiState.copy(
                        isLoading = false,
                        error = e.message,
                        isApiKeyError = isApiKeyError,
                    )
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = context.getString(R.string.icon_psychology),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = context.getString(R.string.ai_personal_trainer),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = context.getString(R.string.back),
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        )

        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lade personalisierte Empfehlungen...")
                }
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isApiKeyError) {
                    // Special handling for API key errors
                    ApiKeyErrorDisplay(
                        onNavigateToApiKeys = onNavigateToApiKeys,
                        onRetry = {
                            uiState = uiState.copy(isLoading = true, error = null, isApiKeyError = false)
                        },
                    )
                } else {
                    // Generic error display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fehler beim Laden",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = uiState.error ?: "Unbekannter Fehler",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // Retry loading
                                uiState = uiState.copy(isLoading = true, error = null, isApiKeyError = false)
                            },
                        ) {
                            Text("Erneut versuchen")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp + contentPadding.calculateBottomPadding(),
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Daily AI Summary Card
                item {
                    DailyAISummaryCard(
                        summary = uiState.dailySummary,
                        motivation = uiState.motivation,
                    )
                }

                // Smart Recommendations
                items(uiState.recommendations) { recommendation ->
                    SmartRecommendationCard(
                        recommendation = recommendation,
                        onAccept = { /* Handle accept */ },
                        onDismiss = { /* Handle dismiss */ },
                    )
                }

                // Workout Plan Card
                uiState.workoutPlan?.let { workoutPlan ->
                    item {
                        WorkoutPlanCard(
                            workoutPlan = workoutPlan,
                            onNavigateToWorkout = onNavigateToWorkout,
                            onStartWorkout = {
                                // Support both signature styles
                                onNavigateToWorkout?.invoke()
                            },
                        )
                    }
                }

                // Meal Plan Card
                uiState.mealPlan?.let { mealPlan ->
                    item {
                        MealPlanCard(
                            mealPlan = mealPlan,
                            onNavigateToNutrition = onNavigateToNutrition,
                            onViewFullPlan = {
                                onNavigateToRecipeGeneration?.invoke()
                            },
                        )
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
                        onGenerateWorkout = {
                            onNavigateToWorkout?.invoke()
                            onNavigateToHiitBuilder?.invoke()
                        },
                        onGetNutritionAdvice = { onNavigateToNutrition?.invoke() },
                        onAnalyzeProgress = {
                            onNavigateToProgress?.invoke()
                            onNavigateToAnalytics?.invoke()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun DailyAISummaryCard(
    summary: String,
    motivation: MotivationalMessage?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Tägliche AI-Zusammenfassung",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            motivation?.let {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun SmartRecommendationCard(
    recommendation: AIRecommendation,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(300)),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector =
                        when (recommendation.type) {
                            "workout" -> Icons.Default.FitnessCenter
                            "nutrition" -> Icons.Default.Restaurant
                            else -> Icons.Default.Lightbulb
                        },
                    contentDescription = null,
                    tint =
                        when (recommendation.priority) {
                            "high" -> MaterialTheme.colorScheme.error
                            "medium" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Text(
                    text = "AI Empfehlung • ${recommendation.priority}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
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
fun WorkoutPlanCard(
    workoutPlan: WorkoutPlan,
    onNavigateToWorkout: (() -> Unit)? = null,
    onStartWorkout: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = workoutPlan.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = workoutPlan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "⏱️ ${workoutPlan.estimatedDuration} Min",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "📊 ${workoutPlan.difficulty}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "🏋️ ${workoutPlan.exercises.size} Übungen",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onNavigateToWorkout?.invoke()
                    onStartWorkout()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(LocalContext.current.getString(R.string.start_workout))
            }
        }
    }
}

@Composable
fun MealPlanCard(
    mealPlan: PersonalizedMealPlan,
    onNavigateToNutrition: (() -> Unit)? = null,
    onViewFullPlan: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mealPlan.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "🔥 ${mealPlan.dailyCalories} kcal",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "🥩 ${mealPlan.macroTargets.protein}g Protein",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            mealPlan.meals.take(2).forEach { meal ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onNavigateToNutrition?.invoke()
                    onViewFullPlan()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(LocalContext.current.getString(R.string.view_full_plan))
            }
        }
    }
}

@Composable
fun ProgressAnalysisCard(analysis: ProgressAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fortschritts-Analyse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Trend: ${analysis.weightTrend}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (analysis.insights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Insights:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                analysis.insights.take(2).forEach { insight ->
                    Text(
                        text = "• $insight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    onAnalyzeProgress: () -> Unit,
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = context.getString(R.string.quick_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    IconButton(
                        onClick = onGenerateWorkout,
                        modifier =
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(12.dp),
                                )
                                .size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = context.getString(R.string.icon_fitness_center),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = context.getString(R.string.workout),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    IconButton(
                        onClick = onGetNutritionAdvice,
                        modifier =
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(12.dp),
                                )
                                .size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = context.getString(R.string.icon_restaurant),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = context.getString(R.string.nutrition),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    IconButton(
                        onClick = onAnalyzeProgress,
                        modifier =
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(12.dp),
                                )
                                .size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = context.getString(R.string.icon_analytics),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = context.getString(R.string.analysis),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
fun ApiKeyErrorDisplay(
    onNavigateToApiKeys: (() -> Unit)?,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Key,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "API-Schlüssel erforderlich",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Für die AI Personal Trainer Features wird ein gültiger Gemini API-Schlüssel benötigt.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show current configuration status
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Aktueller Status:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ApiKeys.getConfigurationStatus(context),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Erneut prüfen")
            }

            Button(
                onClick = { onNavigateToApiKeys?.invoke() },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("API-Schlüssel")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Help text
        Text(
            text = "💡 Tipp: Gemini API-Schlüssel erhalten Sie kostenlos bei aistudio.google.com",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
