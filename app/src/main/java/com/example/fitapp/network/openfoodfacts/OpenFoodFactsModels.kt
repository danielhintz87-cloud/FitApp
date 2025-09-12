package com.example.fitapp.network.openfoodfacts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models for OpenFoodFacts API responses
 */

@JsonClass(generateAdapter = true)
data class ProductResponse(
    @Json(name = "status") val status: Int,
    @Json(name = "status_verbose") val statusVerbose: String?,
    @Json(name = "product") val product: Product?,
)

@JsonClass(generateAdapter = true)
data class SearchResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "page_count") val pageCount: Int,
    @Json(name = "page_size") val pageSize: Int,
    @Json(name = "products") val products: List<Product>,
)

@JsonClass(generateAdapter = true)
data class Product(
    @Json(name = "code") val code: String?,
    @Json(name = "product_name") val productName: String?,
    @Json(name = "product_name_de") val productNameDe: String?,
    @Json(name = "brands") val brands: String?,
    @Json(name = "categories") val categories: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "image_front_url") val imageFrontUrl: String?,
    @Json(name = "image_front_small_url") val imageFrontSmallUrl: String?,
    @Json(name = "serving_size") val servingSize: String?,
    @Json(name = "serving_quantity") val servingQuantity: Double?,
    @Json(name = "quantity") val quantity: String?,
    @Json(name = "packaging") val packaging: String?,
    @Json(name = "labels") val labels: String?,
    @Json(name = "stores") val stores: String?,
    @Json(name = "countries") val countries: String?,
    @Json(name = "ingredients_text") val ingredientsText: String?,
    @Json(name = "ingredients_text_de") val ingredientsTextDe: String?,
    @Json(name = "allergens") val allergens: String?,
    @Json(name = "traces") val traces: String?,
    @Json(name = "nutriments") val nutriments: Nutriments?,
    @Json(name = "nutrition_grades") val nutritionGrades: String?,
    @Json(name = "nova_group") val novaGroup: Int?,
    @Json(name = "ecoscore_grade") val ecoscoreGrade: String?,
    @Json(name = "nutriscore_grade") val nutriscoreGrade: String?,
)

@JsonClass(generateAdapter = true)
data class Nutriments(
    // Energy values
    @Json(name = "energy-kcal_100g") val energyKcal100g: Double?,
    @Json(name = "energy-kj_100g") val energyKj100g: Double?,
    @Json(name = "energy_100g") val energy100g: Double?,
    // Macronutrients per 100g
    @Json(name = "carbohydrates_100g") val carbohydrates100g: Double?,
    @Json(name = "sugars_100g") val sugars100g: Double?,
    @Json(name = "fiber_100g") val fiber100g: Double?,
    @Json(name = "proteins_100g") val proteins100g: Double?,
    @Json(name = "fat_100g") val fat100g: Double?,
    @Json(name = "saturated-fat_100g") val saturatedFat100g: Double?,
    @Json(name = "trans-fat_100g") val transFat100g: Double?,
    @Json(name = "cholesterol_100g") val cholesterol100g: Double?,
    // Minerals per 100g
    @Json(name = "sodium_100g") val sodium100g: Double?,
    @Json(name = "salt_100g") val salt100g: Double?,
    @Json(name = "calcium_100g") val calcium100g: Double?,
    @Json(name = "iron_100g") val iron100g: Double?,
    @Json(name = "magnesium_100g") val magnesium100g: Double?,
    @Json(name = "potassium_100g") val potassium100g: Double?,
    @Json(name = "zinc_100g") val zinc100g: Double?,
    // Vitamins per 100g
    @Json(name = "vitamin-a_100g") val vitaminA100g: Double?,
    @Json(name = "vitamin-c_100g") val vitaminC100g: Double?,
    @Json(name = "vitamin-d_100g") val vitaminD100g: Double?,
    @Json(name = "vitamin-e_100g") val vitaminE100g: Double?,
    @Json(name = "vitamin-k_100g") val vitaminK100g: Double?,
    @Json(name = "vitamin-b1_100g") val vitaminB1100g: Double?,
    @Json(name = "vitamin-b2_100g") val vitaminB2100g: Double?,
    @Json(name = "vitamin-b6_100g") val vitaminB6100g: Double?,
    @Json(name = "vitamin-b12_100g") val vitaminB12100g: Double?,
    @Json(name = "folates_100g") val folates100g: Double?,
    // Per serving values (if available)
    @Json(name = "energy-kcal_serving") val energyKcalServing: Double?,
    @Json(name = "carbohydrates_serving") val carbohydratesServing: Double?,
    @Json(name = "proteins_serving") val proteinsServing: Double?,
    @Json(name = "fat_serving") val fatServing: Double?,
    @Json(name = "fiber_serving") val fiberServing: Double?,
    @Json(name = "sodium_serving") val sodiumServing: Double?,
)

/**
 * Extension functions to safely extract nutritional values
 */
fun Nutriments?.getCaloriesPer100g(): Int {
    return this?.energyKcal100g?.toInt() ?: 0
}

fun Nutriments?.getCarbsPer100g(): Float {
    return this?.carbohydrates100g?.toFloat() ?: 0f
}

fun Nutriments?.getProteinPer100g(): Float {
    return this?.proteins100g?.toFloat() ?: 0f
}

fun Nutriments?.getFatPer100g(): Float {
    return this?.fat100g?.toFloat() ?: 0f
}

fun Nutriments?.getFiberPer100g(): Float {
    return this?.fiber100g?.toFloat() ?: 0f
}

fun Nutriments?.getSugarsPer100g(): Float {
    return this?.sugars100g?.toFloat() ?: 0f
}

fun Nutriments?.getSodiumPer100g(): Float {
    return this?.sodium100g?.toFloat() ?: 0f
}

/**
 * Extension function to get display name in German or fallback to default
 */
fun Product.getDisplayName(): String {
    return productNameDe?.takeIf { it.isNotBlank() }
        ?: productName?.takeIf { it.isNotBlank() }
        ?: "Unbekanntes Produkt"
}

/**
 * Extension function to get localized ingredients text
 */
fun Product.getLocalizedIngredients(): String? {
    return ingredientsTextDe?.takeIf { it.isNotBlank() }
        ?: ingredientsText?.takeIf { it.isNotBlank() }
}
