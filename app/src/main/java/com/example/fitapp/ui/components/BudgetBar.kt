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
import kotlin.math.max

@Composable
fun BudgetBar(consumed: Int, target: Int, modifier: Modifier = Modifier) {
    val pct = if (target > 0) consumed.toFloat() / target.toFloat() else 0f
    val remaining = max(0, target - consumed)
    Column(modifier.fillMaxWidth()) {
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
                    .fillMaxWidth(pct.coerceIn(0f,1f))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Verbleibend: $remaining kcal", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.weight(1f))
            Text("$consumed / $target kcal", style = MaterialTheme.typography.labelMedium)
        }
    }
}
