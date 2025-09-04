package com.example.fitapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitapp.ai.FormFeedback
import com.example.fitapp.ai.MovementPatternAnalysis
import com.example.fitapp.ai.CoachingFeedback
import com.example.fitapp.ui.theme.FitAppTheme

/**
 * Real-time form feedback card that displays ML-powered insights
 * during workout execution for improved form and injury prevention
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeFormFeedbackCard(
    formFeedback: FormFeedback?,
    movementAnalysis: MovementPatternAnalysis?,
    coachingFeedback: CoachingFeedback?,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with AI indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = "AI Coach",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KI-Formanalyse",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Form quality indicator
                    FormQualityIndicator(
                        quality = formFeedback?.formScore ?: coachingFeedback?.formScore ?: 0.7f
                    )
                }
                
                // Safety warnings (highest priority)
                formFeedback?.safetyWarnings?.takeIf { it.isNotEmpty() }?.let { warnings ->
                    SafetyWarningSection(warnings = warnings)
                }
                
                coachingFeedback?.safetyWarnings?.takeIf { it.isNotEmpty() }?.let { warnings ->
                    SafetyWarningSection(warnings = warnings)
                }
                
                // Immediate corrections
                val corrections = (formFeedback?.immediateCorrections ?: emptyList()) + 
                                (coachingFeedback?.immediateCorrections ?: emptyList())
                
                if (corrections.isNotEmpty()) {
                    ImmediateCorrectionsSection(corrections = corrections.distinct())
                }
                
                // Movement analysis insights
                movementAnalysis?.let { analysis ->
                    MovementAnalysisSection(analysis = analysis)
                }
                
                // Motivational feedback
                val motivationalMessages = (formFeedback?.motivationalMessages ?: emptyList()) + 
                                         (coachingFeedback?.motivationalMessages ?: emptyList())
                
                if (motivationalMessages.isNotEmpty()) {
                    MotivationalSection(messages = motivationalMessages.distinct())
                }
                
                // Recommended adjustments
                coachingFeedback?.recommendedAdjustments?.takeIf { it.isNotEmpty() }?.let { adjustments ->
                    RecommendedAdjustmentsSection(adjustments = adjustments)
                }
            }
        }
    }
}

@Composable
private fun FormQualityIndicator(
    quality: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        quality >= 0.8f -> Color(0xFF4CAF50) // Green
        quality >= 0.6f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
    
    val icon = when {
        quality >= 0.8f -> Icons.Filled.CheckCircle
        quality >= 0.6f -> Icons.Filled.Warning
        else -> Icons.Filled.Error
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Form Quality",
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${(quality * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SafetyWarningSection(
    warnings: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ACHTUNG",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            }
            
            warnings.forEach { warning ->
                Text(
                    text = "• $warning",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
private fun ImmediateCorrectionsSection(
    corrections: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Adjust,
                contentDescription = "Corrections",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sofortige Korrekturen",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        corrections.forEach { correction ->
            Text(
                text = "→ $correction",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MovementAnalysisSection(
    analysis: MovementPatternAnalysis,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = "Movement Analysis",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bewegungsanalyse",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Risk indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RiskIndicator(
                    label = "Asymmetrie",
                    value = analysis.asymmetryScore,
                    modifier = Modifier.weight(1f)
                )
                RiskIndicator(
                    label = "Kompensation",
                    value = analysis.compensationScore,
                    modifier = Modifier.weight(1f)
                )
                RiskIndicator(
                    label = "Ermüdung",
                    value = analysis.fatigueScore,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Recommendations
            if (analysis.recommendations.isNotEmpty()) {
                analysis.recommendations.take(2).forEach { recommendation ->
                    Text(
                        text = "💡 $recommendation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskIndicator(
    label: String,
    value: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        value < 0.3f -> Color(0xFF4CAF50) // Green
        value < 0.6f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MotivationalSection(
    messages: List<String>,
    modifier: Modifier = Modifier
) {
    if (messages.isNotEmpty()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEmotions,
                contentDescription = "Motivation",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = messages.first(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun RecommendedAdjustmentsSection(
    adjustments: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = "Adjustments",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Empfohlene Anpassungen",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        
        adjustments.forEach { adjustment ->
            Text(
                text = "⚙️ $adjustment",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun RealTimeFormFeedbackCardPreview() {
    FitAppTheme {
        val sampleFormFeedback = FormFeedback(
            immediateCorrections = listOf("Rumpf anspannen", "Bewegung verlangsamen"),
            motivationalMessages = listOf("Sehr gut! Weiter so! 💪"),
            formScore = 0.75f,
            safetyWarnings = emptyList(),
            timestamp = System.currentTimeMillis()
        )
        
        val sampleMovementAnalysis = MovementPatternAnalysis(
            patterns = emptyList(),
            asymmetryScore = 0.2f,
            compensationScore = 0.1f,
            fatigueScore = 0.4f,
            riskLevel = 0.3f,
            recommendations = listOf("Einseitige Übungen einbauen", "Core-Stabilität verbessern"),
            confidence = 0.85f
        )
        
        val sampleCoachingFeedback = CoachingFeedback(
            immediateCorrections = listOf("Knie über den Füßen halten"),
            motivationalMessages = listOf("Du schaffst das!"),
            safetyWarnings = emptyList(),
            formScore = 0.8f,
            repQuality = 0.75f,
            recommendedAdjustments = listOf("Gewicht um 5% reduzieren"),
            timestamp = System.currentTimeMillis()
        )
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RealTimeFormFeedbackCard(
                formFeedback = sampleFormFeedback,
                movementAnalysis = sampleMovementAnalysis,
                coachingFeedback = sampleCoachingFeedback
            )
        }
    }
}