package com.example.fitapp.ui.nutrition

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit test for the enhanced recipe module functionality
 */
class EnhancedRecipeUnitTest {

    @Test
    fun testIngredientParsing() {
        // Test the ingredient parsing helper functions
        val markdown = """
            ## Zutaten
            - 200g Quinoa
            - 150g Hähnchenbrust
            - 100g Brokkoli
            - 1 Avocado
            
            ## Zubereitung
            Alle Zutaten verarbeiten.
        """.trimIndent()

        val ingredients = parseIngredientsFromMarkdown(markdown, 2)
        
        assertTrue("Should parse ingredients from markdown", ingredients.isNotEmpty())
        assertTrue("Should contain Quinoa", ingredients.any { it.contains("Quinoa") })
        assertTrue("Should contain Hähnchenbrust", ingredients.any { it.contains("Hähnchenbrust") })
        assertEquals("Should have 4 ingredients", 4, ingredients.size)
    }

    @Test
    fun testServingAdjustment() {
        // Test ingredient quantity adjustment for different serving sizes
        val ingredient = "200g Mehl"
        val adjusted = adjustIngredientQuantity(ingredient, 3)
        
        assertTrue("Should adjust quantity for 3 servings: $adjusted", adjusted.contains("600"))
        
        // Test with no numbers
        val noNumber = "Salz nach Geschmack"
        val adjustedNoNumber = adjustIngredientQuantity(noNumber, 2)
        assertEquals("Should not change ingredient without numbers", noNumber, adjustedNoNumber)
    }

    @Test
    fun testTagBuilding() {
        // Test tag building from category and diet type
        val tags1 = buildTags("Hauptgericht", "Vegetarisch")
        assertTrue("Should contain category", tags1.contains("hauptgericht"))
        assertTrue("Should contain diet type", tags1.contains("vegetarian"))
        
        val tags2 = buildTags("Dessert", "Vegan")
        assertTrue("Should contain dessert", tags2.contains("dessert"))
        assertTrue("Should contain vegan", tags2.contains("vegan"))
        
        val tags3 = buildTags("", "Low-Carb")
        assertTrue("Should contain low-carb", tags3.contains("low-carb"))
        assertFalse("Should not contain empty category", tags3.contains(","))
    }

    @Test
    fun testStepParsing() {
        val markdown = """
            ## Zubereitung
            
            ### Schritt 1: Vorbereitung
            Quinoa waschen und abtropfen lassen.
            
            ### Schritt 2: Kochen
            Quinoa in einem Topf mit Wasser kochen.
            Etwa 15 Minuten köcheln lassen.
            
            ### Schritt 3: Anrichten
            Servieren und genießen.
        """.trimIndent()
        
        val steps = parseStepsFromMarkdown(markdown)
        
        assertTrue("Should parse steps from markdown", steps.isNotEmpty())
        assertTrue("Should have multiple steps", steps.size >= 3)
        assertTrue("First step should contain preparation info", 
            steps.any { it.contains("Quinoa waschen") })
    }

    @Test
    fun testMarkdownBuilding() {
        val ingredients = listOf("200g Quinoa", "150g Hähnchenbrust", "100g Brokkoli")
        val instructions = listOf(
            "Quinoa waschen und kochen", 
            "Hähnchen braten", 
            "Brokkoli dämpfen und servieren"
        )
        
        val markdown = buildMarkdown(ingredients, instructions)
        
        assertTrue("Should contain ingredients section", markdown.contains("## Zutaten"))
        assertTrue("Should contain instructions section", markdown.contains("## Zubereitung"))
        assertTrue("Should contain all ingredients", 
            ingredients.all { ingredient -> markdown.contains(ingredient) })
        assertTrue("Should contain all instructions", 
            instructions.all { instruction -> markdown.contains(instruction) })
    }

    @Test
    fun testIngredientJsonBuilding() {
        val ingredients = listOf("200g Quinoa", "150g Hähnchenbrust", "100g Brokkoli")
        val json = buildIngredientsJson(ingredients)
        
        assertTrue("Should start with bracket", json.startsWith("["))
        assertTrue("Should end with bracket", json.endsWith("]"))
        assertTrue("Should contain all ingredients", 
            ingredients.all { ingredient -> json.contains("\"$ingredient\"") })
    }
}

// Helper functions for testing (extracted from the UI files)
private fun parseIngredientsFromMarkdown(markdown: String, servings: Int): List<String> {
    val lines = markdown.lines()
    val ingredients = mutableListOf<String>()
    var inIngredientsSection = false
    
    for (line in lines) {
        when {
            line.contains("Zutaten", ignoreCase = true) -> {
                inIngredientsSection = true
            }
            line.startsWith("##") && inIngredientsSection -> {
                break
            }
            inIngredientsSection && (line.startsWith("- ") || line.startsWith("* ")) -> {
                ingredients.add(line.substring(2).trim())
            }
        }
    }
    
    return ingredients.map { adjustIngredientQuantity(it, servings) }
}

private fun parseStepsFromMarkdown(markdown: String): List<String> {
    val lines = markdown.lines()
    val steps = mutableListOf<String>()
    var inInstructionsSection = false
    var currentStep = ""
    
    for (line in lines) {
        when {
            line.contains("Zubereitung", ignoreCase = true) -> {
                inInstructionsSection = true
            }
            line.startsWith("###") && inInstructionsSection -> {
                if (currentStep.isNotBlank()) {
                    steps.add(currentStep.trim())
                }
                currentStep = line.substring(3).trim()
            }
            inInstructionsSection && line.trim().isNotEmpty() && !line.startsWith("#") -> {
                currentStep += " " + line.trim()
            }
        }
    }
    
    if (currentStep.isNotBlank()) {
        steps.add(currentStep.trim())
    }
    
    return steps
}

private fun adjustIngredientQuantity(ingredient: String, servings: Int): String {
    val numbers = Regex("\\d+").findAll(ingredient).map { it.value.toInt() }.toList()
    if (numbers.isNotEmpty() && servings != 1) {
        var adjusted = ingredient
        numbers.forEach { number ->
            val newAmount = number * servings
            adjusted = adjusted.replaceFirst(number.toString(), newAmount.toString())
        }
        return adjusted
    }
    return ingredient
}

private fun buildTags(category: String, dietType: String): String {
    val tags = mutableListOf<String>()
    
    if (category.isNotBlank()) {
        tags.add(category.lowercase())
    }
    
    when (dietType.lowercase()) {
        "vegetarisch" -> tags.add("vegetarian")
        "vegan" -> tags.add("vegan")
        "low-carb" -> tags.add("low-carb")
        "high-protein" -> tags.add("high-protein")
    }
    
    return tags.joinToString(",")
}

private fun buildMarkdown(ingredients: List<String>, instructions: List<String>): String {
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