package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(onBack: () -> Unit, versionName: String = "1.8") {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Über FitApp") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("FitApp Version $versionName", style = MaterialTheme.typography.titleMedium)
            Text("Eine offene Fitness Plattform mit AI Coach, Ernährung & Fortschrittsanalyse.")
            Text("Lizenz: MIT", fontWeight = FontWeight.Light)
            Text("© 2025 FitApp Contributors", fontWeight = FontWeight.Light)
        }
    }
}
