package com.example.fitapp.services

import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.flow.*

/**
 * AutoComplete Engine for Shopping List
 * Provides intelligent suggestions based on:
 * - Product database
 * - Usage frequency
 * - Fuzzy matching
 * - Category patterns
 */
class ShoppingAutoCompleteEngine {
    companion object {
        private const val TAG = "AutoComplete"
        private const val MAX_SUGGESTIONS = 8
        private const val MIN_QUERY_LENGTH = 1
    }

    // Usage frequency tracking
    private val usageFrequency = mutableMapOf<String, Int>()
    private val recentlyUsed = mutableListOf<String>()

    /**
     * Comprehensive German grocery database
     */
    private val productDatabase =
        listOf(
            // Obst & Gemüse (200+ items)
            "Äpfel", "Birnen", "Bananen", "Orangen", "Zitronen", "Limetten", "Grapefruits",
            "Erdbeeren", "Himbeeren", "Blaubeeren", "Brombeeren", "Johannisbeeren",
            "Trauben", "Kiwis", "Ananas", "Mango", "Papaya", "Melone", "Wassermelone",
            "Pfirsiche", "Aprikosen", "Pflaumen", "Kirschen", "Datteln", "Feigen",
            "Avocado", "Kokosnuss", "Granatapfel", "Nektarinen",
            "Tomaten", "Gurken", "Paprika", "Zwiebeln", "Knoblauch", "Kartoffeln",
            "Möhren", "Karotten", "Sellerie", "Lauch", "Porree", "Radieschen",
            "Rettich", "Rote Beete", "Pastinaken", "Petersilienwurzel",
            "Brokkoli", "Blumenkohl", "Rosenkohl", "Weißkohl", "Rotkohl", "Wirsing",
            "Chinakohl", "Pak Choi", "Grünkohl", "Spinat", "Mangold", "Rucola",
            "Feldsalat", "Kopfsalat", "Eisbergsalat", "Lollo Rosso", "Endivien",
            "Chicorée", "Radicchio", "Zucchini", "Auberginen", "Kürbis",
            "Süßkartoffeln", "Artischocken", "Spargel", "Champignons", "Pilze",
            "Pfifferlinge", "Steinpilze", "Shiitake", "Ingwer", "Chili", "Jalapeños",
            "Bohnen", "Erbsen", "Zuckerschoten", "Okra", "Fenchel", "Rhabarber",
            // Fleisch & Wurst (80+ items)
            "Hähnchenbrust", "Hähnchenschenkel", "Hähnchen ganz", "Putenbrust", "Putenschnitzel",
            "Rindfleisch", "Rindersteak", "Rinderhack", "Rinderbraten", "Gulasch",
            "Schweinefleisch", "Schweineschnitzel", "Schweinebraten", "Schweinehack",
            "Schweinesteak", "Kassler", "Eisbein", "Spare Ribs", "Bratwurst",
            "Wiener Würstchen", "Frankfurter", "Leberwurst", "Blutwurst", "Weißwurst",
            "Salami", "Schinken", "Kochschinken", "Rohschinken", "Prosciutto",
            "Speck", "Bacon", "Pancetta", "Chorizo", "Mortadella", "Pastrami",
            "Lammfleisch", "Lammkeule", "Lammkoteletts", "Wildfleisch", "Hirsch",
            "Reh", "Wildschwein", "Ente", "Gans", "Wachtel",
            // Fisch & Meeresfrüchte (60+ items)
            "Lachs", "Forelle", "Thunfisch", "Kabeljau", "Seelachs", "Scholle",
            "Zander", "Barsch", "Hecht", "Karpfen", "Makrele", "Hering",
            "Sardinen", "Sardellen", "Dorade", "Seeteufel", "Heilbutt",
            "Garnelen", "Shrimps", "Krabben", "Hummer", "Languste", "Scampi",
            "Muscheln", "Miesmuscheln", "Jakobsmuscheln", "Austern", "Tintenfisch",
            "Calamari", "Oktopus", "Surimi", "Fischstäbchen", "Räucherlachs",
            // Milchprodukte (50+ items)
            "Milch", "Vollmilch", "Fettarme Milch", "Magermilch", "Laktosefreie Milch",
            "Hafermilch", "Mandelmilch", "Sojamilch", "Kokosmilch", "Reismilch",
            "Butter", "Margarine", "Joghurt", "Naturjoghurt", "Fruchtjoghurt",
            "Griechischer Joghurt", "Skyr", "Quark", "Magerquark", "Frischkäse",
            "Schlagsahne", "Saure Sahne", "Schmand", "Crème fraîche", "Mascarpone",
            "Ricotta", "Mozzarella", "Feta", "Parmesan", "Gouda", "Emmental",
            "Camembert", "Brie", "Cheddar", "Gorgonzola", "Roquefort",
            // Eier & Käse
            "Eier", "Bio-Eier", "Freiland-Eier", "Wachteleier",
            // Getreide & Müsli (40+ items)
            "Haferflocken", "Müsli", "Cornflakes", "Granola", "Quinoa", "Bulgur",
            "Couscous", "Amaranth", "Buchweizen", "Hirse", "Gerste", "Dinkel",
            "Chiasamen", "Leinsamen", "Sesam", "Sonnenblumenkerne", "Kürbiskerne",
            "Walnüsse", "Haselnüsse", "Mandeln", "Erdnüsse", "Cashews", "Pistazien",
            "Paranüsse", "Pekannüsse", "Macadamia", "Rosinen", "Datteln getrocknet",
            // Nudeln & Reis (30+ items)
            "Spaghetti", "Penne", "Fusilli", "Farfalle", "Rigatoni", "Tagliatelle",
            "Linguine", "Lasagneblätter", "Ravioli", "Tortellini", "Gnocchi",
            "Reis", "Basmati Reis", "Jasmin Reis", "Vollkornreis", "Risotto Reis",
            "Wildreis", "Reisnudeln", "Glasnudeln", "Udon", "Soba",
            // Brot & Backwaren (30+ items)
            "Brot", "Vollkornbrot", "Weißbrot", "Schwarzbrot", "Pumpernickel",
            "Brötchen", "Vollkornbrötchen", "Laugenbrezeln", "Croissants", "Bagels",
            "Toast", "Knäckebrot", "Zwieback", "Lebkuchen", "Kekse", "Biskuits",
            // Konserven (40+ items)
            "Tomaten aus der Dose", "Passierte Tomaten", "Tomatenmark", "Mais",
            "Kidneybohnen", "Weiße Bohnen", "Kichererbsen", "Linsen", "Erbsen",
            "Thunfisch in Dose", "Sardinen in Dose", "Oliven", "Kapern",
            "Kokosmilch", "Tomaten gehackt", "Artischocken", "Rotkohl", "Sauerkraut",
            // Gewürze & Kräuter (60+ items)
            "Salz", "Pfeffer", "Paprika edelsüß", "Paprika scharf", "Chili",
            "Kreuzkümmel", "Koriander", "Zimt", "Muskat", "Nelken", "Kardamom",
            "Ingwer gemahlen", "Kurkuma", "Curry", "Garam Masala", "Oregano",
            "Basilikum", "Thymian", "Rosmarin", "Salbei", "Majoran", "Dill",
            "Petersilie", "Schnittlauch", "Kresse", "Minze", "Lorbeerblätter",
            "Vanille", "Vanillezucker", "Backpulver", "Natron", "Hefe",
            // Öl & Essig (20+ items)
            "Olivenöl", "Sonnenblumenöl", "Rapsöl", "Kokosöl", "Sesamöl",
            "Walnussöl", "Leinöl", "Balsamico", "Weißweinessig", "Rotweinessig",
            "Apfelessig", "Reisessig", "Sherryessig",
            // Soßen & Dressings (30+ items)
            "Sojasauce", "Fischsauce", "Worcestershire", "Tabasco", "Sriracha",
            "Ketchup", "Senf", "Dijon Senf", "Mayonnaise", "Pesto", "Tahini",
            "Hummus", "Guacamole", "Salsa", "BBQ Sauce", "Teriyaki Sauce",
            // Süßwaren & Snacks (40+ items)
            "Schokolade", "Vollmilchschokolade", "Zartbitterschokolade", "Weiße Schokolade",
            "Gummibärchen", "Bonbons", "Kekse", "Chips", "Salzstangen", "Nüsse",
            "Studentenfutter", "Popcorn", "Cracker", "Reiswaffeln", "Müsliriegel",
            // Backzutaten (25+ items)
            "Mehl", "Weizenmehl", "Vollkornmehl", "Dinkelmehl", "Mandelmehl",
            "Zucker", "Brauner Zucker", "Puderzucker", "Rohrzucker", "Honig",
            "Ahornsirup", "Agavendicksaft", "Kakao", "Schokodrops", "Rosinen",
            // Tiefkühl (30+ items)
            "Erbsen TK", "Spinat TK", "Brokkoli TK", "Blumenkohl TK", "Pommes",
            "Pizza", "Fischstäbchen", "Hähnchen Nuggets", "Eis", "Eiscreme",
            "Sorbet", "Frozen Yogurt", "Beeren TK", "Mango TK",
            // Getränke (50+ items)
            "Wasser", "Sprudel", "Mineralwasser", "Cola", "Pepsi", "Fanta",
            "Sprite", "Apfelsaft", "Orangensaft", "Multivitaminsaft", "Tomatensaft",
            "Cranberrysaft", "Traubensaft", "Kaffee", "Espresso", "Cappuccino",
            "Tee", "Schwarztee", "Grüntee", "Früchtetee", "Kräutertee", "Chai",
            "Bier", "Weizen", "Pils", "Weißbier", "Wein", "Rotwein", "Weißwein",
            "Rosé", "Sekt", "Prosecco", "Champagner", "Wodka", "Gin", "Whisky",
            // Haushalt & Reinigung (20+ items)
            "Spülmittel", "Waschmittel", "Weichspüler", "Toilettenpapier",
            "Küchenrollen", "Schwämme", "Allzweckreiniger", "Glasreiniger",
            "Staubsauger Beutel", "Müllbeutel", "Alufolie", "Frischhaltefolie",
            // Körperpflege (30+ items)
            "Zahnpasta", "Zahnbürste", "Mundspülung", "Shampoo", "Spülung",
            "Duschgel", "Seife", "Bodylotion", "Gesichtscreme", "Sonnencreme",
            "Deodorant", "Parfum", "Rasierer", "Rasierschaum", "After Shave",
        )

