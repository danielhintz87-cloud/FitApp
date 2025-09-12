@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SavedRecipeEntity
import kotlinx.coroutines.launch

/**
 * Recipe Edit Screen for creating and editing recipes
 * Supports all the features mentioned in the issue requirements
 */
@Composable
fun RecipeEditScreen(
    recipe: SavedRecipeEntity? = null, // null for new recipe
    onBackPressed: () -> Unit,
    onSaveRecipe: (SavedRecipeEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    // Form state
    var title by remember { mutableStateOf(recipe?.title ?: "") }
    var imageUrl by remember { mutableStateOf(recipe?.imageUrl ?: "") }
    var category by remember { mutableStateOf(getCategory(recipe?.tags ?: "")) }
    var dietType by remember { mutableStateOf(getDietType(recipe?.tags ?: "")) }
    var prepTime by remember { mutableStateOf((recipe?.prepTime ?: 30).toString()) }
    var servings by remember { mutableStateOf((recipe?.servings ?: 2).toString()) }
    var difficulty by remember { mutableStateOf(recipe?.difficulty ?: "medium") }

    // Ingredients state
    var ingredients by remember {
        mutableStateOf(parseIngredientsFromRecipe(recipe?.ingredients ?: "").toMutableList())
    }

    // Instructions state
    var instructions by remember {
        mutableStateOf(parseInstructionsFromMarkdown(recipe?.markdown ?: "").toMutableList())
    }

    // Validation state
    var showValidationError by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    // UI state
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDietTypeDialog by remember { mutableStateOf(false) }
    var showDifficultyDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (recipe == null) "Neues Rezept" else "Rezept bearbeiten",
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
                TextButton(
                    onClick = {
                        // Validate form
                        when {
                            title.isBlank() -> {
                                validationMessage = "Rezeptname ist erforderlich"
                                showValidationError = true
                            }
                            ingredients.all { it.isBlank() } -> {
                                validationMessage = "Mindestens eine Zutat ist erforderlich"
                                showValidationError = true
                            }
                            instructions.all { it.isBlank() } -> {
                                validationMessage = "Anleitung ist erforderlich"
                                showValidationError = true
                            }
                            else -> {
                                // Save recipe
                                scope.launch {
                                    val savedRecipe =
                                        SavedRecipeEntity(
                                            id = recipe?.id ?: java.util.UUID.randomUUID().toString(),
                                            title = title,
                                            markdown = buildMarkdown(ingredients, instructions),
                                            calories = null, // Will be calculated later
                                            imageUrl = imageUrl.ifBlank { null },
                                            ingredients = buildIngredientsJson(ingredients),
                                            tags = buildTags(category, dietType),
                                            prepTime = prepTime.toIntOrNull(),
                                            difficulty = difficulty,
                                            servings = servings.toIntOrNull(),
                                            isFavorite = recipe?.isFavorite ?: false,
                                            createdAt = recipe?.createdAt ?: System.currentTimeMillis() / 1000,
                                        )
                                    onSaveRecipe(savedRecipe)
                                }
                            }
                        }
                    },
                ) {
                    Text("Speichern")
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            // Basic Information Section
            item {
                BasicInfoSection(
                    title = title,
                    onTitleChange = { title = it },
                    imageUrl = imageUrl,
                    onImageUrlChange = { imageUrl = it },
                )
            }

            // Recipe Metadata Section
            item {
                MetadataSection(
                    category = category,
                    onCategoryClick = { showCategoryDialog = true },
                    dietType = dietType,
                    onDietTypeClick = { showDietTypeDialog = true },
                    prepTime = prepTime,
                    onPrepTimeChange = { prepTime = it },
                    servings = servings,
                    onServingsChange = { servings = it },
                    difficulty = difficulty,
                    onDifficultyClick = { showDifficultyDialog = true },
                )
            }

            // Ingredients Section
            item {
                IngredientsSection(
                    ingredients = ingredients,
                    onIngredientChange = { index, value ->
                        ingredients = ingredients.toMutableList().apply { this[index] = value }
                    },
                    onAddIngredient = {
                        ingredients = ingredients.toMutableList().apply { add("") }
                    },
                    onRemoveIngredient = { index ->
                        ingredients = ingredients.toMutableList().apply { removeAt(index) }
                    },
                )
            }

            // Instructions Section
            item {
                InstructionsSection(
                    instructions = instructions,
                    onInstructionChange = { index, value ->
                        instructions = instructions.toMutableList().apply { this[index] = value }
                    },
                    onAddInstruction = {
                        instructions = instructions.toMutableList().apply { add("") }
                    },
                    onRemoveInstruction = { index ->
                        instructions = instructions.toMutableList().apply { removeAt(index) }
                    },
                )
            }
        }
    }

    // Validation Error Dialog
    if (showValidationError) {
        AlertDialog(
            onDismissRequest = { showValidationError = false },
            title = { Text("Fehler") },
            text = { Text(validationMessage) },
            confirmButton = {
                TextButton(onClick = { showValidationError = false }) {
                    Text("OK")
                }
            },
        )
    }

    // Category Selection Dialog
    if (showCategoryDialog) {
        SelectionDialog(
            title = "Kategorie auswählen",
            options =
                listOf(
                    "Hauptgericht",
                    "Vorspeise",
                    "Dessert",
                    "Snack",
                    "Getränk",
                    "Frühstück",
                    "Beilage",
                ),
            selectedOption = category,
            onSelectionChange = { category = it },
            onDismiss = { showCategoryDialog = false },
        )
    }

    // Diet Type Selection Dialog
    if (showDietTypeDialog) {
        SelectionDialog(
            title = "Ernährungsform auswählen",
            options =
                listOf(
                    "Alles",
                    "Vegetarisch",
                    "Vegan",
                    "Low-Carb",
                    "High-Protein",
                    "Glutenfrei",
                    "Laktosefrei",
                ),
            selectedOption = dietType,
            onSelectionChange = { dietType = it },
            onDismiss = { showDietTypeDialog = false },
        )
    }

    // Difficulty Selection Dialog
    if (showDifficultyDialog) {
        SelectionDialog(
            title = "Schwierigkeit auswählen",
            options = listOf("Einfach", "Mittel", "Schwer"),
            selectedOption = difficulty,
            onSelectionChange = { difficulty = it },
            onDismiss = { showDifficultyDialog = false },
        )
    }
}

