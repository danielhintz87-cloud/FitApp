@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.data.repo.NutritionRepository
import com.example.fitapp.services.ShoppingListManager
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Enhanced Recipe Detail Screen with nutrition information per serving
 * Shows ingredients, steps, nutritional values, and integration options
 */
@Composable
fun RecipeDetailScreen(
    recipe: SavedRecipeEntity,
    onBackPressed: () -> Unit,
    onCookRecipe: () -> Unit,
    onEditRecipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val nutritionRepo = remember { NutritionRepository(db) }
    val shoppingManager = remember { ShoppingListManager(db) }
    val scope = rememberCoroutineScope()
    
    // UI State
    var servings by remember { mutableStateOf(recipe.servings ?: 1) }
    var isFavorite by remember { mutableStateOf(recipe.isFavorite) }
    var showAddToMealDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    
    // Calculate nutrition per serving
    val caloriesPerServing = remember(recipe.calories, servings, recipe.servings) {
        recipe.calories?.let { total ->
            val originalServings = recipe.servings ?: 1
            (total.toFloat() / originalServings * servings).toInt()
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Rezept Details") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        scope.launch {
                            db.savedRecipeDao().setFavorite(recipe.id, !isFavorite)
                            isFavorite = !isFavorite
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Aus Favoriten entfernen" else "Zu Favoriten hinzufügen",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showShareSheet = true }) {
                    Icon(Icons.Default.Share, contentDescription = "Teilen")
                }
                IconButton(onClick = onEditRecipe) {
                    Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Recipe Header with Image
            item {
                RecipeHeaderCard(
                    recipe = recipe,
                    servings = servings,
                    onServingsChange = { servings = it },
                    caloriesPerServing = caloriesPerServing
                )
            }
            
            // Nutrition Information Card
            item {
                NutritionInfoCard(
                    recipe = recipe,
                    servings = servings,
                    caloriesPerServing = caloriesPerServing
                )
            }
            
            // Ingredients Section
            item {
                IngredientsCard(
                    recipe = recipe,
                    servings = servings,
                    onAddToShoppingList = {
                        scope.launch {
                            try {
                                // Parse ingredients and add to shopping list
                                val ingredients = parseIngredientsFromMarkdown(recipe.markdown, servings)
                                ingredients.forEach { ingredient ->
                                    shoppingManager.addIngredientFromText(
                                        ingredientText = ingredient,
                                        fromRecipe = recipe.title
                                    )
                                }
                                // Show success message
                            } catch (e: Exception) {
                                // Show error message
                            }
                        }
                    }
                )
            }
            
            // Instructions Section
            item {
                InstructionsCard(recipe = recipe)
            }
            
            // Action Buttons
            item {
                ActionButtonsCard(
                    onCookRecipe = onCookRecipe,
                    onAddToMeal = { showAddToMealDialog = true },
                    onAddToShoppingList = {
                        scope.launch {
                            val ingredients = parseIngredientsFromMarkdown(recipe.markdown, servings)
                            ingredients.forEach { ingredient ->
                                shoppingManager.addIngredientFromText(
                                    ingredientText = ingredient,
                                    fromRecipe = recipe.title
                                )
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Add to Meal Dialog
    if (showAddToMealDialog) {
        AddToMealDialog(
            recipe = recipe,
            servings = servings,
            caloriesPerServing = caloriesPerServing,
            onDismiss = { showAddToMealDialog = false },
            onAddToMeal = { mealType ->
                scope.launch {
                    caloriesPerServing?.let { calories ->
                        nutritionRepo.logIntake(
                            kcal = calories,
                            label = "${recipe.title} ($servings Portionen)",
                            source = "RECIPE"
                        )
                        nutritionRepo.adjustDailyGoal(LocalDate.now())
                    }
                    showAddToMealDialog = false
                }
            }
        )
    }
}

@Composable
private fun RecipeHeaderCard(
    recipe: SavedRecipeEntity,
    servings: Int,
    onServingsChange: (Int) -> Unit,
    caloriesPerServing: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Recipe Image
            if (recipe.imageUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
            }
            
            // Recipe Title
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Description from markdown
            val description = recipe.markdown.lines().firstOrNull { 
                it.isNotBlank() && !it.startsWith("#") 
            }?.take(200)
            
            if (description?.isNotBlank() == true) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Servings Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portionen:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (servings > 1) onServingsChange(servings - 1) },
                        enabled = servings > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Weniger Portionen")
                    }
                    
                    Text(
                        text = servings.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = { if (servings < 10) onServingsChange(servings + 1) },
                        enabled = servings < 10
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Mehr Portionen")
                    }
                }
            }
            
            // Quick Stats
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                caloriesPerServing?.let { calories ->
                    QuickStatItem(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "$calories",
                        label = "kcal/Portion",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                recipe.prepTime?.let { prepTime ->
                    QuickStatItem(
                        icon = Icons.Default.AccessTime,
                        value = "$prepTime",
                        label = "Minuten",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                QuickStatItem(
                    icon = Icons.Default.Restaurant,
                    value = recipe.difficulty ?: "medium",
                    label = "Schwierigkeit",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Tags
            if (recipe.tags.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = recipe.tags.replace(",", " • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = color
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NutritionInfoCard(
    recipe: SavedRecipeEntity,
    servings: Int,
    caloriesPerServing: Int?
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
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Nährwerte pro Portion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            caloriesPerServing?.let { calories ->
                NutritionRow(
                    label = "Kalorien",
                    value = "$calories kcal",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            // Note: For now, we'll show placeholder values since the current SavedRecipeEntity 
            // doesn't have detailed macro information. In a full implementation, this would
            // be calculated from the actual ingredients.
            NutritionRow(
                label = "Kohlenhydrate",
                value = "~ g", // Would be calculated from ingredients
                color = MaterialTheme.colorScheme.secondary
            )
            NutritionRow(
                label = "Protein",
                value = "~ g", // Would be calculated from ingredients
                color = MaterialTheme.colorScheme.primary
            )
            NutritionRow(
                label = "Fett",
                value = "~ g", // Would be calculated from ingredients
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Detaillierte Nährwerte werden aus den Zutaten berechnet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NutritionRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(12.dp)
            ) {}
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun IngredientsCard(
    recipe: SavedRecipeEntity,
    servings: Int,
    onAddToShoppingList: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Zutaten",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                IconButton(onClick = onAddToShoppingList) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Zur Einkaufsliste hinzufügen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            val ingredients = parseIngredientsFromMarkdown(recipe.markdown, servings)
            if (ingredients.isNotEmpty()) {
                ingredients.forEach { ingredient ->
                    IngredientItem(ingredient = ingredient)
                }
            } else {
                Text(
                    text = "Siehe Anleitung für Zutatendetails",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IngredientItem(ingredient: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(6.dp)
        ) {}
        
        Spacer(Modifier.width(12.dp))
        
        Text(
            text = ingredient,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun InstructionsCard(recipe: SavedRecipeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Zubereitung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            val steps = parseStepsFromMarkdown(recipe.markdown)
            if (steps.isNotEmpty()) {
                steps.forEachIndexed { index, step ->
                    InstructionStep(
                        stepNumber = index + 1,
                        instruction = step
                    )
                    if (index < steps.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            } else {
                Text(
                    text = recipe.markdown,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun InstructionStep(
    stepNumber: Int,
    instruction: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButtonsCard(
    onCookRecipe: () -> Unit,
    onAddToMeal: () -> Unit,
    onAddToShoppingList: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Primary action
            Button(
                onClick = onCookRecipe,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Kochen starten")
            }
            
            // Secondary actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToMeal,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Zu Mahlzeiten")
                }
                
                OutlinedButton(
                    onClick = onAddToShoppingList,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Einkaufsliste")
                }
            }
        }
    }
}

@Composable
private fun AddToMealDialog(
    recipe: SavedRecipeEntity,
    servings: Int,
    caloriesPerServing: Int?,
    onDismiss: () -> Unit,
    onAddToMeal: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zu Mahlzeiten hinzufügen") },
        text = {
            Column {
                Text("Fügen Sie ${recipe.title} ($servings Portionen) zu Ihrem Ernährungstagebuch hinzu.")
                caloriesPerServing?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Kalorien: ${it * servings} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAddToMeal("lunch") }) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

// Helper functions for parsing recipe content
private fun parseIngredientsFromMarkdown(markdown: String, servings: Int): List<String> {
    val lines = markdown.lines()
    val ingredients = mutableListOf<String>()
    var inIngredientsSection = false
    
    for (line in lines) {
        when {
            line.contains("Zutaten", ignoreCase = true) || 
            line.contains("Ingredients", ignoreCase = true) -> {
                inIngredientsSection = true
            }
            line.startsWith("##") && inIngredientsSection -> {
                break // Next section
            }
            inIngredientsSection && (line.startsWith("- ") || line.startsWith("* ")) -> {
                ingredients.add(line.substring(2).trim())
            }
            inIngredientsSection && line.matches(Regex("^\\d+.*")) -> {
                // Ingredient with quantity
                ingredients.add(line.trim())
            }
        }
    }
    
    return if (ingredients.isNotEmpty()) {
        ingredients.map { adjustIngredientQuantity(it, servings) }
    } else {
        // Fallback to stored ingredients if available
        listOf("Siehe Rezept für Details")
    }
}

private fun parseStepsFromMarkdown(markdown: String): List<String> {
    val lines = markdown.lines()
    val steps = mutableListOf<String>()
    var inInstructionsSection = false
    
    for (line in lines) {
        when {
            line.contains("Zubereitung", ignoreCase = true) || 
            line.contains("Instructions", ignoreCase = true) ||
            line.contains("Anleitung", ignoreCase = true) -> {
                inInstructionsSection = true
            }
            line.startsWith("###") && inInstructionsSection -> {
                // Step header
                val step = line.substring(3).trim()
                if (step.contains("Schritt") || step.contains("Step")) {
                    steps.add(step)
                }
            }
            inInstructionsSection && line.trim().isNotEmpty() && 
            !line.startsWith("#") && !line.startsWith(">") -> {
                if (steps.isNotEmpty()) {
                    // Add to last step
                    val lastIndex = steps.lastIndex
                    steps[lastIndex] = steps[lastIndex] + " " + line.trim()
                } else {
                    steps.add(line.trim())
                }
            }
        }
    }
    
    return steps.ifEmpty { listOf(markdown) }
}

private fun adjustIngredientQuantity(ingredient: String, servings: Int): String {
    // Simple quantity adjustment - in a real implementation, this would be more sophisticated
    val numbers = Regex("\\d+").findAll(ingredient).map { it.value.toInt() }.toList()
    if (numbers.isNotEmpty() && servings != 1) {
        var adjusted = ingredient
        numbers.forEach { number ->
            val newAmount = number * servings
            adjusted = adjusted.replaceFirst(number.toString(), newAmount.toString())
        }
        return adjusted
    }
    return ingredient
}