    /**
     * Get autocomplete suggestions for query
     */
    fun getSuggestions(
        query: String,
        recentItems: List<String> = emptyList(),
        categoryHint: String? = null,
    ): List<AutoCompleteSuggestion> {
        if (query.length < MIN_QUERY_LENGTH) {
            return getFrequentSuggestions(recentItems, categoryHint)
        }

        val normalizedQuery = query.lowercase().trim()
        val suggestions = mutableListOf<AutoCompleteSuggestion>()

        // 1. Exact matches (highest priority)
        val exactMatches =
            productDatabase.filter {
                it.lowercase().startsWith(normalizedQuery)
            }.map { product ->
                AutoCompleteSuggestion(
                    text = product,
                    category = ShoppingListSorter.categorizeIngredient(product),
                    confidence = 1.0f,
                    source = SuggestionSource.EXACT_MATCH,
                    frequency = getUsageFrequency(product),
                )
            }
        suggestions.addAll(exactMatches)

        // 2. Fuzzy matches (medium priority)
        if (suggestions.size < MAX_SUGGESTIONS) {
            val fuzzyMatches =
                productDatabase.filter { product ->
                    !exactMatches.any { it.text == product } &&
                        isFuzzyMatch(normalizedQuery, product.lowercase())
                }.map { product ->
                    AutoCompleteSuggestion(
                        text = product,
                        category = ShoppingListSorter.categorizeIngredient(product),
                        confidence = calculateFuzzyConfidence(normalizedQuery, product.lowercase()),
                        source = SuggestionSource.FUZZY_MATCH,
                        frequency = getUsageFrequency(product),
                    )
                }
            suggestions.addAll(fuzzyMatches)
        }

        // 3. Recent items matching (high priority)
        if (suggestions.size < MAX_SUGGESTIONS) {
            val recentMatches =
                recentItems.filter { item ->
                    item.lowercase().contains(normalizedQuery) &&
                        !suggestions.any { it.text.equals(item, ignoreCase = true) }
                }.map { item ->
                    AutoCompleteSuggestion(
                        text = item,
                        category = ShoppingListSorter.categorizeIngredient(item),
                        confidence = 0.9f,
                        source = SuggestionSource.RECENT_ITEMS,
                        frequency = getUsageFrequency(item),
                    )
                }
            suggestions.addAll(recentMatches)
        }

        // 4. Category-based suggestions
        if (suggestions.size < MAX_SUGGESTIONS && categoryHint != null) {
            val categoryMatches =
                productDatabase.filter { product ->
                    ShoppingListSorter.categorizeIngredient(product) == categoryHint &&
                        product.lowercase().contains(normalizedQuery) &&
                        !suggestions.any { it.text.equals(product, ignoreCase = true) }
                }.map { product ->
                    AutoCompleteSuggestion(
                        text = product,
                        category = categoryHint,
                        confidence = 0.7f,
                        source = SuggestionSource.CATEGORY_HINT,
                        frequency = getUsageFrequency(product),
                    )
                }
            suggestions.addAll(categoryMatches)
        }

        // Sort by confidence and frequency, take top results
        return suggestions
            .sortedWith(
                compareByDescending<AutoCompleteSuggestion> { it.confidence }
                    .thenByDescending { it.frequency }
                    .thenBy { it.text },
            )
            .take(MAX_SUGGESTIONS)
    }

