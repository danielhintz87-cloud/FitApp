package com.example.fitapp.services

/**
 * Shopping List Sorting and Grouping Strategies
 * Provides different ways to organize shopping list items
 */
object ShoppingListSorter {
    /**
     * Available sorting modes
     */
    enum class SortingMode {
        SUPERMARKET_LAYOUT, // Nach typischem Supermarkt-Layout
        ALPHABETICAL, // Alphabetisch nach Namen
        CATEGORY_ALPHABETICAL, // Nach Kategorie, dann alphabetisch
        PRIORITY, // Nach Priorität (urgent zuerst)
        RECENTLY_ADDED, // Zuletzt hinzugefügt zuerst
        STATUS, // Gekauft/Nicht gekauft
    }

    /**
     * Supermarket layout order - realistic German supermarket flow
     */
    private val supermarketLayout =
        mapOf(
            // Entrance area
            "Obst & Gemüse" to 1,
            "Frische Kräuter" to 2,
            "Nüsse & Trockenfrüchte" to 3,
            // Fresh sections
            "Bäckerei" to 4,
            "Fleisch & Wurst" to 5,
            "Fisch & Meeresfrüchte" to 6,
            "Milchprodukte" to 7,
            "Eier" to 8,
            "Käse" to 9,
            // Pantry aisles
            "Getreide & Müsli" to 10,
            "Nudeln & Reis" to 11,
            "Konserven" to 12,
            "Soßen & Dressings" to 13,
            "Öl & Essig" to 14,
            "Gewürze & Kräuter" to 15,
            "Süßwaren & Snacks" to 16,
            "Backzutaten" to 17,
            // Frozen section
            "Tiefkühl" to 18,
            "Eis" to 19,
            // Beverages
            "Wasser & Erfrischungsgetränke" to 20,
            "Säfte" to 21,
            "Kaffee & Tee" to 22,
            "Alkoholische Getränke" to 23,
            // Non-food
            "Haushalt & Reinigung" to 24,
            "Körperpflege" to 25,
            "Baby & Kind" to 26,
            "Sonstiges" to 27,
        )

