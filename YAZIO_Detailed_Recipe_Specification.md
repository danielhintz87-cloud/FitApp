# YAZIO App - Rezept- und Kochfunktionen für FitApp Integration

## Technische Spezifikation für GitHub Copilot Implementation

### 1. Rezeptdatenbank & Content Management

#### 1.1 Rezept-Datenstruktur
```kotlin
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val prepTime: Int, // in Minuten
    val cookTime: Int, // in Minuten
    val servings: Int,
    val difficulty: RecipeDifficulty,
    val categories: List<String>, // z.B. ["Low-Carb", "Vegetarisch", "Dessert"]
    val ingredients: List<Ingredient>,
    val instructions: List<CookingStep>,
    val nutritionInfo: NutritionInfo,
    val tags: List<String>,
    val rating: Float,
    val createdBy: String, // "YAZIO" oder User ID
    val isCreatedByUser: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)

data class Ingredient(
    val id: String,
    val name: String,
    val amount: Float,
    val unit: String, // g, ml, Stück, etc.
    val nutritionPer100g: NutritionInfo,
    val isOptional: Boolean = false
)

data class CookingStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val duration: Int? = null, // Optional: Zeit für diesen Schritt
    val imageUrl: String? = null,
    val temperature: Int? = null, // Optional: Temperatur
    val tips: List<String> = emptyList()
)

enum class RecipeDifficulty { EASY, MEDIUM, HARD }
```

#### 1.2 Content Repository Features
- **Über 2.900+ vorkonfigurierte Rezepte** von Ernährungsexperten
- **Wöchentliche Updates** mit neuen Rezepten
- **Kategorisierung**: Low-Carb, Vegetarisch, Vegan, Desserts, Pizza, Salate
- **Bewertungssystem**: 5-Sterne Bewertung mit Nutzerkommentaren
- **Suchfunktion**: Volltext-Suche, Filter nach Kategorien, Zubereitungszeit, Schwierigkeit

### 2. Unterscheidung: Mahlzeiten vs. Rezepte

#### 2.1 Mahlzeiten (Meals) - Für schnelle Kombinationen
```kotlin
data class Meal(
    val id: String,
    val name: String,
    val foods: List<FoodItem>, // Einfache Lebensmittel-Kombinationen
    val totalCalories: Int,
    val totalNutrition: NutritionInfo,
    val isCreatedByUser: Boolean,
    val createdAt: Date
)

// UI Flow für Mahlzeit-Erstellung:
// 1. Mehrere Lebensmittel in Tagebuch eingeben
// 2. "Edit" Icon → Alle Lebensmittel auswählen
// 3. "Create a meal" → Name eingeben → Speichern
// ODER
// 1. + Button in Mahlzeiten-Kategorie
// 2. "Create New" → "New meal"
// 3. Beschreibung und Lebensmittel hinzufügen
```

#### 2.2 Rezepte (Recipes) - Für detaillierte Kochanweisungen
```kotlin
// Rezepte enthalten:
// - Mehrere Portionen definierbar
// - Detaillierte Zubereitungsschritte
// - Exakte Gewichtsangaben
// - Kochzeit und Schwierigkeit
// - Können nach Portionen ODER Gewicht zum Tagebuch hinzugefügt werden

// UI Flow für Rezept-Erstellung:
// 1. + Button in Mahlzeiten-Kategorie
// 2. Drei Punkte (⋮) oben rechts → "New recipe"
// 3. Mindestens 2 Lebensmittel als Zutaten hinzufügen
// 4. Zubereitungsschritte definieren
// 5. Portionsangaben festlegen
```

### 3. Step-by-Step Cooking Mode (Geführter Kochmodus)

#### 3.1 Cooking Mode UI/UX Spezifikation
```kotlin
class CookingModeActivity : AppCompatActivity() {

    data class CookingSession(
        val recipe: Recipe,
        val currentStepIndex: Int = 0,
        val completedSteps: Set<Int> = emptySet(),
        val startTime: Date,
        val activeTimer: Timer? = null
    )

    // Features des Cooking Mode:
    // - Schritt-für-Schritt Navigation mit großen, gut lesbaren Texten
    // - Integrierte Timer für einzelne Kochschritte
    // - "Zurück" und "Weiter" Navigation
    // - Automatisches Abhaken erledigter Schritte
    // - Anpassbare Textgröße für bessere Lesbarkeit beim Kochen
    // - Bildschirm bleibt aktiv während des Kochens
    // - Portionsanpassung in Echtzeit (Zutatenmengen werden automatisch skaliert)
}
```

