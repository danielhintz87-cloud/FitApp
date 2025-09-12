package com.example.fitapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/**
 * 📊 CRONOMETER-INSPIRED ADVANCED ANALYTICS DASHBOARD
 *
 * Design Elements based on cronometer_app_screen.png and nutrition_app_trends_charts.png:
 * - Scientific nutrition analysis like Cronometer
 * - Detailed micronutrient tracking
 * - Professional data visualization
 * - Health metric correlations
 * - Trend analysis with predictive insights
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CronometerInspiredAnalyticsScreen(
    contentPadding: PaddingValues,
    onNavigateToFeature: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTimeRange by remember { mutableStateOf(TimeRange.WEEK) }
    var selectedMetric by remember { mutableStateOf(AnalyticsMetric.NUTRITION) }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Analytics Header with Time Range Selector
        item {
            AnalyticsHeader(
                selectedTimeRange = selectedTimeRange,
                onTimeRangeChanged = { selectedTimeRange = it },
                selectedMetric = selectedMetric,
                onMetricChanged = { selectedMetric = it },
            )
        }

        // Key Metrics Overview (Cronometer-style)
        item {
            KeyMetricsOverview(
                timeRange = selectedTimeRange,
                metric = selectedMetric,
            )
        }

        // Advanced Charts Section
        item {
            AdvancedChartsSection(
                timeRange = selectedTimeRange,
                metric = selectedMetric,
            )
        }

        // Micronutrient Analysis (Cronometer signature feature)
        if (selectedMetric == AnalyticsMetric.NUTRITION) {
            item {
                MicronutrientAnalysisSection(
                    onNutrientClick = { nutrient -> onNavigateToFeature("nutrition_detail/$nutrient") },
                )
            }
        }

        // Correlation Analysis
        item {
            CorrelationAnalysisSection(
                metric = selectedMetric,
                onCorrelationClick = { correlation -> onNavigateToFeature("correlation_detail/$correlation") },
            )
        }

        // Predictive Insights (AI-powered)
        item {
            PredictiveInsightsSection(
                metric = selectedMetric,
                timeRange = selectedTimeRange,
                onInsightClick = { insight -> onNavigateToFeature(insight.actionRoute) },
            )
        }

        // Detailed Reports (Cronometer-style)
        item {
            DetailedReportsSection(
                metric = selectedMetric,
                onReportClick = { report -> onNavigateToFeature("report_detail/$report") },
            )
        }

        // Health Score Dashboard
        item {
            HealthScoreDashboard(
                onScoreClick = { score -> onNavigateToFeature("health_score_detail/$score") },
            )
        }
    }
}

enum class TimeRange(val displayName: String, val days: Int) {
    WEEK("7 Tage", 7),
    MONTH("30 Tage", 30),
    QUARTER("3 Monate", 90),
    YEAR("1 Jahr", 365),
}

enum class AnalyticsMetric(val displayName: String, val icon: String) {
    NUTRITION("Ernährung", "🥗"),
    FITNESS("Training", "💪"),
    WEIGHT("Gewicht", "⚖️"),
    HEALTH("Gesundheit", "❤️"),
    SLEEP("Schlaf", "😴"),
    HYDRATION("Hydration", "💧"),
}

@Composable
fun AnalyticsHeader(
    selectedTimeRange: TimeRange,
    onTimeRangeChanged: (TimeRange) -> Unit,
    selectedMetric: AnalyticsMetric,
    onMetricChanged: (AnalyticsMetric) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "📊 Advanced Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Icon(
                    Icons.Default.Science,
                    contentDescription = "Scientific Analysis",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(AnalyticsMetric.entries) { metric ->
                    FilterChip(
                        selected = selectedMetric == metric,
                        onClick = { onMetricChanged(metric) },
                        label = {
                            Text("${metric.icon} ${metric.displayName}")
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Range Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(TimeRange.entries) { range ->
                    FilterChip(
                        selected = selectedTimeRange == range,
                        onClick = { onTimeRangeChanged(range) },
                        label = { Text(range.displayName) },
                    )
                }
            }
        }
    }
}

@Composable
fun KeyMetricsOverview(
    timeRange: TimeRange,
    metric: AnalyticsMetric,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "${metric.icon} ${metric.displayName} - ${timeRange.displayName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Key metrics based on selected type
            when (metric) {
                AnalyticsMetric.NUTRITION -> NutritionKeyMetrics(timeRange)
                AnalyticsMetric.FITNESS -> FitnessKeyMetrics(timeRange)
                AnalyticsMetric.WEIGHT -> WeightKeyMetrics(timeRange)
                AnalyticsMetric.HEALTH -> HealthKeyMetrics(timeRange)
                AnalyticsMetric.SLEEP -> SleepKeyMetrics(timeRange)
                AnalyticsMetric.HYDRATION -> HydrationKeyMetrics(timeRange)
            }
        }
    }
}

@Composable
fun NutritionKeyMetrics(timeRange: TimeRange) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            listOf(
                MetricCard("Kalorien Ø", "1.847", "kcal", Color(0xFF2196F3), 92f),
                MetricCard("Protein Ø", "89.3", "g", Color(0xFF4CAF50), 89f),
                MetricCard("Carbs Ø", "201", "g", Color(0xFFFF9800), 78f),
                MetricCard("Fett Ø", "67.1", "g", Color(0xFF9C27B0), 95f),
                MetricCard("Fiber", "28.4", "g", Color(0xFF607D8B), 85f),
                MetricCard("Zucker", "45.2", "g", Color(0xFFF44336), 65f),
            ),
        ) { metric ->
            AnalyticsMetricCard(metric)
        }
    }
}

@Composable
fun FitnessKeyMetrics(timeRange: TimeRange) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            listOf(
                MetricCard("Workouts", "12", "Sessions", Color(0xFFFF5722), 85f),
                MetricCard("Volume", "2.847", "kg", Color(0xFF3F51B5), 92f),
                MetricCard("Cardio", "180", "min", Color(0xFF009688), 75f),
                MetricCard("Schritte Ø", "8.432", "/Tag", Color(0xFF795548), 68f),
                MetricCard("Kalorien", "2.340", "verbrannt", Color(0xFFE91E63), 88f),
            ),
        ) { metric ->
            AnalyticsMetricCard(metric)
        }
    }
}

@Composable
fun WeightKeyMetrics(timeRange: TimeRange) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            listOf(
                MetricCard("Gewicht Δ", "-2.3", "kg", Color(0xFF4CAF50), 78f),
                MetricCard("BMI", "23.4", "", Color(0xFF2196F3), 85f),
                MetricCard("Körperfett", "15.2", "%", Color(0xFFFF9800), 72f),
                MetricCard("Muskelmasse", "45.8", "kg", Color(0xFF9C27B0), 88f),
                MetricCard("Wasser", "58.3", "%", Color(0xFF00BCD4), 92f),
            ),
        ) { metric ->
            AnalyticsMetricCard(metric)
        }
    }
}

@Composable
fun HealthKeyMetrics(timeRange: TimeRange) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            listOf(
                MetricCard("RHR Ø", "58", "bpm", Color(0xFFF44336), 85f),
                MetricCard("HRV", "42.3", "ms", Color(0xFF4CAF50), 78f),
                MetricCard("VO2 Max", "48.7", "ml/kg/min", Color(0xFF2196F3), 92f),
                MetricCard("Stress", "3.2", "/10", Color(0xFFFF9800), 35f),
                MetricCard("Recovery", "8.1", "/10", Color(0xFF9C27B0), 81f),
            ),
        ) { metric ->
            AnalyticsMetricCard(metric)
        }
    }
}

@Composable
fun SleepKeyMetrics(timeRange: TimeRange) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            listOf(
                MetricCard("Schlaf Ø", "7h 23m", "", Color(0xFF3F51B5), 85f),
                MetricCard("Tiefschlaf", "1h 47m", "", Color(0xFF9C27B0), 78f),
                MetricCard("REM", "1h 32m", "", Color(0xFF2196F3), 72f),
                MetricCard("Qualität", "8.2", "/10", Color(0xFF4CAF50), 82f),
                MetricCard("Effizienz", "89.3", "%", Color(0xFFFF9800), 89f),
            ),
        ) { metric ->
            AnalyticsMetricCard(metric)
        }
    }
}

@Composable
fun HydrationKeyMetrics(timeRange: TimeRange) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            listOf(
                MetricCard("Wasser Ø", "2.3", "L", Color(0xFF00BCD4), 85f),
                MetricCard("Ziel erreicht", "85", "%", Color(0xFF4CAF50), 85f),
                MetricCard("Beste Serie", "12", "Tage", Color(0xFF2196F3), 75f),
                MetricCard("Koffein", "180", "mg", Color(0xFF795548), 60f),
                MetricCard("Elektrolyte", "Good", "", Color(0xFF9C27B0), 88f),
            ),
        ) { metric ->
            AnalyticsMetricCard(metric)
        }
    }
}

data class MetricCard(
    val title: String,
    val value: String,
    val unit: String,
    val color: Color,
    val percentage: Float,
)

@Composable
fun AnalyticsMetricCard(metric: MetricCard) {
    Card(
        modifier = Modifier.width(120.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = metric.color.copy(alpha = 0.1f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = metric.title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = metric.color,
            )

            if (metric.unit.isNotEmpty()) {
                Text(
                    text = metric.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = metric.color.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini progress indicator
            LinearProgressIndicator(
                progress = { metric.percentage / 100f },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                color = metric.color,
            )

            Text(
                text = "${metric.percentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = metric.color,
            )
        }
    }
}

@Composable
fun AdvancedChartsSection(
    timeRange: TimeRange,
    metric: AnalyticsMetric,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "📈 Trend Analysis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chart based on metric type
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
            ) {
                when (metric) {
                    AnalyticsMetric.NUTRITION -> NutritionTrendChart(timeRange)
                    AnalyticsMetric.FITNESS -> FitnessTrendChart(timeRange)
                    AnalyticsMetric.WEIGHT -> WeightTrendChart(timeRange)
                    AnalyticsMetric.HEALTH -> HealthTrendChart(timeRange)
                    AnalyticsMetric.SLEEP -> SleepTrendChart(timeRange)
                    AnalyticsMetric.HYDRATION -> HydrationTrendChart(timeRange)
                }
            }
        }
    }
}

@Composable
fun NutritionTrendChart(timeRange: TimeRange) {
    // Mock data for nutrition trends
    val calorieData = generateMockData(timeRange.days, 1500f, 2200f)
    val proteinData = generateMockData(timeRange.days, 60f, 120f)

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        drawNutritionChart(
            calorieData = calorieData,
            proteinData = proteinData,
            canvasWidth = size.width,
            canvasHeight = size.height,
        )
    }
}

@Composable
fun WeightTrendChart(timeRange: TimeRange) {
    val weightData = generateMockWeightData(timeRange.days)

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        drawWeightChart(
            weightData = weightData,
            canvasWidth = size.width,
            canvasHeight = size.height,
        )
    }
}

@Composable
fun FitnessTrendChart(timeRange: TimeRange) {
    val volumeData = generateMockData(timeRange.days, 800f, 3000f)

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        drawFitnessChart(
            volumeData = volumeData,
            canvasWidth = size.width,
            canvasHeight = size.height,
        )
    }
}

@Composable
fun HealthTrendChart(timeRange: TimeRange) {
    val hrData = generateMockData(timeRange.days, 55f, 75f)

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        drawHealthChart(
            hrData = hrData,
            canvasWidth = size.width,
            canvasHeight = size.height,
        )
    }
}

@Composable
fun SleepTrendChart(timeRange: TimeRange) {
    val sleepData = generateMockData(timeRange.days, 6.5f, 8.5f)

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        drawSleepChart(
            sleepData = sleepData,
            canvasWidth = size.width,
            canvasHeight = size.height,
        )
    }
}

@Composable
fun HydrationTrendChart(timeRange: TimeRange) {
    val hydrationData = generateMockData(timeRange.days, 1.5f, 3.0f)

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        drawHydrationChart(
            hydrationData = hydrationData,
            canvasWidth = size.width,
            canvasHeight = size.height,
        )
    }
}

// Chart drawing functions
fun DrawScope.drawNutritionChart(
    calorieData: List<Float>,
    proteinData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    val stepX = canvasWidth / (calorieData.size - 1)
    val calorieMax = calorieData.maxOrNull() ?: 2200f
    val calorieMin = calorieData.minOrNull() ?: 1500f

    // Draw calorie line
    val caloriePath = Path()
    calorieData.forEachIndexed { index, value ->
        val x = index * stepX
        val y = canvasHeight - ((value - calorieMin) / (calorieMax - calorieMin)) * canvasHeight
        if (index == 0) caloriePath.moveTo(x, y) else caloriePath.lineTo(x, y)
    }

    drawPath(
        path = caloriePath,
        color = Color(0xFF2196F3),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
    )

    // Draw protein line (scaled)
    val proteinMax = proteinData.maxOrNull() ?: 120f
    val proteinMin = proteinData.minOrNull() ?: 60f
    val proteinPath = Path()
    proteinData.forEachIndexed { index, value ->
        val x = index * stepX
        val y = canvasHeight - ((value - proteinMin) / (proteinMax - proteinMin)) * canvasHeight
        if (index == 0) proteinPath.moveTo(x, y) else proteinPath.lineTo(x, y)
    }

    drawPath(
        path = proteinPath,
        color = Color(0xFF4CAF50),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
    )
}

fun DrawScope.drawWeightChart(
    weightData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    val stepX = canvasWidth / (weightData.size - 1)
    val weightMax = weightData.maxOrNull() ?: 85f
    val weightMin = weightData.minOrNull() ?: 78f

    val weightPath = Path()
    weightData.forEachIndexed { index, value ->
        val x = index * stepX
        val y = canvasHeight - ((value - weightMin) / (weightMax - weightMin)) * canvasHeight
        if (index == 0) weightPath.moveTo(x, y) else weightPath.lineTo(x, y)
    }

    // Fill area under curve
    val fillPath =
        Path().apply {
            addPath(weightPath)
            lineTo(canvasWidth, canvasHeight)
            lineTo(0f, canvasHeight)
            close()
        }

    drawPath(
        path = fillPath,
        brush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        Color(0xFF4CAF50).copy(alpha = 0.3f),
                        Color(0xFF4CAF50).copy(alpha = 0.1f),
                    ),
            ),
    )

    drawPath(
        path = weightPath,
        color = Color(0xFF4CAF50),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
    )
}

fun DrawScope.drawFitnessChart(
    volumeData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    val stepX = canvasWidth / volumeData.size
    val volumeMax = volumeData.maxOrNull() ?: 3000f

    volumeData.forEachIndexed { index, value ->
        val x = index * stepX + stepX / 2
        val barHeight = (value / volumeMax) * canvasHeight
        val y = canvasHeight - barHeight

        drawRoundRect(
            color = Color(0xFFFF5722).copy(alpha = 0.8f),
            topLeft = androidx.compose.ui.geometry.Offset(x - stepX / 3, y),
            size = androidx.compose.ui.geometry.Size(stepX * 2 / 3, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
        )
    }
}

fun DrawScope.drawHealthChart(
    hrData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    val stepX = canvasWidth / (hrData.size - 1)
    val hrMax = hrData.maxOrNull() ?: 75f
    val hrMin = hrData.minOrNull() ?: 55f

    val hrPath = Path()
    hrData.forEachIndexed { index, value ->
        val x = index * stepX
        val y = canvasHeight - ((value - hrMin) / (hrMax - hrMin)) * canvasHeight
        if (index == 0) hrPath.moveTo(x, y) else hrPath.lineTo(x, y)
    }

    drawPath(
        path = hrPath,
        color = Color(0xFFF44336),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
    )

    // Draw data points
    hrData.forEachIndexed { index, value ->
        val x = index * stepX
        val y = canvasHeight - ((value - hrMin) / (hrMax - hrMin)) * canvasHeight
        drawCircle(
            color = Color(0xFFF44336),
            radius = 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(x, y),
        )
    }
}

fun DrawScope.drawSleepChart(
    sleepData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    val stepX = canvasWidth / (sleepData.size - 1)
    val sleepMax = sleepData.maxOrNull() ?: 8.5f
    val sleepMin = sleepData.minOrNull() ?: 6.5f

    val sleepPath = Path()
    sleepData.forEachIndexed { index, value ->
        val x = index * stepX
        val y = canvasHeight - ((value - sleepMin) / (sleepMax - sleepMin)) * canvasHeight
        if (index == 0) sleepPath.moveTo(x, y) else sleepPath.lineTo(x, y)
    }

    drawPath(
        path = sleepPath,
        color = Color(0xFF3F51B5),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
    )
}

fun DrawScope.drawHydrationChart(
    hydrationData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    val stepX = canvasWidth / hydrationData.size
    val hydrationMax = hydrationData.maxOrNull() ?: 3.0f

    hydrationData.forEachIndexed { index, value ->
        val x = index * stepX + stepX / 2
        val barHeight = (value / hydrationMax) * canvasHeight
        val y = canvasHeight - barHeight

        drawRoundRect(
            color = Color(0xFF00BCD4).copy(alpha = 0.7f),
            topLeft = androidx.compose.ui.geometry.Offset(x - stepX / 3, y),
            size = androidx.compose.ui.geometry.Size(stepX * 2 / 3, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
        )
    }
}

// Mock data generators
fun generateMockData(
    days: Int,
    min: Float,
    max: Float,
): List<Float> {
    return (0 until days).map {
        min + (max - min) * (0.3f + 0.4f * sin(it * 0.2f) + 0.3f * kotlin.random.Random.nextFloat())
    }
}

fun generateMockWeightData(days: Int): List<Float> {
    val startWeight = 82f
    val targetWeight = 78f
    val weightLossRate = (startWeight - targetWeight) / days

    return (0 until days).map { day ->
        val idealWeight = startWeight - (weightLossRate * day)
        val noise = (kotlin.random.Random.nextFloat() - 0.5f) * 0.8f
        idealWeight + noise
    }
}

@Composable
fun MicronutrientAnalysisSection(onNutrientClick: (String) -> Unit) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🔬 Micronutrient Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Cronometer-Style",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Micronutrient grid
            val micronutrients =
                listOf(
                    MicronutrientData("Vitamin D", 2.3f, 20f, "μg", Color(0xFFFFD700)),
                    MicronutrientData("B12", 4.2f, 2.4f, "μg", Color(0xFF4CAF50)),
                    MicronutrientData("Folate", 320f, 400f, "μg", Color(0xFF2196F3)),
                    MicronutrientData("Iron", 12.8f, 18f, "mg", Color(0xFFF44336)),
                    MicronutrientData("Calcium", 850f, 1000f, "mg", Color(0xFF9C27B0)),
                    MicronutrientData("Omega-3", 1.2f, 1.6f, "g", Color(0xFF00BCD4)),
                    MicronutrientData("Zinc", 8.4f, 11f, "mg", Color(0xFF795548)),
                    MicronutrientData("Magnesium", 280f, 320f, "mg", Color(0xFF607D8B)),
                )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(micronutrients) { nutrient ->
                    MicronutrientCard(
                        nutrient = nutrient,
                        onClick = { onNutrientClick(nutrient.name) },
                    )
                }
            }
        }
    }
}

data class MicronutrientData(
    val name: String,
    val current: Float,
    val target: Float,
    val unit: String,
    val color: Color,
)

@Composable
fun MicronutrientCard(
    nutrient: MicronutrientData,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .width(100.dp)
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = nutrient.color.copy(alpha = 0.1f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = nutrient.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val percentage = (nutrient.current / nutrient.target).coerceAtMost(2f)

            // Circular progress indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(50.dp),
            ) {
                CircularProgressIndicator(
                    progress = { percentage.coerceAtMost(1f) },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 4.dp,
                    color =
                        when {
                            percentage >= 1f -> Color(0xFF4CAF50)
                            percentage >= 0.8f -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        },
                )

                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${nutrient.current} ${nutrient.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = nutrient.color,
            )
        }
    }
}

@Composable
fun CorrelationAnalysisSection(
    metric: AnalyticsMetric,
    onCorrelationClick: (String) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "🔗 Correlation Analysis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val correlations =
                when (metric) {
                    AnalyticsMetric.NUTRITION ->
                        listOf(
                            CorrelationData("Protein ↔ Gewichtsverlust", 0.78f, "Starke positive Korrelation"),
                            CorrelationData("Kalorien ↔ Training", -0.42f, "Moderate negative Korrelation"),
                            CorrelationData("Fiber ↔ Sättigung", 0.65f, "Moderate positive Korrelation"),
                        )
                    AnalyticsMetric.FITNESS ->
                        listOf(
                            CorrelationData("Volume ↔ Kraft", 0.85f, "Sehr starke Korrelation"),
                            CorrelationData("Cardio ↔ RHR", -0.71f, "Starke negative Korrelation"),
                            CorrelationData("Training ↔ Schlaf", 0.58f, "Moderate Korrelation"),
                        )
                    else ->
                        listOf(
                            CorrelationData("Schlaf ↔ Recovery", 0.82f, "Starke Korrelation"),
                            CorrelationData("Stress ↔ HRV", -0.67f, "Moderate negative Korrelation"),
                        )
                }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                correlations.forEach { correlation ->
                    CorrelationItem(
                        correlation = correlation,
                        onClick = { onCorrelationClick(correlation.title) },
                    )
                }
            }
        }
    }
}

data class CorrelationData(
    val title: String,
    val value: Float,
    val description: String,
)

@Composable
fun CorrelationItem(
    correlation: CorrelationData,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = correlation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = correlation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "${if (correlation.value >= 0) "+" else ""}${(correlation.value * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color =
                    when {
                        correlation.value >= 0.7f -> Color(0xFF4CAF50)
                        correlation.value >= 0.3f -> Color(0xFFFF9800)
                        correlation.value >= -0.3f -> Color(0xFF607D8B)
                        correlation.value >= -0.7f -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
            )
        }
    }
}

@Composable
fun PredictiveInsightsSection(
    metric: AnalyticsMetric,
    timeRange: TimeRange,
    onInsightClick: (PredictiveInsight) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "🔮 AI Predictive Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val insights = generatePredictiveInsights(metric, timeRange)

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                insights.forEach { insight ->
                    PredictiveInsightCard(
                        insight = insight,
                        onClick = { onInsightClick(insight) },
                    )
                }
            }
        }
    }
}

data class PredictiveInsight(
    val title: String,
    val description: String,
    val confidence: Float,
    val actionRoute: String,
    val icon: String,
    val color: Color,
)

@Composable
fun PredictiveInsightCard(
    insight: PredictiveInsight,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = insight.color.copy(alpha = 0.1f),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = insight.icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Confidence: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    LinearProgressIndicator(
                        progress = { insight.confidence },
                        modifier =
                            Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                        color = insight.color,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(insight.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = insight.color,
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedReportsSection(
    metric: AnalyticsMetric,
    onReportClick: (String) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "📄 Detailed Reports",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val reports =
                when (metric) {
                    AnalyticsMetric.NUTRITION ->
                        listOf(
                            "📊 Comprehensive Nutrition Report",
                            "🥗 Meal Pattern Analysis",
                            "⚖️ Calorie Balance Summary",
                            "🔬 Micronutrient Deep Dive",
                        )
                    AnalyticsMetric.FITNESS ->
                        listOf(
                            "💪 Strength Progress Report",
                            "🏃 Cardio Performance Analysis",
                            "📈 Volume Load Progression",
                            "🎯 Goal Achievement Summary",
                        )
                    else ->
                        listOf(
                            "📋 Health Metrics Overview",
                            "📈 Trend Analysis Report",
                            "🎯 Goal Progress Summary",
                        )
                }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                reports.forEach { report ->
                    ReportItem(
                        title = report,
                        onClick = { onReportClick(report) },
                    )
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    title: String,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "View Report",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun HealthScoreDashboard(onScoreClick: (String) -> Unit) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "🏆 Health Score Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Overall Health Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                HealthScoreItem(
                    title = "Overall",
                    score = 87,
                    maxScore = 100,
                    color = Color(0xFF4CAF50),
                    onClick = { onScoreClick("overall") },
                )
                HealthScoreItem(
                    title = "Nutrition",
                    score = 92,
                    maxScore = 100,
                    color = Color(0xFF2196F3),
                    onClick = { onScoreClick("nutrition") },
                )
                HealthScoreItem(
                    title = "Fitness",
                    score = 78,
                    maxScore = 100,
                    color = Color(0xFFFF5722),
                    onClick = { onScoreClick("fitness") },
                )
                HealthScoreItem(
                    title = "Recovery",
                    score = 85,
                    maxScore = 100,
                    color = Color(0xFF9C27B0),
                    onClick = { onScoreClick("recovery") },
                )
            }
        }
    }
}

@Composable
fun HealthScoreItem(
    title: String,
    score: Int,
    maxScore: Int,
    color: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp),
        ) {
            CircularProgressIndicator(
                progress = { score.toFloat() / maxScore },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 6.dp,
                color = color,
            )

            Text(
                text = "$score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

// Helper function to generate predictive insights
fun generatePredictiveInsights(
    metric: AnalyticsMetric,
    timeRange: TimeRange,
): List<PredictiveInsight> {
    return when (metric) {
        AnalyticsMetric.NUTRITION ->
            listOf(
                PredictiveInsight(
                    title = "Protein Timing Optimization",
                    description = "Verschiebe 20g Protein auf nach dem Training für 15% bessere Muskelproteinsynthese",
                    confidence = 0.89f,
                    actionRoute = "protein_timing",
                    icon = "🥩",
                    color = Color(0xFF4CAF50),
                ),
                PredictiveInsight(
                    title = "Gewichtsverlust Plateau Warnung",
                    description = "Wahrscheinlichkeit für Plateau in 8 Tagen. Refeed Day empfohlen.",
                    confidence = 0.73f,
                    actionRoute = "plateau_prevention",
                    icon = "⚠️",
                    color = Color(0xFFFF9800),
                ),
            )
        AnalyticsMetric.FITNESS ->
            listOf(
                PredictiveInsight(
                    title = "Deload Week Recommendation",
                    description = "Fatigue-Score deutet auf Übertraining. Deload in 5 Tagen optimal.",
                    confidence = 0.91f,
                    actionRoute = "deload_planning",
                    icon = "😴",
                    color = Color(0xFF9C27B0),
                ),
                PredictiveInsight(
                    title = "PR Opportunity",
                    description = "Optimale Bedingungen für neuen Bankpress-Rekord in 3 Tagen erkannt",
                    confidence = 0.67f,
                    actionRoute = "pr_planning",
                    icon = "🏆",
                    color = Color(0xFFFFD700),
                ),
            )
        else ->
            listOf(
                PredictiveInsight(
                    title = "Sleep Optimization",
                    description = "Schlafqualität-Trend deutet auf bessere Recovery bei 22:30 Schlafenszeit",
                    confidence = 0.84f,
                    actionRoute = "sleep_optimization",
                    icon = "😴",
                    color = Color(0xFF3F51B5),
                ),
            )
    }
}
