package com.example.fitapp.network.openfoodfacts

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * OpenFoodFacts API interface for comprehensive food database integration
 *
 * Provides access to a global food product database with detailed nutritional information,
 * barcode scanning support, and multilingual product data.
 */
interface OpenFoodFactsApi {
    /**
     * Get product details by barcode
     *
     * @param barcode The product barcode (EAN-13, EAN-8, UPC-A, etc.)
     * @return ProductResponse containing detailed product information
     */
    @GET("/api/v0/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
    ): ProductResponse

    /**
     * Search products by text query
     *
     * @param query Search terms (product name, brand, ingredients, etc.)
     * @param simple Return simplified results (1) or full data (0)
     * @param json Return JSON format (1)
     * @param page Page number for pagination
     * @param pageSize Number of results per page (max 100)
     * @param country Country code for localized results (optional)
     * @param lang Language code for localized text (optional)
     * @return SearchResponse containing list of matching products
     */
    @GET("/cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("search_simple") simple: Int = 1,
        @Query("json") json: Int = 1,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("cc") country: String? = null,
        @Query("lc") lang: String? = null,
    ): SearchResponse

    /**
     * Search products by category
     *
     * @param category Category name (e.g., "beverages", "dairy", "meat")
     * @param page Page number for pagination
     * @param pageSize Number of results per page
     * @return SearchResponse containing products in the specified category
     */
    @GET("/cgi/search.pl")
    suspend fun searchByCategory(
        @Query("tagtype_0") tagType: String = "categories",
        @Query("tag_contains_0") tagContains: String = "contains",
        @Query("tag_0") category: String,
        @Query("json") json: Int = 1,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): SearchResponse
}
