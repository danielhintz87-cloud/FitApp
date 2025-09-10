@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitapp.ui.nutrition

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.domain.entities.*
import com.example.fitapp.services.YazioRecipeManager
import kotlinx.coroutines.launch
import com.example.fitapp.ui.util.applyContentPadding

/**
 * Comprehensive Recipe Editor implementing YAZIO specification
 * - Minimum 2 ingredients requirement
 * - Step-by-step instruction builder
 * - Detailed nutrition calculation
 * - Category and difficulty selection
 */
@Composable
fun YazioRecipeEditorScreen(
    recipeId: String? = null, // null for new recipe
    onBackPressed: () -> Unit,
    onRecipeSaved: (Recipe) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val recipeManager = remember { YazioRecipeManager(db) }
    val scope = rememberCoroutineScope()

    // Recipe builder state
    var recipeBuilder by remember { mutableStateOf(RecipeBuilder()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showValidationErrors by remember { mutableStateOf(false) }

    // Load existing recipe if editing
    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            isLoading = true
            try {
                val existingRecipe = recipeManager.getRecipe(recipeId)
                if (existingRecipe != null) {
                    recipeBuilder = RecipeBuilder(
                        title = existingRecipe.title,
                        description = existingRecipe.description,
                        imageUrl = existingRecipe.imageUrl,
                        prepTime = existingRecipe.prepTime,
                        cookTime = existingRecipe.cookTime,
                        servings = existingRecipe.servings,
                        difficulty = existingRecipe.difficulty,
                        categories = existingRecipe.categories,
                        ingredients = existingRecipe.ingredients,
                        steps = existingRecipe.steps,
                        tags = existingRecipe.tags
                    )
                }
            } catch (e: Exception) {
                error = "Fehler beim Laden des Rezepts: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Save recipe function
    fun saveRecipe() {
        scope.launch {
            showValidationErrors = true
            
            if (!recipeBuilder.isValid()) {
                error = "Bitte füllen Sie alle Pflichtfelder aus. Mindestens 2 Zutaten und Zubereitungsschritte sind erforderlich."
                return@launch
            }

            isLoading = true
            try {
                val recipe = if (recipeId != null) {
                    val existingRecipe = recipeManager.getRecipe(recipeId)
                    val updatedRecipe = recipeBuilder.build().copy(id = recipeId)
                    recipeManager.updateRecipe(updatedRecipe)
                    updatedRecipe
                } else {
                    recipeManager.createRecipe(recipeBuilder)
                }
                onRecipeSaved(recipe)
            } catch (e: Exception) {
                error = "Fehler beim Speichern: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier.fillMaxSize().applyContentPadding(contentPadding)) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(if (recipeId != null) "Rezept bearbeiten" else "Neues Rezept") 
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                TextButton(
                    onClick = { saveRecipe() },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Speichern")
                    }
                }
            }
        )

        if (error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Information Section
            item {
                YazioFormSection(
                    title = "Grundinformationen",
                    icon = Icons.Filled.Info,
                    isRequired = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = recipeBuilder.title,
                            onValueChange = { recipeBuilder = recipeBuilder.copy(title = it) },
                            label = { Text("Rezeptname *") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = showValidationErrors && recipeBuilder.title.isBlank(),
                            supportingText = if (showValidationErrors && recipeBuilder.title.isBlank()) {
                                { Text("Rezeptname ist erforderlich") }
                            } else null
                        )
                        
                        OutlinedTextField(
                            value = recipeBuilder.description,
                            onValueChange = { recipeBuilder = recipeBuilder.copy(description = it) },
                            label = { Text("Beschreibung") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        
                        OutlinedTextField(
                            value = recipeBuilder.imageUrl ?: "",
                            onValueChange = { recipeBuilder = recipeBuilder.copy(imageUrl = it.ifBlank { null }) },
                            label = { Text("Bild-URL (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Recipe Details Section
            item {
                YazioFormSection(
                    title = "Rezeptdetails",
                    icon = Icons.Filled.Schedule
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = recipeBuilder.prepTime?.toString() ?: "",
                            onValueChange = { value ->
                                recipeBuilder = recipeBuilder.copy(
                                    prepTime = value.toIntOrNull()
                                )
                            },
                            label = { Text("Vorbereitungszeit (Min.)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        OutlinedTextField(
                            value = recipeBuilder.cookTime?.toString() ?: "",
                            onValueChange = { value ->
                                recipeBuilder = recipeBuilder.copy(
                                    cookTime = value.toIntOrNull()
                                )
                            },
                            label = { Text("Kochzeit (Min.)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        OutlinedTextField(
                            value = recipeBuilder.servings?.toString() ?: "",
                            onValueChange = { value ->
                                recipeBuilder = recipeBuilder.copy(
                                    servings = value.toIntOrNull()
                                )
                            },
                            label = { Text("Portionen") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            // Difficulty Selection
            item {
                YazioFormSection(
                    title = "Schwierigkeitsgrad",
                    icon = Icons.Filled.Speed
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RecipeDifficulty.entries.forEach { difficulty ->
                            FilterChip(
                                onClick = { 
                                    recipeBuilder = recipeBuilder.copy(difficulty = difficulty)
                                },
                                label = { 
                                    Text(
                                        when (difficulty) {
                                            RecipeDifficulty.EASY -> "Einfach"
                                            RecipeDifficulty.MEDIUM -> "Mittel"
                                            RecipeDifficulty.HARD -> "Schwer"
                                        }
                                    ) 
                                },
                                selected = recipeBuilder.difficulty == difficulty,
                                leadingIcon = if (recipeBuilder.difficulty == difficulty) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Categories Selection
            item {
                YazioFormSection(
                    title = "Kategorien",
                    icon = Icons.Filled.Category
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(RecipeCategory.entries) { category ->
                            FilterChip(
                                onClick = { 
                                    val currentCategories = recipeBuilder.categories.toMutableList()
                                    if (category in currentCategories) {
                                        currentCategories.remove(category)
                                    } else {
                                        currentCategories.add(category)
                                    }
                                    recipeBuilder = recipeBuilder.copy(categories = currentCategories)
                                },
                                label = { 
                                    Text(
                                        formatCategoryName(category),
                                        style = MaterialTheme.typography.bodySmall
                                    ) 
                                },
                                selected = category in recipeBuilder.categories,
                                leadingIcon = if (category in recipeBuilder.categories) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Ingredients Section (YAZIO requirement: minimum 2)
            item {
                YazioFormSection(
                    title = "Zutaten (mindestens 2 erforderlich)",
                    icon = Icons.Filled.Restaurant,
                    isRequired = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recipeBuilder.ingredients.forEachIndexed { index, ingredient ->
                            IngredientEditor(
                                ingredient = ingredient,
                                onIngredientChange = { updatedIngredient ->
                                    val ingredients = recipeBuilder.ingredients.toMutableList()
                                    ingredients[index] = updatedIngredient
                                    recipeBuilder = recipeBuilder.copy(ingredients = ingredients)
                                },
                                onRemove = {
                                    val ingredients = recipeBuilder.ingredients.toMutableList()
                                    ingredients.removeAt(index)
                                    recipeBuilder = recipeBuilder.copy(ingredients = ingredients)
                                },
                                canRemove = recipeBuilder.ingredients.size > 1
                            )
                        }
                        
                        if (showValidationErrors && recipeBuilder.ingredients.size < 2) {
                            Text(
                                "Mindestens 2 Zutaten sind erforderlich",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        OutlinedButton(
                            onClick = {
                                val ingredients = recipeBuilder.ingredients.toMutableList()
                                ingredients.add(
                                    RecipeIngredient(
                                        name = "",
                                        amount = 0f,
                                        unit = "g",
                                        order = ingredients.size
                                    )
                                )
                                recipeBuilder = recipeBuilder.copy(ingredients = ingredients)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Zutat hinzufügen")
                        }
                    }
                }
            }

            // Cooking Steps Section
            item {
                YazioFormSection(
                    title = "Zubereitungsschritte",
                    icon = Icons.Filled.List,
                    isRequired = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recipeBuilder.steps.forEachIndexed { index, step ->
                            CookingStepEditor(
                                step = step,
                                stepNumber = index + 1,
                                onStepChange = { updatedStep ->
                                    val steps = recipeBuilder.steps.toMutableList()
                                    steps[index] = updatedStep
                                    recipeBuilder = recipeBuilder.copy(steps = steps)
                                },
                                onRemove = {
                                    val steps = recipeBuilder.steps.toMutableList()
                                    steps.removeAt(index)
                                    recipeBuilder = recipeBuilder.copy(steps = steps)
                                },
                                canRemove = recipeBuilder.steps.size > 1
                            )
                        }
                        
                        if (showValidationErrors && recipeBuilder.steps.isEmpty()) {
                            Text(
                                "Mindestens ein Zubereitungsschritt ist erforderlich",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        OutlinedButton(
                            onClick = {
                                val steps = recipeBuilder.steps.toMutableList()
                                steps.add(
                                    RecipeStep(
                                        instruction = "",
                                        order = steps.size
                                    )
                                )
                                recipeBuilder = recipeBuilder.copy(steps = steps)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Schritt hinzufügen")
                        }
                    }
                }
            }

            // Tags Section
            item {
                YazioFormSection(
                    title = "Tags (optional)",
                    icon = Icons.Filled.Tag
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val commonTags = listOf(
                            "schnell", "einfach", "gesund", "vegetarisch", "vegan", 
                            "glutenfrei", "low-carb", "high-protein", "budget-freundlich"
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(commonTags) { tag ->
                                FilterChip(
                                    onClick = { 
                                        val currentTags = recipeBuilder.tags.toMutableList()
                                        if (tag in currentTags) {
                                            currentTags.remove(tag)
                                        } else {
                                            currentTags.add(tag)
                                        }
                                        recipeBuilder = recipeBuilder.copy(tags = currentTags)
                                    },
                                    label = { Text(tag) },
                                    selected = tag in recipeBuilder.tags
                                )
                            }
                        }
                    }
                }
            }

            // Validation Summary
            if (showValidationErrors) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (recipeBuilder.isValid()) 
                                MaterialTheme.colorScheme.primaryContainer
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (recipeBuilder.isValid()) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = if (recipeBuilder.isValid()) 
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else 
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (recipeBuilder.isValid()) "Rezept ist vollständig" else "Rezept noch nicht vollständig",
                                    fontWeight = FontWeight.Medium,
                                    color = if (recipeBuilder.isValid()) 
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else 
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            
                            if (!recipeBuilder.isValid()) {
                                val issues = mutableListOf<String>()
                                if (recipeBuilder.title.isBlank()) issues.add("• Rezeptname fehlt")
                                if (recipeBuilder.ingredients.size < 2) issues.add("• Mindestens 2 Zutaten erforderlich")
                                if (recipeBuilder.steps.isEmpty()) issues.add("• Zubereitungsschritte fehlen")
                                
                                issues.forEach { issue ->
                                    Text(
                                        issue,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YazioFormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isRequired: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isRequired) "$title *" else title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isRequired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun IngredientEditor(
    ingredient: RecipeIngredient,
    onIngredientChange: (RecipeIngredient) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ingredient.name,
                    onValueChange = { onIngredientChange(ingredient.copy(name = it)) },
                    label = { Text("Zutat") },
                    modifier = Modifier.weight(2f)
                )
                
                OutlinedTextField(
                    value = if (ingredient.amount == 0f) "" else ingredient.amount.toString(),
                    onValueChange = { value ->
                        onIngredientChange(ingredient.copy(amount = value.toFloatOrNull() ?: 0f))
                    },
                    label = { Text("Menge") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Unit dropdown
                var expandedUnit by remember { mutableStateOf(false) }
                val units = listOf("g", "kg", "ml", "l", "TL", "EL", "Tasse", "Stück", "Prise")
                
                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = !expandedUnit },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = ingredient.unit,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Einheit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    onIngredientChange(ingredient.copy(unit = unit))
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }
                
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Filled.Delete, contentDescription = "Zutat entfernen")
                    }
                }
            }
            
            // Optional fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ingredient.preparationNote ?: "",
                    onValueChange = { onIngredientChange(ingredient.copy(preparationNote = it.ifBlank { null })) },
                    label = { Text("Vorbereitung (z.B. gewürfelt)") },
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = ingredient.isOptional,
                        onCheckedChange = { onIngredientChange(ingredient.copy(isOptional = it)) }
                    )
                    Text("Optional", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CookingStepEditor(
    step: RecipeStep,
    stepNumber: Int,
    onStepChange: (RecipeStep) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = stepNumber.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                OutlinedTextField(
                    value = step.instruction,
                    onValueChange = { onStepChange(step.copy(instruction = it)) },
                    label = { Text("Anweisung") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4
                )
                
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Filled.Delete, contentDescription = "Schritt entfernen")
                    }
                }
            }
            
            // Optional step details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = step.estimatedTimeMinutes?.toString() ?: "",
                    onValueChange = { value ->
                        onStepChange(step.copy(estimatedTimeMinutes = value.toIntOrNull()))
                    },
                    label = { Text("Zeit (Min.)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = step.temperature ?: "",
                    onValueChange = { onStepChange(step.copy(temperature = it.ifBlank { null })) },
                    label = { Text("Temperatur") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = step.timerName ?: "",
                    onValueChange = { onStepChange(step.copy(timerName = it.ifBlank { null })) },
                    label = { Text("Timer Name") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
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