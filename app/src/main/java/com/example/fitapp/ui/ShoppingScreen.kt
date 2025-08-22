package com.example.fitapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.fitapp.data.AppRepository
import com.example.fitapp.ui.components.EmptyState
import com.example.fitapp.ui.components.SectionCard
import com.example.fitapp.ui.design.Spacing

@Composable
fun ShoppingListScreen() {
    val items by AppRepository.shopping.collectAsState()

    if (items.isEmpty()) {
        EmptyState(title = "Einkaufsliste ist leer", message = "Füge Zutaten über Rezepte hinzu oder lege manuell los.")
        return
    }

    SectionCard(title = "Einkauf") {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Checkbox(checked = item.checked, onCheckedChange = { AppRepository.toggleShoppingChecked(item.id) })
                    Text("${item.name}  ${if (item.quantity.isNotBlank()) "– ${item.quantity}" else ""}")
                }
                IconButton(onClick = { AppRepository.removeShoppingItem(item.id) }) {
                    androidx.compose.material3.Icon(Icons.Default.Delete, contentDescription = "Löschen")
                }
            }
        }
    }
}
