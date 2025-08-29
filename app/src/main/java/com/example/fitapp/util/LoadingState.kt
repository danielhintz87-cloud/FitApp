package com.example.fitapp.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Comprehensive loading state management for FitApp
 * Provides consistent loading indicators, error states, and empty states
 */

/**
 * Sealed class representing different UI states
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable, val message: String = ErrorHandling.getUserFriendlyMessage(exception)) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

/**
 * Composable for handling different UI states with consistent design
 */
@Composable
fun <T> HandleUiState(
    uiState: UiState<T>,
    onRetry: (() -> Unit)? = null,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    emptyContent: @Composable () -> Unit = { DefaultEmptyContent() },
    errorContent: @Composable (String, (() -> Unit)?) -> Unit = { message, retry -> DefaultErrorContent(message, retry) },
    content: @Composable (T) -> Unit
) {
    when (uiState) {
        is UiState.Loading -> loadingContent()
        is UiState.Success -> content(uiState.data)
        is UiState.Error -> errorContent(uiState.message, onRetry)
        is UiState.Empty -> emptyContent()
    }
}

/**
 * Default loading content with progress indicator
 */
@Composable
fun DefaultLoadingContent(
    message: String = "Lädt...",
    showProgress: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Skeleton loading content for lists
 */
@Composable
fun SkeletonLoadingContent(
    itemCount: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            SkeletonItem()
        }
    }
}

/**
 * Individual skeleton item
 */
@Composable
private fun SkeletonItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                
                // Subtitle placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}

/**
 * Shimmer effect modifier for skeleton loading
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    return this.then(
        Modifier.background(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp)
        )
    )
}

/**
 * Default error content with retry button
 */
@Composable
fun DefaultErrorContent(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Erneut versuchen")
                }
            }
        }
    }
}

/**
 * Default empty content
 */
@Composable
fun DefaultEmptyContent(
    message: String = "Keine Daten verfügbar",
    icon: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            icon?.invoke()
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            action?.invoke()
        }
    }
}

/**
 * Inline loading indicator for buttons
 */
@Composable
fun InlineLoadingIndicator(
    text: String = "Lädt...",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp
        )
        Text(text)
    }
}

/**
 * Progress indicator with percentage
 */
@Composable
fun ProgressIndicatorWithPercentage(
    progress: Float,
    text: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Utility function to create UiState from Result
 */
fun <T> Result<T>.toUiState(): UiState<T> {
    return fold(
        onSuccess = { data ->
            if (data == null) UiState.Empty else UiState.Success(data)
        },
        onFailure = { exception ->
            UiState.Error(exception)
        }
    )
}

/**
 * Utility function to check if data is empty for collections
 */
fun <T> List<T>?.toUiState(): UiState<List<T>> {
    return when {
        this == null -> UiState.Empty
        this.isEmpty() -> UiState.Empty
        else -> UiState.Success(this)
    }
}