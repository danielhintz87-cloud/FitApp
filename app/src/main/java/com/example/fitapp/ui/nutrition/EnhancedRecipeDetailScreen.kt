package com.example.fitapp.ui.nutrition

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.services.RecipeFavoritesManager
import com.example.fitapp.services.SimilarRecipesEngine
import com.example.fitapp.services.ShoppingListManager
import kotlinx.coroutines.launch

/**
 * Enhanced Recipe Detail Screen with Modern UI
 * Displays recipe details with cooking mode integration, favorites, and similar recipes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedRecipeDetailScreen(
    recipe: SavedRecipeEntity,
    onBackPressed: () -> Unit,
    onStartCooking: (SavedRecipeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context) }
    val preferencesRepository = remember { UserPreferencesRepository(context) }
    val favoritesManager = remember { RecipeFavoritesManager(database) }
    val similarRecipesEngine = remember { SimilarRecipesEngine(context, database) }
    val shoppingManager = remember { ShoppingListManager(database, preferencesRepository) }
    val scope = rememberCoroutineScope()
    
    // State
    var isFavorite by remember { mutableStateOf(recipe.isFavorite) }
    var showSimilarRecipes by remember { mutableStateOf(false) }
    var showIngredientDialog by remember { mutableStateOf(false) }
    var showNutritionInfo by remember { mutableStateOf(false) }
    var similarRecipes by remember { mutableStateOf<List<SimilarRecipesEngine.SimilarRecipeResult>>(emptyList()) }
    
    // Load similar recipes
    LaunchedEffect(recipe.id) {
        try {
            similarRecipes = similarRecipesEngine.findSimilarRecipes(recipe, maxResults = 5)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = recipe.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    // Favorite toggle
                    IconButton(
                        onClick = {
                            scope.launch {
                                isFavorite = !isFavorite
                                favoritesManager.toggleFavorite(
                                    recipe.id,
                                    if (isFavorite) "loved" else "general"
                                )
                            }
                        }
                    ) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Aus Favoriten entfernen" else "Zu Favoriten hinzufügen",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // More options
                    IconButton(onClick = { /* Show more options */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Mehr Optionen")
                    }
                }
            )
            
            // Scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero image
                item {
                    RecipeHeroImage(recipe = recipe)
                }
                
                // Quick stats
                item {
                    RecipeQuickStats(recipe = recipe)
                }
                
                // Action buttons row
                item {
                    RecipeActionButtons(
                        onStartCooking = { onStartCooking(recipe) },
                        onAddToShoppingList = { showIngredientDialog = true },
                        onShowNutrition = { showNutritionInfo = true },
                        onShowSimilar = { showSimilarRecipes = true }
                    )
                }
                
                // Ingredients section
                item {
                    RecipeIngredientsSection(
                        recipe = recipe,
                        onAddToShoppingList = { showIngredientDialog = true }
                    )
                }
                
                // Instructions section
                item {
                    RecipeInstructionsSection(recipe = recipe)
                }
                
                // Tags section
                if (recipe.tags.isNotEmpty()) {
                    item {
                        RecipeTagsSection(tags = recipe.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                    }
                }
                
                // Similar recipes section
                if (similarRecipes.isNotEmpty()) {
                    item {
                        SimilarRecipesSection(
                            similarRecipes = similarRecipes.map { it.recipe },
                            onRecipeClick = { /* Navigate to recipe */ }
                        )
                    }
                }
                
                // Cooking tips section
                item {
                    CookingTipsSection(recipe = recipe)
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Floating Action Button for cooking
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            FloatingActionButton(
                onClick = { onStartCooking(recipe) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Filled.Restaurant,
                    contentDescription = "Kochmodus starten"
                )
            }
        }
    }
    
    // Dialogs
    if (showIngredientDialog) {
        AddIngredientsDialog(
            recipe = recipe,
            shoppingManager = shoppingManager,
            onDismiss = { showIngredientDialog = false }
        )
    }
    
    if (showSimilarRecipes) {
        SimilarRecipesDetailDialog(
            recipe = recipe,
            similarRecipes = similarRecipes.map { it.recipe },
            similarRecipesEngine = similarRecipesEngine,
            onDismiss = { showSimilarRecipes = false },
            onRecipeSelected = { /* Navigate to recipe */ }
        )
    }
    
    if (showNutritionInfo) {
        NutritionInfoDialog(
            recipe = recipe,
            onDismiss = { showNutritionInfo = false }
        )
    }
}

/**
 * Hero image section with gradient overlay
 */
@Composable
private fun RecipeHeroImage(recipe: SavedRecipeEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Placeholder or actual image
        recipe.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: run {
            // Placeholder with recipe icon
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
        
        // Gradient overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        ),
                        startY = 100f
                    )
                )
        )
        
        // Recipe title overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Quick stats row (time, servings, difficulty, calories)
 */
