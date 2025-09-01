package com.example.fitapp.network.openfoodfacts

import android.util.Log
import com.example.fitapp.data.entities.FoodItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

/**
 * Repository for OpenFoodFacts API integration with caching and error handling
 * 
 * Provides a clean interface for accessing the OpenFoodFacts database with
 * proper error handling, caching strategies, and conversion to app entities.
 */
class OpenFoodFactsRepository {
    
    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/"
        private const val TAG = "OpenFoodFactsRepository"
        private const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB cache
        private const val TIMEOUT_SECONDS = 30L
    }
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OpenFoodFactsApi::class.java)
    
    // Simple in-memory cache for recently fetched products
    private val productCache = mutableMapOf<String, Product>()
    private val searchCache = mutableMapOf<String, List<Product>>()
    
    /**
     * Search for products by barcode
     * 
     * @param barcode The product barcode
     * @return FoodItemEntity if found, null otherwise
     */
    suspend fun getProductByBarcode(barcode: String): FoodItemEntity? = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            productCache[barcode]?.let { cachedProduct ->
                Log.d(TAG, "Found product in cache for barcode: $barcode")
                return@withContext cachedProduct.toFoodItemEntity()
            }
            
            Log.d(TAG, "Fetching product for barcode: $barcode")
            val response = api.getProduct(barcode)
            
            if (response.status == 1 && response.product != null) {
                // Cache the result
                productCache[barcode] = response.product
                Log.d(TAG, "Successfully fetched product: ${response.product.getDisplayName()}")
                response.product.toFoodItemEntity()
            } else {
                Log.w(TAG, "Product not found for barcode: $barcode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product by barcode: $barcode", e)
            null
        }
    }
    
    /**
     * Search for products by text query
     * 
     * @param query Search terms
     * @param page Page number (1-based)
     * @param pageSize Number of results per page
     * @return List of FoodItemEntity
     */
    suspend fun searchProducts(
        query: String, 
        page: Int = 1, 
        pageSize: Int = 20
    ): List<FoodItemEntity> = withContext(Dispatchers.IO) {
        try {
            val cacheKey = "$query:$page:$pageSize"
            
            // Check cache first
            searchCache[cacheKey]?.let { cachedResults ->
                Log.d(TAG, "Found search results in cache for: $query")
                return@withContext cachedResults.mapNotNull { it.toFoodItemEntity() }
            }
            
            Log.d(TAG, "Searching products for: $query")
            val response = api.searchProducts(
                query = query,
                page = page,
                pageSize = pageSize,
                country = "de", // German products preferred
                lang = "de"     // German language
            )
            
            // Cache search results
            searchCache[cacheKey] = response.products
            
            Log.d(TAG, "Found ${response.products.size} products for query: $query")
            response.products.mapNotNull { product ->
                product.toFoodItemEntity()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching products for: $query", e)
            emptyList()
        }
    }
    
    /**
     * Search products by category
     * 
     * @param category Category name (e.g., "beverages", "dairy")
     * @param page Page number
     * @param pageSize Number of results per page
     * @return List of FoodItemEntity
     */
    suspend fun searchByCategory(
        category: String,
        page: Int = 1,
        pageSize: Int = 20
    ): List<FoodItemEntity> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching products by category: $category")
            val response = api.searchByCategory(
                category = category,
                page = page,
                pageSize = pageSize
            )
            
            Log.d(TAG, "Found ${response.products.size} products in category: $category")
            response.products.mapNotNull { product ->
                product.toFoodItemEntity()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching by category: $category", e)
            emptyList()
        }
    }
    
    /**
     * Clear the cache to free memory
     */
    fun clearCache() {
        productCache.clear()
        searchCache.clear()
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Get cache statistics for debugging
     */
    fun getCacheStats(): String {
        return "Product cache: ${productCache.size} items, Search cache: ${searchCache.size} queries"
    }
}

/**
 * Extension function to convert OpenFoodFacts Product to FoodItemEntity
 */
private fun Product.toFoodItemEntity(): FoodItemEntity? {
    return try {
        FoodItemEntity(
            id = code ?: "",
            name = getDisplayName(),
            barcode = code,
            calories = nutriments.getCaloriesPer100g(),
            carbs = nutriments.getCarbsPer100g(),
            protein = nutriments.getProteinPer100g(),
            fat = nutriments.getFatPer100g(),
            fiber = nutriments.getFiberPer100g(),
            sugar = nutriments.getSugarsPer100g(),
            sodium = nutriments.getSodiumPer100g(),
            brands = brands,
            categories = categories,
            imageUrl = imageFrontSmallUrl ?: imageFrontUrl ?: imageUrl,
            servingSize = servingSize,
            ingredients = getLocalizedIngredients()
        )
    } catch (e: Exception) {
        Log.e("OpenFoodFactsRepository", "Error converting product to FoodItemEntity: ${getDisplayName()}", e)
        null
    }
}