    /**
     * Enhanced ingredient categorization
     */
    private val ingredientCategories =
        mapOf(
            // Obst & Gemüse
            "äpfel" to "Obst & Gemüse", "bananen" to "Obst & Gemüse", "tomaten" to "Obst & Gemüse",
            "kartoffeln" to "Obst & Gemüse", "zwiebeln" to "Obst & Gemüse", "möhren" to "Obst & Gemüse",
            "paprika" to "Obst & Gemüse", "gurken" to "Obst & Gemüse", "salat" to "Obst & Gemüse",
            "zitronen" to "Obst & Gemüse", "orangen" to "Obst & Gemüse", "brokkoli" to "Obst & Gemüse",
            "spinat" to "Obst & Gemüse", "champignons" to "Obst & Gemüse", "avocado" to "Obst & Gemüse",
            // Fleisch & Wurst
            "hähnchen" to "Fleisch & Wurst", "rind" to "Fleisch & Wurst", "schwein" to "Fleisch & Wurst",
            "hackfleisch" to "Fleisch & Wurst", "würstchen" to "Fleisch & Wurst", "salami" to "Fleisch & Wurst",
            "schinken" to "Fleisch & Wurst", "speck" to "Fleisch & Wurst",
            // Fisch & Meeresfrüchte
            "lachs" to "Fisch & Meeresfrüchte", "thunfisch" to "Fisch & Meeresfrüchte",
            "garnelen" to "Fisch & Meeresfrüchte", "kabeljau" to "Fisch & Meeresfrüchte",
            // Milchprodukte
            "milch" to "Milchprodukte", "butter" to "Milchprodukte", "joghurt" to "Milchprodukte",
            "quark" to "Milchprodukte", "sahne" to "Milchprodukte", "schmand" to "Milchprodukte",
            "crème fraîche" to "Milchprodukte", "mascarpone" to "Milchprodukte",
            // Eier
            "eier" to "Eier", "ei" to "Eier",
            // Käse
            "käse" to "Käse", "gouda" to "Käse", "emmental" to "Käse", "mozzarella" to "Käse",
            "parmesan" to "Käse", "feta" to "Käse", "camembert" to "Käse",
            // Getreide & Müsli
            "haferflocken" to "Getreide & Müsli", "müsli" to "Getreide & Müsli",
            "cornflakes" to "Getreide & Müsli", "quinoa" to "Getreide & Müsli",
            // Nudeln & Reis
            "nudeln" to "Nudeln & Reis", "spaghetti" to "Nudeln & Reis", "reis" to "Nudeln & Reis",
            "penne" to "Nudeln & Reis", "fusilli" to "Nudeln & Reis", "basmati" to "Nudeln & Reis",
            // Bäckerei
            "brot" to "Bäckerei", "brötchen" to "Bäckerei", "toast" to "Bäckerei",
            "croissant" to "Bäckerei", "bagel" to "Bäckerei",
            // Konserven
            "tomaten dose" to "Konserven", "bohnen dose" to "Konserven", "mais dose" to "Konserven",
            "thunfisch dose" to "Konserven", "kokosmilch" to "Konserven",
            // Gewürze & Kräuter
            "salz" to "Gewürze & Kräuter", "pfeffer" to "Gewürze & Kräuter", "paprika pulver" to "Gewürze & Kräuter",
            "oregano" to "Gewürze & Kräuter", "basilikum" to "Gewürze & Kräuter", "thymian" to "Gewürze & Kräuter",
            "petersilie" to "Frische Kräuter", "schnittlauch" to "Frische Kräuter", "dill" to "Frische Kräuter",
            // Öl & Essig
            "olivenöl" to "Öl & Essig", "sonnenblumenöl" to "Öl & Essig", "balsamico" to "Öl & Essig",
            "essig" to "Öl & Essig", "sesamöl" to "Öl & Essig",
            // Soßen & Dressings
            "ketchup" to "Soßen & Dressings", "senf" to "Soßen & Dressings", "mayo" to "Soßen & Dressings",
            "sojasauce" to "Soßen & Dressings", "worcestershire" to "Soßen & Dressings",
            // Backzutaten
            "mehl" to "Backzutaten", "zucker" to "Backzutaten", "backpulver" to "Backzutaten",
            "vanillezucker" to "Backzutaten", "hefe" to "Backzutaten",
            // Süßwaren & Snacks
            "schokolade" to "Süßwaren & Snacks", "chips" to "Süßwaren & Snacks", "kekse" to "Süßwaren & Snacks",
            "nüsse" to "Nüsse & Trockenfrüchte", "mandeln" to "Nüsse & Trockenfrüchte",
            // Tiefkühl
            "erbsen tk" to "Tiefkühl", "spinat tk" to "Tiefkühl", "pizza" to "Tiefkühl",
            "pommes" to "Tiefkühl", "eis" to "Eis",
            // Getränke
            "wasser" to "Wasser & Erfrischungsgetränke", "sprudel" to "Wasser & Erfrischungsgetränke",
            "cola" to "Wasser & Erfrischungsgetränke", "apfelsaft" to "Säfte", "orangensaft" to "Säfte",
            "kaffee" to "Kaffee & Tee", "tee" to "Kaffee & Tee", "bier" to "Alkoholische Getränke",
            "wein" to "Alkoholische Getränke",
            // Haushalt
            "spülmittel" to "Haushalt & Reinigung", "toilettenpapier" to "Haushalt & Reinigung",
            "waschmittel" to "Haushalt & Reinigung", "küchenrollen" to "Haushalt & Reinigung",
            // Körperpflege
            "zahnpasta" to "Körperpflege", "shampoo" to "Körperpflege", "seife" to "Körperpflege",
            "deodorant" to "Körperpflege",
        )

    /**
     * Categorize ingredient by name
     */
    fun categorizeIngredient(name: String): String {
        val lowerName = name.lowercase()

        // Direct match first
        ingredientCategories[lowerName]?.let { return it }

        // Partial matches
        for ((keyword, category) in ingredientCategories) {
            if (lowerName.contains(keyword) || keyword.contains(lowerName)) {
                return category
            }
        }

        return "Sonstiges"
    }

