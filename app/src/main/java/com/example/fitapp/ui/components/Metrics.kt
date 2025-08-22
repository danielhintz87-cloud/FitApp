package com.example.fitapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MetricChip(
    label: String,
    value: String,
    filled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val bg = if (filled) cs.primaryContainer else cs.surface
    val fg = if (filled) cs.onPrimaryContainer else cs.onSurface
    val border = if (filled) cs.primaryContainer else cs.outlineVariant

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        contentColor = fg,
        border = BorderStroke(1.dp, border),
        modifier = modifier.heightIn(min = 36.dp)
    ) {
        Row(Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 6.dp))) {
            Text("$label: $value", style = MaterialTheme.typography.labelLarge)
        }
    }
}
