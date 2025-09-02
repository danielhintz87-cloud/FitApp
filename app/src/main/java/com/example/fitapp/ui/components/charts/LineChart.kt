package com.example.fitapp.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun LineChart(
    data: List<Float>,
    labels: List<String>,
    title: String,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (data.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val padding = 40f
                    
                    val dataMax = data.maxOrNull() ?: 1f
                    val dataMin = data.minOrNull() ?: 0f
                    val dataRange = max(dataMax - dataMin, 1f)
                    
                    // Draw grid lines
                    val gridColor = lineColor.copy(alpha = 0.2f)
                    for (i in 0..4) {
                        val y = padding + (canvasHeight - 2 * padding) * i / 4
                        drawLine(
                            color = gridColor,
                            start = Offset(padding, y),
                            end = Offset(canvasWidth - padding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // Draw data line
                    if (data.size > 1) {
                        val path = Path()
                        data.forEachIndexed { index, value ->
                            val x = padding + (canvasWidth - 2 * padding) * index / (data.size - 1)
                            val y = canvasHeight - padding - (canvasHeight - 2 * padding) * (value - dataMin) / dataRange
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        
                        // Draw data points
                        data.forEachIndexed { index, value ->
                            val x = padding + (canvasWidth - 2 * padding) * index / (data.size - 1)
                            val y = canvasHeight - padding - (canvasHeight - 2 * padding) * (value - dataMin) / dataRange
                            
                            drawCircle(
                                color = lineColor,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        "Keine Daten verfügbar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}