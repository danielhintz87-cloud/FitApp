package com.example.fitapp.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.ai.AppAi
import com.example.fitapp.data.AppRepository
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

    fun clear() {
        _messages.value = emptyList()
    }

    // ------- Aktionen für KI-Antworten -------
    fun saveAiAsRecipe(msg: ChatMessage, title: String = "Coach‑Rezept") {
        val recipe = SavedRecipe(title = title, markdown = msg.text, tags = listOf("AI"))
        CoachLocalStore.addRecipe(recipe)
    }

    fun toggleFavoriteForRecipe(id: String) = CoachLocalStore.toggleFav(id)

    fun parseIngredientsToShopping(msg: ChatMessage) {
        // Zutaten aus KI-Antwort als Einkaufspositionen parsen und zur Haupt-Einkaufsliste hinzufügen
        val lines = msg.text.lines()
        val items = mutableListOf<Pair<String, String>>()
        for (line in lines) {
            val t = line.trimStart()
            if (t.startsWith("- ") || t.startsWith("• ")) {
                val content = t.drop(2).trim()
                val split = content.split("—", " - ", "–").map { it.trim() }
                val name = split.firstOrNull().orEmpty()
                val quantity = split.getOrNull(1) ?: ""
                if (name.isNotEmpty()) {
                    items += (name to quantity)
                }
            }
        }
        if (items.isNotEmpty()) {
            AppRepository.addShoppingItems(items)
        }
    }
}
