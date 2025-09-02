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
import com.example.fitapp.data.db.WeightLossProgramEntity
import com.example.fitapp.data.repo.WeightLossRepository
import com.example.fitapp.domain.ActivityLevel
import com.example.fitapp.domain.WeightLossProgram
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLossProgramScreen(
    navController: NavController,
    initialBMI: Float? = null,
    initialTargetWeight: Float? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { WeightLossRepository(AppDatabase.get(context)) }
    
    var currentWeight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf(initialTargetWeight?.toString() ?: "") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var timeframeWeeks by remember { mutableStateOf("12") }
    var selectedActivityLevel by remember { mutableStateOf(ActivityLevel.MODERATELY_ACTIVE) }
    var showActivityLevelDialog by remember { mutableStateOf(false) }
    
    var calculatedProgram by remember { mutableStateOf<WeightLossProgram?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
    // Load active program
    var activeProgram by remember { mutableStateOf<WeightLossProgramEntity?>(null) }
    
    LaunchedEffect(Unit) {
        activeProgram = repository.getActiveWeightLossProgram()
    }
    
    // Show BMI-based suggestion when coming from BMI calculator
    val bmiSuggestion = initialBMI?.let { bmi ->
        when {
            bmi > 25f -> "Basierend auf Ihrem BMI von %.1f könnte ein Gewichtsverlust-Programm hilfreich sein.".format(bmi)
            bmi < 18.5f -> "Ihr BMI von %.1f liegt im untergewichtigen Bereich. Konsultieren Sie einen Arzt vor einem Gewichtsprogramm.".format(bmi)
            else -> "Ihr BMI von %.1f ist im normalen Bereich. Ein Programm kann bei der Erhaltung helfen.".format(bmi)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                "Abnehm-Programm",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Active program card
        activeProgram?.let { program ->
            ActiveProgramCard(
                program = program,
                onDeactivate = {
                    scope.launch {
                        repository.deactivateAllPrograms()
                        activeProgram = null
                        message = "Programm deaktiviert"
                    }
                }
            )
        }
        
        // BMI-based suggestion when coming from BMI calculator
        bmiSuggestion?.let { suggestion ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Program creation form
        if (activeProgram == null) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Neues Programm erstellen",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Basic data inputs
                    OutlinedTextField(
                        value = currentWeight,
                        onValueChange = { currentWeight = it },
                        label = { Text("Aktuelles Gewicht (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.FitnessCenter, contentDescription = null)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Zielgewicht (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.Flag, contentDescription = null)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Größe (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.Height, contentDescription = null)
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = timeframeWeeks,
                        onValueChange = { timeframeWeeks = it },
                        label = { Text("Zeitrahmen (Wochen)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.Schedule, contentDescription = null)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gender selection
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Activity level selection
                    Text(
                        "Aktivitätslevel",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedButton(
                        onClick = { showActivityLevelDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DirectionsRun, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedActivityLevel.germanName)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Calculate program button
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                try {
                                    val currentWeightFloat = currentWeight.toFloat()
                                    val targetWeightFloat = targetWeight.toFloat()
                                    val heightFloat = height.toFloat()
                                    val ageInt = age.toInt()
                                    val timeframeInt = timeframeWeeks.toInt()
                                    
                                    calculatedProgram = repository.createWeightLossProgram(
                                        currentWeight = currentWeightFloat,
                                        targetWeight = targetWeightFloat,
                                        heightCm = heightFloat,
                                        ageYears = ageInt,
                                        isMale = isMale,
                                        timeframeWeeks = timeframeInt,
                                        activityLevel = selectedActivityLevel
                                    )
                                    message = "Programm erfolgreich berechnet!"
                                } catch (e: Exception) {
                                    message = "Fehler bei der Berechnung: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && currentWeight.isNotBlank() && targetWeight.isNotBlank() && 
                                height.isNotBlank() && age.isNotBlank() && timeframeWeeks.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(Icons.Filled.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Programm berechnen")
                    }
                }
            }
        }
        
        // Calculated program display
        calculatedProgram?.let { program ->
            WeightLossProgramCard(
                program = program,
                currentWeight = currentWeight.toFloatOrNull() ?: 0f,
                targetWeight = targetWeight.toFloatOrNull() ?: 0f,
                timeframeWeeks = timeframeWeeks.toIntOrNull() ?: 12,
                onStartProgram = {
                    scope.launch {
                        isLoading = true
                        try {
                            // Deactivate existing programs
                            repository.deactivateAllPrograms()
                            
                            // Create new program
                            val programEntity = WeightLossProgramEntity(
                                startDate = LocalDate.now().toString(),
                                endDate = LocalDate.now().plusWeeks(timeframeWeeks.toLong()).toString(),
                                startWeight = currentWeight.toFloat(),
                                targetWeight = targetWeight.toFloat(),
                                currentWeight = currentWeight.toFloat(),
                                dailyCalorieTarget = program.dailyCalorieTarget,
                                weeklyWeightLossGoal = program.weeklyWeightLossGoal,
                                isActive = true,
                                programType = "standard"
                            )
                            
                            repository.saveWeightLossProgram(programEntity)
                            activeProgram = repository.getActiveWeightLossProgram()
                            calculatedProgram = null
                            message = "Programm erfolgreich gestartet!"
                        } catch (e: Exception) {
                            message = "Fehler beim Starten: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
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
    
    // Activity level selection dialog
    if (showActivityLevelDialog) {
        AlertDialog(
            onDismissRequest = { showActivityLevelDialog = false },
            title = { Text("Aktivitätslevel wählen") },
            text = {
                Column {
                    ActivityLevel.values().forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedActivityLevel == level,
                                onClick = { selectedActivityLevel = level }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    level.germanName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    getActivityDescription(level),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showActivityLevelDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ActiveProgramCard(
    program: WeightLossProgramEntity,
    onDeactivate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Aktives Programm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDeactivate) {
                    Icon(Icons.Filled.Close, contentDescription = "Deaktivieren")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Start: ${program.startWeight.toInt()}kg")
                Text("Ziel: ${program.targetWeight.toInt()}kg")
                Text("Aktuell: ${program.currentWeight.toInt()}kg")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = {
                    val totalLoss = program.startWeight - program.targetWeight
                    val currentLoss = program.startWeight - program.currentWeight
                    if (totalLoss > 0) currentLoss / totalLoss else 0f
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Tagesziel: ${program.dailyCalorieTarget} kcal",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Wöchentlich: ${program.weeklyWeightLossGoal}kg abnehmen",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun WeightLossProgramCard(
    program: WeightLossProgram,
    currentWeight: Float,
    targetWeight: Float,
    timeframeWeeks: Int,
    onStartProgram: () -> Unit,
    isLoading: Boolean
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Dein Abnehm-Programm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Key metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricCard(
                    title = "Tagesziel",
                    value = "${program.dailyCalorieTarget}",
                    unit = "kcal"
                )
                MetricCard(
                    title = "Pro Woche",
                    value = "%.1f".format(program.weeklyWeightLossGoal),
                    unit = "kg"
                )
                MetricCard(
                    title = "Gesamt",
                    value = "%.1f".format(currentWeight - targetWeight),
                    unit = "kg"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macro targets
            Text(
                "Makronährstoffe",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroCard("Protein", "${program.macroTargets.proteinGrams.toInt()}g")
                MacroCard("Kohlenhydrate", "${program.macroTargets.carbsGrams.toInt()}g")
                MacroCard("Fett", "${program.macroTargets.fatGrams.toInt()}g")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Empfohlenes Training: ${program.recommendedExerciseMinutes} Min/Tag",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Milestones
            if (program.milestones.isNotEmpty()) {
                Text(
                    "Meilensteine",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                program.milestones.take(3).forEach { milestone ->
                    Text(
                        "• ${milestone.description} (${milestone.estimatedDate})",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = onStartProgram,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Programm starten")
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun MacroCard(name: String, amount: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getActivityDescription(level: ActivityLevel): String {
    return when (level) {
        ActivityLevel.SEDENTARY -> "Büroarbeit, wenig Bewegung"
        ActivityLevel.LIGHTLY_ACTIVE -> "Leichte Aktivität 1-3x/Woche"
        ActivityLevel.MODERATELY_ACTIVE -> "Moderate Aktivität 3-5x/Woche"
        ActivityLevel.VERY_ACTIVE -> "Intensive Aktivität 6-7x/Woche"
        ActivityLevel.EXTRA_ACTIVE -> "Sehr intensive Aktivität, körperliche Arbeit"
    }
}