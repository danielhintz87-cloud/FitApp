package com.example.fitapp.ui.components.enhanced

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * Enhanced food search interface with auto-complete and recent searches
 *
 * Features:
 * - Real-time search with debouncing
 * - Auto-complete suggestions
 * - Recent searches history
 * - Beautiful food item cards with images
 * - Loading states and error handling
 */

data class FoodSearchItem(
    val id: String,
    val name: String,
    val brand: String? = null,
    val calories: Int,
    val imageUrl: String? = null,
    val categories: String? = null,
    val servingSize: String? = null,
)

@OptIn(FlowPreview::class, ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun EnhancedFoodSearchInterface(
    onFoodSelected: (FoodSearchItem) -> Unit,
    onBarcodeScan: () -> Unit,
    searchProvider: suspend (String) -> List<FoodSearchItem>,
    recentSearches: List<FoodSearchItem> = emptyList(),
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FoodSearchItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Debounced search
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isLoading = true
            showSuggestions = true

            snapshotFlow { searchQuery }
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.length >= 2) {
                        try {
                            searchResults = searchProvider(query)
                        } catch (e: Exception) {
                            searchResults = emptyList()
                        } finally {
                            isLoading = false
                        }
                    } else {
                        searchResults = emptyList()
                        isLoading = false
                        showSuggestions = false
                    }
                }
        } else {
            searchResults = emptyList()
            isLoading = false
            showSuggestions = false
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search header
        SearchHeader(
            searchQuery = searchQuery,
            onSearchQueryChanged = { searchQuery = it },
            onBarcodeScan = onBarcodeScan,
            onSearchSubmit = {
                keyboardController?.hide()
                showSuggestions = false
            },
            focusRequester = focusRequester,
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search results or recent searches
        AnimatedContent(
            targetState =
                when {
                    searchQuery.isEmpty() -> SearchState.RECENT
                    isLoading -> SearchState.LOADING
                    searchResults.isNotEmpty() -> SearchState.RESULTS
                    else -> SearchState.EMPTY
                },
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "search_content",
        ) { state ->
            when (state) {
                SearchState.RECENT -> {
                    RecentSearchesSection(
                        recentSearches = recentSearches,
                        onFoodSelected = onFoodSelected,
                    )
                }
                SearchState.LOADING -> {
                    LoadingSection()
                }
                SearchState.RESULTS -> {
                    SearchResultsSection(
                        results = searchResults,
                        onFoodSelected = { food ->
                            onFoodSelected(food)
                            searchQuery = ""
                            showSuggestions = false
                        },
                    )
                }
                SearchState.EMPTY -> {
                    EmptyResultsSection(query = searchQuery)
                }
            }
        }
    }
}

enum class SearchState {
    RECENT,
    LOADING,
    RESULTS,
    EMPTY,
}

@Composable
private fun SearchHeader(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onBarcodeScan: () -> Unit,
    onSearchSubmit: () -> Unit,
    focusRequester: FocusRequester,
    isLoading: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier =
                Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
            placeholder = { Text("Lebensmittel suchen...") },
            leadingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Suchen")
                }
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Löschen")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions =
                KeyboardActions(
                    onSearch = { onSearchSubmit() },
                ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Barcode scanner button
        IconButton(
            onClick = onBarcodeScan,
            modifier =
                Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp),
                    ),
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = "Barcode scannen",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<FoodSearchItem>,
    onFoodSelected: (FoodSearchItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (recentSearches.isNotEmpty()) {
            item {
                Text(
                    text = "Zuletzt gesucht",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(recentSearches) { food ->
                FoodItemCard(
                    food = food,
                    onClick = { onFoodSelected(food) },
                )
            }
        } else {
            item {
                EmptyRecentSearches()
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    results: List<FoodSearchItem>,
    onFoodSelected: (FoodSearchItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = "${results.size} Ergebnisse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        items(results) { food ->
            FoodItemCard(
                food = food,
                onClick = { onFoodSelected(food) },
            )
        }
    }
}

@Composable
private fun FoodItemCard(
    food: FoodSearchItem,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Food image
            AsyncImage(
                model = food.imageUrl,
                contentDescription = food.name,
                modifier =
                    Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                fallback = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Food details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (food.brand != null) {
                    Text(
                        text = food.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${food.calories} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (food.servingSize != null) {
                        Text(
                            text = " / ${food.servingSize}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Add button
            IconButton(
                onClick = onClick,
                modifier =
                    Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp),
                        ),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Hinzufügen",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Suche läuft...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyResultsSection(query: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Keine Ergebnisse für \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Versuchen Sie eine andere Suchanfrage oder scannen Sie den Barcode",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyRecentSearches() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Keine letzten Suchen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Beginnen Sie mit der Suche nach Lebensmitteln",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
