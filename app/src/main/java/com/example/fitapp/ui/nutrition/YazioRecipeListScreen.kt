@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.domain.entities.*
import com.example.fitapp.services.YazioRecipeManager
import kotlinx.coroutines.launch
import com.example.fitapp.ui.util.applyContentPadding

/**
 * Enhanced Recipe List Screen with YAZIO-style advanced filtering
 */
@Composable
fun YazioRecipeListScreen(
    onBackPressed: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onCreateRecipe: () -> Unit,
    onEditRecipe: (Recipe) -> Unit,
    onStartCooking: (Recipe) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val recipeManager = remember { YazioRecipeManager(db) }
    val scope = rememberCoroutineScope()

    // State
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(RecipeFilters()) }
    var viewMode by remember { mutableStateOf(RecipeViewMode.GRID) }

    // Load recipes
    fun loadRecipes() {
        scope.launch {
            isLoading = true
            try {
                val currentFilters = filters.copy(searchQuery = searchQuery.ifBlank { null })
                recipes = recipeManager.searchRecipes(currentFilters)
                error = null
            } catch (e: Exception) {
                error = "Fehler beim Laden der Rezepte: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Load recipes on filter change
    LaunchedEffect(filters, searchQuery) {
        loadRecipes()
    }

    Column(modifier.fillMaxSize().applyContentPadding(contentPadding)) {
        // Top App Bar
        TopAppBar(
            title = { Text("Rezepte") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { viewMode = if (viewMode == RecipeViewMode.GRID) RecipeViewMode.LIST else RecipeViewMode.GRID }) {
                    Icon(
                        if (viewMode == RecipeViewMode.GRID) Icons.Filled.ViewList else Icons.Filled.ViewModule,
                        contentDescription = "Ansicht wechseln"
                    )
                }
                IconButton(onClick = { showFilters = !showFilters }) {
                    Badge(
                        containerColor = if (filters.hasActiveFilters()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                }
                IconButton(onClick = onCreateRecipe) {
                    Icon(Icons.Filled.Add, contentDescription = "Neues Rezept")
                }
            }
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Rezepte durchsuchen...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Suche löschen")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Filters Section
        if (showFilters) {
            YazioFiltersSection(
                filters = filters,
                onFiltersChange = { filters = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    ErrorMessage(
                        message = error!!,
                        onRetry = { loadRecipes() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                recipes.isEmpty() -> {
                    EmptyRecipesMessage(
                        onCreateRecipe = onCreateRecipe,
                        hasFilters = filters.hasActiveFilters() || searchQuery.isNotEmpty(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    RecipeContent(
                        recipes = recipes,
                        viewMode = viewMode,
                        onRecipeClick = onRecipeClick,
                        onEditRecipe = onEditRecipe,
                        onStartCooking = onStartCooking,
                        onToggleFavorite = { recipe ->
                            scope.launch {
                                // Toggle favorite logic would go here
                                loadRecipes()
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class RecipeViewMode { GRID, LIST }

@Composable
private fun YazioFiltersSection(
    filters: RecipeFilters,
    onFiltersChange: (RecipeFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Filter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Category Filters
            Text("Kategorien", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(RecipeCategory.entries.take(10)) { category -> // Show first 10 categories
                    FilterChip(
                        onClick = { 
                            val currentCategories = filters.categories.toMutableList()
                            if (category in currentCategories) {
                                currentCategories.remove(category)
                            } else {
                                currentCategories.add(category)
                            }
                            onFiltersChange(filters.copy(categories = currentCategories))
                        },
                        label = { 
                            Text(
                                formatCategoryName(category),
                                style = MaterialTheme.typography.bodySmall
                            ) 
                        },
                        selected = category in filters.categories
                    )
                }
            }

            // Difficulty and Time Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Difficulty
                Column(modifier = Modifier.weight(1f)) {
                    Text("Schwierigkeit", style = MaterialTheme.typography.bodyMedium)
                    var expandedDifficulty by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDifficulty,
                        onExpandedChange = { expandedDifficulty = !expandedDifficulty }
                    ) {
                        OutlinedTextField(
                            value = filters.difficulty?.let { formatDifficultyName(it) } ?: "Alle",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDifficulty) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDifficulty,
                            onDismissRequest = { expandedDifficulty = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Alle") },
                                onClick = {
                                    onFiltersChange(filters.copy(difficulty = null))
                                    expandedDifficulty = false
                                }
                            )
                            RecipeDifficulty.entries.forEach { difficulty ->
                                DropdownMenuItem(
                                    text = { Text(formatDifficultyName(difficulty)) },
                                    onClick = {
                                        onFiltersChange(filters.copy(difficulty = difficulty))
                                        expandedDifficulty = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Max Prep Time
                Column(modifier = Modifier.weight(1f)) {
                    Text("Max. Vorbereitungszeit", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = filters.maxPrepTime?.toString() ?: "",
                        onValueChange = { value ->
                            onFiltersChange(filters.copy(maxPrepTime = value.toIntOrNull()))
                        },
                        label = { Text("Minuten") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Quick Filters
            Text("Schnellfilter", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        onClick = { 
                            onFiltersChange(filters.copy(isVegetarian = !(filters.isVegetarian ?: false)))
                        },
                        label = { Text("Vegetarisch") },
                        selected = filters.isVegetarian == true
                    )
                }
                item {
                    FilterChip(
                        onClick = { 
                            onFiltersChange(filters.copy(isVegan = !(filters.isVegan ?: false)))
                        },
                        label = { Text("Vegan") },
                        selected = filters.isVegan == true
                    )
                }
                item {
                    FilterChip(
                        onClick = { 
                            onFiltersChange(filters.copy(isGlutenFree = !(filters.isGlutenFree ?: false)))
                        },
                        label = { Text("Glutenfrei") },
                        selected = filters.isGlutenFree == true
                    )
                }
                item {
                    FilterChip(
                        onClick = { 
                            onFiltersChange(filters.copy(createdByMe = !(filters.createdByMe ?: false)))
                        },
                        label = { Text("Meine Rezepte") },
                        selected = filters.createdByMe == true
                    )
                }
                item {
                    FilterChip(
                        onClick = { 
                            onFiltersChange(filters.copy(isFavorite = !(filters.isFavorite ?: false)))
                        },
                        label = { Text("Favoriten") },
                        selected = filters.isFavorite == true
                    )
                }
            }

            // Clear Filters Button
            if (filters.hasActiveFilters()) {
                OutlinedButton(
                    onClick = { onFiltersChange(RecipeFilters()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Filter zurücksetzen")
                }
            }
        }
    }
}

@Composable
private fun RecipeContent(
    recipes: List<Recipe>,
    viewMode: RecipeViewMode,
    onRecipeClick: (Recipe) -> Unit,
    onEditRecipe: (Recipe) -> Unit,
    onStartCooking: (Recipe) -> Unit,
    onToggleFavorite: (Recipe) -> Unit
) {
    when (viewMode) {
        RecipeViewMode.GRID -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(recipes) { recipe ->
                    RecipeGridCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) },
                        onEditClick = { onEditRecipe(recipe) },
                        onCookClick = { onStartCooking(recipe) },
                        onFavoriteClick = { onToggleFavorite(recipe) }
                    )
                }
            }
        }
        RecipeViewMode.LIST -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recipes) { recipe ->
                    RecipeListCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) },
                        onEditClick = { onEditRecipe(recipe) },
                        onCookClick = { onStartCooking(recipe) },
                        onFavoriteClick = { onToggleFavorite(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeGridCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onCookClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Recipe Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (recipe.imageUrl != null) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Difficulty Badge
                if (recipe.difficulty != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = getDifficultyColor(recipe.difficulty)
                    ) {
                        Text(
                            formatDifficultyName(recipe.difficulty),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Favorite Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        if (false) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, // Would check actual favorite status
                        contentDescription = "Favorit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Recipe Info
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (recipe.description.isNotBlank()) {
                    Text(
                        recipe.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Recipe Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recipe.prepTime != null) {
                        RecipeStatChip(
                            icon = Icons.Filled.Schedule,
                            text = "${recipe.prepTime}min"
                        )
                    }
                    if (recipe.servings != null) {
                        RecipeStatChip(
                            icon = Icons.Filled.People,
                            text = "${recipe.servings}"
                        )
                    }
                    if (recipe.nutrition?.calories != null) {
                        RecipeStatChip(
                            icon = Icons.Filled.LocalFireDepartment,
                            text = "${recipe.nutrition.calories}kcal"
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Bearbeiten", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Button(
                        onClick = onCookClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Kochen", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeListCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onCookClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Recipe Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (recipe.imageUrl != null) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Recipe Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            if (false) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, // Would check actual favorite status
                            contentDescription = "Favorit",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (recipe.description.isNotBlank()) {
                    Text(
                        recipe.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Recipe Stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recipe.prepTime != null) {
                        RecipeStatChip(
                            icon = Icons.Filled.Schedule,
                            text = "${recipe.prepTime}min"
                        )
                    }
                    if (recipe.servings != null) {
                        RecipeStatChip(
                            icon = Icons.Filled.People,
                            text = "${recipe.servings}"
                        )
                    }
                    if (recipe.nutrition?.calories != null) {
                        RecipeStatChip(
                            icon = Icons.Filled.LocalFireDepartment,
                            text = "${recipe.nutrition.calories}kcal"
                        )
                    }
                    if (recipe.difficulty != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = getDifficultyColor(recipe.difficulty)
                        ) {
                            Text(
                                formatDifficultyName(recipe.difficulty),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Bearbeiten", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Button(
                        onClick = onCookClick,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Kochen", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyRecipesMessage(
    onCreateRecipe: () -> Unit,
    hasFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Filled.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            if (hasFilters) "Keine Rezepte gefunden" else "Noch keine Rezepte vorhanden",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            if (hasFilters) 
                "Versuchen Sie andere Filter oder erstellen Sie ein neues Rezept"
            else 
                "Erstellen Sie Ihr erstes Rezept oder nutzen Sie den AI-Generator",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(onClick = onCreateRecipe) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Rezept erstellen")
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            "Fehler",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(onClick = onRetry) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Wiederholen")
        }
    }
}

// Helper functions
private fun RecipeFilters.hasActiveFilters(): Boolean {
    return categories.isNotEmpty() ||
           difficulty != null ||
           maxPrepTime != null ||
           maxCookTime != null ||
           maxCalories != null ||
           isVegetarian == true ||
           isVegan == true ||
           isGlutenFree == true ||
           createdByMe == true ||
           isFavorite == true
}

@Composable
private fun getDifficultyColor(difficulty: RecipeDifficulty) = when (difficulty) {
    RecipeDifficulty.EASY -> MaterialTheme.colorScheme.primary
    RecipeDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
    RecipeDifficulty.HARD -> MaterialTheme.colorScheme.error
}

private fun formatDifficultyName(difficulty: RecipeDifficulty) = when (difficulty) {
    RecipeDifficulty.EASY -> "Einfach"
    RecipeDifficulty.MEDIUM -> "Mittel"
    RecipeDifficulty.HARD -> "Schwer"
}

private fun formatCategoryName(category: RecipeCategory): String {
    return when (category) {
        RecipeCategory.LOW_CARB -> "Low-Carb"
        RecipeCategory.VEGETARIAN -> "Vegetarisch"
        RecipeCategory.VEGAN -> "Vegan"
        RecipeCategory.DESSERTS -> "Desserts"
        RecipeCategory.PIZZA -> "Pizza"
        RecipeCategory.SALADS -> "Salate"
        RecipeCategory.BREAKFAST -> "Frühstück"
        RecipeCategory.LUNCH -> "Mittagessen"
        RecipeCategory.DINNER -> "Abendessen"
        RecipeCategory.SNACKS -> "Snacks"
        RecipeCategory.APPETIZERS -> "Vorspeisen"
        RecipeCategory.MAIN_COURSE -> "Hauptgericht"
        RecipeCategory.SIDE_DISHES -> "Beilagen"
        RecipeCategory.SOUPS -> "Suppen"
        RecipeCategory.BEVERAGES -> "Getränke"
        RecipeCategory.HEALTHY -> "Gesund"
        RecipeCategory.QUICK_MEALS -> "Schnelle Gerichte"
        RecipeCategory.HIGH_PROTEIN -> "Proteinreich"
        RecipeCategory.GLUTEN_FREE -> "Glutenfrei"
        RecipeCategory.DAIRY_FREE -> "Milchfrei"
        RecipeCategory.KETO -> "Keto"
        RecipeCategory.PALEO -> "Paleo"
        RecipeCategory.MEDITERRANEAN -> "Mediterran"
    }
}