@Composable
private fun BasicInfoSection(
    title: String,
    onTitleChange: (String) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Grundinformationen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Rezeptname*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = onImageUrlChange,
                label = { Text("Bild URL (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun MetadataSection(
    category: String,
    onCategoryClick: () -> Unit,
    dietType: String,
    onDietTypeClick: () -> Unit,
    prepTime: String,
    onPrepTimeChange: (String) -> Unit,
    servings: String,
    onServingsChange: (String) -> Unit,
    difficulty: String,
    onDifficultyClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Rezeptdetails",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            // Category and Diet Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onCategoryClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(category.ifBlank { "Kategorie" })
                }

                OutlinedButton(
                    onClick = onDietTypeClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(dietType.ifBlank { "Ernährungsform" })
                }
            }

            // Prep Time and Servings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = prepTime,
                    onValueChange = onPrepTimeChange,
                    label = { Text("Zubereitungszeit (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                )

                OutlinedTextField(
                    value = servings,
                    onValueChange = onServingsChange,
                    label = { Text("Portionen") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                )
            }

            // Difficulty
            OutlinedButton(
                onClick = onDifficultyClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Schwierigkeit: ${difficulty.ifBlank { "Auswählen" }}")
            }
        }
    }
}

@Composable
private fun IngredientsSection(
    ingredients: List<String>,
    onIngredientChange: (Int, String) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Zutaten*",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                IconButton(onClick = onAddIngredient) {
                    Icon(Icons.Default.Add, contentDescription = "Zutat hinzufügen")
                }
            }

            ingredients.forEachIndexed { index, ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = ingredient,
                        onValueChange = { onIngredientChange(index, it) },
                        label = { Text("Zutat ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("z.B. 200g Mehl") },
                    )

                    if (ingredients.size > 1) {
                        IconButton(onClick = { onRemoveIngredient(index) }) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Zutat entfernen",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            if (ingredients.isEmpty()) {
                TextButton(
                    onClick = onAddIngredient,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Erste Zutat hinzufügen")
                }
            }
        }
    }
}

