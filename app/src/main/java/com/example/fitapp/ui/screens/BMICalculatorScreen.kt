package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.repo.WeightLossRepository
import com.example.fitapp.domain.BMICalculator
import com.example.fitapp.domain.BMICategory
import com.example.fitapp.domain.BMIResult
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
    navController: NavController,
    onWeightLossProgramSuggested: ((bmi: Float, targetWeight: Float) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { WeightLossRepository(AppDatabase.get(context)) }
    
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var useMetric by remember { mutableStateOf(true) }
    var bmiResult by remember { mutableStateOf<BMIResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
    // Real-time BMI calculation
    LaunchedEffect(height, weight) {
        if (height.isNotBlank() && weight.isNotBlank()) {
            val heightValue = height.toFloatOrNull()
            val weightValue = weight.toFloatOrNull()
            
            if (heightValue != null && weightValue != null && heightValue > 0 && weightValue > 0) {
                val heightCm = if (useMetric) heightValue else heightValue * 2.54f
                val weightKg = if (useMetric) weightValue else weightValue * 0.453592f
                
                if (heightCm in 100f..250f && weightKg in 20f..300f) {
                    bmiResult = BMICalculator.calculateBMIResult(heightCm, weightKg)
                }
            }
        } else {
            bmiResult = null
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
            }
            Text(
                "BMI Rechner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Unit switcher
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Einheiten",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { useMetric = true },
                        label = { Text("Metrisch (cm/kg)") },
                        selected = useMetric,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { useMetric = false },
                        label = { Text("Imperial (in/lbs)") },
                        selected = !useMetric,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Input fields
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Deine Daten",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { 
                        Text(if (useMetric) "Größe (cm)" else "Größe (in)") 
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Filled.Height, contentDescription = null)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { 
                        Text(if (useMetric) "Gewicht (kg)" else "Gewicht (lbs)") 
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Filled.FitnessCenter, contentDescription = null)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Alter (Jahre)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Filled.Cake, contentDescription = null)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Geschlecht",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { isMale = true },
                        label = { Text("Männlich") },
                        selected = isMale,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { isMale = false },
                        label = { Text("Weiblich") },
                        selected = !isMale,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // BMI Result Display
        bmiResult?.let { result ->
            BMIResultCard(
                result = result,
                onSaveClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val heightCm = if (useMetric) height.toFloat() else height.toFloat() * 2.54f
                            val weightKg = if (useMetric) weight.toFloat() else weight.toFloat() * 0.453592f
                            
                            repository.calculateAndSaveBMI(
                                heightCm = heightCm,
                                weightKg = weightKg,
                                date = LocalDate.now().toString()
                            )
                            message = "BMI erfolgreich gespeichert!"
                        } catch (e: Exception) {
                            message = "Fehler beim Speichern: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                onCreateWeightLossProgramClick = { targetWeight ->
                    onWeightLossProgramSuggested?.invoke(result.bmi, targetWeight)
                },
                isLoading = isLoading
            )
        }
        
        // Message display
        if (message.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (message.contains("Fehler")) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.contains("Fehler")) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun BMIResultCard(
    result: BMIResult,
    onSaveClick: () -> Unit,
    onCreateWeightLossProgramClick: (Float) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(result.category.colorCode)).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Dein BMI",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                "%.1f".format(result.bmi),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color(android.graphics.Color.parseColor(result.category.colorCode))
            )
            
            Text(
                result.category.germanName,
                style = MaterialTheme.typography.titleLarge,
                color = Color(android.graphics.Color.parseColor(result.category.colorCode)),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // BMI Scale Visualizer
            BMIScaleVisualizer(
                currentBMI = result.bmi,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            
            // Ideal weight range
            Text(
                "Idealgewicht: %.1f - %.1f kg".format(
                    result.idealWeightRange.start,
                    result.idealWeightRange.endInclusive
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Weight loss recommendation
            result.recommendedWeightLoss?.let { weightLoss ->
                if (weightLoss > 0) {
                    Text(
                        "Empfohlener Gewichtsverlust: %.1f kg".format(weightLoss),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = { onCreateWeightLossProgramClick(result.idealWeightRange.endInclusive) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abnehm-Programm erstellen")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            OutlinedButton(
                onClick = onSaveClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("BMI speichern")
            }
        }
    }
}

@Composable
fun BMIScaleVisualizer(
    currentBMI: Float,
    targetBMI: Float? = null,
    modifier: Modifier = Modifier
) {
    val categories = BMICategory.values()
    
    Column(modifier = modifier) {
        Text(
            "BMI-Skala",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        ) {
            categories.forEach { category ->
                val weight = when (category) {
                    BMICategory.UNDERWEIGHT -> 0.185f
                    BMICategory.NORMAL -> 0.265f  
                    BMICategory.OVERWEIGHT -> 0.25f
                    BMICategory.OBESE -> 0.3f
                }
                
                Surface(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight(),
                    color = Color(android.graphics.Color.parseColor(category.colorCode)).copy(alpha = 0.7f)
                ) {}
            }
        }
        
        // BMI markers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            // Current BMI indicator
            val currentPosition = when {
                currentBMI < 18.5f -> (currentBMI / 18.5f) * 0.185f
                currentBMI < 25f -> 0.185f + ((currentBMI - 18.5f) / 6.5f) * 0.265f
                currentBMI < 30f -> 0.45f + ((currentBMI - 25f) / 5f) * 0.25f
                else -> 0.7f + minOf((currentBMI - 30f) / 20f, 1f) * 0.3f
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(currentPosition.coerceIn(0f, 1f))
                    .height(4.dp)
                    .align(Alignment.CenterStart)
            ) {
                Surface(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.CenterEnd),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
            }
            
            Text(
                "%.1f".format(currentBMI),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(currentPosition.coerceIn(0f, 1f))
                    .wrapContentWidth(Alignment.End)
            )
            
            // Target BMI indicator (if provided)
            targetBMI?.let { target ->
                val targetPosition = when {
                    target < 18.5f -> (target / 18.5f) * 0.185f
                    target < 25f -> 0.185f + ((target - 18.5f) / 6.5f) * 0.265f
                    target < 30f -> 0.45f + ((target - 25f) / 5f) * 0.25f
                    else -> 0.7f + minOf((target - 30f) / 20f, 1f) * 0.3f
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(targetPosition.coerceIn(0f, 1f))
                        .height(4.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.CenterEnd),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.secondary,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                }
                
                Text(
                    "Ziel: %.1f".format(target),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth(targetPosition.coerceIn(0f, 1f))
                        .wrapContentWidth(Alignment.End)
                )
            }
        }
        
        // Scale labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.forEach { category ->
                Text(
                    category.germanName,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}