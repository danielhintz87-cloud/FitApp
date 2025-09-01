package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.DailyGoalEntity
import com.example.fitapp.data.db.MealEntryEntity
import com.example.fitapp.data.db.FoodItemEntity
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDiaryScreen(
    contentPadding: PaddingValues,
    onBackPressed: () -> Unit,
    onAddFoodClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { NutritionRepository(AppDatabase.get(context)) }
    
    val today = remember { LocalDate.now() }
    val todayString = today.toString()
    
    var goal by remember { mutableStateOf<DailyGoalEntity?>(null) }
    var mealEntries by remember { mutableStateOf<List<MealEntryEntity>>(emptyList()) }
    var foodItems by remember { mutableStateOf<Map<String, FoodItemEntity>>(emptyMap()) }
    
    var totalCalories by remember { mutableFloatStateOf(0f) }
    var totalCarbs by remember { mutableFloatStateOf(0f) }
    var totalProtein by remember { mutableFloatStateOf(0f) }
    var totalFat by remember { mutableFloatStateOf(0f) }
    var totalWater by remember { mutableIntStateOf(0) }
    
    // Load data
    LaunchedEffect(todayString) {
        scope.launch {
            // Get goal by collecting first value
            val goalFlow = repo.goalFlow(today)
            goal = goalFlow.firstOrNull()
            
            mealEntries = repo.getMealEntriesForDate(todayString)
            
            // Load food items for all meal entries
            val foodItemIds = mealEntries.map { it.foodItemId }.distinct()
            val foodItemsList = foodItemIds.mapNotNull { id ->
                repo.getFoodItemById(id)
            }
            foodItems = foodItemsList.associateBy { it.id }
            
            // Calculate totals
            totalCalories = repo.getTotalCaloriesForDate(todayString)
            totalCarbs = repo.getTotalCarbsForDate(todayString)
            totalProtein = repo.getTotalProteinForDate(todayString)
            totalFat = repo.getTotalFatForDate(todayString)
            totalWater = repo.getTotalWaterForDate(todayString)
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Ernährungstagbuch",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onAddFoodClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Food")
            }
        }
        
        // Calories Overview Card
        CaloriesOverviewCard(
            consumedCalories = totalCalories.toInt(),
            targetCalories = goal?.targetKcal ?: 2000,
            burnedCalories = 0 // TODO: Connect with workout tracking
        )
        
        // Macros Overview Card
        MacrosOverviewCard(
            consumedCarbs = totalCarbs,
            consumedProtein = totalProtein,
            consumedFat = totalFat,
            targetCarbs = goal?.targetCarbs ?: 250f,
            targetProtein = goal?.targetProtein ?: 100f,
            targetFat = goal?.targetFat ?: 65f
        )
        
        // Water Tracking Card
        WaterTrackingCard(
            currentWater = totalWater,
            targetWater = goal?.targetWaterMl ?: 2000,
            onAddWater = { amount ->
                scope.launch {
                    repo.addWater(todayString, amount)
                    totalWater = repo.getTotalWaterForDate(todayString)
                }
            }
        )
        
        // Meals Section
        MealsSection(
            mealEntries = mealEntries,
            foodItems = foodItems,
            onDeleteMeal = { mealEntry ->
                scope.launch {
                    repo.deleteMealEntry(mealEntry.id)
                    mealEntries = repo.getMealEntriesForDate(todayString)
                    
                    // Recalculate totals
                    totalCalories = repo.getTotalCaloriesForDate(todayString)
                    totalCarbs = repo.getTotalCarbsForDate(todayString)
                    totalProtein = repo.getTotalProteinForDate(todayString)
                    totalFat = repo.getTotalFatForDate(todayString)
                }
            }
        )
    }
}

