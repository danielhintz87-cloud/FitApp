package com.example.fitapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fitapp.ui.design.Elev
import com.example.fitapp.ui.design.Radii
import com.example.fitapp.ui.design.Spacing

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = Spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String? = null,
    subtitle: String? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.lg),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (title != null) {
        SectionHeader(title = title, subtitle = subtitle, modifier = Modifier.padding(horizontal = Spacing.lg))
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        elevation = CardDefaults.cardElevation(defaultElevation = Elev.mid),
        shape = CardDefaults.shape.copy(all = Radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
