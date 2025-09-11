package com.example.fitapp.ai.ui

import com.example.fitapp.infrastructure.ai.AiErrors

fun AiErrors.toUiState(): AiUiState.Error = when (this) {
    is AiErrors.KeyInvalid -> AiUiState.Error(AiErrorType.KeyInvalid, message)
    is AiErrors.KeyMissing -> AiUiState.Error(AiErrorType.KeyMissing, message)
    is AiErrors.Network -> AiUiState.Error(AiErrorType.Network, message)
    is AiErrors.RateLimit -> AiUiState.Error(AiErrorType.RateLimit, message)
    is AiErrors.ProviderUnavailable -> AiUiState.Error(AiErrorType.ProviderUnavailable, message)
    is AiErrors.Unknown -> AiUiState.Error(AiErrorType.Unknown, message)
}