@Composable
private fun CaloriesOverviewCard(
    consumedCalories: Int,
    targetCalories: Int,
    burnedCalories: Int
) {
    val remainingCalories = targetCalories - consumedCalories + burnedCalories
    val progress = (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
    
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Kalorien",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Circular Progress Indicator
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        consumedCalories.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "von $targetCalories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calories breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CaloriesStat("Gegessen", consumedCalories, Icons.Default.Restaurant)
                CaloriesStat("Verbrannt", burnedCalories, Icons.Default.LocalFireDepartment)
                CaloriesStat("Übrig", remainingCalories, Icons.Default.Balance)
            }
        }
    }
}

@Composable
private fun CaloriesStat(label: String, value: Int, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MacrosOverviewCard(
    consumedCarbs: Float,
    consumedProtein: Float,
    consumedFat: Float,
    targetCarbs: Float,
    targetProtein: Float,
    targetFat: Float
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Makronährstoffe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MacroProgressBar(
                label = "Kohlenhydrate",
                consumed = consumedCarbs,
                target = targetCarbs,
                unit = "g",
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MacroProgressBar(
                label = "Protein",
                consumed = consumedProtein,
                target = targetProtein,
                unit = "g",
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MacroProgressBar(
                label = "Fett",
                consumed = consumedFat,
                target = targetFat,
                unit = "g",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun MacroProgressBar(
    label: String,
    consumed: Float,
    target: Float,
    unit: String,
    color: Color
) {
    val progress = if (target > 0) (consumed / target).coerceIn(0f, 1f) else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${consumed.toInt()}/${target.toInt()}$unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun WaterTrackingCard(
    currentWater: Int,
    targetWater: Int,
    onAddWater: (Int) -> Unit
) {
    val progress = if (targetWater > 0) (currentWater.toFloat() / targetWater.toFloat()).coerceIn(0f, 1f) else 0f
    
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Wasser",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${currentWater}ml / ${targetWater}ml",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quick add buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WaterQuickAddButton("200ml", 200, onAddWater)
                WaterQuickAddButton("300ml", 300, onAddWater)
                WaterQuickAddButton("500ml", 500, onAddWater)
            }
        }
    }
}

@Composable
private fun WaterQuickAddButton(
    label: String,
    amount: Int,
    onAddWater: (Int) -> Unit
) {
    OutlinedButton(
        onClick = { onAddWater(amount) },
        modifier = Modifier.size(width = 80.dp, height = 36.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MealsSection(
    mealEntries: List<MealEntryEntity>,
    foodItems: Map<String, FoodItemEntity>,
    onDeleteMeal: (MealEntryEntity) -> Unit
) {
    val mealsByType = mealEntries.groupBy { it.mealType }
    
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Mahlzeiten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            listOf("breakfast", "lunch", "dinner", "snack").forEach { mealType ->
                val mealTypeLabel = when (mealType) {
                    "breakfast" -> "Frühstück"
                    "lunch" -> "Mittagessen"
                    "dinner" -> "Abendessen"
                    "snack" -> "Snack"
                    else -> mealType
                }
                
                MealTypeSection(
                    mealType = mealTypeLabel,
                    entries = mealsByType[mealType] ?: emptyList(),
                    foodItems = foodItems,
                    onDeleteMeal = onDeleteMeal
                )
                
                if (mealType != "snack") {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun MealTypeSection(
    mealType: String,
    entries: List<MealEntryEntity>,
    foodItems: Map<String, FoodItemEntity>,
    onDeleteMeal: (MealEntryEntity) -> Unit
) {
    Column {
        Text(
            mealType,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        if (entries.isEmpty()) {
            Text(
                "Keine Einträge",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            entries.forEach { entry ->
                val foodItem = foodItems[entry.foodItemId]
                if (foodItem != null) {
                    MealEntryItem(
                        entry = entry,
                        foodItem = foodItem,
                        onDelete = { onDeleteMeal(entry) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun MealEntryItem(
    entry: MealEntryEntity,
    foodItem: FoodItemEntity,
    onDelete: () -> Unit
) {
    val calories = ((entry.quantityGrams / 100f) * foodItem.calories).toInt()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                foodItem.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${entry.quantityGrams.toInt()}g • ${calories} kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}