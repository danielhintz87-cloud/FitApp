package com.example.fitapp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PieChart(
    data: List<Pair<String, Float>>,
    colors: List<Color>,
    title: String,
    modifier: Modifier = Modifier,
) {
    val defaultColor = MaterialTheme.colorScheme.primary

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Pie chart
                    Canvas(
                        modifier = Modifier.size(120.dp),
                    ) {
                        val total = data.sumOf { it.second.toDouble() }.toFloat()
                        if (total > 0) {
                            var startAngle = 0f
                            val radius = size.minDimension / 2 * 0.8f
                            val center = Offset(size.width / 2, size.height / 2)

                            data.forEachIndexed { index, (_, value) ->
                                val sweepAngle = (value / total) * 360f
                                val color = colors.getOrElse(index) { defaultColor }

                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2),
                                )

                                startAngle += sweepAngle
                            }
                        }
                    }

                    // Legend
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        data.forEachIndexed { index, (label, value) ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(12.dp)
                                            .background(
                                                colors.getOrElse(index) { defaultColor },
                                                androidx.compose.foundation.shape.CircleShape,
                                            ),
                                )
                                Column {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = "${value.toInt()}g",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    "Keine Daten verfügbar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
