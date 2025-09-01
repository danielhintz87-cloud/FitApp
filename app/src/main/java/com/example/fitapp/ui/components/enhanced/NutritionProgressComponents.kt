package com.example.fitapp.ui.components.enhanced

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/**
 * Enhanced circular progress indicators for nutrition tracking
 * 
 * Features YAZIO-style animated progress rings with customizable colors,
 * gradient effects, and smooth animations for calorie and macro tracking.
 */

@Composable
fun NutritionProgressRing(
    progress: Float,
    goal: Int,
    current: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 12f,
    animationDuration: Int = 1000
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "progress_animation"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawNutritionRing(
                progress = animatedProgress,
                color = color,
                strokeWidth = strokeWidth.toDp().toPx()
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = current.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = "/ $goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MacroProgressBar(
    progress: Float,
    current: Float,
    goal: Float,
    label: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    height: Float = 8f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "macro_progress"
    )
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${current.toInt()}${unit} / ${goal.toInt()}${unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(height.dp / 2))
        ) {
            val barHeight = height.dp.toPx()
            
            // Background
            drawRoundRect(
                color = Color(0xFFE0E0E0), // Use a static color instead of MaterialTheme
                size = Size(size.width, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2)
            )
            
            // Progress
            if (animatedProgress > 0f) {
                drawRoundRect(
                    color = color,
                    size = Size(size.width * animatedProgress, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2)
                )
            }
        }
    }
}

@Composable
fun CalorieProgressCard(
    caloriesConsumed: Int,
    calorieGoal: Int,
    caloriesBurned: Int = 0,
    modifier: Modifier = Modifier
) {
    val progress = if (calorieGoal > 0) caloriesConsumed.toFloat() / calorieGoal.toFloat() else 0f
    val netCalories = caloriesConsumed - caloriesBurned
    val remainingCalories = maxOf(0, calorieGoal - netCalories)
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large circular progress
            NutritionProgressRing(
                progress = progress,
                goal = calorieGoal,
                current = caloriesConsumed,
                label = "Kalorien",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(120.dp),
                strokeWidth = 16f
            )
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Calorie breakdown
            Column(modifier = Modifier.weight(1f)) {
                CalorieInfoRow(
                    label = "Verbraucht",
                    value = caloriesConsumed,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (caloriesBurned > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CalorieInfoRow(
                        label = "Verbrannt",
                        value = caloriesBurned,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                CalorieInfoRow(
                    label = "Verbleibend",
                    value = remainingCalories,
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalorieInfoRow(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "${value} kcal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun MacroNutrientsCard(
    carbs: Float,
    carbsGoal: Float,
    protein: Float,
    proteinGoal: Float,
    fat: Float,
    fatGoal: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Makronährstoffe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            MacroProgressBar(
                progress = if (carbsGoal > 0) carbs / carbsGoal else 0f,
                current = carbs,
                goal = carbsGoal,
                label = "Kohlenhydrate",
                unit = "g",
                color = Color(0xFF4CAF50),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MacroProgressBar(
                progress = if (proteinGoal > 0) protein / proteinGoal else 0f,
                current = protein,
                goal = proteinGoal,
                label = "Protein",
                unit = "g",
                color = Color(0xFF2196F3),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MacroProgressBar(
                progress = if (fatGoal > 0) fat / fatGoal else 0f,
                current = fat,
                goal = fatGoal,
                label = "Fett",
                unit = "g",
                color = Color(0xFFFF9800),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun WaterIntakeCard(
    currentIntake: Int,
    dailyGoal: Int,
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (dailyGoal > 0) currentIntake.toFloat() / dailyGoal.toFloat() else 0f
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wasseraufnahme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "${currentIntake}ml / ${dailyGoal}ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick add buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WaterAddButton(amount = 200, onAdd = onAddWater)
                WaterAddButton(amount = 300, onAdd = onAddWater)
                WaterAddButton(amount = 500, onAdd = onAddWater)
            }
        }
    }
}

@Composable
private fun WaterAddButton(
    amount: Int,
    onAdd: (Int) -> Unit
) {
    OutlinedButton(
        onClick = { onAdd(amount) },
        modifier = Modifier.width(80.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Text(
            text = "${amount}ml",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun DrawScope.drawNutritionRing(
    progress: Float,
    color: Color,
    strokeWidth: Float
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = (size.minDimension - strokeWidth) / 2
    
    // Background ring
    drawCircle(
        color = color.copy(alpha = 0.1f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Progress ring with gradient
    if (progress > 0f) {
        val sweepAngle = progress * 360f
        val startAngle = -90f
        
        // Create gradient brush
        val gradientBrush = Brush.sweepGradient(
            colors = listOf(
                color.copy(alpha = 0.3f),
                color,
                color.copy(alpha = 0.8f)
            ),
            center = center
        )
        
        drawArc(
            brush = gradientBrush,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(center.x - radius, center.y - radius)
        )
        
        // Add glow effect
        drawArc(
            color = color.copy(alpha = 0.3f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth + 4f, cap = StrokeCap.Round),
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(center.x - radius, center.y - radius)
        )
    }
}