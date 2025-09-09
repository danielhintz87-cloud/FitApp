package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Enhanced Nutrition Hub Screen
 * Central hub for all nutrition and cooking features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNutritionHubScreen(
    navController: NavController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section
        item {
            NutritionHeroCard(
                onQuickRecipeSearch = { navController.navigate("enhanced_recipes") },
                onOpenDiary = { navController.navigate("food_diary") }
            )
        }
        
        // Recipe & Cooking Section
        item {
            NutritionSectionCard(
                title = "🍳 Rezepte & Kochen",
                description = "Entdecke neue Rezepte und koche mit KI-Unterstützung",
                actions = listOf(
                    NutritionAction("Alle Rezepte", Icons.Filled.Restaurant) { 
                        navController.navigate("enhanced_recipes") 
                    },
                    NutritionAction("Gespeicherte Rezepte", Icons.Filled.Bookmark) { 
                        navController.navigate("saved_recipes") 
                    },
                    NutritionAction("KI Rezept Generator", Icons.Filled.AutoAwesome) { 
                        navController.navigate("nutrition") 
                    }
                )
            )
        }
        
        // Food Tracking Section
        item {
            NutritionSectionCard(
                title = "📱 Ernährungstracking",
                description = "Verfolge deine tägliche Nahrungsaufnahme",
                actions = listOf(
                    NutritionAction("Ernährungstagebuch", Icons.AutoMirrored.Filled.MenuBook) { 
                        navController.navigate("food_diary") 
                    },
                    NutritionAction("Lebensmittel Scanner", Icons.Filled.PhotoCamera) { 
                        navController.navigate("foodscan") 
                    },
                    NutritionAction("Lebensmittel Suche", Icons.Filled.Search) { 
                        navController.navigate("food_search") 
                    }
                )
            )
        }
        
        // Shopping & Planning Section
        item {
            NutritionSectionCard(
                title = "🛒 Einkauf & Planung",
                description = "Plane deine Mahlzeiten und erstelle Einkaufslisten",
                actions = listOf(
                    NutritionAction("Einkaufsliste", Icons.Filled.ShoppingCart) { 
                        navController.navigate("shopping_list") 
                    },
                    NutritionAction("Mahlzeitenplaner", Icons.Filled.CalendarMonth) { 
                        // TODO: Implement meal planner
                        navController.navigate("nutrition") 
                    }
                )
            )
        }
        
        // Analytics & Goals Section
        item {
            NutritionSectionCard(
                title = "📊 Analyse & Ziele",
                description = "Verstehe deine Ernährungsgewohnheiten",
                actions = listOf(
                    NutritionAction("Nährstoff-Analyse", Icons.Filled.Analytics) { 
                        navController.navigate("nutrition_analytics") 
                    },
                    NutritionAction("Kalorien-Tracking", Icons.Filled.LocalFireDepartment) { 
                        navController.navigate("enhanced_analytics") 
                    }
                )
            )
        }
        
        // Today's Nutrition Summary
        item {
            TodaysNutritionCard(navController = navController)
        }
    }
}

@Composable
private fun NutritionHeroCard(
    onQuickRecipeSearch: () -> Unit,
    onOpenDiary: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🍽️ Ernährungs-Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Gesunde Ernährung leicht gemacht",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onQuickRecipeSearch,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Rezepte")
                }
                
                OutlinedButton(
                    onClick = onOpenDiary,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tagebuch")
                }
            }
        }
    }
}

@Composable
private fun NutritionSectionCard(
    title: String,
    description: String,
    actions: List<NutritionAction>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(actions) { action ->
                    OutlinedButton(
                        onClick = action.onClick,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Icon(
                            action.icon, 
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(action.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaysNutritionCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Heute's Ernährung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Nutrition Progress Bars (mock data)
            NutritionProgressItem(
                label = "Kalorien",
                current = 850,
                target = 2000,
                unit = "kcal",
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NutritionProgressItem(
                label = "Protein",
                current = 45,
                target = 120,
                unit = "g",
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NutritionProgressItem(
                label = "Wasser",
                current = 1.2f,
                target = 2.5f,
                unit = "L",
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("food_search") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Hinzufügen")
                }
                
                Button(
                    onClick = { navController.navigate("nutrition_analytics") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Analytics, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Details")
                }
            }
        }
    }
}

@Composable
private fun NutritionProgressItem(
    label: String,
    current: Number,
    target: Number,
    unit: String,
    color: androidx.compose.ui.graphics.Color
) {
    val progress = (current.toFloat() / target.toFloat()).coerceAtMost(1f)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$current / $target $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = color
        )
    }
}

data class NutritionAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
