package com.example.fitapp.util.performance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Performance optimization utilities for FitApp
 *
 * Provides intelligent caching, lazy loading, and memory management
 * for improved app performance and user experience.
 */

/**
 * Intelligent image cache with memory and disk storage
 */
class ImageCache private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ImageCache? = null

        fun getInstance(context: Context): ImageCache {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageCache(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val CACHE_SIZE_RATIO = 8 // 1/8 of available memory
        private const val DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
    }

    private val memoryCache: LruCache<String, Bitmap>
    private val diskCacheDir: File
    private val cacheScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Memory cache
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / CACHE_SIZE_RATIO

        memoryCache =
            object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(
                    key: String,
                    bitmap: Bitmap,
                ): Int {
                    return bitmap.byteCount / 1024
                }
            }

        // Disk cache
        diskCacheDir =
            File(context.cacheDir, "image_cache").apply {
                mkdirs()
            }

        // Clean old cache files on startup
        cleanOldCacheFiles()
    }

    /**
     * Get image from cache or load it asynchronously
     */
    suspend fun getImage(
        url: String,
        loader: suspend () -> Bitmap?,
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            val key = generateCacheKey(url)

            // Check memory cache first
            memoryCache.get(key)?.let { return@withContext it }

            // Check disk cache
            getDiskCachedImage(key)?.let { bitmap ->
                memoryCache.put(key, bitmap)
                return@withContext bitmap
            }

            // Load from network/source
            try {
                val bitmap = loader()
                bitmap?.let {
                    // Cache in memory and disk
                    memoryCache.put(key, it)
                    saveToDiskCache(key, it)
                }
                bitmap
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Preload images for better performance
     */
    fun preloadImages(
        urls: List<String>,
        loader: suspend (String) -> Bitmap?,
    ) {
        cacheScope.launch {
            urls.forEach { url ->
                try {
                    getImage(url) { loader(url) }
                } catch (e: Exception) {
                    // Ignore preload errors
                }
            }
        }
    }

    private fun generateCacheKey(url: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun getDiskCachedImage(key: String): Bitmap? {
        return try {
            val file = File(diskCacheDir, key)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToDiskCache(
        key: String,
        bitmap: Bitmap,
    ) {
        cacheScope.launch {
            try {
                val file = File(diskCacheDir, key)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
            } catch (e: Exception) {
                // Ignore disk cache errors
            }
        }
    }

    private fun cleanOldCacheFiles() {
        cacheScope.launch {
            try {
                val files = diskCacheDir.listFiles() ?: return@launch
                val currentTime = System.currentTimeMillis()
                val maxAge = 7 * 24 * 60 * 60 * 1000 // 7 days

                files.forEach { file ->
                    if (currentTime - file.lastModified() > maxAge) {
                        file.delete()
                    }
                }

                // If cache is too large, delete oldest files
                val totalSize = files.sumOf { it.length() }
                if (totalSize > DISK_CACHE_SIZE) {
                    files.sortedBy { it.lastModified() }.forEach { file ->
                        file.delete()
                        if (files.sumOf { it.length() } <= DISK_CACHE_SIZE * 0.8) {
                            return@forEach
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    fun clearCache() {
        memoryCache.evictAll()
        cacheScope.launch {
            diskCacheDir.deleteRecursively()
            diskCacheDir.mkdirs()
        }
    }
}

/**
 * Lazy data loader with caching and debouncing
 */
class LazyDataLoader<T>(
    private val cacheSize: Int = 100,
    private val debounceTimeMs: Long = 300,
) {
    private val cache = LruCache<String, T>(cacheSize)
    private val loadingJobs = ConcurrentHashMap<String, Deferred<T?>>()

    /**
     * Load data with automatic caching and deduplication
     */
    suspend fun loadData(
        key: String,
        loader: suspend () -> T?,
    ): T? =
        withContext(Dispatchers.IO) {
            // Check cache first
            cache.get(key)?.let { return@withContext it }

            // Check if already loading
            loadingJobs[key]?.let { return@withContext it.await() }

            // Start loading
            val deferred =
                async {
                    delay(debounceTimeMs) // Debounce rapid requests
                    try {
                        val data = loader()
                        data?.let { cache.put(key, it) }
                        data
                    } catch (e: Exception) {
                        null
                    } finally {
                        loadingJobs.remove(key)
                    }
                }

            loadingJobs[key] = deferred
            deferred.await()
        }

    fun clearCache() {
        cache.evictAll()
        loadingJobs.clear()
    }

    fun getCacheStats(): String {
        return "Cache size: ${cache.size()}/$cacheSize, Loading: ${loadingJobs.size}"
    }
}

/**
 * Pagination helper for large datasets
 */
class PaginationHelper<T>(
    private val pageSize: Int = 20,
    private val prefetchDistance: Int = 5,
) {
    private val cache = mutableMapOf<Int, List<T>>()
    private val loadingPages = mutableSetOf<Int>()

    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    suspend fun loadInitial(loader: suspend (page: Int, size: Int) -> List<T>) {
        _isLoading.value = true
        try {
            val firstPage = loader(0, pageSize)
            cache[0] = firstPage
            _items.value = firstPage
            _hasMore.value = firstPage.size == pageSize
        } catch (e: Exception) {
            _hasMore.value = false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadMore(loader: suspend (page: Int, size: Int) -> List<T>) {
        if (_isLoading.value || !_hasMore.value) return

        val nextPage = cache.size
        if (loadingPages.contains(nextPage)) return

        loadingPages.add(nextPage)
        _isLoading.value = true

        try {
            val newItems = loader(nextPage, pageSize)
            cache[nextPage] = newItems

            // Update items list
            val allItems = cache.toSortedMap().values.flatten()
            _items.value = allItems

            _hasMore.value = newItems.size == pageSize
        } catch (e: Exception) {
            _hasMore.value = false
        } finally {
            loadingPages.remove(nextPage)
            _isLoading.value = false
        }
    }

    fun shouldLoadMore(currentIndex: Int): Boolean {
        val totalItems = _items.value.size
        return currentIndex >= totalItems - prefetchDistance && _hasMore.value && !_isLoading.value
    }

    fun reset() {
        cache.clear()
        loadingPages.clear()
        _items.value = emptyList()
        _isLoading.value = false
        _hasMore.value = true
    }
}

/**
 * Memory-efficient data repository with smart caching
 */
class SmartRepository<K, V>(
    private val maxCacheSize: Int = 200,
    private val expirationTimeMs: Long = 30 * 60 * 1000, // 30 minutes
) {
    private data class CacheEntry<V>(
        val value: V,
        val timestamp: Long,
    )

    private val cache = LruCache<K, CacheEntry<V>>(maxCacheSize)
    private val loadingKeys = ConcurrentHashMap<K, Deferred<V?>>()

    suspend fun get(
        key: K,
        loader: suspend (K) -> V?,
    ): V? =
        withContext(Dispatchers.IO) {
            // Check cache
            cache.get(key)?.let { entry ->
                if (System.currentTimeMillis() - entry.timestamp < expirationTimeMs) {
                    return@withContext entry.value
                } else {
                    cache.remove(key) // Remove expired entry
                }
            }

            // Check if already loading
            loadingKeys[key]?.let { return@withContext it.await() }

            // Load data
            val deferred =
                async {
                    try {
                        val value = loader(key)
                        value?.let {
                            cache.put(key, CacheEntry(it, System.currentTimeMillis()))
                        }
                        value
                    } catch (e: Exception) {
                        null
                    } finally {
                        loadingKeys.remove(key)
                    }
                }

            loadingKeys[key] = deferred
            deferred.await()
        }

    suspend fun getMultiple(
        keys: List<K>,
        loader: suspend (List<K>) -> Map<K, V>,
    ): Map<K, V> =
        withContext(Dispatchers.IO) {
            val result = mutableMapOf<K, V>()
            val keysToLoad = mutableListOf<K>()

            // Check cache for each key
            keys.forEach { key ->
                cache.get(key)?.let { entry ->
                    if (System.currentTimeMillis() - entry.timestamp < expirationTimeMs) {
                        result[key] = entry.value
                    } else {
                        cache.remove(key)
                        keysToLoad.add(key)
                    }
                } ?: keysToLoad.add(key)
            }

            // Load missing keys
            if (keysToLoad.isNotEmpty()) {
                try {
                    val loadedData = loader(keysToLoad)
                    loadedData.forEach { (key, value) ->
                        cache.put(key, CacheEntry(value, System.currentTimeMillis()))
                        result[key] = value
                    }
                } catch (e: Exception) {
                    // Return partial results
                }
            }

            result
        }

    fun put(
        key: K,
        value: V,
    ) {
        cache.put(key, CacheEntry(value, System.currentTimeMillis()))
    }

    fun invalidate(key: K) {
        cache.remove(key)
    }

    fun invalidateAll() {
        cache.evictAll()
    }

    fun getStats(): String {
        return "Cache: ${cache.size()}/$maxCacheSize, Loading: ${loadingKeys.size}"
    }
}

/**
 * Batch processor for efficient database operations
 */
class BatchProcessor<T>(
    private val batchSize: Int = 50,
    private val flushIntervalMs: Long = 5000,
    private val processor: suspend (List<T>) -> Unit,
) {
    private val pending = mutableListOf<T>()
    private var lastFlush = System.currentTimeMillis()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun add(item: T) {
        synchronized(pending) {
            pending.add(item)

            if (pending.size >= batchSize || shouldFlush()) {
                flush()
            }
        }
    }

    fun addAll(items: List<T>) {
        synchronized(pending) {
            pending.addAll(items)

            if (pending.size >= batchSize || shouldFlush()) {
                flush()
            }
        }
    }

    fun flush() {
        val toProcess =
            synchronized(pending) {
                if (pending.isEmpty()) return

                val items = pending.toList()
                pending.clear()
                lastFlush = System.currentTimeMillis()
                items
            }

        scope.launch {
            try {
                // Process in smaller chunks if too large
                toProcess.chunked(batchSize).forEach { chunk ->
                    processor(chunk)
                }
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }

    private fun shouldFlush(): Boolean {
        return System.currentTimeMillis() - lastFlush > flushIntervalMs
    }

    fun close() {
        flush()
        scope.cancel()
    }
}
