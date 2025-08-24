package com.example.fitapp.ui.coach

import java.time.Instant
import java.util.UUID

sealed class Author { object Me : Author(); object Ai : Author() }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val author: Author,
    val text: String,
    val createdAt: Instant = Instant.now()
)

/** Sehr einfache lokale Modelle – kollidieren nicht mit deinen bestehenden Datenklassen. */
data class SavedRecipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val markdown: String,
    val tags: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
    var favorite: Boolean = false
)

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: String? = null,
    val createdAt: Instant = Instant.now(),
    var checked: Boolean = false
)

/** Lokales In‑Memory-„Repo“ – bewusst simpel für Single‑User/ohne Cloud. */
object CoachLocalStore {
    private val _recipes = mutableListOf<SavedRecipe>()
    private val _shopping = mutableListOf<ShoppingItem>()

    val recipes: List<SavedRecipe> get() = _recipes
    val shopping: List<ShoppingItem> get() = _shopping

    fun addRecipe(r: SavedRecipe) { _recipes.add(0, r) }
    fun toggleFav(id: String) { _recipes.find { it.id == id }?.let { it.favorite = !it.favorite } }

    fun addShoppingItems(items: List<ShoppingItem>) { _shopping.addAll(0, items) }
}