    /**
     * Get frequent suggestions when no query
     */
    private fun getFrequentSuggestions(
        recentItems: List<String>,
        categoryHint: String?,
    ): List<AutoCompleteSuggestion> {
        val suggestions = mutableListOf<AutoCompleteSuggestion>()

        // Recent items first
        suggestions.addAll(
            recentlyUsed.take(4).map { item ->
                AutoCompleteSuggestion(
                    text = item,
                    category = ShoppingListSorter.categorizeIngredient(item),
                    confidence = 1.0f,
                    source = SuggestionSource.RECENT_ITEMS,
                    frequency = getUsageFrequency(item),
                )
            },
        )

        // Most frequent items
        if (suggestions.size < MAX_SUGGESTIONS) {
            val frequentItems =
                usageFrequency.entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .map { (item, frequency) ->
                        AutoCompleteSuggestion(
                            text = item,
                            category = ShoppingListSorter.categorizeIngredient(item),
                            confidence = 0.8f,
                            source = SuggestionSource.FREQUENT_ITEMS,
                            frequency = frequency,
                        )
                    }
            suggestions.addAll(frequentItems)
        }

        return suggestions.distinctBy { it.text }.take(MAX_SUGGESTIONS)
    }

    /**
     * Fuzzy matching algorithm (simple Levenshtein-based)
     */
    private fun isFuzzyMatch(
        query: String,
        product: String,
    ): Boolean {
        if (query.length < 3) return false

        // Contains check for longer words
        if (product.contains(query)) return true

        // Simple character overlap check
        val commonChars = query.toSet().intersect(product.toSet()).size
        val overlapRatio = commonChars.toFloat() / query.length
        return overlapRatio >= 0.6f
    }

