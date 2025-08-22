package com.example.fitapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.ui.components.MetricChip
import com.example.fitapp.ui.components.SectionCard
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.max
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb


@Composable
fun ProgressScreen() {
    val exerciseLogs by AppRepository.exerciseLogs.collectAsState()
    val foodLogs by AppRepository.foodLogs.collectAsState()

    // Woche: Montag–Sonntag (ISO)
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }

    // Aggregation je Tag
    val kcalInPerDay = days.map { d -> foodLogs.filter { it.date == d }.sumOf { it.caloriesIn } }
    val kcalOutPerDay = days.map { d -> exerciseLogs.filter { it.date == d }.sumOf { it.caloriesOut } }
    val minutesPerDay = days.map { d -> exerciseLogs.filter { it.date == d }.sumOf { it.durationMin } }
    val workoutsPerDay = days.map { d -> exerciseLogs.count { it.date == d } }

    // Summen für "Diese Woche"
    val sumWorkouts = workoutsPerDay.sum()
    val sumMinutes = minutesPerDay.sum()
    val sumKcalIn = kcalInPerDay.sum()
    val sumKcalOut = kcalOutPerDay.sum()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Fortschritt",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        item {
            SectionCard(title = "Diese Woche") {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricChip(label = "Workouts", value = "$sumWorkouts")
                    MetricChip(label = "Minuten", value = "$sumMinutes")
                    MetricChip(label = "kcal aufgenommen", value = "$sumKcalIn", filled = false)
                    MetricChip(label = "kcal verbraucht", value = "$sumKcalOut", filled = false)
                }
            }
        }

        item {
            SectionCard(title = "Kalorienbilanz (Mon–Son)") {
                val balance = kcalInPerDay.zip(kcalOutPerDay) { `in`, out -> `in` - out }
                MiniBarChart(
                    values = balance.map { it.toFloat() },
                    labels = days.map { it.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()) },
                    height = 140.dp,
                    posColor = MaterialTheme.colorScheme.primary,
                    negColor = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Positive Balken = Überschuss, Negative = Defizit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SectionCard(title = "Trainingsminuten (Mon–Son)") {
                MiniBarChart(
                    values = minutesPerDay.map { it.toFloat() },
                    labels = days.map { it.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()) },
                    height = 140.dp,
                    posColor = MaterialTheme.colorScheme.secondary,
                    negColor = MaterialTheme.colorScheme.secondary // nie negativ – gleiche Farbe
                )
            }
        }

        // Optional: Liste der Woche (kompakt)
        itemsIndexed(days) { index, date ->
            val w = workoutsPerDay[index]
            val m = minutesPerDay[index]
            val inK = kcalInPerDay[index]
            val outK = kcalOutPerDay[index]
            SectionCard(
                title = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                subtitle = date.toString()
            ) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricChip("Workouts", "$w")
                    MetricChip("Minuten", "$m")
                    MetricChip("In", "$inK kcal", filled = false)
                    MetricChip("Out", "$outK kcal", filled = false)
                }
            }
        }
    }
}

/**
 * Kleines, dependency-freies Balkendiagramm.
 * - Positive Werte: posColor (z. B. primary)
 * - Negative Werte: negColor (z. B. error)
 */
@Composable
private fun MiniBarChart(
    values: List<Float>,
    labels: List<String>,
    height: Dp,
    barSpacing: Dp = 8.dp,
    posColor: Color,
    negColor: Color,
    axisColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val maxAbs = max(1f, values.maxOfOrNull { kotlin.math.abs(it) } ?: 1f)

    val barSpacePx = with(androidx.compose.ui.platform.LocalDensity.current) { barSpacing.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val n = values.size
        if (n == 0) return@Canvas

        val zeroY = size.height / 2f
        // Achse
        drawLine(axisColor, Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 1.5f)

        val totalSpacing = barSpacePx * (n + 1)
        val barWidth = ((size.width - totalSpacing) / n).coerceAtLeast(2f)

        values.forEachIndexed { i, v ->
            val x = barSpacePx + i * (barWidth + barSpacePx)
            val frac = (kotlin.math.abs(v) / maxAbs).coerceIn(0f, 1f)
            val barHeight = frac * (size.height * 0.45f) // 45% je Richtung

            val color = if (v >= 0f) posColor else negColor
            val top = if (v >= 0f) zeroY - barHeight else zeroY
            val bottom = if (v >= 0f) zeroY else zeroY + barHeight

            drawRect(color = color, topLeft = Offset(x, top), size = androidx.compose.ui.geometry.Size(barWidth, bottom - top))
        }

        // Labels (sparsam): kleine Markierung unten
        val labelPaint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 10 * density
            color = axisColor.toArgb()
        }
        val labelY = size.height - 4 * density
        labels.forEachIndexed { i, label ->
            val x = barSpacePx + i * (barWidth + barSpacePx) + barWidth / 2f
            val textWidth = labelPaint.measureText(label)
            drawContext.canvas.nativeCanvas.drawText(label, x - textWidth / 2f, labelY, labelPaint)
        }

        // Rahmen (optional dezent)
        drawRect(
            color = axisColor.copy(alpha = 0.25f),
            style = Stroke(width = 1f)
        )
    }
}
