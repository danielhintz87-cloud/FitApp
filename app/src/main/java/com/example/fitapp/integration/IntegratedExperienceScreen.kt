package com.example.fitapp.integration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.services.*
import com.example.fitapp.ui.components.*
import com.example.fitapp.ui.screens.*
import com.example.fitapp.ui.nutrition.*
import kotlinx.coroutines.launch

/**
 * Integration Example Screen
 * Demonstrates how all enhanced systems work together seamlessly
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegratedExperienceScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val preferencesRepository = remember { UserPreferencesRepository(context) }
    val scope = rememberCoroutineScope()
    
    // Initialize all managers
    val workoutManager = remember { WorkoutExecutionManager(db, SmartRestTimer(context)) }
    val cookingManager = remember { CookingModeManager(db) }
    val shoppingManager = remember { ShoppingListManager(db, preferencesRepository) }
    
    // UI State
    var selectedDemo by remember { mutableStateOf<DemoType?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Integrierte FitApp Experience",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        when (selectedDemo) {
            DemoType.WORKOUT -> {
                EnhancedTrainingExecutionScreen(
                    planId = 1L, // Demo plan ID
                    onBackPressed = { selectedDemo = null },
                    onTrainingCompleted = { selectedDemo = null }
                )
            }
            DemoType.COOKING -> {
                EnhancedCookingModeScreen(
                    recipe = createDemoRecipe(),
                    onBackPressed = { selectedDemo = null },
                    onFinishCooking = { selectedDemo = null }
                )
            }
            DemoType.SHOPPING -> {
                SmartShoppingListScreen()
            }
            null -> {
                IntegrationOverviewScreen(
                    padding = padding,
                    onSelectDemo = { selectedDemo = it }
                )
            }
        }
    }
}

@Composable
private fun IntegrationOverviewScreen(
    padding: PaddingValues,
    onSelectDemo: (DemoType) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Funktionale Workout-Execution & Cooking-Mode",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Erlebe die neue, durchdachte User Experience mit umfassenden Features",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        item {
            Text(
                "System-Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            FeatureShowcaseCard(
                title = "💪 Enhanced Workout Execution",
                description = "Intelligente Trainingsführung mit adaptiven Pausen und Form-Tipps",
                features = listOf(
                    "Smart Rest Timer mit personalisierten Empfehlungen",
                    "RPE-basierte Belastungssteuerung",
                    "Automatische Gewichts-Vorschläge",
                    "Personal Record Tracking",
                    "Echtzeit Formqualität-Analyse"
                ),
                onDemo = { onSelectDemo(DemoType.WORKOUT) }
            )
        }
        
        item {
            FeatureShowcaseCard(
                title = "🍳 Intelligent Cooking Mode",
                description = "Schritt-für-Schritt Kochführung mit intelligenten Timern",
                features = listOf(
                    "Automatische Schritt-Erkennung aus Rezepten",
                    "Integrierte Timer für zeitbasierte Schritte",
                    "Temperatur- und Zutat-Anzeigen",
                    "Koch-Tipps und Formhinweise",
                    "Bildschirm-aktiv Modus"
                ),
                onDemo = { onSelectDemo(DemoType.COOKING) }
            )
        }
        
        item {
            FeatureShowcaseCard(
                title = "🛒 Smart Shopping List",
                description = "Intelligente Einkaufsliste mit automatischem Zutaten-Merging",
                features = listOf(
                    "Automatisches Zusammenführen ähnlicher Zutaten",
                    "Kategorisierung nach Supermarkt-Bereichen",
                    "Rezept-zu-Einkaufsliste Integration",
                    "Mengen-Umrechnung für verschiedene Portionen",
                    "Prioritäts-basierte Sortierung"
                ),
                onDemo = { onSelectDemo(DemoType.SHOPPING) }
            )
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Systemintegration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        "Alle Systeme arbeiten nahtlos zusammen:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    val integrationFeatures = listOf(
                        "Workout → Shopping: Protein-Shakes nach intensivem Training",
                        "Cooking → Nutrition: Automatisches Meal-Logging",
                        "Shopping → Cooking: Verfügbare Zutaten-Vorschläge",
                        "All Systems → Analytics: Umfassende Fortschrittsverfolgung"
                    )
                    
                    integrationFeatures.forEach { feature ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                feature,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Technische Highlights",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    val techFeatures = listOf(
                        "Reactive StateFlow Architecture",
                        "Comprehensive Database Migration System",
                        "Smart Algorithm für Ingredient Merging",
                        "Adaptive REST Timer mit ML-basierten Empfehlungen",
                        "Umfassende Reset-Funktionalität mit Fortschrittsverfolgung"
                    )
                    
                    techFeatures.forEach { feature ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                feature,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureShowcaseCard(
    title: String,
    description: String,
    features: List<String>,
    onDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(Modifier.height(16.dp))
            
            features.forEach { feature ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onDemo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Demo starten")
            }
        }
    }
}

private enum class DemoType {
    WORKOUT, COOKING, SHOPPING
}

private fun createDemoRecipe(): SavedRecipeEntity {
    return SavedRecipeEntity(
        id = "demo-recipe",
        title = "Protein-Bowl nach dem Training",
        markdown = """
            # Protein-Bowl nach dem Training
            
            ## Zutaten
            - 200g Quinoa
            - 150g Hähnchenbrust
            - 100g Brokkoli
            - 1 Avocado
            - 2 EL Olivenöl
            - Salz und Pfeffer
            
            ## Zubereitung
            
            ### Schritt 1: Quinoa kochen
            Quinoa in einem Topf mit doppelter Menge Wasser zum Kochen bringen. 15 Minuten köcheln lassen.
            > Tipp: Quinoa vorher kurz abspülen für besseren Geschmack
            
            ### Schritt 2: Hähnchen braten
            Hähnchenbrust in einer Pfanne mit Olivenöl bei mittlerer Hitze 6-8 Minuten pro Seite braten.
            Temperatur: 165°C Kerntemperatur
            > Tipp: Fleisch vor dem Braten Zimmertemperatur annehmen lassen
            
            ### Schritt 3: Brokkoli dämpfen
            Brokkoli in einem Dampfeinsatz 5 Minuten dämpfen bis er bissfest ist.
            > Tipp: Nicht zu lange dämpfen, damit Vitamine erhalten bleiben
            
            ### Schritt 4: Anrichten
            Alle Zutaten in einer Bowl anrichten und mit gewürfelter Avocado toppen.
        """.trimIndent(),
        calories = 450,
        imageUrl = null,
        ingredients = """["Quinoa", "Hähnchenbrust", "Brokkoli", "Avocado", "Olivenöl"]""",
        tags = "protein,post-workout,gesund",
        prepTime = 25,
        difficulty = "medium",
        servings = 2
    )
}