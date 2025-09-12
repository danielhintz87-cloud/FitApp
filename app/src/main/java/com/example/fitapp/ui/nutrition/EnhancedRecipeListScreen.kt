@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import com.example.fitapp.ui.components.ProFeature
import com.example.fitapp.ui.components.ProFeatureGate
import com.example.fitapp.ui.components.ProFeatureManager
import kotlinx.coroutines.launch

/**
 * Enhanced Recipe List Screen with modern Material 3 design
 * Matches the UI mockups provided in the issue
 */
@Composable
fun EnhancedRecipeListScreen(
    onBackPressed: () -> Unit,
    onRecipeClick: (SavedRecipeEntity) -> Unit,
    onCookRecipe: (SavedRecipeEntity) -> Unit,
    onCreateRecipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    // UI State
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedDietType by remember { mutableStateOf("all") }
    var maxPrepTime by remember { mutableStateOf<Int?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("created") } // created, name, prepTime, calories

    // Data
    val allRecipes by db.savedRecipeDao().allRecipesFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteRecipes by db.savedRecipeDao().favoriteRecipesFlow().collectAsStateWithLifecycle(
        initialValue = emptyList(),
    )

    // Filter recipes
    val filteredRecipes =
        remember(allRecipes, searchQuery, selectedCategory, selectedDietType, maxPrepTime, sortBy) {
            var recipes = allRecipes

            // Apply search filter
            if (searchQuery.isNotBlank()) {
                recipes =
                    recipes.filter { recipe ->
                        recipe.title.contains(searchQuery, ignoreCase = true) ||
                            recipe.tags.contains(searchQuery, ignoreCase = true) ||
                            recipe.ingredients.contains(searchQuery, ignoreCase = true)
                    }
            }

            // Apply category filter (based on tags)
            if (selectedCategory != "all") {
                recipes =
                    when (selectedCategory) {
                        "favorites" -> recipes.filter { it.isFavorite }
                        "vegetarian" -> recipes.filter { it.tags.contains("vegetarian", ignoreCase = true) }
                        "high-protein" -> recipes.filter { it.tags.contains("high-protein", ignoreCase = true) }
                        "low-carb" -> recipes.filter { it.tags.contains("low-carb", ignoreCase = true) }
                        "quick" -> recipes.filter { (it.prepTime ?: Int.MAX_VALUE) <= 30 }
                        else -> recipes
                    }
            }

            // Apply prep time filter
            if (maxPrepTime != null) {
                recipes = recipes.filter { (it.prepTime ?: Int.MAX_VALUE) <= maxPrepTime!! }
            }

            // Apply sorting
            when (sortBy) {
                "name" -> recipes.sortedBy { it.title }
                "prepTime" -> recipes.sortedBy { it.prepTime ?: Int.MAX_VALUE }
                "calories" -> recipes.sortedBy { it.calories ?: Int.MAX_VALUE }
                else -> recipes.sortedByDescending { it.createdAt }
            }
        }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar with modern design
        TopAppBar(
            title = {
                Text(
                    "Rezepte",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { showSortSheet = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sortieren")
                }
                IconButton(
                    onClick = {
                        if (ProFeatureManager.isFeatureAvailable(ProFeature.ADVANCED_FILTERS)) {
                            showFilterSheet = true
                        } else {
                            // Show upgrade prompt
                            showFilterSheet = true
                        }
                    },
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
                IconButton(onClick = onCreateRecipe) {
                    Icon(Icons.Default.Add, contentDescription = "Rezept erstellen")
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        )

        // Search Field with modern styling
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rezepte, Zutaten oder Tags suchen...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Leeren")
                        }
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                    ),
            )
        }

        // Filter/Category Pills
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Quick Filter Chips
            item {
                ScrollableRow {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = { selectedCategory = "all" },
                        label = { Text("Alle") },
                    )
                    FilterChip(
                        selected = selectedCategory == "favorites",
                        onClick = { selectedCategory = "favorites" },
                        label = { Text("Favoriten") },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    )
                    FilterChip(
                        selected = selectedCategory == "quick",
                        onClick = { selectedCategory = "quick" },
                        label = { Text("Schnell (≤30 min)") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                    )
                    FilterChip(
                        selected = selectedCategory == "vegetarian",
                        onClick = { selectedCategory = "vegetarian" },
                        label = { Text("Vegetarisch") },
                        leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null) },
                    )
                    FilterChip(
                        selected = selectedCategory == "high-protein",
                        onClick = { selectedCategory = "high-protein" },
                        label = { Text("Proteinreich") },
                    )
                    FilterChip(
                        selected = selectedCategory == "low-carb",
                        onClick = { selectedCategory = "low-carb" },
                        label = { Text("Low Carb") },
                    )
                }
            }

            // Results header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${filteredRecipes.size} Rezepte gefunden",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (selectedCategory != "all" || searchQuery.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                selectedCategory = "all"
                                searchQuery = ""
                                maxPrepTime = null
                            },
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Filter zurücksetzen")
                        }
                    }
                }
            }

            // Recipe List
            if (filteredRecipes.isEmpty()) {
                item {
                    EmptyStateCard(
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onCreateRecipe = onCreateRecipe,
                    )
                }
            } else {
                items(filteredRecipes) { recipe ->
                    EnhancedRecipeCard(
                        recipe = recipe,
                        onRecipeClick = { onRecipeClick(recipe) },
                        onCookClick = { onCookRecipe(recipe) },
                        onFavoriteClick = {
                            scope.launch {
                                db.savedRecipeDao().setFavorite(recipe.id, !recipe.isFavorite)
                            }
                        },
                        onShareClick = {
                            // Handle recipe sharing
                            shareRecipe(context, recipe)
                        },
                    )
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
        ) {
            ProFeatureGate(
                isPro = ProFeatureManager.isFeatureAvailable(ProFeature.ADVANCED_FILTERS),
                featureName = "Advanced Recipe Filters",
                description = ProFeatureManager.getFeatureDescription(ProFeature.ADVANCED_FILTERS),
                onUpgradeClick = {
                    // TODO: Navigate to upgrade screen
                    showFilterSheet = false
                },
            ) {
                FilterBottomSheetContent(
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    selectedDietType = selectedDietType,
                    onDietTypeChange = { selectedDietType = it },
                    maxPrepTime = maxPrepTime,
                    onMaxPrepTimeChange = { maxPrepTime = it },
                    onDismiss = { showFilterSheet = false },
                )
            }
        }
    }

    // Sort Bottom Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
        ) {
            SortBottomSheetContent(
                currentSort = sortBy,
                onSortChange = { sortBy = it },
                onDismiss = { showSortSheet = false },
            )
        }
    }
}