#### 3.2 Timer Integration
```kotlin
class CookingTimer {
    fun startStepTimer(duration: Int, stepNumber: Int)
    fun pauseTimer()
    fun resetTimer()
    fun getTimeRemaining(): Int
    // Push-Benachrichtigungen wenn Timer abläuft
}
```

### 4. Smart Grocery Lists (Intelligente Einkaufslisten)

#### 4.1 Grocery List Datenstruktur
```kotlin
data class GroceryList(
    val id: String,
    val name: String,
    val items: List<GroceryItem>,
    val createdAt: Date,
    val updatedAt: Date,
    val isShared: Boolean = false,
    val sharedWith: List<String> = emptyList()
)

data class GroceryItem(
    val ingredientId: String,
    val name: String,
    val amount: Float,
    val unit: String,
    val isChecked: Boolean = false,
    val category: GroceryCategory, // Gemüse, Fleisch, Milchprodukte, etc.
    val estimatedPrice: Float? = null,
    val notes: String? = null
)

enum class GroceryCategory {
    VEGETABLES, FRUITS, MEAT, DAIRY, BAKERY, 
    PANTRY, FROZEN, BEVERAGES, OTHER
}
```

#### 4.2 Smart Features
```kotlin
class SmartGroceryService {

    // Automatische Kategorisierung nach Supermarkt-Layout
    fun categorizeByStoreLayout(items: List<GroceryItem>): Map<GroceryCategory, List<GroceryItem>>

    // Intelligente Mengen-Zusammenfassung
    fun consolidateItems(items: List<GroceryItem>): List<GroceryItem>

    // Rezept zu Einkaufsliste hinzufügen
    fun addRecipeToGroceryList(recipe: Recipe, servings: Int, listId: String)

    // Automatische Duplikat-Erkennung
    fun mergeDuplicateItems(items: List<GroceryItem>): List<GroceryItem>

    // Integration mit lokalen Supermarkt-APIs für Preise
    fun updatePriceEstimates(items: List<GroceryItem>, storeLocation: String)
}
```

#### 4.3 UI Flow für Einkaufslisten
```kotlin
// Navigation zu Einkaufsliste:
// 1. PRO Feature: Profil → Einstellungen → "Grocery Lists"
// 2. Oder: Bei Rezept → "Add to Grocery List" Button
// 3. Oder: Drei-Punkte-Menü im Rezept → "Add ingredients to shopping list"

// Einkaufsliste Funktionen:
// - Mehrere Listen parallel verwalten
// - Checkboxen zum Abhaken gekaufter Items
// - Kategorische Sortierung (wie Supermarkt-Gang-Layout)
// - Mengen automatisch für mehrere Rezepte zusammenfassen
// - Export als PDF oder Sharing-Funktion
```

### 5. Erweiterte Filter- und Suchfunktionen

#### 5.1 Rezept-Filter Interface
```kotlin
data class RecipeFilters(
    val categories: List<String> = emptyList(), // Low-Carb, Vegetarian, Vegan
    val maxPrepTime: Int? = null,
    val maxCookTime: Int? = null,
    val difficulty: RecipeDifficulty? = null,
    val maxCaloriesPerServing: Int? = null,
    val allergens: List<String> = emptyList(), // Gluten-free, Dairy-free, etc.
    val availableIngredients: List<String> = emptyList(), // "What can I make with..."
    val rating: Float? = null // Mindest-Bewertung
)

class RecipeSearchService {
    fun searchRecipes(
        query: String,
        filters: RecipeFilters,
        sortBy: RecipeSortOption = RecipeSortOption.RELEVANCE
    ): List<Recipe>
}

enum class RecipeSortOption {
    RELEVANCE, PREP_TIME, RATING, NEWEST, CALORIES
}
```

#### 5.2 UI Navigation Structure
```kotlin
// Rezept-Tab Struktur:
// - "YAZIO" Tab: Offizielle Rezepte (PRO Feature)
// - "Created" Tab: Selbst erstellte Rezepte
// - "Favorites" Tab: Favorisierte Rezepte
// 
// Search Bar mit Filtern:
// - Suchleiste oben
// - Filter-Chips direkt darunter
// - "Created by me" Filter für eigene Rezepte
```

### 6. AI Photo Tracking Integration (Neueste YAZIO Feature)

