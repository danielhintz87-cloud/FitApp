package com.example.fitapp.services

import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Unit tests for ShoppingListManager
 * Tests ingredient management, merging, categorization, and shopping list operations
 */
@ExperimentalCoroutinesApi
class ShoppingListManagerTest {

    @Mock
    private lateinit var database: AppDatabase

    private lateinit var shoppingListManager: ShoppingListManager

    private val sampleItems = listOf(
        ShoppingListManager.ShoppingListItem(
            id = "item_1",
            name = "Chicken Breast",
            quantity = 500.0,
            unit = "g",
            category = "Meat",
            addedFrom = "Chicken Recipe",
            priority = ShoppingListManager.Priority.NORMAL
        ),
        ShoppingListManager.ShoppingListItem(
            id = "item_2", 
            name = "Rice",
            quantity = 200.0,
            unit = "g",
            category = "Grains",
            addedFrom = "Rice Bowl Recipe",
            priority = ShoppingListManager.Priority.NORMAL
        ),
        ShoppingListManager.ShoppingListItem(
            id = "item_3",
            name = "Broccoli",
            quantity = 300.0,
            unit = "g",
            category = "Vegetables",
            addedFrom = "Veggie Recipe",
            priority = ShoppingListManager.Priority.HIGH
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        shoppingListManager = ShoppingListManager(database)
    }

    // Individual Ingredient Management Tests

    @Test
    fun `should add individual ingredients to shopping list`() = runTest {
        // Given: New ingredient to add
        val ingredient = ShoppingListManager.ShoppingListItem(
            id = "new_item",
            name = "Tomatoes",
            quantity = 4.0,
            unit = "pieces",
            category = "Vegetables",
            addedFrom = "Manual",
            priority = ShoppingListManager.Priority.NORMAL
        )

        // When: Adding ingredient to shopping list
        val result = shoppingListManager.addItem(ingredient)

        // Then: Should add successfully
        assertTrue("Should add item successfully", result)
        
        // Verify item was added to flow
        val items = shoppingListManager.shoppingItems.first()
        assertTrue("Should contain new item", items.any { it.id == ingredient.id })
        assertEquals("Should have correct name", ingredient.name, 
            items.find { it.id == ingredient.id }?.name)
    }

    @Test
    fun `should add ingredient with proper validation`() = runTest {
        // Test valid ingredient
        val validIngredient = ShoppingListManager.ShoppingListItem(
            id = "valid_item",
            name = "Milk",
            quantity = 1.0,
            unit = "L",
            category = "Dairy",
            addedFrom = "Recipe",
            priority = ShoppingListManager.Priority.NORMAL
        )
        
        val result1 = shoppingListManager.addItem(validIngredient)
        assertTrue("Should add valid ingredient", result1)

        // Test invalid ingredient (negative quantity)
        val invalidIngredient = validIngredient.copy(
            id = "invalid_item",
            quantity = -1.0
        )
        
        val result2 = shoppingListManager.addItem(invalidIngredient)
        assertFalse("Should reject negative quantity", result2)

        // Test invalid ingredient (empty name)
        val emptyNameIngredient = validIngredient.copy(
            id = "empty_name",
            name = ""
        )
        
        val result3 = shoppingListManager.addItem(emptyNameIngredient)
        assertFalse("Should reject empty name", result3)
    }

    @Test
    fun `should update existing ingredients`() = runTest {
        // Given: Existing ingredient in list
        val originalItem = sampleItems.first()
        shoppingListManager.addItem(originalItem)

        // When: Updating ingredient
        val updatedItem = originalItem.copy(
            quantity = 750.0,
            priority = ShoppingListManager.Priority.HIGH,
            notes = "Get organic"
        )
        val result = shoppingListManager.updateItem(updatedItem)

        // Then: Should update successfully
        assertTrue("Should update item successfully", result)
        
        val items = shoppingListManager.shoppingItems.first()
        val found = items.find { it.id == originalItem.id }
        assertNotNull("Updated item should exist", found)
        assertEquals("Should update quantity", 750.0, found?.quantity, 0.01)
        assertEquals("Should update priority", ShoppingListManager.Priority.HIGH, found?.priority)
        assertEquals("Should update notes", "Get organic", found?.notes)
    }

    @Test
    fun `should remove ingredients from list`() = runTest {
        // Given: Item in shopping list
        val itemToRemove = sampleItems.first()
        shoppingListManager.addItem(itemToRemove)

        // When: Removing item
        val result = shoppingListManager.removeItem(itemToRemove.id)

        // Then: Should remove successfully
        assertTrue("Should remove item successfully", result)
        
        val items = shoppingListManager.shoppingItems.first()
        assertFalse("Should no longer contain removed item", 
            items.any { it.id == itemToRemove.id })
    }

    // Smart Ingredient Merging Tests

    @Test
    fun `should merge similar ingredients intelligently`() = runTest {
        // Given: Similar ingredients with same unit
        val item1 = ShoppingListManager.ShoppingListItem(
            id = "carrots_1",
            name = "Carrots",
            quantity = 200.0,
            unit = "g",
            category = "Vegetables",
            addedFrom = "Recipe 1"
        )
        
        val item2 = ShoppingListManager.ShoppingListItem(
            id = "carrots_2", 
            name = "Carrots",
            quantity = 300.0,
            unit = "g",
            category = "Vegetables",
            addedFrom = "Recipe 2"
        )

        // When: Adding both items
        shoppingListManager.addItem(item1)
        val mergeResult = shoppingListManager.addItem(item2)

        // Then: Should merge intelligently
        assertTrue("Should merge successfully", mergeResult)
        
        val items = shoppingListManager.shoppingItems.first()
        val carrotItems = items.filter { it.name == "Carrots" }
        assertEquals("Should have only one carrot item after merge", 1, carrotItems.size)
        assertEquals("Should sum quantities", 500.0, carrotItems.first().quantity, 0.01)
        assertTrue("Should combine source information", 
            carrotItems.first().addedFrom.contains("Recipe 1") || 
            carrotItems.first().addedFrom.contains("Recipe 2"))
    }

    @Test
    fun `should handle unit conversion in merging`() = runTest {
        // Given: Similar ingredients with different but compatible units
        val item1 = ShoppingListManager.ShoppingListItem(
            id = "milk_1",
            name = "Milk",
            quantity = 1.0,
            unit = "L",
            category = "Dairy",
            addedFrom = "Recipe 1"
        )
        
        val item2 = ShoppingListManager.ShoppingListItem(
            id = "milk_2",
            name = "Milk", 
            quantity = 500.0,
            unit = "ml",
            category = "Dairy",
            addedFrom = "Recipe 2"
        )

        // When: Adding items with different units
        shoppingListManager.addItem(item1)
        val result = shoppingListManager.addItem(item2)

        // Then: Should convert and merge
        assertTrue("Should merge with unit conversion", result)
        
        val items = shoppingListManager.shoppingItems.first()
        val milkItems = items.filter { it.name == "Milk" }
        assertEquals("Should have one milk item", 1, milkItems.size)
        // Should convert to common unit (ml) and sum: 1000ml + 500ml = 1500ml
        assertEquals("Should convert and sum quantities", 1500.0, milkItems.first().quantity, 0.01)
        assertEquals("Should use common unit", "ml", milkItems.first().unit)
    }

    @Test
    fun `should not merge incompatible ingredients`() = runTest {
        // Given: Ingredients with same name but incompatible units
        val item1 = ShoppingListManager.ShoppingListItem(
            id = "flour_1",
            name = "Flour",
            quantity = 2.0,
            unit = "cups",
            category = "Baking",
            addedFrom = "Recipe 1"
        )
        
        val item2 = ShoppingListManager.ShoppingListItem(
            id = "flour_2",
            name = "Flour",
            quantity = 500.0,
            unit = "g",
            category = "Baking", 
            addedFrom = "Recipe 2"
        )

        // When: Adding items with incompatible units
        shoppingListManager.addItem(item1)
        shoppingListManager.addItem(item2)

        // Then: Should keep separate items
        val items = shoppingListManager.shoppingItems.first()
        val flourItems = items.filter { it.name == "Flour" }
        assertEquals("Should keep separate items for incompatible units", 2, flourItems.size)
    }

    @Test
    fun `should handle priority conflicts in merging`() = runTest {
        // Given: Same ingredient with different priorities
        val normalItem = ShoppingListManager.ShoppingListItem(
            id = "onion_1",
            name = "Onions",
            quantity = 2.0,
            unit = "pieces",
            category = "Vegetables",
            addedFrom = "Recipe 1",
            priority = ShoppingListManager.Priority.NORMAL
        )
        
        val urgentItem = ShoppingListManager.ShoppingListItem(
            id = "onion_2",
            name = "Onions",
            quantity = 1.0,
            unit = "pieces", 
            category = "Vegetables",
            addedFrom = "Recipe 2",
            priority = ShoppingListManager.Priority.URGENT
        )

        // When: Adding items with different priorities
        shoppingListManager.addItem(normalItem)
        shoppingListManager.addItem(urgentItem)

        // Then: Should use higher priority
        val items = shoppingListManager.shoppingItems.first()
        val onionItem = items.find { it.name == "Onions" }
        assertNotNull("Merged onion item should exist", onionItem)
        assertEquals("Should use higher priority", ShoppingListManager.Priority.URGENT, onionItem?.priority)
        assertEquals("Should sum quantities", 3.0, onionItem?.quantity, 0.01)
    }

    // Category Grouping Tests

    @Test
    fun `should group ingredients by category`() = runTest {
        // Given: Multiple items in different categories
        sampleItems.forEach { shoppingListManager.addItem(it) }

        // When: Getting categorized items
        val categorizedItems = shoppingListManager.categorizedItems.first()

        // Then: Should group by category
        assertTrue("Should have Meat category", categorizedItems.containsKey("Meat"))
        assertTrue("Should have Grains category", categorizedItems.containsKey("Grains"))
        assertTrue("Should have Vegetables category", categorizedItems.containsKey("Vegetables"))
        
        assertEquals("Meat category should have 1 item", 1, categorizedItems["Meat"]?.size)
        assertEquals("Grains category should have 1 item", 1, categorizedItems["Grains"]?.size)
        assertEquals("Vegetables category should have 1 item", 1, categorizedItems["Vegetables"]?.size)
    }

    @Test
    fun `should sort categories logically`() = runTest {
        // Given: Items from various categories
        val items = listOf(
            createTestItem("Bread", "Bakery"),
            createTestItem("Apples", "Fruits"),
            createTestItem("Milk", "Dairy"),
            createTestItem("Chicken", "Meat"),
            createTestItem("Carrots", "Vegetables")
        )
        
        items.forEach { shoppingListManager.addItem(it) }

        // When: Getting category order
        val categoryOrder = shoppingListManager.getCategoryOrder()

        // Then: Should have logical shopping order
        val expectedOrder = listOf("Fruits", "Vegetables", "Meat", "Dairy", "Bakery")
        assertEquals("Should follow logical shopping order", expectedOrder, categoryOrder)
    }

    @Test
    fun `should handle custom categories`() = runTest {
        // Given: Item with custom category
        val customItem = createTestItem("Special Sauce", "Custom")
        shoppingListManager.addItem(customItem)

        // When: Getting categorized items
        val categorizedItems = shoppingListManager.categorizedItems.first()

        // Then: Should include custom category
        assertTrue("Should include custom category", categorizedItems.containsKey("Custom"))
        assertEquals("Custom category should have 1 item", 1, categorizedItems["Custom"]?.size)
    }

    // Purchase Status Management Tests

    @Test
    fun `should track ingredient purchase status`() = runTest {
        // Given: Item in shopping list
        val item = sampleItems.first()
        shoppingListManager.addItem(item)

        // When: Marking item as purchased
        val result = shoppingListManager.markAsPurchased(item.id, true)

        // Then: Should update purchase status
        assertTrue("Should mark as purchased successfully", result)
        
        val items = shoppingListManager.shoppingItems.first()
        val purchasedItem = items.find { it.id == item.id }
        assertTrue("Item should be marked as purchased", purchasedItem?.isPurchased == true)
    }

    @Test
    fun `should toggle purchase status`() = runTest {
        // Given: Purchased item
        val item = sampleItems.first().copy(isPurchased = true)
        shoppingListManager.addItem(item)

        // When: Toggling purchase status
        shoppingListManager.togglePurchaseStatus(item.id)

        // Then: Should toggle to unpurchased
        val items = shoppingListManager.shoppingItems.first()
        val toggledItem = items.find { it.id == item.id }
        assertFalse("Item should be unpurchased after toggle", toggledItem?.isPurchased == true)
    }

    @Test
    fun `should get purchase statistics`() = runTest {
        // Given: Mix of purchased and unpurchased items
        val items = listOf(
            sampleItems[0].copy(isPurchased = true),
            sampleItems[1].copy(isPurchased = false),
            sampleItems[2].copy(isPurchased = true)
        )
        items.forEach { shoppingListManager.addItem(it) }

        // When: Getting purchase statistics
        val stats = shoppingListManager.getPurchaseStatistics()

        // Then: Should calculate correctly
        assertEquals("Should count total items", 3, stats.totalItems)
        assertEquals("Should count purchased items", 2, stats.purchasedItems)
        assertEquals("Should count remaining items", 1, stats.remainingItems)
        assertEquals("Should calculate completion percentage", 66.67f, stats.completionPercentage, 0.1f)
    }

    // Bulk Operations Tests

    @Test
    fun `should clear completed items efficiently`() = runTest {
        // Given: Mix of purchased and unpurchased items
        val items = listOf(
            sampleItems[0].copy(isPurchased = true),
            sampleItems[1].copy(isPurchased = false),
            sampleItems[2].copy(isPurchased = true)
        )
        items.forEach { shoppingListManager.addItem(it) }

        // When: Clearing completed items
        val result = shoppingListManager.clearCompletedItems()

        // Then: Should remove only purchased items
        assertTrue("Should clear completed items successfully", result)
        
        val remainingItems = shoppingListManager.shoppingItems.first()
        assertEquals("Should have 1 remaining item", 1, remainingItems.size)
        assertFalse("Remaining item should be unpurchased", remainingItems.first().isPurchased)
    }

    @Test
    fun `should clear all items`() = runTest {
        // Given: Items in shopping list
        sampleItems.forEach { shoppingListManager.addItem(it) }

        // When: Clearing all items
        val result = shoppingListManager.clearAllItems()

        // Then: Should remove all items
        assertTrue("Should clear all items successfully", result)
        
        val items = shoppingListManager.shoppingItems.first()
        assertTrue("Should have no items", items.isEmpty())
    }

    @Test
    fun `should add multiple items from recipe`() = runTest {
        // Given: Recipe ingredients
        val recipeIngredients = listOf(
            RecipeIngredient("Pasta", 200f, "g", IngredientCategory.GRAIN),
            RecipeIngredient("Tomato Sauce", 400f, "ml", IngredientCategory.OTHER),
            RecipeIngredient("Cheese", 100f, "g", IngredientCategory.DAIRY)
        )

        // When: Adding recipe ingredients to shopping list
        val result = shoppingListManager.addRecipeIngredients("Pasta Recipe", recipeIngredients)

        // Then: Should add all ingredients
        assertTrue("Should add recipe ingredients successfully", result)
        
        val items = shoppingListManager.shoppingItems.first()
        assertEquals("Should have 3 items", 3, items.size)
        assertTrue("All items should be from recipe", 
            items.all { it.addedFrom == "Pasta Recipe" })
    }

    // Search and Filter Tests

    @Test
    fun `should search items by name`() = runTest {
        // Given: Items in shopping list
        sampleItems.forEach { shoppingListManager.addItem(it) }

        // When: Searching for items
        val searchResults = shoppingListManager.searchItems("Chicken")

        // Then: Should find matching items
        assertEquals("Should find 1 matching item", 1, searchResults.size)
        assertEquals("Should find chicken breast", "Chicken Breast", searchResults.first().name)
    }

    @Test
    fun `should filter items by category`() = runTest {
        // Given: Items in shopping list
        sampleItems.forEach { shoppingListManager.addItem(it) }

        // When: Filtering by category
        val vegetableItems = shoppingListManager.filterByCategory("Vegetables")

        // Then: Should return only vegetables
        assertEquals("Should find 1 vegetable item", 1, vegetableItems.size)
        assertEquals("Should find broccoli", "Broccoli", vegetableItems.first().name)
    }

    @Test
    fun `should filter items by priority`() = runTest {
        // Given: Items with different priorities
        sampleItems.forEach { shoppingListManager.addItem(it) }

        // When: Filtering by high priority
        val highPriorityItems = shoppingListManager.filterByPriority(ShoppingListManager.Priority.HIGH)

        // Then: Should return only high priority items
        assertEquals("Should find 1 high priority item", 1, highPriorityItems.size)
        assertEquals("Should find broccoli", "Broccoli", highPriorityItems.first().name)
    }

    @Test
    fun `should filter items by purchase status`() = runTest {
        // Given: Mix of purchased and unpurchased items
        val items = listOf(
            sampleItems[0].copy(isPurchased = true),
            sampleItems[1].copy(isPurchased = false),
            sampleItems[2].copy(isPurchased = false)
        )
        items.forEach { shoppingListManager.addItem(it) }

        // When: Filtering unpurchased items
        val unpurchasedItems = shoppingListManager.filterByPurchaseStatus(false)

        // Then: Should return only unpurchased items
        assertEquals("Should find 2 unpurchased items", 2, unpurchasedItems.size)
        assertTrue("All items should be unpurchased", 
            unpurchasedItems.all { !it.isPurchased })
    }

    // Helper Methods

    private fun createTestItem(name: String, category: String): ShoppingListManager.ShoppingListItem {
        return ShoppingListManager.ShoppingListItem(
            id = "test_${name.lowercase().replace(" ", "_")}",
            name = name,
            quantity = 1.0,
            unit = "piece",
            category = category,
            addedFrom = "Test",
            priority = ShoppingListManager.Priority.NORMAL
        )
    }

    // Cost Estimation Tests

    @Test
    fun `should estimate total shopping cost`() = runTest {
        // Given: Items with estimated costs
        val itemsWithCosts = listOf(
            sampleItems[0].copy(estimatedCost = 5.99),
            sampleItems[1].copy(estimatedCost = 2.50),
            sampleItems[2].copy(estimatedCost = 3.25)
        )
        itemsWithCosts.forEach { shoppingListManager.addItem(it) }

        // When: Calculating total cost
        val totalCost = shoppingListManager.calculateTotalEstimatedCost()

        // Then: Should sum all costs
        assertEquals("Should calculate total cost correctly", 11.74, totalCost, 0.01)
    }

    @Test
    fun `should handle items without cost estimation`() = runTest {
        // Given: Mix of items with and without costs
        val items = listOf(
            sampleItems[0].copy(estimatedCost = 5.99),
            sampleItems[1].copy(estimatedCost = null),
            sampleItems[2].copy(estimatedCost = 3.25)
        )
        items.forEach { shoppingListManager.addItem(it) }

        // When: Calculating total cost
        val totalCost = shoppingListManager.calculateTotalEstimatedCost()

        // Then: Should only sum items with costs
        assertEquals("Should calculate total for items with costs", 9.24, totalCost, 0.01)
    }
}