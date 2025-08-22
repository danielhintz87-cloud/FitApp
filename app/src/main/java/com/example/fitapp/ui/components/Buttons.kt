package com.example.fitapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitapp.ui.design.Spacing

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, ButtonDefaults.outlinedButtonBorder(true).brush),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Text(label)
    }
}

@Composable
fun InlineActions(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(top = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        if (secondaryLabel != null && onSecondary != null) {
            SecondaryButton(
                label = secondaryLabel,
                onClick = onSecondary,
                modifier = Modifier.weight(1f)
            )
        }
        PrimaryButton(
            label = primaryLabel,
            onClick = onPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
