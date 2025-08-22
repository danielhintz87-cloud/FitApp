package com.example.fitapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.fitapp.ui.design.Spacing

@Composable
fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    filled: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp)
    val bg = if (filled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (filled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier,
        color = bg,
        shape = shape,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 36.dp)
                .padding(horizontal = Spacing.md, vertical = 6.dp)
        ) {
            Text("$label: ", color = fg, style = MaterialTheme.typography.labelMedium)
            Text(value, color = fg, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val bg = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = shape,
        color = bg,
        border = border
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(horizontal = Spacing.md, vertical = 6.dp)
        )
    }
}
