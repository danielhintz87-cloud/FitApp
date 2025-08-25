package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressScreen(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Progress", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Card {
            Text(
                "Deine Fortschritte und Statistiken werden hier angezeigt.",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}