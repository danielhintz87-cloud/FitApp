package com.example.fitapp.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.ShoppingItemEntity
import com.example.fitapp.data.repo.NutritionRepository
import kotlinx.coroutines.launch

@Composable
fun ShoppingListScreen(repo: NutritionRepository = NutritionRepository(AppDatabase.get(LocalContext.current))) {
    val items by repo.shoppingItems().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp,0.dp,16.dp,96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Text("Einkaufsliste", style = MaterialTheme.typography.titleLarge) }
        items(items, key = { it.id }) { item ->
            ShoppingRow(
                item = item,
                onToggle = { checked -> scope.launch { repo.setItemChecked(item.id, checked) } },
                onDelete = { scope.launch { repo.deleteItem(item.id) } }
            )
        }
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingItemEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(checked = item.checked, onCheckedChange = onToggle)
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall)
                val q = listOfNotNull(item.quantity, item.unit).joinToString(" ")
                if (q.isNotBlank()) Text(q, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onDelete) { Text("Löschen") }
        }
    }
}