@Composable
private fun RecipeQuickStats(recipe: SavedRecipeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            recipe.prepTime?.let { prepTime ->
                QuickStatItem(
                    icon = Icons.Filled.AccessTime,
                    label = "Zeit",
                    value = "$prepTime min",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            recipe.servings?.let { servings ->
                QuickStatItem(
                    icon = Icons.Filled.Group,
                    label = "Portionen",
                    value = servings.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            recipe.difficulty?.let { difficulty ->
                QuickStatItem(
                    icon = when (difficulty.lowercase()) {
                        "einfach" -> Icons.AutoMirrored.Filled.TrendingFlat
                        "mittel" -> Icons.AutoMirrored.Filled.TrendingUp
                        "schwer" -> Icons.Filled.Whatshot
                        else -> Icons.AutoMirrored.Filled.TrendingUp
                    },
                    label = "Schwierigkeit",
                    value = difficulty,
                    color = when (difficulty.lowercase()) {
                        "einfach" -> Color.Green
                        "mittel" -> Color(0xFFFFA500) // Orange replacement
                        "schwer" -> Color.Red
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
            
            recipe.calories?.let { calories ->
                QuickStatItem(
                    icon = Icons.Filled.LocalFireDepartment,
                    label = "Kalorien",
                    value = calories.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Action buttons row
 */
@Composable
private fun RecipeActionButtons(
    onStartCooking: () -> Unit,
    onAddToShoppingList: () -> Unit,
    onShowNutrition: () -> Unit,
    onShowSimilar: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            Button(
                onClick = onStartCooking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Restaurant, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Kochen")
            }
        }
        
        item {
            OutlinedButton(onClick = onAddToShoppingList) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Einkaufsliste")
            }
        }
        
        item {
            OutlinedButton(onClick = onShowNutrition) {
                Icon(Icons.Filled.BarChart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nährwerte")
            }
        }
        
        item {
            OutlinedButton(onClick = onShowSimilar) {
                Icon(Icons.Filled.Recommend, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ähnliche")
            }
        }
    }
}

/**
 * Ingredients section with shopping list integration
 */
@Composable
private fun RecipeIngredientsSection(
    recipe: SavedRecipeEntity,
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
                Text(
                    text = "Zutaten",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onAddToShoppingList) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Hinzufügen")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Parse and display ingredients
            val ingredients = parseIngredients(recipe.markdown)
            if (ingredients.isNotEmpty()) {
                ingredients.forEach { ingredient ->
                    IngredientItem(ingredient = ingredient)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "Zutaten im Rezept-Text enthalten",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IngredientItem(ingredient: String) {
    var isChecked by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isChecked = !isChecked },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = ingredient,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant 
                   else MaterialTheme.colorScheme.onSurface,
            modifier = if (isChecked) Modifier else Modifier
        )
    }
}

/**
 * Instructions section with step-by-step view
 */
@Composable
private fun RecipeInstructionsSection(recipe: SavedRecipeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Zubereitung",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Parse and display instructions
            val instructions = parseInstructions(recipe.markdown)
            if (instructions.isNotEmpty()) {
                instructions.forEachIndexed { index, instruction ->
                    InstructionStep(
                        stepNumber = index + 1,
                        instruction = instruction
                    )
                    if (index < instructions.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            } else {
                Text(
                    text = recipe.markdown,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
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
        modifier = Modifier.fillMaxWidth()
    ) {
        // Step number circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Instruction text
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Tags section
 */
@Composable
private fun RecipeTagsSection(tags: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags) { tag ->
                    AssistChip(
                        onClick = { /* Filter by tag */ },
                        label = { Text(tag) }
                    )
                }
            }
        }
    }
}

/**
 * Similar recipes section
 */
@Composable
private fun SimilarRecipesSection(
    similarRecipes: List<SavedRecipeEntity>,
    onRecipeClick: (SavedRecipeEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ähnliche Rezepte",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(similarRecipes) { recipe ->
                    SimilarRecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimilarRecipeCard(
    recipe: SavedRecipeEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${recipe.prepTime ?: "?"} min",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Cooking tips section
 */
@Composable
private fun CookingTipsSection(recipe: SavedRecipeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Kochtipps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val tips = listOf(
                "Alle Zutaten vor dem Kochen bereitlegen",
                "Bei hohen Temperaturen immer aufpassen",
                "Gewürze am Ende hinzufügen für besseren Geschmack"
            )
            
            tips.forEach { tip ->
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

// Dialog Components

@Composable
private fun AddIngredientsDialog(
    recipe: SavedRecipeEntity,
    shoppingManager: ShoppingListManager,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Zur Einkaufsliste hinzufügen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Alle Zutaten von '${recipe.title}' zur Einkaufsliste hinzufügen?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abbrechen")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                // TODO: Add ingredients to shopping list
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hinzufügen")
                    }
                }
            }
        }
    }
}

@Composable
private fun SimilarRecipesDetailDialog(
    recipe: SavedRecipeEntity,
    similarRecipes: List<SavedRecipeEntity>,
    similarRecipesEngine: SimilarRecipesEngine,
    onDismiss: () -> Unit,
    onRecipeSelected: (SavedRecipeEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Ähnliche Rezepte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn {
                    items(similarRecipes) { similarRecipe ->
                        SimilarRecipeListItem(
                            recipe = similarRecipe,
                            onClick = { onRecipeSelected(similarRecipe) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Schließen")
                }
            }
        }
    }
}

@Composable
private fun SimilarRecipeListItem(
    recipe: SavedRecipeEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${recipe.prepTime ?: "?"} min",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun NutritionInfoDialog(
    recipe: SavedRecipeEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Nährwerte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                recipe.calories?.let { calories ->
                    NutritionInfoRow(
                        label = "Kalorien",
                        value = "$calories kcal"
                    )
                }
                
                // TODO: Add more nutrition info when available
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Schließen")
                }
            }
        }
    }
}

@Composable
private fun NutritionInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// Helper functions for parsing recipe content
private fun parseIngredients(markdown: String): List<String> {
    // Simple parsing - look for lines starting with numbers or dashes
    return markdown.lines()
        .filter { line ->
            val trimmed = line.trim()
            trimmed.matches(Regex("^\\d+.*")) || 
            trimmed.startsWith("-") || 
            trimmed.startsWith("*")
        }
        .map { it.trim().removePrefix("-").removePrefix("*").trim() }
        .filter { it.isNotBlank() }
}

private fun parseInstructions(markdown: String): List<String> {
    // Simple parsing - split by double newlines and filter
    return markdown.split("\n\n")
        .filter { it.trim().isNotBlank() }
        .map { it.trim() }
}
