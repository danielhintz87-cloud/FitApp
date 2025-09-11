package com.example.fitapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Quick Actions Screen - Central hub for commonly used actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsScreen(
    onBack: () -> Unit,
    onNavigateToAction: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schnellaktionen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Schnelle Aktionen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Greifen Sie schnell auf die wichtigsten Funktionen zu",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Training Quick Actions
            item {
                QuickActionSection(
                    title = "Training",
                    icon = Icons.Filled.FitnessCenter,
                    actions = listOf(
                        QuickAction("Schnelles Training", Icons.Filled.PlayArrow, "todaytraining"),
                        QuickAction("HIIT Builder", Icons.Filled.Timer, "hiit_builder"),
                        QuickAction("KI Personal Trainer", Icons.Filled.Psychology, "ai_personal_trainer"),
                        QuickAction("Trainingsplan", Icons.Filled.CalendarMonth, "plan")
                    ),
                    onActionClick = onNavigateToAction
                )
            }
            
            // Nutrition Quick Actions
            item {
                QuickActionSection(
                    title = "Ernährung",
                    icon = Icons.Filled.Restaurant,
                    actions = listOf(
                        QuickAction("Barcode Scanner", Icons.Filled.QrCodeScanner, "foodscan"),
                        QuickAction("Rezept Generator", Icons.Filled.AutoAwesome, "recipe_generation"),
                        QuickAction("Ernährungstagebuch", Icons.Filled.MenuBook, "food_diary"),
                        QuickAction("Lebensmittel Suche", Icons.Filled.Search, "food_search")
                    ),
                    onActionClick = onNavigateToAction
                )
            }
            
            // Health Quick Actions
            item {
                QuickActionSection(
                    title = "Gesundheit",
                    icon = Icons.Filled.HealthAndSafety,
                    actions = listOf(
                        QuickAction("Gewicht erfassen", Icons.Filled.Scale, "weight_tracking"),
                        QuickAction("BMI Rechner", Icons.Filled.Calculate, "bmi_calculator"),
                        QuickAction("Fortschritt", Icons.Filled.Insights, "enhanced_analytics"),
                        QuickAction("Health Connect", Icons.Filled.Sync, "health_connect_settings")
                    ),
                    onActionClick = onNavigateToAction
                )
            }
            
            // Tools Quick Actions
            item {
                QuickActionSection(
                    title = "Tools",
                    icon = Icons.Filled.Build,
                    actions = listOf(
                        QuickAction("Einkaufsliste", Icons.Filled.ShoppingCart, "shopping_list"),
                        QuickAction("Einstellungen", Icons.Filled.Settings, "apikeys"),
                        QuickAction("Hilfe", Icons.Filled.Help, "help"),
                        QuickAction("Feedback", Icons.Filled.Feedback, "feedback")
                    ),
                    onActionClick = onNavigateToAction
                )
            }
            
            // Personalized Quick Actions (based on user behavior)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Empfohlen für Sie",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.height(120.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(personalizedActions) { action ->
                                QuickActionCard(
                                    action = action,
                                    onClick = { onNavigateToAction(action.route) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick Action Section Component
 */
@Composable
private fun QuickActionSection(
    title: String,
    icon: ImageVector,
    actions: List<QuickAction>,
    onActionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(actions) { action ->
                    QuickActionCard(
                        action = action,
                        onClick = { onActionClick(action.route) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Quick Action Card
 */
@Composable
private fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                action.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Data class for Quick Actions
 */
data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String = ""
)

/**
 * Personalized actions based on user behavior
 */
private val personalizedActions = listOf(
    QuickAction("Heute Training", Icons.Filled.PlayArrow, "todaytraining"),
    QuickAction("Rezepte", Icons.Filled.Restaurant, "enhanced_recipes"),
    QuickAction("Fortschritt", Icons.Filled.TrendingUp, "progress"),
    QuickAction("Scanner", Icons.Filled.QrCodeScanner, "foodscan")
)