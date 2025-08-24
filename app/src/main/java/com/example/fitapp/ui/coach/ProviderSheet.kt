package com.example.fitapp.ui.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.AppAi

@Composable
fun ProviderSheet(
    onClose: () -> Unit,
    onChoose: (AppAi.Provider) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(Modifier.padding(vertical = 8.dp)) {
            AppAi.Provider.entries.forEachIndexed { i, p ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onChoose(p)
                            onClose()
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(p.display, style = MaterialTheme.typography.titleMedium)
                    val hint = when (p) {
                        AppAi.Provider.OpenAI -> "Empfohlen – nutzt deinen OPENAI_API_KEY"
                        AppAi.Provider.Gemini -> "Vorbereitet – Key/Endpoint später ergänzen"
                        AppAi.Provider.Perplexity -> "Vorbereitet – Key/Endpoint später ergänzen"
                        AppAi.Provider.Copilot -> "Vorbereitet – Key/Endpoint später ergänzen"
                        AppAi.Provider.DeepSeek -> "Vorbereitet – Key/Endpoint später ergänzen"
                    }
                    Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (i < AppAi.Provider.entries.size - 1) Divider()
            }
        }
    }
}

