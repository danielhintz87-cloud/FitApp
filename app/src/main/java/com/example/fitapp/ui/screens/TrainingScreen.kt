package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fitapp.ui.TrainingSetupScreen
import com.example.fitapp.ui.design.Spacing

@Composable
fun TrainingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Spacing.md)
    ) {
        Text(
            "Training",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = Spacing.lg)
        )
        Spacer(Modifier.height(Spacing.md))
        // Bestehende Setup-Ansicht für Trainingsplan einbinden
        TrainingSetupScreen()
    }
}
