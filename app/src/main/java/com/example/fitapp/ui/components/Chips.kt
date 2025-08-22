// app/src/main/java/com/example/fitapp/ui/components/Chips.kt
package com.example.fitapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * App-eigener FilterChip (bewusst nicht der M3-FilterChip),
 * um Signatur-Kollisionen zu vermeiden und Größen zu erzwingen.
 */
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val colors = MaterialTheme.colorScheme
    val bg = if (selected) colors.secondaryContainer else colors.surface
    val borderColor = if (selected) colors.secondaryContainer else colors.outlineVariant
    val txt = if (selected) colors.onSecondaryContainer else colors.onSurface

    Surface(
        modifier = modifier
            .heightIn(min = 36.dp)                     // Chip-Höhe
            .clickable { onClick() },
        shape = shape,
        color = bg,
        contentColor = txt,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 6.dp))) {
            Text(
                text = text,
                maxLines = 1,                          // kein Zeilenumbruch im Chip
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
