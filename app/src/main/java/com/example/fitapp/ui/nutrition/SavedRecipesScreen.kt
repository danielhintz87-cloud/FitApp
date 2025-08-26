package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipesScreen(
    onBackPressed: () -> Unit,
    onRecipeClick: (SavedRecipeEntity) -> Unit,
    onCookRecipe: (SavedRecipeEntity) -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    val recipes by when {
        searchQuery.isNotBlank() -> db.savedRecipeDao().searchRecipesFlow(searchQuery).collectAsState(initial = emptyList())
        selectedFilter == "favorites" -> db.savedRecipeDao().favoriteRecipesFlow().collectAsState(initial = emptyList())
        selectedFilter == "vegetarian" -> db.savedRecipeDao().recipesByTagFlow("vegetarian").collectAsState(initial = emptyList())
        selectedFilter == "high-protein" -> db.savedRecipeDao().recipesByTagFlow("high-protein").collectAsState(initial = emptyList())
        selectedFilter == "low-carb" -> db.savedRecipeDao().recipesByTagFlow("low-carb").collectAsState(initial = emptyList())
        else -> db.savedRecipeDao().allRecipesFlow().collectAsState(initial = emptyList())
    }

    Column(Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Gespeicherte Rezepte") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { showFilterMenu = true }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                }
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Alle Rezepte") },
                        onClick = { selectedFilter = "all"; showFilterMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Favoriten") },
                        onClick = { selectedFilter = "favorites"; showFilterMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Vegetarisch") },
                        onClick = { selectedFilter = "vegetarian"; showFilterMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Proteinreich") },
                        onClick = { selectedFilter = "high-protein"; showFilterMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Low Carb") },
                        onClick = { selectedFilter = "low-carb"; showFilterMenu = false }
                    )
                }
            }
        )
        
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Rezepte suchen...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        
        // Current Filter Indicator
        if (selectedFilter != "all") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Filter: ${getFilterDisplayName(selectedFilter)}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { selectedFilter = "all" }) {
                        Text("Zurücksetzen", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        
        // Recipe List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onFavoriteClick = { 
                        scope.launch {
                            db.savedRecipeDao().setFavorite(recipe.id, !recipe.isFavorite)
                        }
                    },
                    onCookClick = { onCookRecipe(recipe) },
                    onRecipeClick = { onRecipeClick(recipe) }
                )
            }
            
            if (recipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Keine Rezepte gefunden",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "Erstellen Sie Rezepte im Nutrition-Bereich und speichern Sie sie hier",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: SavedRecipeEntity,
    onFavoriteClick: () -> Unit,
    onCookClick: () -> Unit,
    onRecipeClick: () -> Unit
) {
    ElevatedCard(
        onClick = onRecipeClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorit",
                        tint = if (recipe.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                recipe.calories?.let {
                    Text("$it kcal", style = MaterialTheme.typography.bodyMedium)
                }
                recipe.prepTime?.let {
                    Text("$it min", style = MaterialTheme.typography.bodyMedium)
                }
                recipe.servings?.let {
                    Text("$it Portionen", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            if (recipe.tags.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = recipe.tags.replace(",", " • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCookClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Kochen")
                }
                OutlinedButton(
                    onClick = onRecipeClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Details")
                }
            }
        }
    }
}

private fun getFilterDisplayName(filter: String): String {
    return when (filter) {
        "favorites" -> "Favoriten"
        "vegetarian" -> "Vegetarisch"
        "high-protein" -> "Proteinreich"
        "low-carb" -> "Low Carb"
        else -> "Alle"
    }
}