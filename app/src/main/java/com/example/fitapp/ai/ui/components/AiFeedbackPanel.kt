package com.example.fitapp.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitapp.ai.ui.AiUiState
import com.example.fitapp.ai.ui.AiErrorType

@Composable
fun AiFeedbackPanel(
    state: AiUiState,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (state) {
        AiUiState.Idle -> {}
        AiUiState.Loading -> LoadingIndicator(modifier)
        is AiUiState.Success<*> -> {}
        is AiUiState.Unavailable -> InfoBox(
            title = "AI nicht verfügbar",
            body = state.message ?: "Funktion derzeit nicht nutzbar.",
            tone = InfoTone.Warning,
            modifier = modifier
        )
        is AiUiState.Error -> {
            val (title, tone) = when (state.type) {
                AiErrorType.KeyMissing -> "API Key fehlt" to InfoTone.Warning
                AiErrorType.KeyInvalid -> "API Key ungültig" to InfoTone.Error
                AiErrorType.Network -> "Netzwerkproblem" to InfoTone.Error
                AiErrorType.RateLimit -> "Rate Limit erreicht" to InfoTone.Warning
                AiErrorType.ProviderUnavailable -> "Provider nicht erreichbar" to InfoTone.Error
                AiErrorType.Unknown -> "Unbekannter Fehler" to InfoTone.Error
            }
            InfoBox(
                title = title,
                body = state.message ?: "Bitte später erneut versuchen.",
                tone = tone,
                actionLabel = if (onRetry != null) "Erneut" else null,
                onAction = onRetry,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

enum class InfoTone { Info, Warning, Error }

@Composable
private fun InfoBox(
    title: String,
    body: String,
    tone: InfoTone,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val color = when (tone) {
        InfoTone.Info -> MaterialTheme.colorScheme.primaryContainer
        InfoTone.Warning -> Color(0xFFFFF4CC)
        InfoTone.Error -> MaterialTheme.colorScheme.errorContainer
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(text = body, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}