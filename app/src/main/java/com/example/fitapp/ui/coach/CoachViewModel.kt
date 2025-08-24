package com.example.fitapp.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoachViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    fun send(text: String) {
        if (text.isBlank() || _busy.value) return
        val me = ChatMessage(author = Author.Me, text = text)
        _messages.value = _messages.value + me
        askAi(text)
    }

    private fun askAi(userText: String) {
        _busy.value = true
        viewModelScope.launch {
            val reply = AppAi.chatOnce(userText)
            _messages.value = _messages.value + ChatMessage(author = Author.Ai, text = reply)
            _busy.value = false
        }
    }

    fun clear() { _messages.value = emptyList() }

    // ------- Actions auf AI-Antworten -------
    fun saveAiAsRecipe(msg: ChatMessage, title: String = "Coach‑Rezept") {
        val rec = SavedRecipe(title = title, markdown = msg.text, tags = listOf("AI"))
        CoachLocalStore.addRecipe(rec)
    }

    fun toggleFavoriteForRecipe(id: String) = CoachLocalStore.toggleFav(id)

    fun parseIngredientsToShopping(msg: ChatMessage) {
        // Super simpler Parser: sucht Bullet‑Zeilen („- “, „• “) oder format „Name — Menge“
        val lines = msg.text.lines()
        val items = mutableListOf<ShoppingItem>()
        for (l in lines) {
            val t = l.trimStart()
            val isBullet = t.startsWith("- ") || t.startsWith("• ")
            if (isBullet) {
                val content = t.drop(2).trim()
                val split = content.split("—", " - ", "–").map { it.trim() }
                val name = split.firstOrNull().orEmpty()
                val amount = split.getOrNull(1)
                if (name.isNotEmpty()) items += ShoppingItem(name = name, amount = amount)
            }
        }
        if (items.isNotEmpty()) CoachLocalStore.addShoppingItems(items)
    }
}