@Composable
private fun InstructionsSection(
    instructions: List<String>,
    onInstructionChange: (Int, String) -> Unit,
    onAddInstruction: () -> Unit,
    onRemoveInstruction: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Zubereitung*",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                IconButton(onClick = onAddInstruction) {
                    Icon(Icons.Default.Add, contentDescription = "Schritt hinzufügen")
                }
            }

            instructions.forEachIndexed { index, instruction ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    OutlinedTextField(
                        value = instruction,
                        onValueChange = { onInstructionChange(index, it) },
                        label = { Text("Schritt ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        minLines = 2,
                        maxLines = 5,
                        placeholder = { Text("Beschreiben Sie den Zubereitungsschritt...") },
                    )

                    if (instructions.size > 1) {
                        IconButton(onClick = { onRemoveInstruction(index) }) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Schritt entfernen",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            if (instructions.isEmpty()) {
                TextButton(
                    onClick = onAddInstruction,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ersten Schritt hinzufügen")
                }
            }
        }
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelectionChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(options) { option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectionChange(option)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedOption.equals(option, ignoreCase = true),
                            onClick = {
                                onSelectionChange(option)
                                onDismiss()
                            },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        },
    )
}

// Helper functions
private fun parseIngredientsFromRecipe(ingredientsJson: String): List<String> {
    return try {
        // Simple parsing - in a real app, use proper JSON parsing
        ingredientsJson
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    } catch (e: Exception) {
        listOf("")
    }
}

private fun parseInstructionsFromMarkdown(markdown: String): List<String> {
    val lines = markdown.lines()
    val instructions = mutableListOf<String>()
    var currentInstruction = ""

    for (line in lines) {
        when {
            line.startsWith("###") && line.contains("Schritt") -> {
                if (currentInstruction.isNotBlank()) {
                    instructions.add(currentInstruction.trim())
                }
                currentInstruction = ""
            }
            line.trim().isNotEmpty() && !line.startsWith("#") && !line.startsWith(">") -> {
                currentInstruction += line.trim() + " "
            }
        }
    }

    if (currentInstruction.isNotBlank()) {
        instructions.add(currentInstruction.trim())
    }

    return instructions.ifEmpty { listOf("") }
}

private fun buildMarkdown(
    ingredients: List<String>,
    instructions: List<String>,
): String {
    val markdown = StringBuilder()

    // Ingredients section
    markdown.appendLine("## Zutaten")
    markdown.appendLine()
    ingredients.filter { it.isNotBlank() }.forEach { ingredient ->
        markdown.appendLine("- $ingredient")
    }

    // Instructions section
    markdown.appendLine()
    markdown.appendLine("## Zubereitung")
    markdown.appendLine()
    instructions.filter { it.isNotBlank() }.forEachIndexed { index, instruction ->
        markdown.appendLine("### Schritt ${index + 1}")
        markdown.appendLine(instruction)
        markdown.appendLine()
    }

    return markdown.toString()
}

private fun buildIngredientsJson(ingredients: List<String>): String {
    val filteredIngredients = ingredients.filter { it.isNotBlank() }
    return "[${filteredIngredients.joinToString(",") { "\"$it\"" }}]"
}

private fun buildTags(
    category: String,
    dietType: String,
): String {
    val tags = mutableListOf<String>()

    if (category.isNotBlank() && category != "Alles") {
        tags.add(category.lowercase())
    }

    when (dietType.lowercase()) {
        "vegetarisch" -> tags.add("vegetarian")
        "vegan" -> tags.add("vegan")
        "low-carb" -> tags.add("low-carb")
        "high-protein" -> tags.add("high-protein")
        "glutenfrei" -> tags.add("gluten-free")
        "laktosefrei" -> tags.add("lactose-free")
    }

    return tags.joinToString(",")
}

private fun getCategory(tags: String): String {
    return when {
        tags.contains("hauptgericht", ignoreCase = true) -> "Hauptgericht"
        tags.contains("vorspeise", ignoreCase = true) -> "Vorspeise"
        tags.contains("dessert", ignoreCase = true) -> "Dessert"
        tags.contains("snack", ignoreCase = true) -> "Snack"
        tags.contains("getränk", ignoreCase = true) -> "Getränk"
        tags.contains("frühstück", ignoreCase = true) -> "Frühstück"
        tags.contains("beilage", ignoreCase = true) -> "Beilage"
        else -> "Hauptgericht"
    }
}

private fun getDietType(tags: String): String {
    return when {
        tags.contains("vegan", ignoreCase = true) -> "Vegan"
        tags.contains("vegetarian", ignoreCase = true) -> "Vegetarisch"
        tags.contains("low-carb", ignoreCase = true) -> "Low-Carb"
        tags.contains("high-protein", ignoreCase = true) -> "High-Protein"
        tags.contains("gluten-free", ignoreCase = true) -> "Glutenfrei"
        tags.contains("lactose-free", ignoreCase = true) -> "Laktosefrei"
        else -> "Alles"
    }
}
