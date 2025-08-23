package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.AppRepository
import com.example.fitapp.ui.design.Spacing

@Composable
fun ShoppingListScreen() {
    val items by AppRepository.shopping.collectAsState()
    var newName by remember { mutableStateOf("") }
    var newQty by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .padding(bottom = 96.dp)
    ) {
        Text("Einkaufsliste", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = Spacing.md))
        Spacer(Modifier.height(Spacing.sm))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Produkt") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = newQty,
                onValueChange = { newQty = it },
                label = { Text("Menge") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        TextButton(onClick = {
            val n = newName.trim()
            if (n.isNotEmpty()) {
                AppRepository.addShoppingItem(n, newQty.trim())
                newName = ""; newQty = ""
            }
        }) { Text("+ Hinzufügen") }

        Spacer(Modifier.height(Spacing.md))
        Divider()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = Spacing.md)
        ) {
            items(items, key = { it.id }) { it ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Checkbox(checked = it.checked, onCheckedChange = { AppRepository.toggleShoppingChecked(it.id) })
                    Text("${it.name} – ${it.quantity}")
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { AppRepository.removeShoppingItem(it.id) }) { Text("Entfernen") }
                }
            }
        }
    }
}
