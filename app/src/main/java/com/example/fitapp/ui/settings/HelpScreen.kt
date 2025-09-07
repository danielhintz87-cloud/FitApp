package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hilfe & Support") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("FAQ (Auszug)", style = MaterialTheme.typography.titleMedium)
            Text("• Wie aktiviere ich Health Connect? -> Einstellungen > Health Connect\n" +
                 "• Wie wechsle ich das Pose Modell? -> AI Einstellungen (geplant)\n" +
                 "• Datenverlust? -> Backup / Cloud Sync Einstellungen.")
            Text("Weitere Hilfe demnächst.", fontWeight = FontWeight.Light)
        }
    }
}
