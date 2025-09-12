package com.example.fitapp.data.nutrition

/**
 * Data classes for nutrition and food items
 */

/**
 * Food item with complete nutrition information
 * Used for barcode scanning and food database lookups
 */
data class FoodItem(
    val id: Long = 0,
    val name: String,
    val brand: String = "",
    val barcode: String = "",
    val caloriesPer100g: Float,
    val proteinPer100g: Float = 0f,
    val carbsPer100g: Float = 0f,
    val fatPer100g: Float = 0f,
    val fiberPer100g: Float = 0f,
    val sugarPer100g: Float = 0f,
    val sodiumPer100g: Float = 0f, // in mg
    val category: String = "Unbekannt",
    val allergens: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val source: String = "Manual", // "Open Food Facts", "Local Database", "Manual"
    val createdAt: Long = System.currentTimeMillis(),
) {
    /**
     * Calculate nutrition values for a specific serving size
     */
    fun calculateNutrition(servingSizeGrams: Float): NutritionValues {
        val factor = servingSizeGrams / 100f
        return NutritionValues(
            calories = caloriesPer100g * factor,
            protein = proteinPer100g * factor,
            carbs = carbsPer100g * factor,
            fat = fatPer100g * factor,
            fiber = fiberPer100g * factor,
            sugar = sugarPer100g * factor,
            sodium = sodiumPer100g * factor,
        )
    }

    /**
     * Get display name with brand if available
     */
    fun getDisplayName(): String {
        return if (brand.isNotBlank()) {
            "$brand $name"
        } else {
            name
        }
    }
}

/**
 * Nutrition values for a specific serving
 */
data class NutritionValues(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f, // in mg
) {
    /**
     * Add nutrition values together
     */
    operator fun plus(other: NutritionValues): NutritionValues {
        return NutritionValues(
            calories = calories + other.calories,
            protein = protein + other.protein,
            carbs = carbs + other.carbs,
            fat = fat + other.fat,
            fiber = fiber + other.fiber,
            sugar = sugar + other.sugar,
            sodium = sodium + other.sodium,
        )
    }

    /**
     * Multiply nutrition values by a factor
     */
    operator fun times(factor: Float): NutritionValues {
        return NutritionValues(
            calories = calories * factor,
            protein = protein * factor,
            carbs = carbs * factor,
            fat = fat * factor,
            fiber = fiber * factor,
            sugar = sugar * factor,
            sodium = sodium * factor,
        )
    }
}
