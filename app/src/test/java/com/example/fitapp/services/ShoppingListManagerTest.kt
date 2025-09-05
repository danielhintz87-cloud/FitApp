package com.example.fitapp.services

import com.example.fitapp.data.db.AppDatabase
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
 * Tests shopping list management, categorization, and smart merging
 */
class ShoppingListManagerTest {

    @Mock
    private lateinit var database: AppDatabase

    private lateinit var shoppingListManager: ShoppingListManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        shoppingListManager = ShoppingListManager(database)
    }

    @Test
    fun `should instantiate ShoppingListManager correctly`() {
        assertNotNull("ShoppingListManager should be instantiated", shoppingListManager)
    }

    @Test
    fun `should start with empty shopping items`() = runTest {
        // When: Getting initial shopping items
        val initialItems = shoppingListManager.shoppingItems.first()

        // Then: Should be empty
        assertTrue("Initial shopping items should be empty", initialItems.isEmpty())
    }

    @Test
    fun `should start with empty categorized items`() = runTest {
        // When: Getting initial categorized items
        val initialCategorized = shoppingListManager.categorizedItems.first()

        // Then: Should be empty
        assertTrue("Initial categorized items should be empty", initialCategorized.isEmpty())
    }

    @Test
    fun `should create ShoppingListItem with all properties`() {
        // Given: Item properties
        val id = "test-id"
        val name = "Chicken Breast"
        val quantity = 2.0
        val unit = "kg"
        val category = "Meat"
        val isPurchased = false
        val addedFrom = "Protein Recipe"
        val priority = ShoppingListManager.Priority.HIGH
        val notes = "Organic preferred"
        val estimatedCost = 15.99

        // When: Creating shopping list item
        val item = ShoppingListManager.ShoppingListItem(
            id = id,
            name = name,
            quantity = quantity,
            unit = unit,
            category = category,
            isPurchased = isPurchased,
            addedFrom = addedFrom,
            priority = priority,
            notes = notes,
            estimatedCost = estimatedCost
        )

        // Then: All properties should be set correctly
        assertEquals("ID should match", id, item.id)
        assertEquals("Name should match", name, item.name)
        assertEquals("Quantity should match", quantity, item.quantity, 0.01)
        assertEquals("Unit should match", unit, item.unit)
        assertEquals("Category should match", category, item.category)
        assertEquals("Purchase status should match", isPurchased, item.isPurchased)
        assertEquals("Added from should match", addedFrom, item.addedFrom)
        assertEquals("Priority should match", priority, item.priority)
        assertEquals("Notes should match", notes, item.notes)
        assertEquals("Estimated cost should match", estimatedCost, item.estimatedCost)
    }

    @Test
    fun `should create ShoppingListItem with default values`() {
        // When: Creating item with minimal properties
        val item = ShoppingListManager.ShoppingListItem(
            id = "test-id",
            name = "Test Item",
            quantity = 1.0,
            unit = "piece",
            category = "Other",
            addedFrom = "Manual"
        )

        // Then: Should have default values
        assertFalse("Should not be purchased by default", item.isPurchased)
        assertEquals("Should have normal priority by default", 
            ShoppingListManager.Priority.NORMAL, item.priority)
        assertNull("Notes should be null by default", item.notes)
        assertNull("Estimated cost should be null by default", item.estimatedCost)
    }

    @Test
    fun `should handle all priority levels`() {
        val priorities = listOf(
            ShoppingListManager.Priority.LOW,
            ShoppingListManager.Priority.NORMAL,
            ShoppingListManager.Priority.HIGH,
            ShoppingListManager.Priority.URGENT
        )

        for (priority in priorities) {
            // When: Creating item with each priority
            val item = ShoppingListManager.ShoppingListItem(
                id = "test-$priority",
                name = "Test Item",
                quantity = 1.0,
                unit = "piece",
                category = "Test",
                addedFrom = "Test",
                priority = priority
            )

            // Then: Priority should be set correctly
            assertEquals("Priority should match for $priority", priority, item.priority)
        }
    }

    @Test
    fun `should handle different measurement units`() {
        val units = listOf("kg", "g", "l", "ml", "piece", "cup", "tbsp", "tsp")

        for (unit in units) {
            // When: Creating item with different units
            val item = ShoppingListManager.ShoppingListItem(
                id = "test-$unit",
                name = "Test Item",
                quantity = 1.0,
                unit = unit,
                category = "Test",
                addedFrom = "Test"
            )

            // Then: Unit should be set correctly
            assertEquals("Unit should match for $unit", unit, item.unit)
        }
    }

    @Test
    fun `should handle different food categories`() {
        val categories = listOf(
            "Vegetables", "Fruits", "Meat", "Dairy", "Grains", 
            "Spices", "Beverages", "Snacks", "Other"
        )

        for (category in categories) {
            // When: Creating item with different categories
            val item = ShoppingListManager.ShoppingListItem(
                id = "test-$category",
                name = "Test Item",
                quantity = 1.0,
                unit = "piece",
                category = category,
                addedFrom = "Test"
            )

            // Then: Category should be set correctly
            assertEquals("Category should match for $category", category, item.category)
        }
    }

    @Test
    fun `should handle quantity calculations correctly`() {
        // Test with different quantity types
        val quantities = listOf(0.5, 1.0, 2.5, 10.0, 100.0)

        for (quantity in quantities) {
            // When: Creating item with specific quantity
            val item = ShoppingListManager.ShoppingListItem(
                id = "test-$quantity",
                name = "Test Item",
                quantity = quantity,
                unit = "kg",
                category = "Test",
                addedFrom = "Test"
            )

            // Then: Quantity should be exact
            assertEquals("Quantity should match for $quantity", quantity, item.quantity, 0.001)
        }
    }

    @Test
    fun `should handle cost estimation scenarios`() {
        // Test with no cost estimation
        val noCostItem = ShoppingListManager.ShoppingListItem(
            id = "no-cost",
            name = "Test Item",
            quantity = 1.0,
            unit = "piece",
            category = "Test",
            addedFrom = "Test"
        )
        assertNull("No cost should be null", noCostItem.estimatedCost)

        // Test with cost estimation
        val costItem = ShoppingListManager.ShoppingListItem(
            id = "with-cost",
            name = "Test Item",
            quantity = 1.0,
            unit = "piece",
            category = "Test",
            addedFrom = "Test",
            estimatedCost = 5.99
        )
        assertEquals("Cost should match", 5.99, costItem.estimatedCost!!, 0.01)
    }

    @Test
    fun `should handle empty and non-empty notes`() {
        // Test with no notes
        val noNotesItem = ShoppingListManager.ShoppingListItem(
            id = "no-notes",
            name = "Test Item",
            quantity = 1.0,
            unit = "piece",
            category = "Test",
            addedFrom = "Test"
        )
        assertNull("Notes should be null when not provided", noNotesItem.notes)

        // Test with notes
        val notesItem = ShoppingListManager.ShoppingListItem(
            id = "with-notes",
            name = "Test Item",
            quantity = 1.0,
            unit = "piece",
            category = "Test",
            addedFrom = "Test",
            notes = "Special instructions"
        )
        assertEquals("Notes should match", "Special instructions", notesItem.notes)
    }

    @Test
    fun `should track item source correctly`() {
        val sources = listOf("Manual", "Protein Smoothie Recipe", "Weekly Meal Plan", "Bulk Shopping")

        for (source in sources) {
            // When: Creating item from different sources
            val item = ShoppingListManager.ShoppingListItem(
                id = "test-$source",
                name = "Test Item",
                quantity = 1.0,
                unit = "piece",
                category = "Test",
                addedFrom = source
            )

            // Then: Source should be tracked correctly
            assertEquals("Source should match for $source", source, item.addedFrom)
        }
    }
}