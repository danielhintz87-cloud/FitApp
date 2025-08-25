package com.example.fitapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun BudgetBar(consumed: Int, target: Int, modifier: Modifier = Modifier) {
    val pct = if (target <= 0) 0f else consumed.toFloat() / target.toFloat()
    val remaining = (target - consumed).coerceAtLeast(0)
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct.coerceIn(0f, 1f))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Verbleibend: $remaining kcal", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.weight(1f))
            Text("$consumed / $target kcal", style = MaterialTheme.typography.labelMedium)
        }
    }
}