#### 6.1 AI Food Recognition
```kotlin
class AIFoodRecognitionService {

    data class FoodAnalysisResult(
        val recognizedFoods: List<RecognizedFood>,
        val confidence: Float,
        val portionEstimate: Float,
        val nutritionEstimate: NutritionInfo,
        val suggestedRecipes: List<Recipe> = emptyList()
    )

    data class RecognizedFood(
        val name: String,
        val confidence: Float,
        val boundingBox: Rect?,
        val estimatedWeight: Float,
        val nutritionPer100g: NutritionInfo
    )

    suspend fun analyzeFood(imageUri: Uri): FoodAnalysisResult

    // Features:
    // - Foto von Mahlzeit machen → Automatische Erkennung von Zutaten und Portionsgrößen
    // - Kalorien und Nährstoffe ohne manuelles Messen berechnen
    // - Funktioniert für Takeaway, Restaurant-Essen und selbstgekochte Gerichte
    // - Integration in Ernährungstagebuch mit einem Klick
}
```

### 7. Integration mit Ernährungstagebuch

#### 7.1 Rezept zum Tagebuch hinzufügen
```kotlin
class DiaryIntegrationService {

    // Rezept zum Tagebuch hinzufügen
    fun addRecipeToMeal(
        recipeId: String,
        mealCategory: MealCategory, // Breakfast, Lunch, Dinner, Snacks
        servings: Float, // Kann Dezimalwerte haben (0.5 Portionen)
        date: Date = Date.today()
    )

    // Automatische Nährstoffberechnung
    fun calculateNutritionForRecipe(recipe: Recipe, servings: Float): NutritionInfo

    // Custom Portionsangaben
    fun addRecipeByWeight(
        recipeId: String,
        mealCategory: MealCategory,
        weightInGrams: Float,
        date: Date = Date.today()
    )
}
```

### 8. Benutzer-erstellte Rezepte (Custom Recipes)

#### 8.1 Rezept-Editor Interface
```kotlin
class RecipeEditorViewModel {

    val recipeDraft = MutableLiveData<Recipe>()
    val ingredients = MutableLiveData<List<Ingredient>>()
    val cookingSteps = MutableLiveData<List<CookingStep>>()

    // Validierung:
    // - Mindestens 2 Zutaten erforderlich
    // - Titel darf nicht leer sein
    // - Portionsangabe muss > 0 sein

    fun saveRecipe(): Boolean
    fun addIngredient(ingredient: Ingredient)
    fun removeIngredient(index: Int)
    fun addCookingStep(step: CookingStep)
    fun reorderSteps(fromIndex: Int, toIndex: Int)
    fun calculateTotalNutrition(): NutritionInfo
}
```

#### 8.2 Local Storage (Wichtiger Hinweis)
```kotlin
// WICHTIG: YAZIO speichert benutzererstellte Rezepte nur LOKAL
// → Keine Cloud-Synchronisation zwischen Geräten
// → FitApp sollte Cloud-Sync für bessere UX implementieren

@Entity(tableName = "user_recipes")
data class UserRecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    // ... weitere Felder
    val isLocalOnly: Boolean = false // Für möglichen Cloud-Sync
)
```

### 9. PRO Features vs. Free Features

#### 9.1 Feature-Matrix
```kotlin
object RecipeFeatureMatrix {

    // FREE Features:
    // - Eigene Rezepte erstellen und verwalten
    // - Eigene Mahlzeiten erstellen
    // - Basis-Suchfunktion
    // - Rezepte zu Tagebuch hinzufügen

    // PRO Features (YAZIO PRO erforderlich):
    val proFeatures = listOf(
        "access_official_recipe_database", // 2.900+ Rezepte
        "advanced_recipe_filters",
        "grocery_list_creation",
        "detailed_nutrition_analysis", // Fettsäuren, Zucker, Ballaststoffe, Salz
        "recipe_meal_planning",
        "step_by_step_cooking_mode",
        "weekly_recipe_updates"
    )
}

// Implementierung der Feature-Flags
class FeatureManager {
    fun isProFeatureAvailable(feature: String): Boolean
    fun showUpgradePrompt(feature: String)
}
```

### 10. Performance und Optimierung

