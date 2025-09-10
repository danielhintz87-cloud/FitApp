package com.example.fitapp.services

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.fitapp.data.db.*
import com.example.fitapp.data.prefs.UserPreferencesRepository
import com.example.fitapp.util.StructuredLogger
import kotlin.math.abs

/**
 * Smart Shopping List Manager
 * Handles ingredient management, smart merging, category grouping, and AutoComplete
 */
class ShoppingListManager(
    private val database: AppDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "ShoppingListManager"
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    
    // AutoComplete engine for intelligent suggestions
    private val autoCompleteEngine = ShoppingAutoCompleteEngine()
    
    // Flow states for reactive UI
    private val _shoppingItems = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingListItem>> = _shoppingItems.asStateFlow()
    
    private val _categorizedItems = MutableStateFlow<Map<String, List<ShoppingListItem>>>(emptyMap())
    val categorizedItems: StateFlow<Map<String, List<ShoppingListItem>>> = _categorizedItems.asStateFlow()
    
    // AutoComplete suggestions state
    private val _autoCompleteSuggestions = MutableStateFlow<List<AutoCompleteSuggestion>>(emptyList())
    val autoCompleteSuggestions: StateFlow<List<AutoCompleteSuggestion>> = _autoCompleteSuggestions.asStateFlow()
    
    // Current sorting mode
    private val _sortingMode = MutableStateFlow(ShoppingListSorter.SortingMode.SUPERMARKET_LAYOUT)
    val sortingMode: StateFlow<ShoppingListSorter.SortingMode> = _sortingMode.asStateFlow()

    data class ShoppingListItem(
        val id: String,
        val name: String,
        val quantity: Double,
        val unit: String,
        val category: String, // Gemüse, Fleisch, Milchprodukte, etc.
        val isPurchased: Boolean = false,
        val addedFrom: String, // Recipe name oder "Manual"
        val priority: Priority = Priority.NORMAL,
        val notes: String? = null,
        val estimatedCost: Double? = null
    )

    enum class Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    init {
        // Load shopping items on initialization and observe changes
        scope.launch {
            database.shoppingDao().itemsFlow().collect { dbItems ->
                val shoppingItems = dbItems.map { dbItem ->
                    ShoppingListItem(
                        id = dbItem.id.toString(),
                        name = dbItem.name,
                        quantity = parseQuantity(dbItem.quantity ?: "1"),
                        unit = dbItem.unit ?: "Stück",
                        category = dbItem.category ?: "Sonstiges",
                        isPurchased = dbItem.checked,
                        addedFrom = dbItem.fromRecipeId ?: "Manual",
                        priority = Priority.NORMAL // Could be enhanced to store priority in DB
                    )
                }
                
                _shoppingItems.value = shoppingItems
                updateCategorizedItems()
            }
        }
        
        // Load saved sorting mode from preferences
        scope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                val sortingModeStr = prefs.shoppingListSortingMode
                if (sortingModeStr.isNotEmpty()) {
                    try {
                        val savedMode = ShoppingListSorter.SortingMode.valueOf(sortingModeStr)
                        if (_sortingMode.value != savedMode) {
                            _sortingMode.value = savedMode
                            updateCategorizedItems()
                        }
                    } catch (e: IllegalArgumentException) {
                        // Invalid mode, keep default
                    }
                }
            }
        }
        
        // Load autocomplete usage data (could be enhanced to save to preferences/database)
        scope.launch {
            // For now, autocomplete data is kept in memory
            // In a real app, you'd load this from SharedPreferences or database
        }
    }

    /**
     * Add a single ingredient to the shopping list with smart merging
     */
    suspend fun addIngredient(
        ingredient: CookingModeManager.Ingredient,
        addedFrom: String = "Manual",
        priority: Priority = Priority.NORMAL
    ) {
        val category = categorizeIngredient(ingredient.name)
        val unit = normalizeUnit(ingredient.unit)
        val quantity = parseQuantity(ingredient.quantity)
        
        val newItem = ShoppingListItem(
            id = generateId(),
            name = ingredient.name,
            quantity = quantity,
            unit = unit,
            category = category,
            addedFrom = addedFrom,
            priority = priority
        )
        
        // Check for existing similar items to merge
        val existingItem = findSimilarItem(newItem)
        
        if (existingItem != null) {
            val mergedItem = mergeIngredients(existingItem, newItem)
            updateShoppingItem(mergedItem)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Merged ingredient ${ingredient.name} with existing item"
            )
        } else {
            insertShoppingItem(newItem)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Added new ingredient ${ingredient.name} to shopping list"
            )
        }
        
        refreshShoppingItems()
    }

    /**
     * Add all ingredients from a recipe to the shopping list
     */
    suspend fun addAllRecipeIngredients(
        recipeTitle: String,
        ingredients: List<CookingModeManager.Ingredient>,
        servings: Int = 1
    ) {
        ingredients.forEach { ingredient ->
            // Adjust quantities based on servings
            val adjustedIngredient = ingredient.copy(
                quantity = adjustQuantityForServings(ingredient.quantity, servings).toString()
            )
            
            addIngredient(
                ingredient = adjustedIngredient,
                addedFrom = recipeTitle,
                priority = if (ingredient.isOptional) Priority.LOW else Priority.NORMAL
            )
        }
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Added ${ingredients.size} ingredients from recipe '$recipeTitle' for $servings servings"
        )
    }

    /**
     * Remove an ingredient from the shopping list
     */
    suspend fun removeIngredient(itemId: String) {
        database.shoppingDao().delete(itemId.toLongOrNull() ?: return)
        refreshShoppingItems()
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Removed ingredient with ID $itemId from shopping list"
        )
    }

    /**
     * Mark an ingredient as purchased/unpurchased
     */
    suspend fun markIngredientAsPurchased(itemId: String, purchased: Boolean = true) {
        database.shoppingDao().setChecked(itemId.toLongOrNull() ?: return, purchased)
        refreshShoppingItems()
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Marked ingredient $itemId as ${if (purchased) "purchased" else "unpurchased"}"
        )
    }

    /**
     * Clear all completed/purchased items
     */
    suspend fun clearCompletedItems() {
        database.shoppingDao().deleteCheckedItems()
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Cleared all completed items from shopping list"
        )
    }
    
    /**
     * Clear all items from shopping list
     */
    suspend fun clearAllItems() {
        scope.launch {
            val allItems = database.shoppingDao().itemsFlow().first()
            allItems.forEach { item ->
                database.shoppingDao().delete(item.id)
            }
        }
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Cleared entire shopping list"
        )
    }

    /**
     * Update quantity and unit for an existing shopping list item
     */
    suspend fun updateItemQuantity(itemId: String, newQuantity: String, newUnit: String) {
        val longId = itemId.toLongOrNull() ?: return
        val quantityDouble = newQuantity.toDoubleOrNull() ?: return
        
        // Update in database
        database.shoppingDao().updateQuantityAndUnit(longId, quantityDouble, newUnit)
        
        // Refresh the items to reflect changes
        refreshShoppingItems()
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Updated item $itemId quantity to $newQuantity $newUnit"
        )
    }

    /**
     * Group ingredients by category with smart ordering
     */
    fun groupIngredientsByCategory(): Map<String, List<ShoppingListItem>> {
        val currentItems = _shoppingItems.value
        return ShoppingListSorter.sortItems(currentItems, _sortingMode.value)
    }
    
    /**
     * Change sorting mode and update categorized items
     */
    fun changeSortingMode(newMode: ShoppingListSorter.SortingMode) {
        _sortingMode.value = newMode
        updateCategorizedItems()
        
        // Save to preferences
        scope.launch {
            preferencesRepository.updateShoppingListPreferences(newMode.name)
        }
    }
    
    /**
     * Update categorized items based on current sorting mode
     */
    private fun updateCategorizedItems() {
        _categorizedItems.value = groupIngredientsByCategory()
    }

    /**
     * Smart merging of two similar ingredients
     */
    fun mergeIngredients(
        item1: ShoppingListItem,
        item2: ShoppingListItem
    ): ShoppingListItem {
        // If units are compatible, add quantities
        val (mergedQuantity, mergedUnit) = if (areUnitsCompatible(item1.unit, item2.unit)) {
            val normalizedQuantity1 = convertToBaseUnit(item1.quantity, item1.unit)
            val normalizedQuantity2 = convertToBaseUnit(item2.quantity, item2.unit)
            val totalQuantity = normalizedQuantity1 + normalizedQuantity2
            
            // Use the more precise unit
            val bestUnit = chooseBestUnit(item1.unit, item2.unit, totalQuantity)
            val convertedQuantity = convertFromBaseUnit(totalQuantity, bestUnit)
            
            Pair(convertedQuantity, bestUnit)
        } else {
            // If units are incompatible, combine as text
            Pair(
                item1.quantity,
                "${item1.quantity} ${item1.unit} + ${item2.quantity} ${item2.unit}"
            )
        }
        
        return item1.copy(
            quantity = mergedQuantity,
            unit = mergedUnit,
            addedFrom = if (item1.addedFrom == item2.addedFrom) {
                item1.addedFrom
            } else {
                "${item1.addedFrom}, ${item2.addedFrom}"
            },
            priority = maxOf(item1.priority, item2.priority),
            notes = listOfNotNull(item1.notes, item2.notes).joinToString("; ").takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Get AutoComplete suggestions based on input text
     */
    suspend fun getAutoCompleteSuggestions(input: String): List<AutoCompleteSuggestion> {
        if (input.length < 2) {
            // Show frequent suggestions when no input
            val suggestions = autoCompleteEngine.getSuggestions("", emptyList())
            _autoCompleteSuggestions.value = suggestions
            return suggestions
        }
        
        // Get recent items from shopping list for learning
        val recentItems = _shoppingItems.value.map { it.name }.distinct()
        
        // Get suggestions from the autocomplete engine
        val suggestions = autoCompleteEngine.getSuggestions(input, recentItems)
        
        _autoCompleteSuggestions.value = suggestions
        return suggestions
    }
    
    /**
     * Get smart suggestions based on shopping patterns
     */
    suspend fun getSmartSuggestions(limit: Int = 6): List<AutoCompleteSuggestion> {
        // Get recent items from shopping list
        val recentItems = _shoppingItems.value
            .filter { !it.isPurchased } // Only unpurchased items
            .map { it.name }.distinct()
        
        // Get frequent suggestions (this will show most used items)
        val suggestions = autoCompleteEngine.getSuggestions("", recentItems)
        
        return suggestions.take(limit)
    }
    
    /**
     * Record usage of an item for learning
     */
    suspend fun recordItemUsage(itemName: String) {
        autoCompleteEngine.recordItemUsage(itemName)
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Recorded usage for item: $itemName"
        )
    }
    
    /**
     * Add item from AutoComplete suggestion
     */
    suspend fun addItemFromSuggestion(
        suggestion: AutoCompleteSuggestion,
        addedFrom: String = "Manual",
        priority: Priority = Priority.NORMAL
    ) {
        // Record the usage for learning
        recordItemUsage(suggestion.text)
        
        // Create ingredient from suggestion
        val ingredient = CookingModeManager.Ingredient(
            name = suggestion.text,
            quantity = "1",
            unit = "Stück"
        )
        
        addIngredient(ingredient, addedFrom, priority)
        
        StructuredLogger.info(
            StructuredLogger.LogCategory.NUTRITION,
            TAG,
            "Added item from AutoComplete: ${suggestion.text}"
        )
    }
    
    /**
     * Clear AutoComplete suggestions
     */
    fun clearAutoCompleteSuggestions() {
        _autoCompleteSuggestions.value = emptyList()
    }
    fun getShoppingStats(): ShoppingStats {
        val items = _shoppingItems.value
        val totalItems = items.size
        val purchasedItems = items.count { it.isPurchased }
        val estimatedTotal = items.sumOf { it.estimatedCost ?: 0.0 }
        val urgentItems = items.count { it.priority == Priority.URGENT }
        
        return ShoppingStats(
            totalItems = totalItems,
            purchasedItems = purchasedItems,
            completionPercentage = if (totalItems > 0) (purchasedItems * 100) / totalItems else 0,
            estimatedTotal = estimatedTotal,
            urgentItems = urgentItems
        )
    }

    data class ShoppingStats(
        val totalItems: Int,
        val purchasedItems: Int,
        val completionPercentage: Int,
        val estimatedTotal: Double,
        val urgentItems: Int
    )

    // Private helper methods
    
    private suspend fun loadShoppingItems() {
        val dbItems = database.shoppingDao().itemsFlow().first()
        val shoppingItems = dbItems.map { dbItem ->
            ShoppingListItem(
                id = dbItem.id.toString(),
                name = dbItem.name,
                quantity = parseQuantity(dbItem.quantity ?: "1"),
                unit = dbItem.unit ?: "Stück",
                category = dbItem.category ?: "Sonstiges",
                isPurchased = dbItem.checked,
                addedFrom = dbItem.fromRecipeId ?: "Manual",
                priority = Priority.NORMAL // Could be enhanced to store priority in DB
            )
        }
        
        _shoppingItems.value = shoppingItems
        updateCategorizedItems()
    }
    
    private suspend fun refreshShoppingItems() {
        loadShoppingItems()
    }
    
    private suspend fun insertShoppingItem(item: ShoppingListItem) {
        val dbItem = ShoppingItemEntity(
            name = item.name,
            quantity = "${item.quantity} ${item.unit}",
            unit = item.unit,
            category = item.category,
            fromRecipeId = if (item.addedFrom != "Manual") item.addedFrom else null
        )
        database.shoppingDao().insert(dbItem)
    }
    
    private suspend fun updateShoppingItem(item: ShoppingListItem) {
        val existingDbItem = database.shoppingDao().itemsFlow().first()
            .find { it.id.toString() == item.id }
        
        existingDbItem?.let { dbItem ->
            val updatedDbItem = dbItem.copy(
                name = item.name,
                quantity = "${item.quantity} ${item.unit}",
                unit = item.unit,
                category = item.category,
                checked = item.isPurchased
            )
            database.shoppingDao().update(updatedDbItem)
        }
    }
    
    private fun findSimilarItem(newItem: ShoppingListItem): ShoppingListItem? {
        return _shoppingItems.value.find { existingItem ->
            isSimilarIngredient(existingItem.name, newItem.name) &&
            existingItem.category == newItem.category &&
            !existingItem.isPurchased
        }
    }
    
    private fun isSimilarIngredient(name1: String, name2: String): Boolean {
        val normalized1 = normalizeIngredientName(name1)
        val normalized2 = normalizeIngredientName(name2)
        
        // Exact match
        if (normalized1 == normalized2) return true
        
        // Check if one contains the other
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) return true
        
        // Check for common aliases
        val aliases = getIngredientAliases(normalized1)
        return aliases.any { it == normalized2 }
    }
    
    private fun normalizeIngredientName(name: String): String {
        return name.lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(Regex("[^a-z0-9]"), "")
    }
    
    private fun getIngredientAliases(name: String): List<String> {
        val aliasMap = mapOf(
            "tomate" to listOf("tomaten", "rispentomaten", "cherrytomaten"),
            "zwiebel" to listOf("zwiebeln", "gemuesezwiebeln", "speisezwiebeln"),
            "kartoffel" to listOf("kartoffeln", "erdaepfel"),
            "milch" to listOf("vollmilch", "frischmilch"),
            "mehl" to listOf("weizenmehl", "allzweckmehl"),
            "zucker" to listOf("kristallzucker", "weisserzucker"),
            "butter" to listOf("deutsche butter", "suessrahmbutter"),
            "sahne" to listOf("schlagsahne", "susserahm"),
            "quark" to listOf("magerquark", "speisequark")
        )
        
        return aliasMap[name] ?: emptyList()
    }
    
    private fun categorizeIngredient(name: String): String {
        return ShoppingListSorter.categorizeIngredient(name)
    }
    
    private fun normalizeUnit(unit: String): String {
        val normalized = unit.lowercase().trim()
        
        return when (normalized) {
            "g", "gr", "gramm" -> "g"
            "kg", "kilo", "kilogramm" -> "kg"
            "ml", "milliliter" -> "ml"
            "l", "liter" -> "l"
            "el", "essloffel", "esslöffel" -> "EL"
            "tl", "teeloffel", "teelöffel" -> "TL"
            "stuck", "stück", "st", "x" -> "Stück"
            "prise", "pr" -> "Prise"
            "bund" -> "Bund"
            "packung", "pack", "pkg" -> "Packung"
            "dose" -> "Dose"
            "glas" -> "Glas"
            else -> normalized
        }
    }
    
    private fun parseQuantity(quantityStr: String): Double {
        return try {
            // Extract number from string like "2.5 kg" or "1/2 cup"
            val numberRegex = Regex("([0-9]+(?:[.,][0-9]+)?)")
            val match = numberRegex.find(quantityStr.replace(",", "."))
            match?.value?.toDouble() ?: 1.0
        } catch (e: Exception) {
            1.0
        }
    }
    
    private fun adjustQuantityForServings(quantityStr: String, servings: Int): Double {
        val baseQuantity = parseQuantity(quantityStr)
        return baseQuantity * servings
    }
    
    private fun areUnitsCompatible(unit1: String, unit2: String): Boolean {
        val weightUnits = setOf("g", "kg")
        val volumeUnits = setOf("ml", "l")
        val spoonUnits = setOf("EL", "TL")
        
        return when {
            unit1 in weightUnits && unit2 in weightUnits -> true
            unit1 in volumeUnits && unit2 in volumeUnits -> true
            unit1 in spoonUnits && unit2 in spoonUnits -> true
            unit1 == unit2 -> true
            else -> false
        }
    }
    
    private fun convertToBaseUnit(quantity: Double, unit: String): Double {
        return when (unit) {
            "kg" -> quantity * 1000 // to grams
            "l" -> quantity * 1000  // to ml
            "EL" -> quantity * 3    // to TL
            else -> quantity
        }
    }
    
    private fun convertFromBaseUnit(quantity: Double, unit: String): Double {
        return when (unit) {
            "kg" -> quantity / 1000 // from grams
            "l" -> quantity / 1000  // from ml
            "EL" -> quantity / 3    // from TL
            else -> quantity
        }
    }
    
    private fun chooseBestUnit(unit1: String, unit2: String, totalQuantity: Double): String {
        // Choose the unit that results in a reasonable number (not too big, not too small)
        val result1 = convertFromBaseUnit(totalQuantity, unit1)
        val result2 = convertFromBaseUnit(totalQuantity, unit2)
        
        return when {
            result1 in 0.1..999.0 && result2 !in 0.1..999.0 -> unit1
            result2 in 0.1..999.0 && result1 !in 0.1..999.0 -> unit2
            result1 <= result2 -> unit1 // Prefer smaller units when both are reasonable
            else -> unit2
        }
    }
    
    private fun generateId(): String {
        return System.currentTimeMillis().toString()
    }
    
    /**
     * Add ingredient from text string (for recipe integration)
     */
    suspend fun addIngredientFromText(
        ingredientText: String,
        fromRecipe: String = "Manual",
        priority: Priority = Priority.NORMAL
    ) {
        try {
            val (name, quantity, unit) = parseIngredientText(ingredientText)
            val category = categorizeIngredient(name)
            
            val ingredient = CookingModeManager.Ingredient(
                name = name,
                quantity = "$quantity",
                unit = unit
            )
            
            addIngredient(ingredient, fromRecipe, priority)
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NUTRITION,
                TAG,
                "Error parsing ingredient text: $ingredientText",
                exception = e
            )
            
            // Fallback: add as simple text
            val ingredient = CookingModeManager.Ingredient(
                name = ingredientText,
                quantity = "1",
                unit = "Stück"
            )
            
            addIngredient(ingredient, fromRecipe, priority)
        }
    }
    
    private fun parseIngredientText(text: String): Triple<String, Double, String> {
        // Simple parsing - in a real implementation, this would be more sophisticated
        val trimmed = text.trim()
        
        // Try to extract quantity and unit from the beginning
        val quantityPattern = Regex("^(\\d+(?:[.,]\\d+)?)\\s*(\\w+)?\\s+(.+)")
        val match = quantityPattern.find(trimmed)
        
        return if (match != null) {
            val quantity = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 1.0
            val unit = match.groupValues[2].ifBlank { "Stück" }
            val name = match.groupValues[3]
            Triple(name, quantity, unit)
        } else {
            // No quantity found, treat as single item
            Triple(trimmed, 1.0, "Stück")
        }
    }
}