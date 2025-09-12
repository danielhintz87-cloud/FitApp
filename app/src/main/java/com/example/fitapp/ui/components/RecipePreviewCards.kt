package com.example.fitapp.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fitapp.data.db.SavedRecipeEntity

/**
 * Modern Recipe Preview Card System
 * Instagram-style recipe cards with beautiful previews and expandable details
 */
@Composable
fun RecipePreviewGrid(
    recipes: List<SavedRecipeEntity>,
    onRecipeClick: (SavedRecipeEntity) -> Unit,
    onCookClick: (SavedRecipeEntity) -> Unit,
    onFavoriteClick: (SavedRecipeEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(recipes) { recipe ->
            RecipePreviewCard(
                recipe = recipe,
                onRecipeClick = { onRecipeClick(recipe) },
                onCookClick = { onCookClick(recipe) },
                onFavoriteClick = { onFavoriteClick(recipe) },
            )
        }
    }
}

/**
 * Individual Recipe Preview Card
 * Beautiful card with image, quick stats, and expandable preview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipePreviewCard(
    recipe: SavedRecipeEntity,
    onRecipeClick: () -> Unit,
    onCookClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column {
            // Recipe Image with Overlay
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
            ) {
                // Background Image
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(recipe.imageUrl ?: getDefaultRecipeImage(recipe))
                            .crossfade(true)
                            .build(),
                    contentDescription = recipe.title,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                )

                // Gradient Overlay for Text Readability
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f),
                                        ),
                                    startY = 100f,
                                ),
                            ),
                )

                // Favorite Button (Top Right)
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (recipe.isFavorite) "Aus Favoriten entfernen" else "Zu Favoriten hinzufügen",
                        tint = if (recipe.isFavorite) Color.Red else Color.White,
                    )
                }

                // Recipe Title and Quick Stats (Bottom Overlay)
                Column(
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Quick Stats Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        recipe.prepTime?.let { prepTime ->
                            QuickStatChip(
                                icon = Icons.Filled.AccessTime,
                                text = "$prepTime min",
                                backgroundColor = Color.White.copy(alpha = 0.2f),
                            )
                        }

                        recipe.calories?.let { calories ->
                            QuickStatChip(
                                icon = Icons.Filled.LocalFireDepartment,
                                text = "$calories kcal",
                                backgroundColor = Color.White.copy(alpha = 0.2f),
                            )
                        }

                        recipe.servings?.let { servings ->
                            QuickStatChip(
                                icon = Icons.Filled.Group,
                                text = "$servings Portionen",
                                backgroundColor = Color.White.copy(alpha = 0.2f),
                            )
                        }
                    }
                }

                // Expand/Collapse Indicator
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Weniger anzeigen" else "Mehr anzeigen",
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(50),
                            )
                            .padding(4.dp),
                    tint = Color.White,
                )
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically() + expandVertically() + fadeIn(),
                exit = slideOutVertically() + shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    // Recipe Preview Text
                    if (recipe.markdown.isNotBlank()) {
                        Text(
                            text = getRecipePreview(recipe.markdown),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Tags
                    if (recipe.tags.isNotBlank()) {
                        TagsRow(tags = recipe.tags.split(","))
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            onClick = onCookClick,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Kochen")
                        }

                        OutlinedButton(
                            onClick = onRecipeClick,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Details")
                        }
                    }
                }
            }

            // Collapsed State Footer
            if (!isExpanded) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Difficulty & Type Indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        recipe.difficulty?.let { difficulty ->
                            DifficultyChip(difficulty = difficulty)
                        }

                        // Recipe Type from tags
                        getRecipeType(recipe.tags)?.let { type ->
                            TypeChip(type = type)
                        }
                    }

                    // Quick Cook Button
                    IconButton(onClick = onCookClick) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Kochen starten",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick Stat Chip for recipe metadata
 */
@Composable
private fun QuickStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.White,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

/**
 * Tags Row with colored chips
 */
@Composable
private fun TagsRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.take(3).forEach { tag ->
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = tag.trim(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors =
                    AssistChipDefaults.assistChipColors(
                        containerColor = getTagColor(tag.trim()),
                    ),
            )
        }

        if (tags.size > 3) {
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = "+${tags.size - 3}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}

/**
 * Difficulty indicator chip
 */
@Composable
private fun DifficultyChip(difficulty: String) {
    val (color, emoji) =
        when (difficulty.lowercase()) {
            "easy", "einfach" -> Pair(Color(0xFF4CAF50), "😊")
            "medium", "mittel" -> Pair(Color(0xFFFF9800), "🤔")
            "hard", "schwer" -> Pair(Color(0xFFF44336), "😰")
            else -> Pair(MaterialTheme.colorScheme.outline, "🍳")
        }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = difficulty,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
        }
    }
}

/**
 * Recipe type chip
 */
@Composable
private fun TypeChip(type: String) {
    val (emoji, displayName) = getTypeInfo(type)

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// Helper functions
private fun getRecipePreview(markdown: String): String {
    // Extract first meaningful paragraph as preview
    val lines = markdown.split("\n").filter { it.trim().isNotEmpty() }
    val preview =
        lines.find { line ->
            !line.startsWith("#") && !line.startsWith("-") && !line.startsWith("*") && line.length > 50
        } ?: lines.firstOrNull() ?: ""

    return if (preview.length > 150) {
        preview.take(150) + "..."
    } else {
        preview
    }
}

private fun getDefaultRecipeImage(recipe: SavedRecipeEntity): String {
    // Generate a default image URL based on recipe type or first ingredient
    return "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&h=300&fit=crop"
}

@Composable
private fun getTagColor(tag: String): Color {
    return when (tag.lowercase()) {
        "vegetarian", "vegetarisch" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        "vegan" -> Color(0xFF8BC34A).copy(alpha = 0.2f)
        "high-protein", "proteinreich" -> Color(0xFF2196F3).copy(alpha = 0.2f)
        "low-carb" -> Color(0xFF9C27B0).copy(alpha = 0.2f)
        "quick", "schnell" -> Color(0xFFFF5722).copy(alpha = 0.2f)
        "dessert", "nachspeise" -> Color(0xFFE91E63).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
}

private fun getRecipeType(tags: String): String? {
    val tagList = tags.lowercase().split(",").map { it.trim() }
    return when {
        tagList.any { it.contains("dessert") || it.contains("nachspeise") } -> "dessert"
        tagList.any { it.contains("hauptgericht") || it.contains("main") } -> "hauptgericht"
        tagList.any { it.contains("vorspeise") || it.contains("appetizer") } -> "vorspeise"
        tagList.any { it.contains("suppe") || it.contains("soup") } -> "suppe"
        tagList.any { it.contains("salat") || it.contains("salad") } -> "salat"
        else -> null
    }
}

private fun getTypeInfo(type: String): Pair<String, String> {
    return when (type.lowercase()) {
        "dessert" -> Pair("🍰", "Dessert")
        "hauptgericht" -> Pair("🍽️", "Hauptgang")
        "vorspeise" -> Pair("🥗", "Vorspeise")
        "suppe" -> Pair("🍲", "Suppe")
        "salat" -> Pair("🥬", "Salat")
        else -> Pair("🍳", type)
    }
}
