package com.example.fitapp.ui.fasting

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * FastingScreen - YAZIO-style intermittent fasting interface
 *
 * Features:
 * - Beautiful countdown timer with circular progress
 * - Protocol selector with popular fasting methods
 * - Real-time timer updates
 * - Fasting statistics and streaks
 * - Material Design 3 styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastingScreen(
    modifier: Modifier = Modifier,
    fastingManager: FastingManager = FastingManager(LocalContext.current),
) {
    val fastingState by fastingManager.fastingState.collectAsState()
    val fastingStats by fastingManager.fastingStats.collectAsState()

    // Update timer every second when fasting
    LaunchedEffect(fastingState.isFasting) {
        if (fastingState.isFasting) {
            while (true) {
                delay(1000)
                fastingManager.updateFastingState()
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Text(
            text = "Intervallfasten",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        // Main timer and status
        FastingTimerCard(
            fastingState = fastingState,
            onStartFasting = { protocol -> fastingManager.startFasting(protocol) },
            onEndFasting = { fastingManager.endFasting() },
            onStartEating = { fastingManager.startEatingWindow() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Protocol selector
        if (!fastingState.isFasting) {
            ProtocolSelector(
                selectedProtocol = fastingState.protocol,
                onProtocolSelected = { protocol ->
                    fastingManager.startFasting(protocol)
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Statistics
        FastingStatsCard(
            stats = fastingStats,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FastingTimerCard(
    fastingState: FastingManager.FastingState,
    onStartFasting: (FastingManager.FastingProtocol) -> Unit,
    onEndFasting: () -> Unit,
    onStartEating: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Protocol display
            Text(
                text = fastingState.protocol.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = fastingState.protocol.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Circular timer
            if (fastingState.isFasting) {
                CircularTimer(
                    progress = fastingState.progressPercentage,
                    timeRemaining = fastingState.timeRemaining,
                    phase = fastingState.currentPhase,
                    modifier = Modifier.size(200.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Phase indicator
                PhaseIndicator(
                    phase = fastingState.currentPhase,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    if (fastingState.currentPhase == FastingManager.FastingPhase.FASTING) {
                        Button(
                            onClick = onStartEating,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                ),
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Essen starten")
                        }
                    }

                    OutlinedButton(
                        onClick = onEndFasting,
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beenden")
                    }
                }
            } else {
                // Not fasting - show start option
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text =
                        when (fastingState.currentPhase) {
                            FastingManager.FastingPhase.COMPLETED -> "Fasten abgeschlossen! 🎉"
                            else -> "Bereit zum Fasten"
                        },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start fasting button
                Button(
                    onClick = { onStartFasting(fastingState.protocol) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fasten starten")
                }
            }
        }
    }
}

@Composable
private fun CircularTimer(
    progress: Float,
    timeRemaining: Long,
    phase: FastingManager.FastingPhase,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress_animation",
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Background circle
            drawCircle(
                color = surfaceVariant,
                radius = radius,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Progress arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formatTime(timeRemaining),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text =
                    when (phase) {
                        FastingManager.FastingPhase.FASTING -> "Fastenzeit"
                        FastingManager.FastingPhase.EATING_WINDOW -> "Essenszeit"
                        else -> ""
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PhaseIndicator(
    phase: FastingManager.FastingPhase,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when (phase) {
                        FastingManager.FastingPhase.FASTING -> MaterialTheme.colorScheme.primaryContainer
                        FastingManager.FastingPhase.EATING_WINDOW -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector =
                    when (phase) {
                        FastingManager.FastingPhase.FASTING -> Icons.Default.Schedule
                        FastingManager.FastingPhase.EATING_WINDOW -> Icons.Default.Restaurant
                        else -> Icons.Default.CheckCircle
                    },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = phase.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ProtocolSelector(
    selectedProtocol: FastingManager.FastingProtocol,
    onProtocolSelected: (FastingManager.FastingProtocol) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Fastenprotokoll wählen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(FastingManager.FastingProtocol.values()) { protocol ->
                    ProtocolCard(
                        protocol = protocol,
                        isSelected = protocol == selectedProtocol,
                        onSelected = { onProtocolSelected(protocol) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolCard(
    protocol: FastingManager.FastingProtocol,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
        onClick = onSelected,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 4.dp else 1.dp,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = protocol.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = protocol.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Ausgewählt",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun FastingStatsCard(
    stats: FastingManager.FastingStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Statistiken",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    title = "Aktuelle Serie",
                    value = "${stats.currentStreak}",
                    icon = Icons.Default.LocalFireDepartment,
                )

                StatItem(
                    title = "Längste Serie",
                    value = "${stats.longestStreak}",
                    icon = Icons.Default.Star,
                )
            }

            if (stats.lastFastDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Letztes Fasten: ${stats.lastFastDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        else -> String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