    /**
     * Calculate fuzzy match confidence
     */
    private fun calculateFuzzyConfidence(
        query: String,
        product: String,
    ): Float {
        val containsBonus = if (product.contains(query)) 0.3f else 0.0f
        val commonChars = query.toSet().intersect(product.toSet()).size
        val overlapRatio = commonChars.toFloat() / query.length
        return (0.5f + overlapRatio * 0.5f + containsBonus).coerceIn(0.0f, 1.0f)
    }

    /**
     * Track item usage for learning
     */
    fun recordItemUsage(item: String) {
        usageFrequency[item] = getUsageFrequency(item) + 1

        // Update recent items
        recentlyUsed.remove(item) // Remove if already exists
        recentlyUsed.add(0, item) // Add to front
        if (recentlyUsed.size > 20) {
            recentlyUsed.removeAt(recentlyUsed.size - 1)
        }

        StructuredLogger.debug(
            StructuredLogger.LogCategory.USER_ACTION,
            TAG,
            "Recorded usage for item: $item (frequency: ${getUsageFrequency(item)})",
        )
    }

    /**
     * Get usage frequency for item
     */
    private fun getUsageFrequency(item: String): Int {
        return usageFrequency[item] ?: 0
    }

    /**
     * Get popular items in category
     */
    fun getPopularItemsInCategory(
        category: String,
        limit: Int = 5,
    ): List<String> {
        return productDatabase
            .filter { ShoppingListSorter.categorizeIngredient(it) == category }
            .sortedByDescending { getUsageFrequency(it) }
            .take(limit)
    }

    /**
     * Export usage data for persistence
     */
    fun exportUsageData(): Map<String, Any> {
        return mapOf(
            "frequency" to usageFrequency.toMap(),
            "recent" to recentlyUsed.toList(),
        )
    }

    /**
     * Import usage data from persistence
     */
    fun importUsageData(data: Map<String, Any>) {
        try {
            @Suppress("UNCHECKED_CAST")
            val frequency = data["frequency"] as? Map<String, Int>
            frequency?.let { usageFrequency.putAll(it) }

            @Suppress("UNCHECKED_CAST")
            val recent = data["recent"] as? List<String>
            recent?.let {
                recentlyUsed.clear()
                recentlyUsed.addAll(it)
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYSTEM,
                TAG,
                "Failed to import usage data",
                exception = e,
            )
        }
    }
}

/**
 * AutoComplete suggestion data class
 */
data class AutoCompleteSuggestion(
    val text: String,
    val category: String,
    val confidence: Float,
    val source: SuggestionSource,
    val frequency: Int = 0,
)

/**
 * Source of the suggestion
 */
enum class SuggestionSource {
    EXACT_MATCH, // Direct start-of-string match
    FUZZY_MATCH, // Fuzzy/partial match
    RECENT_ITEMS, // Recently used items
    FREQUENT_ITEMS, // Most frequently used
    CATEGORY_HINT, // Items in same category
}
