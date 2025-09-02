package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionAnalyticsScreen(
    contentPadding: PaddingValues,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(context)) }
    
    var selectedPeriod by remember { mutableStateOf("week") }
    var nutritionData by remember { mutableStateOf<List<DailyNutritionData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load nutrition data
    LaunchedEffect(selectedPeriod) {
        scope.launch {
            isLoading = true
            try {
                val endDate = LocalDate.now()
                val startDate = when (selectedPeriod) {
                    "week" -> endDate.minusDays(6)
                    "month" -> endDate.minusDays(29)
                    else -> endDate.minusDays(6)
                }
                
                val data = mutableListOf<DailyNutritionData>()
                var currentDate = startDate
                while (!currentDate.isAfter(endDate)) {
                    val dateString = currentDate.toString()
                    val calories = repo.getTotalCaloriesForDate(dateString)
                    val carbs = repo.getTotalCarbsForDate(dateString)
                    val protein = repo.getTotalProteinForDate(dateString)
                    val fat = repo.getTotalFatForDate(dateString)
                    val water = repo.getTotalWaterForDate(dateString)
                    val goal = repo.goalFlow(currentDate).firstOrNull()
                    
                    data.add(
                        DailyNutritionData(
                            date = currentDate,
                            calories = calories,
                            carbs = carbs,
                            protein = protein,
                            fat = fat,
                            water = water,
                            targetCalories = goal?.targetKcal ?: 2000,
                            targetCarbs = goal?.targetCarbs ?: 250f,
                            targetProtein = goal?.targetProtein ?: 100f,
                            targetFat = goal?.targetFat ?: 65f,
                            targetWater = goal?.targetWaterMl ?: 2000
                        )
                    )
                    currentDate = currentDate.plusDays(1)
                }
                nutritionData = data
            } finally {
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Ernährungs-Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        // Period selection
        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { selectedPeriod = it }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Weekly/Monthly summary cards
            NutritionSummaryCards(nutritionData)
            
            // Charts
            CaloriesChart(nutritionData)
            MacrosChart(nutritionData)
            WaterChart(nutritionData)
            
            // Daily breakdown
            DailyBreakdown(nutritionData)
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    val periods = listOf("week" to "Woche", "month" to "Monat")
    
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periods.forEach { (period, label) ->
                FilterChip(
                    onClick = { onPeriodSelected(period) },
                    label = { Text(label) },
                    selected = selectedPeriod == period,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NutritionSummaryCards(data: List<DailyNutritionData>) {
    val avgCalories = data.map { it.calories }.average().toInt()
    val avgProtein = data.map { it.protein }.average()
    val avgWater = data.map { it.water }.average().toInt()
    
    val goalAchievementRate = data.count { 
        it.calories >= (it.targetCalories * 0.9) && it.calories <= (it.targetCalories * 1.1)
    }.toFloat() / data.size * 100f
    
    LazyColumn(
        modifier = Modifier.height(180.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Ø Kalorien",
                    value = "$avgCalories kcal",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Zielerreichung",
                    value = "${goalAchievementRate.toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Ø Protein",
                    value = "${avgProtein.toInt()}g",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Ø Wasser",
                    value = "${avgWater}ml",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CaloriesChart(data: List<DailyNutritionData>) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Kalorienverbrauch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SimpleLineChart(
                data = data.map { it.calories },
                targetData = data.map { it.targetCalories.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun MacrosChart(data: List<DailyNutritionData>) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Makronährstoffe (Durchschnitt)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val avgCarbs = data.map { it.carbs }.average().toFloat()
            val avgProtein = data.map { it.protein }.average().toFloat()
            val avgFat = data.map { it.fat }.average().toFloat()
            
            MacrosPieChart(
                carbs = avgCarbs,
                protein = avgProtein,
                fat = avgFat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun WaterChart(data: List<DailyNutritionData>) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Wasserverbrauch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SimpleLineChart(
                data = data.map { it.water.toFloat() },
                targetData = data.map { it.targetWater.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun DailyBreakdown(data: List<DailyNutritionData>) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Täglicher Verlauf",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            data.forEach { dayData ->
                DailyBreakdownItem(dayData)
                if (dayData != data.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyBreakdownItem(data: DailyNutritionData) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM")
    val caloriesProgress = (data.calories / data.targetCalories).coerceIn(0f, 1f)
    val waterProgress = (data.water.toFloat() / data.targetWater).coerceIn(0f, 1f)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                data.date.format(formatter),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${data.calories.toInt()}/${data.targetCalories} kcal",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { caloriesProgress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Wasser: ${data.water}/${data.targetWater}ml",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${(waterProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    data: List<Float>,
    targetData: List<Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxValue = (data + targetData).maxOrNull() ?: 1f
        val minValue = (data + targetData).minOrNull() ?: 0f
        val range = maxValue - minValue
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        
        // Draw target line
        for (i in 0 until targetData.size - 1) {
            val x1 = i * stepX
            val x2 = (i + 1) * stepX
            val y1 = size.height - ((targetData[i] - minValue) / range) * size.height
            val y2 = size.height - ((targetData[i + 1] - minValue) / range) * size.height
            
            drawLine(
                color = secondaryColor,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 3.dp.toPx()
            )
        }
        
        // Draw actual data line
        for (i in 0 until data.size - 1) {
            val x1 = i * stepX
            val x2 = (i + 1) * stepX
            val y1 = size.height - ((data[i] - minValue) / range) * size.height
            val y2 = size.height - ((data[i + 1] - minValue) / range) * size.height
            
            drawLine(
                color = primaryColor,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 4.dp.toPx()
            )
        }
        
        // Draw data points
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minValue) / range) * size.height
            
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun MacrosPieChart(
    carbs: Float,
    protein: Float,
    fat: Float,
    modifier: Modifier = Modifier
) {
    val total = carbs + protein + fat
    if (total <= 0) return
    
    val carbsAngle = (carbs / total) * 360f
    val proteinAngle = (protein / total) * 360f
    val fatAngle = (fat / total) * 360f
    
    val carbsColor = MaterialTheme.colorScheme.primary
    val proteinColor = MaterialTheme.colorScheme.secondary
    val fatColor = MaterialTheme.colorScheme.tertiary
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier.size(100.dp)
        ) {
            val radius = size.minDimension / 2 * 0.8f
            val center = Offset(size.width / 2, size.height / 2)
            
            var currentAngle = -90f
            
            // Draw carbs
            drawArc(
                color = carbsColor,
                startAngle = currentAngle,
                sweepAngle = carbsAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            currentAngle += carbsAngle
            
            // Draw protein
            drawArc(
                color = proteinColor,
                startAngle = currentAngle,
                sweepAngle = proteinAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            currentAngle += proteinAngle
            
            // Draw fat
            drawArc(
                color = fatColor,
                startAngle = currentAngle,
                sweepAngle = fatAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            MacroLegendItem("Kohlenhydrate", "${carbs.toInt()}g", carbsColor)
            MacroLegendItem("Protein", "${protein.toInt()}g", proteinColor)
            MacroLegendItem("Fett", "${fat.toInt()}g", fatColor)
        }
    }
}

@Composable
private fun MacroLegendItem(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier.size(12.dp)
        ) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "$label: $value",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

data class DailyNutritionData(
    val date: LocalDate,
    val calories: Float,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val water: Int,
    val targetCalories: Int,
    val targetCarbs: Float,
    val targetProtein: Float,
    val targetFat: Float,
    val targetWater: Int
)