#### 10.1 Datenbank-Optimierungen
```kotlin
// Room Database Entities
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val shortDescription: String,
    // ... weitere Felder
)

// Indizes für bessere Performance
@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE title LIKE :search OR shortDescription LIKE :search")
    suspend fun searchRecipes(search: String): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE categories LIKE :category ORDER BY rating DESC")
    suspend fun getRecipesByCategory(category: String): List<RecipeEntity>

    // Favoriten-Query
    @Query("""
        SELECT r.* FROM recipes r 
        INNER JOIN user_favorites f ON r.id = f.recipe_id 
        WHERE f.user_id = :userId
    """)
    suspend fun getFavoriteRecipes(userId: String): List<RecipeEntity>
}
```

#### 10.2 Image Loading & Caching
```kotlin
// Glide/Coil für Recipe Images
class RecipeImageManager {
    fun loadRecipeImage(imageUrl: String, imageView: ImageView)
    fun preloadImages(recipes: List<Recipe>) // Für bessere UX
    fun clearImageCache()
}
```

### 11. Analytics und User Tracking

#### 11.1 Recipe Analytics
```kotlin
data class RecipeAnalytics(
    val recipeViewCount: Int,
    val cookingModeUsageCount: Int,
    val averageRating: Float,
    val completionRate: Float, // Wie oft wird das Rezept zu Ende gekocht
    val favoriteRate: Float,
    val groceryListAdditions: Int
)

class RecipeAnalyticsService {
    fun trackRecipeView(recipeId: String)
    fun trackCookingModeStart(recipeId: String)
    fun trackCookingModeComplete(recipeId: String)
    fun trackRecipeRating(recipeId: String, rating: Float)
    fun trackGroceryListAddition(recipeId: String)
}
```

### 12. Integration Testing Checklist

#### 12.1 Kritische User Flows zum Testen
- [ ] Rezept erstellen mit mindestens 2 Zutaten
- [ ] Rezept zum Ernährungstagebuch hinzufügen (verschiedene Portionsgrößen)
- [ ] Step-by-Step Cooking Mode durchlaufen
- [ ] Einkaufsliste aus Rezept generieren
- [ ] Filter und Suchfunktion in verschiedenen Kombinationen
- [ ] Mahlzeit vs. Rezept Erstellung und Unterschiede
- [ ] PRO Feature Beschränkungen für Free Users
- [ ] AI Photo Recognition (falls implementiert)
- [ ] Offline-Funktionalität für gespeicherte Rezepte
- [ ] Performance bei großen Rezept-Datenbanken

### 13. API Integration Points

#### 13.1 Externe Services
```kotlin
// Nutrition API für automatische Nährstoffberechnung
interface NutritionApiService {
    suspend fun getNutritionData(foodName: String): NutritionInfo
    suspend fun analyzeRecipe(ingredients: List<Ingredient>): NutritionInfo
}

// Grocery Price API
interface GroceryPriceService {
    suspend fun getPriceEstimates(items: List<String>, location: String): List<PriceEstimate>
}

// AI Food Recognition API
interface FoodRecognitionService {
    suspend fun analyzeImage(imageData: ByteArray): FoodAnalysisResult
}
```

### 14. Accessibility und Usability

#### 14.1 Accessibility Features
- Große, gut lesbare Schriften im Cooking Mode
- VoiceOver/TalkBack Unterstützung für alle UI Elemente
- Farbkontraste gemäß WCAG Guidelines
- Alternative Texte für alle Bilder
- Tastatur-Navigation für alle Funktionen

#### 14.2 Internationalization (i18n)
- Unterstützung für 20+ Sprachen (wie YAZIO)
- Lokalisierte Lebensmittel-Datenbanken
- Kulturell angepasste Maßeinheiten (cups vs. ml)
- Lokale Ernährungsrichtlinien berücksichtigen

## Implementation Priorität

### Phase 1 (MVP):
1. Basis Rezept-Datenstruktur
2. Eigene Rezepte erstellen/bearbeiten
3. Rezept zu Tagebuch hinzufügen
4. Basis-Suchfunktion

### Phase 2:
1. Step-by-Step Cooking Mode
2. Einkaufslisten-Funktionalität
3. Erweiterte Filter
4. Meal vs. Recipe Unterscheidung

### Phase 3:
1. AI Photo Recognition
2. PRO Feature Implementation
3. Analytics Integration
4. Performance Optimierungen

Diese Spezifikation basiert auf der detaillierten Analyse der YAZIO App und sollte GitHub Copilot alle notwendigen Informationen für eine vollständige Implementation der Rezept- und Kochfunktionen bieten.