@Composable
private fun ScrollableRow(content: @Composable RowScope.() -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        content = { item { Row(content = content) } },
    )
}

@Composable
private fun EnhancedRecipeCard(
    recipe: SavedRecipeEntity,
    onRecipeClick: () -> Unit,
    onCookClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onRecipeClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (recipe.markdown.isNotBlank() && recipe.markdown.length > 100) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = recipe.markdown.take(100) + "...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (recipe.isFavorite) "Aus Favoriten entfernen" else "Zu Favoriten hinzufügen",
                        tint = if (recipe.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Recipe metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                recipe.calories?.let { calories ->
                    MetadataChip(
                        icon = Icons.Default.LocalFireDepartment,
                        text = "$calories kcal",
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                recipe.prepTime?.let { prepTime ->
                    MetadataChip(
                        icon = Icons.Default.AccessTime,
                        text = "$prepTime min",
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                recipe.servings?.let { servings ->
                    MetadataChip(
                        icon = Icons.Default.Group,
                        text = "$servings Portionen",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Tags
            if (recipe.tags.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = recipe.tags.replace(",", " • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onCookClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Kochen")
                }

                OutlinedButton(
                    onClick = onRecipeClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Details")
                }

                // Share button with PRO gate
                ProFeatureGate(
                    isPro = ProFeatureManager.isFeatureAvailable(ProFeature.RECIPE_SHARING),
                    featureName = "",
                    description = "",
                    onUpgradeClick = { /* Handle upgrade */ },
                ) {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Rezept teilen",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    selectedCategory: String,
    searchQuery: String,
    onCreateRecipe: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text =
                    when {
                        searchQuery.isNotEmpty() -> "Keine Rezepte für \"$searchQuery\" gefunden"
                        selectedCategory == "favorites" -> "Noch keine Favoriten"
                        else -> "Keine Rezepte gefunden"
                    },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Erstellen Sie Ihr erstes Rezept oder passen Sie die Filter an",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onCreateRecipe) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Rezept erstellen")
            }
        }
    }
}

@Composable
private fun FilterBottomSheetContent(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedDietType: String,
    onDietTypeChange: (String) -> Unit,
    maxPrepTime: Int?,
    onMaxPrepTimeChange: (Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Text(
            text = "Filter",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(16.dp))

        // Category section
        Text(
            text = "Kategorie",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == "all",
                    onClick = { onCategoryChange("all") },
                    label = { Text("Alle") },
                )
            }
            item {
                FilterChip(
                    selected = selectedCategory == "favorites",
                    onClick = { onCategoryChange("favorites") },
                    label = { Text("Favoriten") },
                )
            }
            item {
                FilterChip(
                    selected = selectedCategory == "quick",
                    onClick = { onCategoryChange("quick") },
                    label = { Text("Schnell") },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Prep time filter
        Text(
            text = "Zubereitungszeit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = maxPrepTime == null,
                    onClick = { onMaxPrepTimeChange(null) },
                    label = { Text("Alle") },
                )
            }
            item {
                FilterChip(
                    selected = maxPrepTime == 15,
                    onClick = { onMaxPrepTimeChange(15) },
                    label = { Text("≤ 15 min") },
                )
            }
            item {
                FilterChip(
                    selected = maxPrepTime == 30,
                    onClick = { onMaxPrepTimeChange(30) },
                    label = { Text("≤ 30 min") },
                )
            }
            item {
                FilterChip(
                    selected = maxPrepTime == 60,
                    onClick = { onMaxPrepTimeChange(60) },
                    label = { Text("≤ 1 Std") },
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Anwenden")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SortBottomSheetContent(
    currentSort: String,
    onSortChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Text(
            text = "Sortieren nach",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(16.dp))

        val sortOptions =
            listOf(
                "created" to "Neueste zuerst",
                "name" to "Name (A-Z)",
                "prepTime" to "Zubereitungszeit",
                "calories" to "Kalorien",
            )

        sortOptions.forEach { (value, label) ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSortChange(value)
                            onDismiss()
                        }
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = currentSort == value,
                    onClick = {
                        onSortChange(value)
                        onDismiss()
                    },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Share recipe functionality with text and potential export features
 */
private fun shareRecipe(
    context: android.content.Context,
    recipe: SavedRecipeEntity,
) {
    val shareText =
        buildString {
            appendLine("🍳 ${recipe.title}")
            appendLine()

            // Add recipe metadata
            recipe.servings?.let { appendLine("👥 Portionen: $it") }
            recipe.prepTime?.let { appendLine("⏱️ Zubereitungszeit: $it min") }
            recipe.calories?.let { appendLine("🔥 Kalorien: $it kcal") }
            appendLine()

            // Add recipe content
            appendLine("📝 Rezept:")
            appendLine(recipe.markdown)

            appendLine()
            appendLine("📱 Erstellt mit FitApp")
        }

    val shareIntent =
        android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Rezept: ${recipe.title}")
        }

    val chooser = android.content.Intent.createChooser(shareIntent, "Rezept teilen")
    context.startActivity(chooser)
}