    /**
     * Get supermarket order for category
     */
    fun getSupermarketOrder(category: String): Int {
        return supermarketLayout[category] ?: 999
    }

    /**
     * Sort shopping list items by selected mode
     */
    fun sortItems(
        items: List<ShoppingListManager.ShoppingListItem>,
        mode: SortingMode,
    ): Map<String, List<ShoppingListManager.ShoppingListItem>> {
        return when (mode) {
            SortingMode.SUPERMARKET_LAYOUT -> sortBySupermarketLayout(items)
            SortingMode.ALPHABETICAL -> sortAlphabetically(items)
            SortingMode.CATEGORY_ALPHABETICAL -> sortByCategoryAlphabetical(items)
            SortingMode.PRIORITY -> sortByPriority(items)
            SortingMode.RECENTLY_ADDED -> sortByRecentlyAdded(items)
            SortingMode.STATUS -> sortByStatus(items)
        }
    }

    private fun sortBySupermarketLayout(
        items: List<ShoppingListManager.ShoppingListItem>,
    ): Map<String, List<ShoppingListManager.ShoppingListItem>> {
        return items
            .groupBy { categorizeIngredient(it.name) }
            .toSortedMap(compareBy { getSupermarketOrder(it) })
            .mapValues { (_, items) ->
                items.sortedWith(
                    compareBy<ShoppingListManager.ShoppingListItem> { it.isPurchased }
                        .thenBy { it.name },
                )
            }
    }

    private fun sortAlphabetically(
        items: List<ShoppingListManager.ShoppingListItem>,
    ): Map<String, List<ShoppingListManager.ShoppingListItem>> {
        val sorted =
            items.sortedWith(
                compareBy<ShoppingListManager.ShoppingListItem> { it.isPurchased }
                    .thenBy { it.name },
            )
        return mapOf("Alle Artikel (A-Z)" to sorted)
    }

    private fun sortByCategoryAlphabetical(
        items: List<ShoppingListManager.ShoppingListItem>,
    ): Map<String, List<ShoppingListManager.ShoppingListItem>> {
        return items
            .groupBy { categorizeIngredient(it.name) }
            .toSortedMap() // Kategorien alphabetisch
            .mapValues { (_, items) ->
                items.sortedWith(
                    compareBy<ShoppingListManager.ShoppingListItem> { it.isPurchased }
                        .thenBy { it.name },
                )
            }
    }

    private fun sortByPriority(
        items: List<ShoppingListManager.ShoppingListItem>,
    ): Map<String, List<ShoppingListManager.ShoppingListItem>> {
        val grouped =
            items.groupBy {
                when {
                    it.isPurchased -> "✅ Erledigt"
                    it.priority == ShoppingListManager.Priority.URGENT -> "🔴 Dringend"
                    it.priority == ShoppingListManager.Priority.HIGH -> "🟡 Wichtig"
                    else -> "⚪ Normal"
                }
            }

        // Custom order for priority groups
        val priorityOrder = listOf("🔴 Dringend", "🟡 Wichtig", "⚪ Normal", "✅ Erledigt")
        return priorityOrder.mapNotNull { key ->
            grouped[key]?.let { items ->
                key to items.sortedBy { it.name }
            }
        }.toMap()
    }

    private fun sortByRecentlyAdded(
        items: List<ShoppingListManager.ShoppingListItem>,
    ): Map<String, List<ShoppingListManager.ShoppingListItem>> {
        val sorted =
            items.sortedWith(
                compareBy<ShoppingListManager.ShoppingListItem> { it.isPurchased }
                    .thenByDescending { it.id }, // ID ist timestamp-based
            )
        return mapOf("Zuletzt hinzugefügt" to sorted)
    }

    private fun sortByStatus(
        items: List<ShoppingListManager.ShoppingListItem>,
    ): Map<String, List<ShoppingListManager.ShoppingListItem>> {
        return items.groupBy {
            if (it.isPurchased) "✅ Erledigt" else "📋 Zu kaufen"
        }.mapValues { (_, items) ->
            items.sortedBy { it.name }
        }
    }
}
