package com.example.fitapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.fitapp.services.AutoCompleteSuggestion
import com.example.fitapp.services.SuggestionSource

/**
 * AutoComplete TextField Component
 * Provides intelligent suggestions with category hints and usage learning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<AutoCompleteSuggestion>,
    onSuggestionSelected: (AutoCompleteSuggestion) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Product eingeben",
    placeholder: String = "z.B. Äpfel, Milch, Brot...",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    maxSuggestions: Int = 6,
    showCategoryBadges: Boolean = true,
    showConfidenceIndicator: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Show suggestions when there's input and suggestions available
    val shouldShowSuggestions = value.isNotEmpty() && suggestions.isNotEmpty() && expanded
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                expanded = newValue.isNotEmpty()
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Dropdown with suggestions
        if (shouldShowSuggestions) {
            AutoCompleteSuggestionsDropdown(
                suggestions = suggestions.take(maxSuggestions),
                onSuggestionClick = { suggestion ->
                    onSuggestionSelected(suggestion)
                    expanded = false
                },
                onDismiss = { expanded = false },
                showCategoryBadges = showCategoryBadges,
                showConfidenceIndicator = showConfidenceIndicator
            )
        }
    }
}

/**
 * Suggestions Dropdown Component
 */
@Composable
private fun AutoCompleteSuggestionsDropdown(
    suggestions: List<AutoCompleteSuggestion>,
    onSuggestionClick: (AutoCompleteSuggestion) -> Unit,
    onDismiss: () -> Unit,
    showCategoryBadges: Boolean,
    showConfidenceIndicator: Boolean
) {
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .shadow(8.dp, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(suggestions) { suggestion ->
                    AutoCompleteSuggestionItem(
                        suggestion = suggestion,
                        onClick = { onSuggestionClick(suggestion) },
                        showCategoryBadge = showCategoryBadges,
                        showConfidenceIndicator = showConfidenceIndicator
                    )
                    
                    if (suggestion != suggestions.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Suggestion Item
 */
@Composable
private fun AutoCompleteSuggestionItem(
    suggestion: AutoCompleteSuggestion,
    onClick: () -> Unit,
    showCategoryBadge: Boolean,
    showConfidenceIndicator: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Source icon
        Icon(
            imageVector = getSuggestionSourceIcon(suggestion.source),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = getSuggestionSourceColor(suggestion.source)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Main content
        Column(modifier = Modifier.weight(1f)) {
            // Product name
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Category and metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showCategoryBadge) {
                    CategoryBadge(
                        category = suggestion.category,
                        modifier = Modifier
                    )
                }
                
                if (suggestion.frequency > 0) {
                    FrequencyIndicator(frequency = suggestion.frequency)
                }
                
                if (showConfidenceIndicator) {
                    ConfidenceIndicator(confidence = suggestion.confidence)
                }
            }
        }
        
        // Quick add icon
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Hinzufügen",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Category Badge Component
 */
@Composable
private fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val emoji = getCategoryEmoji(category)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = "$emoji $category",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * Frequency Indicator
 */
@Composable
private fun FrequencyIndicator(frequency: Int) {
    if (frequency > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = frequency.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Confidence Indicator (for debugging/advanced users)
 */
@Composable
private fun ConfidenceIndicator(confidence: Float) {
    val percentage = (confidence * 100).toInt()
    Text(
        text = "$percentage%",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Get icon for suggestion source
 */
private fun getSuggestionSourceIcon(source: SuggestionSource) = when (source) {
    SuggestionSource.EXACT_MATCH -> Icons.Filled.Search
    SuggestionSource.FUZZY_MATCH -> Icons.Filled.FindInPage
    SuggestionSource.RECENT_ITEMS -> Icons.Filled.History
    SuggestionSource.FREQUENT_ITEMS -> Icons.Filled.Favorite
    SuggestionSource.CATEGORY_HINT -> Icons.Filled.Category
}

/**
 * Get color for suggestion source
 */
@Composable
private fun getSuggestionSourceColor(source: SuggestionSource) = when (source) {
    SuggestionSource.EXACT_MATCH -> MaterialTheme.colorScheme.primary
    SuggestionSource.FUZZY_MATCH -> MaterialTheme.colorScheme.secondary
    SuggestionSource.RECENT_ITEMS -> MaterialTheme.colorScheme.tertiary
    SuggestionSource.FREQUENT_ITEMS -> MaterialTheme.colorScheme.error
    SuggestionSource.CATEGORY_HINT -> MaterialTheme.colorScheme.outline
}

/**
 * Get emoji for category
 */
private fun getCategoryEmoji(category: String): String = when (category) {
    "Obst & Gemüse" -> "🥬"
    "Fleisch & Wurst" -> "🥩"
    "Fisch & Meeresfrüchte" -> "🐟"
    "Milchprodukte" -> "🥛"
    "Eier" -> "🥚"
    "Käse" -> "🧀"
    "Getreide & Müsli" -> "🌾"
    "Nudeln & Reis" -> "🍝"
    "Bäckerei" -> "🍞"
    "Konserven" -> "🥫"
    "Gewürze & Kräuter" -> "🌿"
    "Frische Kräuter" -> "🌿"
    "Öl & Essig" -> "🫒"
    "Soßen & Dressings" -> "🥗"
    "Süßwaren & Snacks" -> "🍫"
    "Backzutaten" -> "🧁"
    "Nüsse & Trockenfrüchte" -> "🥜"
    "Tiefkühl" -> "🧊"
    "Eis" -> "🍦"
    "Wasser & Erfrischungsgetränke" -> "💧"
    "Säfte" -> "🧃"
    "Kaffee & Tee" -> "☕"
    "Alkoholische Getränke" -> "🍷"
    "Haushalt & Reinigung" -> "🧽"
    "Körperpflege" -> "🧴"
    "Baby & Kind" -> "👶"
    else -> "📦"
}

/**
 * Quick Add AutoComplete for simple usage
 */
@Composable
fun QuickAddAutoComplete(
    onItemSelected: (String) -> Unit,
    suggestions: List<AutoCompleteSuggestion>,
    modifier: Modifier = Modifier,
    placeholder: String = "Produkt eingeben..."
) {
    var value by remember { mutableStateOf("") }
    
    AutoCompleteTextField(
        value = value,
        onValueChange = { value = it },
        suggestions = suggestions,
        onSuggestionSelected = { suggestion ->
            onItemSelected(suggestion.text)
            value = ""
        },
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    )
}
